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

import org.dive4elements.river.model.FlowVelocityMeasurementValue.FastFlowVelocityMeasurementValue;
import org.dive4elements.river.artifacts.model.FlowVelocityData;

public class FlowVelocityProcessor extends DefaultProcessor {

    private final static Logger log =
            LogManager.getLogger(FlowVelocityProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.flow_velocity.section.yaxis.label";
    public static final String I18N_AXIS_LABEL_DEFAULT =
        "Geschwindigkeit v [m/s]";

    protected String yAxisLabel;

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        Map<String, String> metaData = bundle.getFacet().getMetaData();
        StyledXYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(),
            theme);
        series.putMetaData(metaData, bundle.getArtifact(), context);
        yAxisLabel = metaData.get("Y");
        String facetName = bundle.getFacetName();
        Object data = bundle.getData(context);
        if (data == null) {
            // Check has been here before so we keep it for security reasons
            // this should never happen though.
            log.error("Data is null for facet: " + facetName);
            return;
        }
        double [][] points;

        if (facetName.equals(FacetTypes.FLOW_VELOCITY_TOTALCHANNEL) ||
                facetName.equals(
                    FacetTypes.FLOW_VELOCITY_TOTALCHANNEL_FILTERED)
        ) {
            FlowVelocityData fData = (FlowVelocityData) data;
            points = fData.getTotalChannelPoints();
        } else if (facetName.equals(FacetTypes.FLOW_VELOCITY_MAINCHANNEL) ||
                facetName.equals(
                    FacetTypes.FLOW_VELOCITY_MAINCHANNEL_FILTERED)
        ) {
            FlowVelocityData fData = (FlowVelocityData) data;
            points = fData.getMainChannelPoints(); // I hate facets!
        } else if (facetName.equals(FacetTypes.FLOW_VELOCITY_MEASUREMENT)) {
            FastFlowVelocityMeasurementValue fData =
                (FastFlowVelocityMeasurementValue) data;
            points = new double[][] {{fData.getStation()},{fData.getV()}};
        } else {
            log.error("Unknown facet name: " + facetName);
            return;
        }
        StyledSeriesBuilder.addPoints(series, points, true);
        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        return facettype.equals(FacetTypes.FLOW_VELOCITY_MAINCHANNEL_FILTERED)
            || facettype.equals(FacetTypes.FLOW_VELOCITY_MAINCHANNEL)
            || facettype.equals(FacetTypes.FLOW_VELOCITY_TOTALCHANNEL_FILTERED)
            || facettype.equals(FacetTypes.FLOW_VELOCITY_TOTALCHANNEL)
            || facettype.equals(FacetTypes.FLOW_VELOCITY_MEASUREMENT);
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
