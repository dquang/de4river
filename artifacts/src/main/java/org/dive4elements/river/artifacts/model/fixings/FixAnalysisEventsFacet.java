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

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.utils.KMIndex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Facet to show W values for Q values at km for a date.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixAnalysisEventsFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixAnalysisEventsFacet.class);

    /** Trivial Constructor. */
    public FixAnalysisEventsFacet() {
    }


    /**
     * @param name
     */
    public FixAnalysisEventsFacet(int index, String name, String description) {
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
        log.debug("FixAnalysisEventsFacet.getData");

        if (!(artifact instanceof D4EArtifact)) {
            log.debug("Not an instance of FixationArtifact.");
            return null;
        }
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
            log.debug("getData: kmPeriodsEntry == null");
            return null;
        }

        AnalysisPeriod[] periods = kmPeriodsEntry.getValue();
        if (periods == null) {
            log.debug("getData: periods == null");
            return null;
        }
        int ndx = index >> 8;
        QWD[] qwdData = periods[ndx].getQWDs();
        if (qwdData == null) {
            return null;
        }
        int ndy = index & 255;

        for (QWD qwd: qwdData) {
            if (qwd.getIndex() == ndy) {
                return qwd;
            }
        }
        return null;
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixAnalysisEventsFacet deepCopy() {
        FixAnalysisEventsFacet copy = new FixAnalysisEventsFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
