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

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.model.AreaFacet;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.AreaArtifact;
import org.dive4elements.river.artifacts.model.FacetTypes;


/** Trivial state to create areafacets, no caching. */
public class AreaCreationState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(AreaCreationState.class);


    /**
     * From this state can only be continued trivially.
     */
    @Override
    protected String getUIProvider() {
        return "continue";
    }


    /** Just reproduce the Facet. */
    protected Object compute(
        D4EArtifact  areaArtifact,
        CallContext   cc,
        String        hash,
        List<Facet>   facets,
        Object        old
    ) {
        log.debug("AreaCreationState.compute");

        if (facets != null) {
            AreaArtifact aArt = (AreaArtifact) areaArtifact;

            facets.add(
                new AreaFacet(0, aArt.getFacetName(), aArt.getAreaName()));
        }

        // TODO use compute to exploit caching strategies.

        return null;
    }


    /**
     */
    @Override
    public Object computeFeed(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return compute((D4EArtifact) artifact, context, hash, facets, old);
    }


    /**
     *
     */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return compute((D4EArtifact) artifact, context, hash, facets, old);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
