/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;


public class StaticPorosityCacheKey
implements   Serializable
{
   public static final String CACHE_NAME = "porosity-table-static";

    private int porosityId;

    public StaticPorosityCacheKey(
        int porosityId
    ) {
        this.porosityId = porosityId;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(porosityId)).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StaticPorosityCacheKey)) {
            return false;
        }
        StaticPorosityCacheKey o = (StaticPorosityCacheKey) other;
        return this.porosityId == o.porosityId;
    }
}
