/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.StaticWQKmsArtifact;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * Facet to show W|Q|km Values.
 * We have following 'Types' (from FacetTypes):
 *   String STATIC_WQKMS = "other.wqkms";
 *   String STATIC_WQMS_W = "other.wqkms.w";
 *   String STATIC_WQKMS_Q = "other.wqkms.q";
 */
public class WQKmsFacet
extends      DataFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(WQKmsFacet.class);

    /** Trivial Constructor. */
    public WQKmsFacet(String description) {
        this(STATIC_WQKMS, description);
    }


    /**
     * @param name Name of this facet (we have at least two flavors (w and q).
     */
    public WQKmsFacet(String name, String description) {
        super(0, name, description, ComputeType.FEED, null, null);
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
        log.debug("WQKmsFacet.getData");

        StaticWQKmsArtifact staticData =
            (StaticWQKmsArtifact) artifact;
        Object res = staticData.compute(context, hash, stateId, type, false);

        return res;
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
