/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class BooleanAttribute extends VisibleAttribute {


    public BooleanAttribute(String name, boolean value, boolean visible) {
        super(name, value, visible);
    }


    /**
     * Calls VisibleAttribute.toXML() and appends afterwards an attribute
     * <i>type</i> with value <i>boolean</i>.
     *
     * @param parent The parent Node.
     *
     * @return the new Node that represents this Attribute.
     */
    @Override
    public Node toXML(Node parent) {

        Element ele = (Element) super.toXML(parent);
        ele.setAttribute("type", "boolean");

        return ele;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
