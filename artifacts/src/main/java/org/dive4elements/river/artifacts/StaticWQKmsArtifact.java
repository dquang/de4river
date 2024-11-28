/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.geom.Lines;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.model.FastCrossSectionLine;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WKmsFactory;
import org.dive4elements.river.artifacts.model.WQKmsFactory;

import org.dive4elements.river.artifacts.states.DefaultState;


/**
 * Artifact to access additional "waterlevel/discharge"-type of data, like
 * fixation measurements.
 *
 * This artifact neglects (Static)D4EArtifacts capabilities of interaction
 * with the StateEngine by overriding the getState*-methods.
 */
public class StaticWQKmsArtifact
extends      StaticD4EArtifact
implements   FacetTypes, WaterLineArtifact
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(StaticWQKmsArtifact.class);

    public static final String STATIC_STATE_NAME =
        "state.additional_wqkms.static";

    private static final String NAME = "staticwqkms";

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance().register(
            NAME,
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   outputName
                ) {
                    String fname = facet.getName();
                    return (fname.equals(STATIC_WQKMS)
                        || fname.equals(STATIC_WQKMS_W));
                }});
    }

    /**
     * Trivial Constructor.
     */
    public StaticWQKmsArtifact() {
        log.debug("StaticWQKmsArtifact.StaticWQKmsArtifact");
    }


    /**
     * Gets called from factory, to set things up.
     *
     * If the id's string starts with official- it will be treated as
     * an Artifact containing official data for the according special
     * case handling.
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
        log.debug("StaticWQKmsArtifact.setup");

        // Store the 'ids' (from datacage).
        if (log.isDebugEnabled()) {
            log.debug("StaticWQKmsArtifact.setup" + XMLUtils.toString(data));
        }

        String code = getDatacageIDValue(data);
        addStringData("ids", code);
        if (code != null) {
            String [] parts = code.split("-");

            if (parts.length >= 1) {
                boolean official = parts[0].toLowerCase()
                    .startsWith("official");
                addStringData("official", official ? "1" : "0");
            }

            if (parts.length >= 4) {
                int col = Integer.parseInt(parts[2]);
                int wst = Integer.parseInt(parts[3]);

                addStringData("col_pos", parts[2]);
                addStringData("wst_id",  parts[3]);
            }
        }

        // Do this AFTER we have set the col_pos etc.
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
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
        log.debug("StaticWQKmsArtifact.initialize");
        D4EArtifact flys = (D4EArtifact) artifact;
        // TODO: The river is of no interest, so far., also use importData
        importData(flys, "river");

        List<Facet> fs = new ArrayList<Facet>();

        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);
        if (!fs.isEmpty()) {
            log.debug("Facets to add in StaticWQKmsArtifact.initialize .");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in StaticWQKmsArtifact.initialize ("
                + state.getID() + ").");
        }
    }


    /**
     * Get WQKms from factory.
     * @return WQKms according to parameterization (can be null);
     */
    public WQKms getWQKms() {
        log.debug("StaticWQKmsArtifact.getWQKms");

        int col = Integer.parseInt(getDataAsString("col_pos"));
        int wst = Integer.parseInt(getDataAsString("wst_id"));

        /** TODO do not run twice against db to do this. */
        String wkmsName = WKmsFactory.getWKmsName(col, wst);

        WQKms res = WQKmsFactory.getWQKms(col, wst);
        res.setName(wkmsName);
        return res;
    }

    /** Return specific name. */
    @Override
    public String getName() {
        return NAME;
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

        WKms wkms = getWQKms();

        double km = csl.getKm();

        // Find W at km.
        double wAtKm;

        // If heightmarks, only deliver if data snaps.
        /*
        if (getDataAsString(DATA_HEIGHT_TYPE) != null &&
            getDataAsString(DATA_HEIGHT_TYPE).equals("true")) {
            wAtKm = getWAtCloseKm(wkms, km, next, prev);
        }
        else {
        */
            wAtKm = StaticWKmsArtifact.getWAtKm(wkms, km);
        //}

        if (wAtKm == -1 || Double.isNaN(wAtKm)) {
            log.warn("Waterlevel at km " + km + " unknown.");
            return new Lines.LineData(new double[][] {{}}, 0d, 0d);
        }

        return Lines.createWaterLines(points, wAtKm);
    }
    // TODO implement deepCopy.
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
