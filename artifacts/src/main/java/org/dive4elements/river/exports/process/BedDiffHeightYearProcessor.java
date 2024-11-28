/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.data.xy.XYSeries;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;


public class BedDiffHeightYearProcessor
extends DefaultProcessor implements FacetTypes {

    private final static Logger log =
            LogManager.getLogger(BedDiffHeightYearProcessor.class);

    protected static double GAP_TOLERANCE = 0.101d;

    public static final String I18N_AXIS_LABEL =
        "chart.beddifference.height.yaxis.label";
    public static final String I18N_AXIS_LABEL_DEFAULT =
        "delta S [cm / Jahr]";

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible
    ) {
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);
        Map<String, String> metaData = bundle.getFacet().getMetaData(
            bundle.getArtifact(), context);

        if (!(data instanceof double[][])) {
            // Should not happen if canHandle is correct
            log.error("Can't process " + data.getClass().getName()
                + " objects");
            return;
        }

        double[][] bData = (double[][]) data;

        StyledXYSeries series =
            new StyledXYSeries(bundle.getFacetDescription(), theme);
        series.putMetaData(metaData, bundle.getArtifact(), context);

        StyledSeriesBuilder.addPoints(series, bData, false, GAP_TOLERANCE);

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument theme,
            boolean visible,
            int index
    ) {
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);

        if (!(data instanceof double[][])) {
            // Should not happen if canHandle is correct
            log.error("Can't process " + data.getClass().getName()
                + " objects");
            return;
        }

        double[][] bData = (double[][]) data;
        XYSeries series =
            new StyledXYSeries(bundle.getFacetDescription(), theme);
        StyledSeriesBuilder.addPoints(series, bData, false, GAP_TOLERANCE);

        generator.addAxisSeries(series, index, visible);
    }

    @Override
    public boolean canHandle(String facetType) {
        return BED_DIFFERENCE_HEIGHT_YEAR.equals(facetType)
            // from BedDiffYearHeight
            || BED_DIFFERENCE_HEIGHT_YEAR_FILTERED.equals(facetType);
            // from BedDiffYearHeight
    }


    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT);
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
