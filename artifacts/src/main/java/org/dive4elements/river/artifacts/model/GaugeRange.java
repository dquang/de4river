/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Gauge, km-range, main values.
 */
public class GaugeRange
extends      Range
{
    private static Logger log = LogManager.getLogger(GaugeRange.class);

    private static final class Sector implements Serializable {

        int    sector;
        double value;

        Sector(int sector, double value) {
            this.sector = sector;
            this.value  = value;
        }
    } // class Sector

    protected String name;

    protected int gaugeId;

    /** Certain main value. */
    protected Map<String, Double> mainValues;

    protected List<Sector> sectors;


    public GaugeRange() {
    }


    public GaugeRange(double start, double end, int gaugeId) {
        this(start, end, null, gaugeId);
    }


    public GaugeRange(
        double start,
        double end,
        String name,
        int    gaugeId
    ) {
        super(start, end);
        this.name = name;
        this.gaugeId = gaugeId;
        mainValues = new HashMap<String, Double>();
        sectors = new ArrayList<Sector>(3);
    }


    public void addMainValue(String label, Double value) {
        int idx = label.indexOf('(');
        if (idx >= 0) {
            label = label.substring(0, idx);
        }
        mainValues.put(label, value);
    }


    protected Double getMainValue(String label) {
        Double v = mainValues.get(label);
        if (v == null) {
            log.warn("Missing main value '"
                + label + "' for gauge " + gaugeId);
        }
        return v;
    }


    public Map<String, Double> getMainValues() {
        return mainValues;
    }


    public void buildClasses() {
        Double mnq = getMainValue("MNQ");
        Double mq  = getMainValue("MQ");
        Double mhq = getMainValue("MHQ");
        Double hq5 = getMainValue("HQ5");

        Double [][] pairs = {
            { mnq,  mq },
            {  mq, mhq },
            { hq5, hq5 } };

        for (int c = 0; c < pairs.length; ++c) {
            Double [] pair = pairs[c];
            if (pair[0] != null && pair[1] != null) {
                double value = 0.5*(pair[0] + pair[1]);
                sectors.add(new Sector(c, value));
            }
        }
    }


    public double getSectorBorder(int sector) {
        for (Sector s: sectors) {
            if (s.sector == sector) {
                return s.value;
            }
        }
        return Double.NaN;
    }


    public int classify(double value) {
        for (Sector sector: sectors) {
            if (value < sector.value) {
                return sector.sector;
            }
        }
        return sectors.size();
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder("sectors: [");

        for (int i = 0, S = sectors.size(); i < S; ++i) {
            if (i > 0) sb.append(", ");
            Sector s = sectors.get(i);
            sb.append(s.sector).append(": ").append(s.value);;
        }

        sb.append("] mainvalues: ").append(mainValues);

        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
