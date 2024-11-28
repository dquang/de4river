/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.StaticWQKmsArtifact;

import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RelativePointFacet;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WQKmsFacet;

/**
 * Only state of WQKmsArtifact.
 */
public class StaticWQKmsState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(StaticWQKmsState.class);


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
        StaticWQKmsArtifact artifact,
        CallMeta      metaLocale,
        String        hash,
        List<Facet>   facets,
        Object        old
    ) {
        String id = getID();
        String code = artifact.getDataAsString("ids");
        String [] parts = code.split("-");

        // Return from cache, if present.
        WQKms res = old instanceof WQKms
            ? (WQKms)old
            : artifact.getWQKms();

        WQKms wqkms = res;

        if (facets == null) {
            return res;
        }

        String wkmsName = wqkms.getName();

        Facet wqfacet = new WQKmsFacet(
            STATIC_WQ,
            wkmsName);
        facets.add(wqfacet);

        if (parts[0].equals("officials_wq")) {
            // Early stop. We only want wq-points in that case.
            return res;
        }

        /*
         * TODO: re-enable HEIGHTMARKS_POINTS-thing

           String name;
           if (parts[0].equals(HEIGHTMARKS_POINTS)) {
               name = HEIGHTMARKS_POINTS;
           }
           else {
               name = STATIC_WQKMS;
           }
        */
        // Spawn Q Facet only if at least one discharge value
        // is != -1
        boolean qEmpty = true;
        TDoubleArrayList qs = wqkms.allQs();
        for (int i = 0, Q = qs.size(); i < Q; i++) {
            if (qs.getQuick(i) != -1d) {
                qEmpty = false;
                break;
            }
        }

        // issue1494: Only spawn qfacet if discharges are given
        if (!qEmpty) {
            Facet qfacet = new WQKmsFacet(
                STATIC_WQKMS_Q,
                wkmsName
                // TODO re-enable translations.
                /*
                Resources.getMsg(
                    metaLocale,
                    wkmsName,
                    wkmsName)*/);
            facets.add(qfacet);
        }

        Facet rpFacet = new RelativePointFacet(wkmsName);
        facets.add(rpFacet);

        Facet csFacet = new CrossSectionWaterLineFacet(0,
             wkmsName);
        facets.add(csFacet);

        if (!qEmpty) {
            wkmsName = "W (" + wkmsName + ")";
        }

        Facet wfacet = new WQKmsFacet(
            STATIC_WQKMS_W,
            wkmsName
            /*
            // TODO re-enable translations.
            Resources.getMsg(
                metaLocale,
                wkmsName,
                wkmsName)*/);
        facets.add(wfacet);

        Facet wstfacet = new DataFacet(
            WST,
            wkmsName,
            ComputeType.ADVANCE, hash, id);
        facets.add(wstfacet);

        return res;
    }

    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return compute((StaticWQKmsArtifact) artifact, context.getMeta(),
            hash, facets, old);
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
        return compute((StaticWQKmsArtifact) artifact, context.getMeta(),
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
        return compute((StaticWQKmsArtifact) artifact, meta,
            hash, facets, null);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
