/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.Color;

import org.jfree.chart.plot.IntervalMarker;

import org.dive4elements.river.themes.ThemeDocument;

/**
 * Marker that represents a highlighted interval.
 *
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 */
public class StyledDomainMarker extends IntervalMarker {

    private static final long serialVersionUID = -4369417661339512342L;

    private final Color backgroundColor, backgroundColor2;

    public StyledDomainMarker(double start, double end, ThemeDocument theme) {
        super(start, end);

        backgroundColor = theme.parseAreaBackgroundColor();
        backgroundColor2 = new Color(
            255 - backgroundColor.getRed(),
            255 - backgroundColor.getGreen(),
            255 - backgroundColor.getBlue());
        useSecondColor(false);

        int alpha = 100 - theme.parseAreaTransparency(50);
        setAlpha(alpha / 100.0f);
    }

    /**
     * To properly differentiate several styled domain markers side by side,
     * we can use this switch to toggle between two colors.
     * @param secondColor
     */
    public void useSecondColor(boolean secondColor) {
        if(!secondColor) {
            if(backgroundColor != null)
                setPaint(backgroundColor);
        }
        else {
            if(backgroundColor2 != null)
                setPaint(backgroundColor2);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
