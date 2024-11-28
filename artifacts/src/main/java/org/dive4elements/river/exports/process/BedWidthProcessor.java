/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.Map;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

import org.dive4elements.river.artifacts.model.minfo.MorphologicWidth;

public class BedWidthProcessor extends DefaultProcessor {

    private final static Logger log =
            LogManager.getLogger(BedWidthProcessor.class);

    public static final String I18N_AXIS_LABEL_DEFAULT =
        "Breite [m]";
    public static final String I18N_AXIS_LABEL =
        "chart.beddifference.yaxis.label.morph";

    protected String yAxisLabel;

    public static final double GAP_WIDTH = 0.101;

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        Map<String, String> metaData = bundle.getFacet().getMetaData();
        StyledXYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                theme);
        if (!metaData.isEmpty()) {
            series.putMetaData(metaData, bundle.getArtifact(), context);
            yAxisLabel = metaData.get("Y");
        }
        Object data = bundle.getData(context);

        if (data instanceof MorphologicWidth) {
            MorphologicWidth bData = (MorphologicWidth) data;
            StyledSeriesBuilder.addPoints(series, bData.getAsArray(), true);
        } else if (data instanceof double[][]) {
            double[][]values = (double[][]) data;
            StyledSeriesBuilder.addPoints(series,
                values,
                false,
                GAP_WIDTH);
        } else {
            log.error("Unknown data for facet: " + bundle.getFacetName());
        }

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        return facettype.equals(FacetTypes.MORPHOLOGIC_WIDTH) ||
            facettype.equals(FacetTypes.BEDHEIGHT_SOUNDING_WIDTH);
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
