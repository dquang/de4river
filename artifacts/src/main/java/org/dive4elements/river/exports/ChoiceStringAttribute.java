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
public class ChoiceStringAttribute extends StringAttribute {

    /** Indicator which type of choice is dealt with. */
    protected String choiceType;


    public ChoiceStringAttribute(String name,
                           String value,
                           boolean visible,
                           String choiceType) {
        super(name, value, visible);
        this.choiceType = choiceType;
    }


    /**
     * Calls VisibleAttribute.toXML() and appends afterwards an attribute
     * <i>type</i> with value <i>string</i>.
     *
     * @param parent The parent Node.
     *
     * @return the new Node that represents this Attribute.
     */
    @Override
    public Node toXML(Node parent) {
        Element ele = (Element) super.toXML(parent);
        ele.setAttribute("type", "string");
        ele.setAttribute("choice", choiceType);

        return ele;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :