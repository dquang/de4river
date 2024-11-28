/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Theme {

    /**
     * Method to initialize the theme.
     *
     * @param config The configuration node.
     */
    void init(Node config);


    /**
     * Returns the name of the theme.
     *
     * @return the name of the theme.
     */
    String getName();


    /**
     * Returns the description of the theme.
     *
     * @return the description of the theme.
     */
    String getDescription();


    String getFacet();

    void setFacet(String facet);

    int getIndex();

    void setIndex(int index);


    /**
     * Adds a new attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    void addAttribute(String name, String value);


    /**
     * Returns the value of a specific attribute.
     *
     * @param name the name of the attribute.
     *
     * @return the value of the attribute <i>name</i>.
     */
    String getAttribute(String name);


    /**
     * Adds a new field to the theme.
     *
     * @param name The name of the field.
     * @param field The field.
     */
    void addField(String name, ThemeField field);


    /**
     * Sets the value of an field.
     *
     * @param name The name of the field.
     * @param value The new value of the field.
     */
    void setFieldValue(String name, Object value);


    /**
     * Returns the field specified by name.
     *
     * @param name The name of the desired field.
     *
     * @return an field.
     */
    ThemeField getField(String name);


    /**
     * Returns the typename of a field.
     *
     * @param name the name of the field.
     *
     * @return the typename of a field.
     */
    String getFieldType(String name);


    /**
     * Returns the value of a field.
     *
     * @param name The name of the field.
     *
     * @return the value of a field.
     */
    Object getFieldValue(String name);


    /**
     * Dumps the theme to XML.
     *
     * @return a document.
     */
    Document toXML();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
