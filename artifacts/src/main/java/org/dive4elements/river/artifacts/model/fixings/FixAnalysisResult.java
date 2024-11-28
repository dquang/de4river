/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import gnu.trove.TIntObjectHashMap;

import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.utils.KMIndex;

public class FixAnalysisResult
extends      FixResult
{
    private static Logger log =
        LogManager.getLogger(FixAnalysisResult.class);

    protected KMIndex<AnalysisPeriod []> analysisPeriods;

    public FixAnalysisResult() {
    }

    public FixAnalysisResult(
        Parameters                 parameters,
        KMIndex<QWD []>            referenced,
        KMIndex<QWI []>            outliers,
        KMIndex<AnalysisPeriod []> analysisPeriods
    ) {
        super(parameters, referenced, outliers);
        this.analysisPeriods = analysisPeriods;
    }

    public int getUsedSectorsInAnalysisPeriods() {
        int result = 0;
        for (KMIndex.Entry<AnalysisPeriod []> entry: analysisPeriods) {
            for (AnalysisPeriod period: entry.getValue()) {
                for (int i = 0; i < 4; ++i) {
                    result |= period.getQSectorAverage(i) != null
                        ? (1 << i)
                        : 0;
                }
                // XXX: Stop early on result == ~(~0 << 4)) ?
            }
        }
        return result;
    }


    public void makeAnalysisEventsUnique() {
        TIntObjectHashMap dums = new TIntObjectHashMap();

        for (KMIndex.Entry<AnalysisPeriod []> entry: analysisPeriods) {
            AnalysisPeriod [] aps = entry.getValue();
            for (int i = 0; i < aps.length; ++i) {
                AnalysisPeriod ap = aps[i];
                QWD [] qwds = ap.getQWDs();
                if (qwds == null) {
                    continue;
                }
                DateUniqueMaker dum = (DateUniqueMaker)dums.get(i);
                if (dum == null) {
                    dums.put(i, dum = new DateUniqueMaker());
                }
                for (QWD qwd: qwds) {
                    dum.makeUnique(qwd);
                }
            }
        }
    }

    public Collection<Date> getAnalysisEventsDates(int analysisPeriod) {
        TreeSet<Date> dates = new TreeSet<Date>();
        for (KMIndex.Entry<AnalysisPeriod []> entry: analysisPeriods) {
            QWD [] qwds = entry.getValue()[analysisPeriod].getQWDs();
            if (qwds != null) {
                for (QWD qwd: qwds) {
                    dates.add(qwd.date);
                }
            }
        }
        return dates;
    }

    public Collection<Integer> getAnalysisEventsIndices(int analysisPeriod) {
        TreeMap<Date, Integer> dates = new TreeMap<Date, Integer>();
        for (KMIndex.Entry<AnalysisPeriod []> entry: analysisPeriods) {
            QWD [] qwds = entry.getValue()[analysisPeriod].getQWDs();
            if (qwds != null) {
                for (QWD qwd: qwds) {
                    dates.put(qwd.date, qwd.index);
                }
            }
        }
        return dates.values();
    }

    public void remapAnalysisEventsIndicesToRank(int analysisPeriod) {
        RankRemapper remapper = new RankRemapper();
        for (Integer index: getAnalysisEventsIndices(analysisPeriod)) {
            remapper.toMap(index);
        }
        for (KMIndex.Entry<AnalysisPeriod []> entry: analysisPeriods) {
            QWD [] qwds = entry.getValue()[analysisPeriod].getQWDs();
            if (qwds != null) {
                for (QWD qwd: qwds) {
                    remapper.remap(qwd);
                }
            }
        }
    }

    public KMIndex<AnalysisPeriod []> getAnalysisPeriods() {
        return analysisPeriods;
    }

    public void setAnalysisPeriods(KMIndex<AnalysisPeriod []> analysisPeriods) {
        this.analysisPeriods = analysisPeriods;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
