/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.collections;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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

import org.dive4elements.artifactdatabase.Backend;
import org.dive4elements.artifactdatabase.Backend.PersistentArtifact;
import org.dive4elements.artifactdatabase.DefaultArtifactCollection;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.Settings;
import org.dive4elements.artifactdatabase.state.StateEngine;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.exports.OutGenerator;
import org.dive4elements.river.exports.OutputHelper;
import org.dive4elements.river.utils.RiverUtils;

/**
 * Collection of artifacts, can do outs, describe.
 * Lots of stuff done in AttributeParser and AttributeWriter.
 * Critical out and facet merging.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class D4EArtifactCollection extends DefaultArtifactCollection {
    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(D4EArtifactCollection.class);

    /** Constant XPath that points to the outputmodes of an artifact. */
    public static final String XPATH_ARTIFACT_OUTPUTMODES =
        "/art:result/art:outputmodes";

    public static final String XPATH_ARTIFACT_STATE_DATA =
        "/art:result/art:ui/art:static/art:state/art:data";

    public static final String XPATH_COLLECTION_ITEMS =
        "/art:result/art:artifact-collection/art:collection-item";

    public static final String XPATH_OUT_NAME = "/art:action/@art:name";

    public static final String XPATH_OUT_TYPE = "/art:action/@art:type";

    /** Xpath to master artifacts uuid. */
    public static final String XPATH_MASTER_UUID =
        "/art:artifact-collection/art:artifact/@art:uuid";

    public static final String XPATH_LOADED_RECOMMENDATIONS =
        "/art:attribute/art:loaded-recommendations";

    private CallContext context;

    private ArtifactDatabase db;

    protected CallContext getContext() {
        return this.context;
    }

    protected ArtifactDatabase getArtifactDB() {
        return this.db;
    }

    protected void setContext(CallContext context) {
        this.context = context;
        this.db = context.getDatabase();
    }

    /**
     * Create and return description Document for this collection.
     */
    @Override
    public Document describe(CallContext context) {
        log.debug("D4EArtifactCollection.describe: " + identifier);

        setContext(context);

        CollectionDescriptionHelper helper = new CollectionDescriptionHelper(
            getName(), identifier(), getCreationTime(), getTTL(),
            context);


        Document        oldAttrs = getAttribute();
        AttributeParser parser   = new AttributeParser(oldAttrs);

        try {
            String[] aUUIDs  = getArtifactUUIDs();

            oldAttrs = removeAttributes(oldAttrs);
            parser   = new AttributeParser(oldAttrs);

            CollectionAttribute newAttr = mergeAttributes(parser, aUUIDs);

            if (checkOutputSettings(newAttr)) {
                saveCollectionAttribute(newAttr);
            }

            helper.setAttribute(newAttr);

            if (aUUIDs != null) {
                for (String uuid: aUUIDs) {
                    helper.addArtifact(uuid);
                }
            }
        }
        catch (ArtifactDatabaseException ade) {
            log.error("Error while merging attribute documents.", ade);

            helper.setAttribute(parser.getCollectionAttribute());
        }

        return helper.toXML();
    }


    /**
     * Merge the current art:outputs nodes with the the outputs provided by the
     * artifacts in the Collection.
     *
     * @param uuids Artifact uuids.
     */
    protected CollectionAttribute mergeAttributes(
        AttributeParser  oldParser,
        String[]         uuids
    ) {
        CollectionAttribute cAttribute =
            buildOutAttributes(oldParser, uuids);

        if (cAttribute == null) {
            log.warn("mergeAttributes: cAttribute == null");
            return null;
        }

        cAttribute.setLoadedRecommendations(
            getLoadedRecommendations(oldParser.getAttributeDocument()));

        saveCollectionAttribute(cAttribute);

        return cAttribute;
    }


    /**
     * Remove those output-elements which have a name that does
     * not appear in master artifacts out-list.
     * @param attr[in,out] Document to clean and return.
     * @return param attr.
     */
    protected Document removeAttributes(Document attrs) {
        Node outs = (Node) XMLUtils.xpath(
            attrs,
            "/art:attribute/art:outputs",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        NodeList nodes = (NodeList) XMLUtils.xpath(
            attrs,
            "/art:attribute/art:outputs/art:output",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element)nodes.item(i);
                if(!outputExists(e.getAttribute("name"))) {
                    outs.removeChild(e);
                }
            }
        }
        return attrs;
    }


    /**
     * True if current MasterArtifact has given output.
     * @param name Name of the output of interest.
     * @param context current context
     * @return true if current master artifact has given output.
     */
    protected boolean outputExists(String name) {
        D4EArtifact master = getMasterArtifact();
        List<Output> outList = master.getOutputs(getContext());

        for (Output o : outList) {
            if (name.equals(o.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param db The ArtifactDatabase which is required to save the attribute
     * into.
     * @param attribute The CollectionAttribute that should be stored in the
     * database.
     *
     * @return true, if the transaction was successful, otherwise false.
     */
    protected boolean saveCollectionAttribute(
        CollectionAttribute attribute
    ) {
        log.info("Save new CollectionAttribute into database.");

        Document doc = attribute.toXML();

        try {
            // Save the merged document into database.
            getArtifactDB().setCollectionAttribute(
                identifier(), getContext().getMeta(), doc);

            log.info("Saving CollectionAttribute was successful.");

            return true;
        }
        catch (ArtifactDatabaseException adb) {
            log.error(adb, adb);
        }

        return false;
    }


    /**
     * Merge the recommendations which have already been loaded from the old
     * attribute document into the new attribute document. This is necessary,
     * because mergeAttributes() only merges the art:outputs nodes - all
     * other nodes are skipped.
     */
    protected Node getLoadedRecommendations(Document oldAttrs) {
        Element loadedRecoms = (Element) XMLUtils.xpath(
            oldAttrs,
            XPATH_LOADED_RECOMMENDATIONS,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        return loadedRecoms;
    }


    /**
     * Evaluates the Output settings. If an Output has no Settings set, the
     * relevant OutGenerator is used to initialize a default Settings object.
     *
     * @param attribute The CollectionAttribute.
     * @param cc The CallContext.
     *
     * @return true, if the CollectionAttribute was modified, otherwise false.
     */
    protected boolean checkOutputSettings(
        CollectionAttribute attribute
    ) {
        boolean modified = false;

        Map<String, Output> outputMap = attribute != null
            ? attribute.getOutputs()
            : null;

        if (outputMap == null || outputMap.isEmpty()) {
            log.debug("No Output Settings check necessary.");
            return modified;
        }


        for (Map.Entry<String, Output> entry: outputMap.entrySet()) {
            String outName = entry.getKey();
            Output output  = entry.getValue();

            Settings settings = output.getSettings();

            if (settings == null) {
                log.debug("No Settings set for Output '" + outName + "'.");
                output.setSettings(
                    createInitialOutputSettings(attribute, outName));

                modified = true;
            }
        }

        return modified;
    }


    /**
     * This method uses the the OutGenerator for the specified Output
     * <i>out</i> to create an initial Settings object.
     *
     * @param cc The CallContext object.
     * @param attr The CollectionAttribute.
     * @param out The name of the output.
     *
     * @return a default Settings object for the specified Output.
     */
    protected Settings createInitialOutputSettings(
        CollectionAttribute attr,
        String              out
    ) {
        OutGenerator outGen = RiverContext.getOutGenerator(getContext(), out);

        if (outGen == null) {
            return null;
        }

        // XXX NOTE: outGen is not able to process its generate() operation,
        // because it has no OutputStream set!
        Document dummy = XMLUtils.newDocument();
        outGen.init(out, dummy, null, getContext());
        D4EArtifact master = getMasterArtifact();
        prepareMasterArtifact(master, outGen);

        try {
            Document outAttr = getAttribute(attr, out);
            OutputHelper helper = new OutputHelper(identifier(), master);
            helper.doOut(outGen, out, out, outAttr, getContext(), dummy);
        }
        catch (ArtifactDatabaseException adbe) {
            log.error(adbe, adbe);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }

        return outGen.getSettings();
    }


    @Override
    public void out(
        String       type,
        Document     format,
        OutputStream out,
        CallContext  context)
    throws IOException
    {
        boolean debug = log.isDebugEnabled();

        setContext(context);

        long reqBegin = System.currentTimeMillis();

        if (debug) {
            log.debug(XMLUtils.toString(format));
            log.debug("D4EArtifactCollection.out");
        }

        String name = XMLUtils.xpathString(
            format, XPATH_OUT_NAME, ArtifactNamespaceContext.INSTANCE);

        String subtype = XMLUtils.xpathString(
            format, XPATH_OUT_TYPE, ArtifactNamespaceContext.INSTANCE);

        if (debug) {
            log.debug("-> Output name = " + name);
            log.debug("-> Output type = " + type);
            log.debug("-> Output subtype = " + subtype);
        }

        // If type contains 'chartinfo' use a generator that
        // just allow access to width, height etc.

        String key = type != null
            && !type.isEmpty()
            && type.indexOf("chartinfo") > 0
            ? type
            : name;

        OutGenerator generator = RiverContext.getOutGenerator(context, key);

        if (generator == null) {
            // TODO Throw an exception.

            return;
        }

        Document        oldAttrs  = getAttribute();
        AttributeParser parser    = new AttributeParser(oldAttrs);
        CollectionAttribute cAttr = parser.getCollectionAttribute();

        Output output = cAttr.getOutput(name);
        Settings settings = null;
        if (output != null) {
            settings = output.getSettings();

            if (debug) {
                List<Facet> facets = output.getFacets();
                for(Facet facet: facets) {
                    log.debug("  -- Facet " + facet.getName());
                }
            }
        }

        generator.init(key, format, out, context);
        generator.setSettings(settings);
        generator.setCollection(this);

        D4EArtifact master = getMasterArtifact();
        prepareMasterArtifact(master, generator);

        try {
            Document attr = getAttribute(cAttr, name);
            OutputHelper helper = new OutputHelper(identifier(), master);
            helper.doOut(generator, name, subtype, attr, context, format);
            generator.generate();
        }
        catch (ArtifactDatabaseException adbe) {
            log.error(adbe, adbe);
        }

        if (debug) {
            long duration = System.currentTimeMillis() -reqBegin;
            log.info("Processing out(" + name + ") took " + duration + " ms.");
        }
    }


    /**
     * Sets the master Artifact at the given <i>generator</i>.
     *
     * @param generator The generator that gets a master Artifact.
     */
    protected void prepareMasterArtifact(
        D4EArtifact master,
        OutGenerator generator
    ) {
        // Get master artifact.
        if (master != null) {
            log.debug("Set master Artifact to uuid: " + master.identifier());
            generator.setMasterArtifact(master);
        }
        else {
            log.warn("Could not set master artifact.");
        }
    }


    /**
     * @return masterartifact or null if exception/not found.
     */
    protected D4EArtifact getMasterArtifact()
    {
        try {
            ArtifactDatabase db = getArtifactDB();
            CallMeta callMeta   = getContext().getMeta();
            Document document   = db.getCollectionsMasterArtifact(
                identifier(), callMeta);

            String masterUUID   = XMLUtils.xpathString(
                document,
                XPATH_MASTER_UUID,
                ArtifactNamespaceContext.INSTANCE);
            D4EArtifact masterArtifact =
                (D4EArtifact) getArtifact(masterUUID);
            return masterArtifact;
        }
        catch (ArtifactDatabaseException ade) {
            log.error(ade, ade);
        }
        return null;
    }


    /**
     * Return merged output document.
     * @param uuids List of artifact uuids.
     */
    protected CollectionAttribute buildOutAttributes(
        AttributeParser  aParser,
        String[]         uuids)
    {
        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        if (engine == null) {
            log.error("buildOutAttributes: engine == null");
            return null;
        }

        D4EArtifact masterArtifact = getMasterArtifact();

        if (masterArtifact == null) {
            log.debug("buildOutAttributes: masterArtifact == null");
            return null;
        }

        OutputParser oParser = new OutputParser(
            getArtifactDB(),
            getContext());

        if (uuids != null) {
            for (String uuid: uuids) {
                try {
                    oParser.parse(uuid);
                }
                catch (ArtifactDatabaseException ade) {
                    log.warn(ade, ade);
                }
            }
        }

        aParser.parse();

        AttributeWriter aWriter = new AttributeWriter(
            getArtifactDB(),
            aParser.getCollectionAttribute(),
            aParser.getOuts(),
            aParser.getFacets(),
            oParser.getOuts(),
            oParser.getFacets(),
            engine.getCompatibleFacets(masterArtifact.getStateHistoryIds())
            );
        return aWriter.write();
    }


    /**
     * Returns the "attribute" (part of description document) for a specific
     * output type.
     *
     * @param context The CallContext object.
     * @param cAttr The CollectionAttribute.
     * @param output The name of the desired output type.
     *
     * @return the attribute for the desired output type.
     */
    protected Document getAttribute(
        CollectionAttribute cAttr,
        String              output)
    throws    ArtifactDatabaseException
    {
        Document attr = cAttr.toXML();

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("output", output);

        Node out = (Node) XMLUtils.xpath(
            attr,
            "art:attribute/art:outputs/art:output[@name=$output]",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE,
            vars);


        if (out != null) {
            Document o = XMLUtils.newDocument();

            o.appendChild(o.importNode(out, true));

            return o;
        }

        return null;
    }


    /**
     * This method returns the list of artifact UUIDs that this collections
     * contains.
     *
     * @param context CallContext that is necessary to get information about
     * the ArtifactDatabase.
     *
     * @return a list of uuids.
     */
    protected String[] getArtifactUUIDs()
    throws    ArtifactDatabaseException
    {
        log.debug("D4EArtifactCollection.getArtifactUUIDs");

        ArtifactDatabase db = getArtifactDB();
        CallMeta meta       = getContext().getMeta();

        Document itemList = db.listCollectionArtifacts(identifier(), meta);
        NodeList items    = (NodeList) XMLUtils.xpath(
            itemList,
            XPATH_COLLECTION_ITEMS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (items == null || items.getLength() == 0) {
            log.debug("No artifacts found in this collection.");
            return null;
        }

        int num = items.getLength();

        List<String> uuids = new ArrayList<String>(num);

        for (int i = 0; i < num; i++) {
            String uuid = XMLUtils.xpathString(
                items.item(i),
                "@art:uuid",
                ArtifactNamespaceContext.INSTANCE);

            if (uuid != null && uuid.trim().length() != 0) {
                uuids.add(uuid);
            }
        }

        return uuids.toArray(new String[uuids.size()]);
    }


    /**
     * Returns a concrete Artifact of this collection specified by its uuid.
     *
     * @param uuid The Artifact's uuid.
     * @param context The CallContext.
     *
     * @return an Artifact.
     */
    protected Artifact getArtifact(String uuid)
    throws    ArtifactDatabaseException
    {
        log.debug("D4EArtifactCollection.getArtifact");

        Backend backend               = Backend.getInstance();
        PersistentArtifact persistent = backend.getArtifact(uuid);

        return persistent != null ? persistent.getArtifact() : null;
    }

    /**
     * Returns artifacts with name name.
     *
     * @param name The Artifact name to search
     * @param context The CallContext
     *
     * @return a list of artifacts matching this name.
     */
    public List<Artifact> getArtifactsByName(String name, CallContext context)
    {
        setContext(context);
        return getArtifactsByName(name);
    }


    /**
     * Returns artifacts with name name.
     *
     * @param name The Artifact name to search
     *
     * @return a list of artifacts matching this name.
     */
    protected List<Artifact> getArtifactsByName(String name)
    {
        log.debug("Searching for Artifacts: " + name);
        List<Artifact> ret =  new ArrayList<Artifact>();
        try {
            for (String uuid: getArtifactUUIDs()) {
                D4EArtifact subArt = (D4EArtifact) getArtifact(uuid);
                if (
                    subArt.getName() != null && subArt.getName().equals(name)
                ) {
                    ret.add(subArt);
                }
            }
        } catch (ArtifactDatabaseException e) {
            log.error("Unexpected Error!", e);
        } finally {
            return ret;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
