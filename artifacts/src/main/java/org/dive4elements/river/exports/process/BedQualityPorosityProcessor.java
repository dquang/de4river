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
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

public class BedQualityPorosityProcessor extends DefaultProcessor {

    private final static Logger log =
            LogManager.getLogger(BedQualityPorosityProcessor.class);

    public static final String I18N_AXIS_LABEL_DEFAULT =
        "Porosität [%]";
    public static final String I18N_AXIS_LABEL =
        "chart.bedquality.yaxis.label.porosity";

    protected String yAxisLabel;

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
        series.putMetaData(metaData, bundle.getArtifact(), context);
        yAxisLabel = metaData.get("Y");

        Object raw = bundle.getData(context);
        if (raw == null) {
            return;
        }
        if (!(raw instanceof double[][])) {
            log.error("Unknown data type: " + raw.getClass().getName());
            return;
        }

        double[][] values = (double[][])raw;
        StyledSeriesBuilder.addPoints(series, values, true);

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        String name = facettype.replace(".interpol","");
        return name.equals(FacetTypes.BED_QUALITY_POROSITY_TOPLAYER) ||
            name.equals(FacetTypes.BED_QUALITY_POROSITY_SUBLAYER) ||
            name.equals(FacetTypes.POROSITY);
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


