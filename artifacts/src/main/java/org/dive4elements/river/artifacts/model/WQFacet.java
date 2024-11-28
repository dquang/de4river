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

import org.dive4elements.river.artifacts.WQKmsInterpolArtifact;
import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;

/**
 * Facet to show W|Q Values.
 */
public class WQFacet
extends      DefaultFacet
implements   FacetTypes {

    /** Trivial Constructor. */
    public WQFacet(String description) {
        this(STATIC_WQ, description);
    }


    /**
     * A Facet with WQ data.
     */
    public WQFacet(String name, String description) {
        this.name        = name;
        this.description = description;
        this.index       = 0;
    }


    /**
     * Returns the data this facet provides at km given in context.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext.
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        WQKmsInterpolArtifact interpolData =
            (WQKmsInterpolArtifact) artifact;
        Double currentKm = (Double)context.getContextValue(CURRENT_KM);
        return interpolData.getWQAtKm(currentKm);
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public WQKmsFacet deepCopy() {
        WQKmsFacet copy = new WQKmsFacet(description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
