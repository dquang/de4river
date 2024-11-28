/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */


package org.dive4elements.river.exports.process;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import org.jfree.data.xy.XYSeries;
import org.jfree.chart.annotations.XYTextAnnotation;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

public class ManualPointsProcessor extends DefaultProcessor {

    private static final Logger log = LogManager.getLogger(
        ManualPointsProcessor.class);

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        String seriesName = bundle.getFacetDescription();
        XYSeries series = new StyledXYSeries(seriesName, theme);
        String jsonData = (String) bundle.getData(context);

        // Add text annotations for single points.
        List<XYTextAnnotation> xy = new ArrayList<XYTextAnnotation>();

        try {
            JSONArray points = new JSONArray(jsonData);
            for (int i = 0, P = points.length(); i < P; i++) {
                JSONArray array = points.getJSONArray(i);
                double x    = array.getDouble(0);
                double y    = array.getDouble(1);
                String name = array.getString(2);
                boolean act = array.getBoolean(3);
                if (!act) {
                    continue;
                }
                //log.debug(" x " + x + " y " + y );
                series.add(x, y, false);
                xy.add(new CollisionFreeXYTextAnnotation(name, x, y));
            }
        }
        catch(JSONException e){
            log.error("Could not decode json.");
        }

        RiverAnnotation annotation = new RiverAnnotation(
            null, null, null, theme);
        annotation.setTextAnnotations(xy);

        if (visible) {
            generator.addAnnotations(annotation);
        }

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }
        return FacetTypes.IS.MANUALPOINTS(facetType);
    }
}
