/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;



/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ExportSection extends TypeSection {

    public static final String WIDTH_ATTR  = "width";
    public static final String HEIGHT_ATTR = "height";


    public ExportSection() {
        super("export");
    }


    public void setWidth(int width) {
        if (width <= 0) {
            return;
        }

        setIntegerValue(WIDTH_ATTR, width);
    }


    public Integer getWidth() {
        return getIntegerValue(WIDTH_ATTR);
    }


    public void setHeight(int height) {
        if (height <= 0) {
            return;
        }

        setIntegerValue(HEIGHT_ATTR, height);
    }


    public Integer getHeight() {
        return getIntegerValue(HEIGHT_ATTR);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
