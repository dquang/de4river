/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import org.dive4elements.river.artifacts.math.Linear;

import org.apache.commons.math.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.NonMonotonousSequenceException;

import gnu.trove.TDoubleArrayList;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Utils to deal with Double precision values. */
public class DoubleUtil
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(DoubleUtil.class);

    public static final double DEFAULT_STEP_PRECISION = 1e6;

    public static final Comparator<double []> DOUBLE_PAIR_CMP =
        new Comparator<double []>() {
            @Override
            public int compare(double [] a, double [] b) {
                double diff = a[0] - b[0];
                if (diff < 0d) return -1;
                if (diff > 0d) return +1;
                return 0;
            }
        };

    /** EPSILON for comparison of double precision values. */
    public static final double EPSILON = 1e-4;

    private DoubleUtil() {
    }

    public static final double [] explode(
        double from,
        double to,
        double step
    ) {
        return explode(from, to, step, DEFAULT_STEP_PRECISION);
    }

    public static final double round(double x, double precision) {
        return Math.round(x * precision)/precision;
    }

    public static final double round(double x) {
        return Math.round(x * DEFAULT_STEP_PRECISION)/DEFAULT_STEP_PRECISION;
    }

    /**
     * Returns array with values from parameter from to to
     * with given step width.
     * from and to are included.
     */
    public static final double [] explode(
        double from,
        double to,
        double step,
        double precision
    ) {
        double lower = from;

        double diff = to - from;
        double tmp  = diff / step;
        int    num = (int)Math.abs(Math.ceil(tmp)) + 1;

        if (num < 1) {
            return new double[0];
        }

        double [] values = new double[num];

        if (from > to) {
            step = -step;
        }

        double max = Math.max(from, to);

        for (int idx = 0; idx < num; idx++) {
            if (lower - max > EPSILON) {
                return Arrays.copyOfRange(values, 0, idx);
            }

            values[idx] = round(lower, precision);
            lower      += step;
        }

        return values;
    }

    public static final double interpolateSorted(
        double [] xs,
        double [] ys,
        double x
    ) {
        int lo = 0, hi = xs.length-1;

        int mid = -1;

        while (lo <= hi) {
            mid = (lo + hi) >> 1;
            double mx = xs[mid];
                 if (x < mx) hi = mid - 1;
            else if (x > mx) lo = mid + 1;
            else return ys[mid];
        }
        if (mid < lo) {
            return lo >= xs.length
                ? Double.NaN
                : Linear.linear(x, xs[mid], xs[mid+1], ys[mid], ys[mid+1]);
        }
        return hi < 0
            ? Double.NaN
            : Linear.linear(x, xs[mid-1], xs[mid], ys[mid-1], ys[mid]);
    }

    public static final boolean isIncreasing(double [] array) {
        int inc = 0;
        int dec = 0;
        int sweet = (array.length-1)/2;
        for (int i = 1; i < array.length; ++i) {
            if (array[i] > array[i-1]) {
                if (++inc > sweet) {
                    return true;
                }
            }
            else if (++dec > sweet) {
                return false;
            }
        }
        return inc > sweet;
    }

    public static final double [] swap(double [] array) {
        int lo = 0;
        int hi = array.length-1;
        while (hi > lo) {
            double t  = array[lo];
            array[lo] = array[hi];
            array[hi] = t;
            ++lo;
            --hi;
        }

        return array;
    }

    public static final double [] swapClone(double [] in) {
        double [] out = new double[in.length];

        for (int j = out.length-1, i = 0; j >= 0;) {
            out[j--] = in[i++];
        }

        return out;
    }

    public static final double [] sumDiffs(double [] in) {
        double [] out = new double[in.length];

        for (int i = 1; i < out.length; ++i) {
            out[i] = out[i-1] + Math.abs(in[i-1] - in[i]);
        }

        return out;
    }

    public static final double sum(double [] values) {
        double sum = 0.0;
        for (double value: values) {
            sum += value;
        }
        return sum;
    }

    public static final double [] fill(int N, double value) {
        double [] result = new double[N];
        Arrays.fill(result, value);
        return result;
    }


    /** Use with parseSegments. */
    public interface SegmentCallback {
        void newSegment(double from, double to, double [] values);
    }


    /** Call callback for every string split by colon.
     * Expected format FROM:TO:VALUE1,VALUE2,VALUE3*/
    public static final void parseSegments(
        String          input,
        SegmentCallback callback
    ) {
        TDoubleArrayList vs = new TDoubleArrayList();

        for (String segmentStr: input.split(":")) {
            String [] parts = segmentStr.split(";");
            if (parts.length < 3) {
                log.warn("invalid segment: '" + segmentStr + "'");
                continue;
            }
            try {
                double from = Double.parseDouble(parts[0].trim());
                double to   = Double.parseDouble(parts[1].trim());

                vs.resetQuick();

                for (String valueStr: parts[3].split(",")) {
                    vs.add(round(Double.parseDouble(valueStr.trim())));
                }

                callback.newSegment(from, to, vs.toNativeArray());
            }
            catch (NumberFormatException nfe) {
                log.warn("invalid segment: '" + segmentStr + "'");
            }
        }
    }

    public static final boolean isValid(double [][] data) {
        for (double [] ds: data) {
            if (!isValid(ds)) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isValid(double [] data) {
        for (double d: data) {
            if (Double.isNaN(d)) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isNaN(double [] values) {
        for (double value: values) {
            if (!Double.isNaN(value)) {
                return false;
            }
        }
        return true;
    }

    /** In an array of doubles, search and return the maximum value. */
    public static final double maxInArray(double[] values) {
        double max = - Double.MAX_VALUE;
        for (double d: values) {
            if (d > max) max = d;
        }
        return max;
    }



    /** Sort a and b with a as key. b is ordered accordingly */
    public static final void sortByFirst(double [] a, double [] b) {
        // XXX: Not efficient but bulletproof.
        double [][] pairs = new double[a.length][2];
        for (int i = 0; i < a.length; ++i) {
            double [] p = pairs[i];
            p[0] = a[i];
            p[1] = b[i];

        }
        Arrays.sort(pairs, DOUBLE_PAIR_CMP);
        for (int i = 0; i < a.length; ++i) {
            double [] p = pairs[i];
            a[i] = p[0];
            b[i] = p[1];
        }
    }

    public static void removeNaNs(TDoubleArrayList [] arrays) {

        int dest = 0;

        int A = arrays.length;
        int N = arrays[0].size();

        OUTER: for (int i = 0; i < N; ++i) {
            for (int j = 0; j < A; ++j) {
                TDoubleArrayList a = arrays[j];
                double v = a.getQuick(i);
                if (Double.isNaN(v)) {
                    continue OUTER;
                }
                a.setQuick(dest, v);
            }
            ++dest;
        }

        if (dest < N) {
            for (int i = 0; i < A; ++i) {
                arrays[i].remove(dest, N-dest);
            }
        }
    }

    /** Convieniance function for results to get an interpolator.
     * This is basically a static wrapper to for LinearInterpolator.interpolate
     * with error handling. Returns null on error.*/
    public static PolynomialSplineFunction getLinearInterpolator(
        double[] x,
        double[] y
    ) {
        /* We want copies here to remove NaNs but don't
         * change the original data. */
        TDoubleArrayList tX = new TDoubleArrayList();
        TDoubleArrayList tY = new TDoubleArrayList();
        for (int i = 0; i < x.length; i++) {
            if (!Double.isNaN(y[i])) {
                tX.add(x[i]);
                tY.add(y[i]);
            }
        }
        LinearInterpolator lpol = new LinearInterpolator();
        try {
            return lpol.interpolate(tX.toNativeArray(), tY.toNativeArray());
        } catch (DimensionMismatchException e) {
            log.error("KMs and Result values have different sizes. "
                + "Failed to interpolate: "
                + e.getMessage());
        } catch (NonMonotonousSequenceException e) {
            log.error("KMs are not monotonous. Failed to interpolate: "
                + e.getMessage());
        } catch (NumberIsTooSmallException e) {
            log.error("Result is to small. Failed to interpolate: "
                + e.getMessage());
        }
        return null;
    }

    public static PolynomialSplineFunction getLinearInterpolator(
        TDoubleArrayList x,
        TDoubleArrayList y
    ) {
        return getLinearInterpolator(x.toNativeArray(), y.toNativeArray());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
