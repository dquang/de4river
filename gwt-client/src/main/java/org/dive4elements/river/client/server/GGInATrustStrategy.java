/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.TrustStrategy;

public class GGInATrustStrategy implements TrustStrategy {

    /**
     * Temporary class to accept all certificates for GGinA Authentication.
     */

    @Override
    public boolean isTrusted(
        X509Certificate[] chain,
        String authType
    ) throws CertificateException {
        // FIXME validate Certificate
        return true;
    }
}

