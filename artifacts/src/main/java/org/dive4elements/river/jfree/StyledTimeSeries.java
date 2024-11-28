/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.themes.ThemeDocument;

import org.jfree.data.time.TimeSeries;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StyledTimeSeries extends TimeSeries implements StyledSeries {

    private static final Logger log =
        LogManager.getLogger(StyledTimeSeries.class);


    protected Style style;


    public StyledTimeSeries(String key, ThemeDocument theme) {
        super(key);
        setStyle(new XYStyle(theme));
    }


    @Override
    public void setStyle(Style style) {
        this.style = style;
    }


    @Override
    public Style getStyle() {
        return style;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
