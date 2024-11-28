/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.axis.ValueAxis;

import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;

import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;

import org.jfree.data.xy.XYDataset;

import org.jfree.text.TextUtilities;

import org.jfree.ui.RectangleEdge;

public class ShapeRenderer
extends      StandardXYItemRenderer {

    public static class Entry {
        protected Shape   shape;
        protected Shape   frame;
        protected Paint   paint;
        protected boolean filled;

        public Entry(
            Shape shape,
            Paint paint,
            boolean filled
        ) {
            this.shape = shape;
            this.paint = paint;
            this.filled = filled;
        }

        public Entry(
            Shape   shape,
            Shape   frame,
            Paint   paint,
            boolean filled
        ) {
            this.shape  = shape;
            this.frame  = frame;
            this.paint  = paint;
            this.filled = filled;
        }

        public Shape getShape() {
            return shape;
        }

        public void setShape(Shape shape) {
            this.shape = shape;
        }


        public Paint getPaint() {
            return paint;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        public boolean getFilled() {
            return filled;
        }

        public void setFilled(boolean filled) {
            this.filled = filled;
        }

        public boolean equals(Object other) {
            Entry entry = (Entry)other;
            return filled == entry.filled
                   &&   paint.equals(entry.paint)
                   &&   shape.equals(entry.shape);
        }

        public int hashCode() {
            return
                shape.hashCode() ^
                paint.hashCode() ^
                (filled ? 1231 : 1237);
        }
    } // class Entry

    public interface LabelGenerator {
        String createLabel(Entry entry);
    } // interface EntryLabelGenerator

    protected Entry []  entries;

    protected List<Rectangle2D> labelBoundingBoxes;

    protected Rectangle2D area;

    public ShapeRenderer() {
        this(SHAPES);
    }

    public ShapeRenderer(int type) {
        super(type);
    }

    public ShapeRenderer(Map<Entry, Integer> map) {
        super(SHAPES);
        setEntries(map);
    }

    public void setEntries(Entry [] entries) {
        this.entries = entries;
    }

    public void setEntries(Map<Entry, Integer> map) {
        Entry [] entries = new Entry[map.size()];

        for (Map.Entry<Entry, Integer> entry: map.entrySet()) {
            entries[entry.getValue()] = entry.getKey();
        }

        setEntries(entries);
    }

    @Override
    public Shape getSeriesShape(int series) {
        return entries[series].shape;
    }

    public Shape getSeriesFrame(int series) {
        return entries[series].frame;
    }

    @Override
    public Paint getSeriesPaint(int series) {
        return entries[series].paint;
    }

    @Override
    public boolean getItemShapeFilled(int series, int item) {
        return entries[series].filled;
    }

    @Override
    public XYItemRendererState initialise(
        Graphics2D        g2,
        Rectangle2D       dataArea,
        XYPlot            plot,
        XYDataset         data,
        PlotRenderingInfo info
    ) {
        if (labelBoundingBoxes == null) {
            labelBoundingBoxes = new ArrayList<Rectangle2D>(32);
        }
        else {
            labelBoundingBoxes.clear();
        }

        area = dataArea;

        return super.initialise(g2, dataArea, plot, data, info);
    }

    @Override
    public void drawItem(
        Graphics2D          g2,
        XYItemRendererState state,
        Rectangle2D         dataArea,
        PlotRenderingInfo   info,
        XYPlot              plot,
        ValueAxis           domainAxis,
        ValueAxis           rangeAxis,
        XYDataset           dataset,
        int                 series,
        int                 item,
        CrosshairState      crosshairState,
        int                 pass
    ) {
        if (!getItemVisible(series, item)) {
            return;
        }

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(x1) || Double.isNaN(y1)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double x = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double y = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        if (dataArea.contains(x, y))
            super.drawItem(
                g2,
                state,
                dataArea,
                info,
                plot,
                domainAxis,
                rangeAxis,
                dataset,
                series,
                item,
                crosshairState,
                pass);
    }

    protected Point2D shiftBox(Rectangle2D box) {

        double cx1 = area.getX();
        double cy1 = area.getY();
        double cx2 = cx1 + area.getWidth();
        double cy2 = cy1 + area.getHeight();

        double bx1 = box.getX();
        double by1 = box.getY();
        double bx2 = bx1 + box.getWidth();
        double by2 = by1 + box.getHeight();

        double dx;
        double dy;

        if (bx1 < cx1) {
            dx = cx1 - bx1;
        }
        else if (bx2 > cx2) {
            dx = cx2 - bx2;
        }
        else {
            dx = 0d;
        }

        if (by1 < cy1) {
            dy = cy1 - by1;
        }
        else if (by2 > cy2) {
            dy = cy2 - by2;
        }
        else {
            dy = 0d;
        }

        return new Point2D.Double(dx, dy);
    }

    @Override
    protected void drawItemLabel(
        Graphics2D      g2,
        PlotOrientation orientation,
        XYDataset       dataset,
        int             series,
        int             item,
        double          x,
        double          y,
        boolean         negative
    ) {
        XYItemLabelGenerator generator = getItemLabelGenerator(series, item);
        if (generator == null) {
            return;
        }

        Font labelFont = getItemLabelFont(series, item);

        Paint paint = getItemLabelPaint(series, item);

        g2.setFont(labelFont);
        g2.setPaint(paint);

        String label = generator.generateLabel(dataset, series, item);

        ATTEMPS: for (int attempt = 0; attempt < 2; ++attempt) {
            // get the label position..
            ItemLabelPosition position = null;

            boolean pos;
            switch (attempt) {
                case 0: pos = negative; break;
                case 1: pos = !negative; break;
                default: break ATTEMPS;
            }

            if (pos) {
                position = getNegativeItemLabelPosition(series, item);
            }
            else {
                position = getPositiveItemLabelPosition(series, item);
            }

            // work out the label anchor point...
            Point2D anchorPoint = calculateLabelAnchorPoint(
                position.getItemLabelAnchor(), x, y, orientation);

            Shape labelShape = TextUtilities.calculateRotatedStringBounds(
                label, g2,
                (float)anchorPoint.getX(), (float)anchorPoint.getY(),
                position.getTextAnchor(), position.getAngle(),
                position.getRotationAnchor());

            Rectangle2D bbox = labelShape.getBounds2D();

            Point2D shift = shiftBox(bbox);

            bbox = new Rectangle2D.Double(
                bbox.getX() + shift.getX(),
                bbox.getY() + shift.getY(),
                bbox.getWidth(),
                bbox.getHeight());

            if (labelBoundingBoxes != null) {
                for (Rectangle2D old: labelBoundingBoxes) {
                    if (old.intersects(bbox)) {
                        continue ATTEMPS;
                    }
                }
                labelBoundingBoxes.add(bbox);
            }

            TextUtilities.drawRotatedString(
                label, g2,
                (float)(anchorPoint.getX() + shift.getX()),
                (float)(anchorPoint.getY() + shift.getY()),
                position.getTextAnchor(), position.getAngle(),
                position.getRotationAnchor());
            break;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
