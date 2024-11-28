/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import org.dive4elements.river.themes.ThemeDocument;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


/**
 * Utility to apply theme-settings to a renderer.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class XYStyle implements Style {

    private static Logger log = LogManager.getLogger(XYStyle.class);

    protected ThemeDocument theme;

    protected XYLineAndShapeRenderer renderer;

    protected Shape shape;


    public XYStyle(ThemeDocument theme) {
        this.theme = theme;
    }

    public XYStyle(ThemeDocument theme, Shape shape) {
        this.theme = theme;
        this.shape = shape;
    }


    /**
     * Applies line color, size and type attributes to renderer, also
     * whether to draw lines and/or points.
     */
    @Override
    public XYLineAndShapeRenderer applyTheme(
        XYLineAndShapeRenderer r,
        int idx
    ) {
        this.renderer = r;
        if (shape != null) {
            r.setShape(shape);
        }
        if (theme == null) {
            // Hurray we already applied nothing :)
            return r;
        }
        applyUseFillPaint(r);
        applyLineColor(r, idx);
        applyLineSize(r, idx);
        applyLineType(r, idx);
        applyShowLine(r, idx);
        applyShowPoints(r, idx);
        applyPointSize(r, idx);
        applyPointColor(r, idx);
        applyShowMinimum(r, idx);
        applyShowMaximum(r, idx);

        // Line label styles
        applyShowLineLabel(r, idx);
        applyShowLineLabelBG(r, idx);
        applyLineLabelFont(r, idx);
        applyLineLabelColor(r, idx);
        applyLineLabelBGColor(r, idx);

        // Point label styles
        // TODO: Currently point label are annotations and
        // are not drawn this way
        /*
        applyShowPointLabelBG(r, idx);
        applyLinePointFont(r, idx);
        applyLinePointColor(r, idx);
        applyLinePointBGColor(r, idx);*/

        return r;
    }

    protected void applyUseFillPaint(XYLineAndShapeRenderer r) {
        Boolean use = theme.parseUseFillPaint();
        if (use != null) {
            r.setUseFillPaint(use);
        }
    }


    /** Set line color to renderer. */
    protected void applyLineColor(XYLineAndShapeRenderer r, int idx) {
        Color c = theme.parseLineColorField();
        if (c != null) {
            r.setSeriesPaint(idx, c);
        }
    }


    /** Tells the renderer whether or not to add a label to a line. */
    protected void applyShowLineLabel(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }
        boolean showLabelLine = theme.parseShowLineLabel();
        boolean anyLabel = showLabelLine || theme.parseShowWidth() ||
                           theme.parseShowLevel() ||
                           theme.parseShowMiddleHeight();
        ((EnhancedLineAndShapeRenderer)r).setShowLineLabel(anyLabel, idx);
    }


    /** Tells the renderer whether or not to fill the bg of a lines label. */
    protected void applyShowLineLabelBG(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }
        boolean showLabelLine = theme.parseLabelShowBackground();
        ((EnhancedLineAndShapeRenderer)r).setShowLineLabelBG(
            idx, showLabelLine);
    }

    /** Tell the renderer which font (and -size and -style) to use for
     * linelabels. */
    protected void applyLineLabelFont(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }
        ((EnhancedLineAndShapeRenderer)r).setLineLabelFont(
                theme.parseTextFont(), idx);
    }

    /** Tell the renderer which color to use for
     * linelabels. */
    protected void applyLineLabelColor(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }
        ((EnhancedLineAndShapeRenderer)r).setLineLabelTextColor(
                idx, theme.parseTextColor());
    }

    /** Tell the renderer which color to use for bg of
     * linelabels. */
    protected void applyLineLabelBGColor(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }
        ((EnhancedLineAndShapeRenderer)r).setLineLabelBGColor(idx,
            theme.parseTextBackground());
    }

    /** Set stroke of series. */
    protected void applyLineSize(XYLineAndShapeRenderer r, int idx) {
        int size = theme.parseLineWidth();
        r.setSeriesStroke(
            idx,
            new BasicStroke(size));
    }


    /** Set stroke strength of series. */
    protected void applyLineType(XYLineAndShapeRenderer r, int idx) {
        int size = theme.parseLineWidth();
        float[] dashes = theme.parseLineStyle();

        // Do not apply the dashed style.
        if (dashes.length <= 1) {
            return;
        }

        r.setSeriesStroke(
            idx,
            new BasicStroke(size,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND,
                            1.0f,
                            dashes,
                            0.0f));
    }


    protected void applyPointSize(XYLineAndShapeRenderer r, int idx) {
        int size = theme.parsePointWidth();
        int dim  = 2 * size;

        r.setSeriesShape(idx, new Ellipse2D.Double(-size, -size, dim, dim));
    }


    protected void applyPointColor(XYLineAndShapeRenderer r, int idx) {
        Color c = theme.parsePointColor();

        if (c != null) {
            r.setSeriesFillPaint(idx, c);
            r.setUseFillPaint(true);
            r.setDrawOutlines(false);
        }
    }


    /**
     * Sets form and visibility of points.
     */
    protected void applyShowPoints(XYLineAndShapeRenderer r, int idx) {
        boolean show = theme.parseShowPoints();

        r.setSeriesShapesVisible(idx, show);
        r.setDrawOutlines(true);
    }


    protected void applyShowLine(XYLineAndShapeRenderer r, int idx) {
        boolean show = theme.parseShowLine();
        r.setSeriesLinesVisible(idx, show);
    }


    protected void applyShowMinimum(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }

        boolean visible = theme.parseShowMinimum();

        EnhancedLineAndShapeRenderer er = (EnhancedLineAndShapeRenderer) r;
        er.setIsMinimumShapeVisisble(idx, visible);
    }


    protected void applyShowMaximum(XYLineAndShapeRenderer r, int idx) {
        if (!(r instanceof EnhancedLineAndShapeRenderer)) {
            return;
        }

        boolean visible = theme.parseShowMaximum();

        EnhancedLineAndShapeRenderer er = (EnhancedLineAndShapeRenderer) r;
        er.setIsMaximumShapeVisible(idx, visible);
    }


    @Override
    public XYLineAndShapeRenderer getRenderer() {
        return this.renderer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
