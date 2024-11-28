/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.access.FixAnalysisAccess;

import org.dive4elements.river.artifacts.math.fitting.Function;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DateRange;

import org.dive4elements.river.artifacts.model.FixingsOverview.AndFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.DateRangeFilter;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing.Filter;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing;
import org.dive4elements.river.artifacts.model.FixingsOverview.IdsFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.KmFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.SectorFilter;

import org.dive4elements.river.artifacts.model.FixingsOverview;
import org.dive4elements.river.artifacts.model.Parameters;
import org.dive4elements.river.artifacts.model.Range;

import org.dive4elements.river.utils.DateAverager;
import org.dive4elements.river.utils.KMIndex;

import gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FixAnalysisCalculation
extends      FixCalculation
{
    private static Logger log = LogManager.getLogger(FixAnalysisCalculation.class);

    protected DateRange    referencePeriod;
    protected DateRange [] analysisPeriods;

    public FixAnalysisCalculation() {
    }

    public FixAnalysisCalculation(FixAnalysisAccess access) {
        super(access);

        DateRange    referencePeriod = access.getReferencePeriod();
        DateRange [] analysisPeriods = access.getAnalysisPeriods();

        if (referencePeriod == null) {
            addProblem("fix.missing.reference.period");
        }

        if (analysisPeriods == null || analysisPeriods.length < 1) {
            addProblem("fix.missing.analysis.periods");
        }

        if (!hasProblems()) {
            this.referencePeriod = referencePeriod;
            this.analysisPeriods = analysisPeriods;
        }
    }

    @Override
    public CalculationResult innerCalculate(
        FixingsOverview overview,
        Function        func
    ) {
        ColumnCache cc = new ColumnCache();

        FitResult fitResult = doFitting(overview, cc, func);

        if (fitResult == null) {
            return new CalculationResult(this);
        }

        KMIndex<AnalysisPeriod []> analysisPeriods =
            calculateAnalysisPeriods(
                func,
                fitResult.getParameters(),
                overview,
                cc);

        analysisPeriods.sort();

        FixAnalysisResult far = new FixAnalysisResult(
            fitResult.getParameters(),
            fitResult.getReferenced(),
            fitResult.getOutliers(),
            analysisPeriods);

        // Workaraound to deal with same dates in data set
        far.makeAnalysisEventsUnique();
        for (int i = 0; i < this.analysisPeriods.length; ++i) {
            far.remapAnalysisEventsIndicesToRank(i);
        }

        return new CalculationResult(far, this);
    }

    @Override
    protected Filter createFilter() {
        Filter ids = super.createFilter();
        DateRangeFilter rdf = new DateRangeFilter(
            referencePeriod.getFrom(),
            referencePeriod.getTo());
        return new AndFilter().add(rdf).add(ids);
    }

    protected KMIndex<AnalysisPeriod []> calculateAnalysisPeriods(
        Function        function,
        Parameters      parameters,
        FixingsOverview overview,
        ColumnCache     cc
    ) {
        Range range = new Range(from, to);

        int kmIndex   = parameters.columnIndex("km");
        int maxQIndex = parameters.columnIndex("max_q");

        double [] wq = new double[2];

        int [] parameterIndices =
            parameters.columnIndices(function.getParameterNames());

        double [] parameterValues = new double[parameterIndices.length];

        DateAverager dateAverager = new DateAverager();

        KMIndex<AnalysisPeriod []> results =
            new KMIndex<AnalysisPeriod []>(parameters.size());

        IdsFilter idsFilter = new IdsFilter(events);

        TIntIntHashMap [] col2indices =
            new TIntIntHashMap[analysisPeriods.length];

        DateRangeFilter [] drfs = new DateRangeFilter[analysisPeriods.length];

        boolean debug = log.isDebugEnabled();

        for (int i = 0; i < analysisPeriods.length; ++i) {
            col2indices[i] = new TIntIntHashMap();
            drfs[i] = new DateRangeFilter(
                analysisPeriods[i].getFrom(),
                analysisPeriods[i].getTo());

            if (debug) {
                log.debug("Analysis period " + (i+1) + " date range: " +
                    analysisPeriods[i].getFrom() + " - " +
                    analysisPeriods[i].getTo());
            }
        }

        for (int row = 0, R = parameters.size(); row < R; ++row) {
            double km = parameters.get(row, kmIndex);
            parameters.get(row, parameterIndices, parameterValues);

            // This is the parameterized function for a given km.
            org.dive4elements.river.artifacts.math.Function instance =
                function.instantiate(parameterValues);

            KmFilter kmFilter = new KmFilter(km);

            ArrayList<AnalysisPeriod> periodResults =
                new ArrayList<AnalysisPeriod>(analysisPeriods.length);

            for (int ap = 0; ap < analysisPeriods.length; ++ap) {
                DateRange analysisPeriod = analysisPeriods[ap];
                TIntIntHashMap col2index = col2indices[ap];

                DateRangeFilter drf = drfs[ap];

                QWD []    qSectorAverages = new QWD[4];
                double [] qSectorStdDevs  = new double[4];

                ArrayList<QWD> allQWDs = new ArrayList<QWD>();

                // for all Q sectors.
                for (int qSector = qSectorStart;
                     qSector <= qSectorEnd;
                     ++qSector
                ) {
                    Filter filter = new AndFilter()
                        .add(kmFilter)
                        .add(new SectorFilter(qSector))
                        .add(drf)
                        .add(idsFilter);

                    List<Fixing.Column> metas = overview.filter(range, filter);

                    if (metas.isEmpty()) {
                        // No fixings for km and analysis period
                        continue;
                    }

                    double sumQ = 0.0;
                    double sumW = 0.0;

                    StandardDeviation stdDev = new StandardDeviation();

                    List<QWD> qwds = new ArrayList<QWD>(metas.size());

                    dateAverager.clear();

                    for (Fixing.Column meta: metas) {
                        if (meta.findQSector(km) != qSector) {
                            // Ignore not matching sectors.
                            continue;
                        }

                        Column column = cc.getColumn(meta);
                        if (column == null || !column.getQW(km, wq)) {
                            continue;
                        }

                        double fw = instance.value(wq[1]);
                        if (Double.isNaN(fw)) {
                            continue;
                        }

                        double dw = (wq[0] - fw)*100.0;

                        stdDev.increment(dw);

                        Date date = column.getDate();
                        String description = column.getDescription();

                        QWD qwd = new QWD(
                            wq[1], wq[0],
                            description,
                            date, true,
                            dw, getIndex(col2index, column.getIndex()));

                        qwds.add(qwd);

                        sumW += wq[0];
                        sumQ += wq[1];

                        dateAverager.add(date);
                    }

                    // Calulate average per Q sector.
                    int N = qwds.size();
                    if (N > 0) {
                        allQWDs.addAll(qwds);
                        double avgW = sumW / N;
                        double avgQ = sumQ / N;

                        double avgFw = instance.value(avgQ);
                        if (!Double.isNaN(avgFw)) {
                            double avgDw = (avgW - avgFw)*100.0;
                            Date avgDate = dateAverager.getAverage();

                            String avgDescription = "avg.deltawt." + qSector;

                            QWD avgQWD = new QWD(
                                avgQ, avgW, avgDescription,
                                avgDate, true, avgDw, 0);

                            qSectorAverages[qSector] = avgQWD;
                        }
                        qSectorStdDevs[qSector] = stdDev.getResult();
                    }
                    else {
                        qSectorStdDevs[qSector] = Double.NaN;
                    }
                } // for all Q sectors

                QWD [] aqwds = allQWDs.toArray(new QWD[allQWDs.size()]);

                AnalysisPeriod periodResult = new AnalysisPeriod(
                    analysisPeriod,
                    aqwds,
                    qSectorAverages,
                    qSectorStdDevs);
                periodResults.add(periodResult);
            }

            double maxQ = -Double.MAX_VALUE;
            for (AnalysisPeriod ap: periodResults) {
                double q = ap.getMaxQ();
                if (q > maxQ) {
                    maxQ = q;
                }
            }

            double oldMaxQ = parameters.get(row, maxQIndex);
            if (oldMaxQ < maxQ) {
                parameters.set(row, maxQIndex, maxQ);
            }

            AnalysisPeriod [] rap = new AnalysisPeriod[periodResults.size()];
            periodResults.toArray(rap);
            results.add(km, rap);
        }

        return results;
    }

    /** Returns the mapped value of colIdx or the size of the hashmap. */
    private static final int getIndex(TIntIntHashMap map, int colIdx) {
        if (map.containsKey(colIdx)) {
            return map.get(colIdx);
        }
        int index = map.size();
        map.put(colIdx, index);
        return index;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
