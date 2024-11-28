/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.artifacts.model.minfo.SedimentDensityFacet;
import org.dive4elements.river.artifacts.model.minfo.SedimentDensityFactory;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.river.artifacts.model.FacetTypes;


/** Artifact to access sediment density measurements. */
public class SedimentDensityArtifact
extends      StaticD4EArtifact
implements   FacetTypes
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(SedimentDensityArtifact.class);

    /** Artifact key name. */
    private static final String NAME = "sedimentdensity";

    /** Spawn only inactive facets. */
    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(NAME, FacetActivity.INACTIVE);
    }

    /** Need to give the state an id. */
    public static final String STATIC_STATE_NAME =
        "state.sedimentdensity.static";

    /** One and only state to be in. */
    protected transient State state = null;

    protected String DATA_ID = "ID";

    /**
     * Trivial Constructor.
     */
    public SedimentDensityArtifact() {
        log.debug("SedimentDensityArtifact.SedimentDensityArtifact");
    }


    /** Get artifact key name. */
    @Override
    public String getName() {
        return NAME;
    }


    private Object getSedimentDensity() {
        log.debug("SedimentDensityArtifact.getSedimentDensity");
        Integer id = getDataAsInteger(DATA_ID);

        // TODO use cache if possible
        return SedimentDensityFactory.getSedimentDensityUncached(id);
    }


    private State newDensityState() {
        return new StaticState(STATIC_STATE_NAME) {
            public Object staticCompute(List<Facet> facets) {
                 return getSedimentDensity();
            }
        };
    }


    /** Create a new state with bogus output. */
    protected State spawnState() {
        state = newDensityState();
        List<Facet> fs = getFacets(STATIC_STATE_NAME);
        DefaultOutput output = new DefaultOutput(
            "general",
            "general",
            "image/png",
            fs,
            "chart");

        state.getOutputs().add(output);

        return state;
    }


    /**
     * Gets called from factory, to set things up.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("SedimentDensityArtifact.setup");

        state = newDensityState();
        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        List<Facet> fs = new ArrayList<Facet>();

        String code = getDatacageIDValue(data);

        if (code != null) {
            double[] depth = SedimentDensityFactory.getDepth(
                Integer.valueOf(code));

            String name = Resources.getMsg(
                callMeta, "sedimentdensity", "sedimentdensity");
            name += " " + depth[0] + " - " + depth[1] + " cm";
            Facet facet = new SedimentDensityFacet(
                0,
                SEDIMENT_DENSITY,
                name,
                ComputeType.ADVANCE, state.getID(), "hash"
                );
            fs.add(facet);
            addFacets(state.getID(), fs);
            addStringData(DATA_ID, code);
        }
        else {
            log.error("No id given.");
        }

        spawnState();
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    /**
     * Get a list containing the one and only State.
     * @param  context ignored.
     * @return list with one and only state.
     */
    @Override
    protected List<State> getStates(Object context) {
        ArrayList<State> states = new ArrayList<State>();
        states.add(getState());
        return states;
    }


    /**
     * Get the "current" state (there is but one).
     * @param cc ignored.
     * @return the "current" (only possible) state.
     */
    @Override
    public State getCurrentState(Object cc) {
        return getState();
    }


    /**
     * Get the only possible state.
     * @return the state.
     */
    protected State getState() {
        return getState(null, null);
    }


    /**
     * Get the state.
     * @param context ignored.
     * @param stateID ignored.
     * @return the state.
     */
    @Override
    protected State getState(Object context, String stateID) {
        return (state != null)
            ? state
            : spawnState();
    }


    /**
     * Called via setup. Overridden to avoid cloning all data.
     *
     * @param artifact The master-artifact.
     */
    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta)
    {
        log.debug("SedimentDensityArtifact.initialize");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
