/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import org.dive4elements.river.artifacts.math.Linear;
import org.dive4elements.river.artifacts.math.Function;
import org.dive4elements.river.utils.DoubleUtil;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.commons.math.analysis.interpolation.SplineInterpolator;

import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import org.apache.commons.math.ArgumentOutsideDomainException;

import org.apache.commons.math.exception.MathIllegalArgumentException;

import gnu.trove.TDoubleArrayList;

import static org.dive4elements.river.backend.utils.EpsilonComparator.CMP;

/**
 * W, Q and km data from database 'wsts' spiced with interpolation algorithms.
 */
public class WstValueTable
implements   Serializable
{
    private static Logger log = LogManager.getLogger(WstValueTable.class);

    public static final int DEFAULT_Q_STEPS = 500;

    public static final int RELATE_WS_SAMPLES = 200;

    public static final double W_EPSILON = 0.000001;

    /**
     * A Column in the table, typically representing one measurement session.
     */
    public static final class Column
    implements                Serializable
    {
        protected String name;

        protected QRangeTree qRangeTree;

        public Column() {
        }

        public Column(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public QRangeTree getQRangeTree() {
            return qRangeTree;
        }

        public void setQRangeTree(QRangeTree qRangeTree) {
            this.qRangeTree = qRangeTree;
        }
    } // class Column

    /**
     * A (weighted) position used for interpolation.
     */
    public static final class QPosition {

        protected int    index;
        protected double weight;

        public QPosition() {
        }

        public QPosition(int index, double weight) {
            this.index  = index;
            this.weight = weight;
        }

        public QPosition set(int index, double weight) {
            this.index  = index;
            this.weight = weight;
            return this;
        }

    } // class Position

    public static final class SplineFunction {

        public PolynomialSplineFunction spline;
        public double []                splineQs;
        public double []                splineWs;

        public SplineFunction(
            PolynomialSplineFunction spline,
            double []                splineQs,
            double []                splineWs
        ) {
            this.spline   = spline;
            this.splineQs = splineQs;
            this.splineWs = splineWs;
        }

        public double [][] sample(
            int         numSamples,
            double      km,
            Calculation errors
        ) {
            double minQ = getQMin();
            double maxQ = getQMax();

            double [] outWs = new double[numSamples];
            double [] outQs = new double[numSamples];

            Arrays.fill(outWs, Double.NaN);
            Arrays.fill(outQs, Double.NaN);

            double stepWidth = (maxQ - minQ)/numSamples;

            try {
                double q = minQ;
                for (int i = 0; i < outWs.length; ++i, q += stepWidth) {
                    outWs[i] = spline.value(outQs[i] = q);
                }
            }
            catch (ArgumentOutsideDomainException aode) {
                if (errors != null) {
                    errors.addProblem(km, "spline.interpolation.failed");
                }
                log.error("spline interpolation failed.", aode);
            }

            return new double [][] { outWs, outQs };
        }

        public double getQMin() {
            return Math.min(splineQs[0], splineQs[splineQs.length-1]);
        }

        public double getQMax() {
            return Math.max(splineQs[0], splineQs[splineQs.length-1]);
        }

        /** Constructs a continues index between the columns to Qs. */
        public PolynomialSplineFunction createIndexQRelation() {

            double [] indices = new double[splineQs.length];
            for (int i = 0; i < indices.length; ++i) {
                indices[i] = i;
            }

            try {
                SplineInterpolator interpolator = new SplineInterpolator();
                return interpolator.interpolate(indices, splineQs);
            }
            catch (MathIllegalArgumentException miae) {
                // Ignore me!
            }
            return null;
        }
    } // class SplineFunction

    /**
     * A row, typically a position where measurements were taken.
     */
    public static final class Row
    implements                Serializable, Comparable<Row>
    {
        double    km;
        double [] ws;

        public Row() {
        }

        public Row(double km) {
            this.km = km;
        }

        public Row(double km, double [] ws) {
            this(km);
            this.ws = ws;
        }

        /**
         * Sort Qs and Ws for this Row over Q.
         */
        private double[][] getSortedWQ(
            WstValueTable table,
            Calculation   errors
        ) {
            int W = ws.length;

            if (W < 1) {
                if (errors != null) {
                    errors.addProblem(km, "no.ws.found");
                }
                return new double[][] {{Double.NaN}, {Double.NaN}};
            }

            double [] sortedWs = ws;
            double [] sortedQs = new double[W];

            for (int i=0; i < W; ++i) {
                double q = table.getQIndex(i, km);
                if (Double.isNaN(q) && errors != null) {
                    errors.addProblem(
                        km, "no.q.found.in.column", i+1);
                }
                sortedQs[i] = q;
            }

            DoubleUtil.sortByFirst(sortedQs, sortedWs);

            return new double[][] { sortedWs, sortedQs };
        }

        /**
         * Return an array of Qs and Ws for the given km between
         * this Row and another, sorted over Q.
         */
        private double[][] getSortedWQ(
            Row           other,
            double        km,
            WstValueTable table,
            Calculation   errors
        ) {
            int W = Math.min(ws.length, other.ws.length);

            if (W < 1) {
                if (errors != null) {
                    errors.addProblem("no.ws.found");
                }
                return new double[][] {{Double.NaN}, {Double.NaN}};
            }

            double factor = Linear.factor(km, this.km, other.km);

            double [] sortedQs = new double[W];
            double [] sortedWs = new double[W];

            for (int i = 0; i < W; ++i) {
                double wws = Linear.weight(factor, ws[i], other.ws[i]);
                double wqs = table.getQIndex(i, km);

                if (Double.isNaN(wws) || Double.isNaN(wqs)) {
                    if (errors != null) {
                        errors.addProblem(km, "cannot.find.w.or.q");
                    }
                }

                sortedWs[i] = wws;
                sortedQs[i] = wqs;
            }

            DoubleUtil.sortByFirst(sortedQs, sortedWs);

            return new double[][] { sortedWs, sortedQs };
        }

        /**
         * Find Qs matching w in an array of Qs and Ws sorted over Q.
         */
        private double[] findQsForW(double w, double[][] sortedWQ) {
            int W = sortedWQ[0].length;

            double[] sortedQs = sortedWQ[1];
            double[] sortedWs = sortedWQ[0];

            TDoubleArrayList qs = new TDoubleArrayList();

            if (W > 0 && Math.abs(sortedWs[0]-w) < W_EPSILON) {
                double q = sortedQs[0];
                if (!Double.isNaN(q)) {
                    qs.add(q);
                }
            }

            for (int i = 1; i < W; ++i) {
                double w2 = sortedWs[i];
                if (Double.isNaN(w2)) {
                    continue;
                }
                if (Math.abs(w2-w) < W_EPSILON) {
                    double q = sortedQs[i];
                    if (!Double.isNaN(q)) {
                        qs.add(q);
                    }
                    continue;
                }
                double w1 = sortedWs[i-1];
                if (Double.isNaN(w1)) {
                    continue;
                }

                if (w < Math.min(w1, w2) || w > Math.max(w1, w2)) {
                    continue;
                }

                double q1 = sortedQs[i-1];
                double q2 = sortedQs[i];
                if (Double.isNaN(q1) || Double.isNaN(q2)) {
                    continue;
                }

                double q = Linear.linear(w, w1, w2, q1, q2);
                qs.add(q);
            }

            return qs.toNativeArray();
        }

        /**
         * Compare according to place of measurement (km).
         */
        public int compareTo(Row other) {
            return CMP.compare(km, other.km);
        }

        /**
         * Interpolate Ws, given Qs and a km.
         *
         * @param iqs Given ("input") Qs.
         * @param ows Resulting ("output") Ws.
         * @param table Table of which to use data for interpolation.
         */
        public void interpolateW(
            Row           other,
            double        km,
            double []     iqs,
            double []     ows,
            WstValueTable table,
            Calculation   errors
        ) {
            double kmWeight = Linear.factor(km, this.km, other.km);

            QPosition qPosition = new QPosition();

            for (int i = 0; i < iqs.length; ++i) {
                if (table.getQPosition(km, iqs[i], qPosition) != null) {
                    double wt =       getW(qPosition);
                    double wo = other.getW(qPosition);
                    if (Double.isNaN(wt) || Double.isNaN(wo)) {
                        if (errors != null) {
                            errors.addProblem(
                                km, "cannot.find.w.for.q", iqs[i]);
                        }
                        ows[i] = Double.NaN;
                    }
                    else {
                        ows[i] = Linear.weight(kmWeight, wt, wo);
                    }
                }
                else {
                    if (errors != null) {
                        errors.addProblem(km, "cannot.find.q", iqs[i]);
                    }
                    ows[i] = Double.NaN;
                }
            }
        }


        public SplineFunction createSpline(
            WstValueTable table,
            Calculation   errors
        ) {
            double[][] sortedWQ = getSortedWQ(table, errors);

            try {
                SplineInterpolator interpolator = new SplineInterpolator();
                PolynomialSplineFunction spline =
                    interpolator.interpolate(sortedWQ[1], sortedWQ[0]);

                return new SplineFunction(spline, sortedWQ[1], ws);
            }
            catch (MathIllegalArgumentException miae) {
                if (errors != null) {
                    errors.addProblem(km, "spline.creation.failed");
                }
                log.error("spline creation failed", miae);
            }
            return null;
        }

        public SplineFunction createSpline(
            Row           other,
            double        km,
            WstValueTable table,
            Calculation   errors
        ) {
            double[][] sortedWQ = getSortedWQ(other, km, table, errors);

            SplineInterpolator interpolator = new SplineInterpolator();

            try {
                PolynomialSplineFunction spline =
                    interpolator.interpolate(sortedWQ[1], sortedWQ[0]);

                return new SplineFunction(spline, sortedWQ[1], sortedWQ[0]);
            }
            catch (MathIllegalArgumentException miae) {
                if (errors != null) {
                    errors.addProblem(km, "spline.creation.failed");
                }
                log.error("spline creation failed", miae);
            }

            return null;
        }

        public double [][] interpolateWQ(
            Row           other,
            double        km,
            int           steps,
            WstValueTable table,
            Calculation   errors
        ) {
            SplineFunction sf = createSpline(other, km, table, errors);

            return sf != null
                ? sf.sample(steps, km, errors)
                : new double[2][0];
        }


        public double [][] interpolateWQ(
            int           steps,
            WstValueTable table,
            Calculation   errors
        ) {
            SplineFunction sf = createSpline(table, errors);

            return sf != null
                ? sf.sample(steps, km, errors)
                : new double[2][0];
        }


        public double getW(QPosition qPosition) {
            int    index  = qPosition.index;
            double weight = qPosition.weight;

            return weight == 1.0
                ? ws[index]
                : Linear.weight(weight, ws[index-1], ws[index]);
        }

        public double getW(
            Row       other,
            double    km,
            QPosition qPosition
        ) {
            double kmWeight = Linear.factor(km, this.km, other.km);

            int    index  = qPosition.index;
            double weight = qPosition.weight;

            double tw, ow;

            if (weight == 1.0) {
                tw = ws[index];
                ow = other.ws[index];
            }
            else {
                tw = Linear.weight(weight, ws[index-1], ws[index]);
                ow = Linear.weight(weight, other.ws[index-1], other.ws[index]);
            }

            return Linear.weight(kmWeight, tw, ow);
        }

        public double [] findQsForW(
            double        w,
            WstValueTable table,
            Calculation   errors
        ) {
            log.debug("Find Qs for given W at tabulated km " + km);
            return findQsForW(w, getSortedWQ(table, errors));
        }

        public double [] findQsForW(
            Row           other,
            double        w,
            double        km,
            WstValueTable table,
            Calculation   errors
        ) {
            log.debug("Find Qs for given W at non-tabulated km " + km);
            return findQsForW(w, getSortedWQ(other, km, table, errors));
        }

        public double [] getMinMaxW(double [] result) {
            double minW =  Double.MAX_VALUE;
            double maxW = -Double.MAX_VALUE;
            for (int i = 0; i < ws.length; ++i) {
                double w = ws[i];
                if (w < minW) minW = w;
                if (w > maxW) maxW = w;
            }
            result[0] = minW;
            result[1] = maxW;
            return result;
        }

        public double [] getMinMaxW(Row other, double km, double [] result) {
            double [] m1 = this .getMinMaxW(new double [2]);
            double [] m2 = other.getMinMaxW(new double [2]);
            double factor = Linear.factor(km, this.km, other.km);
            result[0] = Linear.weight(factor, m1[0], m2[0]);
            result[1] = Linear.weight(factor, m1[1], m2[1]);
            return result;
        }
    } // class Row

    /** Rows in table. */
    protected List<Row> rows;

    /** Columns in table. */
    protected Column [] columns;

    public WstValueTable() {
        rows = new ArrayList<Row>();
    }

    public WstValueTable(Column [] columns) {
        this();
        this.columns = columns;
    }

    /**
     * @param columns The WST-columns.
     * @param rows A list of Rows that must be sorted by km.
     */
    public WstValueTable(Column [] columns, List<Row> rows) {
        this.columns = columns;
        this.rows    = rows;
    }

    public Column [] getColumns() {
        return columns;
    }

    /**
     * @param km Given kilometer.
     * @param qs Given Q values.
     * @param ws output parameter.
     */
    public double [] interpolateW(double km, double [] qs, double [] ws) {
        return interpolateW(km, qs, ws, null);
    }


    /**
     * @param ws (output parameter), gets returned.
     * @return output parameter ws.
     */
    public double [] interpolateW(
        double      km,
        double []   qs,
        double []   ws,
        Calculation errors
    ) {
        int rowIndex = Collections.binarySearch(rows, new Row(km));

        QPosition qPosition = new QPosition();

        if (rowIndex >= 0) { // direct row match
            Row row = rows.get(rowIndex);
            for (int i = 0; i < qs.length; ++i) {
                if (getQPosition(km, qs[i], qPosition) == null) {
                    if (errors != null) {
                        errors.addProblem(km, "cannot.find.q", qs[i]);
                    }
                    ws[i] = Double.NaN;
                }
                else {
                    if (Double.isNaN(ws[i] = row.getW(qPosition))
                    && errors != null) {
                        errors.addProblem(
                            km, "cannot.find.w.for.q", qs[i]);
                    }
                }
            }
        }
        else { // needs bilinear interpolation
            rowIndex = -rowIndex -1;

            if (rowIndex < 1 || rowIndex >= rows.size()) {
                // do not extrapolate
                Arrays.fill(ws, Double.NaN);
                if (errors != null) {
                    errors.addProblem(km, "km.not.found");
                }
            }
            else {
                Row r1 = rows.get(rowIndex-1);
                Row r2 = rows.get(rowIndex);
                r1.interpolateW(r2, km, qs, ws, this, errors);
            }
        }

        return ws;
    }

    public double [] getMinMaxQ(double km) {
        return getMinMaxQ(km, new double [2]);
    }

    public double [] getMinMaxQ(double km, double [] result) {
        double minQ =  Double.MAX_VALUE;
        double maxQ = -Double.MAX_VALUE;

        for (int i = 0; i < columns.length; ++i) {
            double q = columns[i].getQRangeTree().findQ(km);
            if (!Double.isNaN(q)) {
                if (q < minQ) minQ = q;
                if (q > maxQ) maxQ = q;
            }
        }

        if (minQ < Double.MAX_VALUE) {
            result[0] = minQ;
            result[1] = maxQ;
            return result;
        }

        return null;
    }

    public double [] getMinMaxQ(double from, double to, double step) {
        double [] result = new double[2];

        double minQ =  Double.MAX_VALUE;
        double maxQ = -Double.MAX_VALUE;

        if (from > to) {
            double tmp = from;
            from = to;
            to = tmp;
        }

        step = Math.max(Math.abs(step), 0.0001);

        double d = from;
        for (; d <= to; d += step) {
            if (getMinMaxQ(d, result) != null) {
                if (result[0] < minQ) minQ = result[0];
                if (result[1] > maxQ) maxQ = result[1];
            }
        }

        if (d != to) {
            if (getMinMaxQ(to, result) != null) {
                if (result[0] < minQ) minQ = result[0];
                if (result[1] > maxQ) maxQ = result[1];
            }
        }

        return minQ < Double.MAX_VALUE
            ? new double [] { minQ, maxQ }
            : null;
    }

    public double [] getMinMaxW(double km) {
        return getMinMaxW(km, new double [2]);

    }
    public double [] getMinMaxW(double km, double [] result) {
        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex >= 0) {
            return rows.get(rowIndex).getMinMaxW(result);
        }

        rowIndex = -rowIndex -1;

        if (rowIndex < 1 || rowIndex >= rows.size()) {
            // do not extrapolate
            return null;
        }

        Row r1 = rows.get(rowIndex-1);
        Row r2 = rows.get(rowIndex);

        return r1.getMinMaxW(r2, km, result);
    }

    public double [] getMinMaxW(double from, double to, double step) {
        double [] result = new double[2];

        double minW =  Double.MAX_VALUE;
        double maxW = -Double.MAX_VALUE;

        if (from > to) {
            double tmp = from;
            from = to;
            to = tmp;
        }

        step = Math.max(Math.abs(step), 0.0001);

        double d = from;
        for (; d <= to; d += step) {
            if (getMinMaxW(d, result) != null) {
                if (result[0] < minW) minW = result[0];
                if (result[1] > maxW) maxW = result[1];
            }
        }

        if (d != to) {
            if (getMinMaxW(to, result) != null) {
                if (result[0] < minW) minW = result[0];
                if (result[1] > maxW) maxW = result[1];
            }
        }

        return minW < Double.MAX_VALUE
            ? new double [] { minW, maxW }
            : null;
    }

    /**
     * Interpolate W and Q values at a given km.
     */
    public double [][] interpolateWQ(double km) {
        return interpolateWQ(km, null);
    }

    /**
     * Interpolate W and Q values at a given km.
     *
     * @param errors where to store errors.
     *
     * @return double double array, first index Ws, second Qs.
     */
    public double [][] interpolateWQ(double km, Calculation errors) {
        return interpolateWQ(km, DEFAULT_Q_STEPS, errors);
    }


    /**
     * Interpolate W and Q values at a given km.
     */
    public double [][] interpolateWQ(
        double km,
        int steps,
        Calculation errors
    ) {
        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex >= 0) { // direct row match
            Row row = rows.get(rowIndex);
            return row.interpolateWQ(steps, this, errors);
        }

        rowIndex = -rowIndex -1;

        if (rowIndex < 1 || rowIndex >= rows.size()) {
            // do not extrapolate
            if (errors != null) {
                errors.addProblem(km, "km.not.found");
            }
            return new double[2][0];
        }

        Row r1 = rows.get(rowIndex-1);
        Row r2 = rows.get(rowIndex);

        return r1.interpolateWQ(r2, km, steps, this, errors);
    }

    public boolean interpolate(
        double    km,
        double [] out,
        QPosition qPosition,
        Function  qFunction
    ) {
        int R1 = rows.size()-1;

        out[1] = qFunction.value(getQ(qPosition, km));

        if (Double.isNaN(out[1])) {
            return false;
        }

        QPosition nPosition = new QPosition();
        if (getQPosition(km, out[1], nPosition) == null) {
            return false;
        }

        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex >= 0) {
            // direct row match
            out[0] = rows.get(rowIndex).getW(nPosition);
            return !Double.isNaN(out[0]);
        }

        rowIndex = -rowIndex -1;

        if (rowIndex < 1 || rowIndex > R1) {
            // do not extrapolate
            return false;
        }

        Row r1 = rows.get(rowIndex-1);
        Row r2 = rows.get(rowIndex);
        out[0] = r1.getW(r2, km, nPosition);

        return !Double.isNaN(out[0]);
    }


    /**
     * Look up interpolation of a Q at given positions.
     *
     * @param q           the non-interpolated Q value.
     * @param referenceKm the reference km (e.g. gauge position).
     * @param kms         positions for which to interpolate.
     * @param ws          (output) resulting interpolated ws.
     * @param qs          (output) resulting interpolated qs.
     * @param errors      calculation object to store errors.
     */
    public QPosition interpolate(
        double      q,
        double      referenceKm,
        double []   kms,
        double []   ws,
        double []   qs,
        Calculation errors
    ) {
        return interpolate(
            q, referenceKm, kms, ws, qs, 0, kms.length, errors);
    }

    /**
     * Interpolate Q at given positions.
     * @param kms positions for which to calculate qs and ws
     * @param ws [out] calculated ws for kms
     * @param qs [out] looked up qs for kms.
     */
    public QPosition interpolate(
        double      q,
        double      referenceKm,
        double []   kms,
        double []   ws,
        double []   qs,
        int         startIndex,
        int         length,
        Calculation errors
    ) {
        QPosition qPosition = getQPosition(referenceKm, q);

        if (qPosition == null) {
            // we cannot locate q at km
            Arrays.fill(ws, Double.NaN);
            Arrays.fill(qs, Double.NaN);
            if (errors != null) {
                errors.addProblem(referenceKm, "cannot.find.q", q);
            }
            return null;
        }

        Row kmKey = new Row();

        int R1 = rows.size()-1;

        for (int i = startIndex, end = startIndex+length; i < end; ++i) {

            if (Double.isNaN(qs[i] = getQ(qPosition, kms[i]))) {
                if (errors != null) {
                    errors.addProblem(kms[i], "cannot.find.q", q);
                }
                ws[i] = Double.NaN;
                continue;
            }

            kmKey.km = kms[i];
            int rowIndex = Collections.binarySearch(rows, kmKey);

            if (rowIndex >= 0) {
                // direct row match
                if (Double.isNaN(ws[i] = rows.get(rowIndex).getW(qPosition))
                && errors != null) {
                    errors.addProblem(kms[i], "cannot.find.w.for.q", q);
                }
                continue;
            }

            rowIndex = -rowIndex -1;

            if (rowIndex < 1 || rowIndex > R1) {
                // do not extrapolate
                if (errors != null) {
                    errors.addProblem(kms[i], "km.not.found");
                }
                ws[i] = Double.NaN;
                continue;
            }
            Row r1 = rows.get(rowIndex-1);
            Row r2 = rows.get(rowIndex);

            if (Double.isNaN(ws[i] = r1.getW(r2, kms[i], qPosition))
            && errors != null) {
                errors.addProblem(kms[i], "cannot.find.w.for.q", q);
            }
        }

        return qPosition;
    }

    /**
     * Linearly interpolate w at a km at a column of two rows.
     *
     * @param km   position for which to interpolate.
     * @param row1 first row.
     * @param row2 second row.
     * @param col  column-index at which to look.
     *
     * @return Linearly interpolated w, NaN if one of the given rows was null.
     */
    public static double linearW(double km, Row row1, Row row2, int col) {
        if (row1 == null || row2 == null) {
            return Double.NaN;
        }

        return Linear.linear(km,
            row1.km, row2.km,
            row1.ws[col], row2.ws[col]);
    }

    /**
     * Do interpolation/lookup of W and Q within columns (i.e. ignoring values
     * of other columns).
     * @param km position (km) at which to interpolate/lookup.
     * @return [[q0, q1, .. qx] , [w0, w1, .. wx]] (can contain NaNs)
     */
    public double [][] interpolateWQColumnwise(double km) {
        log.debug("WstValueTable.interpolateWQColumnwise");
        double [] qs = new double[columns.length];
        double [] ws = new double[columns.length];

        // Find out row from where we will start searching.
        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex < 0) {
            rowIndex = -rowIndex -1;
        }

        // TODO Beyond definition, we could stop more clever.
        if (rowIndex >= rows.size()) {
            rowIndex = rows.size() -1;
        }

        Row startRow = rows.get(rowIndex);

        for (int col = 0; col < columns.length; col++) {
            qs[col] = columns[col].getQRangeTree().findQ(km);
            if (startRow.km == km && !Double.isNaN(startRow.ws[col])) {
                // Great. W is defined at km.
                ws[col] = startRow.ws[col];
                continue;
            }

            // Search neighbouring rows that define w at this col.
            Row rowBefore = null;
            Row rowAfter  = null;
            for (int before = rowIndex -1; before >= 0; before--) {
                if (!Double.isNaN(rows.get(before).ws[col])) {
                    rowBefore = rows.get(before);
                    break;
                }
            }
            if (rowBefore != null) {
                for (int after = rowIndex, R = rows.size();
                     after < R;
                     after++
                ) {
                    if (!Double.isNaN(rows.get(after).ws[col])) {
                        rowAfter = rows.get(after);
                        break;
                    }
                }
            }

            ws[col] = linearW(km, rowBefore, rowAfter, col);
        }

        return new double [][] {qs, ws};
    }

    public double [] findQsForW(double km, double w, Calculation errors) {

        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex >= 0) {
            return rows.get(rowIndex).findQsForW(w, this, errors);
        }

        rowIndex = -rowIndex - 1;

        if (rowIndex < 1 || rowIndex >= rows.size()) {
            // Do not extrapolate.
            return new double[0];
        }

        // Needs bilinear interpolation.
        Row r1 = rows.get(rowIndex-1);
        Row r2 = rows.get(rowIndex);

        return r1.findQsForW(r2, w, km, this, errors);
    }

    protected SplineFunction createSpline(double km, Calculation errors) {

        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex >= 0) {
            SplineFunction sf = rows.get(rowIndex).createSpline(this, errors);
            if (sf == null && errors != null) {
                errors.addProblem(km, "cannot.create.wq.relation");
            }
            return sf;
        }

        rowIndex = -rowIndex - 1;

        if (rowIndex < 1 || rowIndex >= rows.size()) {
            // Do not extrapolate.
            if (errors != null) {
                errors.addProblem(km, "km.not.found");
            }
            return null;
        }

        // Needs bilinear interpolation.
        Row r1 = rows.get(rowIndex-1);
        Row r2 = rows.get(rowIndex);

        SplineFunction sf = r1.createSpline(r2, km, this, errors);
        if (sf == null && errors != null) {
            errors.addProblem(km, "cannot.create.wq.relation");
        }

        return sf;
    }

    /** 'Bezugslinienverfahren' */
    public double [][] relateWs(
        double      km1,
        double      km2,
        Calculation errors
    ) {
        return relateWs(km1, km2, RELATE_WS_SAMPLES, errors);
    }

    private static class ErrorHandler {

        boolean     hasErrors;
        Calculation errors;

        ErrorHandler(Calculation errors) {
            this.errors = errors;
        }

        void error(double km, String key, Object ... args) {
            if (errors != null && !hasErrors) {
                hasErrors = true;
                errors.addProblem(km, key, args);
            }
        }
    } // class ErrorHandler


    /* TODO: Add optimized methods of relateWs to relate one
     *       start km to many end kms. The index generation/spline stuff for
     *       the start km is always the same.
     */
    public double [][] relateWs(
        double      km1,
        double      km2,
        int         numSamples,
        Calculation errors
    ) {
        SplineFunction sf1 = createSpline(km1, errors);
        if (sf1 == null) {
            return new double[2][0];
        }

        SplineFunction sf2 = createSpline(km2, errors);
        if (sf2 == null) {
            return new double[2][0];
        }

        PolynomialSplineFunction iQ1 = sf1.createIndexQRelation();
        if (iQ1 == null) {
            if (errors != null) {
                errors.addProblem(km1, "cannot.create.index.q.relation");
            }
            return new double[2][0];
        }

        PolynomialSplineFunction iQ2 = sf2.createIndexQRelation();
        if (iQ2 == null) {
            if (errors != null) {
                errors.addProblem(km2, "cannot.create.index.q.relation");
            }
            return new double[2][0];
        }

        int N = Math.min(sf1.splineQs.length, sf2.splineQs.length);
        double stepWidth = N/(double)numSamples;

        PolynomialSplineFunction qW1 = sf1.spline;
        PolynomialSplineFunction qW2 = sf2.spline;

        TDoubleArrayList ws1 = new TDoubleArrayList(numSamples);
        TDoubleArrayList ws2 = new TDoubleArrayList(numSamples);
        TDoubleArrayList qs1 = new TDoubleArrayList(numSamples);
        TDoubleArrayList qs2 = new TDoubleArrayList(numSamples);

        ErrorHandler err = new ErrorHandler(errors);

        int i = 0;
        for (double p = 0d; p <= N-1; p += stepWidth, ++i) {

            double q1;
            try {
                q1 = iQ1.value(p);
            }
            catch (ArgumentOutsideDomainException aode) {
                err.error(km1, "w.w.qkm1.failed", p);
                continue;
            }

            double w1;
            try {
                w1 = qW1.value(q1);
            }
            catch (ArgumentOutsideDomainException aode) {
                err.error(km1, "w.w.wkm1.failed", q1, p);
                continue;
            }

            double q2;
            try {
                q2 = iQ2.value(p);
            }
            catch (ArgumentOutsideDomainException aode) {
                err.error(km2, "w.w.qkm2.failed", p);
                continue;
            }

            double w2;
            try {
                w2 = qW2.value(q2);
            }
            catch (ArgumentOutsideDomainException aode) {
                err.error(km2, "w.w.wkm2.failed", q2, p);
                continue;
            }

            ws1.add(w1);
            ws2.add(w2);
            qs1.add(q1);
            qs2.add(q2);
        }

        return new double [][] {
            ws1.toNativeArray(),
            qs1.toNativeArray(),
            ws2.toNativeArray(),
            qs2.toNativeArray() };
    }

    public QPosition getQPosition(double km, double q) {
        return getQPosition(km, q, new QPosition());
    }

    public QPosition getQPosition(double km, double q, QPosition qPosition) {

        if (columns.length == 0) {
            return null;
        }

        double qLast = columns[0].getQRangeTree().findQ(km);

        if (Math.abs(qLast - q) < 0.00001) {
            return qPosition.set(0, 1d);
        }

        for (int i = 1; i < columns.length; ++i) {
            double qCurrent = columns[i].getQRangeTree().findQ(km);
            if (Math.abs(qCurrent - q) < 0.00001) {
                return qPosition.set(i, 1d);
            }

            double qMin, qMax;
            if (qLast < qCurrent) { qMin = qLast; qMax = qCurrent; }
            else                  { qMin = qCurrent; qMax = qLast; }

            if (q > qMin && q < qMax) {
                double weight = Linear.factor(q, qLast, qCurrent);
                return qPosition.set(i, weight);
            }
            qLast = qCurrent;
        }

        return null;
    }

    public double getQIndex(int index, double km) {
        return columns[index].getQRangeTree().findQ(km);
    }

    public double getQ(QPosition qPosition, double km) {
        int    index  = qPosition.index;
        double weight = qPosition.weight;

        if (weight == 1d) {
            return columns[index].getQRangeTree().findQ(km);
        }
        double q1 = columns[index-1].getQRangeTree().findQ(km);
        double q2 = columns[index  ].getQRangeTree().findQ(km);
        return Linear.weight(weight, q1, q2);
    }

    public double [][] interpolateTabulated(double km) {
        return interpolateTabulated(km, new double[2][columns.length]);
    }

    public double [][] interpolateTabulated(double km, double [][] result) {

        int rowIndex = Collections.binarySearch(rows, new Row(km));

        if (rowIndex >= 0) {
            // Direct hit -> copy ws.
            Row row = rows.get(rowIndex);
            System.arraycopy(
                row.ws, 0, result[0], 0,
                Math.min(row.ws.length, result[0].length));
        }
        else {
            rowIndex = -rowIndex -1;
            if (rowIndex < 1 || rowIndex >= rows.size()) {
                // Out of bounds.
                return null;
            }
            // Interpolate ws.
            Row r1 = rows.get(rowIndex-1);
            Row r2 = rows.get(rowIndex);
            double factor = Linear.factor(km, r1.km, r2.km);
            Linear.weight(factor, r1.ws, r2.ws, result[0]);
        }

        double [] qs = result[1];
        for (int i = Math.min(qs.length, columns.length)-1; i >= 0; --i) {
            qs[i] = columns[i].getQRangeTree().findQ(km);
        }
        return result;
    }


    /** True if no QRange is given or Q equals zero. */
    public boolean hasEmptyQ() {
        for (Column column: columns) {
            if (column.getQRangeTree() == null) {
                return true;
            }
            else {
                if (Math.abs(column.getQRangeTree().maxQ()) <= 0.01d) {
                    return true;
                }
            }
        }

        if (columns.length == 0) {
            log.warn("No columns in WstValueTable.");
        }

        return false;
    }


    /** Find ranges that are between km1 and km2 (inclusive?) */
    public List<Range> findSegments(double km1, double km2) {
        return columns.length != 0
            ? columns[columns.length-1].getQRangeTree().findSegments(km1, km2)
            : Collections.<Range>emptyList();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
