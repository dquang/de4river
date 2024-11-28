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
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.minfo.BedHeightFacet;
import org.dive4elements.river.artifacts.model.minfo.BedHeightFactory;
import org.dive4elements.river.artifacts.model.minfo.BedHeightSoundingWidthFacet;
import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.river.artifacts.resources.Resources;

public class BedHeightsArtifact
extends      AbstractStaticStateArtifact
implements   FacetTypes
{
    /** The log for this class. */
    private static Logger log =
        LogManager.getLogger(BedHeightsArtifact.class);

    /** Artifact name. */
    private static final String NAME = "bedheights";

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(NAME, FacetActivity.INACTIVE);
    }

    public static final String STATIC_STATE_NAME =
        "state.additional_bedheights.static";

    /** Data Item name to know whether we are Heighmarks and receive
     * some data slightly different. */
    public static final String DATA_HEIGHT_TYPE =
        "height_marks";

    /**
     * Trivial Constructor.
     */
    public BedHeightsArtifact() {
        log.debug("BedHeightsArtifact.BedHeightsArtifact");
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
        log.debug("BedHeightsArtifact.setup");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        String code = getDatacageIDValue(data);

        if (code != null) {
            String [] parts = code.split("-");

            if (parts.length >= 3) {
                // The setting is a bit complicated:
                // This artifact can spawn epoch type bedheight facets,
                // 'singlevalue'/singleyear bedheight facets or
                // sounding-width facets. The type is indicated by
                // the ids-param which comes from datacage.

                String name = parts[0];
                String type = parts[1];
                String facetType = BEDHEIGHT;
                if (type.equals("soundings")) {
                    type = "singlevalues";
                    facetType = BEDHEIGHT_SOUNDING_WIDTH;
                }
                addStringData("height_id", parts[2]);
                addStringData("type", type);
                String btype = type;
                int hId = Integer.parseInt(parts[2]);

                if (type.equals("singlevalues")) {
                    btype = "single";
                }

                String bedHName = BedHeightFactory.getHeightName(btype, hId);

                Facet facet =  null;
                if (facetType.equals(BEDHEIGHT_SOUNDING_WIDTH)) {
                    bedHName = Resources.getMsg(
                        callMeta,
                        "facet.bedheight.sounding_width",
                        new Object[] { bedHName });
                    facet = new BedHeightSoundingWidthFacet(
                        facetType, bedHName);
                }
                else {
                    facet = new BedHeightFacet(facetType, bedHName);
                }


                ArrayList<Facet> facets = new ArrayList<Facet>(1);
                facets.add(facet);

                addFacets(STATIC_STATE_NAME, facets);
            }
            else {
                log.error("Invalid datacage ID '" + code + "'");
            }
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
        // do not clone facets, etc. from master artifact

        log.debug("initialize");
        importData((D4EArtifact)artifact, "river");
        importData((D4EArtifact)artifact, "ld_from");
        importData((D4EArtifact)artifact, "ld_to");

        log.debug("ld_from " + getDataAsString("ld_from"));
        log.debug("ld_to " + getDataAsString("ld_to"));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
