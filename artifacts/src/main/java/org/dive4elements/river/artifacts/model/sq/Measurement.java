/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.List;
import java.util.Map;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Measurement
{
    private static final Logger log = LogManager.getLogger(Measurement.class);

    public static final double LOG_10_8 = Math.log(10) - Math.log(8);
    public static final double SCALE_8 = Math.log(10) - Math.log(6.3);

    public static final double LOG_8_6 = Math.log(8) - Math.log(6.3);
    public static final double SCALE_4 = Math.log(10) - Math.log(6.3);

    protected Map<String, Object> data;

    protected List<Sieve> sieves;

    protected SieveArray sieveArray;

    public Measurement() {
    }

    public Measurement(Map<String, Object> data, List<Sieve> sieves) {
        this.data = data;
        this.sieves = sieves;
        if (sieves != null && !sieves.isEmpty()) {
            adjustSieves();
        }
    }

    protected double get(String name) {
        Number value = (Number)data.get(name);
        return value != null ? value.doubleValue() : Double.NaN;
    }

    protected void set(String name, double value) {
        data.put(name, Double.valueOf(value));
    }

    public Object getData(String name) {
        return data.get(name);
    }

    public Map<String, Object> getData() {
        return data;
    }

    protected void putData(String name, Object value) {
        data.put(name, value);
    }

    public double S_SS() {
        return get("TSAND");
    }

    public double S_SF() {
        return get("TSCHWEB") - get("TSAND");
    }

    public double Q() {
        return get("Q_BPEGEL");
    }

    public double TOTAL_BL() {
        return get("TGESCHIEBE");
    }

    public double BL_G() {
        return get("BL_G");
    }

    public double BL_C() {
        return get("BL_C");
    }

    public double BL_S() {
        return get("BL_S");
    }

    public double S_BL_S() {
        return TOTAL_BL() * BL_S();
    }

    public double S_BL_FG() {
        return TOTAL_BL() * BL_G();
    }

    public double S_BL_CG() {
        return TOTAL_BL() * BL_C();
    }

    public double S_BL_1() {
        return S_BL_S() + S_BL_FG() + S_BL_CG();
    }

    public double S_BL_2() {
        return S_SS() + S_BL_S() + S_BL_FG() + S_BL_CG();
    }

    public Date getDate() {
        return  (Date)data.get("DATUM");
    }

    @Override
    public String toString() {
        return "Measurement: " + data;
    }

    /**
     * Gets the sieves for this instance.
     *
     * @return The sieves.
     */
    public List<Sieve> getSieves() {
        return this.sieves;
    }

    /**
     * Gets the sieveArray for this instance.
     *
     * @return The sieveArray.
     */
    public SieveArray getSieveArray() {
        if (sieveArray == null) {
            sieveArray = calculateSieveArray();
        }
        // XXX: @rrenkert: Why did you place the adjument here?
        // adjustSieves();
        return sieveArray;
    }

    protected Sieve findSieve(double diameter) {
        for (Sieve s: sieves) {
            if (s.matchesDiameter(diameter)) {
                return s;
            }
        }
        return null;
    }

    protected void deleteSieve(double diameter) {
        for (int i = sieves.size()-1; i >= 0; --i) {
            if (sieves.get(i).matchesDiameter(diameter)) {
                sieves.remove(i);
                break;
            }
        }
    }

    public void adjustSieves() {

        // If we already have an 8mm diameter sieve
        // we dont need to 'invent' it.
        if (findSieve(8d) != null) {
            return;
        }

        // create a new 8mm sieve.
        // delete 6.3mm sieve.
        // modify 4mm sieve.

        Sieve six  = findSieve(6.3d);
        Sieve ten  = findSieve(10d);
        Sieve four = findSieve(4d);

        if (six == null || ten == null || four == null) {
            log.warn("missing diameter");
            return;
        }

        double sixValue  = six.getLoad();
        double tenValue  = ten.getLoad();
        double fourValue = four.getLoad();

        deleteSieve(6.3);

        double eightValue = ((LOG_10_8 / SCALE_8*sixValue) + tenValue);
        double newFourValue = ((LOG_8_6 / SCALE_4*sixValue) + fourValue);

        deleteSieve(4.0);
        sieves.add(new Sieve(8d, eightValue));
        sieves.add(new Sieve(4d, newFourValue));

    }

    protected SieveArray calculateSieveArray() {

        SieveArray sa = new SieveArray();

        for (Sieve s: sieves) {
            sa.doSieving(s);
        }

        sa.calculateNormLoads();

        return sa;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
