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
 * Cache Key (identifier) for WstValueTables.
 */
public final class WstValueTableCacheKey
implements         Serializable
{
    public static final String CACHE_NAME = "wst-value-table";

    private int riverId;
    private int kind;

    public WstValueTableCacheKey(int riverId, int kind) {
        this.riverId = riverId;
        this.kind    = kind;
    }

    public int hashCode() {
        return (riverId << 8) | kind;
    }

    public boolean equals(Object other) {
        if (!(other instanceof WstValueTableCacheKey)) {
            return false;
        }
        WstValueTableCacheKey o = (WstValueTableCacheKey)other;
        return riverId == o.riverId && kind == o.kind;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
