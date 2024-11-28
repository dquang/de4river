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

import org.jfree.chart.plot.ValueMarker;

/**
 * Marker that represents a single value.
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 */
public class StyledValueMarker extends ValueMarker {

    private static final long serialVersionUID = -3607777705307785140L;

    public StyledValueMarker(double value, ThemeDocument theme) {
        super(value);

        Color color = theme.parseAreaBackgroundColor();
        if(color == null) {
            color = Color.BLACK;
        }
        this.setPaint(color);

        int size = theme.parsePointWidth();
        setStroke(new BasicStroke(size));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
