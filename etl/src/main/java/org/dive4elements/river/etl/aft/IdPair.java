/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

public class IdPair
{
    protected int id1;
    protected int id2;

    public IdPair() {
    }

    public IdPair(int id1) {
        this.id1 = id1;
    }

    public IdPair(int id1, int id2) {
        this(id1);
        this.id2 = id2;
    }

    public int getId1() {
        return id1;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public String toString() {
        return "[IdPair: id1=" + id1 + ", id2=" + id2 + "]";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
