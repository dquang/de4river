/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.ManualPointsArtifact;
import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ManualPointsFacet;
import org.dive4elements.river.artifacts.resources.Resources;

/**
 * The only state for an ManualPointArtifact.
 */
public class ManualPointsSingleState
extends      DefaultState
implements   FacetTypes
{
    /** Developer-centric description of facet. */
    public static final String I18N_DESCRIPTION
        = "facet.longitudinal_section.manualpoint";

    /** Part of data key. */
    protected static final String DOT_DATA
        = ".data";

    /** Part of data key. */
    protected static final String DOT_LINES
        = ".lines";

    /** The log that is used in this state. */
    private static final Logger log =
        LogManager.getLogger(ManualPointsSingleState.class);


    /**
     * Add ManualPointsFacets to list of Facets.
     *
     * @param artifact Ignored.
     * @param hash Ignored.
     * @param meta CallMeta to be used for internationalization.
     * @param facets List to add ManualPointsFacet to.
     *
     * @return null.
     */
    public Object compute(
        D4EArtifact artifact,
        String       hash,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        log.debug("ManualPointsSingleState.compute()");
        ManualPointsArtifact points = (ManualPointsArtifact) artifact;

        // Add Facet per Diagram type if data given.
        for (ChartType ct: ChartType.values()) {
            // Handle points.
            String pointData = points.getDataAsString(ct + "." + MANUALPOINTS +
                DOT_DATA);
            if (pointData != null && pointData.length() != 0
                && !pointData.equals("[]")) {
                String fName = ct + "." + MANUALPOINTS;
                ManualPointsFacet facet = new ManualPointsFacet(
                    0,
                    fName,
                    Resources.getMsg(meta, MANUALPOINTS, "Manual Points"));
                facets.add(facet);
                log.debug("compute(): ManualPointsFacet for "
                    + ct + " created");
            }
            else {
                log.debug("compute(): No points for " + ct);
            }

            // Handle lines.
            String linesData = points.getDataAsString(ct + "." + MANUALPOINTS +
                DOT_LINES);
            if (linesData != null && linesData.length() != 0
                && !linesData.equals("[]")) {
                try {
                    JSONArray lines = new JSONArray(linesData);
                    for (int i = 0, P = lines.length(); i < P; i++) {
                        JSONArray array = lines.getJSONArray(i);
                        double y    = array.getDouble(0);
                        String name = array.getString(1);
                        String fName = ct + "." + MANUALLINE;
                        log.debug("have facet: " + y + " / "
                            + name + " -> " + fName);
                        CrossSectionWaterLineFacet facet =
                            new CrossSectionWaterLineFacet(
                                i,
                                fName,
                                name);

                        facets.add(facet);
                    }
                }
                catch(JSONException e){
                    log.error("Could not decode json.");
                }

            }
            else {
                //log.debug("No points for " + ct);
            }
        }

        return null;
    }


    /** Call compute. */
    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
     ) {
        return compute(artifact, hash, meta, facets);
    }


    /** Call compute. */
    @Override
    public Object computeFeed(
        D4EArtifact artifact,
        String hash,
        CallContext context,
        List<Facet> facets,
        Object old
    ) {
        return compute(artifact, hash, context.getMeta(), facets);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
