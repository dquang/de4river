/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import java.awt.Color;

public class LineStyle {
    protected Color lineColor;
    protected int   lineWidth;

    public LineStyle(Color color, int width) {
        this.lineColor = color;
        this.lineWidth = width;
    }

    public int getWidth() {
        return lineWidth;
    }

    public Color getColor() {
        return lineColor;
    }
}
