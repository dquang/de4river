/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.context.RiverContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 *
 * Mapping-matching rules:
 *
 */
public class ThemeFactory {

    private static Logger log = LogManager.getLogger(ThemeFactory.class);

    /** Trivial, hidden constructor. */
    private ThemeFactory() {
    }


    /**
     * Creates a new theme from <i>config</i>.
     *
     * @param themeCfg The theme config document that is used to fetch parent
     * themes.
     * @param config The theme config node.
     *
     * @return a new theme.
     */
    public static Theme createTheme(Document themeCfg, Node config) {
        String name = getName(config);
        String desc = getDescription(config);

        log.trace("Create new theme: " + name);

        Theme theme = new DefaultTheme(name, desc);

        parseInherits(themeCfg, config, theme);
        parseFields(config, theme);
        parseAttrs(config, theme);

        return theme;
    }


    /**
     * Get first matching theme for facet.
     *
     * @param name    Name to match "from" of theme mapping.
     * @param pattern String to 'compare' to pattern in mapping.
     * @param output  Name of the current output
     *
     * @return First matching theme.
     */
    public static Theme getTheme(
        RiverContext c,
        String name,
        String pattern,
        String output,
        String groupName)
    {
        if (log.isDebugEnabled()) {
            log.debug(
                "Search theme for: " + name + " - pattern: " + pattern);
        }

        if (c == null || name == null) {
            log.warn("Cannot search for theme.");
            return null;
        }

        // Fetch mapping and themes.
        @SuppressWarnings("unchecked")
        Map<String, List<ThemeMapping>> map = (Map<String, List<ThemeMapping>>)
            c.get(RiverContext.THEME_MAPPING);

        @SuppressWarnings("unchecked")
        List<ThemeGroup> tgs = (List<ThemeGroup>)
            c.get(RiverContext.THEMES);

        ThemeGroup group = null;
        for (ThemeGroup tg: tgs) {
            if (tg.getName().equals(groupName)) {
                group = tg;
                break;
            }
        }

        if (group == null) {
            log.warn("No theme group found: '" + groupName + "'");
            return null;
        }

        Map<String, Theme> t = group.getThemes();

        D4EArtifact artifact = (D4EArtifact) c.get(RiverContext.ARTIFACT_KEY);

        if (map == null || map.isEmpty() || t == null || t.isEmpty()) {
            log.warn("No mappings or themes found. Cannot retrieve theme!");
            return null;
        }

        List<ThemeMapping> mapping = map.get(name);

        if (mapping == null) {
            log.warn("No theme found for mapping: " + name);
            return null;
        }

        // Take first mapping of which all conditions are satisfied.
        for (ThemeMapping tm: mapping) {
            if (name.equals(tm.getFrom())
                && tm.applyPattern(pattern)
                && tm.masterAttrMatches(artifact)
                && tm.outputMatches(output))
            {
                String target = tm.getTo();

                log.debug("Found theme '" + target + "'");
                return t.get(target);
            }
        }

        String msg =
            "No theme found for '" + name +
            "' with pattern '" + pattern + "' and output " + output + ".";

        log.warn(msg);

        return null;
    }


    @SuppressWarnings("unchecked")
    public static List<ThemeGroup> getThemeGroups(RiverContext c) {
        List<ThemeGroup> tgs = (List<ThemeGroup>)
            c.get(RiverContext.THEMES);
        return tgs;
    }


    @SuppressWarnings("unchecked")
    public static List<Theme> getThemes (RiverContext c, String name) {
        List<ThemeGroup> tgs = (List<ThemeGroup>)
            c.get(RiverContext.THEMES);
        if (tgs == null) {
            return null;
        }

        List<Theme> themes = new ArrayList<Theme>();
        for (ThemeGroup tg: tgs) {
            themes.add(tg.getThemeByName(name));
        }
        return themes;
    }

    protected static String getName(Node config) {
        return ((Element)config).getAttribute("name");
    }


    protected static String getDescription(Node config) {
        return ((Element)config).getAttribute("desc");
    }


    protected static void parseInherits(Document themeCfg, Node cfg, Theme t) {
        parseInherits(themeCfg, cfg, t, null);
    }

    protected static void parseInherits(
        Document themeCfg,
        Node     cfg,
        Theme    t,
        Map<String, Node> themes
    ) {
        log.trace("ThemeFactory.parseInherits");

        NodeList inherits = ((Element)cfg).getElementsByTagName("inherit");

        int num = inherits.getLength();

        if (num == 0) {
            log.trace("Theme does not inherit from other themes.");
            return;
        }

        log.trace("Theme inherits from " + num + " other themes.");

        if (themes == null) {
            themes = buildThemeMap(themeCfg);
        }

        for (int i = 0; i < num; i++) {
            Node inherit = inherits.item(i);
            String from = ((Element)inherit).getAttribute("from");

            Node tmp = themes.get(from);

            parseInherits(themeCfg, tmp, t, themes);
            parseFields(tmp, t);
        }
    }

    protected static Map<String, Node> buildThemeMap(Document themeCfg) {
        Map<String, Node> map = new HashMap<String, Node>();
        String xpath = "/themes/themegroup/theme";

        NodeList nodes = (NodeList)XMLUtils.xpath(
            themeCfg, xpath, XPathConstants.NODESET);

        if (nodes != null) {
            for (int i = 0, N = nodes.getLength(); i < N; ++i) {
                Node node = nodes.item(i);
                String name = ((Element)node).getAttribute("name");
                map.put(name, node);
            }
        }
        return map;
    }


    protected static void parseFields(Node config, Theme theme) {
        if (config == null || theme == null) {
            log.warn("Parsing fields without node or theme is senseless!");
            return;
        }

        NodeList fields = ((Element)config).getElementsByTagName("field");

        int num = fields.getLength();

        log.trace("Found " + num + " own fields in this theme.");

        if (num == 0) {
            log.trace("Theme has no own fields.");
            return;
        }

        for (int i = 0; i < num; i++) {
            Node field = fields.item(i);

            addField(theme, field);
        }
    }


    protected static void addField(Theme theme, Node field) {
        String name = ((Element)field).getAttribute("name");

        log.trace("Add field " + name + " to theme " + theme.getName());

        NamedNodeMap attrs = field.getAttributes();

        int num = attrs != null ? attrs.getLength() : 0;

        if (num == 0) {
            log.warn("This field has no attributes.");
            return;
        }

        ThemeField theField = new DefaultThemeField(name);

        for (int i = 0; i < num; i++) {
            Node attr    = attrs.item(i);

            String key   = attr.getNodeName();
            String value = attr.getNodeValue();

            theField.setAttribute(key, value);
        }

        theme.addField(name, theField);
    }


    protected static void parseAttrs(Node config, Theme theme) {
        NamedNodeMap attrs = config.getAttributes();

        int num = attrs != null ? attrs.getLength() : 0;

        if (num == 0) {
            log.trace("Theme has no attributes set.");
            return;
        }

        for (int i = 0; i < num; i++) {
            Node attr = attrs.item(i);

            String name  = attr.getNodeName();
            String value = attr.getNodeValue();

            theme.addAttribute(name, value);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
