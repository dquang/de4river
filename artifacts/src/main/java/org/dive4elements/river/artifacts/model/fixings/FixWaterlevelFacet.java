/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WaterlevelFacet;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

/** Waterlevel from fix realize compute. */
public class FixWaterlevelFacet
extends      WaterlevelFacet
{
    public FixWaterlevelFacet() {
    }

    public FixWaterlevelFacet(int index, String name, String description) {
        super(index, name, description, ComputeType.ADVANCE, null, null);
    }

    public FixWaterlevelFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      hash,
        String      stateID
    ) {
        // Note that in super, hash and stateID are on switched positions.
        // on super.super it is this way around again.
        super(index, name, description, type, stateID, hash);
    }

    @Override
    protected WQKms [] getWQKms(CalculationResult res) {
        FixRealizingResult fr = (FixRealizingResult)res.getData();
        return fr != null ? fr.getWQKms() : null;
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        FixWaterlevelFacet copy = new FixWaterlevelFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
