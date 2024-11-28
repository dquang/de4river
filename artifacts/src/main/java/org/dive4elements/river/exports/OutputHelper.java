/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.ContextInjector;

import org.dive4elements.artifactdatabase.Backend;
import org.dive4elements.artifactdatabase.Backend.PersistentArtifact;
import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.model.ManagedDomFacet;
import org.dive4elements.river.artifacts.model.ManagedFacet;
import org.dive4elements.river.themes.Theme;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.themes.ThemeFactory;

public class OutputHelper {
    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(OutputHelper.class);

    protected String identifier;
    private  D4EArtifact masterArtifact;

    public OutputHelper(String identifier, D4EArtifact masterArtifact) {
        this.identifier = identifier;
        this.masterArtifact = masterArtifact;
    }

    private static List<ContextInjector> getContextInjectors(
        CallContext context,
        String out
    ) {
        RiverContext flysContext = context instanceof RiverContext
            ? (RiverContext)context
            : (RiverContext)context.globalContext();

        GeneratorLookup generators =
            (GeneratorLookup)flysContext.get(RiverContext.OUTGENERATORS_KEY);

        if (generators == null) {
            return null;
        }

        GeneratorLookup.Item item = generators.getGenerator(out);
        return item != null
            ? item.getContextInjectors()
            : null;
    }
    /**
     * Creates a concrete output.
     *
     * @param generator The OutGenerator that creates the output.
     * @param outputName The name of the requested output.
     * @param attributes The collection's attributes for this concrete output
     * type.
     * @param context The context object.
     */
    public void doOut(
        OutGenerator generator,
        String       outName,
        String       facet,
        Document     attributes,
        CallContext  context,
        Document     request
    ) throws IOException {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("OutputHelper.doOut: " + outName);
        }

        ThemeList themeList = new ThemeList(attributes);

        ThemeDocument themeDoc = new ThemeDocument(attributes);

        List<ArtifactAndFacet> dataProviders =
            doBlackboardPass(themeList, context, outName);

        List<ContextInjector> cis = getContextInjectors(context, outName);

        if (cis != null) {
            for (ContextInjector ci: cis) {
                ci.injectContext(context, masterArtifact, request);
            }
        }

        try {
            for (int i = 0, T = themeList.size(); i < T; i++) {
                ManagedFacet theme = themeList.get(i);

                if (theme == null) {
                    log.debug("Theme is empty - no output is generated.");
                    continue;
                }

                String art = theme.getArtifact();
                String facetName = theme.getName();

                if (debug) {
                    log.debug("Do output for...");
                    log.debug("... artifact: " + art);
                    log.debug("... facet: " + facetName);
                }

                if (outName.equals("export") && !facetName.equals(facet)) {
                    continue;
                }

                // Skip invisible themes.
                if (theme.getVisible() == 0) {
                    continue;
                }

                generator.doOut(
                    dataProviders.get(i),
                    getFacetThemeFromAttribute(
                        art,
                        outName,
                        facetName,
                        theme.getDescription(),
                        theme.getIndex(),
                        context),
                    theme.getActive() == 1);
            }
        }
        catch (ArtifactDatabaseException ade) {
            log.error(ade, ade);
        }
    }
    /**
     * Returns the attribute that belongs to an artifact and facet stored in
     * this collection.
     *
     * @param uuid The Artifact's uuid.
     * @param outname The name of the requested output.
     * @param facet The name of the requested facet.
     * @param context The CallContext.
     *
     * @return an attribute in form of a document.
     */
    protected ThemeDocument getFacetThemeFromAttribute(
        String      uuid,
        String      outName,
        String      facet,
        String      pattern,
        int         index,
        CallContext context)
    throws    ArtifactDatabaseException
    {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "OutputHelper.getFacetThemeFromAttribute(facet="
                + facet + ", index=" + index + ")");
        }

        ArtifactDatabase db = context.getDatabase();
        CallMeta       meta = context.getMeta();

        Document attr = db.getCollectionItemAttribute(identifier, uuid, meta);

        if (attr == null) {
            attr = initItemAttribute(
                uuid, facet, pattern, index, outName, context);

            if (attr == null) {
                return null;
            }
        }

        if (debug) {
            log.debug("Search attribute of collection item: " + uuid);
        }

        Node tmp = (Node) XMLUtils.xpath(
            attr,
            "/art:attribute",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (tmp == null) {
            log.warn("No attribute found. Operation failed.");
            return null;
        }

        if (debug) {
            log.debug("Search theme for facet '" + facet + "' in attribute.");
        }

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("facet", facet);
        vars.put("index", String.valueOf(index));

        Node theme = (Node) XMLUtils.xpath(
            tmp,
            "art:themes/theme[@facet=$facet and @index=$index]",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE,
            vars);

        if (theme == null) {
            log.warn("Could not find the theme in attribute of: "
                + facet + " " + uuid);

            Theme t = getThemeForFacet(
                uuid, facet, pattern, index, outName, context);

            if (t == null) {
                log.warn("No theme found for facet: " + facet);
                return null;
            }

            addThemeToAttribute(uuid, attr, t, context);
            theme = t.toXML().getFirstChild();
        }

        Document doc = XMLUtils.newDocument();
        doc.appendChild(doc.importNode(theme, true));

        return new ThemeDocument(doc);
    }
    /**
     * Adds the theme of a facet to a CollectionItem's attribute.
     *
     * @param uuid The uuid of the artifact.
     * @param attr The current attribute of an artifact.
     * @param t The theme to add.
     * @param context The CallContext.
     */
    protected void addThemeToAttribute(
        String      uuid,
        Document    attr,
        Theme       t,
        CallContext context)
    {
        log.debug("OutputHelper.addThemeToAttribute: " + uuid);

        if (t == null) {
            log.warn("Theme is empty - cancel adding it to attribute!");
            return;
        }

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            attr,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Node tmp = (Node) XMLUtils.xpath(
            attr,
            "/art:attribute",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (tmp == null) {
            tmp = ec.create("attribute");
            attr.appendChild(tmp);
        }

        Node themes = (Node) XMLUtils.xpath(
            tmp,
            "art:themes",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (themes == null) {
            themes = ec.create("themes");
            tmp.appendChild(themes);
        }

        themes.appendChild(attr.importNode(t.toXML().getFirstChild(), true));

        try {
            setCollectionItemAttribute(uuid, attr, context);
        }
        catch (ArtifactDatabaseException e) {
            // do nothing
            log.warn("Cannot set attribute of item: " + uuid);
        }
    }

    /**
     * Sets the attribute of a CollectionItem specified by <i>uuid</i> to a new
     * value <i>attr</i>.
     *
     * @param uuid The uuid of the CollectionItem.
     * @param attr The new attribute for the CollectionItem.
     * @param context The CallContext.
     */
    public void setCollectionItemAttribute(
        String      uuid,
        Document    attr,
        CallContext context)
    throws ArtifactDatabaseException
    {
        Document doc = ClientProtocolUtils.newSetItemAttributeDocument(
            uuid,
            attr);

        if (doc == null) {
            log.warn("Cannot set item attribute: No attribute found.");
            return;
        }

        ArtifactDatabase db = context.getDatabase();
        CallMeta       meta = context.getMeta();

        db.setCollectionItemAttribute(identifier, uuid, doc, meta);
    }


    /**
     * Show blackboard (context) to each facet and create a list of
     * ArtifactAndFacets on the fly (with the same ordering as the passed
     * ThemeList).
     * @param themeList ThemeList to create a ArtifactAndFacetList along.
     * @param context   The "Blackboard".
     */
    protected List<ArtifactAndFacet> doBlackboardPass(
        ThemeList themeList, CallContext context, String outname
    ) {
        ArrayList<ArtifactAndFacet> dataProviders =
            new ArrayList<ArtifactAndFacet>();

        try {
            // Collect all ArtifactAndFacets for blackboard pass.
            for (int i = 0; i < themeList.size(); i++) {
                log.debug("BLackboard pass for: " + outname);
                ManagedFacet theme = themeList.get(i);
                if (theme == null) {
                    log.warn("A ManagedFacet in ThemeList is null.");
                    themeList.remove(i);
                    i--;
                    continue;
                }

                String uuid        = theme.getArtifact();
                Artifact artifact  = getArtifact(uuid, context);
                D4EArtifact flys  = (D4EArtifact) artifact;
                Facet face = flys.getNativeFacet(theme, outname);
                log.debug("Looking for Native Facet for theme: "
                    + theme + " and out: "
                    + outname + " in artifact: " + uuid
                    + face == null ? " Found. " : " Not Found. ");
                if (face == null) {
                    log.warn("Theme " + theme.getName()
                        + " for " + outname
                        + " has no facets!. Removing theme.");
                    themeList.remove(i);
                    i--;
                    continue;
                }

                ArtifactAndFacet artifactAndFacet = new ArtifactAndFacet(
                    artifact,
                    face);

                // XXX HELP ME PLEASE
                artifactAndFacet.setFacetDescription(theme.getDescription());

                // Show blackboard to facet.
                artifactAndFacet.register(context);

                // Add to themes.
                dataProviders.add(i, artifactAndFacet);
            }
        }
        catch (ArtifactDatabaseException ade) {
            log.error("ArtifactDatabaseException!", ade);
        }

        return dataProviders;
    }
    /**
     * Returns a concrete Artifact of this collection specified by its uuid.
     *
     * @param uuid The Artifact's uuid.
     * @param context The CallContext.
     *
     * @return an Artifact.
     */
    protected Artifact getArtifact(String uuid, CallContext context)
    throws    ArtifactDatabaseException
    {
        log.debug("OutputHelper.getArtifact");

        Backend backend               = Backend.getInstance();
        PersistentArtifact persistent = backend.getArtifact(uuid);

        return persistent != null ? persistent.getArtifact() : null;
    }

    /**
     * Initializes the attribute of an collection item with the theme of a
     * specific facet.
     *
     * @param uuid The uuid of an artifact.
     * @param facet The name of a facet.
     * @param context The CallContext.
     *
     * @param the new attribute.
     */
    protected Document initItemAttribute(
        String      uuid,
        String      facet,
        String      pattern,
        int         index,
        String      outName,
        CallContext context)
    {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("OutputHelper.initItemAttribute");
        }

        Theme t = getThemeForFacet(
            uuid, facet, pattern, index, outName, context);

        if (t == null) {
            log.info("Could not find theme for facet. Cancel initialization.");
            return null;
        }

        Document attr = XMLUtils.newDocument();
        addThemeToAttribute(uuid, attr, t, context);

        if (debug) {
            log.debug("initItemAttribute for facet " + facet + ": "
                + XMLUtils.toString(attr));
        }

        return attr;
    }

        /**
     * Returns the theme of a specific facet.
     *
     * @param uuid The uuid of an artifact.
     * @param facet The name of the facet.
     * @param context The CallContext object.
     *
     * @return the desired theme.
     */
    protected Theme getThemeForFacet(
        String uuid,
        String facet,
        String pattern,
        int    index,
        String outName,
        CallContext context)
    {
        log.info("OutputHelper.getThemeForFacet: " + facet);

        RiverContext flysContext = context instanceof RiverContext
            ? (RiverContext) context
            : (RiverContext) context.globalContext();

        // Push artifact in flysContext.
        ArtifactDatabase db = context.getDatabase();
        try {
            D4EArtifact artifact = (D4EArtifact) db.getRawArtifact(uuid);
            log.debug("Got raw artifact");
            flysContext.put(RiverContext.ARTIFACT_KEY, artifact);
        }
        catch (ArtifactDatabaseException dbe) {
            log.error("Exception caught when trying to get art.", dbe);
        }

        Theme t = ThemeFactory.getTheme(
                      flysContext,
                      facet,
                      pattern,
                      outName,
                      "default");

        if (t != null) {
            log.debug("found theme for facet '" + facet + "'");
            t.setFacet(facet);
            t.setIndex(index);
        }
        else {
            log.warn("unable to find theme for facet '" + facet + "'");
        }

        return t;
    }

    /**
     * Inner class to structure/order the themes of a chart.
     */
    private static class ThemeList {
        private Logger log = LogManager.getLogger(ThemeList.class);
        protected List<ManagedFacet> themes;

        public ThemeList(Document output) {
            themes = new ArrayList<ManagedFacet>();
            parse(output);
        }

        protected void parse(Document output) {
            NodeList themeList = (NodeList) XMLUtils.xpath(
                output,
                "art:output/art:facet",
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

            int num = themeList != null ? themeList.getLength() : 0;

            log.debug("Output has " +  num + " elements.");

            if (num == 0) {
                return;
            }

            for (int i = 0; i < num; i++) {
                Element theme = (Element) themeList.item(i);

                ManagedDomFacet facet = new ManagedDomFacet(theme);
                themes.add(facet);
            }

            Collections.sort(themes);
        }

        public ManagedFacet get(int idx) {
            return themes.get(idx);
        }

        public void remove(int idx) {
            themes.remove(idx);
        }

        public int size() {
            return themes.size();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
