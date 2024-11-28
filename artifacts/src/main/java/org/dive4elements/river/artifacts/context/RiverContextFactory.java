/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.context;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifactdatabase.state.StateEngine;

import org.dive4elements.artifactdatabase.transition.Transition;
import org.dive4elements.artifactdatabase.transition.TransitionEngine;

import org.dive4elements.artifacts.ArtifactContextFactory;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.ContextInjector;

import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.ElementConverter;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.Module;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.artifacts.model.ZoomScale;

import org.dive4elements.river.artifacts.states.StateFactory;

import org.dive4elements.river.artifacts.transitions.TransitionFactory;

import org.dive4elements.river.exports.GeneratorLookup;
import org.dive4elements.river.exports.OutGenerator;

import org.dive4elements.river.model.River;
import org.dive4elements.river.themes.Theme;
import org.dive4elements.river.themes.ThemeFactory;
import org.dive4elements.river.themes.ThemeGroup;
import org.dive4elements.river.themes.ThemeMapping;
import org.dive4elements.river.utils.RiverUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The ArtifactContextFactory is used to initialize basic components and put
 * them into the global context of the application.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RiverContextFactory implements ArtifactContextFactory {

    /** The log that is used in this class. */
    private static Logger log = LogManager.getLogger(RiverContextFactory.class);

    /** The XPath to the artifacts configured in the configuration. */
    public static final String XPATH_ARTIFACTS =
        "/artifact-database/artifacts/artifact";

    /** The XPath to the name of the artifact. */
    public static final String XPATH_ARTIFACT_NAME = "/artifact/@name";

    /** The XPath to the xlink ref in an artifact configuration. */
    public static final String XPATH_XLINK = "xlink:href";

    /** The XPath to the transitions configured in the artifact config. */
    public static final String XPATH_TRANSITIONS =
        "/artifact/states/transition";

    /** The XPath to the states configured in the artifact config. */
    public static final String XPATH_STATES =
        "/artifact/states/state";

    public static final String XPATH_OUTPUT_GENERATORS =
        "/artifact-database/output-generators//output-generator";

    public static final String XPATH_THEME_CONFIG =
        "/artifact-database/flys/themes/configuration/text()";

    public static final String XPATH_THEMES =
        "theme";

    public static final String XPATH_THEME_GROUPS =
        "/themes/themegroup";

    public static final String XPATH_THEME_MAPPINGS =
        "/themes/mappings/mapping";

    public static final String XPATH_RIVER_WMS =
        "/artifact-database/floodmap/river";

    public static final String XPATH_MODULES =
        "/artifact-database/modules/module";

    private static final String XPATH_ZOOM_SCALES =
        "/artifact-database/options/zoom-scales/zoom-scale";

    private static final String XPATH_DGM_PATH =
        "/artifact-database/options/dgm-path/text()";

    private static GlobalContext GLOBAL_CONTEXT_INSTANCE;


    /**
     * Creates a new D4EArtifactContext object and initialize all
     * components required by the application.
     *
     * @param config The artifact server configuration.
     * @return a D4EArtifactContext.
     */
    @Override
    public GlobalContext createArtifactContext(Document config) {
        RiverContext context = new RiverContext(config);

        configureTransitions(config, context);
        configureStates(config, context);
        configureOutGenerators(config, context);
        configureThemes(config, context);
        configureThemesMappings(config, context);
        configureFloodmapWMS(config, context);
        configureModules(config, context);
        configureZoomScales(config, context);
        configureDGMPath(config, context);

        synchronized (RiverContextFactory.class) {
            GLOBAL_CONTEXT_INSTANCE = context;
        }

        return context;
    }

    public static synchronized GlobalContext getGlobalContext() {
        return GLOBAL_CONTEXT_INSTANCE;
    }


    private void configureDGMPath(Document config, RiverContext context) {
        String dgmPath = (String) XMLUtils.xpath(
            config,
            XPATH_DGM_PATH,
            XPathConstants.STRING);

        context.put("dgm-path", dgmPath);
    }


    protected void configureZoomScales(Document config, RiverContext context) {
        NodeList list = (NodeList)XMLUtils.xpath(
            config,
            XPATH_ZOOM_SCALES,
            XPathConstants.NODESET);
        ZoomScale scale = new ZoomScale();
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element)list.item(i);
            String river = "default";
            double range = 0d;
            double radius = 10d;
            if (element.hasAttribute("river")) {
                river = element.getAttribute("river");
            }
            if (!element.hasAttribute("range")) {
                continue;
            }
            else {
                String r = element.getAttribute("range");
                try {
                    range = Double.parseDouble(r);
                }
                catch (NumberFormatException nfe) {
                    continue;
                }
            }
            if (!element.hasAttribute("radius")) {
                continue;
            }
            else {
                String r = element.getAttribute("radius");
                try {
                    radius = Double.parseDouble(r);
                }
                catch (NumberFormatException nfe) {
                    continue;
                }
            }
            scale.addRange(river, range, radius);
       }
       context.put("zoomscale", scale);
    }


    /**
     * This method initializes the transition configuration.
     *
     * @param config the config document.
     * @param context the RiverContext.
     */
    protected void configureTransitions(
        Document config,
        RiverContext context
    ) {
        TransitionEngine engine = new TransitionEngine();

        List<Document> artifacts = getArtifactConfigurations(config);
        log.info("Found " + artifacts.size() + " artifacts in the config.");

        for (Document doc: artifacts) {

            String artName = (String) XMLUtils.xpath(
                doc, XPATH_ARTIFACT_NAME, XPathConstants.STRING);

            NodeList list = (NodeList) XMLUtils.xpath(
                doc, XPATH_TRANSITIONS, XPathConstants.NODESET);

            if (list == null) {
                log.warn("The artifact " + artName +
                    " has no transitions configured.");
                continue;
            }

            int trans = list.getLength();

            log.info(
                "Artifact '" + artName + "' has " + trans + " transitions.");

            for (int i = 0; i < trans; i++) {
                Transition t = TransitionFactory.createTransition(
                    list.item(i));
                String     s = t.getFrom();
                engine.addTransition(s, t);
            }
        }

        context.put(RiverContext.TRANSITION_ENGINE_KEY, engine);
    }


    /**
     * This method returns all artifact documents defined in
     * <code>config</code>. <br>NOTE: The artifact configurations need to be
     * stored in own files referenced by an xlink.
     *
     * @param config The global configuration.
     *
     * @return an array of Artifact configurations.
     */
    protected List<Document> getArtifactConfigurations(Document config) {
        NodeList artifacts = (NodeList) XMLUtils.xpath(
            config, XPATH_ARTIFACTS, XPathConstants.NODESET);

        int count = artifacts.getLength();

        ArrayList<Document> docs = new ArrayList<Document>(count);

        for (int i = 0; i < count; i++) {
            Element tmp = (Element) artifacts.item(i);

            String xlink = tmp.getAttribute(XPATH_XLINK);
            xlink        = Config.replaceConfigDir(xlink);

            if (!xlink.isEmpty()) {
                File file = new File(xlink);
                if (!file.isFile() || !file.canRead()) {
                    log.warn("Artifact configuration '"
                        + file + "' not found.");
                } else {
                    Document doc = XMLUtils.parseDocument(file);
                    if (doc != null) {
                        docs.add(doc);
                    }
                }
                continue;
            }
            Document doc = XMLUtils.newDocument();
            Node copy = doc.adoptNode(tmp.cloneNode(true));
            doc.appendChild(copy);
            docs.add(doc);
        }
        return docs;
    }


    /**
     * This method initializes the transition configuration.
     *
     * @param config the config document.
     * @param context the RiverContext.
     */
    protected void configureStates(Document config, RiverContext context) {
        StateEngine engine = new StateEngine();

        List<Document> artifacts = getArtifactConfigurations(config);
        log.info("Found " + artifacts.size() + " artifacts in the config.");

        for (Document doc: artifacts) {
            List<State> states = new ArrayList<State>();

            String artName = (String) XMLUtils.xpath(
                doc, XPATH_ARTIFACT_NAME, XPathConstants.STRING);

            NodeList stateList = (NodeList) XMLUtils.xpath(
                doc, XPATH_STATES, XPathConstants.NODESET);

            if (stateList == null) {
                log.warn("The artifact " + artName +
                    " has no states configured.");
                continue;
            }

            int count = stateList.getLength();

            log.info(
                "Artifact '" + artName + "' has " + count + " states.");

            for (int i = 0; i < count; i++) {
                states.add(StateFactory.createState(
                    stateList.item(i)));
            }

            engine.addStates(artName, states);
        }

        context.put(RiverContext.STATE_ENGINE_KEY, engine);
    }


    /**
     * This method intializes the provided output generators.
     *
     * @param config the config document.
     * @param context the RiverContext.
     */
    protected void configureOutGenerators(
        Document config,
        RiverContext context
    ) {
        NodeList outGenerators = (NodeList) XMLUtils.xpath(
            config,
            XPATH_OUTPUT_GENERATORS,
            XPathConstants.NODESET);

        int num = outGenerators == null ? 0 : outGenerators.getLength();

        if (num == 0) {
            log.warn("No output generators configured in this application.");
            return;
        }

        log.info("Found " + num + " configured output generators.");

        GeneratorLookup generators = new GeneratorLookup();

        int idx = 0;

        for (int i = 0; i < num; i++) {
            Element item = (Element)outGenerators.item(i);

            String names      = item.getAttribute("names").trim();
            String clazz      = item.getAttribute("class").trim();
            String converter  = item.getAttribute("converter").trim();
            String injectors  = item.getAttribute("injectors").trim();

            if (names.isEmpty() || clazz.isEmpty()) {
                continue;
            }

            Class<OutGenerator> generatorClass = null;

            try {
                generatorClass = (Class<OutGenerator>)Class.forName(clazz);
            }
            catch (ClassNotFoundException cnfe) {
                log.error(cnfe, cnfe);
                continue;
            }

            Object cfg = null;

            if (!converter.isEmpty()) {
                try {
                    ElementConverter ec =
                        (ElementConverter)Class.forName(converter)
                            .newInstance();
                    cfg = ec.convert(item);
                }
                catch (ClassNotFoundException cnfe) {
                    log.error(cnfe, cnfe);
                }
                catch (InstantiationException ie) {
                    log.error(ie);
                }
                catch (IllegalAccessException iae) {
                    log.error(iae);
                }
            }

            List<ContextInjector> cis = null;

            if (!injectors.isEmpty()) {
                cis = new ArrayList<ContextInjector>();
                for (String injector: injectors.split("[\\s,]+")) {
                    try {
                        ContextInjector ci = (ContextInjector)Class
                            .forName(injector)
                            .newInstance();
                        ci.setup(item);
                        cis.add(ci);
                    }
                    catch (ClassNotFoundException cnfe) {
                        log.error(cnfe, cnfe);
                    }
                    catch (InstantiationException ie) {
                        log.error(ie);
                    }
                    catch (IllegalAccessException iae) {
                        log.error(iae);
                    }
                }
            }

            for (String key: names.split("[\\s,]+")) {
                if (!(key = key.trim()).isEmpty()) {
                    generators.putGenerator(key, generatorClass, cfg, cis);
                    idx++;
                }
            }
        }

        log.info("Successfully loaded " + idx + " output generators.");
        context.put(RiverContext.OUTGENERATORS_KEY, generators);
        context.put(RiverContext.FACETFILTER_KEY, generators);
    }


    /**
     * This methods reads the configured themes and puts them into the
     * RiverContext.
     *
     * @param config The global configuration.
     * @param context The RiverContext.
     */
    protected void configureThemes(Document config, RiverContext context) {
        log.debug("RiverContextFactory.configureThemes");

        Document cfg = getThemeConfig(config);

        NodeList themeGroups = (NodeList) XMLUtils.xpath(
            cfg, XPATH_THEME_GROUPS, XPathConstants.NODESET);

        int groupNum = themeGroups != null ? themeGroups.getLength() : 0;

        if (groupNum == 0) {
            log.warn("There are no theme groups configured!");
        }

        log.info("Found " + groupNum + " theme groups in configuration");

        List<ThemeGroup> groups = new ArrayList<ThemeGroup>();

        for (int g = 0; g < groupNum; g++) {
            Element themeGroup = (Element) themeGroups.item(g);
            NodeList themes = (NodeList) XMLUtils.xpath(
                themeGroup, XPATH_THEMES, XPathConstants.NODESET);

            int num = themes != null ? themes.getLength() : 0;

            if (num == 0) {
                log.warn("There are no themes configured!");
                return;
            }

            log.info("Theme group has " + num + " themes.");

            Map<String, Theme> theThemes = new HashMap<String, Theme>();

            for (int i = 0; i < num; i++) {
                Node theme = themes.item(i);

                Theme theTheme = ThemeFactory.createTheme(cfg, theme);

                if (theme != null) {
                    theThemes.put(theTheme.getName(), theTheme);
                }
            }
            String gName = themeGroup.getAttribute("name");
            groups.add(new ThemeGroup(gName, theThemes));

            log.info(
                "Initialized " + theThemes.size() + "/" + num + " themes " +
                "of theme-group '" + gName + "'");
        }
        context.put(RiverContext.THEMES, groups);
    }

    /**
     * This method is used to retrieve the theme configuration document.
     *
     * @param config The global configuration.
     *
     * @return the theme configuration.
     */
    protected Document getThemeConfig(Document config) {
        String themeConfig = (String) XMLUtils.xpath(
            config,
            XPATH_THEME_CONFIG,
            XPathConstants.STRING);

        themeConfig = Config.replaceConfigDir(themeConfig);

        log.debug("Parse theme cfg: " + themeConfig);

        return XMLUtils.parseDocument(
            new File(themeConfig), true, XMLUtils.CONF_RESOLVER);
    }


    protected void configureThemesMappings(
        Document cfg,
        RiverContext context
    ) {
        log.debug("RiverContextFactory.configureThemesMappings");

        Document config = getThemeConfig(cfg);

        NodeList mappings = (NodeList) XMLUtils.xpath(
            config, XPATH_THEME_MAPPINGS, XPathConstants.NODESET);

        int num = mappings != null ? mappings.getLength() : 0;

        if (num == 0) {
            log.warn("No theme <--> facet mappins found!");
            return;
        }

        Map<String, List<ThemeMapping>> mapping =
            new HashMap<String, List<ThemeMapping>>();

        for (int i = 0; i < num; i++) {
            Element node = (Element)mappings.item(i);

            String from              = node.getAttribute("from");
            String to                = node.getAttribute("to");
            String pattern           = node.getAttribute("pattern");
            String masterAttrPattern = node.getAttribute("masterAttr");
            String outputPattern     = node.getAttribute("output");

            if (from.length() > 0 && to.length() > 0) {
                List<ThemeMapping> tm = mapping.get(from);

                if (tm == null) {
                    tm = new ArrayList<ThemeMapping>();
                    mapping.put(from, tm);
                }

                tm.add(new ThemeMapping(
                    from, to, pattern, masterAttrPattern, outputPattern));
            }
        }

        log.debug("Found " + mapping.size() + " theme mappings.");

        context.put(RiverContext.THEME_MAPPING, mapping);
    }


    /**
     * Reads configured floodmap river WMSs from floodmap.xml and
     * loads them into the given RiverContext.
     * @param cfg
     * @param context
     */
    protected void configureFloodmapWMS(Document cfg, RiverContext context) {
        Map<String, String> riverWMS = new HashMap<String, String>();

        NodeList rivers = (NodeList) XMLUtils.xpath(
            cfg, XPATH_RIVER_WMS, XPathConstants.NODESET);

        int num = rivers != null ? rivers.getLength() : 0;

        for (int i = 0; i < num; i++) {
            Element e = (Element) rivers.item(i);

            String river = e.getAttribute("name");
            String url   = RiverUtils.getUserWMSUrl();

            if (river != null && url != null) {
                riverWMS.put(river, url);
            }
        }

        log.debug("Found " + riverWMS.size() + " river WMS.");

        context.put(RiverContext.RIVER_WMS, riverWMS);
    }


    /**
     * This method initializes the modules configuration.
     *
     * @param config the config document.
     * @param context the RiverContext.
     */
    protected void configureModules(Document cfg, RiverContext context) {
        NodeList modulenodes = (NodeList) XMLUtils.xpath(
            cfg, XPATH_MODULES, XPathConstants.NODESET);

        int num = modulenodes != null ? modulenodes.getLength() : 0;
        ArrayList<Module> modules = new ArrayList<Module>(num);

        for (int i = 0; i < num; i++) {
            Element e = (Element) modulenodes.item(i);
            String modulename = e.getAttribute("name");
            String attrselected = e.getAttribute("selected");
            boolean selected = attrselected == null ? false :
                attrselected.equalsIgnoreCase("true");
            log.debug("Loaded module " + modulename);
            NodeList children = e.getChildNodes();
            List<String> rivers = new ArrayList<String>(children.getLength());
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element ce = (Element)children.item(j);
                if (ce.hasAttribute("uuid")) {
                    rivers.add(ce.getAttribute("uuid"));
                }
                else if (ce.hasAttribute("name")) {
                    List<River> allRivers = RiverFactory.getRivers();
                    String name = ce.getAttribute("name");
                    for (River r: allRivers) {
                        if (name.equals(r.getName())) {
                            rivers.add(r.getModelUuid());
                            break;
                        }
                    }
                }
            }
            modules.add(new Module(modulename, selected, rivers));
        }
        context.put(RiverContext.MODULES, modules);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
