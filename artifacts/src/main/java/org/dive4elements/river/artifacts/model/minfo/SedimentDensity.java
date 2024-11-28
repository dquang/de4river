/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.common.utils.Config;


/** Sediment Densities for multiple years. */
public class SedimentDensity implements Serializable
{
    private static final Logger log = LogManager
        .getLogger(SedimentDensity.class);

    public static final double DEFAULT_SEDIMNET_DENSITY_FACTOR = 1.9;

    public static String SEDIMENT_DENSITY_FACTOR_XPATH =
        "/artifact-database/options/sediment-density-factor/text()";

    public static final double SEDIMNET_DENSITY_FACTOR =
        getSedimentDensityFactor();

    private TreeMap<Integer, List<SedimentDensityValue>> densities;


    /** Figures out the sediment density factor from global config. */
    private static final double getSedimentDensityFactor() {

        double factor = DEFAULT_SEDIMNET_DENSITY_FACTOR;

        String factorString =
            Config.getStringXPath(SEDIMENT_DENSITY_FACTOR_XPATH);

        if (factorString != null) {
            try {
                factor = Double.parseDouble(factorString.trim());
            }
            catch (NumberFormatException nfe) {
                log.error(nfe);
            }
        }

        log.info("Sedmiment density factor: " + factor);

        return factor;
    }

    public SedimentDensity() {
        densities = new TreeMap<Integer, List<SedimentDensityValue>>();
    }

    public Map<Integer, List<SedimentDensityValue>> getDensities() {
        return densities;
    }

    private static final Comparator<SedimentDensityValue> BY_KM =
        new Comparator<SedimentDensityValue>() {
            @Override
            public int compare(
                SedimentDensityValue a,
                SedimentDensityValue b
            ) {
                double diff = a.getKm() - b.getKm();
                if (diff < 0.0) return -1;
                if (diff > 0.0) return +1;
                return 0;
            }
        };

    public void addDensity(double km, double density, int year) {

        if (log.isDebugEnabled()) {
            log.debug("adding " + year);
        }

        Integer key = Integer.valueOf(year);

        List<SedimentDensityValue> list = densities.get(key);

        if (list == null) {
            list = new ArrayList<SedimentDensityValue>();
            densities.put(key, list);
        }

        // Keep list sorted by km.
        SedimentDensityValue sdv = new SedimentDensityValue(km, density, year);
        int index = Collections.binarySearch(list, sdv, BY_KM);

        if (index < 0) {
            // index = -(insertion point) - 1
            // -(index + 1) = insertion point
            index = -(index + 1);
        }

        list.add(index, sdv);
    }

    /**
     * Get the density at year.
     * Measured densities are valid until the next measurement.
     * If no measurement was found 1.8 is returned.
     */
    public double getDensity(double km, int year) {

        if (densities.isEmpty()) {
            return SEDIMNET_DENSITY_FACTOR;
        }

        if (densities.size() == 1) {
            Map.Entry<Integer, List<SedimentDensityValue>> entry =
                densities.firstEntry();
            return entry.getKey() <= year
                ? getDensityAtKm(entry.getValue(), km)
                : SEDIMNET_DENSITY_FACTOR;
        }

        Iterator<Map.Entry<Integer, List<SedimentDensityValue>>> iter =
            densities.entrySet().iterator();

        Map.Entry<Integer, List<SedimentDensityValue>> last = iter.next();

        while (iter.hasNext()) {
            Map.Entry<Integer, List<SedimentDensityValue>> current =
                iter.next();
            last = current;
            int y1 = last.getKey();
            int y2 = current.getKey();
            if (year >= y1 && year < y2) {
                return getDensityAtKm(last.getValue(), km);
            }
            if (year >= y2 && !iter.hasNext()) {
                return getDensityAtKm(current.getValue(), km);
            }
        }

        return SEDIMNET_DENSITY_FACTOR;
    }

    /** Get (sorted) map of km to density of all years. */
    public double[][] getAllDensities()
    {
        TreeMap<Double, Double> map = new TreeMap<Double,Double>();
        // XXX: This looks stupid.
        for (List<SedimentDensityValue> sdvs: densities.values()) {
            for (SedimentDensityValue sdv: sdvs) {
                map.put(sdv.getKm(), sdv.getDensity());
            }
        }
        double[][] points = new double[2][map.keySet().size()];
        int i = 0;
        for (Map.Entry<Double, Double> kmDens: map.entrySet()) {
            points[0][i] = kmDens.getKey();
            points[1][i] = kmDens.getValue();
            i++;
        }

        return points;
    }

    /** Get points  km,density (sorted by km), for a given year. */
    public double[][] getDensities(int year)
    {
        List<SedimentDensityValue> list = densities.get(year);
        if (list == null) {
            return new double[2][0];
        }
        // List is sorted in km.
        double[][] points = new double[2][list.size()];
        int i = 0;
        for (SedimentDensityValue sdv: list) {
            points[0][i] = sdv.getKm();
            points[1][i] = sdv.getDensity();
            i++;
        }

        return points;
    }

    /** Get value at km, interpolated. */
    private static double getDensityAtKm(
        List<SedimentDensityValue> values,
        double km
    ) {
        SedimentDensityValue prev = null;
        SedimentDensityValue next = null;
        for (SedimentDensityValue sdv: values) {
            if (Math.abs(sdv.getKm() - km) < 0.00001) {
                return prev.getDensity();
            }
            if (sdv.getKm() > km) {
                next = sdv;
                break;
            }
            prev = sdv;
        }
        return spline(prev, next, km);
    }

    /** Linearly interpolate between density values. */
    private static double spline(
        SedimentDensityValue prev,
        SedimentDensityValue next,
        double km
    ) {
        if (prev == null && next == null) {
            log.warn("prev and next are null -> NaN");
            return Double.NaN;
        }

        if (prev == null) return next.getDensity();
        if (next == null) return prev.getDensity();

        // XXX: This is no spline interpolation!
        double lower = prev.getKm();
        double upper = next.getKm();
        double upperDensity = next.getDensity();
        double lowerDensity = prev.getDensity();

        double m = (upperDensity - lowerDensity)/(upper - lower);
        double b = lowerDensity - (m * lower);
        return m * km + b;
    }


    /** If multiple values for same year and station are found,
     * build and store average, dismiss multiple values. */
    public void cleanUp() {
        Set<Integer> keys = densities.keySet();
        // Walk over years
        for (Integer key : keys) {
            List<SedimentDensityValue> list = densities.get(key);
            if (list.size() == 0) {
                return;
            }
            List<SedimentDensityValue> cleaned =
                new ArrayList<SedimentDensityValue>();
            double prevkm = list.get(0).getKm();
            int counter = 0;
            double sum = 0d;
            for (SedimentDensityValue value : list) {
                // Apparently we can assume that values are ordered by km.
                if (value.getKm() == prevkm) {
                    sum += value.getDensity();
                    counter++;
                }
                else {
                    cleaned.add(new SedimentDensityValue(
                        prevkm,
                        sum / counter,
                        value.getYear()));
                    sum = value.getDensity();
                    counter = 1;
                }
                prevkm = value.getKm();
            }
            this.densities.put(key, cleaned);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
