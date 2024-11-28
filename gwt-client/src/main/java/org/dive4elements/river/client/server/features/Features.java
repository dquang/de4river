/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.features;

import java.util.List;

public interface Features {

    public static final String CONTEXT_ATTRIBUTE =
        "org.dive4elements.river.client.server.features";

    /**
     * Returns all allowed features to a list of roles
     */
    public List<String> getFeatures(List<String> roles);
}
