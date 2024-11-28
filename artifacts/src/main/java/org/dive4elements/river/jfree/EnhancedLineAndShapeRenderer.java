/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.BooleanList;
import org.jfree.util.ShapeUtilities;

/**
 * Renderer with additional the additional functionality of renderering minima
 * and/or maxima of dataseries contained in datasets.
 */
public class EnhancedLineAndShapeRenderer extends XYLineAndShapeRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** Own log. */
    private static final Logger log =
        LogManager.getLogger(EnhancedLineAndShapeRenderer.class);

    protected BooleanList isMinimumShapeVisible;
    protected BooleanList isMaximumShapeVisible;
    protected BooleanList showLineLabel;

    protected Map<Integer, Double> seriesMinimum;
    protected Map<Integer, Double> seriesMinimumX;
    protected Map<Integer, Double> seriesMaximum;

    protected Map<Integer, Font> lineLabelFonts;
    protected Map<Integer, Color> lineLabelTextColors;
    protected BooleanList showLineLabelBG;
    protected Map<Integer, Color> lineLabelBGColors;


    public EnhancedLineAndShapeRenderer(boolean lines, boolean shapes) {
        super(lines, shapes);
        this.isMinimumShapeVisible = new BooleanList();
        this.isMaximumShapeVisible = new BooleanList();
        this.showLineLabel         = new BooleanList();
        this.showLineLabelBG       = new BooleanList();
        this.seriesMinimum         = new HashMap<Integer, Double>();
        this.seriesMaximum         = new HashMap<Integer, Double>();
        this.seriesMinimumX        = new HashMap<Integer, Double>();
        this.lineLabelFonts        = new HashMap<Integer, Font>();
        this.lineLabelTextColors   = new HashMap<Integer, Color>();
        this.lineLabelBGColors     = new HashMap<Integer, Color>();
    }


    /**
     * Draw a background-box of a text to render.
     * @param g2 graphics device to use
     * @param text text to draw
     * @param textX x-position for text
     * @param textY y-position for text
     * @param bgColor color to fill box with.
     */
    public static void drawTextBox(Graphics2D g2,
        String text, float textX, float textY, Color bgColor
    ) {
        Rectangle2D hotspotBox = g2.getFontMetrics().getStringBounds(text, g2);
        float w = (float)hotspotBox.getWidth();
        float h = (float)hotspotBox.getHeight();
        hotspotBox.setRect(textX, textY-h, w, h);
        Color oldColor = g2.getColor();
        g2.setColor(bgColor);
        g2.fill(hotspotBox);
        g2.setColor(oldColor);
    }


    /**
     * Whether or not a specific item in a series (maybe the maxima) should
     * be rendered with shape.
     */
    public boolean getItemShapeVisible(
        XYDataset dataset,
        int series,
        int item
    ){
        if (super.getItemShapeVisible(series, item)) {
            return true;
        }

        if (isMinimumShapeVisible(series)
            && isMinimum(dataset, series, item)
        ) {
            return true;
        }

        if (isMaximumShapeVisible(series)
            && isMaximum(dataset, series, item)
        ) {
            return true;
        }

        return false;
    }


    /**
     * Rectangle used to draw maximums shape.
     */
    public Shape getMaximumShape(int series, int column) {
        return new Rectangle2D.Double(-5d, -5d, 10d, 10d);
    }


    /**
     * Rectangle used to draw minimums shape.
     */
    public Shape getMinimumShape(int series, int column) {
        return new Rectangle2D.Double(-5d, -5d, 10d, 10d);
    }


    /** Get fill paint for the maximum indicators. */
    public Paint getMaximumFillPaint(int series, int column) {
        Paint p = getItemPaint(series, column);

        if (p instanceof Color) {
            Color c = (Color) p;
            Color b = c;

            for (int i = 0; i < 2; i++) {
                b = b.darker();
            }

            return b;
        }

        log.warn("Item paint is no instance of Color!");
        return p;
    }


    /** Get fill paint for the minimum indicators. */
    public Paint getMinimumFillPaint(int series, int column) {
        Paint p = getItemPaint(series, column);

        if (p instanceof Color) {
            Color c = (Color) p;
            Color b = c;

            for (int i = 0; i < 2; i++) {
                b = b.darker();
            }

            return b;
        }

        log.warn("Item paint is no instance of Color!");
        return p;
    }


    /**
     * Overrides XYLineAndShapeRenderer.drawSecondaryPass() to call an adapted
     * method getItemShapeVisible() which now takes an XYDataset. So, 99% of
     * code equal the code in XYLineAndShapeRenderer.
     */
    @Override
    protected void drawSecondaryPass(
        Graphics2D       g2,
        XYPlot           plot,
        XYDataset        dataset,
        int              pass,
        int              series,
        int              item,
        ValueAxis        domainAxis,
        Rectangle2D      dataArea,
        ValueAxis        rangeAxis,
        CrosshairState   crosshairState,
        EntityCollection entities
    ) {
        Shape entityArea = null;

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        if (getItemShapeVisible(dataset, series, item)) {
            Shape shape = null;

            // OPTIMIZE: instead of calculating minimum and maximum for every
            //           point, calculate it just once (assume that dataset
            //           content does not change during rendering).
            // NOTE:     Above OPTIMIZE might already be fulfilled to
            //           most extend.
            boolean isMinimum = isMinimumShapeVisible(series)
                && isMinimum(dataset, series, item);

            boolean isMaximum = isMaximumShapeVisible(series)
                && isMaximum(dataset, series, item);

            if (isMinimum) {
                log.debug("Create a Minimum shape.");
                shape = getMinimumShape(series, item);
            }
            else if (isMaximum) {
                log.debug("Create a Maximum shape.");
                shape = getMaximumShape(series, item);
            }
            else {
                shape = getItemShape(series, item);
            }

            if (orientation == PlotOrientation.HORIZONTAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, transY1,
                        transX1);
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, transX1,
                        transY1);
            }
            entityArea = shape;
            if (shape.intersects(dataArea)) {
                if (getItemShapeFilled(series, item)) {
                    if (getUseFillPaint()) {
                        g2.setPaint(getItemFillPaint(series, item));
                    }
                    else {
                        g2.setPaint(getItemPaint(series, item));
                    }
                    g2.fill(shape);
                }
                if (getDrawOutlines()) {
                    if (getUseOutlinePaint()) {
                        g2.setPaint(getItemOutlinePaint(series, item));
                    }
                    else {
                        g2.setPaint(getItemPaint(series, item));
                    }
                    g2.setStroke(getItemOutlineStroke(series, item));
                    g2.draw(shape);
                }

                if (isMinimum) {
                    g2.setPaint(getMinimumFillPaint(series, item));
                    g2.fill(shape);
                    g2.setPaint(getItemOutlinePaint(series, item));
                    g2.setStroke(getItemOutlineStroke(series, item));
                    g2.draw(shape);
                }
                else if (isMaximum) {
                    g2.setPaint(getMaximumFillPaint(series, item));
                    g2.fill(shape);
                    g2.setPaint(getItemOutlinePaint(series, item));
                    g2.setStroke(getItemOutlineStroke(series, item));
                    g2.draw(shape);
                }
            }
        } // if (getItemShapeVisible(dataset, series, item))

        double xx = transX1;
        double yy = transY1;
        if (orientation == PlotOrientation.HORIZONTAL) {
            xx = transY1;
            yy = transX1;
        }

        // Draw the item label if there is one...
        if (isItemLabelVisible(series, item)) {
            drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
                    (y1 < 0.0));
        }

        // Draw label of line.
        if (dataset instanceof XYSeriesCollection
            && isShowLineLabel(series)
            && isMinimumX (dataset, series, item)
            ) {
            XYSeries xYSeries = ((XYSeriesCollection)dataset)
                .getSeries(series);
            String waterlevelLabel = (xYSeries instanceof HasLabel)
                ? ((HasLabel)xYSeries).getLabel()
                : xYSeries.getKey().toString();
            // TODO Force water of some German rivers to flow
            // direction mountains.

            Font oldFont = g2.getFont();

            Color oldColor = g2.getColor();
            g2.setFont(this.getLineLabelFont(series));
            g2.setColor(this.getLineLabelTextColor(series));
            g2.setBackground(Color.black);

            // Try to always display label if the data is visible.
            if (!isPointInRect(dataArea, xx, yy)) {
                // Move into the data area.
                xx = Math.max(xx, dataArea.getMinX());
                xx = Math.min(xx, dataArea.getMaxX());
                yy = Math.max(yy, dataArea.getMinY());
                yy = Math.min(yy, dataArea.getMaxY());
            }

            // Move to right until no collisions exist anymore
            Shape hotspot = TextUtilities.calculateRotatedStringBounds(
                waterlevelLabel, g2, (float)xx, (float)yy-3f,
                TextAnchor.CENTER_LEFT,
                0f, TextAnchor.CENTER_LEFT);
            while (JFreeUtil.collides(hotspot, entities,
                                      CollisionFreeLineLabelEntity.class)) {
                xx += 5f;
                hotspot = TextUtilities.calculateRotatedStringBounds(
                    waterlevelLabel,
                    g2,
                    (float)xx,
                    (float)yy-3f,
                    TextAnchor.CENTER_LEFT,
                    0f,
                    TextAnchor.CENTER_LEFT);
            }

            // Register to avoid collissions.
            entities.add(new CollisionFreeLineLabelEntity(hotspot,
                1, "", ""));

            // Fill background.
            if (isShowLineLabelBG(series)) {
                drawTextBox(g2, waterlevelLabel, (float)xx, (float)yy-3f,
                    getLineLabelBGColor(series));
            }

            g2.drawString(waterlevelLabel, (float)xx, (float)yy-3f);

            g2.setFont(oldFont);
            g2.setColor(oldColor);
        }

        int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
        updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
                rangeAxisIndex, transX1, transY1, orientation);

        // Add an entity for the item, but only if it falls within the data
        // area...
        if (entities != null && isPointInRect(dataArea, xx, yy)) {
            addEntity(entities, entityArea, dataset, series, item, xx, yy);
        }
    }


    /**
     * Sets whether or not the minimum should be rendered with shape.
     */
    public void setIsMinimumShapeVisisble(int series, boolean isVisible) {
        this.isMinimumShapeVisible.setBoolean(series, isVisible);
    }


    /**
     * Whether or not the minimum should be rendered with shape.
     */
    public boolean isMinimumShapeVisible(int series) {
        if (this.isMinimumShapeVisible.size() <= series) {
            return false;
        }

        return isMinimumShapeVisible.getBoolean(series);
    }


    /**
     * Sets whether or not the maximum should be rendered with shape.
     */
    public void setIsMaximumShapeVisible(int series, boolean isVisible) {
        this.isMaximumShapeVisible.setBoolean(series, isVisible);
    }


    /**
     * Whether or not the maximum should be rendered with shape.
     */
    public boolean isMaximumShapeVisible(int series) {
        if (this.isMaximumShapeVisible.size() <= series) {
            return false;
        }

        return isMaximumShapeVisible.getBoolean(series);
    }

    /** Whether or not a label should be shown for series. */
    public boolean isShowLineLabel(int series) {
        if (this.showLineLabel.size() <= series) {
            return false;
        }

        return showLineLabel.getBoolean(series);
    }


    /** Sets whether or not a label should be shown for series. */
    public void setShowLineLabel(boolean showLineLabel, int series) {
        this.showLineLabel.setBoolean(series, showLineLabel);
    }


    /** Whether or not a label should be shown for series. */
    public boolean isShowLineLabelBG(int series) {
        if (this.showLineLabelBG.size() <= series) {
            return false;
        }

        return showLineLabelBG.getBoolean(series);
    }


    public void setShowLineLabelBG(int series, boolean doShow) {
        this.showLineLabelBG.setBoolean(series, doShow);
    }

    public Color getLineLabelBGColor(int series) {
        if (this.lineLabelBGColors.size() <= series) {
            return null;
        }

        return this.lineLabelBGColors.get(series);
    }

    public void setLineLabelBGColor(int series, Color color) {
        this.lineLabelBGColors.put(series, color);
    }

    public Color getLineLabelTextColor(int series) {
        if (this.lineLabelTextColors.size() <= series) {
            return null;
        }

        return this.lineLabelTextColors.get(series);
    }

    public void setLineLabelTextColor(int series, Color color) {
        this.lineLabelTextColors.put(series, color);
    }

    public void setLineLabelFont(Font font, int series) {
        this.lineLabelFonts.put(series, font);
    }

    public Font getLineLabelFont(int series) {
        return this.lineLabelFonts.get(series);
    }


    /**
     * True if the given item of given dataset has the smallest
     * X value within this set.
     */
    public boolean isMinimumX(XYDataset dataset, int series, int item) {
        return dataset.getXValue(series, item) == getMinimumX(dataset, series);
    }


    /**
     * Get Minimum X Value of a given series in a dataset.
     * The value is stored for later use if queried the first time.
     */
    public double getMinimumX(XYDataset dataset, int series) {
        Integer key = Integer.valueOf(series);
        Double  old = seriesMinimumX.get(key);

        if (old != null) {
            return old.doubleValue();
        }

        log.debug("Compute minimum of Series: " + series);

        double min = Double.MAX_VALUE;

        for (int i = 0, n = dataset.getItemCount(series); i < n; i++) {
            double tmpValue = dataset.getXValue(series, i);

            if (tmpValue < min) {
                min = tmpValue;
            }
        }

        seriesMinimumX.put(key, Double.valueOf(min));

        return min;
    }


    /**
     * True if the given item of given dataset has the smallest
     * Y value within this set.
     */
    public boolean isMinimum(XYDataset dataset, int series, int item) {
        return dataset.getYValue(series, item) == getMinimum(dataset, series);
    }


    /**
     * Get Minimum Y Value of a given series in a dataset.
     * The value is stored for later use if queried the first time.
     */
    public double getMinimum(XYDataset dataset, int series) {
        Integer key = Integer.valueOf(series);
        Double  old = seriesMinimum.get(key);

        if (old != null) {
            return old.doubleValue();
        }

        log.debug("Compute minimum of Series: " + series);

        double min = Double.MAX_VALUE;

        for (int i = 0, n = dataset.getItemCount(series); i < n; i++) {
            double tmpValue = dataset.getYValue(series, i);

            if (tmpValue < min) {
                min = tmpValue;
            }
        }

        seriesMinimum.put(key, Double.valueOf(min));

        return min;
    }


    /**
     * True if the given item of given dataset has the biggest
     * Y value within this set.
     */
    public boolean isMaximum(XYDataset dataset, int series, int item) {
        return dataset.getYValue(series, item) == getMaximum(dataset, series);
    }


    /**
     * Get maximum Y Value of a given series in a dataset.
     * The value is stored for later use if queried the first time.
     */
    public double getMaximum(XYDataset dataset, int series) {
        Integer key = Integer.valueOf(series);
        Double  old = seriesMaximum.get(key);

        if (old != null) {
            return old.doubleValue();
        }

        log.debug("Compute maximum of Series: " + series);

        double max = -Double.MAX_VALUE;

        for (int i = 0, n = dataset.getItemCount(series); i < n; i++) {
            double tmpValue = dataset.getYValue(series, i);

            if (tmpValue > max) {
                max = tmpValue;
            }
        }

        seriesMaximum.put(key, Double.valueOf(max));

        return max;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
