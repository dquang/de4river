/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;


/**
 * Settings regarding legend of chart.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LegendSection extends TypeSection {

    public static final String VISIBILITY_ATTR  = "visibility";
    public static final String FONTSIZE_ATTR    = "font-size";
    public static final String AGGREGATION_ATTR = "aggregation-threshold";


    public LegendSection() {
        super("legend");
    }


    /** Register font size attribute and value. */
    public void setFontSize(int fontSize) {
        if (fontSize <= 0) {
            return;
        }

        setIntegerValue(FONTSIZE_ATTR, fontSize);
    }


    public Integer getFontSize() {
        return getIntegerValue(FONTSIZE_ATTR);
    }


    public Integer getAggregationThreshold() {
        return getIntegerValue(AGGREGATION_ATTR);
    }


    public void setAggregationThreshold(int aggregationThreshold) {
        setIntegerValue(AGGREGATION_ATTR, Math.abs(aggregationThreshold));
    }


    public void setVisibility(boolean visibility) {
        setBooleanValue(VISIBILITY_ATTR, visibility);
    }


    public Boolean getVisibility() {
        return getBooleanValue(VISIBILITY_ATTR);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
