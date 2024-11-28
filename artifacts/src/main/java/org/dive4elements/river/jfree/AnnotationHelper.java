/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.jfree;

import org.dive4elements.river.themes.ThemeDocument;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.jfree.ui.TextAnchor;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import org.dive4elements.river.themes.LineStyle;
import org.dive4elements.river.themes.TextStyle;
import org.dive4elements.river.exports.ChartSettings;
import org.dive4elements.river.exports.LegendSection;
import org.dive4elements.river.exports.ChartArea;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Annotation helper class, handles plotting of annotations. */
public class AnnotationHelper {
    private static final Logger log = LogManager.getLogger(AnnotationHelper.class);

    protected static float ANNOTATIONS_AXIS_OFFSET = 0.02f;

    /* arr this would be better in chartsettings */
    public static final int    DEFAULT_FONT_SIZE       = 12;
    public static final String DEFAULT_FONT_NAME       = "Tahoma";


    public static void addYAnnotationsToRenderer(
        SortedMap<Integer, RiverAnnotation> yAnnotations,
        XYPlot plot,
        ChartSettings settings,
        Map<Integer, AxisDataset> datasets
    ) {
        List<RiverAnnotation> annotations = new ArrayList<RiverAnnotation>();

        for (Map.Entry<Integer, RiverAnnotation> entry:
                 yAnnotations.entrySet()) {
            int axis = entry.getKey();
            AxisDataset dataset = datasets.get(new Integer(axis));

            if (dataset == null || dataset.getRange() == null) {
                log.warn("No dataset available and active for axis " + axis);
            }
            else {
                RiverAnnotation ya = entry.getValue();
                for (StickyAxisAnnotation sta: ya.getAxisTextAnnotations()) {
                    sta.setAxisSymbol(axis);
                }
                annotations.add(ya);
            }
        }

        addAnnotationsToRenderer(annotations, plot, settings, datasets);
    }

    /**
     * Add annotations (Sticky, Text and hyk zones) to a plot.
     * @param annotations Annotations to add
     * @param plot XYPlot to add annotations to.
     * @param settings ChartSettings object for settings.
     * @param datasets Map of axis index and datasets
     */
    public static void addAnnotationsToRenderer(
        List<RiverAnnotation> annotations,
        XYPlot plot,
        ChartSettings settings,
        Map<Integer, AxisDataset> datasets
    ) {
        if (annotations == null || annotations.isEmpty()) {
            log.debug("addAnnotationsToRenderer: no annotations.");
            return;
        }

        // OPTMIMIZE: Pre-calculate positions
        ChartArea area = new ChartArea(
            plot.getDomainAxis(0),
            plot.getRangeAxis());

        // Walk over all Annotation sets.
        for (RiverAnnotation fa: annotations) {

            // Access text styling, if any.
            ThemeDocument theme = fa.getTheme();
            TextStyle textStyle = null;
            LineStyle lineStyle = null;

            // Get Theming information and add legend item.
            if (theme != null) {
                textStyle = theme.parseComplexTextStyle();
                lineStyle = theme.parseComplexLineStyle();
                if (fa.getLabel() != null) {
                    // Legend handling, maybe misplaced?
                    LegendItemCollection lic = new LegendItemCollection();
                    LegendItemCollection old = plot.getFixedLegendItems();

                    Color color = theme.parseLineColorField();
                    if (color == null) {
                        color = Color.BLACK;
                    }

                    Color textColor = theme.parseTextColor();
                    if (textColor == null) {
                        textColor = Color.BLACK;
                    }

                    LegendItem newItem = new LegendItem(fa.getLabel(), color);
                    LegendSection ls = (settings != null ?
                            settings.getLegendSection() : null);
                    newItem.setLabelFont (new Font(
                        DEFAULT_FONT_NAME,
                        Font.PLAIN,
                        ls != null ? ls.getFontSize() : null)
                    );

                    newItem.setLabelPaint(textColor);

                    lic.add(newItem);
                    // (Re-)Add prior legend entries.
                    if (old != null) {
                        old.addAll(lic);
                    }
                    else {
                        old = lic;
                    }
                    plot.setFixedLegendItems(old);
                }
            }

            // The 'Sticky' Annotations (at axis, with line and text).
            for (StickyAxisAnnotation sta: fa.getAxisTextAnnotations()) {
                addStickyAnnotation(
                    sta, plot, area, lineStyle, textStyle, theme,
                    datasets.get(new Integer(sta.getAxisSymbol())));
            }

            // Other Text Annotations (e.g. labels of (manual) points).
            for (XYTextAnnotation ta: fa.getTextAnnotations()) {
                // Style the text.
                if (textStyle != null) {
                    textStyle.apply(ta);
                }
                ta.setY(area.above(0.05d, ta.getY()));
                plot.getRenderer().addAnnotation(
                    ta, org.jfree.ui.Layer.FOREGROUND);
            }
        }
    }

    /**
     * Add a text and a line annotation.
     * @param area convenience to determine positions in plot.
     * @param theme (optional) theme document
     */
    public static void addStickyAnnotation(
        StickyAxisAnnotation annotation,
        XYPlot plot,
        ChartArea area,
        LineStyle lineStyle,
        TextStyle textStyle,
        ThemeDocument theme,
        AxisDataset dataset
    ) {
        // OPTIMIZE pre-calculate area-related values
        final float TEXT_OFF = 0.03f;

        XYLineAnnotation lineAnnotation = null;
        XYTextAnnotation textAnnotation = null;

        int axisIndex = annotation.getAxisSymbol();
        XYItemRenderer renderer = null;
        if (dataset != null && dataset.getDatasets().length > 0) {
            renderer = plot.getRendererForDataset(dataset.getDatasets()[0]);
        }
        else {
            renderer = plot.getRenderer();
        }

        if (annotation.atX()) {
            textAnnotation = new CollisionFreeXYTextAnnotation(
                annotation.getText(),
                annotation.getPos(),
                area.ofGround(TEXT_OFF));
            // OPTIMIZE externalize the calculation involving PI.
            //textAnnotation.setRotationAngle(270f*Math.PI/180f);
            lineAnnotation = createGroundStickAnnotation(
                area, annotation.getPos(), lineStyle);
            textAnnotation.setRotationAnchor(TextAnchor.CENTER_LEFT);
            textAnnotation.setTextAnchor(TextAnchor.CENTER_LEFT);
        }
        else {
            // Stick to the "right" (opposed to left) Y-Axis.
            if (axisIndex != 0 && plot.getRangeAxis(axisIndex) != null) {
                // OPTIMIZE: Pass a different area to this function,
                //           do the adding to renderer outside (let this
                //           function return the annotations).
                //           Note that this path is travelled rarely.
                textAnnotation = new CollisionFreeXYTextAnnotation(
                    annotation.getText(),
                    area.ofRight(TEXT_OFF),
                    annotation.getPos()
                );
                textAnnotation.setRotationAnchor(TextAnchor.CENTER_RIGHT);
                textAnnotation.setTextAnchor(TextAnchor.CENTER_RIGHT);
                lineAnnotation = createRightStickAnnotation(
                    area, annotation.getPos(), lineStyle);

                // hit-lines for duration curve
                ChartArea area2 = new ChartArea(
                    plot.getDomainAxis(), plot.getRangeAxis(axisIndex));
                if (!Float.isNaN(annotation.getHitPoint()) && theme != null) {
                    // New line annotation to hit curve.
                    if (theme.parseShowVerticalLine()) {
                        XYLineAnnotation hitLineAnnotation =
                            createStickyLineAnnotation(
                                StickyAxisAnnotation.SimpleAxis.X_AXIS,
                                annotation.getHitPoint(), annotation.getPos(),
                                // annotation.getHitPoint(),
                                area2, lineStyle);
                        renderer.addAnnotation(hitLineAnnotation,
                            org.jfree.ui.Layer.BACKGROUND);
                    }
                    if (theme.parseShowHorizontalLine()) {
                        XYLineAnnotation lineBackAnnotation =
                            createStickyLineAnnotation(
                                StickyAxisAnnotation.SimpleAxis.Y_AXIS2,
                                annotation.getPos(), annotation.getHitPoint(),
                                area2, lineStyle);
                        renderer.addAnnotation(lineBackAnnotation,
                            org.jfree.ui.Layer.BACKGROUND);
                    }
                }
            }
            else { // Stick to the left y-axis.
                textAnnotation = new CollisionFreeXYTextAnnotation(
                    annotation.getText(),
                    area.ofLeft(TEXT_OFF),
                    annotation.getPos());
                textAnnotation.setRotationAnchor(TextAnchor.CENTER_LEFT);
                textAnnotation.setTextAnchor(TextAnchor.CENTER_LEFT);
                lineAnnotation = createLeftStickAnnotation(
                    area, annotation.getPos(), lineStyle);
                if (!Float.isNaN(annotation.getHitPoint()) && theme != null) {
                    // New line annotation to hit curve.
                    if (theme.parseShowHorizontalLine()) {
                        XYLineAnnotation hitLineAnnotation =
                            createStickyLineAnnotation(
                                StickyAxisAnnotation.SimpleAxis.Y_AXIS,
                                annotation.getPos(), annotation.getHitPoint(),
                                area, lineStyle);
                        renderer.addAnnotation(hitLineAnnotation,
                            org.jfree.ui.Layer.BACKGROUND);
                    }
                    if (theme.parseShowVerticalLine()) {
                        XYLineAnnotation lineBackAnnotation =
                            createStickyLineAnnotation(
                                StickyAxisAnnotation.SimpleAxis.X_AXIS,
                                annotation.getHitPoint(), annotation.getPos(),
                                area, lineStyle);
                        renderer.addAnnotation(lineBackAnnotation,
                            org.jfree.ui.Layer.BACKGROUND);
                    }
                }
            }
        }

        // Style the text.
        if (textStyle != null) {
            textStyle.apply(textAnnotation);
        }

        // Add the Annotations to renderer.
        renderer.addAnnotation(textAnnotation, org.jfree.ui.Layer.FOREGROUND);
        renderer.addAnnotation(lineAnnotation, org.jfree.ui.Layer.FOREGROUND);
    }

   /**
     * Create annotation that sticks to "ground" (X) axis.
     * @param area helper to calculate coordinates
     * @param pos one-dimensional position (distance from axis)
     * @param lineStyle the line style to use for the line.
     */
    public static XYLineAnnotation createGroundStickAnnotation(
        ChartArea area, float pos, LineStyle lineStyle
    ) {
        // Style the line.
        if (lineStyle != null) {
            return new XYLineAnnotation(
                pos, area.atGround(),
                pos, area.ofGround(ANNOTATIONS_AXIS_OFFSET),
                new BasicStroke(lineStyle.getWidth()),lineStyle.getColor());
        }
        else {
            return new XYLineAnnotation(
                pos, area.atGround(),
                pos, area.ofGround(ANNOTATIONS_AXIS_OFFSET));
        }
    }


    /**
     * Create annotation that sticks to the second Y axis ("right").
     * @param area helper to calculate coordinates
     * @param pos one-dimensional position (distance from axis)
     * @param lineStyle the line style to use for the line.
     */
    public static XYLineAnnotation createRightStickAnnotation(
        ChartArea area, float pos, LineStyle lineStyle
    ) {
        // Style the line.
        if (lineStyle != null) {
            return new XYLineAnnotation(
                area.atRight(), pos,
                area.ofRight(ANNOTATIONS_AXIS_OFFSET), pos,
                new BasicStroke(lineStyle.getWidth()), lineStyle.getColor());
        }
        else {
            return new XYLineAnnotation(
                area.atRight(), pos,
                area.ofRight(ANNOTATIONS_AXIS_OFFSET), pos);
        }
    }
    /**
     * Create annotation that sticks to the first Y axis ("left").
     * @param area helper to calculate coordinates
     * @param pos one-dimensional position (distance from axis)
     * @param lineStyle the line style to use for the line.
     */
    public static XYLineAnnotation createLeftStickAnnotation(
        ChartArea area, float pos, LineStyle lineStyle
    ) {
        // Style the line.
        if (lineStyle != null) {
            return new XYLineAnnotation(
                area.atLeft(), pos,
                area.ofLeft(ANNOTATIONS_AXIS_OFFSET), pos,
                new BasicStroke(lineStyle.getWidth()), lineStyle.getColor());
        }
        else {
            return new XYLineAnnotation(
                area.atLeft(), pos,
                area.ofLeft(ANNOTATIONS_AXIS_OFFSET), pos);
        }
    }


    /**
     * Create a line from a axis to a given point.
     * @param axis   The "simple" axis.
     * @param fromD1 from-location in first dimension.
     * @param toD2   to-location in second dimension.
     * @param area   helper to calculate offsets.
     * @param lineStyle optional line style.
     */
    public static XYLineAnnotation createStickyLineAnnotation(
        StickyAxisAnnotation.SimpleAxis axis, float fromD1, float toD2,
        ChartArea area, LineStyle lineStyle
    ) {
        double anchorX1 = 0d, anchorX2 = 0d, anchorY1 = 0d, anchorY2 = 0d;
        switch(axis) {
            case X_AXIS:
                anchorX1 = fromD1;
                anchorX2 = fromD1;
                anchorY1 = area.atGround();
                anchorY2 = toD2;
                break;
            case Y_AXIS:
                anchorX1 = area.atLeft();
                anchorX2 = toD2;
                anchorY1 = fromD1;
                anchorY2 = fromD1;
                break;
            case Y_AXIS2:
                anchorX1 = area.atRight();
                anchorX2 = toD2;
                anchorY1 = fromD1;
                anchorY2 = fromD1;
                break;
        }
        // Style the line.
        if (lineStyle != null) {
            return new XYLineAnnotation(
                anchorX1, anchorY1,
                anchorX2, anchorY2,
                new BasicStroke(lineStyle.getWidth()), lineStyle.getColor());
        }
        else {
            return new XYLineAnnotation(
                anchorX1, anchorY1,
                anchorX2, anchorY2);
        }
    }

};
