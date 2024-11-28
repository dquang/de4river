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

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.QSectorArtifact;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.QSectorFacet;

import org.dive4elements.river.artifacts.resources.Resources;

/**
 * The only state for an QSectorArtifact.
 */
public class QSectorSingleState
extends      DefaultState
implements   FacetTypes
{
    /** Developer-centric description of facet. */
    public static final String I18N_DESCRIPTION
        = "facet.qsector";

    /** The log that is used in this state. */
    private static final Logger log =
        LogManager.getLogger(QSectorSingleState.class);


    /**
     * Add QSectorFacets to list of Facets.
     *
     * @param artifact Ignored.
     * @param hash Ignored.
     * @param meta CallMeta to be used for internationalization.
     * @param facets List to add QSectorFacet to.
     *
     * @return null.
     */
    public Object compute(
        D4EArtifact artifact,
        String       hash,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        log.debug("QSectorSingleState.compute()");
        QSectorArtifact points = (QSectorArtifact) artifact;

        QSectorFacet qfacet = new QSectorFacet(
            0,
            QSECTOR,
            Resources.getMsg(meta, "qsectors", "Q Sectors"));

        facets.add(qfacet);

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
