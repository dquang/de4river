/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;


public class StaticHWSCacheKey
{
    public static final String CACHE_NAME = "hws-value-table-static";

    private String river;
    private int type;

    public StaticHWSCacheKey(String river, int type) {
        this.river = river;
        this.type = type;
    }

    public int hashCode() {
        return river.hashCode() | (type << 8);
    }

    public boolean equals(Object other) {
        if (!(other instanceof StaticHWSCacheKey)) {
            return false;
        }
        StaticHWSCacheKey o = (StaticHWSCacheKey) other;
        return this.river == o.river;
    }
}
