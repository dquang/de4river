/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.StaticWKmsArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.access.FixAnalysisAccess;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.NamedDouble;
import org.dive4elements.river.artifacts.model.QWDDateRange;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.fixings.FixFunction;
import org.dive4elements.river.artifacts.model.fixings.FixWQCurveFacet;
import org.dive4elements.river.artifacts.model.fixings.QWD;
import org.dive4elements.river.artifacts.model.fixings.QWI;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.ChartGenerator;
import org.dive4elements.river.exports.DischargeCurveGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.JFreeUtil;
import org.dive4elements.river.jfree.StickyAxisAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.java2d.ShapeUtils;

import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.DoubleBounds;

import org.jfree.data.Range;

/**
 * Generator for WQ fixing charts.
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 */
public class FixWQCurveGenerator
extends      FixChartGenerator
implements   FacetTypes
{
    /** Private log. */
    private static Logger log =
            LogManager.getLogger(FixWQCurveGenerator.class);

    public static final String I18N_CHART_TITLE =
            "chart.fixings.wq.title";

    public static final String I18N_CHART_SUBTITLE =
            "chart.fixings.wq.subtitle";

    public static final String I18N_CHART_SUBTITLE1 =
            "chart.fixings.wq.subtitle1";

    public static final String I18N_XAXIS_LABEL =
            "chart.fixings.wq.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
            "chart.fixings.wq.yaxis.label";

    public static final String I18N_CHART_TITLE_DEFAULT  =
            "Fixierungsanalyse";

    public static final String I18N_XAXIS_LABEL_DEFAULT  =
            "Q [m\u00B3/s]";

    public static final String I18N_YAXIS_LABEL_DEFAULT  =
            "W [NN + m]";

    public static final double EPSILON = 0.001d;

    public static enum YAXIS {
        WCm(0),
        W(1);
        public int idx;
        private YAXIS(int c) {
            idx = c;
        }
    }


    /** Needed to access data to create subtitle. */
    protected D4EArtifact artifact;

    /** Returns value != 0 if the current km is not at a gauge. */
    public double getCurrentGaugeDatum() {
        Object ckm = context.getContextValue(CURRENT_KM);
        if (ckm != null) {
            return DischargeCurveGenerator.getCurrentGaugeDatum(
                (Double) ckm,
                (D4EArtifact) getMaster(), 1e-4);
        }
        return 0d;
    }

    /** Overriden to show second axis also if no visible data present. */
    @Override
    protected void adjustAxes(XYPlot plot) {
        super.adjustAxes(plot);
        if (getCurrentGaugeDatum() != 0d) {
            // Show the W[*m] axis even if there is no data.
            plot.setRangeAxis(1, createYAxis(YAXIS.W.idx));
            syncWAxisRanges();
        }
    }

    // XXX This is a copy of DischargeCurveGenerator syncWAxisRanges
    // even without fancy Q Symetry this class should inherit
    // from there..
    protected void syncWAxisRanges() {
        // Syncronizes the ranges of both W Axes to make sure
        // that the Data matches for both axes.
        Bounds boundsInMGauge = getYBounds(YAXIS.W.idx);
        Bounds boundsInCM = getYBounds(YAXIS.WCm.idx);

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

    @Override
    public void doOut(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        log.debug("doOut: " + aaf.getFacetName());
        if (!prepareChartData(aaf, doc, visible)) {
            log.warn("Unknown facet, name " + aaf.getFacetName());
        }
    }

    /**
     * Return true if data could be handled,
     * to be overridden to add more handled data.
     */
    public boolean prepareChartData(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        String name = aaf.getFacetName();

        this.artifact = (D4EArtifact) aaf.getArtifact();

        if(name.startsWith(FIX_SECTOR_AVERAGE_WQ)) {
            doSectorAverageOut(aaf, doc, visible);
        }
        else if(FIX_ANALYSIS_EVENTS_WQ.equals(name)) {
            doAnalysisEventsOut(aaf, doc, visible);
        }
        else if(FIX_REFERENCE_EVENTS_WQ.equals(name)
             || FIX_EVENTS.equals(name)) {
            doReferenceEventsOut(aaf, doc, visible);
        }
        else if(FIX_WQ_CURVE.equals(name)) {
            doWQCurveOut(aaf, doc, visible);
        }
        else if(FIX_OUTLIER.equals(name)) {
            doOutlierOut(aaf, doc, visible);
        }
        else if(QSECTOR.equals(name)) {
            doQSectorOut(aaf, doc, visible);
        }
        /*
        else if(FIX_EVENTS.equals(name)) {
            doEventsOut(aaf, doc, visible);
        }
        */
        else if(/*STATIC_WKMS_INTERPOL.equals(name) ||*/
                STATIC_WKMS_MARKS.equals(name) ||
                STATIC_WKMS.equals(name) ||
                HEIGHTMARKS_POINTS.equals(name) ) {
            doWAnnotations(
                    aaf.getData(context),
                    aaf,
                    doc,
                    visible);
        }
        else if (LONGITUDINAL_W.equals(name) || STATIC_WQ.equals(name)
                        || STATIC_WKMS_INTERPOL.equals(name)
                        || FIX_WQ_LS.equals(name)) {
            doWQOut(aaf.getData(context), aaf, doc, visible);
        }
        else if (name.equals(DISCHARGE_CURVE)) {
            log.debug("diso " + name);
            doDischargeOut(
                    (WINFOArtifact) aaf.getArtifact(),
                    aaf.getData(context),
                    aaf.getFacetDescription(),
                    doc,
                    visible);
        }
        else if (name.equals(MAINVALUES_W) || name.equals(MAINVALUES_Q)) {
            RiverAnnotation mainValues = (RiverAnnotation) aaf.getData(context);
            doAnnotations(
                mainValues,
                aaf,
                doc,
                visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(aaf.getFacetName())) {
            doPoints(aaf.getData(context),
                    aaf,
                    doc, visible, YAXIS.W.idx);
        }
        else {
            return false;
        }
        return true;
    }


    /** Add sector average points to chart. */
    protected void doSectorAverageOut(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        log.debug("doSectorAverageOut");

        QWDDateRange qwdd = (QWDDateRange) aaf.getData(context);
        QWD qwd = qwdd != null ? qwdd.getQWD() : null;

        if(qwd != null) {
            addQWSeries(new QWD[] { qwd }, aaf, doc, visible);
        }
        else {
            log.debug("doSectorAverageOut: qwd == null");
        }
    }

    /** Add analysis event points to chart. */
    protected void doAnalysisEventsOut(
        ArtifactAndFacet aaf,
        ThemeDocument    doc,
        boolean          visible
    ) {
        log.debug("doAnalysisEventsOut");

        QWD qwd = (QWD)aaf.getData(context);

        if (qwd == null) {
            log.debug("doAnalysisEventsOut: qwd == null");
            return;
        }

        double gaugeDatum = getCurrentGaugeDatum();
        boolean atGauge = gaugeDatum != 0d;

        double factor = atGauge ? 100d : 1d;

        double w = factor*(qwd.getW()-gaugeDatum);

        // Force empty symbol.
        if (qwd.getInterpolated()) {
            doc = new ThemeDocument(doc); // prevent potential side effects.
            doc.setValue(ThemeDocument.USE_FILL_PAINT, "true");
        }

        XYSeries series = new StyledXYSeries(
            aaf.getFacetDescription(),
            doc,
            qwd.getInterpolated()
                ? ShapeUtils.INTERPOLATED_SHAPE
                : ShapeUtils.MEASURED_SHAPE);

        series.add(qwd.getQ(), w);

        addAxisSeries(series, atGauge ? YAXIS.WCm.idx : YAXIS.W.idx, visible);

        if (visible && doc.parseShowPointLabel()) {

            List<XYTextAnnotation> textAnnos =
                new ArrayList<XYTextAnnotation>();

            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);
            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                dateFormat.format(qwd.getDate()),
                qwd.getQ(),
                w);
            textAnnos.add(anno);

            RiverAnnotation flysAnno =
                new RiverAnnotation(null, null, null, doc);
            flysAnno.setTextAnnotations(textAnnos);
            addAnnotations(flysAnno);
        }
    }


    /** Add reference event points to chart. */
    protected void doReferenceEventsOut(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        log.debug("doReferenceEventsOut");

        QWI qwd = (QWI)aaf.getData(context);
        if (qwd == null) {
            log.debug("doReferenceEventsOut: qwds == null");
            return;
        }

        // Force empty symbol.
        if (qwd.getInterpolated()) {
            doc = new ThemeDocument(doc); // prevent potential side effects.
            doc.setValue(ThemeDocument.USE_FILL_PAINT, "true");
        }

        XYSeries series = new StyledXYSeries(
            aaf.getFacetDescription(),
            false, true, doc,
            qwd.getInterpolated()
                ? ShapeUtils.INTERPOLATED_SHAPE
                : ShapeUtils.MEASURED_SHAPE);

        double gaugeDatum = getCurrentGaugeDatum();

        boolean atGauge = gaugeDatum != 0d;

        double factor = atGauge ? 100d : 1d;
        double w = factor*(qwd.getW()-gaugeDatum);

        series.add(qwd.getQ(), w, false);

        if (visible && doc.parseShowPointLabel()) {
            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);

            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                dateFormat.format(qwd.getDate()),
                qwd.getQ(),
                w);

            List<XYTextAnnotation> textAnnos =
                new ArrayList<XYTextAnnotation>();
            textAnnos.add(anno);
            RiverAnnotation flysAnno =
                new RiverAnnotation(null, null, null, doc);
            flysAnno.setTextAnnotations(textAnnos);
            addAnnotations(flysAnno);
        }

        addAxisSeries(series, atGauge ? YAXIS.WCm.idx : YAXIS.W.idx, visible);
    }


    private void addPointFromWQKms(WQKms wqkms,
        String        title,
        ThemeDocument theme,
        boolean       visible
    ) {
        XYSeries series = new StyledXYSeries(title, theme);
        Double ckm = (Double) context.getContextValue(CURRENT_KM);
        if (wqkms == null || wqkms.getKms().length == 0 || ckm == null) {
            log.info("addPointFromWQKms: No event data to show.");
            return;
        }
        double[] kms = wqkms.getKms();
        double gaugeDatum = getCurrentGaugeDatum();
        double factor = (gaugeDatum == 0d) ? 1d : 100d;
        for (int i = 0 ; i< kms.length; i++) {
            if (Math.abs(kms[i] - ckm) <= EPSILON) {
                series.add(wqkms.getQ(i), wqkms.getW(i), false);
                addAxisSeries(series, YAXIS.W.idx, visible);
                if(visible && theme.parseShowPointLabel()) {
                    List<XYTextAnnotation> textAnnos =
                        new ArrayList<XYTextAnnotation>();
                    XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                            title,
                            wqkms.getQ(i),
                            factor*(wqkms.getW(i)-gaugeDatum));
                    textAnnos.add(anno);
                    RiverAnnotation flysAnno =
                        new RiverAnnotation(null, null, null, theme);
                    flysAnno.setTextAnnotations(textAnnos);
                    addAnnotations(flysAnno);
                }
                return;
            }
        }
    }

    protected void doEventsOut(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        log.debug("doEventsOut");
        // Find W/Q at km.
        addPointFromWQKms((WQKms) aaf.getData(context),
            aaf.getFacetDescription(), doc, visible);
    }


    protected void doWQCurveOut(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        log.debug("doWQCurveOut");

        FixWQCurveFacet facet = (FixWQCurveFacet)aaf.getFacet();
        FixFunction func = (FixFunction)facet.getData(
                aaf.getArtifact(), context);

        if (func == null) {
            log.warn("doWQCurveOut: Facet does not contain FixFunction");
            return;
        }

        double maxQ = func.getMaxQ();

        if (maxQ > 0) {
            StyledXYSeries series = JFreeUtil.sampleFunction2D(
                    func.getFunction(),
                    doc,
                    aaf.getFacetDescription(),
                    500,   // number of samples
                    0.0 ,  // start
                    maxQ); // end

            double gaugeDatum = getCurrentGaugeDatum();

            if (gaugeDatum == 0d) {
                addAxisSeries(series, YAXIS.W.idx, visible);
            }
            else {
                StyledXYSeries series2 = JFreeUtil.sampleFunction2D(
                        func.getFunction(),
                        doc,
                        aaf.getFacetDescription(),
                        500,   // number of samples
                        0.0 ,  // start
                        maxQ); // end
                addAxisSeries(series2, YAXIS.W.idx, false);
                // Use second axis at cm if at gauge.
                for (int i = 0, N = series.getItemCount(); i < N; i++) {
                    series.updateByIndex(
                        i,
                        new Double(100d *
                                (series.getY(i).doubleValue() - gaugeDatum))
                    );
                }
                addAxisSeries(series, YAXIS.WCm.idx, visible);
            }
        }
        else {
            log.warn("doWQCurveOut: maxQ <= 0");
        }
    }

    protected void doOutlierOut(
        ArtifactAndFacet aaf,
        ThemeDocument doc,
        boolean visible
    ) {
        log.debug("doOutlierOut");

        QWI[] qws = (QWI[])aaf.getData(context);
        addQWSeries(qws, aaf, doc, visible);
    }


    /** Add markers for q sectors. */
    protected void doQSectorOut(
        ArtifactAndFacet aaf,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doQSectorOut");
        if (!visible) {
            return;
        }

        Object qsectorsObj = aaf.getData(context);
        if (qsectorsObj == null || !(qsectorsObj instanceof List)) {
            log.warn("No QSectors coming from data.");
            return;
        }

        List<?> qsectorsList = (List<?>) qsectorsObj;
        if (qsectorsList.size() == 0
            || !(qsectorsList.get(0) instanceof NamedDouble)
        ) {
            log.warn("No QSectors coming from data.");
            return;
        }

        @SuppressWarnings("unchecked")
        List<NamedDouble> qsectors = (List<NamedDouble>) qsectorsList;

        for (NamedDouble qsector : qsectors) {
            if (Double.isNaN(qsector.getValue())) {
                continue;
            }
            Marker m = new ValueMarker(qsector.getValue());
            m.setPaint(Color.black);

            float[] dashes = theme.parseLineStyle();
            int size       = theme.parseLineWidth();
            BasicStroke stroke;
            if (dashes.length <= 1) {
                stroke = new BasicStroke(size);
            }
            else {
                stroke = new BasicStroke(size,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_ROUND,
                        1.0f,
                        dashes,
                        0.0f);
            }
            m.setStroke(stroke);

            if (theme.parseShowLineLabel()) {
                m.setLabel(qsector.getName());
                m.setPaint(theme.parseTextColor());
                m.setLabelFont(theme.parseTextFont());
            }
            Color paint = theme.parseLineColorField();
            if (paint != null) {
                m.setPaint(paint);
            }
            m.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            m.setLabelTextAnchor(TextAnchor.TOP_LEFT);
            m.setLabelOffset(new RectangleInsets(5, 5, 10, 10));
            addDomainMarker(m);
        }
    }


    /**
     * Add W-Annotations to plot.
     * @param wqkms actual data (double[][]).
     * @param theme theme to use.
     */
    protected void doWAnnotations(
            Object           wqkms,
            ArtifactAndFacet aandf,
            ThemeDocument    theme,
            boolean          visible
            ) {
        Facet facet = aandf.getFacet();

        List<StickyAxisAnnotation> xy = new ArrayList<StickyAxisAnnotation>();
        if (wqkms instanceof double[][]) {
            log.debug("Got double[][]");
            double [][] data = (double [][]) wqkms;
            for (int i = 0; i< data[0].length; i++) {
                xy.add(new StickyAxisAnnotation(aandf.getFacetDescription(),
                        (float) data[1][i],
                        StickyAxisAnnotation.SimpleAxis.Y_AXIS));
            }

            doAnnotations(new RiverAnnotation(facet.getDescription(), xy),
                    aandf, theme, visible);
        }
        else {
            // Assume its WKms.
            log.debug("Got WKms");
            WKms data = (WKms) wqkms;

            Double ckm = (Double) context.getContextValue(CURRENT_KM);
            double location = (ckm != null)
                    ? ckm.doubleValue()
                    : getRange()[0];
            double w = StaticWKmsArtifact.getWAtKmLin(data, location);
            xy.add(new StickyAxisAnnotation(aandf.getFacetDescription(),
                    (float) w, StickyAxisAnnotation.SimpleAxis.Y_AXIS));

            doAnnotations(new RiverAnnotation(facet.getDescription(), xy),
                    aandf, theme, visible);
        }
    }


    /**
     * Add series with discharge curve to diagram.
     */
    protected void doDischargeOut(
            WINFOArtifact artifact,
            Object        o,
            String        description,
            ThemeDocument theme,
            boolean       visible)
    {
        WQKms wqkms = (WQKms) o;

        String gaugeName = wqkms.getName();

        River river = RiverUtils.getRiver(artifact);

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

        double gaugeDatum = getCurrentGaugeDatum();

        if (true || gaugeDatum == 0d) {
            StyledSeriesBuilder.addPointsQW(series, wqkms);
            addAxisSeries(series, YAXIS.W.idx, visible);
        }
        else {
            XYSeries series2 = new StyledXYSeries(description, theme);
            StyledSeriesBuilder.addPointsQW(series2, wqkms);
            addAxisSeries(series2, YAXIS.W.idx, false);

            // Use second axis...
            StyledSeriesBuilder.addPointsQW(series, wqkms, -gaugeDatum, 100d);
            addAxisSeries(series, YAXIS.WCm.idx, visible);
        }
    }


    /**
     * Add WQ Data to plot.
     * @param wqkms data as double[][]
     */
    protected void doWQOut(
            Object           wqkms,
            ArtifactAndFacet aaf,
            ThemeDocument    theme,
            boolean          visible
            ) {
        log.debug("FixWQCurveGenerator: doWQOut");
        if (wqkms instanceof WQKms) {
            // TODO As in doEventsOut, the value-searching should
            // be delivered by the facet already (instead of in the Generator).
            log.debug("FixWQCurveGenerator: doWQOut: WQKms");

            addPointFromWQKms((WQKms)aaf.getData(context),
                aaf.getFacetDescription(), theme, visible);
        }
        else {
            log.debug("FixWQCurveGenerator: doWQOut: double[][]");
            double [][] data = (double [][]) wqkms;

            XYSeries series = new StyledXYSeries(
                aaf.getFacetDescription(), false, true, theme);
            StyledSeriesBuilder.addPoints(series, data, true);

            addAxisSeries(series, YAXIS.W.idx, visible);
        }
    }


    protected void addQWSeries(
            QWI []           qws,
            ArtifactAndFacet aaf,
            ThemeDocument    theme,
            boolean          visible
            ) {
        if (qws == null) {
            return;
        }

        XYSeries series = new StyledXYSeries(
            aaf.getFacetDescription(),
            false, true,
            theme);

        List<XYTextAnnotation> textAnnos =
                new ArrayList<XYTextAnnotation>(qws.length);

        DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);

        double gaugeDatum = getCurrentGaugeDatum();
        double factor = (gaugeDatum == 0d) ? 1d : 100d;
        for (QWI qw: qws) {
            series.add(qw.getQ(), factor*(qw.getW()-gaugeDatum), false);

            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                    dateFormat.format(qw.getDate()),
                    qw.getQ(),
                    factor*(qw.getW()-gaugeDatum));
            textAnnos.add(anno);
        }

        if (gaugeDatum == 0d) {
            addAxisSeries(series, YAXIS.W.idx, visible);
        }
        else {
            addAxisSeries(series, YAXIS.WCm.idx, visible);
        }
        if (visible && theme != null && theme.parseShowPointLabel()) {
            RiverAnnotation flysAnno =
                    new RiverAnnotation(null, null, null, theme);
            flysAnno.setTextAnnotations(textAnnos);
            addAnnotations(flysAnno);
        }
    }

    @Override
    protected String getChartTitle() {
        return Resources.format(
                context.getMeta(),
                I18N_CHART_TITLE,
                I18N_CHART_TITLE_DEFAULT,
                context.getContextValue(CURRENT_KM));
    }

    @Override
    protected String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE_DEFAULT);
    }

    @Override
    protected String getDefaultChartSubtitle() {
        FixAnalysisAccess access = new FixAnalysisAccess(artifact);
        DateRange dateRange = access.getDateRange();
        DateRange refRange  = access.getReferencePeriod();

        if (dateRange != null && refRange != null) {
            return Resources.format(
                    context.getMeta(),
                    I18N_CHART_SUBTITLE,
                    "",
                    access.getRiverName(),
                    dateRange.getFrom(),
                    dateRange.getTo(),
                    refRange.getFrom(),
                    refRange.getTo());
        }

        return null;
    }

    @Override
    protected void addSubtitles(JFreeChart chart) {
        String defaultSubtitle = getDefaultChartSubtitle();

        if (defaultSubtitle == null || defaultSubtitle.length() == 0) {
            return;
        }

        chart.addSubtitle(new TextTitle(defaultSubtitle));

        StringBuilder buf = new StringBuilder();

        // Add analysis periods as additional subtitle
        FixAnalysisAccess access = new FixAnalysisAccess(artifact);
        DateRange[] aperiods = access.getAnalysisPeriods();
        buf.append(msg("fix.analysis.periods"));
        buf.append(": ");
        for(int n = 0; n < aperiods.length; n++) {
            buf.append(
                    Resources.format(
                            context.getMeta(),
                            I18N_CHART_SUBTITLE1,
                            "",
                            aperiods[n].getFrom(),
                            aperiods[n].getTo()));
            if(n + 1 < aperiods.length) {
                buf.append("; ");
            }
        }

        chart.addSubtitle(new TextTitle(buf.toString()));
    }

    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL_DEFAULT);
    }

    @Override
    protected String getDefaultYAxisLabel(int pos) {
        D4EArtifact flys = (D4EArtifact) master;

        String unit = pos == 0
            ? "cm"
            : RiverUtils.getRiver(flys).getWstUnit().getName();

        return msg(
            I18N_YAXIS_LABEL,
            I18N_YAXIS_LABEL_DEFAULT,
            new Object[] { unit });
    }

    @Override
    protected ChartGenerator.YAxisWalker getYAxisWalker() {
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
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
