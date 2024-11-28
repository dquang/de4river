/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import org.w3c.dom.Document;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface ThemeField {

    /**
     * Returns the name of this field.
     *
     * @return the name of this field.
     */
    String getName();

    /**
     * Returns the type of this field.
     *
     * @return the type of this field.
     */
    String getType();


    /**
     * Returns the value of this field.
     *
     * @return the value of this field.
     */
    Object getValue();


    /**
     * Changes the value of this field.
     *
     * @param value The new value.
     */
    void setValue(Object value);


    /**
     * Sets the value of an attribute.
     *
     * @param name The name of an attribute.
     * @param value The value of an attribute.
     */
    void setAttribute(String name, Object value);


    /**
     * Dumps the field to XML.
     *
     * @return a document.
     */
    Document toXML();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
