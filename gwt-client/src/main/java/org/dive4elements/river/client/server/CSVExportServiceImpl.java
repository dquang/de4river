/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.List;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import au.com.bytecode.opencsv.CSVReader;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.client.services.CSVExportService;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class CSVExportServiceImpl
extends      RemoteServiceServlet
implements   CSVExportService
{
    private static final Logger log =
        LogManager.getLogger(CSVExportServiceImpl.class);


    public static final String ERROR_NO_EXPORT_FOUND =
        "error_no_export_found";

    public List<String[]> getCSV(
        String locale,
        String uuid,
        String name)
    throws ServerException
    {
        log.info("CSVExportServiceImpl.getCSV");

        String url  = getServletContext().getInitParameter("server-url");

        Document requestDoc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            requestDoc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = ec.create("action");
        ec.addAttr(action, "type", "csv", true);
        ec.addAttr(action, "name", name, true);

        requestDoc.appendChild(action);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            InputStream in = client.collectionOut(requestDoc, uuid, "export");
            Reader reader       = new InputStreamReader (in, "UTF-8");
            CSVReader csvReader = new CSVReader (reader, ';');

            List<String[]> lines = new ArrayList<String[]>();
            String[]       line  = null;

            while ((line = csvReader.readNext()) != null) {
                if (line != null) {
                    if (!line[0].startsWith("#") && line.length > 0) {
                        if (line[0].replace("'", "").length() > 0) {
                            lines.add(line);
                        }
                    }
                }
            }

            return lines;
        }
        catch (IOException ce) {
            log.error(ce.getLocalizedMessage());
        }

        throw new ServerException(ERROR_NO_EXPORT_FOUND);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
