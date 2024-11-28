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

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.BedHeightAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.minfo.MiddleBedHeightCalculation;
import org.dive4elements.river.artifacts.model.minfo.MiddleBedHeightData;
import org.dive4elements.river.artifacts.model.minfo.MiddleBedHeightFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Unit;


/** State in which Middle Bed Heights are generated. */
public class MiddleBedHeight extends DefaultState implements FacetTypes {

    /** Private log. */
    private static final Logger log = LogManager.getLogger(MiddleBedHeight.class);


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("MiddleBedHeight.computeAdvance");

        List<Facet> newFacets = new ArrayList<Facet>();

        BedHeightAccess access = new BedHeightAccess(artifact);
        River river = access.getRiver();
        Unit u = river.getWstUnit();

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult) old
            : new MiddleBedHeightCalculation().calculate(access);

        if (facets == null || res == null) {
            return res;
        }

        MiddleBedHeightData[] data = (MiddleBedHeightData[]) res.getData();

        log.debug("Calculated " + data.length + " MiddleBedHeightData objects");

        String id  = getID();
        int    idx = 0;

        for (MiddleBedHeightData d: data) {
            MiddleBedHeightFacet mf = new MiddleBedHeightFacet(
                idx,
                MIDDLE_BED_HEIGHT_SINGLE,
                d.getDescription(),
                ComputeType.ADVANCE,
                id,
                hash
            );
            mf.addMetaData(Resources.getMsg(
                    context.getMeta(),
                    "meta.bedheight.cur.elevation",
                    "Current elevation model"),
                d.getCurElevationModel());
            mf.addMetaData(Resources.getMsg(
                    context.getMeta(),
                    "meta.bedheight.old.elevation",
                    "Old elevation model"),
                d.getOldElevationModel());
            mf.addMetaData(Resources.getMsg(
                    context.getMeta(),
                    "meta.bedheight.river.elevation",
                    "River elevation model"), u.getName());
            newFacets.add(mf);

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
                    if (facet.getName().equals(MIDDLE_BED_HEIGHT_SINGLE)) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
            });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
