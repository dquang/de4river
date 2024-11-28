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
import org.jfree.data.xy.XYSeries;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.FlowVelocityData;
import org.dive4elements.river.model.FlowVelocityMeasurementValue.FastFlowVelocityMeasurementValue;

import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

/**
 * Add data to chart/generator.
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class QOutProcessor extends DefaultProcessor {

    public static final String I18N_LONGITUDINAL_LABEL =
        "chart.longitudinal.section.yaxis.second.label";

    public static final String
        I18N_LONGITUDINAL_LABEL_DEFAULT = "Q [m\u00b3/s]";

    /** Private log. */
    private static final Logger log =
            LogManager.getLogger(QOutProcessor.class);

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);
        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), theme);
        String facetName = bundle.getFacetName();

        if (facetName.equals(FacetTypes.FLOW_VELOCITY_DISCHARGE)) {
            if (data instanceof FlowVelocityData) {
                FlowVelocityData fData = (FlowVelocityData) data;
                StyledSeriesBuilder.addPoints(series, fData.getQPoints(), true);
            }
            else {
                FastFlowVelocityMeasurementValue fData =
                    (FastFlowVelocityMeasurementValue) data;
                double[][] points = new double[][] {
                    {fData.getStation()},{fData.getQ()}};
                StyledSeriesBuilder.addPoints(series, points, true);
            }
        } else {
            WQKms wqkms = (WQKms) data;
            StyledSeriesBuilder.addStepPointsKmQ(series, wqkms);
        }

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible,
            int              index)
    {
        CallContext context = generator.getCallContext();
        WQKms wqkms = (WQKms) bundle.getData(context);

        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), theme);

        StyledSeriesBuilder.addStepPointsKmQ(series, wqkms);

        generator.addAxisSeries(series, index, visible);

        /* Check if the diagram should be inverted*/
        generator.setInverted(wqkms.guessRTLData());
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(I18N_LONGITUDINAL_LABEL,
                I18N_LONGITUDINAL_LABEL_DEFAULT);
    }

    /**
     * Returns true if facettype is q-type.
     */
    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }

        if (facetType.equals(FacetTypes.STATIC_WQKMS_Q)
            || facetType.equals(FacetTypes.LONGITUDINAL_Q)
            || facetType.startsWith(FacetTypes.DISCHARGE_LONGITUDINAL_Q)
            || facetType.startsWith(FacetTypes.FLOW_VELOCITY_DISCHARGE)) {
            return true;
        }
        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
