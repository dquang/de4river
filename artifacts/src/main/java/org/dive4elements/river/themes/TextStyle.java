/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import org.dive4elements.river.jfree.StableXYDifferenceRenderer;

import java.awt.Color;
import java.awt.Font;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.annotations.XYTextAnnotation;

public class TextStyle {
    @SuppressWarnings("unused")
    private static Logger log = LogManager.getLogger(TextStyle.class);

    protected Color   textColor;
    protected Font    font;
    protected Color   bgColor;
    protected boolean showBg;
    protected boolean isVertical;

    public TextStyle(Color fgColor, Font font, Color bgColor,
        boolean showBg, boolean isVertical
    ) {
        this.textColor  = fgColor;
        this.font       = font;
        this.bgColor    = bgColor;
        this.showBg     = showBg;
        this.isVertical = isVertical;
    }

    public void apply(XYTextAnnotation ta) {
        if (textColor != null) {
            ta.setPaint(textColor);
        }
        if (font != null) {
            ta.setFont(font);
        }
        if (showBg) {
            ta.setBackgroundPaint(bgColor);
        }
        if (isVertical) {
            ta.setRotationAngle(270f*Math.PI/180f);
        }
        else {
            ta.setRotationAngle(0);
        }
    }

    public void apply(StableXYDifferenceRenderer renderer) {
        renderer.setLabelColor(textColor);
        renderer.setLabelFont(font);
        if (this.showBg) {
            renderer.setLabelBGColor(bgColor);
        }
    }
}
