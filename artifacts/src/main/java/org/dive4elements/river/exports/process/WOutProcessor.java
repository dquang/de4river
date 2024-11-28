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
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.model.FlowVelocityMeasurementValue.FastFlowVelocityMeasurementValue;
import org.dive4elements.river.utils.RiverUtils;

/**
 * Add data to chart/generator.
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class WOutProcessor extends DefaultProcessor {

    /** Private log. */
    private static final Logger log =
            LogManager.getLogger(WOutProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.longitudinal.section.yaxis.label";

    public static final String I18N_AXIS_LABEL_DEFAULT  = "W [NN + m]";

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible
    ) {
        log.debug("Processing facet: " + bundle.getFacetName());
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);

        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), theme);

        // Handle non WKms data.
        if (
            bundle.getFacetName().equals(FacetTypes.FLOW_VELOCITY_WATERLEVEL)
        ) {
            FastFlowVelocityMeasurementValue fData =
                (FastFlowVelocityMeasurementValue) data;
            double[][] points = new double[][] {
                {fData.getStation()},{fData.getW()}};
            StyledSeriesBuilder.addPoints(series, points, true);
            generator.addAxisSeries(series, axisName, visible);
            return;
        }

        // Handle WKms data.
        WKms wkms = (WKms) data;

        if (
            bundle.getFacetName().equals(FacetTypes.DISCHARGE_LONGITUDINAL_C)
        ) {
            // Add corrected values
            WQCKms wqckms = (WQCKms) data;
            int size = wqckms.size();
            for (int i = 0; i < size; i++) {
                series.add(wqckms.getKm(i), wqckms.getC(i), false);
            }
        } else {
            StyledSeriesBuilder.addPoints(series, wkms);
        }
        generator.addAxisSeries(series, axisName, visible);

        // If a "band around the curve shall be drawn, add according area.
        double bandWidth = theme.parseBandWidth();
        if (bandWidth > 0 ) {
            XYSeries seriesDown = new StyledXYSeries(
                "band " + bundle.getFacetDescription(), false, theme);
            XYSeries seriesUp = new StyledXYSeries(
                bundle.getFacetDescription()+"+/-"+bandWidth, false, theme);
            StyledSeriesBuilder.addUpperBand(seriesUp, wkms, bandWidth);
            StyledSeriesBuilder.addLowerBand(seriesDown, wkms, bandWidth);

            StyledAreaSeriesCollection area =
                new StyledAreaSeriesCollection(theme);
            area.addSeries(seriesUp);
            area.addSeries(seriesDown);
            area.setMode(StyledAreaSeriesCollection.FILL_MODE.BETWEEN);
            generator.addAreaSeries(area, axisName, visible);
        }

        if (bundle.getFacetName().equals(FacetTypes.LONGITUDINAL_W)
            || bundle.getFacetName().equals(
                FacetTypes.DISCHARGE_LONGITUDINAL_W)
            || bundle.getFacetName().equals(
                FacetTypes.STATIC_WQKMS_W)
            || bundle.getFacetName().equals(
                FacetTypes.DISCHARGE_LONGITUDINAL_C)) {
            /* Only use W values to check if the diagram should be inverted
             * see flys/issue1290 for details */
            log.debug("Check for RTL data: "+ wkms.guessRTLData());
            generator.setInverted(wkms.guessRTLData());
        }
    }

    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible,
            int              index)
    {
        log.debug("doOut");

        CallContext context = generator.getCallContext();

        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), theme);

        WKms wkms = (WKms) bundle.getData(context);

        StyledSeriesBuilder.addPoints(series, wkms);
        generator.addAxisSeries(series, index, visible);

        // If a "band around the curve shall be drawn, add according area.
        double bandWidth = theme.parseBandWidth();
        if (bandWidth > 0 ) {
            XYSeries seriesDown = new StyledXYSeries(
                "band " + bundle.getFacetDescription(), false, theme);
            XYSeries seriesUp = new StyledXYSeries(
                bundle.getFacetDescription()+"+/-"+bandWidth, false, theme);
            StyledSeriesBuilder.addUpperBand(seriesUp, wkms, bandWidth);
            StyledSeriesBuilder.addLowerBand(seriesDown, wkms, bandWidth);

            StyledAreaSeriesCollection area =
                new StyledAreaSeriesCollection(theme);
            area.addSeries(seriesUp);
            area.addSeries(seriesDown);
            area.setMode(StyledAreaSeriesCollection.FILL_MODE.BETWEEN);
            generator.addAreaSeries(area, index, visible);
        }

        if (bundle.getFacetName().equals(FacetTypes.LONGITUDINAL_W)
            || bundle.getFacetName().equals(
                FacetTypes.DISCHARGE_LONGITUDINAL_W)
            || bundle.getFacetName().equals(
                FacetTypes.STATIC_WQKMS_W)) {
            /* Only use W values to check if the diagram should be inverted
             * see flys/issue1290 for details */
            generator.setInverted(wkms.guessRTLData());
        }
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        D4EArtifact flys = (D4EArtifact) generator.getMaster();

        String unit = RiverUtils.getRiver(flys).getWstUnit().getName();

        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT,
                new Object[] { unit });
    }

    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }

        if (facetType.equals(FacetTypes.LONGITUDINAL_W)
                || facetType.equals(FacetTypes.STATIC_WKMS)
                || facetType.equals(FacetTypes.HEIGHTMARKS_POINTS)
                || facetType.equals(FacetTypes.STATIC_WQKMS)
                || facetType.equals(FacetTypes.STATIC_WQKMS_W)
                || facetType.equals(FacetTypes.FLOW_VELOCITY_WATERLEVEL)
                || facetType.equals(FacetTypes.DISCHARGE_LONGITUDINAL_W)
                || facetType.equals(FacetTypes.DISCHARGE_LONGITUDINAL_C)) {
            return true;
        }
        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
