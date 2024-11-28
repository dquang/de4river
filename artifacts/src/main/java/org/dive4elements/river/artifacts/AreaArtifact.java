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

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.model.AreaFacet;


import org.dive4elements.river.artifacts.states.AreaCreationState;
import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.State;


/**
 * Artifact describing the area between two WKms.
 */
public class AreaArtifact extends StaticD4EArtifact {

    /** Name of Artifact. */
    public static final String AREA_ARTIFACT_NAME = "area_artifact";

    /** Dataitem: Facet name. Facets with this name will be created (important
     * to not have the area calculated in e.g. a CrossSection to be shown in
     * LongitudinalSection.  */
    protected static final String FACET_NAME = "area.facet";

    /** Name of state. */
    public static final String STATIC_STATE_NAME = "state.area_artifact";

    /** data item name to access upper curve. */
    protected static final String AREA_CURVE_OVER = "area.curve_over";

    /** data item name to access lower curve. */
    protected static final String AREA_CURVE_UNDER = "area.curve_under";

    /** data item name to access whether or not paint over and under. */
    protected static final String AREA_BETWEEN = "area.between";

    /** Name of state. */
    protected static final String AREA_NAME = "area.name";

    /** Own log. */
    private static final Logger log =
        LogManager.getLogger(AreaArtifact.class);


    /** Return given name. */
    @Override
    public String getName() {
        return AREA_ARTIFACT_NAME;
    }


    /** Store ids, create an AreaFacet. */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.info("AreaArtifact.setup");

        super.setup(identifier, factory, context, callMeta, data, loadFacets);

        // TODO yet unused.
        String ids = getDatacageIDValue(data);

        // TODO this facet will be remodeled during next feed.
        List<Facet> fs = new ArrayList<Facet>();
        fs.add(new AreaFacet(0, "", "TODO: I am an AreaFacet"));

        AreaCreationState state = (AreaCreationState) getCurrentState(context);

        if (!fs.isEmpty()) {
            addFacets(getCurrentStateId(), fs);
        }
    }

    // TODO Data is not cached in this way.

    /** Do not copy data from daddyfact. */
    @Override
    protected void initialize(
        Artifact artifact,
        Object   context,
        CallMeta callMeta)
    {
        // do nothing
    }


    /**
     * Get name of facets to create.
     */
    public String getFacetName() {
        return getDataAsString(FACET_NAME);
    }


    /**
     * Get dataprovider key for the 'lower' curve (we got that information fed
     * from the client and store it as data).
     */
    public String getLowerDPKey() {
        return getDataAsString(AREA_CURVE_UNDER);
    }


    /**
     * True if the whole area between the two curves shall be filled.
     */
    public boolean getPaintBetween() {
        String val = getDataAsString(AREA_BETWEEN);

        return val != null && val.equals("true");
    }


    /**
     * Get dataprovider key for the 'upper' curve (we got that information fed
     * from the client and store it as data).
     */
    public String getUpperDPKey() {
        return getDataAsString(AREA_CURVE_OVER);
    }


    /** Return data item that is used to configure name of area. */
    public String getAreaName() {
        return getDataAsString(AREA_NAME);
    }


    /**
     * Create and return a new AreaCreationState with charting output.
     */
    @Override
    public State getCurrentState(Object cc) {
        final List<Facet> fs = getFacets(getCurrentStateId());

        AreaCreationState state = new AreaCreationState();

        StaticState.addDefaultChartOutput(state, "cross_section", fs);

        return state;
    }


    /**
     * Get a list containing the one and only State.
     * @param  context ignored.
     * @return list with one and only state.
     */
    @Override
    protected List<State> getStates(Object context) {
        ArrayList<State> states = new ArrayList<State>();
        states.add(getCurrentState(context));

        return states;
    }


    /** Trivia. */
    protected State getState(Object context, String stateID) {
        return getCurrentState(null);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
