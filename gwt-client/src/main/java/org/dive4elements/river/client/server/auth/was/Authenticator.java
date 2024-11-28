/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.was;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.ServletContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import org.dive4elements.river.client.server.GGInATrustStrategy;
import org.dive4elements.river.client.server.auth.Authentication;
import org.dive4elements.river.client.server.auth.AuthenticationException;
import org.dive4elements.river.client.server.features.Features;

public class Authenticator
implements org.dive4elements.river.client.server.auth.Authenticator {

    @Override
    public Authentication auth(
        String username,
        String password,
        String encoding,
        Features features,
        ServletContext context
    ) throws
        AuthenticationException,
        IOException
    {
            try {
                SSLSocketFactory sf = new SSLSocketFactory(
                        new GGInATrustStrategy());
                Scheme https = new Scheme("https", 443, sf);
                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getConnectionManager().getSchemeRegistry().register(
                        https);

                Request httpget = new Request("https://geoportal.bafg.de/" +
                        "administration/WAS", username, password, encoding);
                HttpResponse response = httpclient.execute(httpget);
                StatusLine stline = response.getStatusLine();
                if (stline.getStatusCode() != 200) {
                    throw new AuthenticationException("GGInA Server Error. " +
                            "Statuscode: " + stline.getStatusCode() +
                            ". Reason: " + stline.getReasonPhrase());
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    //FIXME throw AuthenticationException
                    return null;
                }
                else {
                    File trustedKey = new File(
                        context.getInitParameter("saml-trusted-public-key"));
                    String path = trustedKey.isAbsolute()
                        ? trustedKey.getPath()
                        : context.getRealPath(trustedKey.getPath());
                    String timeEpsilon = context.getInitParameter(
                        "saml-time-tolerance");
                    return new Response(entity, username, password, features,
                        path, timeEpsilon);
                }
            }
            catch(GeneralSecurityException e) {
                throw new AuthenticationException(e);
            }
    }
}
