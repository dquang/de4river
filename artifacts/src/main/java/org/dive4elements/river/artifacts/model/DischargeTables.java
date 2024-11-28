/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.DischargeTableValue;

import gnu.trove.TDoubleArrayList;

/** Documentation goes here. */
public class DischargeTables
implements   Serializable
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(DischargeTables.class);

    public static final int MASTER = 0;

    protected List<String> gaugeNames;

    protected String riverName;

    protected int    kind;

    protected Map<String, double [][]> values;

    public DischargeTables() {
    }

    public DischargeTables(String riverName, String gaugeName) {
        this(riverName, gaugeName, MASTER);
    }

    public DischargeTables(String riverName, String gaugeName, int kind) {
        this(riverName, new String [] { gaugeName }, kind);
    }

    public DischargeTables(String riverName, String [] gaugeNames) {
        this(riverName, gaugeNames, MASTER);
    }

    public DischargeTables(String riverName, String [] gaugeNames, int kind) {
        this(riverName, Arrays.asList(gaugeNames), kind);
    }

    public DischargeTables(
        String       riverName,
        List<String> gaugeNames,
        int          kind
    ) {
        this.kind       = kind;
        this.riverName  = riverName;
        this.gaugeNames = gaugeNames;
    }

    public double [][] getFirstTable() {
        Map<String, double [][]> values = getValues();
        for (double [][] table: values.values()) {
            return table;
        }
        return null;
    }

    public Map<String, double [][]> getValues() {
        if (values == null) {
            values = loadValues();
        }
        return values;
    }

    /**
     * Returns mapping of gauge name to values.
     */
    protected Map<String, double [][]> loadValues() {
        Map<String, double [][]> values = new HashMap<String, double [][]>();

        Session session = SessionHolder.HOLDER.get();

        Query gaugeQuery = session.createQuery(
            "from Gauge where name=:gauge and river.name=:river");
        gaugeQuery.setParameter("river", riverName);

        for (String gaugeName: gaugeNames) {
            gaugeQuery.setParameter("gauge", gaugeName);
            List<Gauge> gauges = gaugeQuery.list();
            if (gauges.isEmpty()) {
                log.warn(
                    "no gauge '"+gaugeName+"' at river '"+riverName+"'");
                continue;
            }
            Gauge gauge = gauges.get(0);

            List<DischargeTable> tables = gauge.getDischargeTables();

            if (tables.isEmpty()) {
                log.warn(
                    "no discharge table for gauge '" + gaugeName + "'");
                continue;
            }

            // TODO: Filter by time interval
            DischargeTable table = null;
            for (DischargeTable dt : tables) {
                if (dt.getKind() == 0) {
                    table = dt;
                    break;
                }
            }
            if (table == null) {
                table = tables.get(0);
            }
            double [][] vs = loadDischargeTableValues(table);

            values.put(gaugeName, vs);
        }

        return values;
    }


    /**
     * @param table The discharge table
     *
     * @return the values of a discharge table.
     */
    public static double[][] loadDischargeTableValues(DischargeTable table) {
        List<DischargeTableValue> dtvs = table.getDischargeTableValues();

        final double [][] vs = new double[2][dtvs.size()];

        int idx = 0;
        for (DischargeTableValue dtv: dtvs) {
            double q = dtv.getQ().doubleValue();
            vs[0][idx] = q;
            vs[1][idx] = dtv.getW().doubleValue();
            ++idx;
        }

        return vs;
    }

    private static final double EPSILON = 1e-5;

    private static final boolean epsEquals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    private static final boolean between(double a, double b, double x) {
        if (a > b) { double t = a; a = b; b = t; }
        return x > a && x < b;
    }

    /**
     * Find or interpolate Qs from q/w array.
     * @param values [[q0,q1,q2],[w0,w1,w2]]
     * @param w      W value to look for in values.
     */
    public static double [] getQsForW(double [][] values, double w) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("getQsForW: W = " + w);
        }

        double [] qs = values[0];
        double [] ws = values[1];

        int N = Math.min(qs.length, ws.length);

        if (N == 0) {
            if (debug) {
                log.debug("Q(" + w + ") = []");
            }
            return new double [0];
        }

        TDoubleArrayList outQs = new TDoubleArrayList();

        if (epsEquals(ws[0], w)) {
            outQs.add(qs[0]);
        }

        for (int i = 1; i < N; ++i) {
            if (epsEquals(ws[i], w)) {
                outQs.add(qs[i]);
            }
            else if (between(ws[i-1], ws[i], w)) {
                double w1 = ws[i-1];
                double w2 = ws[i];
                double q1 = qs[i-1];
                double q2 = qs[i];

                // q1 = m*w1 + b
                // q2 = m*w2 + b
                // q2 - q1 = m*(w2 - w1)
                // m = (q2 - q1)/(w2 - w1) # w2 != w1
                // b = q1 - m*w1
                // w1 != w2

                double m = (q2 - q1)/(w2 - w1);
                double b = q1 - m*w1;
                double q = w*m + b;

                outQs.add(q);
            }
        }

        double [] result = outQs.toNativeArray();

        if (debug) {
            log.debug("Q(" + w + ") = " + Arrays.toString(result));
        }

        return result;
    }

    public static double [] getWsForQ(double [][] values, double q) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("getWsForQ: W = " + q);
        }

        double [] qs = values[0];
        double [] ws = values[1];

        int N = Math.min(qs.length, ws.length);

        if (N == 0) {
            if (debug) {
                log.debug("W(" + q + ") = []");
            }
            return new double [0];
        }

        TDoubleArrayList outWs = new TDoubleArrayList();

        if (epsEquals(qs[0], q)) {
            outWs.add(ws[0]);
        }

        for (int i = 1; i < N; ++i) {
            if (epsEquals(qs[i], q)) {
                outWs.add(ws[i]);
            }
            else if (between(qs[i-1], qs[i], q)) {
                double w1 = ws[i-1];
                double w2 = ws[i];
                double q1 = qs[i-1];
                double q2 = qs[i];

                // w1 = m*q1 + b
                // w2 = m*q2 + b
                // w2 - w1 = m*(q2 - q1)
                // m = (w2 - w1)/(q2 - q1) # q2 != q1
                // b = w1 - m*q1
                // q1 != q2

                double m = (w2 - w1)/(q2 - q1);
                double b = w1 - m*q1;
                double w = q*m + b;

                outWs.add(w);
            }
        }

        double [] result = outWs.toNativeArray();

        if (debug) {
            log.debug("W(" + q + ") = " + Arrays.toString(result));
        }

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
