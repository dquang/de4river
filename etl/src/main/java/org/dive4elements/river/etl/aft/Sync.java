/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import org.dive4elements.river.etl.db.ConnectionBuilder;

import org.dive4elements.river.etl.utils.XML;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.SQLException;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Sync
{
    private static Logger log = LogManager.getLogger(Sync.class);

    public static final String FLYS = "flys";
    public static final String AFT  = "aft";

    public static final String XPATH_DIPS   = "/sync/dips/file/text()";
    public static final String XPATH_REPAIR = "/sync/dips/repair/text()";

    public static final String XPATH_NOTIFICATIONS =
        "/sync/notifications/notification";

    public static final String CONFIG_FILE =
        System.getProperty("config.file", "config.xml");

    public static void sendNotifications(Document config) {
        NodeList notifications = (NodeList)XML.xpath(
            config, XPATH_NOTIFICATIONS, XPathConstants.NODESET, null, null);

        if (notifications == null) {
            return;
        }

        for (int i = 0, N = notifications.getLength(); i < N; ++i) {
            Element notification = (Element)notifications.item(i);
            String urlString = notification.getAttribute("url");

            URL url;
            try {
                url = new URL(urlString);
            }
            catch (MalformedURLException mfue) {
                log.warn("NOTIFY: Invalid URL '" + urlString + "'. Ignored.", mfue);
                continue;
            }

            Notification n = new Notification(notification);

            Document result = n.sendPOST(url);

            if (result != null) {
                log.info("Send notifcation to '" + urlString + "'.");
                log.info(XML.toString(result));
            }
        }
    }

    public static void main(String [] args) {

        File configFile = new File(CONFIG_FILE);

        if (!configFile.isFile() || !configFile.canRead()) {
            log.error("cannot read config file");
            System.exit(1);
        }

        Document config = XML.parseDocument(configFile, Boolean.FALSE);

        if (config == null) {
            log.error("Cannot load config file.");
            System.exit(1);
        }

        String dipsF = (String)XML.xpath(
            config, XPATH_DIPS, XPathConstants.STRING, null, null);

        if (dipsF == null || dipsF.length() == 0) {
            log.error("Cannot find path to DIPS XML in config.");
            System.exit(1);
        }

        File dipsFile = new File(dipsF);

        if (!dipsFile.isFile() || !dipsFile.canRead()) {
            log.error("DIPS: Cannot find '" + dipsF + "'.");
            System.exit(1);
        }

        Document dips = XML.parseDocument(dipsFile, Boolean.FALSE);

        if (dips == null) {
            log.error("DIPS: Cannot load DIPS document.");
            System.exit(1);
        }

        String repairF = (String)XML.xpath(
            config, XPATH_REPAIR, XPathConstants.STRING, null, null);

        if (repairF != null && repairF.length() > 0) {
            File repairFile = new File(repairF);
            if (!repairFile.isFile() || !repairFile.canRead()) {
                log.warn("REPAIR: Cannot open DIPS repair XSLT file.");
            }
            else {
                Document fixed = XML.transform(dips, repairFile);
                if (fixed == null) {
                    log.warn("REPAIR: Fixing DIPS failed.");
                }
                else {
                    dips = fixed;
                }
            }
        }

        int exitCode = 0;

        ConnectionBuilder aftConnectionBuilder =
            new ConnectionBuilder(AFT, config);

        ConnectionBuilder flysConnectionBuilder =
            new ConnectionBuilder(FLYS, config);

        SyncContext syncContext = null;

        boolean modified = false;
        try {
            syncContext = new SyncContext(
                aftConnectionBuilder.getConnectedStatements(),
                flysConnectionBuilder.getConnectedStatements(),
                dips);
            syncContext.init();
            Rivers rivers = new Rivers();
            modified = rivers.sync(syncContext);
        }
        catch (SQLException sqle) {
            log.error("SYNC: Syncing failed.", sqle);
            exitCode = 1;
        }
        finally {
            if (syncContext != null) {
                syncContext.close();
            }
        }

        if (modified) {
            log.info("Modifications found.");
            sendNotifications(config);
        }
        else {
            log.info("No modifications found.");
        }

        if (exitCode != 0) {
            System.exit(1);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
