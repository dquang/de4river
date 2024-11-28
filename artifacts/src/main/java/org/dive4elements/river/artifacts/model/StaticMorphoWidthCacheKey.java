/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;


public class StaticMorphoWidthCacheKey
{
   public static final String CACHE_NAME = "morpho-width-table-static";

    private int width_id;

    public StaticMorphoWidthCacheKey(
        int width_id
    ) {
        this.width_id = width_id;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(width_id)).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StaticMorphoWidthCacheKey)) {
            return false;
        }
        StaticMorphoWidthCacheKey o = (StaticMorphoWidthCacheKey) other;
        return this.width_id == o.width_id;
    }
}
