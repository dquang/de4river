/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import org.dive4elements.river.etl.utils.XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Notification
{
    private static Logger log = LogManager.getLogger(Notification.class);

    protected Document message;

    public Notification() {
    }

    public Notification(Document message) {
        this.message = message;
    }

    public Notification(Node message) {
        this(wrap(message));
    }

    public static Document wrap(Node node) {
        Document document = XML.newDocument();

        // Send first element as message.
        // Fall back to root node.
        Node toImport = node;

        NodeList children = node.getChildNodes();
        for (int i = 0, N = children.getLength(); i < N; ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                toImport = child;
                break;
            }
        }

        toImport = document.importNode(toImport, true);
        document.appendChild(toImport);
        document.normalizeDocument();
        return document;
    }

    public Document sendPOST(URL url) {

        OutputStream out    = null;
        InputStream  in     = null;
        Document     result = null;

        try {
            URLConnection ucon = url.openConnection();

            if (!(ucon instanceof HttpURLConnection)) {
                log.warn("NOTIFY: '" + url + "' is not an HTTP(S) connection.");
                return null;
            }

            HttpURLConnection con = (HttpURLConnection)ucon;

            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "text/xml");

            out = con.getOutputStream();
            XML.toStream(message, out);
            out.flush();
            in = con.getInputStream();
            result = XML.parseDocument(in);
        }
        catch (IOException ioe) {
            log.error("NOTIFY: Sending message to '" + url + "' failed.", ioe);
        }
        finally {
            if (out != null) {
                try { out.close(); } catch (IOException ioe) {}
            }
            if (in != null) {
                try { in.close(); } catch (IOException ioe) {}
            }
        }

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
