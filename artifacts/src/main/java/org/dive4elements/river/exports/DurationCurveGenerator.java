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
import org.dive4elements.river.artifacts.model.WQDay;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

import java.awt.Font;
import java.awt.geom.Point2D;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;


/**
 * An OutGenerator that generates duration curves.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DurationCurveGenerator
extends      XYChartGenerator
implements   FacetTypes
{
    public static enum YAXIS {
        W(0),
        Q(1);
        public int idx;
        private YAXIS(int c) {
           idx = c;
        }
    }

    /** Local log. */
    private static Logger log =
        LogManager.getLogger(DurationCurveGenerator.class);

    public static final String I18N_CHART_TITLE =
        "chart.duration.curve.title";

    public static final String I18N_CHART_SUBTITLE =
        "chart.duration.curve.subtitle";

    public static final String I18N_XAXIS_LABEL =
        "chart.duration.curve.xaxis.label";

    public static final String I18N_YAXIS_LABEL_W =
        "chart.duration.curve.yaxis.label.w";

    public static final String I18N_YAXIS_LABEL_Q =
        "chart.duration.curve.yaxis.label.q";

    public static final String I18N_CHART_TITLE_DEFAULT  =
        "Dauerlinie";

    public static final String I18N_XAXIS_LABEL_DEFAULT  =
        "Unterschreitungsdauer [Tage]";


    public DurationCurveGenerator() {
        super();
    }


    /**
     * Create Axis for given index.
     * @return axis with according internationalized label.
     */
    @Override
    protected NumberAxis createYAxis(int index) {
        Font labelFont = new Font("Tahoma", Font.BOLD, 14);
        String label   = getYAxisLabel(index);

        NumberAxis axis = createNumberAxis(index, label);
        if (index == YAXIS.W.idx) {
            axis.setAutoRangeIncludesZero(false);
        }
        axis.setLabelFont(labelFont);
        return axis;
    }


    @Override
    protected String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE_DEFAULT);
    }


    @Override
    protected String getDefaultChartSubtitle() {
        double[] dist  = getRange();

        Object[] args = new Object[] {
            getRiverName(),
            dist[0]
        };

        return msg(I18N_CHART_SUBTITLE, "", args);
    }


    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL_DEFAULT);
    }


    @Override
    protected String getDefaultYAxisLabel(int index) {
        String label = "default";
        if (index == YAXIS.W.idx) {
            label = msg(I18N_YAXIS_LABEL_W, new Object[] { getRiverUnit() });
        }
        else if (index == YAXIS.Q.idx) {
            label = msg(I18N_YAXIS_LABEL_Q);
        }

        return label;
    }


    @Override
    protected boolean zoomX(
        XYPlot plot,
        ValueAxis axis,
        Bounds bounds,
        Range x
    ) {
        boolean zoomin = super.zoom(plot, axis, bounds, x);

        if (!zoomin) {
            axis.setLowerBound(0d);
        }

        axis.setUpperBound(364);

        return zoomin;
    }


    /**
     * This method overrides the method in the parent class to set the lower
     * bounds of the Q axis to 0. This axis should never display negative
     * values on its own.
     */
    @Override
    protected boolean zoomY(
        XYPlot plot,
        ValueAxis axis,
        Bounds bounds,
        Range x
    ) {
        boolean zoomin = super.zoom(plot, axis, bounds, x);

        if (!zoomin && axis instanceof IdentifiableNumberAxis) {
            String id = ((IdentifiableNumberAxis) axis).getId();

            if (YAXIS.Q.toString().equals(id)) {
                axis.setLowerBound(0d);
            }
        }

        return zoomin;
    }


    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    attr,
        boolean          visible
    ) {
        String name = artifactFacet.getFacetName();

        log.debug("DurationCurveGenerator.doOut: " + name);

        if (name == null || name.length() == 0) {
            log.error("No facet given. Cannot create dataset.");
            return;
        }

        if (name.equals(DURATION_W)) {
            doWOut(
                (WQDay) artifactFacet.getData(context),
                artifactFacet,
                attr,
                visible);
        }
        else if (name.equals(DURATION_Q)) {
            doQOut(
                (WQDay) artifactFacet.getData(context),
                artifactFacet,
                attr,
                visible);
        }
        else if (name.equals(MAINVALUES_Q) || name.equals(MAINVALUES_W)) {
            doAnnotations(
                (RiverAnnotation) artifactFacet.getData(context),
                artifactFacet,
                attr,
                visible);
        }
        else if (name.equals(RELATIVE_POINT)) {
            doPointOut((Point2D) artifactFacet.getData(context),
                artifactFacet,
                attr,
                visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            doPoints(
                artifactFacet.getData(context),
                artifactFacet,
                attr, visible, YAXIS.W.idx);
        }
        else {
            log.warn("Unknown facet name: " + name);
            return;
        }
    }


    /**
     * Creates the series for a duration curve's W facet.
     *
     * @param wqdays The WQDay store that contains the Ws.
     * @param theme
     */
    protected void doWOut(
        WQDay            wqdays,
        ArtifactAndFacet aaf,
        ThemeDocument    theme,
        boolean          visible
    ) {
        log.debug("DurationCurveGenerator.doWOut");

        XYSeries series = new StyledXYSeries(aaf.getFacetDescription(), theme);

        int size = wqdays.size();
        for (int i = 0; i < size; i++) {
            int  day = wqdays.getDay(i);
            double w = wqdays.getW(i);

            series.add(day, w);
        }

        addAxisSeries(series, YAXIS.W.idx, visible);
    }

    protected void doPointOut(
        Point2D          point,
        ArtifactAndFacet aandf,
        ThemeDocument    theme,
        boolean          visible
    ){
        log.debug("DurationCurveGenerator.doPointOut");

        XYSeries series =
            new StyledXYSeries(aandf.getFacetDescription(), theme);

        series.add(point.getX(), point.getY());

        addAxisSeries(series, YAXIS.W.idx, visible);
    }


    /**
     * Creates the series for a duration curve's Q facet.
     *
     * @param wqdays The WQDay store that contains the Qs.
     * @param theme
     */
    protected void doQOut(
        WQDay            wqdays,
        ArtifactAndFacet aaf,
        ThemeDocument    theme,
        boolean          visible
    ) {
        log.debug("DurationCurveGenerator.doQOut");

        XYSeries series = new StyledXYSeries(aaf.getFacetDescription(), theme);

        int size = wqdays.size();
        for (int i = 0; i < size; i++) {
            int  day = wqdays.getDay(i);
            double q = wqdays.getQ(i);

            series.add(day, q);
        }

        addAxisSeries(series, YAXIS.Q.idx, visible);
    }


    @Override
    protected YAxisWalker getYAxisWalker() {
        return new YAxisWalker() {
            @Override
            public int length() {
                return YAXIS.values().length;
            }

            @Override
            public String getId(int idx) {
                YAXIS[] yaxes = YAXIS.values();
                return yaxes[idx].toString();
            }
        };
    }

    // MainValue-Annotations should be visualized by
    // a line that goes to the curve itself.
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
