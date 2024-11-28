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
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

public class BedHeightProcessor extends DefaultProcessor {

    private final static Logger log =
            LogManager.getLogger(BedHeightProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.bedheight_middle.section.yaxis.label";

    protected static final double GAP_TOLERANCE = 0.101d;

    protected String yAxisLabel;

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        XYSeries series = prepareSeries(
            bundle, theme, generator.getCallContext());
        if (series != null) {
            generator.addAxisSeries(series, axisName, visible);
        }
    }

    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument theme,
            boolean visible,
            int index
    ) {
        XYSeries series = prepareSeries(
            bundle, theme, generator.getCallContext());
        if (series != null) {
            generator.addAxisSeries(series, index, visible);
        }
    }

    /** Prepare an series, independent of axis. */
    private XYSeries prepareSeries(
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        CallContext context
    ) {
        Map<String, String> metaData = bundle.getFacet().getMetaData(
            bundle.getArtifact(), context);
        StyledXYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                theme);
        series.putMetaData(metaData, bundle.getArtifact(), context);
        yAxisLabel = metaData.get("Y");

        Object raw = bundle.getData(context);
        if (raw == null) {
            return null;
        }
        if (!(raw instanceof double[][])) {
            log.error("Unkown datatype: " + raw.getClass().getName());
            return null;
        }

        double[][] data = (double[][])raw;
        StyledSeriesBuilder.addPoints(series,
            data,
            false,
            GAP_TOLERANCE);
        return series;
    }


    @Override
    public boolean canHandle(String facetType) {
        return FacetTypes.BEDHEIGHT.equals(facetType)
            || FacetTypes.BED_DIFFERENCE_YEAR_HEIGHT1.equals(facetType)
            || FacetTypes.BED_DIFFERENCE_YEAR_HEIGHT2.equals(facetType)
            || FacetTypes.BED_DIFFERENCE_YEAR_HEIGHT1_FILTERED.equals(
                facetType)
            || FacetTypes.BED_DIFFERENCE_YEAR_HEIGHT2_FILTERED.equals(
                facetType);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        D4EArtifact flys = (D4EArtifact) generator.getMaster();
        String unit = new RiverAccess(flys).getRiver().getWstUnit().getName();

        CallMeta meta = generator.getCallContext().getMeta();

        if (yAxisLabel != null && !yAxisLabel.isEmpty()) {
            return Resources.getMsg(
                meta,
                yAxisLabel,
                new Object[] {unit});
        }
        return Resources.getMsg(
                meta,
                I18N_AXIS_LABEL,
                new Object[] { unit });
    }
}
