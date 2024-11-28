/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class AuthenticationFactory {

    private static final Logger log =
        LogManager.getLogger(AuthenticationFactory.class);

    public static Authenticator getInstance(String name)
        throws IllegalArgumentException
    {
        if (name == null) {
            throw new IllegalArgumentException(
                "Authentication type name is null");
        }

        if (name.equalsIgnoreCase("was") ||
            name.equalsIgnoreCase("ggina")) {
            log.debug("Using GGinA authenticator.");
            return
                new org.dive4elements.river.client.server.auth.was
                .Authenticator();
        }
        else if (name.equalsIgnoreCase("plain")) {
            log.debug("Using plain authenticator.");
            return
                new org.dive4elements.river.client.server.auth.plain
                .Authenticator();
        }

        throw new IllegalArgumentException("Unkown Authentication " + name);
    }
}
