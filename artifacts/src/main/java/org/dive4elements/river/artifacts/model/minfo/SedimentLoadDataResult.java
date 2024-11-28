/* Copyright (C) 2016 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.model.minfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SedimentLoadDataResult
implements   Serializable
{

    public static class Fraction implements Serializable {
        private String      name;
        /* Period is the validity of the result. It is either a single
         * year or a range of years (epoch). As this is only used for
         * presentation purposes the type is a string so that years
         * and epochs need not be handled differently.*/
        private String      period;
        private double [][] data;

        public Fraction() {
        }

        public Fraction(String name, double [][] data, String period) {
            this.name = name;
            this.data = data;
            this.period = period;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double [][] getData() {
            return data;
        }

        public void setData(double [][] data) {
            this.data = data;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getPeriod() {
            return period;
        }

    } // class Fraction

    private List<Fraction> fractions;

    public SedimentLoadDataResult() {
        fractions = new ArrayList<Fraction>();
    }

    public void addFraction(Fraction fraction) {
        fractions.add(fraction);
    }

    public List<Fraction> getFractions() {
        return fractions;
    }

    public List<Fraction> getFractionsByName(String name) {
        List<Fraction> result = new ArrayList<Fraction>();
        for (Fraction fraction: fractions) {
            if (fraction.getName().equals(name)) {
                result.add(fraction);
            }
        }
        return result.isEmpty() ? null : result;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
