/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

public class FacetFilter
implements   Serializable
{
    protected String name;
    protected String ids;

    public FacetFilter() {
    }

    public FacetFilter(String name, String ids) {
        this.name = name;
        this.ids  = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    /** Return false if only a or b are null, true if both are null
     * result of String.equals otherwise. */
    protected static boolean equals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null && b != null) return false;
        if (a != null && b == null) return false;
        return a.equals(b);
    }

    public boolean equals(Object other) {
        if (!(other instanceof FacetFilter)) {
            return false;
        }
        FacetFilter o = (FacetFilter)other;
        return equals(o.name, name) && equals(o.ids, ids);
    }

    public void collectIds(StringBuilder sb) {
        if (ids != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ids);
        }
    }

    public Recommendation.Facet toFacet() {
        return new Recommendation.Facet(name, ids);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
