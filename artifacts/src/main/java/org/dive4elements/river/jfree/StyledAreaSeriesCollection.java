/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.jfree.data.xy.XYSeriesCollection;

import org.dive4elements.river.themes.ThemeDocument;

/**
 * One or more dataseries to draw a polygon (either "open up/downwards", or
 * the area between two curves), a theme-document and further display options.
 * The theme-document will later "style" the graphical representation.
 * The display options can be used to control the z-order and the axis of the
 * dataset.
 */
public class StyledAreaSeriesCollection extends XYSeriesCollection {
    private static final long serialVersionUID = 5274940965666948237L;

    /** Mode, how to draw/which areas to fill. */
    public enum FILL_MODE {UNDER, ABOVE, BETWEEN};

    /** MODE in use. */
    protected FILL_MODE mode;

    /** Theme-document with attributes about actual visual representation. */
    protected ThemeDocument theme;


    /**
     * @param theme the theme-document.
     */
    public StyledAreaSeriesCollection(ThemeDocument theme) {
        this.theme = theme;
        this.mode = FILL_MODE.BETWEEN;
   }


    /** Gets the Fill mode. */
    public FILL_MODE getMode() {
        return this.mode;
    }


    /** Sets the Fill mode. */
    public void setMode(FILL_MODE fMode) {
        this.mode = fMode;
    }


    /**
     * Applies line color, size and type attributes to renderer, also
     * whether to draw lines and/or points.
     * @param renderer Renderer to apply theme to.
     * @return \param renderer
     */
    public StableXYDifferenceRenderer applyTheme(
        StableXYDifferenceRenderer renderer
    ) {
        applyFillColor(renderer);
        applyShowShape(renderer);
        applyOutlineColor(renderer);
        applyOutlineStyle(renderer);
        applyShowAreaLabel(renderer);
        if (mode == FILL_MODE.UNDER) {
            renderer.setAreaCalculationMode(
                StableXYDifferenceRenderer.CALCULATE_NEGATIVE_AREA);
        }
        else if (mode == FILL_MODE.ABOVE) {
            renderer.setAreaCalculationMode(
                StableXYDifferenceRenderer.CALCULATE_POSITIVE_AREA);
        }
        else {
            renderer.setAreaCalculationMode(
                StableXYDifferenceRenderer.CALCULATE_ALL_AREA);
        }

        // Apply text style.
        theme.parseComplexTextStyle().apply(renderer);
        return renderer;
    }


    protected void applyFillColor(StableXYDifferenceRenderer renderer) {
        Color paint = theme.parseAreaBackgroundColor();

        int transparency = theme.parseAreaTransparency();
        if (transparency > 0 && paint != null) {
            paint = new Color(
                        paint.getRed(),
                        paint.getGreen(),
                        paint.getBlue(),
                        (int)((100 - transparency) * 2.55f));
        }

        if (paint != null && this.getMode() == FILL_MODE.ABOVE) {
            renderer.setPositivePaint(paint);
            renderer.setNegativePaint(new Color(0,0,0,0));
        }
        else if (paint != null && this.getMode() == FILL_MODE.UNDER) {
            renderer.setNegativePaint(paint);
            renderer.setPositivePaint(new Color(0,0,0,0));
        }
        else {
            if (paint == null)
                paint = new Color(177, 117, 102);
            renderer.setPositivePaint(paint);
            renderer.setNegativePaint(paint);
        }
    }


    protected void applyShowShape(StableXYDifferenceRenderer renderer) {
        boolean show = theme.parseAreaShowBorder();
        renderer.setDrawOutline(show);
    }


    protected void applyShowLine(StableXYDifferenceRenderer renderer) {
        boolean show = theme.parseShowLine();
        renderer.setShapesVisible(show);
    }


    protected void applyOutlineColor(StableXYDifferenceRenderer renderer) {
        Color c = theme.parseLineColorField();
        renderer.setOutlinePaint(c);
    }

    protected void applyOutlineWidth(StableXYDifferenceRenderer renderer) {
        // int size = theme.parseLineWidth();
        // XXX: Why is this not set?
    }

    /** Inform renderer whether it should draw a label. */
    protected void applyShowAreaLabel(StableXYDifferenceRenderer renderer) {
        renderer.setLabelArea(theme.parseShowAreaLabel());
    }

    protected void applyOutlineStyle(StableXYDifferenceRenderer renderer) {
        float[] dashes = theme.parseLineStyle();
        int size       = theme.parseLineWidth();

        Stroke stroke = null;

        if (dashes.length <= 1) {
            stroke = new BasicStroke(Integer.valueOf(size));
        }
        else {
            stroke = new BasicStroke(Integer.valueOf(size),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
                1.0f,
                dashes,
                0.0f);
        }

        renderer.setOutlineStroke(stroke);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
