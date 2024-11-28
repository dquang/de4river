/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.artifactdatabase.state.Facet;

public class WSPLGENLayerFacet
extends      WMSLayerFacet
{
    public WSPLGENLayerFacet() {
    }


    public WSPLGENLayerFacet(int index, String name, String description) {
        this(index, name, description, ComputeType.FEED, null, null);
    }


    public WSPLGENLayerFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash
    ) {
        super(index, name, description, type, stateId, hash);
    }

    public WSPLGENLayerFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash,
        String      url
    ) {
        super(index, name, description, type, stateId, hash, url);
    }


    @Override
    public boolean isQueryable() {
        return true;
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        WSPLGENLayerFacet copy = new WSPLGENLayerFacet();
        copy.set(this);

        cloneData(copy);

        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
