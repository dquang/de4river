/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class MovingAverage
{

    public static double[][] simple(double[][] values, double radius) {
        TreeMap<Double, Double> map = toMap(values);
        int N = map.size();
        double [] xs = new double[N];
        double [] ys = new double[N];
        int ndx = 0;
        for (double x: map.keySet()) {
            SortedMap<Double, Double> range =
                map.subMap(x-radius, true, x+radius, true);
            double avg = 0d;
            for (double v: range.values()) {
                avg += v;
            }
            avg /= range.size();
            xs[ndx] = x;
            ys[ndx] = avg;
            ndx++;
        }
        return new double [][] { xs, ys };
    }

    /** Build moving average over values. Weight them. */
    public static double[][] weighted(
        double[][] values,
        double radius
    ) {
        TreeMap<Double, Double> map = toMap(values);
        int N = map.size();
        double [] xs = new double[N];
        double [] ys = new double[N];
        int ndx = 0;
        double _1radius = 1d/radius;
        for (double x: map.keySet()) {
            double avg = 0d;
            double weights = 0d;
            for (Map.Entry<Double, Double> e:
                map.subMap(x-radius, false, x+radius, false).entrySet()
            ) {
                double weight = 1d - Math.abs(x - e.getKey())*_1radius;
                avg += weight*e.getValue();
                weights += weight;
            }
            avg /= weights;
            xs[ndx] = x;
            ys[ndx] = avg;
            ndx++;
        }
        return new double [][] { xs, ys };
    }

    /** From [x1,x2][y1,y2] makes {x1:y1,x2:y2}. Sorted by x! */
    private static TreeMap<Double, Double> toMap(double[][] values) {
        TreeMap<Double, Double> map = new TreeMap<Double, Double>();
        double [] xs = values[0];
        double [] ys = values[1];
        for (int i = 0; i < xs.length; i++) {
            map.put(xs[i], ys[i]);
        }
        return map;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
