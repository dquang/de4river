/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.RiverFactory;

import org.dive4elements.river.model.River;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Access to river data of an artifact. */
public class RiverAccess
extends      Access
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(RiverAccess.class);

    /** River name. */
    protected String river;


    public RiverAccess() {
    }

    public RiverAccess(D4EArtifact artifact) {
        super(artifact);
    }


    /** Get River name. */
    public String getRiverName() {
        if (river == null) {
            river = getString("river");
        }
        if (log.isDebugEnabled()) {
            log.debug("river: '" + river + "'");
        }
        return river;
    }

    public River getRiver() {
        getRiverName();

        return (river != null)
            ? RiverFactory.getRiver(river)
            : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
