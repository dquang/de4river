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
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.geom.Lines;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.model.FastCrossSectionLine;


/**
 * Artifact to store user-added points and water lines.
 */
public class ManualPointsArtifact
extends      StaticD4EArtifact
implements   FacetTypes, WaterLineArtifact
{
    private static final long serialVersionUID = 7096025125474986011L;

    /** The log for this class. */
    private static Logger log = LogManager.getLogger(ManualPointsArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "manualpoints";


    public ManualPointsArtifact() {
        log.debug("ManualPointsArtifact.ManualPointsArtifact()");
    }


    /**
     * Gets called from factory to set things up.
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
        log.debug("ManualPointsArtifact.setup");
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
        initialize(null, context, callMeta);
    }


    /** Return the name of this artifact. */
    @Override
    public String getName() {
        return ARTIFACT_NAME;
    }


    /** Access state data storing the jsonstring with points. */
    public String getPointsData(String facetName) {
        return getDataAsString(facetName + ".data");
    }


    /**
     * Access state data storing the jsonstring with lines.
     * @param facetName Name of facet or null if the so far
     *                  only known case should be picked.
     * @return (String) value of data element (expect json).
     */
    public String getLinesData(String facetName) {
        if (facetName == null)
            return getDataAsString("cross_section.manualpoints.lines");
        // TODO .lineS?
        return getDataAsString(facetName + ".line");
    }


    /** Setup state and facet. */
    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta
    ) {
        log.debug("ManualPointsArtifact.initialize");
        List<Facet> fs = new ArrayList<Facet>();

        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);
        if (!fs.isEmpty()) {
            log.debug("Facets to add in ManualPointsArtifact.initialize .");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in ManualPointsArtifact.initialize ("
                    + state.getID() + ").");
        }
    }


    /**
     * Get value of line at index.
     * @param index index in json array defining lines.
     * @return water height of line at given index.
     */
    protected double getLine(int index) {
        try {
            JSONArray lines = new JSONArray(getLinesData(null));
            JSONArray array = lines.getJSONArray(index);

            return array.getDouble(0);
        }
        catch(JSONException e){
            log.error("Could not decode json for line.");
            return 0d;
        }
    }


    /**
     * Get the water line "surface".
     * @param index index of facets data.
     * @param csl 'ground' against which to determine water surface.
     * @param a (ignored in this implementation).
     * @param b (ignored in this implementation).
     * @param context (ignored in this implementation).
     */
    @Override
    public Lines.LineData getWaterLines(
            int                  index,
            FastCrossSectionLine csl,
            double a, double b,
            CallContext context
            ) {
        List<Point2D> points = csl.getPoints();
        return Lines.createWaterLines(points, getLine(index));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
