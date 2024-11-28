/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.importer.parsers.AnnotationClassifier;
import org.dive4elements.river.importer.parsers.BundesWasserStrassenParser;
import org.dive4elements.river.importer.parsers.InfoGewParser;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Transaction;
import org.hibernate.HibernateException;

import org.w3c.dom.Document;

import org.dive4elements.river.backend.utils.StringUtil;

/** Data Importer. Further processing happens per-river. */
public class Importer
{
    /** Private log. */
    private static final Logger log = LogManager.getLogger(Importer.class);

    private static final String BWASTR_ID_CSV_FILE = "BWASTR_ID.csv";

    protected List<ImportRiver> rivers;

    public Importer() {
    }

    public Importer(List<ImportRiver> rivers) {
        this.rivers = rivers;
    }

    public List<ImportRiver> getRivers() {
        return rivers;
    }

    public void setRivers(List<ImportRiver> rivers) {
        this.rivers = rivers;
    }

    /** Write rivers and their dependencies/dependants to db. */
    public void writeRivers() {
        log.debug("write rivers started");

        for (ImportRiver river: rivers) {
            log.debug("writing river '" + river.getName() + "'");
            river.storeDependencies();
            ImporterSession.getInstance().getDatabaseSession().flush();
        }

        log.debug("write rivers finished");
    }

    public void writeToDatabase() {

        Transaction tx = null;

        try {
            tx = ImporterSession.getInstance()
                .getDatabaseSession().beginTransaction();

            try {
                writeRivers();
            }
            catch (HibernateException he) {
                Throwable t = he.getCause();
                while (t instanceof SQLException) {
                    SQLException sqle = (SQLException) t;
                    log.error("SQL exeception chain:", sqle);
                    t = sqle.getNextException();
                }
                throw he;
            }

            tx.commit();
        }
        catch (RuntimeException re) {
            if (tx != null) {
                tx.rollback();
            }
            throw re;
        }
    }

    public static AnnotationClassifier getAnnotationClassifier() {
        String annotationTypes = Config.INSTANCE.getAnnotationTypes();

        if (annotationTypes == null) {
            log.info("no annotation types file configured.");
            return null;
        }

        File file = new File(annotationTypes);

        log.info("use annotation types file '" + file + "'");

        if (!(file.isFile() && file.canRead())) {
            log.warn("annotation type file '" + file + "' is not readable.");
            return null;
        }

        Document rules = XMLUtils.parseDocument(file, false, null);

        if (rules == null) {
            log.warn("cannot parse annotation types file.");
            return null;
        }

        return new AnnotationClassifier(rules);
    }


    /** Starting point for importing river data. */
    public static void main(String [] args) {

        // Take paths of GEW files from arguments and system property
        List<String> gews = new ArrayList<>(Arrays.asList(args));
        String gewProperty = Config.INSTANCE.getInfoGewFile();
        if (gewProperty != null && gewProperty.length() > 0) {
            gews.add(gewProperty);
        }

        if (gews.isEmpty()) {
            log.error("No info gew files given");
            System.exit(1);
        }

        InfoGewParser infoGewParser = new InfoGewParser(
            getAnnotationClassifier());

        log.info("Start parsing rivers...");

        File bwastrFile = null;

        for (String gew: gews) {
            log.info("parsing info gew file: " + gew);
            File gewFile = new File(gew);
            if (bwastrFile == null) {
                bwastrFile = new File(
                    gewFile.getParentFile(), BWASTR_ID_CSV_FILE);
            }
            try {
                infoGewParser.parse(gewFile);
            }
            catch (IOException ioe) {
                log.error("error while parsing gew: " + gew, ioe);
                System.exit(1);
            }
        }

        // Look for official numbers.
        BundesWasserStrassenParser bwastrIdParser =
            new BundesWasserStrassenParser();

        // Read bwastFile (river-dir + BWASTR_ID_CSV_FILE).
        if (!Config.INSTANCE.skipBWASTR()) {
            try{
                bwastrIdParser.parse(bwastrFile);
                HashMap<String,Long> map = bwastrIdParser.getMap();

                // Now link rivers with official numbers.
                for(ImportRiver river: infoGewParser.getRivers()) {
                    for(Map.Entry<String, Long> entry: map.entrySet()) {
                        if (StringUtil.containsIgnoreCase(
                                river.getName(), entry.getKey())) {
                            river.setOfficialNumber(entry.getValue());
                            log.debug(river.getName()
                                + " is mapped to bwastr " + entry.getValue());
                        }
                    }
                }
            } catch (IOException ioe) {
                log.warn("BWASTR-file could not be loaded.");
            }
        }
        else {
            log.debug("skip reading BWASTR_ID.csv");
        }

        if (!Config.INSTANCE.dryRun()) {
            new Importer(infoGewParser.getRivers()).writeToDatabase();
        }
        else {
            log.info("Dry run, not writing to database.");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
