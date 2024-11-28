/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.FileTools;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.utils.RiverUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service that receives XML-packed Shapefile bundles from the client.
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FileUploadService extends D4EService {

    /** The log used in this service.*/
    private static Logger log = LogManager.getLogger(FileUploadService.class);

    /** XPath that points to the artifact uuid.*/
    public static final String XPATH_ARTIFACT_UUID =
        "/upload/artifact-uuid/text()";

    /** XPath that points to the base64 encoded data.*/
    public static final String XPATH_DATA = "/upload/data/text()";

    public FileUploadService() {
    }

    @Override
    protected Document doProcess(
        Document data,
        GlobalContext context,
        CallMeta callMeta
    ) {
        log.debug("FileUploadService.process");

        Document doc = XMLUtils.newDocument();
        ElementCreator ec = new ElementCreator(doc, null, null);
        Element resp   = ec.create("response");
        Element status = ec.create("status");
        resp.appendChild(status);
        doc.appendChild(resp);

        String uuid = extractUuid(data);

        byte[] fileData = extractData(data);
        if (fileData != null) {
            try {
                String shapePath = RiverUtils.getXPathString(
                    RiverUtils.XPATH_MAPFILES_PATH);

                File artifactDir = FileTools.getDirectory(shapePath, uuid);
                FileOutputStream fos =
                    new FileOutputStream(
                    new File(artifactDir, "user-rgd.zip"));
                try {
                    fos.write(fileData);

                    // Write operation successful
                    status.setTextContent("Upload erfolgreich!"); // TODO: i18n
                }
                finally {
                    fos.close();
                }
            }
            catch (IOException ioe) {
                log.warn(ioe, ioe);
                status.setTextContent("Upload fehlgeschlagen!");
            }
        }
        else {
            log.debug("No data in uploaded xml.");
            status.setTextContent("Upload fehlgeschlagen!");
        }

        return doc;
    }

    /**
     * Extracts the UUID from the XML document.
     * @param data
     * @return
     */
    protected String extractUuid(Document data) {
        return XMLUtils.xpathString(
            data, XPATH_ARTIFACT_UUID, ArtifactNamespaceContext.INSTANCE);
    }

    /**
     * Extracts the base64 encoded ZIP file from the XML document.
     * @param data
     * @return
     */
    protected byte[] extractData(Document data) {
        String b64Data = XMLUtils.xpathString(
            data, XPATH_DATA, ArtifactNamespaceContext.INSTANCE);

        if (b64Data != null && b64Data.length() > 0) {
           byte[] fileData = Base64.decodeBase64(b64Data);
           return fileData;
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
