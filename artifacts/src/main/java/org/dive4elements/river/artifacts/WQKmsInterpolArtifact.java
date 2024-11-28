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

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WQFacet;
import org.dive4elements.river.artifacts.model.WKmsFactory;
import org.dive4elements.river.artifacts.model.WQKmsFactory;
import org.dive4elements.river.artifacts.model.WstValueTable;
import org.dive4elements.river.artifacts.model.WstValueTableFactory;

import org.dive4elements.river.artifacts.states.StaticState;
import org.dive4elements.river.artifacts.resources.Resources;


/**
 * Artifact to access additional "waterlevel/discharge"-type of data, like
 * fixation measurements, but doing so with costy interpolation.
 *
 * This artifact neglects (Static)D4EArtifacts capabilities of interaction
 * with the StateEngine by overriding the getState*-methods.
 */
public class WQKmsInterpolArtifact
extends      StaticD4EArtifact
implements   FacetTypes
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(WQKmsInterpolArtifact.class);

    /** State name. */
    public static final String STATIC_STATE_NAME =
        "state.additional_wqkms.interpol.static";

    /** Artifact name. */
    private static final String NAME = "staticwqkmsinterpol";

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(NAME, FacetActivity.INACTIVE);
    }

    /** One and only state to be in. */
    protected transient State state = null;


    /**
     * Trivial Constructor.
     */
    public WQKmsInterpolArtifact() {
        log.debug("WQKmsInterpolArtifact.WQKmsInterpolArtifact");
    }


    /** Return fixed artifact (types) name. */
    @Override
    public String getName() {
        return NAME;
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
        log.debug("WQKmsInterpolArtifact.setup");

        state = new StaticState(STATIC_STATE_NAME);

        List<Facet> fs = new ArrayList<Facet>();
        String code = getDatacageIDValue(data);

        // TODO Go for JSON, one day.
        //ex.: flood_protection-wstv-114-12
        if (code != null) {
            String [] parts = code.split("-");

            log.debug("WQKmsInterpolArtifact.setup: code " + code);

            if (parts.length >= 4) {
                int wst = Integer.parseInt(parts[3]);
                int col = -1;
                String colpos = parts[2];
                // Are we interested in a single column or in all columns?
                if (colpos.equals("A")) {
                    ; // Take all.
                }
                else {
                    col = Integer.parseInt(colpos);
                    addStringData("col_pos", parts[2]);
                }
                addStringData("wst_id",  parts[3]);
                String wkmsName = (col >= 0)
                                ? WKmsFactory.getWKmsName(col, wst)
                                : WKmsFactory.getWKmsName(wst);
                String name;
                if (parts[0].startsWith("height")){
                    name = STATIC_WQ_ANNOTATIONS;
                }
                else if (parts[0].startsWith("flood")) {
                    name = STATIC_WKMS_INTERPOL;
                }
                else {
                    // If all Qs are zero, add different facet to
                    // signalize that we want data to be drawn as marks
                    // on axis.
                    if (wstValueHasZeroQ()) {
                        name = STATIC_W_INTERPOL;
                    }
                    else {
                        name = STATIC_WQ;
                    }
                }

                Facet wQFacet = new WQFacet(name,
                    Resources.getMsg(
                        callMeta,
                        wkmsName,
                        wkmsName));
                fs.add(wQFacet);
                addFacets(state.getID(), fs);
            }
        }
        else {
            log.warn("WQKmsInterpolArtifact: no code");
        }

        spawnState();
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    /**
     * Initialize the static state with output.
     * @return static state
     */
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
     * Called via setup.
     *
     * @param artifact The master-artifact.
     */
    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta)
    {
        log.debug("WQKmsInterpolArtifact.initialize");
        D4EArtifact winfo = (D4EArtifact) artifact;
        importData(winfo, "river");
        importData(winfo, "ld_locations");
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


    /** True if Wst has only 'fake' (zero) Q-ranges. */
    private boolean wstValueHasZeroQ() {
        WstValueTable table = getValueTable();
        return table.hasEmptyQ();
    }


    /** Get the WstValueTable that matches parameterization. */
    private WstValueTable getValueTable() {
        // Get WstValueTable
        int wstId = getDataAsInt("wst_id");
        if (getDataAsString("col_pos") != null) {
            return WstValueTableFactory.getWstColumnTable(
                wstId, getDataAsInt("col_pos"));
        }
        else {
            return WstValueTableFactory.getTable(wstId);
        }
    }


    /**
     * Get WQ Values at a certain km, interpolating only if distance
     * between two stations is smaller than given distance.
     */
    public double [][] getWQAtKm(
        Double currentKm,
        double maxKmInterpolDistance
    ) {
        // TODO yet to be implemented (issue1378).
        return null;
    }


    /**
     * Get WQ at a given km.
     *
     * @param currentKm the requested km. If NULL, ld_location data
     *        will be used.
     * @return [[q1,q2,q2],[w1,w2,w3]] ...
     */
    public double [][] getWQAtKm(Double currentKm) {

        // TODO issue1378: only interpolate if dist <= 100m
        WstValueTable interpolator = getValueTable();

        Double tmp = (currentKm != null)
                     ? currentKm
                     : getDataAsDouble("ld_locations");

        double [][] vs = interpolator.interpolateWQColumnwise(
            tmp != null ? tmp : 0);

        for (int x = 0; x < vs[1].length; x++) {
            log.debug("getWQAtKm: Q/W " + vs[0][x] + " / " + vs[1][x]);
        }

        return vs;
    }


    /**
     * Get a DataItem casted to int (0 if fails).
     */
    public int getDataAsInt(String dataName) {
        String val = getDataAsString(dataName);
        try {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e) {
            log.warn("Could not get data " + dataName + " as int", e);
            return 0;
        }
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
     * Get WQKms from factory.
     * @param idx param is not needed (TODO)
     * @return WQKms according to parameterization (can be null);
     */
    public WQKms getWQKms(int idx) {
        log.debug("WQKmsInterpolArtifact.getWQKms");
        log.warn("Stub, getWQKms not yet implemented.");

        return WQKmsFactory.getWQKms(
            Integer.parseInt(getDataAsString("col_pos")),
            Integer.parseInt(getDataAsString("wst_id")));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
