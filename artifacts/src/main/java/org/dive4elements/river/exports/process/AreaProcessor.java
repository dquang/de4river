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


import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.geom.Lines;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.artifacts.model.AreaFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

import org.jfree.data.xy.XYSeries;

public class AreaProcessor extends DefaultProcessor {

    private static final Logger log = LogManager.getLogger(AreaProcessor.class);

    protected XYSeries getSeries(
        String seriesName,
        Object data,
        String facetType,
        ThemeDocument theme
    ) {
        if (data == null || facetType == null) {
            return null;
        }

        XYSeries series = new StyledXYSeries(seriesName, false, theme);

        if (facetType.equals(FacetTypes.DISCHARGE_LONGITUDINAL_C)) {
            WQCKms wqckms = (WQCKms) data;
            int size = wqckms.size();
            for (int i = 0; i < size; i++) {
                series.add(wqckms.getKm(i), wqckms.getC(i), false);
            }
        } else if (data instanceof WQKms) {
            if (FacetTypes.IS.Q(facetType)) {
                StyledSeriesBuilder.addPointsKmQ(series, (WQKms) data);
            }
            else {
                StyledSeriesBuilder.addPoints(series, (WKms) data);
            }
        }
        else if (data instanceof double[][]) {
            StyledSeriesBuilder.addPoints(series, (double [][]) data, false);
        }
        else if (data instanceof WKms) {
            StyledSeriesBuilder.addPoints(series, (WKms) data);
        }
        else if (data instanceof Lines.LineData) {
            StyledSeriesBuilder.addPoints(
                series, ((Lines.LineData) data).points, false);
        }
        else {
            log.error("Do not know how to deal with (up) area info from: "
                    + data);
        }
        return series;
    }


    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        String seriesName = bundle.getFacetDescription();
        StyledAreaSeriesCollection area = new StyledAreaSeriesCollection(theme);

        log.debug("Area Processor processing: " + seriesName);

        AreaFacet.Data data = (AreaFacet.Data) bundle.getData(context);

        String lowerFacetName = data.getLowerFacetName();
        String upperFacetName = data.getUpperFacetName();

        XYSeries up = getSeries(
            seriesName, data.getUpperData(), upperFacetName, theme);
        XYSeries down = getSeries(
            seriesName + " ", data.getLowerData(), lowerFacetName, theme);


        // TODO Depending on style, the area (e.g. 20m^2)
        // should be added as annotation.

        if (up == null && down != null) {
            area.setMode(StyledAreaSeriesCollection.FILL_MODE.ABOVE);
            down.setKey(seriesName);
            area.addSeries(down);
            area.addSeries(StyledSeriesBuilder.createGroundAtInfinity(down));
        }
        else if (up != null && down == null) {
            area.setMode(StyledAreaSeriesCollection.FILL_MODE.UNDER);
            area.addSeries(up);
            area.addSeries(StyledSeriesBuilder.createGroundAtInfinity(up));
        }
        else if (up != null && down != null) {
            if (data.doPaintBetween()) {
                area.setMode(StyledAreaSeriesCollection.FILL_MODE.BETWEEN);
            }
            else {
                area.setMode(StyledAreaSeriesCollection.FILL_MODE.ABOVE);
            }
            area.addSeries(up);
            area.addSeries(down);
        }

        String facetNameForAxis = lowerFacetName == null
            ? upperFacetName
            : lowerFacetName;
        /* Decide axis name based on facet name */
        generator.addAreaSeries(area,
                axisNameForFacet(facetNameForAxis), visible);
    }

    /** Look up the axis identifier for a given facet type. */
    private String axisNameForFacet(String facetName) {
        if (FacetTypes.W_DIFFERENCES.equals(facetName)) {
            return "diffW";
        }
        if (FacetTypes.IS.W(facetName) ||
            FacetTypes.DISCHARGE_LONGITUDINAL_C.equals(facetName)) {
            return "W";
        }
        if (FacetTypes.IS.Q(facetName)) {
            return "Q";
        }
        log.warn("Could not find axis for facet " + facetName);
        return "W";
    }

    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }
        return FacetTypes.IS.AREA(facetType);
    }
}
