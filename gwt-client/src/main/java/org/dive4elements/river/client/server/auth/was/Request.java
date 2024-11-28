/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.was;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Request extends HttpGet {

    private final static String VERSION = "1.1";
    private final static String REQUEST_SAML_RESPONSE = "GetSAMLResponse";
    private final static String METHOD_AUTH_PASSWORD =
        "urn:opengeospatial:authNMethod:OWS:1.0:password";

    private static Logger log = LogManager.getLogger(Request.class);

    public Request(String uri) {
        String request = uri + "?VERSION=" + VERSION + "&REQUEST=" +
            REQUEST_SAML_RESPONSE + "&METHOD=" + METHOD_AUTH_PASSWORD +
            "&ANONYMOUS=TRUE&CREDENTIALS=";
        this.setURI(URI.create(request));
    }

    public Request(String uri, String user, String pass, String encoding) {
        try {
            String base64user = this.toBase64(user, encoding);
            String base64pass = this.toBase64(pass, encoding);

            String request = uri + "?VERSION=" + VERSION + "&REQUEST=" +
                REQUEST_SAML_RESPONSE + "&METHOD=" + METHOD_AUTH_PASSWORD +
                "&CREDENTIALS=" + base64user + "," + base64pass;

            this.setURI(URI.create(request));
        }
        catch(UnsupportedEncodingException e) {
            log.error(e);
        }
    }

    private String toBase64(String value, String encoding) throws
        UnsupportedEncodingException {
        if (encoding == null) {
            encoding = "utf-8";
        }
        try {
            return new String(Base64.encodeBase64(value.getBytes(encoding)));
        }
        catch(UnsupportedEncodingException e) {
            log.warn("Can't encode string with encoding " + encoding +
                    ". Falling back to utf-8. " + e);
            return this.toBase64(value, "utf-8");
        }
    }

}
// vim: set et si fileencoding=utf-8 ts=4 sw=4 tw=80:

