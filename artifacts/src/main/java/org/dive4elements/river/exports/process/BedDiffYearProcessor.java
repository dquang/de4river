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
import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;


public class BedDiffYearProcessor
extends DefaultProcessor implements FacetTypes {

    private final static Logger log =
            LogManager.getLogger(BedDiffYearProcessor.class);

    protected static double GAP_TOLERANCE = 0.101d;

    protected String yAxisLabel;

    public static final String I18N_AXIS_LABEL =
        "chart.beddifference.yaxis.label.diff";
    public static final String I18N_AXIS_LABEL_DEFAULT =
        "delta S [cm]";

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible
    ) {
        CallContext context = generator.getCallContext();
        Map<String, String> metaData =
            bundle.getFacet().getMetaData(bundle.getArtifact(), context);
        yAxisLabel = metaData.get("Y");

        Object data = bundle.getData(context);
        if (data == null) {
            return;
        }

        if (!(data instanceof double[][])) {
            log.error("Can't process " + data.getClass().getName()
                + " objects");
            return;
        }
        double[][] bData = (double[][]) data;
        for (int N = bData[0].length, i = 0; i < N; i++) {
            // scale to cm
            bData[1][i] *= 100d;
        }

        StyledXYSeries series =
            new StyledXYSeries(bundle.getFacetDescription(), theme);
        series.putMetaData(metaData, bundle.getArtifact(), context);
        StyledSeriesBuilder.addPoints(series,
            bData,
            false,
            GAP_TOLERANCE);

        generator.addAxisSeries(series, axisName, visible);

        return;
    }

    @Override
    public boolean canHandle(String facetType) {
        return BED_DIFFERENCE_YEAR.equals(facetType)
            // from BedDifferencesYear
            || BED_DIFFERENCE_YEAR_FILTERED.equals(facetType);
            // from BedDifferencesYear
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        if (yAxisLabel != null && !yAxisLabel.isEmpty()) {
            return generator.msg(yAxisLabel, I18N_AXIS_LABEL_DEFAULT);
        }
        return generator.msg(
            I18N_AXIS_LABEL,
            I18N_AXIS_LABEL_DEFAULT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
