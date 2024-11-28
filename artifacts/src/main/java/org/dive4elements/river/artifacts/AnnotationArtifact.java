/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifactdatabase.state.StateEngine;

import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.artifacts.context.RiverContext;

import org.dive4elements.river.utils.RiverUtils;

/**
 * Artifact to access names of Points Of Interest along a segment of a river.
 */
public class AnnotationArtifact
extends      StaticD4EArtifact
implements   FacetTypes {

    /** The log for this class. */
    private static Logger log = LogManager.getLogger(AnnotationArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "annotation";

    // Let Annotations enter life inactively if in Fix Analysis LS setting.
    static {
        FacetActivity.Registry.getInstance().register(
            "annotation",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    if (output.contains(
                            FacetTypes.ChartType.FLSC.toString())
                    ) {
                        // Longitudinal section chart
                        String name = facet.getName();

                        if (name.contains(
                                FacetTypes.LONGITUDINAL_ANNOTATION)
                        ) {
                            return Boolean.FALSE;
                        }
                    }

                    return Boolean.TRUE;
                }
            });
    }


    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("AnnotationArtifact.setup");
        String filter = StaticD4EArtifact.getDatacageIDValue(data);
        String[] splits = filter.split(":");
        if (splits.length > 1) {
            addStringData("nameFilter", splits[1]);
        }
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    /** Get river, setup Facets. */
    @Override
    protected void initialize(Artifact artifact, Object context,
            CallMeta meta) {
        log.debug("AnnotationArtifact.initialize, id: "
            + artifact.identifier());

        D4EArtifact flys = (D4EArtifact) artifact;
        importData(flys, "river");

        List<Facet> fs = new ArrayList<Facet>();

        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);

        if (!fs.isEmpty()) {
            log.debug("Facets to add in AnnotationsArtifact.initialize .");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in AnnotationsArtifact.initialize .");
        }
    }


    /** Shortcut to nameFilter-data (TODO: move to respective Access). */
    public String getFilterName() {
        return getDataAsString("nameFilter");
    }


    public double[] getDistance() {
        /** TODO In initialize(), access maximal range of river (via
         * AnnotationFactory) instead of overriding getDistance,
         * important for diagram generation. */
        return new double[] {0f, 1000f};
    }


    /**
     * Create the description of this AnnotationArtifact-instance.
     *
     * @param data Some data.
     * @param context The CallContext.
     *
     * @return the description of this artifact.
     */
    @Override
    public Document describe(Document data, CallContext context) {
        log.debug("Describe: the current state is: " + getCurrentStateId());

        if (log.isDebugEnabled()) {
            dumpArtifact();
        }

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

        Element name = ProtocolUtils.createArtNode(
            creator, "name",
            new String[] { "value" },
            new String[] { getName() });

        Element outs = ProtocolUtils.createArtNode(
            creator, "outputmodes", null, null);
        appendOutputModes(description, outs, context);

        root.appendChild(name);
        root.appendChild(outs);

        return description;
    }


    /**
     * Returns the name of the concrete artifact.
     *
     * @return the name of the concrete artifact.
     */
    public String getName() {
        return ARTIFACT_NAME;
    }


    /**
     * Append outputmode elements to given document.
     *
     * @param doc Document to add outputmodes to.
     * @param outs Element to add outputmode elements to.
     * @param context The given CallContext (mostly for internationalization).
     */
    //@Override
    protected void appendOutputModes(
        Document    doc,
        Element     outs,
        CallContext context)
    {
        List<String> stateIds = getPreviousStateIds();

        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine      = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        for (String stateId: stateIds) {
            log.debug("Append output modes for state: " + stateId);
            DefaultState state = (DefaultState) engine.getState(stateId);

            List<Output> list = state.getOutputs();
            if (list == null || list.isEmpty()) {
                log.debug("-> No output modes for this state.");
                continue;
            }

            List<Facet> fs = getFacets(stateId);
            if (fs == null || fs.isEmpty()) {
                log.debug("No facets found.");
                continue;
            }

            log.debug("Found " + fs.size() + " facets in previous states.");

            List<Output> generated = generateOutputs(list, fs);

            ProtocolUtils.appendOutputModes(doc, outs, generated);
        }

        try {
            DefaultState cur = (DefaultState) getCurrentState(context);
            if (cur.validate(this)) {
                List<Output> list = cur.getOutputs();
                if (list != null && list.size() > 0) {
                    log.debug(
                        "Append output modes for state: " + cur.getID());

                    List<Facet> fs = getFacets(cur.getID());
                    if (fs != null && fs.size() > 0) {
                        List<Output> generated = generateOutputs(list, fs);

                        log.debug("Found " + fs.size() + " current facets.");
                        if (!generated.isEmpty()) {
                            ProtocolUtils.appendOutputModes(
                                doc, outs, generated);
                        }
                        else{
                            log.debug(
                                "Cannot append output to generated document.");
                        }
                    }
                    else {
                        log.debug("No facets found for the current state.");
                    }
                }
            }
        }
        catch (IllegalArgumentException iae) {
            // state is not valid, so we do not append its outputs.
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
