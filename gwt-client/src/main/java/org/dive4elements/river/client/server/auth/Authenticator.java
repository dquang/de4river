/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.dive4elements.river.client.server.features.Features;


public interface Authenticator {

    public Authentication auth(String username, String password,
                               String encoding, Features features,
                               ServletContext context)
        throws AuthenticationException, IOException;

}
