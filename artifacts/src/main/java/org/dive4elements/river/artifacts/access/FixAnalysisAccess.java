/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.DateRange;

import java.util.Arrays;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FixAnalysisAccess
extends      FixAccess
{
    private static Logger log = LogManager.getLogger(FixAnalysisAccess.class);

    protected DateRange    referencePeriod;
    protected DateRange [] analysisPeriods;

    protected double [] qs;

    public FixAnalysisAccess(D4EArtifact artifact) {
        super(artifact);
    }

    /** Access the reference date period, return null in case of 'errors'. */
    public DateRange getReferencePeriod() {
        if (referencePeriod == null) {
            StateData refStart = artifact.getData("ref_start");
            StateData refEnd   = artifact.getData("ref_end");

            if (refStart == null || refEnd == null) {
                log.warn("missing 'ref_start' or 'ref_start' value");
                return null;
            }

            try {
                long rs = Long.parseLong((String)refStart.getValue());
                long re = Long.parseLong((String)refEnd  .getValue());

                if (rs > re) { long t = rs; rs = re; re = t; }

                Date from = new Date(rs);
                Date to   = new Date(re);
                referencePeriod = new DateRange(from, to);
            }
            catch (NumberFormatException nfe) {
                log.warn("ref_start or ref_end is not an integer.");
            }
        }

        return referencePeriod;
    }

    public DateRange [] getAnalysisPeriods() {
        if (analysisPeriods == null) {
            analysisPeriods = getDateRange("ana_data");
        }

        return analysisPeriods;
    }

    /**
     * @return DateRange object ranging from eldest to youngest date
     * of analysis and reference periods.
     */
    public DateRange getDateRange() {
        DateRange refP = getReferencePeriod();

        if (refP == null) {
            return null;
        }

        Date from = refP.getFrom();
        Date to   = refP.getTo();

        DateRange[] rs = getAnalysisPeriods();
        for (DateRange r: rs) {
            if (r.getFrom().before(from)) {
                from = r.getFrom();
            }
            if (r.getTo().after(to)) {
                to = r.getTo();
            }
        }

        return new DateRange(from, to);
    }

    public double [] getQs() {
        if (qs == null) {
            qs = getDoubleArray("qs");
        }

        if (log.isDebugEnabled() && qs != null) {
            log.debug("qs: " + Arrays.toString(qs));
        }
        return qs;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
