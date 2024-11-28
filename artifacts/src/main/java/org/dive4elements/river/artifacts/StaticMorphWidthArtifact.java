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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.model.minfo.MorphologicWidthFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.StaticState;

public class StaticMorphWidthArtifact
extends      AbstractStaticStateArtifact
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(StaticMorphWidthArtifact.class);

    private static final String NAME = "morph-width";
    private static final String STATIC_FACET_NAME = "morph-width";

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(NAME, FacetActivity.INACTIVE);
    }

    public static final String STATIC_STATE_NAME =
        "state.morph-width.static";

    /**
     * Trivial Constructor.
     */
    public StaticMorphWidthArtifact() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Gets called from factory, to set things up.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("setup");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        String code = getDatacageIDValue(data);

        if (code != null) {
                Facet facet = new MorphologicWidthFacet(
                        STATIC_FACET_NAME,
                        Resources.getMsg(
                            callMeta,
                            "facet.morphologic.width",
                            "morphologische Breite"));
                addStringData("width_id", code);
                ArrayList<Facet> facets = new ArrayList<Facet>(1);
                facets.add(facet);

                addFacets(STATIC_STATE_NAME, facets);
        }
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }

    @Override
    protected void initStaticState() {

        log.debug("initStaticState " + getName() + " " + identifier());

        StaticState state = new StaticState(STATIC_STATE_NAME);
        DefaultOutput output = new DefaultOutput(
                "general",
                "general",
                "image/png",
                "chart");

        List<Facet> facets = getFacets(STATIC_STATE_NAME);
        output.addFacets(facets);
        state.addOutput(output);

        setStaticState(state);
    }

    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta
    ) {
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
