/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import javax.xml.xpath.XPathConstants;

import net.sf.ehcache.Cache;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifactdatabase.ArtifactDatabaseImpl;
import org.dive4elements.artifactdatabase.DefaultArtifact;
import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.DefaultStateData;
import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifactdatabase.state.StateEngine;
import org.dive4elements.artifactdatabase.state.StaticFacet;
import org.dive4elements.artifactdatabase.transition.TransitionEngine;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.Message;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.context.RiverContextFactory;
import org.dive4elements.river.artifacts.model.CalculationMessage;
import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.utils.RiverUtils;

/**
 * The default FLYS artifact with convenience added.
 * (Subclass to get fully functional artifacts).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class D4EArtifact extends DefaultArtifact {

    /** The log that is used in this artifact. */
    private static Logger log = LogManager.getLogger(D4EArtifact.class);

    public static final String COMPUTING_CACHE = "computed.values";

    /** XPath that points to the input data elements of the FEED document. */
    public static final String XPATH_FEED_INPUT =
        "/art:action/art:data/art:input";

    /** The XPath that points to the name of the target state of ADVANCE. */
    public static final String XPATH_ADVANCE_TARGET =
        "/art:action/art:target/@art:name";

    public static final String XPATH_MODEL_ARTIFACT =
        "/art:action/art:template/@uuid";

    public static final String XPATH_FILTER =
        "/art:action/art:filter/art:out";

    /** Path to 'ids' (data) in doc that comes from datacage. */
    public static final String XPATH_IDS = "/art:action/art:ids/@value";

    /** Path to 'target_out' (data) in doc that comes from datacage. */
    public static final String XPATH_TARGET_OUT =
        "/art:action/art:target_out/@value";

    /** The constant string that shows that an operation was successful. */
    public static final String OPERATION_SUCCESSFUL = "SUCCESS";

    /** The constant string that shows that an operation failed. */
    public static final String OPERATION_FAILED = "FAILURE";

    /** The identifier of the current state. */
    protected String currentStateId;

    /** The identifiers of previous states on a stack. */
    protected List<String> previousStateIds;

    /** The data that have been inserted into this artifact. */
    private Map<String, StateData> data;

    /** Mapping of state names to created facets. */
    private Map<String, List<Facet>> facets;

    /**
     * Used to generates "view" on the facets (hides facets not matching the
     * filter in output of collection);  out -&gt; facets.
     */
    protected Map<String, List<Facet>> filterFacets;

    /** To which out this artifacts facets are bound by default. */
    private String boundToOut;


    /**
     * Interface to a global facet filter.
     */
    public interface FacetFilter {
        boolean accept(String outName, String facetName);
    } // interface FacetFilter


    /**
     * The default constructor that creates an empty D4EArtifact.
     */
    public D4EArtifact() {
        data             = new TreeMap<String, StateData>();
        previousStateIds = new ArrayList<String>();
        facets           = new HashMap<String, List<Facet>>();
    }


    /**
     * This method appends the static data - that has already been inserted by
     * the user - to the static node of the DESCRIBE document.
     *
     * @param doc The document.
     * @param ui The root node.
     * @param context The CallContext.
     * @param uuid The identifier of the artifact.
     */
    protected void appendStaticUI(
        Document    doc,
        Node        ui,
        CallContext context,
        String uuid)
    {
        List<String> stateIds = getPreviousStateIds();

        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine      = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        boolean debug = log.isDebugEnabled();

        for (String stateId: stateIds) {
            if (debug) {
                log.debug("Append static data for state: " + stateId);
            }
            DefaultState state = (DefaultState) engine.getState(stateId);

            ui.appendChild(state.describeStatic(this, doc, ui, context, uuid));
        }
    }


    /**
     * Initialize the artifact and insert new data
     * if <code>data</code> contains
     * information necessary for this artifact.
     *
     * @param identifier The UUID.
     * @param factory The factory that is used to create this artifact.
     * @param context The CallContext.
     * @param data Some optional data.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     facets)
    {
        log.debug("Setup this artifact with the uuid: " + identifier);

        super.setup(identifier, factory, context, callMeta, data, facets);

        RiverContext flysContext = RiverUtils.getFlysContext(context);

        List<State> states = getStates(context);

        String name = getName();

        log.debug("setup(): Set initial state for artifact '" + name + "'");

        if (states == null) {
            log.error("No states found from which an initial "
                + "state could be picked.");
        }
        setCurrentState(states.get(0));

        handleInitModel(data, context, callMeta);

        if (!facets.isEmpty()) {
            buildStaticFacets(data, facets, callMeta);
        }

        filterFacets = buildFilterFacets(data);

        extractOut(data);
    }

    protected void buildStaticFacets(
        Document data,
        List<Class> facets,
        CallMeta callMeta)
    {
        List<Facet> staticFacets = new ArrayList<Facet>();
        String currentState = getCurrentStateId();
        for (int i = 0; i < facets.size(); i++) {
            try {
                StaticFacet facet = (StaticFacet)facets.get(i).newInstance();
                facet.setup(this, data, callMeta);
                staticFacets.add(facet);
            }
            catch (InstantiationException ie) {
                log.error(ie.getLocalizedMessage(), ie);
            }
            catch (IllegalAccessException iae) {
                log.error(iae.getLocalizedMessage(), iae);
            }
        }
        this.facets.put(currentState, staticFacets);
    }

    protected void handleInitModel(
        Document data,
        Object context,
        CallMeta callMeta
    ) {
        RiverContext flysContext = RiverUtils.getFlysContext(context);

        String model = XMLUtils.xpathString(
            data,
            XPATH_MODEL_ARTIFACT,
            ArtifactNamespaceContext.INSTANCE);

        if (model != null && model.length() > 0) {
            ArtifactDatabase db = (ArtifactDatabase) flysContext.get(
                ArtifactDatabaseImpl.GLOBAL_CONTEXT_KEY);

            try {
                initialize(db.getRawArtifact(model), context, callMeta);
            }
            catch (ArtifactDatabaseException adbe) {
                log.error(adbe, adbe);
            }
        }
    }

    protected void extractOut(Document data) {
        String targetOut = XMLUtils.xpathString(data, XPATH_TARGET_OUT,
            ArtifactNamespaceContext.INSTANCE);
        if (targetOut.isEmpty()) {
            targetOut = null;
        }

        setBoundToOut(targetOut);
    }

    /**
     * Return the value of id element in Datacage data document.
     * @param data Document as passed by datacage.
     * @return the id element value of data document.
     */
    public static String getDatacageIDValue(Document data) {
        String ids = XMLUtils.xpathString(data, XPATH_IDS,
            ArtifactNamespaceContext.INSTANCE);

        return ids;
    }



    /** Get copy of previous state ids as Strings in list. */
    protected List<String> clonePreviousStateIds() {
        return new ArrayList<String>(previousStateIds);
    }


    /**
     * Copies data item from other artifact to this artifact.
     *
     * @param other Artifact from which to get data.
     * @param name  Name of data.
     */
    protected void importData(D4EArtifact other, final String name) {
        if (other == null) {
            log.error("No other art. to import data " + name + " from.");
            return;
        }

        StateData sd = other.getData(name);

        if (sd == null) {
            log.warn("Other artifact has no data " + name + ".");
            return;
        }

        this.addData(name, sd);
    }


    /** Clone the internal map of map of state-name to state-data. */
    protected Map<String, StateData> cloneData() {
        Map<String, StateData> copy = new TreeMap<String, StateData>();

        for (Map.Entry<String, StateData> entry: data.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().deepCopy());
        }

        return copy;
    }


    /**
     * Return a copy of the facet mapping.
     * @return Mapping of state-ids to facets.
     */
    protected Map<String, List<Facet>> cloneFacets() {
        Map<String, List<Facet>> copy = new HashMap<String, List<Facet>>();

        for (Map.Entry<String, List<Facet>> entry: facets.entrySet()) {
            List<Facet> facets      = entry.getValue();
            List<Facet> facetCopies = new ArrayList<Facet>(facets.size());
            for (Facet facet: facets) {
                facetCopies.add(facet.deepCopy());
            }
            copy.put(entry.getKey(), facetCopies);
        }

        return copy;
    }


    /**
     * (called from setup).
     * @param artifact master-artifact
     *                 (if any, otherwise initialize is not called).
     */
    protected void initialize(
        Artifact artifact,
        Object   context,
        CallMeta callMeta)
    {
        if (!(artifact instanceof D4EArtifact)) {
            return;
        }

        D4EArtifact flys = (D4EArtifact)artifact;

        currentStateId   = flys.currentStateId;
        previousStateIds = flys.clonePreviousStateIds();
        name             = flys.name;
        data             = flys.cloneData();
        facets           = flys.cloneFacets();
        // Do not clone filter facets!

        ArrayList<String> stateIds = (ArrayList<String>) getPreviousStateIds();
        ArrayList<String> toInitialize = (ArrayList<String>) stateIds.clone();

        toInitialize.add(getCurrentStateId());

        for (String stateId: toInitialize) {
            State state = getState(context, stateId);

            if (state != null) {
                state.initialize(artifact, this, context, callMeta);
            }
        }
    }


    /**
     * Builds filter facets from document.
     * @see filterFacets
     */
    protected Map<String, List<Facet>> buildFilterFacets(Document document) {
        if (log.isDebugEnabled()) {
            log.debug("Building filter factes for artifact " + this.getName());
        }

        NodeList nodes = (NodeList)XMLUtils.xpath(
            document,
            XPATH_FILTER,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }

        Map<String, List<Facet>> result = new HashMap<String, List<Facet>>();

        for (int i = 0, N = nodes.getLength(); i < N; ++i) {
            Element element = (Element)nodes.item(i);
            String oName = element.getAttribute("name");
            if (oName == null || oName.isEmpty()) {
                continue;
            }

            List<Facet> facets = new ArrayList<Facet>();

            NodeList facetNodes = element.getElementsByTagNameNS(
                ArtifactNamespaceContext.NAMESPACE_URI,
                "facet");

            for (int j = 0, M = facetNodes.getLength(); j < M; ++j) {
                Element facetElement = (Element)facetNodes.item(j);

                String fName = facetElement.getAttribute("name");

                int index;
                try {
                    index = Integer.parseInt(
                        facetElement.getAttribute("index"));
                }
                catch (NumberFormatException nfe) {
                    log.warn(nfe);
                    index = 0;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Creating filter facet " + fName
                        + " with  index " + index
                        + " for out " + oName);
                }
                facets.add(new DefaultFacet(index, fName, ""));
            }

            if (!facets.isEmpty()) {
                result.put(oName, facets);
            }
        }

        return result;
    }


    /**
     * Insert new data included in <code>input</code> into the current state.
     *
     * @param target XML document that contains new data.
     * @param context The CallContext.
     *
     * @return a document that contains a SUCCESS or FAILURE message.
     */
    @Override
    public Document feed(Document target, CallContext context) {
        log.debug("D4EArtifact.feed()");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element result = creator.create("result");
        doc.appendChild(result);

        try {
            saveData(target, context);

            compute(context, ComputeType.FEED, true);

            return describe(target, context);
        }
        catch (IllegalArgumentException iae) {
            // do not store state if validation fails.
            context.afterCall(CallContext.NOTHING);
            creator.addAttr(result, "type", OPERATION_FAILED, true);

            result.setTextContent(iae.getMessage());
        }

        return doc;
    }


    /**
     * This method returns a description of this artifact.
     *
     * @param data Some data.
     * @param context The CallContext.
     *
     * @return the description of this artifact.
     */
    @Override
    public Document describe(Document data, CallContext context) {

        if (log.isDebugEnabled()) {
            log.debug(
                "Describe: the current state is: " + getCurrentStateId());
            dumpArtifact();
        }

        RiverContext flysContext = RiverUtils.getFlysContext(context);

        StateEngine stateEngine = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        TransitionEngine transitionEngine = (TransitionEngine) flysContext.get(
            RiverContext.TRANSITION_ENGINE_KEY);

        List<State> reachable = transitionEngine.getReachableStates(
            this, getCurrentState(context), stateEngine);

        Document description            = XMLUtils.newDocument();
        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            description,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ProtocolUtils.createRootNode(creator);
        description.appendChild(root);

        State current = getCurrentState(context);

        ProtocolUtils.appendDescribeHeader(
            creator, root, identifier(), hash());
        ProtocolUtils.appendState(creator, root, current);
        ProtocolUtils.appendReachableStates(creator, root, reachable);

        appendBackgroundActivity(creator, root, context);

        Element ui = ProtocolUtils.createArtNode(
            creator, "ui", null, null);

        Element staticUI  = ProtocolUtils.createArtNode(
            creator, "static", null, null);

        Element outs = ProtocolUtils.createArtNode(
            creator, "outputmodes", null, null);
        appendOutputModes(description, outs, context, identifier());

        appendStaticUI(description, staticUI, context, identifier());

        Element name = ProtocolUtils.createArtNode(
            creator, "name",
            new String[] { "value" },
            new String[] { getName() });

        Element dynamic = current.describe(
            this,
            description,
            root,
            context,
            identifier());

        if (dynamic != null) {
            ui.appendChild(dynamic);
        }

        ui.appendChild(staticUI);

        root.appendChild(name);
        root.appendChild(ui);
        root.appendChild(outs);

        return description;
    }

    /** Override me! */

    protected void appendBackgroundActivity(
        ElementCreator cr,
        Element        root,
        CallContext    context
    ) {
        LinkedList<Message> messages = context.getBackgroundMessages();

        if (messages == null) {
            return;
        }

        Element inBackground = cr.create("background-processing");
        root.appendChild(inBackground);

        cr.addAttr(
            inBackground,
            "value",
            String.valueOf(context.isInBackground()),
            true);

        CalculationMessage message = (CalculationMessage) messages.getLast();
        cr.addAttr(
            inBackground,
            "steps",
            String.valueOf(message.getSteps()),
            true);

        cr.addAttr(
            inBackground,
            "currentStep",
            String.valueOf(message.getCurrentStep()),
            true);

        inBackground.setTextContent(message.getMessage());
    }

    /**
     * Append output mode nodes to a document.
     */
    protected void appendOutputModes(
        Document    doc,
        Element     outs,
        CallContext context,
        String      uuid)
    {
        List<Output> generated = getOutputs(context);

        if (log.isDebugEnabled()) {
            log.debug("This Artifact has " + generated.size() + " Outputs.");
        }

        ProtocolUtils.appendOutputModes(doc, outs, generated);
    }


    /**
     * This method handles request for changing the current state of an
     * artifact. It is possible to step forward or backward.
     *
     * @param target The incoming ADVANCE document.
     * @param context The CallContext.
     *
     * @return a document that contains a SUCCESS or FAILURE message.
     */
    @Override
    public Document advance(Document target, CallContext context) {

        boolean debug = log.isDebugEnabled();

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element result = ec.create("result");

        String currentStateId = getCurrentStateId();
        String targetState    = XMLUtils.xpathString(
            target, XPATH_ADVANCE_TARGET, ArtifactNamespaceContext.INSTANCE);

        if (debug) {
            log.debug("D4EArtifact.advance() to '" + targetState + "'");
        }

        if (!currentStateId.equals(targetState)
            && isStateReachable(targetState, context))
        {
            if (debug) {
                log.debug("Advance: Step forward");
            }

            List<String> prev = getPreviousStateIds();
            prev.add(currentStateId);

            setCurrentStateId(targetState);

            if (debug) {
                log.debug("Compute data for state: " + targetState);
            }
            compute(context, ComputeType.ADVANCE, true);

            return describe(target, context);
        }
        else if (isPreviousState(targetState, context)) {
            if (debug) {
                log.debug("Advance: Step back to");
            }

            List<String> prevs   = getPreviousStateIds();
            int targetIdx        = prevs.indexOf(targetState);
            int start            = prevs.size() - 1;

            destroyStates(prevs, context);

            for (int i = start; i >= targetIdx; i--) {
                String prev = prevs.get(i);
                if (debug) {
                    log.debug("Remove state id '" + prev + "'");
                }

                prevs.remove(prev);
                facets.remove(prev);
            }

            destroyState(getCurrentStateId(), context);
            setCurrentStateId(targetState);

            return describe(target, context);
        }

        log.warn("Advance: Cannot advance to '" + targetState + "'");
        ec.addAttr(result, "type", OPERATION_FAILED, true);

        doc.appendChild(result);

        return doc;
    }


    /**
     * Returns the identifier of the current state.
     *
     * @return the identifier of the current state.
     */
    public String getCurrentStateId() {
        return currentStateId;
    }


    /**
     * Sets the identifier of the current state.
     *
     * @param id the identifier of a state.
     */
    protected void setCurrentStateId(String id) {
        currentStateId = id;
    }


    /**
     * Set the current state of this artifact. <b>NOTE</b>We don't store the
     * State object itself - which is not necessary - but its identifier. So
     * this method will just call the setCurrentStateId() method with the
     * identifier of <i>state</i>.
     *
     * @param state The new current state.
     */
    protected void setCurrentState(State state) {
        setCurrentStateId(state.getID());
    }


    /**
     * Returns the current state of the artifact.
     *
     * @return the current State of the artifact.
     */
    public State getCurrentState(Object context) {
        return getState(context, getCurrentStateId());
    }


    /**
     * Get list of existant states for this Artifact.
     * @param context Contex to get StateEngine from.
     * @return list of states.
     */
    protected List<State> getStates(Object context) {
        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine      = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);
        return engine.getStates(getName());
    }


    /**
     * Get state with given ID.
     * @param context Context to get StateEngine from.
     * @param stateID ID of state to get.
     * @return state with given ID.
     */
    protected State getState(Object context, String stateID) {
        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine      = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);
        return engine.getState(stateID);
    }


    /**
     * Returns the vector of previous state identifiers.
     *
     * @return the vector of previous state identifiers.
     */
    protected List<String> getPreviousStateIds() {
        return previousStateIds;
    }


    /**
     * Get all previous and the current state id.
     * @return #getPreviousStateIds() + #getCurrentStateId()
     */
    public List<String> getStateHistoryIds() {
        ArrayList<String> prevIds = (ArrayList) getPreviousStateIds();
        ArrayList<String> allIds  = (ArrayList) prevIds.clone();

        allIds.add(getCurrentStateId());
        return allIds;
    }


    /**
     * Adds a new StateData item to the data pool of this artifact.
     *
     * @param name the name of the data object.
     * @param data the data object itself.
     */
    protected void addData(String name, StateData data) {
        this.data.put(name, data);
    }


    /** Remove and return statedata associated to name. */
    protected StateData removeData(String name) {
        return this.data.remove(name);
    }


    /**
     * This method returns a specific StateData object that is stored in the
     * data pool of this artifact.
     *
     * @param name The name of the data object.
     *
     * @return the StateData object if existing, otherwise null.
     */
    public StateData getData(String name) {
        return data.get(name);
    }


    /**
     * A derived Artifact class can use this method to set the data
     */
    protected void setData(Map<String, StateData> data) {
        this.data = data;
    }


    /** Return named data item, null if not found. */
    public String getDataAsString(String name) {
        StateData data = getData(name);
        return data != null ? (String) data.getValue() : null;
    }


    /**
     * This method returns the value of a StateData object stored in the data
     * pool of this Artifact as Integer.
     *
     * @param name The name of the StateData object.
     *
     * @return an Integer representing the value of the data object or null if
     * no object was found for <i>name</i>.
     *
     * @throws NumberFormatException if the value of the data object could not
     * be transformed into an Integer.
     */
    public Integer getDataAsInteger(String name)
    throws NumberFormatException
    {
        String value = getDataAsString(name);

        if (value != null && value.length() > 0) {
            return Integer.parseInt(value);
        }

        return null;
    }


    /**
     * This method returns the value of a StateData object stored in the data
     * pool of this Artifact as Double.
     *
     * @param name The name of the StateData object.
     *
     * @return an Double representing the value of the data object or null if
     * no object was found for <i>name</i>.
     *
     * @throws NumberFormatException if the value of the data object could not
     * be transformed into a Double.
     */
    public Double getDataAsDouble(String name)
    throws NumberFormatException
    {
        String value = getDataAsString(name);

        if (value != null && value.length() > 0) {
            return Double.parseDouble(value);
        }

        return null;
    }


    /**
     * This method returns the value of a StateData object stored in the data
     * pool of this Artifact as Long.
     *
     * @param name The name of the StateData object.
     *
     * @return a Long representing the value of the data object or null if
     * no object was found for <i>name</i>.
     *
     * @throws NumberFormatException if the value of the data object could not
     * be transformed into a Long.
     */
    public Long getDataAsLong(String name)
    throws NumberFormatException
    {
        String value = getDataAsString(name);

        if (value != null && value.length() > 0) {
            return Long.parseLong(value);
        }

        return null;
    }


    /**
     * This method returns the value of a StateData object stored in the data
     * pool of this Artifact is Boolean using Boolean.valueOf().
     *
     * @param name The name of the StateData object.
     *
     * @return a Boolean representing the value of the data object or
     * null if no such object is existing.
     */
    public Boolean getDataAsBoolean(String name) {
        String value = getDataAsString(name);

        if (value == null || value.length() == 0) {
            return null;
        }

        return Boolean.valueOf(value);
    }


    /**
     * Add StateData containing a given string.
     * @param name Name of the data object.
     * @param value String to store.
     */
    public void addStringData(String name, String value) {
        addData(name, new DefaultStateData(name, null, null, value));
    }

    /**
     * Returns all stored StateData in this artifact as a Collection
     * @return a Collection of all StateData objects in this artifact
     */
    public Collection<StateData> getAllData() {
        return data.values();
    }


    /** Return all produced facets. */
    public List<Facet> getFacets() {
        List<Facet> all = new ArrayList<Facet>();

        // Iterate over facets of each state.
        for (List<Facet> fs: facets.values()) {
            all.addAll(fs);
        }

        return all;
    }


    /**
     * Get facet as stored internally, with equalling name and index than given
     * facet.
     * @param facet that defines index and name of facet searched.
     * @return facet instance or null if not found.
     */
    public Facet getNativeFacet(Facet facet, String outName) {
        String name  = facet.getName();
        int    index = facet.getIndex();
        if (getBoundToOut() != null && !getBoundToOut().isEmpty() &&
                !getBoundToOut().equals(outName)) {
            log.debug(name + ": not returning facets for " + outName +
                    " because bound to " + getBoundToOut());
            return null;
        }
        log.debug("Facet: " + facet.getName());
        log.debug("Bound to out: " + getBoundToOut());
        log.debug("OutName: " + outName);

        for (List<Facet> fs: facets.values()) {
            for (Facet f: fs) {
                if (f.getIndex() == index && f.getName().equals(name)) {
                    return f;
                }
            }
        }

        log.warn("Could not find facet: " + name + " at " + index);
        log.warn("Available facets for : " + getName() + " " + identifier() +
                ": " + facets.values());
        return null;
    }


    /**
     * This method stores the data that is contained in the FEED document.
     *
     * @param feed The FEED document.
     * @param xpath The XPath that points to the data nodes.
     */
    public void saveData(Document feed, CallContext context)
    throws IllegalArgumentException
    {
        if (feed == null) {
            throw new IllegalArgumentException("error_feed_no_data");
        }

        NodeList nodes = (NodeList) XMLUtils.xpath(
            feed,
            XPATH_FEED_INPUT,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (nodes == null || nodes.getLength() == 0) {
            throw new IllegalArgumentException("error_feed_no_data");
        }

        boolean debug = log.isDebugEnabled();

        int count = nodes.getLength();

        if (debug) {
            log.debug("Try to save " + count + " data items.");
        }

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        DefaultState current = (DefaultState) getCurrentState(context);

        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine      = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        for (int i = 0; i < count; i++) {
            Element node = (Element)nodes.item(i);

            String name  = node.getAttributeNS(uri, "name");
            String value = node.getAttributeNS(uri, "value");

            if (name.length() > 0 && value.length() > 0) {
                if (debug) {
                    log.debug("Save data item for '" + name + "' : " + value);
                }

                StateData model = engine.getStateData(getName(), name);

                StateData sd = model != null
                    ? model.deepCopy()
                    : new DefaultStateData(name, null, null, value);

                addData(
                    name, current.transform(this, context, sd, name, value));
            }
            else if (name.length() > 0 && value.length() == 0) {
                if (removeData(name) != null && debug) {
                    log.debug("Removed data '" + name + "' successfully.");
                }
            }
        }

        current.validate(this, context);
    }


    /**
     * Determines if the state with the identifier <i>stateId</i> is reachable
     * from the current state. The determination itself takes place in the
     * TransitionEngine.
     *
     * @param stateId The identifier of a state.
     * @param context The context object.
     *
     * @return true, if the state specified by <i>stateId</i> is reacahble,
     * otherwise false.
     */
    protected boolean isStateReachable(String stateId, Object context) {

        if (log.isDebugEnabled()) {
            log.debug("Determine if the state '"
                + stateId + "' is reachable.");
        }

        RiverContext flysContext = RiverUtils.getFlysContext(context);

        State currentState  = getCurrentState(context);
        StateEngine sEngine = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        TransitionEngine tEngine = (TransitionEngine) flysContext.get(
            RiverContext.TRANSITION_ENGINE_KEY);

        return tEngine.isStateReachable(this, stateId, currentState, sEngine);
    }


    /**
     * Determines if the state with the identifier <i>stateId</i> is a previous
     * state of the current state.
     *
     * @param stateId The target state identifier.
     * @param context The context object.
     */
    protected boolean isPreviousState(String stateId, Object context) {
        if (log.isDebugEnabled()) {
            log.debug("Determine if the state '" + stateId + "' is old.");
        }

        return getPreviousStateIds().contains(stateId);
    }


    /**
     * Computes the hash code of the entered values.
     *
     * @return a hash code.
     */
    @Override
    public String hash() {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ObjectOutputStream oa = new ObjectOutputStream(ba);
            for (Map.Entry<String, StateData> entry: data.entrySet()) {
                oa.writeObject(entry.getKey());
                oa.writeObject(entry.getValue().getValue());
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5sum = md.digest(ba.toByteArray());
            return Base64.getEncoder().encodeToString(md5sum);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 unavailable. Can't happen.");
        } catch (IOException e) {
            throw new RuntimeException("Cant write parameter. Can't happen.");
        }
    }


    /**
     * Return List of outputs, where combinations of outputname and filtername
     * that match content in filterFacets is left out.
     * @return filtered Outputlist.
     */
    protected List<Output> filterOutputs(List<Output> outs) {
        if (filterFacets == null || filterFacets.isEmpty()) {
            log.debug("No filter for Outputs.");
            return outs;
        }

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "Filter Facets with " + filterFacets.size() + " filters.");
        }

        List<Output> filtered = new ArrayList<Output>();

        for (Output out: outs) {
            String outName = out.getName();

            if (debug) {
                log.debug("  filter Facets for Output: " + outName);
            }

            List<Facet> fFacets = filterFacets.get(outName);
            if (fFacets != null) {
                if (debug) {
                    log.debug("" + fFacets.size()
                        + " filters for: " + outName);
                    for (Facet tmp: fFacets) {
                        log.debug("   filter = '" + tmp.getName() + "'");
                    }
                }

                List<Facet> resultFacets = new ArrayList<Facet>();

                for (Facet facet: out.getFacets()) {
                    for (Facet fFacet: fFacets) {
                        if (facet.getIndex() == fFacet.getIndex()
                        &&  facet.getName().equals(fFacet.getName())) {
                            resultFacets.add(facet);
                            break;
                        }
                    }
                }

                if (debug) {
                    log.debug(
                        "Facets after filtering = " + resultFacets.size());
                }

                if (!resultFacets.isEmpty()) {
                    DefaultOutput nout = new DefaultOutput(
                        out.getName(),
                        out.getDescription(),
                        out.getMimeType(),
                        resultFacets);
                    filtered.add(nout);
                }
            }
            else if (debug) {
                log.debug("No filter Factes for Output: " + outName);
            }
        }

        if (debug) {
            log.debug("Number of outs after filtering = " + filtered.size());
        }

        return filtered;
    }


    /**
     * Get all outputs that the Artifact can do in this state (which includes
     * all previous states).
     *
     * @return list of outputs
     */
    public List<Output> getOutputs(Object context) {
        if (log.isDebugEnabled()) {
            dumpArtifact();
        }

        List<String> stateIds  = getPreviousStateIds();
        List<Output> generated = new ArrayList<Output>();

        for (String stateId: stateIds) {
            DefaultState state = (DefaultState) getState(context, stateId);
            generated.addAll(getOutputForState(state));
        }

        generated.addAll(getCurrentOutputs(context));

        return filterOutputs(generated);
    }


    /**
     * Get output(s) for current state.
     * @return list of outputs for current state.
     */
    public List<Output> getCurrentOutputs(Object context) {
        DefaultState cur = (DefaultState) getCurrentState(context);

        try {
            if (context instanceof CallContext) {
                /* should be always true */
                CallContext cc = (CallContext) context;
                cur.validate(this, cc);
            }
            return getOutputForState(cur);
        }
        catch (IllegalArgumentException iae) { }

        return new ArrayList<Output>();
    }


    /**
     * Get output(s) for a specific state.
     * @param state State of interest
     * @return list of output(s) for given state.
     */
    protected List<Output> getOutputForState(DefaultState state) {

        if (state == null) {
            log.error("state == null: This should not happen!");
            return new ArrayList<Output>();
        }

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Find Outputs for State: " + state.getID());
        }

        List<Output> list = state.getOutputs();
        if (list == null || list.isEmpty()) {
            if (debug) {
                log.debug("-> No output modes for this state.");
            }
            return new ArrayList<Output>();
        }

        String stateId = state.getID();

        List<Facet> fs = getFacets(stateId);

        if (fs == null || fs.isEmpty()) {
            if (debug) {
                log.debug("No facets found.");
            }
            return new ArrayList<Output>();
        }
        if (debug) {
            log.debug("State '" + stateId + "' has " + fs.size() + " facets");
        }

        List<Output> gen = generateOutputs(list, applyGlobalFilterFacets(fs));

        if (debug) {
            log.debug("State '" + stateId + "' has " + gen.size() + " outs");
        }

        return gen;
    }

    /** If we use a facet filter that bases the list of compatible facets
     * on the output this artifact is bound to then return true. */
    public boolean usesOutputGlobalFacetFilter() {
        if (boundToOut == null || boundToOut.isEmpty()) {
            return false;
        }

        FacetFilter facetFilter =
            (FacetFilter)RiverContextFactory.getGlobalContext()
                .get(RiverContext.FACETFILTER_KEY);

        return facetFilter != null;
    }

    /**
     * If a global facet filter and a bounded out are defined
     * use them to eliminate unwished facets.
     */
    protected List<Facet> applyGlobalFilterFacets(List<Facet> facets) {
        if (!usesOutputGlobalFacetFilter()) {
            return facets;
        }

        FacetFilter facetFilter =
            (FacetFilter)RiverContextFactory.getGlobalContext()
                .get(RiverContext.FACETFILTER_KEY);

        List<Facet> result = new ArrayList<Facet>(facets.size());
        for (Facet facet: facets) {
            if (facetFilter.accept(boundToOut, facet.getName())) {
                result.add(facet);
            }
        }
        return result;
    }

    /**
     * Generate a list of outputs with facets from fs if type is found in list
     * of output.
     *
     * @param list List of outputs
     * @param fs List of facets
     */
    protected List<Output> generateOutputs(List<Output> list, List<Facet> fs) {
        boolean debug = log.isDebugEnabled();

        List<Output> generated = new ArrayList<Output>();

        if (debug) {
            log.debug("generateOutputs for Artifact " + getName() + " "
                + identifier());
        }

        boolean useFacetFilter = usesOutputGlobalFacetFilter();

        for (Output out: list) {
            if (debug) {
                log.debug("check facets for output: " + out.getName());
            }
            String outName = out.getName();
            Output o = new DefaultOutput(
                outName,
                out.getDescription(),
                out.getMimeType(),
                out.getType());

            Set<String> outTypes = new HashSet<String>();

            for (Facet f: out.getFacets()) {
                if (outTypes.add(f.getName()) && debug) {
                    log.debug("configured facet " + f);
                }
            }

            boolean facetAdded = false;
            for (Facet f: fs) {
                String type = f.getName();

                /* Match the facets to the output configuration.
                 * This is only done when we are not using the Output
                 * we are bound to to determine the compatible facets. */
                if (useFacetFilter || outTypes.contains(type)) {
                    if (debug) {
                        log.debug("Add facet " + f);
                    }
                    facetAdded = true;
                    o.addFacet(f);
                }
            }

            if (facetAdded) {
                generated.add(o);
            }
        }

        return generated;
    }


    /**
     * Dispatches the computation request to
     * compute(CallContext context, String hash)
     * with the current hash value of the artifact which is provided by
     * hash().
     *
     * @param context The CallContext.
     */
    public Object compute(
        CallContext context,
        ComputeType type,
        boolean     generateFacets
    ) {
        return compute(context, hash(), type, generateFacets);
    }


    /**
     * Dispatches computation requests to the current state which needs to
     * implement a createComputeCallback(String hash, D4EArtifact artifact)
     * method.
     *
     * @param context The CallContext.
     * @param hash The hash value which is used to fetch computed data from
     * cache.
     *
     * @return the computed data.
     */
    public Object compute(
        CallContext context,
        String      hash,
        ComputeType type,
        boolean     generateFacets
    ) {
        DefaultState current = (DefaultState) getCurrentState(context);
        return compute(context, hash, current, type, generateFacets);
    }


    /**
     * Like compute, but identify State by its id (string).
     */
    public Object compute(
        CallContext context,
        String      hash,
        String      stateID,
        ComputeType type,
        boolean     generateFacets
    ) {
        DefaultState current =
            (stateID == null)
            ? (DefaultState)getCurrentState(context)
            : (DefaultState)getState(context, stateID);

        if (hash == null) {
            hash = hash();
        }

        return compute(context, hash, current, type, generateFacets);
    }


    /**
     * Let current state compute and register facets.
     *
     * @param key key of state
     * @param state state
     * @param type Type of compute
     * @param generateFacets Whether new facets shall be generated.
     */
    public Object compute(
        CallContext   context,
        String        key,
        DefaultState  state,
        ComputeType   type,
        boolean       generateFacets
    ) {
        String stateID = state.getID();

        List<Facet> fs = (generateFacets) ? new ArrayList<Facet>() : null;

        try {
            Cache cache = CacheFactory.getCache(COMPUTING_CACHE);

            Object old = null;

            if (cache != null) {
                net.sf.ehcache.Element element = cache.get(key);
                if (element != null) {
                    log.debug(
                        "Got computation result from cache for key: " + key);
                    old = element.getValue();
                }
            }
            else {
                log.debug("cache not configured.");
            }

            Object res;
            switch (type) {
                case FEED:
                    res = state.computeFeed(this, key, context, fs, old);
                    break;
                case ADVANCE:
                    res = state.computeAdvance(this, key, context, fs, old);
                    break;
                case INIT:
                    res = state.computeInit(
                        this, key, context, context.getMeta(), fs);
                default:
                    res = null;
            }

            if (cache != null && old != res && res != null) {
                log.debug("Store computation result to cache.");
                net.sf.ehcache.Element element =
                    new net.sf.ehcache.Element(key, res);
                cache.put(element);
            }

            return res;
        }
        finally {
            if (generateFacets) {
                if (fs.isEmpty()) {
                    facets.remove(stateID);
                }
                else {
                    addFacets(stateID, fs);
                }
            }
        }
    }

    /**
     * Sets the facets for an ID, which is normally a state ID.
     *
     * @param id ID to map the facets to
     * @param facets List of facets to be stored
     */
    protected void addFacets(String id, List<Facet> facets) {
        for (Facet fac : facets) {
            fac.setBoundToOut(getBoundToOut());
        }
        this.facets.put(id, facets);
    }


    /**
     * Method to dump the artifacts state/data.
     */
    public void dumpArtifact() {
        log.debug("++++++++++++++ DUMP ARTIFACT DATA +++++++++++++++++");
        // Include uuid, type, name
        log.debug(" - Name: " + getName());
        log.debug(" - UUID: " + identifier());
        log.debug(" - Class: " + getClass().getName());
        log.debug(" - BoundToOut: " + getBoundToOut());

        log.debug("------ DUMP DATA ------");
        Collection<StateData> allData = data.values();

        for (StateData d: allData) {
            String name  = d.getName();
            String value = (String) d.getValue();

            log.debug("- " + name + ": " + value);
        }

        log.debug("------ DUMP PREVIOUS STATES ------");
        List<String> stateIds = getPreviousStateIds();

        for (String id: stateIds) {
            log.debug("- State: " + id);
        }

        log.debug("CURRENT STATE: " + getCurrentStateId());

        debugFacets();
        dumpFilterFacets();

        log.debug("++++++++++++++ END ARTIFACT DUMP +++++++++++++++++");
    }


    public void debugFacets() {
        log.debug("######### FACETS #########");

        for (Map.Entry<String, List<Facet>> entry: facets.entrySet()) {
            String out = entry.getKey();
            List<Facet> fs = entry.getValue();
            for (Facet f: fs) {
                log.debug("  # " + out + " : " + f.getName());
                log.debug("  # boundToOut : " + f.getBoundToOut());
            }
        }

        log.debug("######## FACETS END ########");
    }


    public void dumpFilterFacets() {
        log.debug("######## FILTER FACETS ########");

        if (filterFacets == null || filterFacets.isEmpty()) {
            log.debug("No Filter Facets defined.");
            return;
        }

        for (Map.Entry<String, List<Facet>> entry: filterFacets.entrySet()) {
            String      out     = entry.getKey();
            List<Facet> filters = entry.getValue();

            log.debug("There are " + filters.size() + " filters for: " + out);

            for (Facet filter: filters) {
                log.debug("  filter: " + filter.getName());
            }
        }

        log.debug("######## FILTER FACETS END ########");
    }


    /** Destroy and clean up state with given id. */
    protected void destroyState(String id, Object context) {
        State s = getState(context, id);
        s.endOfLife(this, context);
    }


    /**
     * Calls endOfLife() for each state in the list <i>ids</i>.
     *
     * @param ids The State IDs that should be destroyed.
     * @param context The RiverContext.
     */
    protected void destroyStates(List<String> ids, Object context) {
        for (int i = 0, num = ids.size(); i < num; i++) {
            destroyState(ids.get(i), context);
        }
    }


    /**
     * Destroy the states.
     */
    @Override
    public void endOfLife(Object context) {
        if (log.isDebugEnabled()) {
            log.debug("D4EArtifact.endOfLife: " + identifier());
        }

        ArrayList<String> ids = (ArrayList<String>) getPreviousStateIds();
        ArrayList<String> toDestroy = (ArrayList<String>) ids.clone();

        toDestroy.add(getCurrentStateId());

        destroyStates(toDestroy, context);
    }

    /**
     * Return the Facets which a state provides.
     * @param stateid String that identifies the state
     * @return List of Facets belonging to the state identifier
     */
    protected List<Facet> getFacets(String stateid) {
        return facets.get(stateid);
    }

    public String getBoundToOut() {
        return boundToOut;
    }

    /**
     * Binds this artifact and all its facet to an out.
     */
    public void setBoundToOut(String out) {
        boundToOut = out;
        for (List<Facet> stateFacets: facets.values()) {
            for (Facet fac: stateFacets) {
                fac.setBoundToOut(out);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
