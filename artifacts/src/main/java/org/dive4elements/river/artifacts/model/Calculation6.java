/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.TimeInterval;

import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess;
import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess.EvaluationMode;


/**
 * Historical Discharge Calculation.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Calculation6 extends Calculation {

    private static final Logger log = LogManager.getLogger(Calculation6.class);

    private int       mode;
    private long []   timerange;
    private double [] values;
    private Long      officialGaugeNumber;
    private String    riverName;


    public Calculation6(HistoricalDischargeAccess access) {
        EvaluationMode mode = access.getEvaluationMode();
        Timerange tr = access.getEvaluationTimerange();
        double [] vs = mode != null && mode == EvaluationMode.W
            ? access.getWs()
            : access.getQs();
        riverName = access.getRiverName();

        Long officialGaugeNumber = access.getOfficialGaugeNumber();

        if (mode == null) {
            // TODO: i18n
            addProblem("hist.discharge.mode.not.set");
        }
        if (tr == null) {
            // TODO: i18n
            addProblem("hist.discharge.time.interval.not.set");
        }
        if (vs == null || vs.length == 0) {
            // TODO: i18n
            addProblem("hist.discharge.values.not.set");
        }

        if (officialGaugeNumber == null) {
            // TODO: i18n
            addProblem("hist.discharge.reference.gauge.not.set");
        }

        if (!hasProblems()) {
            set(
                mode.getMode(),
                new long [] { tr.getStart(), tr.getEnd()},
                vs,
                officialGaugeNumber);
        }
    }

    protected void set(
        int       mode,
        long []   timerange,
        double [] values,
        Long      officialGaugeNumber
    ) {
        this.mode                = mode;
        this.timerange           = timerange;
        this.values              = values;
        this.officialGaugeNumber = officialGaugeNumber;
    }

    protected CalculationResult error(String msg) {
        addProblem(msg);
        return new CalculationResult(new HistoricalDischargeData(), this);
    }

    public CalculationResult calculate() {
        if (hasProblems()) {
            log.warn("Parameters not valid for calculation.");
            return null;
        }

        Gauge gauge = Gauge.getGaugeByOfficialNumber(officialGaugeNumber,
                riverName);
        if (gauge == null) {
            // TODO: i18n
            return error("hist.discharge.gauge.not.found");
        }

        if (log.isDebugEnabled()) {
            debug();
        }

        List<DischargeTable> dts = fetchDischargeTables(gauge);
        if (dts.isEmpty()) {
            return error("cannot.find.hist.q.tables");
        }

        DischargeTable refTable = fetchReferenceTable(dts);

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Take " + dts.size() + " into account.");
        }

        ValuesCache vc = new ValuesCache();

        WQTimerange [] wqt = prepareData(refTable, dts, vc);

        if (debug) {
            log.debug("Number of calculation results: " + wqt.length);
        }

        return new CalculationResult(new HistoricalDischargeData(wqt),
            this);
    }

    /** The youngest discharge table of the selected set is the reference */
    protected DischargeTable fetchReferenceTable(List<DischargeTable> dts) {
        DischargeTable ref = null;
        long now = System.currentTimeMillis();
        for (DischargeTable dt: dts) {
            if (ref == null) {
                ref = dt;
            }
            else {
                TimeInterval cti = dt.getTimeInterval();
                TimeInterval rti = ref.getTimeInterval();

                long ct = cti.getStopTime() != null
                    ? cti.getStopTime().getTime()
                    : now;
                long rt = rti.getStopTime() != null
                    ? rti.getStopTime().getTime()
                    : now;

                if (ct > rt) {
                    ref = dt;
                }

            }
        }
        return ref;
    }

    protected List<DischargeTable> fetchDischargeTables(Gauge gauge) {

        List<DischargeTable> all = gauge.getDischargeTables();
        List<DischargeTable> relevant =
            new ArrayList<DischargeTable>(all.size());

        for (DischargeTable dt: all) {
            if (isDischargeTableRelevant(dt)) {
                relevant.add(dt);
            }
        }

        return relevant;
    }

    /** True if timerange of given discharge table overlaps with timerange. */
    protected boolean isDischargeTableRelevant(DischargeTable dt) {

        TimeInterval ti = dt.getTimeInterval();

        if (dt.getKind() == Gauge.MASTER_DISCHARGE_TABLE || ti == null) {
            return false;
        }

        long dtStart = ti.getStartTime().getTime();
        long dtStop  = ti.getStopTime() != null
            ? ti.getStopTime().getTime()
            : System.currentTimeMillis();

        return !(timerange[1] < dtStart || timerange[0] > dtStop);
    }

    protected String name(double value) {
        return mode == EvaluationMode.W.getMode()
            ? "W=" + value
            : "Q=" + value;
    }

    /** With reference. */
    protected HistoricalWQTimerange[] prepareData(
        DischargeTable       refTable,
        List<DischargeTable> dts,
        ValuesCache          vc
    ) {
        List<HistoricalWQTimerange> wqts =
            new ArrayList<HistoricalWQTimerange>(values.length);

        boolean debug = log.isDebugEnabled();

        for (double value: values) {
            if (debug) {
                log.debug("Prepare data plus diff for value: " + value);
            }

            double ref = mode == EvaluationMode.W.getMode()
                ? vc.findValueForW(refTable, value)
                : vc.findValueForQ(refTable, value);

            if (Double.isNaN(ref)) {
                addProblem("hist.discharge.no.value.in.ref", value,
                           mode == EvaluationMode.W.getMode()
                           ? "cm"
                           : "m\u00b3/s");
                continue;
            }

            String name = name(value);
            HistoricalWQTimerange wqt = null;

            for (DischargeTable dt : dts) {
                Date[] ti = prepareTimeInterval(dt);
                Timerange t = new Timerange(ti[0], ti[1]);
                double w;
                double q;
                double diff;

                if (mode == EvaluationMode.W.getMode()) {
                    q = vc.findValueForW(dt, w = value);

                    if (Double.isNaN(q)) {
                        log.warn("Cannot find Q for W: " + w);
                        addProblem("cannot.find.hist.q.for.w", w, ti[0], ti[1]);
                        continue;
                    }

                    diff = ref - q;
                }
                else {
                    w = vc.findValueForQ(dt, q = value);

                    if (Double.isNaN(w)) {
                        log.warn("Cannot find W for Q: " + q);
                        addProblem("cannot.find.hist.w.for.q", q, ti[0], ti[1]);
                        continue;
                    }
                    diff = ref - w;
                }

                if (debug) {
                    log.debug("Q=" + q + " | W=" + w + " | Ref = " + ref);
                }

                if (wqt == null) {
                    wqt = new HistoricalWQTimerange(name);
                }

                wqt.add(w, q, diff, t);
            }

            if (wqt != null) {
                wqts.add(wqt);
            }
        }

        return (HistoricalWQTimerange[])wqts.toArray(
            new HistoricalWQTimerange[wqts.size()]);
    }

    /** Returns discharge table interval as Date[]. */
    protected Date[] prepareTimeInterval(DischargeTable dt) {
        TimeInterval ti = dt.getTimeInterval();

        Date start = ti.getStartTime();
        Date end = ti.getStopTime();

        if (end == null) {
            log.warn("TimeInterval has no stop time set!");

            end = new Date();
        }

        return new Date[] { start, end };
    }


    /** Helper to avoid redundant loading of discharge table values. */
    private static final class ValuesCache {

        private Map<Integer, double[][]> cache;

        ValuesCache() {
            cache = new HashMap<Integer, double [][]>();
        }

        double [][] getValues(DischargeTable dt) {
            Integer id = dt.getId();
            double [][] vs = cache.get(id);
            if (vs == null) {
                vs = DischargeTables.loadDischargeTableValues(dt);
                cache.put(id, vs);
            }
            return vs;
        }

        private static double firstOrNaN(double [] vs) {
            return vs.length > 0 ? vs[0] : Double.NaN;
        }

        double findValueForW(DischargeTable dt, double w) {
            return firstOrNaN(DischargeTables.getQsForW(getValues(dt), w));
        }

        double findValueForQ(DischargeTable dt, double q) {
            return firstOrNaN(DischargeTables.getWsForQ(getValues(dt), q));
        }
    } // class ValuesCache

    /**
     * Writes the parameters used for this calculation to log.
     */
    public void debug() {
        log.debug("========== Calculation6 ==========");
        log.debug("   Mode:         " + mode);
        log.debug("   Timerange:    " + timerange[0] + " - " + timerange[1]);
        log.debug("   Input values: " + Arrays.toString(values));
        log.debug("==================================");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
