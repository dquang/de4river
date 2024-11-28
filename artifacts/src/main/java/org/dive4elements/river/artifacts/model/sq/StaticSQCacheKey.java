/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.io.Serializable;


public class StaticSQCacheKey
implements Serializable
{
    public static final String CACHE_NAME = "static-sq-relation";

    private String river;
    private int measurementId;

    public StaticSQCacheKey(String river, int measurementId) {
        this.river = river;
        this.measurementId = measurementId;
    }

    public int hashCode() {
        return this.river.hashCode() | measurementId;
    }

    public boolean equals(Object other) {
        if (!(other instanceof StaticSQCacheKey)) {
            return false;
        }
        StaticSQCacheKey o = (StaticSQCacheKey) other;
        return this.river == o.river && this.measurementId == o.measurementId;
    }
}
