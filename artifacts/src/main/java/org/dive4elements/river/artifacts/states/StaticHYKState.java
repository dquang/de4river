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
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.HYKArtifact;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.HYKFacet;
import org.dive4elements.river.artifacts.model.HYKFactory;

/**
 * Only state of a HYKArtifact.
 */
public class StaticHYKState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    transient private static final Logger log = LogManager.getLogger(
        StaticHYKState.class);


    /**
     * From this state can not be continued.
     */
    @Override
    protected String getUIProvider() {
        return "noinput";
    }


    /**
     * Compute, create Facets, do the same stuff as all the other states do.
     */
    protected Object compute(
        HYKArtifact   hyk,
        CallMeta      metaLocale,
        String        hash,
        List<Facet>   facets,
        Object        old
    ) {
        log.debug("StaticHYKState.compute");
        String id = getID();

        // Prepare comparison against cached result.
        List<HYKFactory.Zone> resZones = old instanceof List
            ? (List<HYKFactory.Zone>)old
            : null;

        // TODO Compare against cached object.

        // Get Zones from HYKFactory
        List<HYKFactory.Zone> zones = (List<HYKFactory.Zone>)
            HYKFactory.getHYKs(hyk.getHykId(), hyk.getKm());

        if (facets == null) {
            log.debug("StaticHYKState.compute no facets");
            return zones;
        }

        // Spawn Facets.
        Facet facet = new HYKFacet(0, HYKFactory.getHykName(hyk.getHykId()));
        facets.add(facet);

        return zones;
    }


    /**
     * Get data, create the facets.
     *
     * @param context Ignored.
     */
    @Override
    public Object computeFeed(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return compute((HYKArtifact) artifact, context.getMeta(),
            hash, facets, old);
    }


    /**
     * Create the facets.
     * @param context Ignored.
     */
    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        return compute((HYKArtifact) artifact, meta, hash, facets,
            null);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
