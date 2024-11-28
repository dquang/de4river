/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

public class NamedObjectImpl
implements   NamedObject
{
    /** The name of this object.*/
    protected String name;

    public NamedObjectImpl() {
    }

    public NamedObjectImpl(String name) {
        this.name = name;
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
