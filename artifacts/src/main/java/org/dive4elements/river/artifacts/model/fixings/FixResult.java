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
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.utils.KMIndex;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

public class FixResult
implements   Serializable
{
    private static Logger log =
        LogManager.getLogger(FixResult.class);

    protected Parameters      parameters;
    protected KMIndex<QWD []> referenced;
    protected KMIndex<QWI []> outliers;

    public FixResult() {
    }

    public FixResult(
        Parameters      parameters,
        KMIndex<QWD []> referenced,
        KMIndex<QWI []> outliers
    ) {
        this.parameters = parameters;
        this.referenced = referenced;
        this.outliers   = outliers;
    }

    public KMIndex<QWD []> getReferenced() {
        return referenced;
    }

    public void setReferenced(KMIndex<QWD []> referenced) {
        this.referenced = referenced;
    }

    public void makeReferenceEventsDatesUnique() {
        DateUniqueMaker dum = new DateUniqueMaker();
        for (KMIndex.Entry<QWD []> entry: referenced) {
            for (QWD ref: entry.getValue()) {
                dum.makeUnique(ref);
            }
        }
    }

    public Collection<Integer> getReferenceEventsIndices() {
        TreeMap<Date, Integer> dates = new TreeMap<Date, Integer>();
        for (KMIndex.Entry<QWD []> entry: referenced) {
            for (QWD value: entry.getValue()) {
                dates.put(value.date, value.index);
            }
        }
        return dates.values();
    }

    public void remapReferenceIndicesToRank() {
        RankRemapper remapper = new RankRemapper();
        for (Integer idx: getReferenceEventsIndices()) {
            remapper.toMap(idx);
        }
        for (KMIndex.Entry<QWD []> entry: referenced) {
            for (QWD value: entry.getValue()) {
                remapper.remap(value);
            }
        }
    }

    public Collection<Date> getReferenceEventsDates() {
        TreeSet<Date> dates = new TreeSet<Date>();
        for (KMIndex.Entry<QWD []> entry: referenced) {
            for (QWD qwd: entry.getValue()) {
                dates.add(qwd.date);
            }
        }
        return dates;
    }


    public KMIndex<QWI []> getOutliers() {
        return outliers;
    }

    public void setOutliers(KMIndex<QWI []> outliers) {
        this.outliers = outliers;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
