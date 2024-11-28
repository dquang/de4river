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
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.utils.KMIndex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Facet to show W values for Q values at km for a date.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixAnalysisPeriodsFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixAnalysisPeriodsFacet.class);

    /** Trivial Constructor. */
    public FixAnalysisPeriodsFacet() {
    }


    /**
     * @param name
     */
    public FixAnalysisPeriodsFacet(int index, String name, String description) {
        super(index,
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
     * @param context  the CallContext (ignored).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("FixAnalysisPeriodsFacet.getData");

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
                return null;
            }

            AnalysisPeriod[] periods = kmPeriodsEntry.getValue();

            if (periods == null) {
                return null;
            }
            DateRange[] dates = new DateRange[periods.length];
            for (int i = 0; i < periods.length; i++) {
                dates[i] = periods[i].getDateRange();
            }
            return dates;
        }
        else {
            log.debug("Not an instance of FixationArtifact.");
            return null;
        }
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixAnalysisPeriodsFacet deepCopy() {
        FixAnalysisPeriodsFacet copy = new FixAnalysisPeriodsFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
