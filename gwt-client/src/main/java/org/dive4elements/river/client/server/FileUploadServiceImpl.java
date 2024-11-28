/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileUploadServiceImpl
extends      HttpServlet
{
    private static final Logger log = LogManager.getLogger(
        FileUploadServiceImpl.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        log.debug("handling post request.");

        String url  = getServletContext().getInitParameter("server-url");

        Document request = createFileXML(req);

        if (request == null) {
            return;
        }
        HttpClient client = new HttpClientImpl(url);

        try {
            Document result = client.callService(url, "fileupload", request);

            resp.setContentType("text/html");

            PrintWriter respWriter = resp.getWriter();
            respWriter.write(
                "<html><body><div style='font-face: Arial,Verdana,sans-serif; "
                + "font-size: 11px'>");

            if (result == null) {
                log.warn("FileUpload service returned no result.");
                respWriter.write("FileUpload service returned no result");
            }
            else {
                String status = result.getElementsByTagName("status")
                        .item(0).getTextContent();
                respWriter.write(status);
            }

            respWriter.write("</div></body></html>");
            respWriter.flush();

            return;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }
        catch (IOException e) {
            log.error(e, e);
        }
    }


    protected Document createFileXML(HttpServletRequest req) {
        ServletFileUpload upload = new ServletFileUpload();

        try{
            FileItemIterator iter = upload.getItemIterator(req);

            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                InputStream stream = item.openStream();

                // Process the input stream
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int len, cnt = 0;
                byte[] buffer = new byte[stream.available()];
                while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                    cnt += len;
                }

                buffer = Base64.encodeBase64(out.toByteArray());
                String b64File = new String(buffer);
                log.debug("FileUploadServiceImpl.createFileXML(): " + cnt
                        + "/" + b64File.length()
                        + " bytes (orig/base64) file to be uploaded");

                Document fileDoc = XMLUtils.newDocument();

                ElementCreator ec = new ElementCreator(fileDoc, null, null);
                Element root = ec.create("upload");
                Element id = ec.create("artifact-uuid");
                id.setTextContent(req.getParameter("uuid"));

                Element data = ec.create("data");
                data.setTextContent(b64File);

                fileDoc.appendChild(root);
                root.appendChild(id);
                root.appendChild(data);

                return fileDoc;
            }
        }
        catch(Exception e){
            log.debug("Failed to create xml document containing the file.");
            log.debug(e, e);
        }
        return null;
    }
}
