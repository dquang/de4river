/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.river.client.shared.model.Style;
import org.dive4elements.river.client.shared.model.StyleSetting;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class StyleHelper {

    public static Style getStyle (Element element) {
        if (!element.getTagName().equals("theme")) {
            return null;
        }

        NodeList list = element.getElementsByTagName("field");
        Style style = new Style();

        style.setName (element.getAttribute("name"));
        style.setFacet (element.getAttribute("facet"));

        try {
            int ndx = Integer.parseInt(element.getAttribute("index"));
            style.setIndex (ndx);
        }
        catch(NumberFormatException nfe) {
            return null;
        }

        for(int i = 0; i < list.getLength(); i++) {
            Element     e = (Element) list.item(i);
            String hints = e.getAttribute("hints");

            StyleSetting set = new StyleSetting (
                e.getAttribute("name"),
                e.getAttribute("default"),
                e.getAttribute("display"),
                e.getAttribute("hints"),
                e.getAttribute("type"),
                (hints != null && hints.contains("hidden") ? true : false)
            );
            style.appendStyleSetting(set);
        }
        return style;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
