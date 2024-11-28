/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.AttributedTheme;
import org.dive4elements.river.client.shared.model.ChartMode;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItem;
import org.dive4elements.river.client.shared.model.DefaultCollection;
import org.dive4elements.river.client.shared.model.DefaultCollectionItem;
import org.dive4elements.river.client.shared.model.DefaultFacet;
import org.dive4elements.river.client.shared.model.ExportMode;
import org.dive4elements.river.client.shared.model.Facet;
import org.dive4elements.river.client.shared.model.MapMode;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.OverviewMode;
import org.dive4elements.river.client.shared.model.ReportMode;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.ThemeList;
import org.dive4elements.river.client.shared.model.Settings;
import org.dive4elements.river.client.shared.model.Property;
import org.dive4elements.river.client.shared.model.PropertyGroup;
import org.dive4elements.river.client.shared.model.PropertySetting;
import org.dive4elements.river.client.shared.model.StringProperty;
import org.dive4elements.river.client.shared.model.DoubleProperty;
import org.dive4elements.river.client.shared.model.IntegerProperty;
import org.dive4elements.river.client.shared.model.BooleanProperty;
import org.dive4elements.river.client.shared.model.OutputSettings;

//temporary

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CollectionHelper {

    private static final Logger log =
        LogManager.getLogger(CollectionHelper.class);

    public static final String ERROR_ADD_ARTIFACT = "error_add_artifact";

    public static final String ERROR_REMOVE_ARTIFACT = "error_remove_artifact";

    public static final String XPATH_FACETS = "art:facets/art:facet";

    public static final String XPATH_LOADED_RECOMMENDATIONS =
        "/art:artifact-collection/art:attribute/"
        + "art:loaded-recommendations/art:recommendation";


    public static Document createAttribute(Collection collection) {
        log.debug("CollectionHelper.createAttribute");

        Document doc = XMLUtils.newDocument();

        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element attr = cr.create("attribute");

        doc.appendChild(attr);

        Map<String, OutputMode> tmpOuts = collection.getOutputModes();

        Element outs = createOutputElements(cr, collection, tmpOuts);
        Element recs = createRecommendationsElements(cr, collection);

        if (outs != null) {
            attr.appendChild(outs);
        }

        if (recs != null) {
            attr.appendChild(recs);
        }

        return doc;
    }


    /**
     * Creates a whole block with art:output nodes.
     *
     * @param cr The ElementCreator used to create new elements.
     * @param c  The collection.
     * @param modes The OutputModes that should be included.
     *
     * @return an element with output modes.
     */
    protected static Element createOutputElements(
        ElementCreator          cr,
        Collection              c,
        Map<String, OutputMode> mmodes)
    {
        log.debug("CollectionHelper.createOutputElements");

        java.util.Collection<OutputMode> modes = mmodes != null
            ? mmodes.values()
            : null;

        if (modes == null || modes.size() == 0) {
            log.debug("Collection has no modes: " + c.identifier());
            return null;
        }

        Element outs = cr.create("outputs");

        for (OutputMode mode: modes) {
            Element out = createOutputElement(cr, c, mode);

            if (out != null) {
                outs.appendChild(out);
            }
        }

        return outs;
    }


    /**
     * Create a node art:output that further consist of art:theme nodes.
     *
     * @param cr The ElementCreator used to create new elements.
     * @param c  The collection.
     * @param mode The OutputMode.
     *
     * @return an element that represents an output mode with its themes.
     */
    protected static Element createOutputElement(
        ElementCreator cr,
        Collection     collection,
        OutputMode     mode)
    {
        log.debug("CollectionHelper.createOutputElement");

        Element out = cr.create("output");
        cr.addAttr(out, "name", mode.getName(), false);

        ThemeList themeList = collection.getThemeList(mode.getName());
        List<Theme> themes  = themeList != null ? themeList.getThemes() : null;

        if (themes == null || themes.size() == 0) {
            log.debug("No themes for output mode: " + mode.getName());
            return null;
        }

        for (Theme theme: themes) {
            Element t = createThemeElement(cr, collection, theme);

            if (t != null) {
                out.appendChild(t);
            }
        }

        Document doc = out.getOwnerDocument();

        ElementCreator settingscr = new ElementCreator(doc, "", "");

        Settings settings = collection.getSettings(mode.getName());
        if (settings == null ||
            settings.getCategories().size() == 0)
        {
            log.debug("No settings for output mode: " + mode.getName());
        }
        else {
            Element s = createSettingsElement(settingscr, collection, settings);
            if (s != null) {
                out.appendChild(s);
            }
        }
        log.info(XMLUtils.toString(out));
        return out;
    }


    /**
     * Creates a theme node art:theme that represents a curve in a chart or map.
     *
     * @param cr The ElementCreator used to create new elements.
     * @param collection The collection.
     * @param theme The theme whose attributes should be written to an element.
     *
     * @return an element that contains the informtion of the given theme.
     */
    protected static Element createThemeElement(
        ElementCreator cr,
        Collection     collection,
        Theme          theme)
    {
        if (theme == null) {
            return null;
        }

        Element t = cr.create("facet");

        if (theme instanceof AttributedTheme) {
            AttributedTheme at = (AttributedTheme) theme;
            Set<String>   keys = at.getKeys();

            for (String key: keys) {
                cr.addAttr(t, key, at.getAttr(key), true);
            }
        }
        else {
            cr.addAttr(t, "active", Integer.toString(theme.getActive()), true);
            cr.addAttr(t, "artifact", theme.getArtifact(), true);
            cr.addAttr(t, "facet", theme.getFacet(), true);
            cr.addAttr(t, "pos", Integer.toString(theme.getPosition()), true);
            cr.addAttr(t, "index", Integer.toString(theme.getIndex()), true);
            cr.addAttr(t, "description", theme.getDescription(), true);
            cr.addAttr(t, "visible", Integer.toString(theme.getVisible()),
                true);
        }

        return t;
    }


    /**
     * Creates a whole block with art:loaded-recommendations nodes.
     *
     * @param cr The ElementCreator used to create new elements.
     * @param c  The collection.
     *
     * @return an element with loaded recommendations.
     */
    protected static Element createRecommendationsElements(
        ElementCreator cr,
        Collection     c)
    {
        log.debug("CollectionHelper.createRecommendationsElements");

        List<Recommendation> rs = c.getRecommendations();

        if (rs == null || rs.size() == 0) {
            log.debug("Collection did not load recommendations: " +
                c.identifier());
            return null;
        }

        Element loaded = cr.create("loaded-recommendations");

        for (Recommendation r: rs) {
            Element recommendation = createRecommendationElement(cr, c, r);

            if (recommendation != null) {
                loaded.appendChild(recommendation);
            }
        }

        return loaded;
    }


    /**
     * Create a node art:recommended.
     *
     * @param cr The ElementCreator used to create new elements.
     * @param c  The collection.
     * @param r  The Recommendation.
     *
     * @return an element that represents an output mode with its themes.
     */
    protected static Element createRecommendationElement(
        ElementCreator cr,
        Collection     c,
        Recommendation r)
    {
        log.debug("CollectionHelper.createRecommendationElement");

        Element recommendation = cr.create("recommendation");
        cr.addAttr(recommendation, "factory", r.getFactory(), true);
        cr.addAttr(recommendation, "ids", r.getIDs(), true);

        return recommendation;
    }


    /**
     *
     */
    protected static Element createSettingsElement(
        ElementCreator cr,
        Collection c,
        Settings s)
    {
        log.debug("CollectionHelper.createSettingsElement");
        Element settings = cr.create("settings");

        List<String> categories = s.getCategories();

        for (String category: categories) {
            Element cat =cr.create(category);
            settings.appendChild(cat);
            List<Property> props = s.getSettings(category);
            for (Property p: props) {
                if (p instanceof PropertyGroup) {
                    Element prop = createPropertyGroupElement(cr,
                                                              (PropertyGroup)p);
                    cat.appendChild(prop);
                }
                else if (p instanceof PropertySetting) {
                    Element prop = createPropertyElement (cr,
                                                          (PropertySetting)p);
                    cat.appendChild(prop);
                }
            }
        }
        return settings;
    }


    /**
     *
     */
    protected static Element createPropertyGroupElement(
        ElementCreator cr,
        PropertyGroup pg)
    {
        Element e = cr.create(pg.getName());

        List<Property> list = pg.getProperties();
        for (Property p: list) {
            Element pe = createPropertyElement(cr, (PropertySetting)p);
            e.appendChild(pe);
        }
        return e;
    }


    /**
     *
     */
    protected static Element createPropertyElement(
        ElementCreator cr,
        PropertySetting p)
    {
        Element e = cr.create(p.getName());

        if(p instanceof BooleanProperty) {
            cr.addAttr(e, "type", "boolean", false);
        }
        else if(p instanceof DoubleProperty) {
            cr.addAttr(e, "type", "double", false);
        }
        else if(p instanceof IntegerProperty) {
            cr.addAttr(e, "type", "integer", false);
        }
        else if(p instanceof StringProperty) {
            cr.addAttr(e, "type", "string", false);
        }

        e.setTextContent(p.getValue().toString());
        cr.addAttr(e, "display", p.getAttribute("display"), false);
        return e;
    }


    /**
     * Take the DESCRIBE document of the Collections describe()
     * operation and extracts the information about the collection itself and
     * the collection items.
     *
     * @param description The DESCRIBE document of the Collections describe()
     * operation.
     *
     * @return a Collection with CollectionItems.
     */
    public static Collection parseCollection(Document description) {
        log.debug("CollectionHelper.parseCollection");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(description));
        }

        if (description == null) {
            log.warn("The DESCRIBE of the Collection is null!");
            return null;
        }

        String uuid = XMLUtils.xpathString(
            description,
            "art:artifact-collection/@art:uuid",
            ArtifactNamespaceContext.INSTANCE);

        String ttlStr = XMLUtils.xpathString(
            description,
            "art:artifact-collection/@art:ttl",
            ArtifactNamespaceContext.INSTANCE);

        String name = XMLUtils.xpathString(
            description,
            "art:artifact-collection/@art:name",
            ArtifactNamespaceContext.INSTANCE);

        if (uuid.length() == 0) {
            log.warn("Found an invalid (zero length uuid) Collection!");
            return null;
        }

        if (ttlStr.length() == 0) {
            log.warn("Found an invalid Collection (zero length ttl)!");
            return null;
        }


        long ttl = -1;
        try {
            ttl = Long.valueOf(ttlStr);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        List<Recommendation> recommended = parseRecommendations(description);
        Map<String, CollectionItem> collectionItems =
            new HashMap<String, CollectionItem>();

        name = (name != null && name.length() > 0) ? name : uuid;

        Collection c = new DefaultCollection(uuid, ttl, name, recommended);

        NodeList items = (NodeList) XMLUtils.xpath(
            description,
            "art:artifact-collection/art:artifacts/art:artifact",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (items == null || items.getLength() == 0) {
            log.debug("No collection item found for this collection.");

            return c;
        }

        int size = items.getLength();

        for (int i = 0; i < size; i++) {
            CollectionItem item = parseCollectionItem(
                (Element)items.item(i),
                i == 0);

            if (item != null) {
                c.addItem(item);
                collectionItems.put(item.identifier() ,item);
            }
        }

        Map<String, ThemeList> themeLists = parseThemeLists(
            description, collectionItems);
        c.setThemeLists(themeLists);

        Map<String, Settings> outSettings = parseSettings(description);
        c.setSettings(outSettings);
        log.debug(
            "Found " + c.getItemLength() + " collection items " +
            "for the Collection '" + c.identifier() + "'.");

        return c;
    }


    /**
     * @param collectionItems map to look up collection item mapping a themes
     *                        (artifact) uuid.
     */
    protected static Map<String, ThemeList> parseThemeLists(
        Document desc, Map<String, CollectionItem> collectionItems
    ) {
        log.debug("CollectionHelper.parseThemeLists");

        NodeList lists = (NodeList) XMLUtils.xpath(
            desc,
            "/art:artifact-collection/art:attribute/art:outputs/art:output",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = lists != null ? lists.getLength() : 0;

        Map<String, ThemeList> themeList = new HashMap<String, ThemeList>(num);

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        for (int i = 0; i < num; i++) {
            Element node = (Element)lists.item(i);

            String outName = node.getAttribute("name");

            if (outName.length() > 0) {
                ThemeList list = parseThemeList(node, collectionItems);

                if (list.getThemeCount() > 0) {
                    themeList.put(outName, list);
                }
            }
        }

        return themeList;
    }


    /**
     * @param collectionItems map to look up collection item mapping a themes
     *                        (artifact) uuid.
     */
    protected static ThemeList parseThemeList(
        Element node, Map<String, CollectionItem> collectionItems
    ) {
        log.debug("CollectionHelper.parseThemeList");

        NodeList themes = node.getElementsByTagNameNS(
            ArtifactNamespaceContext.NAMESPACE_URI,
            "facet");

        int num = themes != null ? themes.getLength() : 0;

        List<Theme> themeList = new ArrayList<Theme>(num);

        for (int i = 0; i < num; i++) {
            Theme theme = parseTheme((Element)themes.item(i));
            theme.setCollectionItem(collectionItems.get(theme.getArtifact()));

            if (theme != null) {
                themeList.add(theme);
            }
        }

        return new ThemeList(themeList);
    }


    protected static Theme parseTheme(Element ele) {
        log.debug("CollectionHelper.parseTheme");

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        NamedNodeMap attrMap = ele.getAttributes();
        int          attrNum = attrMap != null ? attrMap.getLength() : 0;

        AttributedTheme t = new AttributedTheme();

        for (int i = 0; i < attrNum; i++) {
            Node attr = attrMap.item(i);

            String prefix = attr.getPrefix();
            String name   = attr.getNodeName().replace(prefix + ":", "");
            String value  = attr.getNodeValue();

            t.addAttr(name, value);
        }

        return t;
    }


    /**
     * Parse Settings elements.
     *
     * @param description The collection description.
     *
     * @return Map containing the settings.
     */
    protected static Map<String, Settings> parseSettings(Document description) {
        log.info("parseSettings");

        NodeList lists = (NodeList) XMLUtils.xpath(
            description,
            "/art:artifact-collection/art:attribute/art:outputs/art:output",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = lists != null ? lists.getLength() : 0;

        Map<String, Settings> list = new HashMap<String, Settings>(num);

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        for (int i = 0; i < num; i++) {
            Element node = (Element)lists.item(i);
            String outName = node.getAttribute("name");
            Settings s = parseSettings(outName, node);
            list.put(outName, s);
        }

        return list;
    }


    /**
     * From a document, parse the settings for an output and create an
     * OutputSettings object.
     */
    protected static Settings parseSettings(String outName, Element node) {
        OutputSettings set = new OutputSettings(outName);

        NodeList elements = node.getElementsByTagName("settings");

        if (elements.getLength() == 0 || elements.getLength() > 1) {
            return set;
        }

        Element settings = (Element)elements.item(0);

        // Get the categories
        NodeList catNodes = settings.getChildNodes();
        for (int i = 0; i < catNodes.getLength(); i++) {
            Element catNode = (Element)catNodes.item(i);

            // The category name
            String category = catNode.getTagName();

            // list of properties or groups (groups have child nodes).
            NodeList list = catNode.getChildNodes();

            // iterate through all properties or groups.
            List<Property> props = new ArrayList<Property> ();
            for (int j = 0; j < list.getLength(); j++) {
                Property p;
                Element e = (Element)list.item(j);
                if (e.hasChildNodes() &&
                    e.getFirstChild().getNodeType() != Node.TEXT_NODE) {
                    p = parseSettingsGroup(e);
                }
                else {
                    p = parseSetting(e);
                }
                props.add(p);
            }
            set.setSettings(category, props);
        }
        return set;
    }


    /**
     *
     */
    protected static Property parseSettingsGroup(Element group) {
        PropertyGroup p = new PropertyGroup();
        p.setName(group.getTagName());

        NodeList list = group.getChildNodes();
        ArrayList<Property> props = new ArrayList<Property>();
        for (int i = 0; i < list.getLength(); i++) {
            props.add(parseSetting((Element)list.item(i)));
        }
        p.setProperties(props);
        return p;
    }


    /**
     * From a property element create a Property object.
     */
    protected static Property parseSetting(Element property){
        NamedNodeMap attrMap = property.getAttributes();
        int          attrNum = attrMap != null ? attrMap.getLength() : 0;

        Node type = attrMap.getNamedItem("type");
        String t = type.getNodeValue();
        PropertySetting ps = new PropertySetting();

        if(t.equals("string")) {
            ps = new StringProperty();
        }
        else if (t.equals("integer")) {
            ps = new IntegerProperty();
        }
        else if (t.equals("double")) {
            ps = new DoubleProperty();
        }
        else if (t.equals("boolean")) {
            ps = new BooleanProperty();
        }
        ps.setName(property.getTagName());
        ps.setValue(property.getTextContent());

        for (int i = 0; i < attrNum; i++) {
            Node attr = attrMap.item(i);

            String name   = attr.getNodeName();
            String value  = attr.getNodeValue();
            if(name.equals("type")) {
                continue;
            }
            ps.setAttribute(name, value);
        }
        return ps;
    }


    /**
     * This method extracts the CollectionItem from <i>node</i> with its output
     * modes. The output modes are parsed using the parseOutputModes() method.
     *
     * @param node A node that contains information about a CollectionItem.
     *
     * @return a CollectionItem.
     */
    protected static CollectionItem parseCollectionItem(
        Element node,
        boolean outs
    ) {
        log.debug("CollectionHelper.parseCollectionItem");

        if (node == null) {
            log.debug("The node for parsing CollectionItem is null!");
            return null;
        }

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        String uuid = node.getAttributeNS(uri, "uuid");
        String hash = node.getAttributeNS(uri, "hash");

        if (uuid == null || uuid.length() == 0) {
            log.warn("Found an invalid CollectionItem!");
            return null;
        }

        List<OutputMode> modes = new ArrayList<OutputMode>();

        if (outs) {
            NodeList outputmodes = node.getElementsByTagNameNS(
                uri, "outputmodes");

            if (outputmodes.getLength() < 1) {
                return null;
            }

            Element om = (Element)outputmodes.item(0);

            modes = parseOutputModes(om);
        }

        HashMap<String, String> dataItems = new HashMap<String, String>();

        NodeList dataItemNodes = node.getElementsByTagNameNS(
            uri, "data-items");

        Element di = (Element)dataItemNodes.item(0);
        dataItems = parseDataItems(di);

        return new DefaultCollectionItem(uuid, hash, modes, dataItems);
    }


    /**
     * This method extracts the OutputModes available for a specific
     * CollectionItem and returns them as list.
     *
     * @param node The root node of the outputmodes list.
     *
     * @return a list of OutputModes.
     */
    protected static List<OutputMode> parseOutputModes(Element node) {
        log.debug("CollectionHelper.parseOutputModes");

        if (node == null) {
            log.debug("The node for parsing OutputModes is null!");
            return null;
        }

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        NodeList list = node.getElementsByTagNameNS(uri, "output");

        int size = list.getLength();

        if (size == 0) {
            log.debug("No outputmode nodes found!");
            return null;
        }

        List<OutputMode> modes = new ArrayList<OutputMode>(size);

        for (int i = 0; i < size; i++) {
            Element tmp = (Element)list.item(i);

            String name = tmp.getAttributeNS(uri, "name");
            String desc = tmp.getAttributeNS(uri, "description");
            String mime = tmp.getAttributeNS(uri, "mime-type");
            String type = tmp.getAttributeNS(uri, "type");

            if (name.length() == 0) {
                log.warn("Found an invalid output mode.");
                continue;
            }

            OutputMode outmode = null;
            List<Facet> fs     = extractFacets(tmp);

            if (type.equals("export")) {
                outmode = new ExportMode(name, desc, mime, fs);
            }
            else if (type.equals("report")) {
                outmode = new ReportMode(name, desc, mime, fs);
            }
            else if (type.equals("chart")){
                outmode = new ChartMode(name, desc, mime, fs, type);
            }
            else if (type.equals("map")){
                outmode = new MapMode(name, desc, mime, fs);
            }
            else if (type.equals("overview")) {
                outmode = new OverviewMode(name, desc, mime, fs, type);
            }
            else {
                log.warn("Broken Output mode without type found.");
                continue;
            }

            modes.add(outmode);
        }

        return modes;
    }


    /**
     * Create a Key/Value map for data nodes of artifact/collectionitem.
     */
    protected static HashMap<String, String> parseDataItems(Element node) {
        log.debug("CollectionHelper.parseDataItems");

        if (node == null) {
            log.debug("The node for parsing DataItems is null!");
            return null;
        }

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        NodeList list = node.getElementsByTagNameNS(uri, "data");

        int size = list.getLength();

        if (size == 0) {
            log.debug("No static data-item nodes found!");
        }

        HashMap<String, String> data = new HashMap<String, String>(size*2);

        for (int i = 0; i < size; i++) {
            Element tmp = (Element)list.item(i);

            String key = tmp.getAttributeNS(uri, "name");

            if (key.length() == 0) {
                log.warn("Found an invalid data item mode.");
                continue;
            }

            // XXX We are restricted on 1/1 key/values in the data map here.
            NodeList valueNodes = tmp.getElementsByTagName("art:item");

            Element item = (Element) valueNodes.item(0);
            String value = item.getAttributeNS(uri, "value");
            log.debug("Found a data item " + key + " : " + value);

            data.put(key, value);
        }


        // Dynamic data.
        list = node.getElementsByTagNameNS(uri, "select");

        size = list.getLength();

        if (size == 0) {
            log.debug("No dynamic data-item nodes found!");
        }

        for (int i = 0; i < size; i++) {
            Element tmp = (Element)list.item(i);

            String key = tmp.getAttributeNS(uri, "name");

            if (key.length() == 0) {
                log.warn("Found an invalid data item node (missing key).");
                continue;
            }

            String value = tmp.getAttributeNS(uri, "defaultValue");

            if (value.length() == 0) {
                log.warn("Found an invalid data item node (missing value).");
                continue;
            }

            log.debug("Found a (dyn) data item " + key + " : " + value);

            data.put(key, value);
        }

        return data;
    }

    protected static List<Facet> extractFacets(Element outmode) {
        log.debug("CollectionHelper - extractFacets()");

        NodeList facetList = (NodeList) XMLUtils.xpath(
            outmode,
            XPATH_FACETS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = facetList != null ? facetList.getLength() : 0;

        List<Facet> facets = new ArrayList<Facet>(num);

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        for (int i = 0; i < num; i++) {
            Element facetEl = (Element) facetList.item(i);

            String name  = facetEl.getAttributeNS(uri, "name");
            String desc  = facetEl.getAttributeNS(uri, "description");
            String index = facetEl.getAttributeNS(uri, "index");

            if (name != null && name.length() > 0 && index != null) {
                facets.add(new DefaultFacet(name, Integer.valueOf(index),desc));
            }
        }

        return facets;
    }


    public static List<Recommendation> parseRecommendations(Document doc) {
        log.debug("CollectionHelper.parseRecommendations");

        NodeList list = (NodeList) XMLUtils.xpath(
            doc,
            XPATH_LOADED_RECOMMENDATIONS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = list != null ? list.getLength() : 0;

        List<Recommendation> recs = new ArrayList<Recommendation>(num);

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        for (int i = 0; i < num; i++) {
            Element rec = (Element) list.item(i);

            String factory = rec.getAttributeNS(uri, "factory");
            String dbids   = rec.getAttributeNS(uri, "ids");

            if (factory != null && factory.length() > 0) {
                recs.add(new Recommendation(factory, dbids));
            }
        }

        return recs;
    }


    /**
     * Add an artifact to a collection.
     * @param collection Collection to add artifact to.
     * @param artifact   Artifact to add to collection
     */
    public static Collection addArtifact(
        Collection collection,
        Artifact   artifact,
        String     url,
        String     locale)
    throws ServerException
    {
        log.debug("CollectionHelper.addArtifact");

        if (collection == null) {
            log.warn("The given Collection is null!");
            return null;
        }

        Document add = ClientProtocolUtils.newAddArtifactDocument(
            artifact.getUuid(), null);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            log.debug("Do HTTP request now.");

            Document response = (Document) client.doCollectionAction(
                add, collection.identifier(), new DocumentResponseHandler());

            log.debug(
                "Finished HTTP request successfully. Parse Collection now.");

            Collection c = CollectionHelper.parseCollection(response);

            if (c == null) {
                throw new ServerException(ERROR_ADD_ARTIFACT);
            }

            return c;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }
        catch (Exception e) {
            log.error(e, e);
        }

        throw new ServerException(ERROR_ADD_ARTIFACT);
    }


    /**
     * Remove an artifact from a collection.
     * @param collection Collection to remove artifact to.
     * @param artifact   Artifact to add to collection
     */
    public static Collection removeArtifact(
        Collection collection,
        String     artifactId,
        String     url,
        String     locale)
    throws ServerException
    {
        log.debug("CollectionHelper.removeArtifact");

        if (collection == null) {
            log.warn("The given Collection is null!");
            return null;
        }

        Document remove = ClientProtocolUtils.newRemoveArtifactDocument(
            artifactId);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            log.debug("Do HTTP request now.");

            Document response = (Document) client.doCollectionAction(
                remove, collection.identifier(), new DocumentResponseHandler());

            log.debug(
                "Finished HTTP request successfully. Parse Collection now.");
            log.debug(XMLUtils.toString(response));

            Collection c = CollectionHelper.parseCollection(response);

            if (c == null) {
                throw new ServerException(ERROR_REMOVE_ARTIFACT);
            }

            return c;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }
        catch (Exception e) {
            log.error(e, e);
        }
        throw new ServerException(ERROR_REMOVE_ARTIFACT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
