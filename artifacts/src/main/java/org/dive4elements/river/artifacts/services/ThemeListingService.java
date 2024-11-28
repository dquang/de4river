/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.themes.Theme;
import org.dive4elements.river.themes.ThemeGroup;

import org.dive4elements.river.themes.ThemeFactory;
import org.dive4elements.river.artifacts.context.RiverContext;

/**
 * This service provides a list of themes filtered by the theme name.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class ThemeListingService extends D4EService {

    /** The log used in this service.*/
    private static Logger log = LogManager.getLogger(ThemeListingService.class);

    private static final String XPATH_THEME_NAME = "/theme/@name";

    protected Document doProcess(
        Document      data,
        GlobalContext context,
        CallMeta      callMeta
    ) {
        log.debug("ThemeListingService.process");
        String name = XMLUtils.xpathString(data, XPATH_THEME_NAME, null);

        if (name == null) {
            log.warn("No theme name provided.");
        }
        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            null,
            null);

        List<ThemeGroup> tgs =
            ThemeFactory.getThemeGroups((RiverContext) context);

        Element te = ec.create("themes");

        for (ThemeGroup tg: tgs) {
            Element elem = ec.create("themegroup");
            if (tg.getName().equals("virtual")) {
                continue;
            }
            ec.addAttr(elem, "name", tg.getName());
            Theme theme = tg.getThemeByName(name);
            Document d = theme.toXML();
            Node imported = result.importNode(d.getDocumentElement(), true);
            elem.appendChild(imported);
            te.appendChild(elem);
        }

        result.appendChild(te);
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
