/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.NamedDouble;
import org.dive4elements.river.artifacts.model.QWDDateRange;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.fixings.FixFunction;
import org.dive4elements.river.artifacts.model.fixings.FixWQCurveFacet;
import org.dive4elements.river.artifacts.model.fixings.QWD;
import org.dive4elements.river.artifacts.model.fixings.QWI;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.java2d.ShapeUtils;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.JFreeUtil;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StickyAxisAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;

public class FixWQProcessor
extends DefaultProcessor
implements FacetTypes
{

    private static Logger log = LogManager.getLogger(FixWQProcessor.class);

    private String I18N_AXIS_LABEL = "chart.discharge.curve.yaxis.label";


    public FixWQProcessor() {
    }

    @Override
    public void doOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        // TODO: Simplify this processor and move general facets/data to
        // MiscDischargeProcessor or something...
        String facetType = bundle.getFacetName();
        log.debug("facet: " + facetType
            + " name: " + bundle.getFacetDescription());
        if(facetType.startsWith(FIX_SECTOR_AVERAGE_WQ)) {
            doSectorAverageOut(generator, bundle, theme, visible);
        }
        else if(FIX_ANALYSIS_EVENTS_WQ.equals(facetType)
            || FIX_REFERENCE_EVENTS_WQ.equals(facetType)
            || FIX_EVENTS.equals(facetType)) {
            doEventsOut(generator, bundle, theme, visible);
        }
        else if(FIX_WQ_CURVE.equals(facetType)) {
            doWQCurveOut(generator, bundle, theme, visible);
        }
        else if(FIX_OUTLIER.equals(facetType)) {
            doOutlierOut(generator, bundle, theme, visible);
        }
        else if(QSECTOR.equals(facetType)) {
            doQSectorOut(generator, bundle, theme, visible);
        }
        else if(STATIC_WKMS_MARKS.equals(facetType) ||
                STATIC_WKMS.equals(facetType) ||
                HEIGHTMARKS_POINTS.equals(facetType) ) {
            doWAnnotations(generator, bundle, theme, visible);
        }
        else if (LONGITUDINAL_W.equals(facetType)
            || STATIC_WKMS_INTERPOL.equals(facetType)
            || FIX_WQ_LS.equals(facetType)) {
            doWQOut(generator, bundle, theme, visible);
        }

    }

    /** Add sector average points to chart. */
    protected void doSectorAverageOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doSectorAverageOut");
        QWDDateRange qwdd = (QWDDateRange)bundle.getData(
            generator.getCallContext());
        QWD qwd = qwdd != null ? qwdd.getQWD() : null;

        if(qwd != null) {
            XYSeries series = new StyledXYSeries(
                bundle.getFacetDescription(),
                false, true,
                theme);
            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);

            series.add(qwd.getQ(), qwd.getW(), false);

            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                    dateFormat.format(qwd.getDate()),
                    qwd.getQ(),
                    qwd.getW());
            List<XYTextAnnotation> annos = new ArrayList<XYTextAnnotation>();
            annos.add(anno);
            generator.addAxisSeries(series, axisName, visible);

            if (visible && theme != null && theme.parseShowPointLabel()) {
                RiverAnnotation flysAnno =
                        new RiverAnnotation(null, null, null, theme);
                flysAnno.setTextAnnotations(annos);
                generator.addAnnotations(flysAnno);
            }
        }
        else {
            log.debug("doSectorAverageOut: qwd == null");
        }
    }


    /** Add analysis event points to chart. */
    protected void doEventsOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument    theme,
        boolean          visible
    ) {
        log.debug("doAnalysisEventsOut");

        QWD qwd = (QWD)bundle.getData(generator.getCallContext());

        if (qwd == null) {
            log.debug("doAnalysisEventsOut: qwd == null");
            return;
        }

        // Force empty symbol.
        if (qwd.getInterpolated()) {
            theme = new ThemeDocument(theme); // prevent potential side effects
            theme.setValue(ThemeDocument.USE_FILL_PAINT, "true");
        }

        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(),
            theme,
            qwd.getInterpolated()
                ? ShapeUtils.INTERPOLATED_SHAPE
                : ShapeUtils.MEASURED_SHAPE);

        series.add(qwd.getQ(), qwd.getW());

        generator.addAxisSeries(series, axisName, visible);

        if (visible && theme.parseShowPointLabel()) {

            List<XYTextAnnotation> textAnnos =
                new ArrayList<XYTextAnnotation>();

            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);
            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                dateFormat.format(qwd.getDate()),
                qwd.getQ(),
                qwd.getW());
            textAnnos.add(anno);

            RiverAnnotation flysAnno =
                new RiverAnnotation(null, null, null, theme);
            flysAnno.setTextAnnotations(textAnnos);
            generator.addAnnotations(flysAnno);
        }
    }

    /** Add reference event points to chart. */
    protected void doReferenceEventsOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible) {
        log.debug("doReferenceEventsOut");

        QWI qwd = (QWI)bundle.getData(generator.getCallContext());
        if (qwd == null) {
            log.debug("doReferenceEventsOut: qwds == null in "
                + bundle.getFacetDescription());
            return;
        }

        // Force empty symbol.
        if (qwd.getInterpolated()) {
            theme = new ThemeDocument(theme); // prevent potential side effects
            theme.setValue(ThemeDocument.USE_FILL_PAINT, "true");
        }

        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(),
            false, true, theme,
            qwd.getInterpolated()
                ? ShapeUtils.INTERPOLATED_SHAPE
                : ShapeUtils.MEASURED_SHAPE);

        series.add(qwd.getQ(), qwd.getW(), false);

        if (visible && theme.parseShowPointLabel()) {
            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);

            XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                dateFormat.format(qwd.getDate()),
                qwd.getQ(),
                qwd.getW());

            List<XYTextAnnotation> textAnnos =
                new ArrayList<XYTextAnnotation>();
            textAnnos.add(anno);
            RiverAnnotation flysAnno =
                new RiverAnnotation(null, null, null, theme);
            flysAnno.setTextAnnotations(textAnnos);
            generator.addAnnotations(flysAnno);
        }

        generator.addAxisSeries(series, axisName, visible);
    }

    protected void doWQCurveOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doWQCurveOut");

        FixWQCurveFacet facet = (FixWQCurveFacet)bundle.getFacet();
        FixFunction func = (FixFunction)facet.getData(
                bundle.getArtifact(), generator.getCallContext());

        if (func == null) {
            log.warn("doWQCurveOut: Facet does not contain FixFunction");
            return;
        }

        double maxQ = func.getMaxQ();

        if (maxQ > 0) {
            StyledXYSeries series = JFreeUtil.sampleFunction2D(
                    func.getFunction(),
                    theme,
                    bundle.getFacetDescription(),
                    500,   // number of samples
                    0.0 ,  // start
                    maxQ); // end

            generator.addAxisSeries(series, axisName, visible);
        }
        else {
            log.warn("doWQCurveOut: maxQ <= 0");
        }
    }

    protected void doOutlierOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doOutlierOut");

        QWI[] qws = (QWI[])bundle.getData(generator.getCallContext());
        if(qws != null) {
            XYSeries series = new StyledXYSeries(
                bundle.getFacetDescription(),
                false, true,
                theme);
            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT);

            List<XYTextAnnotation> annos = new ArrayList<XYTextAnnotation>();

            for (QWI qw: qws) {
                series.add(qw.getQ(), qw.getW(), false);

                XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                        dateFormat.format(qw.getDate()),
                        qw.getQ(),
                        qw.getW());
                annos.add(anno);
            }
            generator.addAxisSeries(series, axisName, visible);

            if (visible && theme != null && theme.parseShowPointLabel()) {
                RiverAnnotation flysAnno =
                        new RiverAnnotation(null, null, null, theme);
                flysAnno.setTextAnnotations(annos);
                generator.addAnnotations(flysAnno);
            }
        }
        else {
            log.debug("doOutlierOut: qwd == null");
        }
    }

    /** Add markers for q sectors. */
    protected void doQSectorOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doQSectorOut");
        if (!visible) {
            return;
        }

        Object qsectorsObj = bundle.getData(generator.getCallContext());
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
            generator.addDomainMarker(m);
        }
    }

    /**
     * Add W-Annotations to plot.
     * @param wqkms actual data (double[][]).
     * @param theme theme to use.
     */
    protected void doWAnnotations(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument    theme,
        boolean          visible
    ) {
        Object data = bundle.getData(generator.getCallContext());
        List<StickyAxisAnnotation> xy = new ArrayList<StickyAxisAnnotation>();
        if (data instanceof double[][]) {
            log.debug("Got double[][]");
            double [][] values = (double [][]) data;
            for (int i = 0; i< values[0].length; i++) {
                xy.add(new StickyAxisAnnotation(
                        bundle.getFacetDescription(),
                        (float) values[1][i],
                        StickyAxisAnnotation.SimpleAxis.Y_AXIS));
            }

            if (visible) {
                generator.addAnnotations(
                    new RiverAnnotation(
                        bundle.getFacetDescription(), xy, null, theme));
            }
        }
        else {
            // Assume its WKms.
            log.debug("Got WKms");
            /* TODO
            WKms wkms = (WKms) data;

            Double ckm =
                (Double)generator.getCallContext().getContextValue(
                    FixChartGenerator.CURRENT_KM);
            double location = (ckm != null)
                    ? ckm.doubleValue()
                    : getRange()[0];
            double w = StaticWKmsArtifact.getWAtKmLin(data, location);
            xy.add(new StickyAxisAnnotation(aandf.getFacetDescription(),
                    (float) w, StickyAxisAnnotation.SimpleAxis.Y_AXIS));

            doAnnotations(new RiverAnnotation(facet.getDescription(), xy),
                    aandf, theme, visible);*/
        }
    }

    /**
     * Add WQ Data to plot.
     * @param wqkms data as double[][]
     */
    protected void doWQOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument    theme,
        boolean          visible
    ) {
        Object data = bundle.getData(generator.getCallContext());
        if (data instanceof WQKms) {
            WQKms wqkms = (WQKms)data;
            // TODO As in doEventsOut, the value-searching should
            // be delivered by the facet already
            XYSeries series = new StyledXYSeries(
                bundle.getFacetDescription(), theme);
            Double ckm = (Double) generator.getCallContext()
                .getContextValue(CURRENT_KM);

            if (wqkms == null || wqkms.getKms().length == 0 || ckm == null) {
                log.info("addPointFromWQKms: No event data to show.");
                return;
            }

            double[] kms = wqkms.getKms();
            for (int i = 0 ; i< kms.length; i++) {
                /* We use a tolerance of 1m here to find a hit.
                 * Probably to avoid some rounding errors. */
                if (Math.abs(kms[i] - ckm) <= 0.001) {
                    series.add(wqkms.getQ(i), wqkms.getW(i), false);
                    generator.addAxisSeries(series, axisName, visible);
                    if(visible && theme.parseShowPointLabel()) {
                        List<XYTextAnnotation> textAnnos =
                            new ArrayList<XYTextAnnotation>();
                        XYTextAnnotation anno =
                            new CollisionFreeXYTextAnnotation(
                                bundle.getFacetDescription(),
                                wqkms.getQ(i),
                                wqkms.getW(i));
                        textAnnos.add(anno);
                        RiverAnnotation flysAnno =
                            new RiverAnnotation(null, null, null, theme);
                        flysAnno.setTextAnnotations(textAnnos);
                        generator.addAnnotations(flysAnno);
                    }
                    return;
                }
            }
        }
        else {
            log.debug("FixWQCurveGenerator: doWQOut: double[][]");
            double [][] values = (double [][]) data;

            XYSeries series = new StyledXYSeries(
                bundle.getFacetDescription(), false, true, theme);
            StyledSeriesBuilder.addPoints(series, values, true);

            generator.addAxisSeries(series, axisName, visible);
        }
    }

    @Override
    public boolean canHandle(String facettype) {
        return facettype.startsWith(FIX_SECTOR_AVERAGE_WQ)
            || FIX_ANALYSIS_EVENTS_WQ.equals(facettype)
            || FIX_REFERENCE_EVENTS_WQ.equals(facettype)
            || FIX_EVENTS.equals(facettype)
            || FIX_WQ_CURVE.equals(facettype)
            || FIX_OUTLIER.equals(facettype)
            || QSECTOR.equals(facettype)
            || STATIC_WKMS_MARKS.equals(facettype)
            || STATIC_WKMS.equals(facettype)
            || HEIGHTMARKS_POINTS.equals(facettype)
            || LONGITUDINAL_W.equals(facettype)
            || STATIC_WKMS_INTERPOL.equals(facettype)
            || FIX_WQ_LS.equals(facettype);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        CallMeta meta = generator.getCallContext().getMeta();

        RiverAccess access = new RiverAccess((D4EArtifact)generator
            .getMaster());
        String unit = access.getRiver().getWstUnit().getName();
        return Resources.getMsg(
                meta,
                I18N_AXIS_LABEL,
                new Object[] { unit });
    }
}
