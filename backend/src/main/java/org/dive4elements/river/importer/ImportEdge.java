/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Edge;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

import java.math.BigDecimal;

public class ImportEdge
implements   Comparable<ImportEdge>
{
    protected BigDecimal top;
    protected BigDecimal bottom;

    protected Edge peer;

    public ImportEdge() {
    }

    public ImportEdge(BigDecimal top, BigDecimal bottom) {
        this.top    = top;
        this.bottom = bottom;
    }

    public BigDecimal getTop() {
        return top;
    }

    public void setTop(BigDecimal top) {
        this.top = top;
    }

    public BigDecimal getBottom() {
        return bottom;
    }

    public void setBottom(BigDecimal bottom) {
        this.bottom = bottom;
    }

    private static final int compare(BigDecimal a, BigDecimal b) {
        if (a == null && b != null) return -1;
        if (a != null && b == null) return +1;
        if (a == null && b == null) return  0;
        return a.compareTo(b);
    }

    public int compareTo(ImportEdge other) {
        int cmp = compare(top, other.top);
        return cmp != 0 ? cmp : compare(bottom, other.bottom);
    }

    public Edge getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from Edge where top=:top and bottom=:bottom");
            query.setParameter("top", top);
            query.setParameter("bottom", bottom);
            List<Edge> edges = query.list();
            if (edges.isEmpty()) {
                peer = new Edge(top, bottom);
                session.save(peer);
            }
            else {
                peer = edges.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
