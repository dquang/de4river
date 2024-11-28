/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.util.ArrayList;
import java.util.List;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.exports.process.MiscDischargeProcessor;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.DoubleBounds;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StickyAxisAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;
import org.dive4elements.river.themes.ThemeDocument;

import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.river.artifacts.GaugeDischargeCurveArtifact;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;


/**
 * An OutGenerator that generates discharge curves.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DischargeCurveGenerator
extends      XYChartGenerator
implements   FacetTypes {

    /** Beware, in this implementation, the W axis is also in cm! */
    public static enum YAXIS {
        WCm(0),
        W(1);
        protected int idx;
        private YAXIS(int c) {
            idx = c;
        }
    }

    /** The log used in this generator. */
    private static Logger log =
        LogManager.getLogger(DischargeCurveGenerator.class);

    public static final String I18N_CHART_TITLE =
        "chart.discharge.curve.title";

    public static final String I18N_CHART_SUBTITLE =
        "chart.discharge.curve.subtitle";

    public static final String I18N_XAXIS_LABEL =
        "chart.discharge.curve.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
        "chart.discharge.curve.yaxis.label";

    public static final String I18N_CHART_TITLE_DEFAULT  = "Abflusskurven";
    public static final String I18N_XAXIS_LABEL_DEFAULT  = "Q [m\u00b3/s]";
    public static final String I18N_YAXIS_LABEL_DEFAULT  = "W [cm]";


    /**
     * Returns the PNP (Datum) of gauge, if at gauge, 0 otherwise.
     */
    public static double getCurrentGaugeDatum(
        double km,
        D4EArtifact artifact,
        double tolerance
    ) {
        // Look if there is a gauge at chosen km:
        // Get gauge which is defined for km
        Gauge gauge = new RiverAccess(artifact).getRiver()
            .determineGaugeAtStation(km);
        if (gauge == null) {
            log.error("No Gauge could be found at station " + km + "!");
            return 0d;
        }
        double subtractPNP = 0d;
        // Compare to km.
        if (Math.abs(km - gauge.getStation().doubleValue()) < tolerance) {
            subtractPNP = gauge.getDatum().doubleValue();
        }
        return subtractPNP;
    }


    /** Get the current Gauge datum with default distance tolerance. */
    public double getCurrentGaugeDatum() {
        return getCurrentGaugeDatum(getRange()[0],
            (D4EArtifact) getMaster(), 1e-4);
    }


    /** Overriden to show second axis also if no visible data present. */
    @Override
    protected void adjustAxes(XYPlot plot) {
        super.adjustAxes(plot);
        // XXX Hacking around that there were two axes shown in official Gauge
        // Discharge, the one from the WINFO module.
        // This should be made unecessary in a Q Diagram refactoring with
        // decent inheritance.
        if (getMaster() instanceof GaugeDischargeCurveArtifact) {
            GaugeDischargeCurveArtifact myMaster =
                (GaugeDischargeCurveArtifact) getMaster();
            State state = myMaster.getCurrentState(context);
            if (myMaster.STATIC_STATE_NAME.equals(state.getID())) {
                return;
            }
        }
        // End Hack

        if (getCurrentGaugeDatum() != 0d) {
            // Show the W[*m] axis even if there is no data.
            plot.setRangeAxis(1, createYAxis(YAXIS.W.idx));
            syncWAxisRanges();
        }
    }

    protected void syncWAxisRanges() {
        // Syncronizes the ranges of both W Axes to make sure
        // that the Data matches for both axes.
        Bounds boundsInMGauge = getYBounds(YAXIS.W.idx);
        Bounds boundsInCM = getYBounds(YAXIS.WCm.idx);

        if (boundsInMGauge == null || boundsInCM == null) {
            // One axis does not exist. Nothing to sync
            return;
        }

        // XXX Q-Symetry: I am assuming here that there can only
        // be a fixed Range for WinM as this is currently the only
        // thing that is configureable.
        Range fixedWinMRange = getRangeForAxisFromSettings(
                getYAxisWalker().getId(YAXIS.W.idx));

        // The combination of Range and Bounds is crazy..
        if (fixedWinMRange != null) {
            boundsInMGauge = new DoubleBounds(fixedWinMRange.getLowerBound(),
                    fixedWinMRange.getUpperBound());
        }

        log.debug("Syncing Axis Bounds. Bounds W: "
            + boundsInMGauge.toString()
            + " Bounds Wcm: " + boundsInCM.toString());

        double datum = getCurrentGaugeDatum();

        // Convert boundsInMGauge to Datum+cm
        double convertedLower =
            ((Double)boundsInMGauge.getLower() - datum) * 100;
        double convertedUpper =
            ((Double)boundsInMGauge.getUpper() - datum) * 100;
        Bounds convertedBounds =
            new DoubleBounds(convertedLower, convertedUpper);

        // Now combine both Ranges
        boundsInCM = boundsInCM.combine(convertedBounds);

        // Recalculate absolute bounds
        boundsInMGauge = new DoubleBounds(
            (Double)boundsInCM.getLower() / 100d + datum,
            (Double)boundsInCM.getUpper() / 100d + datum);

        // Set the new combined bounds
        setYBounds(YAXIS.W.idx, boundsInMGauge);
        setYBounds(YAXIS.WCm.idx, boundsInCM);
        log.debug("Synced Bounds W: " + boundsInMGauge.toString() +
                " Bounds Wcm: " + boundsInCM.toString());
    }

    public DischargeCurveGenerator() {
        super();
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


    /**
     * Returns always null to suppress subtitles.
     */
    @Override
    protected String getDefaultChartTitle() {
        return null;
    }


    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL_DEFAULT);
    }

    @Override
    protected String getDefaultYAxisLabel(int pos) {
        return msg(I18N_YAXIS_LABEL, I18N_YAXIS_LABEL_DEFAULT);
    }


    /* TODO is this one really needed? */
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

        return zoomin;
    }

    /** Translate River annotations if a gauge. */
    public void translateRiverAnnotation(RiverAnnotation riverAnnotation) {
        if (getCurrentGaugeDatum() == 0d) {
            return;
        }
        log.debug("Translate some river annotation.");
        double translate = getCurrentGaugeDatum();
        double factor    = 100d;
        for (StickyAxisAnnotation annotation:
                 riverAnnotation.getAxisTextAnnotations()
        ){
            if (!annotation.atX()) {
                annotation.setPos((annotation.getPos() - translate)*factor);
            }
        }
        for (
            XYTextAnnotation annotation: riverAnnotation.getTextAnnotations()
        ) {
            annotation.setY((annotation.getY() - translate)*factor);
        }
    }


    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    theme,
        boolean          visible
    ) {
        String name = artifactFacet.getFacetName();
        log.debug("DischargeCurveGenerator.doOut: " + name);

        MiscDischargeProcessor dProcessor = new MiscDischargeProcessor(
            getRange()[0]);
        if (dProcessor.canHandle(name)) {
            // In Base DischargeCurveGenerator, always at gauge, use WCm axis.
            dProcessor.doOut(
                this, artifactFacet, theme, visible, YAXIS.WCm.idx);
        }
        else if (name.equals(DISCHARGE_CURVE)
                || name.equals(GAUGE_DISCHARGE_CURVE)) {
            doDischargeOut(
                (D4EArtifact)artifactFacet.getArtifact(),
                artifactFacet.getData(context),
                artifactFacet.getFacetDescription(),
                theme,
                visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            doPoints(artifactFacet.getData(context),
                artifactFacet,
                theme, visible, YAXIS.W.idx);
        }
        else if (STATIC_WQ.equals(name)) {
            doWQOut(artifactFacet.getData(context),
                artifactFacet,
                theme,
                visible);
        }
        else {
           log.warn("DischargeCurveGenerator.doOut: Unknown facet name: "
               + name);
           return;
        }
    }


    /**
     * Add series with discharge curve to diagram.
     */
    protected void doDischargeOut(
        D4EArtifact artifact,
        Object        o,
        String        description,
        ThemeDocument theme,
        boolean       visible)
    {
        log.debug("DischargeCurveGenerator.doDischargeOut");
        WQKms wqkms = (WQKms) o;

        String gaugeName = wqkms.getName();

        River river = new RiverAccess(artifact).getRiver();

        if (river == null) {
            log.debug("no river found");
            return;
        }

        Gauge gauge = river.determineGaugeByName(gaugeName);

        if (gauge == null) {
            log.debug("no gauge found");
            return;
        }

        XYSeries series = new StyledXYSeries(description, theme);

        StyledSeriesBuilder.addPointsQW(series, wqkms);

        addAxisSeries(series, YAXIS.W.idx, visible);
    }

    /**
     * Add W/Q-Series to plot.
     * @param wqkms actual data
     * @param theme theme to use.
     */
    protected void doQOut(
        Object           wqkms,
        ArtifactAndFacet aaf,
        ThemeDocument    theme,
        boolean          visible
    ) {
        log.debug("DischargeCurveGenerator: doQOut (add W/Q data).");
        XYSeries series = new StyledXYSeries(aaf.getFacetDescription(), theme);

        StyledSeriesBuilder.addPointsQW(series, (WQKms) wqkms);

        addAxisSeries(series, YAXIS.W.idx, visible);
    }


    /** Add a point annotation at given x and y coordinates. */
    protected void addPointTextAnnotation(
        String title,
        double x,
        double y,
        ThemeDocument theme
    ) {
        List<XYTextAnnotation> textAnnos =
            new ArrayList<XYTextAnnotation>();
        XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                title,
                x,
                y);
        textAnnos.add(anno);
        RiverAnnotation flysAnno = new RiverAnnotation(
            null, null, null, theme);
        flysAnno.setTextAnnotations(textAnnos);
        addAnnotations(flysAnno);
    }


    /**
     * Return true if all values in data[0] are smaller than zero
     * (in imported data they are set to -1 symbolically).
     * Return false if data is null or empty
     */
    private static boolean hasNoDischarge(double[][] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        double[] qs = data[0];
        for (double q: qs) {
            if (q > 0d) {
                return false;
            }
        }

        return true;
    }


    /**
     * Add WQ Data to plot.
     * @param wq data as double[][]
     */
    protected void doWQOut(
        Object           wq,
        ArtifactAndFacet aaf,
        ThemeDocument    theme,
        boolean          visible
    ) {
        log.debug("DischargeCurveGenerator: doWQOut");
        double [][] data = (double [][]) wq;
        String title = aaf.getFacetDescription();

        double translate = getCurrentGaugeDatum();

        // If no Q values (i.e. all -1) found, add annotations.
        if (hasNoDischarge(data)) {
            List<StickyAxisAnnotation> xy =
                new ArrayList<StickyAxisAnnotation>();

            for (double y: data[1]) {
                if (translate != 0d) {
                    y = (y-translate)*100d;
                }

                xy.add(new StickyAxisAnnotation(
                    title,
                    (float) y,
                    StickyAxisAnnotation.SimpleAxis.Y_AXIS));
            }

            doAnnotations(
                new RiverAnnotation(title, xy),
                aaf, theme, visible);
            return;
        }

        // Otherwise add points.
        XYSeries series = new StyledXYSeries(title, theme);

        if (translate != 0d) {
            StyledSeriesBuilder.addPointsQW(series, data, -translate, 100d);
            addAxisSeries(series, YAXIS.W.idx, visible);
        }
        else {
            StyledSeriesBuilder.addPoints(series, data, true);
            addAxisSeries(series, YAXIS.W.idx, visible);
        }

        if (visible && theme.parseShowPointLabel()
            && data != null && data.length != 0) {

            double[] xs = data[0];
            double[] ys = data[1];
            for (int i = 0; i < xs.length; i++) {
                double x = xs[i];
                double y = ys[i];

                if (translate != 0d) {
                    y = (y-translate)*100d;
                }

                addPointTextAnnotation(title, x, y, theme);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
