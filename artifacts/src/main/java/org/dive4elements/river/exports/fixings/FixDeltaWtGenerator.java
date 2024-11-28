/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.FixAnalysisAccess;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.QWDDateRange;
import org.dive4elements.river.artifacts.model.fixings.QWD;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.TimeseriesChartGenerator;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StyledDomainMarker;
import org.dive4elements.river.jfree.StyledTimeSeries;
import org.dive4elements.river.jfree.StyledValueMarker;
import org.dive4elements.river.themes.ThemeDocument;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.w3c.dom.Document;

import gnu.trove.TLongHashSet;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;

/**
 * Generator for Delta W(t) charts.
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixDeltaWtGenerator
extends      TimeseriesChartGenerator
implements   FacetTypes
{
    /** Private log. */
    private static Logger log =
            LogManager.getLogger(FixDeltaWtGenerator.class);

    public static final String I18N_CHART_TITLE =
            "chart.fix.deltawt.title";

    public static final String I18N_CHART_SUBTITLE =
            "chart.fix.deltawt.subtitle";

    public static final String I18N_XAXIS_LABEL =
            "chart.fix.deltawt.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
            "chart.fix.deltawt.yaxis.label";

    public static final String I18N_YAXIS_SECOND_LABEL =
            "chart.fix.deltawt.yaxis.second.label";


    public static enum YAXIS {
        dW(0);
        protected int idx;
        private YAXIS(int c) {
            idx = c;
        }
    }


    private D4EArtifact artifact;

    // Used to make the dates collision free.
    private TLongHashSet uniqueDates = new TLongHashSet();


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


    @Override
    protected String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE);
    }


    @Override
    protected String getChartTitle() {
        return Resources.format(
                context.getMeta(),
                I18N_CHART_TITLE,
                "",
                FixChartGenerator
                .getCurrentKmFromRequest(request).doubleValue());
    }


    @Override
    protected String getDefaultChartSubtitle() {
        FixAnalysisAccess access = new FixAnalysisAccess(artifact);
        DateRange dateRange = access.getDateRange();
        DateRange refRange  = access.getReferencePeriod();
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


    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL);
    }

    @Override
    protected String getDefaultYAxisLabel(int pos) {
        if (pos == 0) {
            return msg(I18N_YAXIS_LABEL, I18N_YAXIS_LABEL);
        }
        else if (pos == 1) {
            return msg(I18N_YAXIS_SECOND_LABEL, I18N_YAXIS_SECOND_LABEL);
        }
        else {
            return "NO TITLE FOR Y AXIS: " + pos;
        }
    }


    @Override
    public void doOut(
            ArtifactAndFacet artifactFacet,
            ThemeDocument    theme,
            boolean          visible
            ) {
        String name = artifactFacet.getFacetName();
        log.debug("FixDeltaWtGenerator.doOut: " + name);
        log.debug("Theme description is: "
            + artifactFacet.getFacetDescription());

        this.artifact = (D4EArtifact)artifactFacet.getArtifact();

        if (name.contains(FIX_SECTOR_AVERAGE_DWT)) {
            doSectorAverageOut(
                    (D4EArtifact) artifactFacet.getArtifact(),
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    theme,
                    visible);
        }
        else if (name.equals(FIX_REFERENCE_EVENTS_DWT)) {
            doReferenceEventsOut(
                    (D4EArtifact) artifactFacet.getArtifact(),
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    theme,
                    visible);
        }
        else if (name.equals(FIX_ANALYSIS_EVENTS_DWT)) {
            doAnalysisEventsOut(
                    (D4EArtifact) artifactFacet.getArtifact(),
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    theme,
                    visible);
        }
        else if (name.equals(FIX_DEVIATION_DWT)) {
            doDeviationOut(
                    (D4EArtifact) artifactFacet.getArtifact(),
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    theme,
                    visible);
        }
        else if (name.equals(FIX_ANALYSIS_PERIODS_DWT)) {
            doAnalysisPeriodsOut(
                    (D4EArtifact) artifactFacet.getArtifact(),
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    theme,
                    visible);
        }
        else if (name.equals(FIX_REFERENCE_PERIOD_DWT)) {
            doReferencePeriodsOut(
                    (D4EArtifact) artifactFacet.getArtifact(),
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    theme,
                    visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            doPoints (artifactFacet.getData(context),
                    artifactFacet,
                    theme, visible, YAXIS.dW.idx);
        }
        else {
            log.warn("doOut(): unknown facet name: " + name);
            return;
        }
    }


    protected void doReferencePeriodsOut(
            D4EArtifact   artifact,
            Object        data,
            String        desc,
            ThemeDocument theme,
            boolean       visible)
    {
        log.debug("doReferencePeriodsOut()");

        if (visible) {
            FixAnalysisAccess access = new FixAnalysisAccess(artifact);
            DateRange refRange  = access.getReferencePeriod();

            RegularTimePeriod start = new FixedMillisecond(refRange.getFrom());
            RegularTimePeriod end = new FixedMillisecond(refRange.getTo());
            StyledDomainMarker marker = new StyledDomainMarker(
                    start.getMiddleMillisecond(),
                    end.getMiddleMillisecond(),
                    theme);
            domainMarker.add(marker);
        }
    }

    private long uniqueDate(long date) {
        return uniqueDates.add(date)
            ? date
            : uniqueDate(date+30L*1000L); // add 30secs.
    }


    protected void doSectorAverageOut(
            D4EArtifact   artifact,
            Object        data,
            String        desc,
            ThemeDocument theme,
            boolean       visible)
    {
        log.debug("doSectorAverageOut(): description = " + desc);

        QWDDateRange qwd = (QWDDateRange) data;
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        TimeSeries        series = new StyledTimeSeries(desc, theme);

        if (qwd == null || qwd.qwd == null || qwd.dateRange == null) {
            return;
        }
        RegularTimePeriod rtp = new FixedMillisecond(qwd.qwd.getDate());
        double value = qwd.qwd.getDeltaW();

        // Draw a line spanning the analysis time.
        series.add(rtp, value);
        rtp = new FixedMillisecond(qwd.dateRange.getFrom());
        series.addOrUpdate(rtp, value);
        rtp = new FixedMillisecond(qwd.dateRange.getTo());
        series.addOrUpdate(rtp, value);

        tsc.addSeries(series);

        addAxisDataset(tsc, 0, visible);

        if (visible && theme.parseShowLineLabel()) {
            List<XYTextAnnotation> textAnnos =
                new ArrayList<XYTextAnnotation>();
            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                    "\u0394 W(t) [cm] "
                    + (float)Math.round(qwd.qwd.getDeltaW() * 10000) / 10000,
                    tsc.getXValue(0, 0),
                    qwd.qwd.getDeltaW());
            textAnnos.add(anno);

            RiverAnnotation flysAnno =
                new RiverAnnotation(null, null, null, theme);
            flysAnno.setTextAnnotations(textAnnos);
            addAnnotations(flysAnno);
        }
    }


    protected void doAnalysisEventsOut(
            D4EArtifact artifact,
            Object       data,
            String       desc,
            ThemeDocument theme,
            boolean      visible
            ) {
        log.debug("doAnalysisEventsOut: desc = " + desc);

        QWD qwd = (QWD) data;
        doQWDEventsOut(qwd, desc, theme, visible);
    }


    protected void doQWDEventsOut(
        QWD qwd,
        String desc,
        ThemeDocument theme,
        boolean visible
    ) {
        TimeSeriesCollection tsc = new TimeSeriesCollection();

        TimeSeries   series = new StyledTimeSeries(desc, theme);
        TimeSeries interpol = new StyledTimeSeries(desc + "interpol", theme);

        if (qwd == null) {
            log.debug("doQWDEventsOut: qwd == null");
            return;
        }

        Map<Integer, int[]> annoIdxMap = new HashMap<Integer, int[]>();

        int idxInterpol = 0;
        int idxRegular = 0;
        long time = uniqueDate(qwd.getDate().getTime());
        RegularTimePeriod rtp = new FixedMillisecond(time);
        double value =  qwd.getDeltaW();
        boolean interpolate = qwd.getInterpolated();
        if (interpolate) {
            if(interpol.addOrUpdate(rtp, value) == null) {
                annoIdxMap.put(
                        0,
                        new int[]{1, idxInterpol});
                idxInterpol++;
            }
        }
        else {
            if(series.addOrUpdate(rtp, value) == null) {
                annoIdxMap.put(
                        0,
                        new int[]{0, idxRegular});
                idxRegular++;
            }
        }

        tsc.addSeries(series);
        tsc.addSeries(interpol);
        addAxisDataset(tsc, 0, visible);
        addAttribute(desc + "interpol", "interpolate");
        addAttribute(desc, "outline");

        doQWDTextAnnotations(annoIdxMap, tsc, qwd, theme, visible);
    }


    /**
     * @param annoIdxMap map of index in qwds to series/data item indices
     *                   in tsc.
     */
    protected void doQWDTextAnnotations(Map<Integer, int[]> annoIdxMap,
            TimeSeriesCollection tsc, QWD qwd, ThemeDocument theme,
            boolean visible) {
        log.debug("doQWDTextAnnotation()");

        if (!visible || !theme.parseShowPointLabel()) {
            log.debug("doQWDTextAnnotation: annotation not visible");
            return;
        }

        Locale locale = Resources.getLocale(context.getMeta());
        NumberFormat nf = NumberFormat.getInstance(locale);

        List<XYTextAnnotation> textAnnos = new ArrayList<XYTextAnnotation>();

        for (int[] idxs: annoIdxMap.values()) {

            double x = tsc.getXValue(idxs[0], idxs[1]);

            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                    nf.format(qwd.getQ()) + " m\u00B3/s",
                    x,
                    qwd.getDeltaW());
            textAnnos.add(anno);
        }

        RiverAnnotation flysAnno = new RiverAnnotation(null, null, null, theme);
        flysAnno.setTextAnnotations(textAnnos);
        addAnnotations(flysAnno);
    }


    protected void doReferenceEventsOut(
            D4EArtifact  artifact,
            Object        data,
            String        desc,
            ThemeDocument theme,
            boolean       visible
            ) {
        log.debug("doReferenceEventsOut: desc = " + desc);

        QWD qwd = (QWD) data;
        doQWDEventsOut(qwd, desc, theme, visible);
    }


    protected void doDeviationOut(
            D4EArtifact   artifact,
            Object        data,
            String        desc,
            ThemeDocument theme,
            boolean       visible
            ) {
        log.debug("doDeviationOut: desc = " + desc);

        if (data == null || !visible) {
            log.debug("no standard deviation");
            return;
        }
        double[] value = (double[]) data;
        StyledDomainMarker lower =
            new StyledDomainMarker((value[0] * -1), 0, theme);
        StyledDomainMarker upper =
            new StyledDomainMarker(0, value[0], theme);

        valueMarker.add(lower);
        valueMarker.add(upper);
    }


    protected void doAnalysisPeriodsOut(
            D4EArtifact artifact,
            Object        data,
            String        desc,
            ThemeDocument theme,
            boolean       visible)
    {
        DateRange[] ranges = (DateRange[]) data;
        if (ranges == null || !visible) {
            return;
        }
        for (int i = 0; i < ranges.length; i++) {
            log.debug("creating domain marker");
            RegularTimePeriod start = new FixedMillisecond(ranges[i].getFrom());
            RegularTimePeriod end = new FixedMillisecond(ranges[i].getTo());
            StyledDomainMarker marker =
                    new StyledDomainMarker(start.getMiddleMillisecond(),
                            end.getMiddleMillisecond(), theme);
            marker.useSecondColor(i % 2 == 0);
            domainMarker.add(marker);
        }
        log.debug("domainmarkers: " + domainMarker.size());
    }


    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        super.init(outName, request, out, context);

        Double currentKm = FixChartGenerator.getCurrentKmFromRequest(request);

        if (log.isDebugEnabled()) {
            log.debug("currentKm = " + currentKm);
        }

        context.putContextValue(CURRENT_KM, currentKm);

        // XXX: This looks hackish!
        StyledValueMarker marker =
            new StyledValueMarker(0, new ThemeDocument(request));
        valueMarker.add(marker);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
