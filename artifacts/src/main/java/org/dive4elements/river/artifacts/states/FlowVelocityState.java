/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.FlowVelocityAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.FlowVelocityCalculation;
import org.dive4elements.river.artifacts.model.FlowVelocityData;
import org.dive4elements.river.artifacts.model.FlowVelocityFacet;
import org.dive4elements.river.artifacts.model.FlowVelocityFilterFacet;
import org.dive4elements.river.artifacts.resources.Resources;


/* State in which flow velocities can/will be calculated. */
public class FlowVelocityState extends DefaultState implements FacetTypes {

    private static Logger log = LogManager.getLogger(FlowVelocityState.class);

    public static final String I18N_MAINCHANNEL_FACET =
        "facet.flow_velocity.mainchannel";

    public static final String I18N_TOTALCHANNEL_FACET =
        "facet.flow_velocity.totalchannel";

    public static final String I18N_TAU_FACET =
        "facet.flow_velocity.tauchannel";

    public static final String I18N_MAINCHANNEL_FACET_RAW =
        "facet.flow_velocity.mainchannel.raw";

    public static final String I18N_TOTALCHANNEL_FACET_RAW =
        "facet.flow_velocity.totalchannel.raw";

    public static final String I18N_TAU_FACET_RAW =
        "facet.flow_velocity.tauchannel.raw";

    public static final String I18N_DISCHARGE_FACET =
        "facet.flow_velocity.discharge";


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("FlowVelocityState.computeAdvance");

        List<Facet> newFacets = new ArrayList<Facet>();

        FlowVelocityAccess access = new FlowVelocityAccess(artifact);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult) old
            : new FlowVelocityCalculation().calculate(access);

        if (facets == null || res == null) {
            return res;
        }

        FlowVelocityData[] data = (FlowVelocityData[]) res.getData();

        log.debug("Calculated " + data.length + " FlowVelocityData objects");

        String id  = getID();
        int    idx = 0;

        for (FlowVelocityData d: data) {
            if (d.getType().equals("main")) {
                newFacets.add(new FlowVelocityFacet(
                    idx,
                    FLOW_VELOCITY_MAINCHANNEL,
                    buildFacetName(
                        artifact, context, d, I18N_MAINCHANNEL_FACET_RAW),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));

                newFacets.add(new FlowVelocityFacet(
                    idx,
                    FLOW_VELOCITY_TAU,
                    buildFacetName(artifact, context, d, I18N_TAU_FACET_RAW),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFilterFacet(
                    idx,
                    FLOW_VELOCITY_MAINCHANNEL_FILTERED,
                    buildMainChannelName(artifact, context, d),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFilterFacet(
                    idx,
                    FLOW_VELOCITY_TAU_FILTERED,
                    buildTauName(artifact, context, d),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
            }
            else if (d.getType().equals("total")) {
                newFacets.add(new FlowVelocityFacet(
                    idx,
                    FLOW_VELOCITY_TOTALCHANNEL,
                    buildFacetName(
                        artifact, context, d, I18N_TOTALCHANNEL_FACET_RAW),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFilterFacet(
                    idx,
                    FLOW_VELOCITY_TOTALCHANNEL_FILTERED,
                    buildTotalChannelName(artifact, context, d),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));

            }
            else if(d.getType().equals("main_total")) {
                 newFacets.add(new FlowVelocityFacet(
                    idx,
                    FLOW_VELOCITY_MAINCHANNEL,
                    buildFacetName(
                        artifact, context, d, I18N_MAINCHANNEL_FACET_RAW),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFacet(
                    idx,
                    FLOW_VELOCITY_TAU,
                    buildFacetName(artifact, context, d, I18N_TAU_FACET_RAW),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFacet(
                    idx,
                    FLOW_VELOCITY_TOTALCHANNEL,
                    buildFacetName(
                        artifact, context, d, I18N_TOTALCHANNEL_FACET_RAW),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFilterFacet(
                    idx,
                    FLOW_VELOCITY_MAINCHANNEL_FILTERED,
                    buildMainChannelName(artifact, context, d),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFilterFacet(
                    idx,
                    FLOW_VELOCITY_TAU_FILTERED,
                    buildTauName(artifact, context, d),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
                newFacets.add(new FlowVelocityFilterFacet(
                    idx,
                    FLOW_VELOCITY_TOTALCHANNEL_FILTERED,
                    buildTotalChannelName(artifact, context, d),
                    ComputeType.ADVANCE,
                    id,
                    hash
                ));
            }

            newFacets.add(new FlowVelocityFacet(
                idx,
                FLOW_VELOCITY_DISCHARGE,
                buildDischargeName(artifact, context, d),
                ComputeType.ADVANCE,
                id,
                hash
            ));

            idx++;
        }

        Facet csv = new DataFacet(
            CSV, "CSV data", ComputeType.ADVANCE, hash, id);

        // TODO ADD PDF FACET

        newFacets.add(csv);

        log.debug("Created " + newFacets.size() + " new Facets.");

        facets.addAll(newFacets);

        return res;
    }


    protected String buildFacetName(
        D4EArtifact      flys,
        CallContext      cc,
        FlowVelocityData data,
        String           resourceId
    ) {
        Object[] args = new Object[] {
            data.getZone()
        };

        return Resources.getMsg(
            cc.getMeta(),
            resourceId,
            resourceId,
            args);
    }


    protected String buildMainChannelName(
        D4EArtifact     flys,
        CallContext      cc,
        FlowVelocityData data
    ) {
        return buildFacetName(flys, cc, data, I18N_MAINCHANNEL_FACET);
    }


    protected String buildTotalChannelName(
        D4EArtifact     flys,
        CallContext      cc,
        FlowVelocityData data
    ) {
        return buildFacetName(flys, cc, data, I18N_TOTALCHANNEL_FACET);
    }


    protected String buildDischargeName(
        D4EArtifact     flys,
        CallContext      cc,
        FlowVelocityData data
    ) {
        return buildFacetName(flys, cc, data, I18N_DISCHARGE_FACET);
    }

    protected String buildTauName(
        D4EArtifact     flys,
        CallContext      cc,
        FlowVelocityData data
    ) {
        return buildFacetName(flys, cc, data, I18N_TAU_FACET);
    }

    static {
        // Active/deactivate facets.
        FacetActivity.Registry.getInstance().register(
            "minfo",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    String name = facet.getName();
                    if (name.equals(FLOW_VELOCITY_MAINCHANNEL_FILTERED) ||
                        name.equals(FLOW_VELOCITY_TAU_FILTERED) ||
                        name.equals(FLOW_VELOCITY_TOTALCHANNEL_FILTERED)) {
                        return Boolean.TRUE;
                    }
                    else if (name.equals(FLOW_VELOCITY_MAINCHANNEL) ||
                        name.equals(FLOW_VELOCITY_TAU) ||
                        name.equals(FLOW_VELOCITY_TOTALCHANNEL) ||
                        name.equals(FLOW_VELOCITY_DISCHARGE)
                        ) {
                        return Boolean.FALSE;
                    }
                    else {
                        return null;
                    }
                }
            });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
