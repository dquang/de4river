/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Attribute;
import org.dive4elements.artifactdatabase.state.DefaultSection;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class TypeSection extends DefaultSection {

    private static final Logger log = LogManager.getLogger(TypeSection.class);

    public TypeSection(String key) {
        super(key);
    }


    /** Set a string value for a attribute with additional (choice) type. */
    public void setChoiceStringValue(
        String key,
        String value,
        String choiceType
    ) {
        if (value == null || value.length() == 0) {
            return;
        }

        Attribute attr = getAttribute(key);
        if (attr == null) {
            attr = new ChoiceStringAttribute(key, value, true, choiceType);
            addAttribute(key, attr);
        }
        else {
            attr.setValue(value);
        }
    }


    public void setStringValue(String key, String value) {
        if (value == null || value.length() == 0) {
            return;
        }

        Attribute attr = getAttribute(key);
        if (attr == null) {
            attr = new StringAttribute(key, value, true);
            addAttribute(key, attr);
        }
        else {
            attr.setValue(value);
        }
    }


    public String getStringValue(String key) {
        Attribute attr = getAttribute(key);

        if (attr instanceof StringAttribute) {
            return (String) attr.getValue();
        }

        log.debug("attribute " + key + " not found in typesection.getString");

        return null;
    }


    public void setIntegerValue(String key, int value) {
        Attribute attr = getAttribute(key);
        if (attr == null) {
            attr = new IntegerAttribute(key, value, true);
            addAttribute(key, attr);
        }
        else {
            attr.setValue(value);
        }
    }


    public Integer getIntegerValue(String key) {
        Attribute attr = getAttribute(key);

        if (attr instanceof IntegerAttribute) {
            return (Integer) attr.getValue();
        }

        return null;
    }



    public void setDoubleValue(String key, double value) {
        Attribute attr = getAttribute(key);
        if (attr == null) {
            attr = new DoubleAttribute(key, value, true);
            addAttribute(key, attr);
        }
        else {
            attr.setValue(value);
        }
    }


    public Double getDoubleValue(String key) {
        Attribute attr = getAttribute(key);

        if (attr instanceof DoubleAttribute) {
            return (Double) attr.getValue();
        }

        return null;
    }


    public void setBooleanValue(String key, boolean value) {
        Attribute attr = getAttribute(key);
        if (attr == null) {
            attr = new BooleanAttribute(key, value, true);
            addAttribute(key, attr);
        }
        else {
            attr.setValue(value);
        }
    }


    public Boolean getBooleanValue(String key) {
        Attribute attr = getAttribute(key);

        if (attr instanceof BooleanAttribute) {
            return (Boolean) attr.getValue();
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
