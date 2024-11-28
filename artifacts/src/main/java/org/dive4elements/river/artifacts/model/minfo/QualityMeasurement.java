/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** A measurement of the bed quality, serving different diameter at given km. */
public class QualityMeasurement {
    private static Logger log = LogManager.getLogger(QualityMeasurement.class);

    private double              km;
    private Date                date;
    private double              depth1;
    private double              depth2;
    private Map<String, Double> charDiameter;

    private QualityMeasurement() {

    }

    public QualityMeasurement(
        double km,
        Date date,
        double depth1,
        double depth2,
        Map<String, Double> diameter) {
        this.setKm(km);
        this.setDate(date);
        this.depth1 = depth1;
        this.depth2 = depth2;
        this.setDiameter(diameter);
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, Double> getAllDiameter() {
        return charDiameter;
    }

    public void setDiameter(Map<String, Double> charDiameter) {
        this.charDiameter = charDiameter;
    }

    /**
     * Get the stored diameter for given key (e.g. d10).
     * @return NaN if no data found in this measurement.
     */
    public double getDiameter(String key) {
        Double diameter = charDiameter.get(key);
        if (diameter == null) {
            log.warn("No Diameter at km " + km + " for " + key);
        }
        return (diameter != null) ? diameter : Double.NaN;
    }

    public void setDiameter(String key, double value) {
        charDiameter.put(key, value);
    }

    public double getDepth1() {
        return depth1;
    }

    public void setDepth1(double depth1) {
        this.depth1 = depth1;
    }

    public double getDepth2() {
        return depth2;
    }

    public void setDepth2(double depth2) {
        this.depth2 = depth2;
    }
}
