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

import org.dive4elements.river.artifacts.ManualPointsArtifact;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;


/**
 * Facet to access ManualPoints that where added by user.
 */
public class ManualPointsFacet
extends      DefaultFacet
{
    /** Logger for this class. */
    private static final Logger log = LogManager.getLogger(
        ManualPointsFacet.class);


    /**
     * Trivial Constructor.
     */
    public ManualPointsFacet() {
    }


    /**
     * Trivial Constructor for a ManualPointsFacet.
     *
     * @param index       Database-Index to use.
     * @param name        Name (~type) of Facet.
     * @param description Description of Facet.
     */
    public ManualPointsFacet(int index, String name, String description) {
        super(index, name, description);
    }


    /**
     * Get List of ManualPoints for river from Artifact.
     *
     * @param artifact (ManualPoints-)Artifact to query
     *                 for list of ManualPoints.
     * @param context  Ignored.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        ManualPointsArtifact pointsArtifact = (ManualPointsArtifact) artifact;
        return pointsArtifact.getPointsData(this.name);
    }


    /** Do a deep copy. */
    @Override
    public Facet deepCopy() {
        ManualPointsFacet copy = new ManualPointsFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
