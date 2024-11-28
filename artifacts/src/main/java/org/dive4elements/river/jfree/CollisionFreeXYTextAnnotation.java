/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.Shape;

import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Plot;

import org.jfree.text.TextUtilities;

import org.jfree.ui.RectangleEdge;

/**
 * Custom Annotations class that is drawn only if no collisions with other
 * already drawn CustomAnnotations in current plot are found.
 */
public class CollisionFreeXYTextAnnotation extends XYTextAnnotation {

    public CollisionFreeXYTextAnnotation(String text, double x, double y) {
        super(text, x, y);
    }


    /**
     * Draw the Annotation only if it does not collide with other
     * already drawn Annotations- texts.
     *
     * @param g2            the graphics device.
     * @param plot          the plot.
     * @param dataArea      the data area.
     * @param domainAxis    the domain axis.
     * @param rangeAxis     the range axis.
     * @param rendererIndex the render index.
     * @param info          state information, escpecially collects info about
     *                      already drawn shapes (and thus annotations), used
     *                      for collision detection.
     */
    @Override
    public void draw(
        java.awt.Graphics2D g2,
        XYPlot plot,
        java.awt.geom.Rectangle2D dataArea,
        ValueAxis domainAxis,
        ValueAxis rangeAxis,
        int rendererIndex,
        PlotRenderingInfo info
    ) {
        // From superclass, adjusted access only.
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);

        float anchorX = (float) domainAxis.valueToJava2D(
                this.getX(), dataArea, domainEdge);
        float anchorY = (float) rangeAxis.valueToJava2D(
                this.getY(), dataArea, rangeEdge);

        if (orientation == PlotOrientation.HORIZONTAL) {
            float tempAnchor = anchorX;
            anchorX = anchorY;
            anchorY = tempAnchor;
        }

        g2.setFont(getFont());
        Shape hotspot = TextUtilities.calculateRotatedStringBounds(
                getText(), g2, anchorX, anchorY, getTextAnchor(),
                getRotationAngle(), getRotationAnchor());

        // Deviation from superclass: prevent collision.
        if (JFreeUtil.collides(hotspot, info.getOwner().getEntityCollection(),
            XYAnnotationEntity.class)) {
            return;
        }

        if (this.getBackgroundPaint() != null) {
            g2.setPaint(this.getBackgroundPaint());
            g2.fill(hotspot);
        }
        g2.setPaint(getPaint());
        TextUtilities.drawRotatedString(getText(), g2, anchorX, anchorY,
                getTextAnchor(), getRotationAngle(), getRotationAnchor());
        if (this.isOutlineVisible()) {
            g2.setStroke(this.getOutlineStroke());
            g2.setPaint(this.getOutlinePaint());
            g2.draw(hotspot);
        }

        //String toolTip = getToolTipText();
        //String url = getURL();
        String toolTip = "CollisionFreeXYTextAnnotation";
        String url     = toolTip;

        if (toolTip != null || url != null) {
            addEntity(info, hotspot, rendererIndex, toolTip, url);
        }
        // XXX: DEAD CODE (as long as a hard value is assigned to toolTip
        /*
        else {
            addEntity(info, hotspot, rendererIndex,
                "CollisionFreeXYTextAnnotation",
                "CollisionFreeXYTextAnnotation");
        }
        */
    }

    /**
     * A utility method for adding an {@link CollisionFreeXYAnnotationEntity} to
     * a {@link PlotRenderingInfo} instance.
     *
     * @param info  the plot rendering info (<code>null</code> permitted).
     * @param hotspot  the hotspot area.
     * @param rendererIndex  the renderer index.
     * @param toolTipText  the tool tip text.
     * @param urlText  the URL text.
     */
    protected void addEntity(PlotRenderingInfo info,
                             Shape hotspot, int rendererIndex,
                             String toolTipText, String urlText) {
        if (info == null) {
            return;
        }
        EntityCollection entities = info.getOwner().getEntityCollection();
        if (entities == null) {
            return;
        }
        CollisionFreeXYTextAnnotationEntity entity =
            new CollisionFreeXYTextAnnotationEntity(hotspot,
                rendererIndex, toolTipText, urlText);
        entities.add(entity);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
