/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifactdatabase.state.Attribute;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AxisSection extends TypeSection {

    public static final String IDENTIFIER_ATTR           = "id";
    public static final String LABEL_ATTR                = "label";
    public static final String SUGGESTED_LABEL_ATTR      = "suggested-label";
    public static final String FONTSIZE_ATTR             = "font-size";
    public static final String FIXATION_ATTR             = "fixation";
    public static final String UPPERRANGE_ATTR           = "upper";
    public static final String LOWERRANGE_ATTR           = "lower";
    public static final String UPPERRANGE_TIME_ATTR      = "upper-time";
    public static final String LOWERRANGE_TIME_ATTR      = "lower-time";


    public AxisSection() {
        super("axis");
    }


    public void setIdentifier(String identifier) {
        setStringValue(IDENTIFIER_ATTR, identifier);
    }


    public String getIdentifier() {
        return getStringValue(IDENTIFIER_ATTR);
    }


    public void setLabel(String label) {
        setStringValue(LABEL_ATTR, label);
    }


    public String getLabel() {
        return getStringValue(LABEL_ATTR);
    }


    public void setSuggestedLabel(String label) {
        setStringValue(SUGGESTED_LABEL_ATTR, label);
    }


    public String getSuggestedLabel() {
        return getStringValue(SUGGESTED_LABEL_ATTR);
    }

    public void setFontSize(int fontSize) {
        if (fontSize <= 0) {
            return;
        }

        setIntegerValue(FONTSIZE_ATTR, fontSize);
    }


    public Integer getFontSize() {
        return getIntegerValue(FONTSIZE_ATTR);
    }


    public void setFixed(boolean fixed) {
        setBooleanValue(FIXATION_ATTR, fixed);
    }


    public Boolean isFixed() {
        return getBooleanValue(FIXATION_ATTR);
    }


    public void setUpperRange(double upperRange) {
        setDoubleValue(UPPERRANGE_ATTR, upperRange);
    }


    public Double getUpperRange() {
        return getDoubleValue(UPPERRANGE_ATTR);
    }


    public void setLowerRange(double lowerRange) {
        setDoubleValue(LOWERRANGE_ATTR, lowerRange);
    }


    public Double getLowerRange() {
        return getDoubleValue(LOWERRANGE_ATTR);
    }

    public void setUpperTimeRange(long upperRange) {
        setStringValue(UPPERRANGE_TIME_ATTR, Long.toString(upperRange));
    }

    /* return the upper time rang limit as a long value that can be converted
     * to a date. If none is set null is returned. */
    public Long getUpperTimeRange() {
        try {
            return Long.valueOf(getStringValue(UPPERRANGE_TIME_ATTR));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setLowerTimeRange(long lowerRange) {
        setStringValue(LOWERRANGE_TIME_ATTR, Long.toString(lowerRange));
    }

    /* See getUpperTimeRange */
    public Long getLowerTimeRange() {
        try {
            return Long.valueOf(getStringValue(LOWERRANGE_TIME_ATTR));
        } catch (NumberFormatException e) {
            return null;
        }
    }



    @Override
    public void toXML(Node parent) {
        Document owner = parent.getOwnerDocument();
        Element   axis = owner.createElement("axis");

        parent.appendChild(axis);

        for (String key: getKeys()) {
            Attribute attr = getAttribute(key);
            attr.toXML(axis);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
