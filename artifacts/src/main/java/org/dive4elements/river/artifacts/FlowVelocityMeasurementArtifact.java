/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.text.DateFormat;

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
import org.dive4elements.river.model.FlowVelocityMeasurementValue;
import org.dive4elements.river.artifacts.model.minfo.FlowVelocityMeasurementFacet;
import org.dive4elements.river.artifacts.model.minfo.FlowVelocityMeasurementFactory;
import org.dive4elements.river.artifacts.states.StaticState;
import org.dive4elements.river.artifacts.states.FlowVelocityState;

import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.Formatter;


/** Artefact to access flow velocity measurements. */
public class FlowVelocityMeasurementArtifact
extends      StaticD4EArtifact
implements   FacetTypes
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(FlowVelocityMeasurementArtifact.class);

    /** Artifact key name. */
    private static final String NAME = "flowvelocitymeasurement";

    public static final String I18N_WATERLEVEL_FACET =
        "facet.flow_velocity.waterlevel";

    public static final String I18N_VELOCITY_FACET =
        "facet.flow_velocity.velocity";

    /** Spawn only inactive facets. */
    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(NAME, FacetActivity.INACTIVE);
    }

    /** Need to give the state an id. */
    public static final String STATIC_STATE_NAME =
        "state.flowvelocitymeasurement.static";

    /** One and only state to be in. */
    protected transient State state = null;

    protected String DATA_NAME = "ID";

    /**
     * Trivial Constructor.
     */
    public FlowVelocityMeasurementArtifact() {
    }


    /** Get artifact key name. */
    @Override
    public String getName() {
        return NAME;
    }


    /** Create a new state with bogus output. */
    protected State spawnState() {
        state = new StaticState(STATIC_STATE_NAME);
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
        log.debug("FlowVelocityMeasurementArtifact.setup");

        state = new StaticState(STATIC_STATE_NAME);

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        List<Facet> fs = new ArrayList<Facet>();
        String code = getDatacageIDValue(data);
        DateFormat dateFormatter = Formatter.getDateFormatter(
            callMeta, "dd.MM.yyy HH:mm");

        if (code != null) {
            // parse code, interact with factory, add real facets.
            // store relevant parts of code as data.
            FlowVelocityMeasurementValue.FastFlowVelocityMeasurementValue
                flowVelocityMeasurement =
                    FlowVelocityMeasurementFactory.getFlowVelocityMeasurement(
                        Integer.parseInt(code));
            String name = flowVelocityMeasurement.getDescription();
            log.debug ("datetime " + flowVelocityMeasurement.getDatetime());
            name += " - " + dateFormatter.format(
                flowVelocityMeasurement.getDatetime());

            Facet vFacet = new FlowVelocityMeasurementFacet(
                FLOW_VELOCITY_MEASUREMENT,
                Resources.getMsg(callMeta,
                    I18N_VELOCITY_FACET,
                    FlowVelocityState.I18N_TAU_FACET,
                    new Object[] { name }));
            fs.add(vFacet);

            Facet qFacet = new FlowVelocityMeasurementFacet(
                FLOW_VELOCITY_DISCHARGE,
                Resources.getMsg(callMeta,
                    FlowVelocityState.I18N_DISCHARGE_FACET,
                    FlowVelocityState.I18N_DISCHARGE_FACET,
                    new Object[] { name }));
            fs.add(qFacet);

            Facet wFacet = new FlowVelocityMeasurementFacet(
                FLOW_VELOCITY_WATERLEVEL,
                Resources.getMsg(callMeta,
                    I18N_WATERLEVEL_FACET,
                    FlowVelocityState.I18N_TAU_FACET,
                    new Object[] { name }));
            fs.add(wFacet);

            addFacets(state.getID(), fs);
            addStringData(DATA_NAME, code);
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
        log.debug("initialize");
    }


    /**
     * Get the db-unbound flow velocity measurement value with given
     * id.
     */
    public FlowVelocityMeasurementValue.FastFlowVelocityMeasurementValue
        getFlowVelocityMeasurementValue()
    {
        return FlowVelocityMeasurementFactory.getFlowVelocityMeasurement(
            Integer.parseInt(getDataAsString(DATA_NAME)));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
