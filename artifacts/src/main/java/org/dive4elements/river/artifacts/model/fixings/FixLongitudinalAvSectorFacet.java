/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.utils.KMIndex;

public class FixLongitudinalAvSectorFacet
extends DataFacet
implements FacetTypes {

    /** House log. */
    private static Logger log =
        LogManager.getLogger(FixLongitudinalAvSectorFacet.class);

    /** Trivial Constructor. */
    public FixLongitudinalAvSectorFacet() {
    }


    public FixLongitudinalAvSectorFacet(
        int ndx,
        String name,
        String description)
    {
        super(
            ndx,
            name,
            description,
            ComputeType.ADVANCE,
            null,
            null);
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext.
     *
     * @return the data as KMIndex.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("FixLongitudinalAvSectorFacet.getData");

        if (artifact instanceof D4EArtifact) {
            D4EArtifact flys = (D4EArtifact)artifact;

            CalculationResult res =
                (CalculationResult) flys.compute(context,
                                                 ComputeType.ADVANCE,
                                                 false);

            FixAnalysisResult result = (FixAnalysisResult) res.getData();

            KMIndex<AnalysisPeriod []> kmPeriods = result.getAnalysisPeriods();
            if (kmPeriods == null) {
                log.warn("No analysis periods found.");
                return null;
            }
            int periodNdx = index >> 2;
            KMIndex<AnalysisPeriod> resPeriods =
                    new KMIndex<AnalysisPeriod>();
            for (KMIndex.Entry<AnalysisPeriod[]> entry: kmPeriods) {
                AnalysisPeriod ap = entry.getValue()[periodNdx];
                resPeriods.add(entry.getKm(), ap);
            }

            return resPeriods;
        }
        else {
            log.warn("Artifact is no instance of D4EArtifact.");
            return null;
        }
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixLongitudinalAvSectorFacet deepCopy() {
        FixLongitudinalAvSectorFacet copy = new FixLongitudinalAvSectorFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :