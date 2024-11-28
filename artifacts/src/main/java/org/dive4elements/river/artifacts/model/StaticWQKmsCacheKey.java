/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

/**
 * Caching-Key object for 'static' wst- data.
 */
public final class StaticWQKmsCacheKey
implements         Serializable
{
    public static final String CACHE_NAME = "wst-wq-value-table-static";

    private int column;
    private int wst_id;

    public StaticWQKmsCacheKey(int column, int wst_id) {
        this.wst_id  = wst_id;
        this.column  = column;
    }

    public int hashCode() {
        return (wst_id << 8) | column;
    }

    public boolean equals(Object other) {
        if (!(other instanceof StaticWQKmsCacheKey)) {
            return false;
        }
        StaticWQKmsCacheKey o = (StaticWQKmsCacheKey) other;
        return this.wst_id == o.wst_id && this.column == o.column;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
