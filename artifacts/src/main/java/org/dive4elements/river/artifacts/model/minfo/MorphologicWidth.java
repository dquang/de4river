/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.HashMap;
import java.util.Map;


public class MorphologicWidth
{

    private Map<Double, Double> pairs;


    public MorphologicWidth() {
        pairs = new HashMap<Double, Double>();
    }

    public void add(double station, double width) {
        this.pairs.put(station, width);
    }

    public Map<Double, Double> getAll() {
        return this.pairs;
    }

    public double[][] getAsArray() {
        double [][] array = new double[2][pairs.size()];
        Double[] kms = pairs.keySet().toArray(new Double[pairs.size()]);
        Double[] width = pairs.values().toArray(new Double[pairs.size()]);
        int realIndex = 0;
        for (int i = 0; i < kms.length; i++) {
            if (kms[i] == null || width[i] == null) {
                continue;
            }
            array[0][realIndex] = kms[i];
            array[1][realIndex] = width[i];
            realIndex++;
        }
        return array;
    }


    public Double getWidth(double station) {
        if (this.pairs.containsKey(station)) {
            return this.pairs.get(station);
        }
        return null;
    }
}
