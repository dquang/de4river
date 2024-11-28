/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.geom.Lines;

import org.dive4elements.river.artifacts.math.Distance;
import org.dive4elements.river.artifacts.math.Linear;

import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RelativePointFacet;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WKmsFacet;
import org.dive4elements.river.artifacts.model.WKmsFactory;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.river.model.FastCrossSectionLine;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

/**
 * Artifact to access additional "waterlevel"-type of data, like the height
 * of protective measures (dikes).
 *
 * This artifact neglects (Static)D4EArtifacts capabilities of interaction
 * with the StateEngine by overriding the getState*-methods.
 */
public class StaticWKmsArtifact
extends      StaticD4EArtifact
implements   FacetTypes, WaterLineArtifact
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(StaticWKmsArtifact.class);

    private static final String NAME = "staticwkms";

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(NAME, FacetActivity.INACTIVE);
    }

    public static final String STATIC_STATE_NAME =
        "state.additional_wkms.static";

    /** Data Item name to know whether we are Heighmarks and reveive
     * some data slightly different. */
    public static final String DATA_HEIGHT_TYPE =
        "height_marks";

    /** One and only state to be in. */
    protected transient State state = null;


    /**
     * Trivial Constructor.
     */
    public StaticWKmsArtifact() {
        log.debug("StaticWKmsArtifact.StaticWKmsArtifact");
    }

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
        log.debug("StaticWKmsArtifact.setup");

        state = new StaticState(STATIC_STATE_NAME);

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        List<Facet> fs = new ArrayList<Facet>();
        String code = getDatacageIDValue(data);

        // TODO Go for JSON, one day.
        //ex.: flood_protection-wstv-114-12
        if (code != null) {
            String [] parts = code.split("-");

            if (parts.length >= 4) {
                int col = -1;
                int wst = Integer.parseInt(parts[3]);

                if (!parts[2].equals("A")) {
                    col = Integer.parseInt(parts[2]);
                }

                addStringData("col_pos", parts[2]);
                addStringData("wst_id",  parts[3]);

                String wkmsName;
                if (col >= 0) {
                    // The W-Wrapping could be done in here (like in
                    // StaticWQKmsArtifact), with benefit of i18nation,
                    // but slower execution (it wrappes based on kind
                    // which can be fetched in same sql query).
                    wkmsName = WKmsFactory.getWKmsNameWWrapped(col, wst);
                }
                else {
                    wkmsName = WKmsFactory.getWKmsNameWWrapped(wst);
                }

                String name;
                if (parts[0].equals(HEIGHTMARKS_POINTS)) {
                    name = HEIGHTMARKS_POINTS;
                    addStringData(DATA_HEIGHT_TYPE, "true");
                }
                else if (parts[0].equals("additionalsmarks")) {
                    name = STATIC_WKMS_MARKS;
                }
                else if (parts[0].equals("delta_w")) {
                    name = STATIC_DELTA_W;
                }
                else if (parts[0].equals("delta_w_cma")) {
                    name = STATIC_DELTA_W_CMA;
                }
                else {
                    name = STATIC_WKMS;
                }

                String facetDescription = Resources.getMsg(
                    callMeta, wkmsName, wkmsName);
                Facet wKmsFacet = new WKmsFacet(
                    name,
                    facetDescription);
                Facet csFacet = new CrossSectionWaterLineFacet(0,
                    facetDescription);
                Facet rpFacet = new RelativePointFacet(facetDescription);

                fs.add(wKmsFacet);
                fs.add(csFacet);
                fs.add(rpFacet);
                addFacets(state.getID(), fs);
            }
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
            "general", "image/png",
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
        log.debug("StaticWKmsArtifact.initialize");
        D4EArtifact winfo = (D4EArtifact) artifact;
        // TODO: The river is of no interest, so far.
        addData("river", winfo.getData("river"));
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
     * Get WKms from factory.
     * @param idx param is not needed (TODO?)
     * @return WKms according to parameterization (can be null);
     */
    public WKms getWKms(int idx) {
        log.debug("StaticWKmsArtifact.getWKms");

        return WKmsFactory.getWKms(
            Integer.parseInt(getDataAsString("col_pos")),
            Integer.parseInt(getDataAsString("wst_id")));
    }

    public WKms getWKms(int idx, double from, double to) {
        log.debug("StaticWKmsArtifact.getWKms");

        return WKmsFactory.getWKms(
            Integer.parseInt(getDataAsString("col_pos")),
            Integer.parseInt(getDataAsString("wst_id")),
            from, to);
    }

    /**
     * Returns W at Km of WKms, linearly interpolated.
     * Returns -1 if not found.
     */
    public static double getWAtKmLin(WKms wkms, double km) {
        // Uninformed search.
        int size = wkms.size();
        if (size == 0) {
            return -1;
        }
        int idx = 0;

        boolean kmIncreasing;
        if (size == 1) {
            kmIncreasing = true;
        }
        else {
            kmIncreasing = (wkms.getKm(0) < wkms.getKm(wkms.size()-1))
                ? true : false;
        }
        if (kmIncreasing) {
            while (idx < size && wkms.getKm(idx) < km) {
                idx++;
            }
        }
        else {
            idx = wkms.size() -1;
            while (idx > 0 && wkms.getKm(idx) > km) {
                idx--;
            }
        }

       if (wkms.getKm(idx) == km) {
           return wkms.getW(idx);
       }

        if (idx == size -1 || idx == 0) {
            return -1;
        }

        // Do linear interpolation.
        int mod = kmIncreasing ? -1 : +1;
        return Linear.linear(
            km,
            wkms.getKm(idx+mod),
            wkms.getKm(idx),
            wkms.getW(idx+mod),
            wkms.getW(idx)
        );
    }


    /**
     * Get the W at a specific km, only if it is closer to km than to any of
     * the other given km.
     * Return Double.NaN otherwise
     *
     * @param wkms WKms in which to search for a spatially close W value.
     * @param km the input km, which is compared to values from wkms.
     * @param next the next available input km (-1 if unavailable).
     * @param prev the previous available input km (-1 if unavailable).
     *
     * @return W in wkms that is closer to km than to next and prev,
     *         or Double.NaN.
     */
    public double getWAtCloseKm(
        WKms wkms,
        double km,
        double next,
        double prev
    ) {
        // TODO symbolic "-1" pr next/prev is a bad idea (tm), as we compare
        //      distances to these values later.
        // TODO issue888

        int size = wkms.size();
        for (int i = 0; i < size; i++) {
            double wkmsKm = wkms.getKm(i);
            double dist = Distance.distance(wkmsKm, km);
            if (dist == 0d) {
                return wkms.getW(i);
            }

            // Problematic Cases:
            // X == km , | and | == prev and next, (?) == wkmsKm
            //
            // Standard case:
            // ----------|----X-----|-------
            //     (1)    (2)    (3)   (4)
            //
            // With prev==-1
            // -1 ------X-------|------
            //    (5)      (6)     (7)
            //
            // With next==-1
            //
            // ---|-----X----- -1
            // (8)  (9)   (10)

            if (dist <= Distance.distance(wkmsKm, prev)
                && dist <= Distance.distance(wkmsKm, next)) {
                return wkms.getW(i);
            }
        }

        return Double.NaN;
    }


    /**
     * Returns W at Km of WKms, searching linearly.
     * Returns -1 if not found.
     * @param wkms the WKms object to search for given km.
     * @param km The searched km.
     * @return W at given km if in WKms, -1 if not found.
     */
    public static double getWAtKm(WKms wkms, double km) {
        // Uninformed search, intolerant.
        double TOLERANCE = 0.0d;
        int size = wkms.size();
        for (int i = 0; i < size; i++) {
            if (Distance.within(wkms.getKm(i), km, TOLERANCE)) {
                return wkms.getW(i);
            }
        }

        return -1;
    }


    /**
     * Get points of line describing the surface of water at cross section.
     *
     * @param idx Index of facet and in wkms array.
     * @param csl FastCrossSectionLine to compute water surface agains.
     * @param next The km of the next crosssectionline.
     * @param prev The km of the previous crosssectionline.
     * @param context Ignored in this implementation.
     *
     * @return an array holding coordinates of points of surface of water (
     *         in the form {{x1, x2}, {y1, y2}} ).
     */
    @Override
    public Lines.LineData getWaterLines(int idx, FastCrossSectionLine csl,
        double next, double prev, CallContext context
    ) {
        log.debug("getWaterLines(" + idx + ")/" + identifier());

        List<Point2D> points = csl.getPoints();

        WKms wkms = getWKms(0);

        double km = csl.getKm();

        // Find W at km.
        double wAtKm;

        // If heightmarks, only deliver if data snaps.
        if (getDataAsString(DATA_HEIGHT_TYPE) != null &&
            getDataAsString(DATA_HEIGHT_TYPE).equals("true")) {
            wAtKm = getWAtCloseKm(wkms, km, next, prev);
        }
        else {
            wAtKm = getWAtKm(wkms, km);
        }

        if (wAtKm == -1 || Double.isNaN(wAtKm)) {
            log.warn("Waterlevel at km " + km + " unknown.");
            return new Lines.LineData(new double[][] {{}}, 0d, 0d);
        }

        return Lines.createWaterLines(points, wAtKm);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
