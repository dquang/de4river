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
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;


public class FixDeltaWProcessor
extends DefaultProcessor implements FacetTypes {

    private final static Logger log =
            LogManager.getLogger(FixDeltaWProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.beddifference.yaxis.label.diff";
    public static final String I18N_AXIS_LABEL_DEFAULT =
        "delta S [cm]";

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

        // Handle WKms data.
        WKms wkms = (WKms) data;

        StyledSeriesBuilder.addPoints(series, wkms);
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
    }

    @Override
    public boolean canHandle(String facetType) {
        return STATIC_DELTA_W.equals(facetType);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
