/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WW;
import org.dive4elements.river.artifacts.model.WW.ApplyFunctionIterator;
import org.dive4elements.river.artifacts.model.WWAxisTypes;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.Formatter;

import java.awt.geom.Point2D;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYSeries;

/**
 * An OutGenerator that generates reference curves.
 */
public class ReferenceCurveGenerator
extends      XYChartGenerator
implements   FacetTypes
{
    public static enum YAXIS {
        W(0);

        public int idx;
        private YAXIS(int c) {
           idx = c;
        }
    }

    /** House log. */
    private static Logger log =
        LogManager.getLogger(ReferenceCurveGenerator.class);

    public static final String I18N_CHART_TITLE =
        "chart.reference.curve.title";

    public static final String I18N_CHART_SUBTITLE =
        "chart.reference.curve.subtitle";

    public static final String I18N_X_AXIS_IN_CM =
        "chart.reference.curve.x.axis.in.cm";

    public static final String I18N_X_AXIS_IN_M =
        "chart.reference.curve.x.axis.in.m";

    public static final String I18N_Y_AXIS_IN_CM =
        "chart.reference.curve.y.axis.in.cm";

    public static final String I18N_Y_AXIS_IN_M =
        "chart.reference.curve.y.axis.in.m";

    public static final String I18N_CHART_TITLE_DEFAULT  =
        "Bezugslinie";


    public ReferenceCurveGenerator() {
    }

    /**
     * Create Axis for given index.
     * @return axis with according internationalized label.
     */
    @Override
    protected NumberAxis createYAxis(int index) {
        NumberAxis axis = super.createYAxis(index);
        axis.setAutoRangeIncludesZero(false);
        return axis;
    }


    /** Get default chart title. */
    @Override
    protected String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE_DEFAULT);
    }

    @Override
    protected String getDefaultChartSubtitle() {
        Object[] args = new Object[] {
            getRiverName(),
        };

        return msg(I18N_CHART_SUBTITLE, "", args);
    }


    /** True if axis is in cm (because at gauge). */
    protected boolean getInCm(int index) {
        Object obj = context.getContextValue("reference.curve.axis.scale");
        return obj instanceof WWAxisTypes && ((WWAxisTypes)obj).getInCm(index);
    }


    /** Get Label for X-axis (W). */
    @Override
    protected String getDefaultXAxisLabel() {
        return msg(getInCm(0) ? I18N_X_AXIS_IN_CM : I18N_X_AXIS_IN_M);
    }


    /**
     * Get Label for primary and other Y Axes.
     * @param index Axis-Index (0-based).
     */
    @Override
    protected String getDefaultYAxisLabel(int index) {
        return msg(getInCm(1) ? I18N_Y_AXIS_IN_CM : I18N_Y_AXIS_IN_M);
    }

    protected String facetName() {
        return REFERENCE_CURVE;
    }


    /**
     * Called for each facet/them in the out mapped to this generator.
     * @param artifactFacet artifact and facet for this theme.
     * @param theme         styling info.
     * @param visible       Whether or not the theme is visible.
     */
    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    theme,
        boolean          visible
    ) {
        String name = artifactFacet.getFacetName();

        log.debug("ReferenceCurveGenerator.doOut: " + name);

        if (name == null || name.length() == 0) {
            log.error("No facet given. Cannot create dataset.");
            return;
        }

        if (name.equals(facetName())) {
            doReferenceOut(artifactFacet.getData(context), theme, visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            doPoints(
                artifactFacet.getData(context),
                artifactFacet,
                theme,
                visible,
                YAXIS.W.idx);
        }
        else if (name.equals(RELATIVE_POINT)) {
            doPointOut(
                (Point2D) artifactFacet.getData(context),
                artifactFacet,
                theme,
                visible);
        }
        else if (name.equals(MAINVALUES_W)) {
            doAnnotations(
                ((RiverAnnotation)artifactFacet.getData(context))
                    .flipStickyAxis(),
                artifactFacet,
                theme,
                visible);

        }
        else {
            log.warn("Unknown facet name: " + name);
        }
    }

    protected boolean doNormalize() {
        return false;
    }


    /** Register DataSeries with (maybe transformed) points. */
    public void doReferenceOut(
        Object        data,
        ThemeDocument theme,
        boolean       visible
    ) {
        WW ww = (WW)data;

        Object obj = context.getContextValue("reference.curve.axis.scale");

        WWAxisTypes wwat = obj instanceof WWAxisTypes
            ? (WWAxisTypes)obj
            : new WWAxisTypes(ww);

        ApplyFunctionIterator iter = wwat.transform(ww, doNormalize());

        XYSeries series = new StyledXYSeries(
            ww.getName(), false, theme);

        double [] values = new double[2];

        while (iter.hasNext()) {
            iter.next(values);
            series.add(values[0], values[1], false);
        }

        addAxisSeries(series, YAXIS.W.idx, visible);
    }

    // TODO resolve duplicate in DurationCurveGenerator
    protected void doPointOut(
        Point2D          point,
        ArtifactAndFacet aandf,
        ThemeDocument    theme,
        boolean          visible
    ){
        log.debug("ReferenceCurveGenerator.doPointOut");

        XYSeries series =
            new StyledXYSeries(aandf.getFacetDescription(), theme);

        series.add(point.getX(), point.getY());

        addAxisSeries(series, YAXIS.W.idx, visible);
    }


    /** Set the tick units for given axis. */
    protected void setAxisTickUnit(double tick, ValueAxis axis) {
        TickUnits units = new TickUnits();
        units.add(new NumberTickUnit(tick, Formatter.getWaterlevelW(context)));
        axis.setStandardTickUnits(units);
        axis.setAutoTickUnitSelection(true);
    }

    @Override
    protected void localizeDomainAxis(ValueAxis domainAxis) {
        super.localizeDomainAxis(domainAxis);
        if (getInCm(0)) {
            setAxisTickUnit(100d, domainAxis);
        }
        else {
            setAxisTickUnit(1d, domainAxis);
        }
    }


    @Override
    protected void localizeRangeAxis(ValueAxis rangeAxis) {
        super.localizeRangeAxis(rangeAxis);
        setAxisTickUnit(1d, rangeAxis);
    }

    /** Get Walker to iterate over all axes. */
    @Override
    protected YAxisWalker getYAxisWalker() {
        return new YAxisWalker() {
            /** Get number of items. */
            @Override
            public int length() {
                return YAXIS.values().length;
            }

            /** Get identifier for this index. */
            @Override
            public String getId(int idx) {
                YAXIS[] yaxes = YAXIS.values();
                return yaxes[idx].toString();
            }
        };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
