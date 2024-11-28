/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.model.DGM;

public class DGMAccess
extends      RangeAccess
{
    private DGM dgm;

    private String geoJSON;

    public DGMAccess() {
    }

    public DGMAccess(D4EArtifact artifact) {
        super(artifact);
    }

    public DGM getDGM() {
        if (dgm == null) {
            Integer sridId = getInteger("dgm");
            dgm = DGM.getDGM(sridId);
        }
        return dgm;
    }

    public String getGeoJSON() {
        if (geoJSON == null) {
            geoJSON = getString("uesk.barriers");
        }
        return geoJSON;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
