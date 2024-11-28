/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.model.QWDDateRange;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.utils.KMIndex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Facet to show average W values for Q sectors.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixAvSectorFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixAvSectorFacet.class);

    /** Trivial Constructor. */
    public FixAvSectorFacet() {
    }


    public FixAvSectorFacet(int ndx, String name, String description) {
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
     * @return the data as QWD array (QWD[]).
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("FixAvSectorFacet.getData");

        if (artifact instanceof D4EArtifact) {
            D4EArtifact flys = (D4EArtifact)artifact;

            CalculationResult res =
                (CalculationResult) flys.compute(context,
                                                 ComputeType.ADVANCE,
                                                 false);

            FixAnalysisResult result = (FixAnalysisResult) res.getData();

            double currentKm = getCurrentKm(context);
            KMIndex<AnalysisPeriod []> kmPeriods = result.getAnalysisPeriods();
            KMIndex.Entry<AnalysisPeriod []> kmPeriodsEntry =
                kmPeriods.binarySearch(currentKm);

            if (kmPeriodsEntry == null) {
                log.warn("No analysis periods found for km '"
                    + currentKm + "'");
                return null;
            }

            AnalysisPeriod[] periods = kmPeriodsEntry.getValue();

            if (periods == null) {
                log.warn("No analysis periods specified!");
                return null;
            }

            QWD[] qwdData = null;
            int sectorNdx = index & 3;
            int periodNdx = index >> 2;

            if (periodNdx < periods.length) {
                qwdData = periods[periodNdx].getQSectorAverages();
            }

            if (log.isDebugEnabled()) {
                int resSize = qwdData != null ? qwdData.length : -1;
                log.debug("Found " + resSize + " result elements.");
            }

            if (qwdData == null) {
                return null;
            }
            return new QWDDateRange(
                qwdData[sectorNdx], periods[periodNdx].getDateRange());
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
    public FixAvSectorFacet deepCopy() {
        FixAvSectorFacet copy = new FixAvSectorFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
