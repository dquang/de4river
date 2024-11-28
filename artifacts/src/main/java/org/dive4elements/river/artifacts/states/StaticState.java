/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Output;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

/**
 * Yet, a non-abstract DefaultState.
 */
public class StaticState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static final Logger log = LogManager.getLogger(StaticState.class);

    private static String uiprovider;


    public StaticState() {
        super();
    }

    /**
     * Trivial constructor, sets id and description.
     * @param id String used for both id and description.
     */
    public StaticState(String id) {
        this(id, id);
    }


    public StaticState(String id, String description) {
        super();
        setID(id);
        setDescription(description);
    }

    public void addDefaultChartOutput(String nameDesc, List<Facet> facets) {
        DefaultOutput output = new DefaultOutput(nameDesc,
            nameDesc, "image/png", facets, "chart");
        getOutputs().add(output);
    }

    public static void addDefaultChartOutput(
        DefaultState state,
        String nameDesc,
        List<Facet> facets
    ) {
        DefaultOutput output = new DefaultOutput(nameDesc,
            nameDesc, "image/png", facets, "chart");
        state.getOutputs().add(output);
    }


    /**
     * Do nothing (override to include your logic).
     * @param facets List of facets (to add to).
     */
    public Object staticCompute(List<Facet> facets, D4EArtifact artifact) {
        return staticCompute(facets);
    }

    /** End-point and most important compute-method.
     * Override for desired effect. */
    public Object staticCompute(List<Facet> facets) {
        return null;
    }


    /** Call staticCompute to allow easy adjustments. */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return staticCompute(facets, artifact);
    }


    /** Call staticCompute to allow easy adjustments. */
    @Override
    public Object computeFeed(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return staticCompute(facets, artifact);
    }


    /** Call staticCompute to allow easy adjustments. */
    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        return staticCompute(facets, artifact);
    }

    public void addOutput(Output out) {
        super.addOutput(out);
    }

    @Override
    protected String getUIProvider() {
        return this.uiprovider;
    }

    /**
     * Allow to set the uiprovider for displaying the static data.
     */
    public void setUIProvider(String uiprovider) {
        this.uiprovider = uiprovider;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
