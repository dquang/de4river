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

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.AnnotationArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.AnnotationFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.resources.Resources;


/**
 * The only state for an AnnotationArtifact (River is known).
 */
public class AnnotationRiverState
extends      DefaultState
implements   FacetTypes
{
    /** Developer-centric description of facet. */
    public static final String I18N_DESCRIPTION =
        "facet.longitudinal_section.annotations";

    /** The log that is used in this state. */
    private static final Logger log = LogManager.getLogger(
        AnnotationRiverState.class);


    /**
     * Add an AnnotationFacet to list of Facets.
     *
     * @param artifact Ignored.
     * @param hash Ignored.
     * @param context Ignored.
     * @param meta CallMeta to be used for internationalization.
     * @param facets List to add AnnotationFacet to.
     *
     * @return null.
     */
    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        log.debug("AnnotationRiverState.computeInit()");

        AnnotationArtifact annotationArtifact = (AnnotationArtifact) artifact;

        String facetName = annotationArtifact.getFilterName();
        if (facetName == null) {
            facetName = Resources.getMsg(meta, I18N_DESCRIPTION);
        }

        AnnotationFacet facet = new AnnotationFacet(
            0,
            LONGITUDINAL_ANNOTATION,
            facetName);
        facets.add(facet);

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
