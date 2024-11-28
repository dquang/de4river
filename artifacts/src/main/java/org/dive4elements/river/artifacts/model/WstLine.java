/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import gnu.trove.TDoubleArrayList;


/**
 * A model class that is used to store a line of a WST.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WstLine {

    /** The kilometer value of the line.*/
    protected double km;

    /** The W values.*/
    protected TDoubleArrayList ws;

    /** The Q values.*/
    protected TDoubleArrayList qs;


    /**
     * A constructor that builds a new WstLine for a specific kilometer.
     *
     * @param km The kilometer.
     */
    public WstLine(double km) {
        this.km = km;
        this.ws = new TDoubleArrayList();
        this.qs = new TDoubleArrayList();
    }


    /**
     * Adds a pair of W/Q to this line.
     *
     * @param w The W value.
     * @param q The Q value.
     */
    public void add(double w, double q) {
        ws.add(w);
        qs.add(q);
    }


    /**
     * Returns the kilometer of this line.
     *
     * @return the kilomter of this line.
     */
    public double getKm() {
        return km;
    }


    /**
     * Returns the W value at index <i>idx</i> of this line.
     *
     * @param idx The position of the desired W value.
     *
     * @return the W at position <i>idx</i>.
     */
    public double getW(int idx) {
        return ws.size() > idx ? ws.get(idx) : -1d;
    }


    /**
     * Returns the Q value at index <i>idx</i> of this line.
     *
     * @param idx The position of the desired Q value.
     *
     * @return the Q at position <i>idx</i>.
     */
    public double getQ(int idx) {
        return qs.size() > idx ? qs.get(idx) : -1d;
    }


    /**
     * Returns the Q values of this line.
     *
     * @return the Q values of this line.
     */
    public double[] getQs() {
        return qs.toNativeArray();
    }


    /**
     * Returns the number of columns this line consists of.
     *
     * @return the columns this line consists of.
     */
    public int getSize() {
        return qs.size();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
