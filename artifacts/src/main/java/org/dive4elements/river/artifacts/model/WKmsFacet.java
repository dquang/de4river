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

import org.dive4elements.river.artifacts.StaticWKmsArtifact;

/**
 * Facet to show W|km Values.
 */
public class WKmsFacet
extends      BlackboardDataFacet
implements   FacetTypes {

    /** Trivial Constructor. */
    public WKmsFacet(String description) {
        this(STATIC_WKMS, description);
    }

    public WKmsFacet(String name, String description) {
        this.name        = name;
        this.description = description;
        this.index       = 0;
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext (ignored).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        StaticWKmsArtifact staticData =
            (StaticWKmsArtifact) artifact;
        return staticData.getWKms(0);
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public WKmsFacet deepCopy() {
        WKmsFacet copy = new WKmsFacet(description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
