/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

public class EmptyFacet
extends      DefaultFacet
{
    /** Trivial constructor. */
    public EmptyFacet() {
        super(0, "empty.facet", "empty.facet");
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        return null;
    }


    /**
     * Return a deep copy.
     */
    @Override
    public Facet deepCopy() {
        EmptyFacet copy = new EmptyFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
