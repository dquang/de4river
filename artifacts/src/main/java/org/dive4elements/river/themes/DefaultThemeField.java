/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultThemeField implements ThemeField {

    protected String name;

    protected Map<String, Object> attr;


    public DefaultThemeField(String name) {
        this.name = name;
        this.attr = new HashMap<String, Object>();
    }


    public String getName() {
        return name;
    }


    public String getType() {
        return (String) getAttribute("type");
    }


    public Object getValue() {
        return getAttribute("value");
    }


    public void setValue(Object value) {
        setAttribute("value", value);
    }


    public Object getAttribute(String name) {
        return attr.get(name);
    }


    public void setAttribute(String name, Object value) {
        if (name == null || value == null) {
            return;
        }

        attr.put(name, value);
    }


    public Document toXML() {
        Document doc = XMLUtils.newDocument();

        ElementCreator cr = new ElementCreator(doc, null, null);

        Element field = cr.create("field");

        for (Map.Entry<String, Object> entry: attr.entrySet()) {
            cr.addAttr(field, entry.getKey(), (String)entry.getValue());
        }

        doc.appendChild(field);

        return doc;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
