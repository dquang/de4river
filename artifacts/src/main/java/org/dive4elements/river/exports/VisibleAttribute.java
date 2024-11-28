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

import org.dive4elements.artifactdatabase.state.DefaultAttribute;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class VisibleAttribute extends DefaultAttribute {

    protected boolean visible;


    public VisibleAttribute(String name, Object value, boolean visible) {
        super(name, value);
        this.visible = visible;
    }


    /**
     * This implementation of Attribute calls DefaultAttribute.toXML() first.
     * After this, a new Attr <i>display</i> is added to the resulting Node.
     *
     * @param parent The parent Node.
     *
     * @return a new Node that represents this Attribute.
     */
    @Override
    public Node toXML(Node parent) {
        Element ele = (Element) super.toXML(parent);
        ele.setAttribute("display", String.valueOf(visible));

        return ele;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
