/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.DataProvider;
import org.dive4elements.river.artifacts.HYKArtifact;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.model.FastCrossSectionLine;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Trival Facet for HYKs
 */
public class HYKFacet
extends      DataFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(HYKFacet.class);

    /** Trivial constructor, set (maybe localized) description. */
    public HYKFacet(int idx, String description) {
        super(idx, HYK, description, ComputeType.FEED, null, null);
    }


    /**
     * Set km from cross section- master to HYKArtifact, then fire up
     * computation.
     *
     * @param art artifact to get data from.
     * @param context ignored
     */
     @Override
    public Object getData(Artifact art, CallContext context) {
        log.debug("HYKFacet.getData");

        String dataKey = CrossSectionFacet.BLACKBOARD_CS_MASTER_DATA;

        List<DataProvider> providers = context.getDataProvider(dataKey);
        if (providers.size() < 1) {
            log.warn("Could not find Cross-Section data provider "
                + "to get master cs km.");
            return null;
        }

        FastCrossSectionLine crossSection =
            (FastCrossSectionLine)providers.get(0).provideData(
                dataKey, null, context);

        if(crossSection == null) {
            log.debug("getData: crossSection is null");
            return null;
        }

        double km = crossSection.getKm();
        log.debug("HYKFacet.getData: Master Cross Section is at km: " + km);

        // Set this km at hyk artifact to be evaluated.
        HYKArtifact hyk = (HYKArtifact) art;
        hyk.setKm(km);

        return hyk.compute(context, hash, stateId, type, false);
    }


    /** Do a deep copy. */
    @Override
    public Facet deepCopy() {
        HYKFacet copy = new HYKFacet(this.index, this.description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
