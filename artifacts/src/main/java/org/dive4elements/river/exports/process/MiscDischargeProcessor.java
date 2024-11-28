/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.data.xy.XYSeries;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.DischargeCurveGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StickyAxisAnnotation;
import org.dive4elements.river.jfree.StyledValueMarker;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

import org.jfree.chart.annotations.XYTextAnnotation;


/** Helper for data handling in discharge diagrams. */
public class MiscDischargeProcessor
extends DefaultProcessor implements FacetTypes {

    private final static Logger log =
            LogManager.getLogger(MiscDischargeProcessor.class);

    /** Station for which the diagram is shown. */
    private double km;

    /** Tolerance for comparison of kilometers. */
    public static final double KM_EPSILON = 0.001d;

    private String I18N_AXIS_LABEL = "chart.discharge.curve.yaxis.label";

    /** This processor needs to be constructed with a given km. */
    public MiscDischargeProcessor() {
        km = Double.NaN;
    }


    public MiscDischargeProcessor(double km) {
        this.km = km;
    }

    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);
        if (HISTORICAL_DISCHARGE_WQ_Q.equals(bundle.getFacetName())) {
            doHistoricalDischargeOutQ(generator, bundle, theme, visible);
        }
        else if (HISTORICAL_DISCHARGE_WQ_W.equals(bundle.getFacetName())) {
            doHistoricalDischargeOutW(generator, bundle, theme, visible);
        }
        else if (data instanceof WQKms) {
            doWQKmsPointOut(
                generator, (WQKms) data, bundle, theme, visible);
            return;
        }
        if (MAINVALUES_W.equals(bundle.getFacetName())) {
            doYRiverAnnotationOut(
                generator, (RiverAnnotation)data, theme, visible);
            return;
            }
        else if (data instanceof RiverAnnotation) {
            doRiverAnnotationOut(
                generator, (RiverAnnotation)data, theme, visible);
            return;
        }
        else if (data instanceof double[][]) {
            doPointsOut(generator, (double[][])data, bundle, theme, visible);
        }
        else {
            log.error("Can't process "
                + data.getClass().getName() + " objects of facet "
                + bundle.getFacetName());
        }
    }

    /** Process data, add it to plot. */
    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument theme,
            boolean visible,
            int axisIndex
    ) {
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);
        /* TODO: Remove the first case.*/
        if (bundle.getFacetName().equals(STATIC_WQ)) {
            doPointOut(
                generator, bundle, theme, visible, axisIndex);
        }
        else if (data instanceof WQKms) {
            doWQKmsPointOut(
                generator, (WQKms) data, bundle, theme, visible, axisIndex);
            return;
        }
        else if (data instanceof RiverAnnotation) {
            doRiverAnnotationOut(
                generator, (RiverAnnotation) data, bundle, theme, visible);
            return;
        }
        else if (data instanceof double[][]) {
            doMarksOut(
                generator, (double[][]) data, bundle, theme, visible);
            return;
        }
        else {
            log.error("Can't process "
                + data.getClass().getName() + " objects of facet "
                + bundle.getFacetName());
        }
    }

    private void doPointOut(XYChartGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible,
        int axisIndex
    ) {
        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), theme);
        Object wq = bundle.getData(generator.getCallContext());
        if (wq instanceof double[][]) {
            double [][] data = (double [][]) wq;
            StyledSeriesBuilder.addPoints(series, data, true);
        } else if (wq instanceof WQKms) {
            WQKms wqkms = (WQKms) wq;
            StyledSeriesBuilder.addPointsQW(series, (WQKms) wq);
        }

        generator.addAxisSeries(series, axisIndex, visible);
    }

    /** Handle WQKms data by finding w/q values at given km. */
    protected void doWQKmsPointOut(XYChartGenerator generator,
        WQKms wqkms,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible,
        int axidx
    ) {
        log.debug("doWQKmsPointOut");
        String title = bundle.getFacetDescription();
        XYSeries series = new StyledXYSeries(
            title,
            theme);

        double[] kms = wqkms.getKms();

        for (int i = 0 ; i< kms.length; i++) {
            if (Math.abs(kms[i] - getKm()) <= KM_EPSILON) {
                series.add(wqkms.getQ(i), wqkms.getW(i));
                generator.addAxisSeries(series, axidx, visible);
                if(visible && theme.parseShowPointLabel()) {
                    List<XYTextAnnotation> textAnnos =
                        new ArrayList<XYTextAnnotation>();
                    XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                            title,
                            wqkms.getQ(i),
                            // TODO add a percentage to the extend of W axis
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

        log.warn("No WQ found for km " + getKm());
    }

    protected void doRiverAnnotationOut(XYChartGenerator generator,
        RiverAnnotation annotations,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        if (!(generator instanceof DischargeCurveGenerator)) {
            log.error("DischargeProcessor can only be used in " +
                "DischargeCurveGenerator-classes.");
            return;
        }
        log.debug("doRiverAnnotationOut");
        DischargeCurveGenerator dGenerator =
            (DischargeCurveGenerator) generator;

        dGenerator.translateRiverAnnotation(annotations);
        dGenerator.doAnnotations(
            annotations,
            bundle, theme, visible);
    }


    /**
     * Put Sticky Axis Markers to Y-axis for each value.
     * @param data [[-,y1],[-,y2],...] ('x'-coordinates ignored)
     */
    protected void doMarksOut(XYChartGenerator generator,
        double[][] data,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doMarksOut");

        if (!visible) {
            return;
        }

        // TODO subtract gauge null point if at gauge.
        String title = bundle.getFacetDescription();
        List<StickyAxisAnnotation> yMarks =
            new ArrayList<StickyAxisAnnotation>();

        for (double yPos: data[1]) {
            yMarks.add(new StickyAxisAnnotation(
                title,
                (float) yPos,
                StickyAxisAnnotation.SimpleAxis.Y_AXIS));
        }

        generator.doAnnotations(new RiverAnnotation(title, yMarks),
            bundle, theme, visible);
    }

    /** True if this processor knows how to deal with facetType. */
    @Override
    public boolean canHandle(String facetType) {
        return STATIC_WQKMS_W.equals(facetType)
            || MAINVALUES_Q.equals(facetType)
            || MAINVALUES_W.equals(facetType)
            || STATIC_W_INTERPOL.equals(facetType)
            || STATIC_WQ.equals(facetType)
            || STATIC_WQ_ANNOTATIONS.equals(facetType)
            || HISTORICAL_DISCHARGE_WQ_W.equals(facetType)
            || HISTORICAL_DISCHARGE_WQ_Q.equals(facetType);
    }


    /** The station of the current calculation/view. */
    protected double getKm() {
        return km;
    }

    protected void doHistoricalDischargeOutQ(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        double value = Double.valueOf(
            bundle.getData(generator.getCallContext()).toString());
        generator.addDomainMarker(
            new StyledValueMarker(value, theme), visible);
    }

    protected void doHistoricalDischargeOutW(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        double value = Double.valueOf(
            bundle.getData(generator.getCallContext()).toString());
        generator.addValueMarker(
            new StyledValueMarker(value, theme), visible);
    }

    private void doPointsOut(
        DiagramGenerator generator,
        double[][] data,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), theme);
        StyledSeriesBuilder.addPoints(series, data, true);
        generator.addAxisSeries(series, axisName, visible);
    }

    /** Handle WQKms data by finding w/q values at given km. */
    protected void doWQKmsPointOut(
        DiagramGenerator generator,
        WQKms wqkms,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doWQKmsPointOut");
        String title = bundle.getFacetDescription();
        XYSeries series = new StyledXYSeries(
            title,
            theme);

        double[] kms = wqkms.getKms();

        for (int i = 0 ; i< kms.length; i++) {
            if (Math.abs(kms[i] - getKm()) <= KM_EPSILON) {
                series.add(wqkms.getQ(i), wqkms.getW(i));
                generator.addAxisSeries(series, axisName, visible);
                if(visible && theme.parseShowPointLabel()) {
                    List<XYTextAnnotation> textAnnos =
                        new ArrayList<XYTextAnnotation>();
                    XYTextAnnotation anno = new CollisionFreeXYTextAnnotation(
                            title,
                            wqkms.getQ(i),
                            // TODO add a percentage to the extend of W axis
                            wqkms.getW(i));
                    textAnnos.add(anno);
                    RiverAnnotation flysAnno = new RiverAnnotation(
                        null, null, null, theme);
                    flysAnno.setTextAnnotations(textAnnos);
                    generator.addAnnotations(flysAnno);
                }
                return;
            }
        }

        log.warn("No WQ found for km " + getKm());
    }

    protected void doYRiverAnnotationOut(DiagramGenerator generator,
        RiverAnnotation annotations,
        ThemeDocument theme,
        boolean visible
    ) {
        if (visible) {
            annotations.setTheme(theme);
            generator.addYAnnotation(annotations, axisName);
        }
    }

    protected void doRiverAnnotationOut(DiagramGenerator generator,
        RiverAnnotation annotations,
        ThemeDocument theme,
        boolean visible
    ) {
        if (visible) {
            annotations.setTheme(theme);
            generator.addAnnotations(annotations);
        }
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
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
