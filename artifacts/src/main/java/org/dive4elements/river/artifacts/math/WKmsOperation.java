/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WKmsImpl;

import java.util.Arrays;

public abstract class WKmsOperation
{
    public static final double EPSILON = 1e-6;

    public static final class KmW
    implements                Comparable<KmW>
    {
        protected double km;
        protected double w;

        public KmW(double km, double w) {
            this.km = km;
            this.w  = w;
        }

        public int compareTo(KmW other) {
            return km < other.km
                ? -1
                : km > other.km ? +1 : 0;
        }

        public boolean kmEquals(KmW other) {
            return Math.abs(km - other.km) < EPSILON;
        }

        public double subtract(KmW other) {
            return w - other.w;
        }
    } // class KmW

    public static final WKmsOperation SUBTRACTION = new WKmsOperation() {

        @Override
        public WKms operate(WKms a, WKms b) {
            return subtract(a, b);
        }
    };

    protected WKmsOperation() {
    }

    public abstract WKms operate(WKms a, WKms b);

    /**
     * Subtract two series from each other, interpolate values
     * missing in one series in the other.
     */
    public static WKms subtract(WKms minuend, WKms subtrahend) {

        int M = minuend   .size();
        int S = subtrahend.size();

        // Don't subtract empty sets
        if (M < 1 || S < 1) {
            return new WKmsImpl();
        }

        KmW [] ms = new KmW[M];
        KmW [] ss = new KmW[S];

        for (int i = 0; i < M; ++i) {
            ms[i] = new KmW(minuend.getKm(i), minuend.getW(i));
        }

        for (int i = 0; i < S; ++i) {
            ss[i] = new KmW(subtrahend.getKm(i), subtrahend.getW(i));
        }

        Arrays.sort(ms);
        Arrays.sort(ss);

        // no overlap -> empty result set
        if (ms[0].km > ss[S-1].km || ss[0].km > ms[M-1].km) {
            return new WKmsImpl();
        }

        WKmsImpl result = new WKmsImpl();

        int mi = 0;
        int si = 0;

        OUT: while (mi < M && si < S) {
            KmW m = ms[mi];
            KmW s = ss[si];

            if (m.km + EPSILON < s.km) {
                // minuend is before subtrahend

                while (ms[mi].km + EPSILON < s.km) {
                    if (++mi >= M) {
                        break OUT;
                    }
                }

                if (ms[mi].km + EPSILON > s.km) {
                    double mw = Linear.linear(
                        s.km,
                        ms[mi-1].km, ms[mi].km,
                        ms[mi-1].w,  ms[mi].w);
                    result.add(s.km, mw - s.w);
                    ++si;
                }
                else { // s.km == ms[mi].km
                    result.add(s.km, ms[mi].subtract(s));
                    ++mi;
                    ++si;
                }
            }
            else if (m.km > s.km + EPSILON) {
                // subtrahend is before minuend

                while (m.km > ss[si].km + EPSILON) {
                    if (++si >= S) {
                        break OUT;
                    }
                }

                if (ss[si].km + EPSILON > m.km) {
                    double sw = Linear.linear(
                        m.km,
                        ss[si-1].km, ss[si].km,
                        ss[si-1].w,  ss[si].w);
                    result.add(m.km, m.w - sw);
                    ++mi;
                }
                else { // ss[si].km == m.km
                    result.add(m.km, m.subtract(ss[si]));
                    ++mi;
                    ++si;
                }
            }
            else { // m.km == s.km
                result.add(s.km, m.subtract(s));
                ++mi;
                ++si;
            }
        }

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
