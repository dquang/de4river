/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.sq;

import java.text.DateFormat;
import java.util.List;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.StaticSQRelationAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.sq.StaticSQContainer;
import org.dive4elements.river.artifacts.model.sq.StaticSQFactory;
import org.dive4elements.river.artifacts.model.sq.StaticSQRelation;
import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.river.artifacts.resources.Resources;

public class SQStaticState
extends StaticState
implements FacetTypes
{

    private static final Logger log =
        LogManager.getLogger(SQStaticState.class);

    private static final String FACET_DESCRIPTION =
        "facet.sq_relation.static_data";

    public SQStaticState() {
        super();
    }

    public SQStaticState(String name) {
        super(name);
    }

    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String hash,
        CallContext context,
        List<Facet> facets,
        Object old) {
        StaticSQContainer sqRelations;
        StaticSQRelationAccess access = new StaticSQRelationAccess(artifact);
        String river = access.getRiverName();
        String measurementStation = access.getMeasurementStation();

        int ms = -1;
        try {
            ms = Integer.parseInt(measurementStation);
        }
        catch (NumberFormatException nfe) {
            log.error("Unparseable measurement station: "
                + measurementStation);
            return null;
        }
        log.debug("Parsed measurement station: " + ms);

        sqRelations = StaticSQFactory.getSQRelationsForLocation(river, ms);
        DateFormat df = new SimpleDateFormat("yyyy");

        for (
            StaticSQRelation.Parameter p: StaticSQRelation.Parameter.values()
        ) {
            log.debug("parameter: " + p.toString());
            List<StaticSQRelation> relations =
                sqRelations.getRelationsByParameter(p);

            if (!relations.isEmpty()) {
                int count = 0;

                for (StaticSQRelation relation : relations) {
                    log.debug("add facet for " + p.toString().toLowerCase());
                    String name = "sq_" + p.toString().toLowerCase()
                        + "_curve";
                    String desc =
                        Resources.getMsg(context.getMeta(),
                            FACET_DESCRIPTION,
                            FACET_DESCRIPTION,
                            new Object[] {
                                df.format(relation.getStartTime()),
                                df.format(relation.getStopTime())});
                    facets.add(new StaticSQRelationFacet(
                        count,
                        name,
                        desc,
                        relation));
                    count++;
                }
            }
        }
        return null;
    }

    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        StaticSQContainer sqRelations;

        String id_string = artifact.getDataAsString("ids");

        int static_id = -1;
        String static_desc = null;

        if (id_string != null && !id_string.isEmpty()) {
            String[] id_parts = id_string.split(";");
            static_id = Integer.parseInt(id_parts[0]);
            if (id_parts.length > 1) {
                static_desc = id_parts[1];
            }
        }

        if (static_id != -1) {
            // If next line fails a traceback is the best debug output anyhow
            sqRelations = StaticSQFactory.getDistinctRelation(static_id);
            log.debug("Got a distinct relation" + sqRelations);
        } else {
            StaticSQRelationAccess access =
                new StaticSQRelationAccess(artifact);
            String river = access.getRiverName();
            String measurementStation = access.getMeasurementStation();

            int ms = -1;
            try {
                ms = Integer.parseInt(measurementStation);
            }
            catch (NumberFormatException nfe) {
                log.error("Unparseable measurement station: "
                    + measurementStation);
                return null;
            }
            log.debug("Parsed measurement station: " + ms);

            sqRelations = StaticSQFactory.getSQRelationsForLocation(river, ms);
        }

        DateFormat df = new SimpleDateFormat("yyyy");

        for (
            StaticSQRelation.Parameter p: StaticSQRelation.Parameter.values()
        ) {

            List<StaticSQRelation> relations =
                sqRelations.getRelationsByParameter(p);

            if (!relations.isEmpty()) {
                int count = 0;

                for (StaticSQRelation relation : relations) {
                    String name = "sq_" + p.toString().toLowerCase()
                        + "_curve";
                    String desc = static_desc == null ?
                        Resources.getMsg(meta,
                            FACET_DESCRIPTION,
                            FACET_DESCRIPTION,
                            new Object[] {
                                df.format(relation.getStartTime()),
                                df.format(relation.getStopTime())}) :
                            static_desc;
                    facets.add(new StaticSQRelationFacet(
                        count,
                        name,
                        desc,
                        relation));
                    count++;
                }
            }
        }
        return null;
    }
}
