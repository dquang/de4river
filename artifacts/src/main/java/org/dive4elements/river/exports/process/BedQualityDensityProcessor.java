/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
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
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

public class BedQualityDensityProcessor extends DefaultProcessor {

    private final static Logger log =
            LogManager.getLogger(BedQualityDensityProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.bedquality.yaxis.label.density";

    protected String yAxisLabel;
    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        StyledXYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                theme);
        Map<String, String> metaData = bundle.getFacet().getMetaData();
        series.putMetaData(metaData, bundle.getArtifact(), context);
        yAxisLabel = metaData.get("Y");

        Object data = bundle.getData(context);
        if (data == null) {
            return;
        }
        if (!(data instanceof double[][])) {
            log.error("Unknown data type: " + data.getClass().getName());
            return;
        }

        double[][] values = (double[][])data;
        StyledSeriesBuilder.addPoints(series, values, true);

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        String name = facettype.replace(".interpol","");
        return name.equals(FacetTypes.BED_QUALITY_SEDIMENT_DENSITY_SUBLAYER) ||
            name.equals(FacetTypes.BED_QUALITY_SEDIMENT_DENSITY_TOPLAYER);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {

        CallMeta meta = generator.getCallContext().getMeta();

        if (yAxisLabel != null && !yAxisLabel.isEmpty()) {
            return Resources.getMsg(meta, yAxisLabel);
        }
        return Resources.getMsg(meta, I18N_AXIS_LABEL);
    }
}
