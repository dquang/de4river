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
import org.w3c.dom.Node;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultTheme implements Theme {

    /** The name of the theme.*/
    protected String name;

    /** The description of the theme.*/
    protected String description;

    protected String facet;

    protected int index;


    /** The map storing the fields of this theme.*/
    protected Map<String, ThemeField> fields;

    /** The map storing the attributes of this theme.*/
    protected Map<String, String> attr;


    /**
     * Initializes the components of this Theme.
     */
    public DefaultTheme(String name, String description) {
        this.name        = name;
        this.description = description;
        this.fields      = new HashMap<String, ThemeField>();
        this.attr        = new HashMap<String, String>();
    }


    public void init(Node config) {
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public String getFacet() {
        return facet;
    }


    public void setFacet(String facet) {
        this.facet = facet;
    }


    public int getIndex() {
        return index;
    }


    public void setIndex(int index) {
        this.index = index;
    }


    public void addAttribute(String name, String value) {
        if (name != null && value != null) {
            attr.put(name, value);
        }
    }


    public String getAttribute(String name) {
        return attr.get(name);
    }


    public void addField(String name, ThemeField field) {
        if (name != null && field != null) {
            fields.put(name, field);
        }
    }


    public void setFieldValue(String name, Object value) {
        if (name != null && value != null) {
            ThemeField field = fields.get(name);

            if (field != null) {
                field.setValue(value);
            }
        }
    }


    public ThemeField getField(String name) {
        return fields.get(name);
    }


    public String getFieldType(String name) {
        ThemeField field = fields.get(name);

        return field != null ? field.getType() : null;
    }


    public Object getFieldValue(String name) {
        ThemeField field = fields.get(name);

        return field != null ? field.getValue() : null;
    }


    public Document toXML() {
        Document doc = XMLUtils.newDocument();

        ElementCreator cr = new ElementCreator(doc, null, null);

        Element theme = cr.create("theme");
        theme.setAttribute("facet", facet);
        theme.setAttribute("index", String.valueOf(index));

        appendAttributes(cr, theme);
        appendFields(cr, theme);

        doc.appendChild(theme);

        return doc;
    }


    /**
     * Appends the attributes configured for this theme.
     *
     * @param cr The ElementCreator.
     * @param theme The document root element.
     */
    protected void appendAttributes(ElementCreator cr, Element theme) {

        for (Map.Entry<String, String> entry: attr.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();

            if (key != null && val != null) {
                cr.addAttr(theme, key, val);
            }
        }
    }


    /**
     * Appends the fields configured for this theme.
     *
     * @param cr The ElementCreator.
     * @param theme The document root element.
     */
    protected void appendFields(ElementCreator cr, Element theme) {

        for (ThemeField field: fields.values()) {
            Document doc = field.toXML();
            Node    root = doc.getFirstChild();

            theme.appendChild(theme.getOwnerDocument().importNode(root, true));
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
