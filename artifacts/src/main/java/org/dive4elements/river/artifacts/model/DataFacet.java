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

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

public class DataFacet
extends      BlackboardDataFacet
{
    protected ComputeType type;
    protected String      hash;
    protected String      stateId;


    /** Trivial constructor. */
    public DataFacet() {
    }

    /**
     * Defaults to ADVANCE Compute type.
     * @param name Name of the facet.
     * @param description maybe localized description of the facet.
     */
    public DataFacet(String name, String description) {
        this(name, description, ComputeType.ADVANCE);
    }


    public DataFacet(String name, String description, ComputeType type) {
        this(name, description, type, null);
    }


    public DataFacet(
        String      name,
        String      description,
        ComputeType type,
        String      hash
    ) {
        super(name, description);
        this.type = type;
        this.hash = hash;
    }


    public DataFacet(
        String      name,
        String      description,
        ComputeType type,
        String      hash,
        String      stateId
    ) {
        super(name, description);
        this.type    = type;
        this.hash    = hash;
        this.stateId = stateId;
    }


    public DataFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      hash,
        String      stateId
    ) {
        super(index, name, description);
        this.type    = type;
        this.hash    = hash;
        this.stateId = stateId;
    }


    /**
     * Return computation result.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        D4EArtifact flys = (D4EArtifact)artifact;
        String    theHash = (hash != null) ? hash : flys.hash();

        return (stateId != null && stateId.length() > 0)
            ? flys.compute(context, theHash, stateId, type, false)
            : flys.compute(context, theHash, type, false);
    }


    /**
     * Return a deep copy.
     */
    @Override
    public Facet deepCopy() {
        DataFacet copy = new DataFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
