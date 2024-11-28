/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.math.Linear;


/** Has to do with adaptive smoothing based on current diagram extent. */
public class ZoomScale
{
    private static Logger log = LogManager.getLogger(ZoomScale.class);

    private HashMap<String, TreeMap<Double, Double>> rivers;

    public ZoomScale() {
        this.rivers = new HashMap<String, TreeMap<Double, Double>>();
    }

    public ZoomScale(String river) {
        this();
        rivers.put(river, new TreeMap<Double, Double>());
    }

    public double getRadius(String river, double lower, double upper) {
        if (lower > upper) {
            double buf = lower;
            lower = upper;
            upper = buf;
        }
        double range = Math.abs(upper) - Math.abs(lower);
        TreeMap<Double, Double> ranges = rivers.get(river);
        if (ranges == null) {
            TreeMap<Double, Double> defaultRanges = rivers.get("default");
            if (defaultRanges == null) {
                return 0.001;
            }
            ranges = defaultRanges;
        }
        Map.Entry<Double, Double> next = ranges.higherEntry(range);
        Map.Entry<Double, Double> prev = ranges.lowerEntry(range);
        double x0 = 0d;
        double x1 = 0d;
        double y0 = 0d;
        double y1 = 0d;
        if (prev == null && next != null) {
            x1 = next.getKey();
            y1 = next.getValue();
        }
        else if (prev != null && next == null) {
            return prev.getValue();
        }
        else {
            x0 = prev.getKey();
            x1 = next.getKey();
            y0 = prev.getValue();
            y1 = next.getValue();
        }
        return Linear.linear(range, x0, x1, y0, y1);
    }

    public void addRiver(String river) {
        if (!this.rivers.containsKey(river)) {
            this.rivers.put(river, new TreeMap<Double, Double>());
        }
    }

    public Set<String> getRivers() {
        return this.rivers.keySet();
    }

    public void addRange(String river, double range, double radius) {
        if (this.rivers.containsKey(river)) {
            this.rivers.get(river).put(range, radius);
        }
        else {
            this.rivers.put(river, new TreeMap<Double, Double>());
            this.rivers.get(river).put(range, radius);
        }
    }
}
