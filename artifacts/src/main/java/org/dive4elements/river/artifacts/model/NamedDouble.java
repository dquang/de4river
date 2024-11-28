/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

/**
 * Implementation of a <String,double> pair.
 */
public class NamedDouble
extends      NamedObjectImpl
{
    protected double value;


    /**
     * @param name  name for the given value.
     * @param value value.
     */
    public NamedDouble(String name, double value) {
        super(name);
        this.value = value;
    }


    /**
     * Get the value.
     * @return the value.
     */
    public double getValue() {
        return this.value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
