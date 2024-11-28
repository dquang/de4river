/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;

import org.dive4elements.river.model.WstQRange;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Range;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportWstQRange
{
    protected ImportRange range;
    protected BigDecimal  q;

    protected WstQRange peer;

    public ImportWstQRange() {
    }

    public ImportWstQRange(
        ImportRange range,
        BigDecimal  q
    ) {
        this.range = range;
        this.q     = q;
    }

    public ImportWstQRange(
        BigDecimal a,
        BigDecimal b,
        BigDecimal q
    ) {
        this.range = new ImportRange(a, b);
        this.q     = q;
    }

    public ImportRange getRange() {
        return range;
    }

    public void setRange(ImportRange range) {
        this.range = range;
    }

    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    public WstQRange getPeer(River river) {
        if (peer == null) {
            Range r = range.getPeer(river);
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from WstQRange where " +
                "range=:range and q=:q");
            query.setParameter("range", r);
            query.setParameter("q",     q);
            List<WstQRange> wstQRanges = query.list();
            if (wstQRanges.isEmpty()) {
                peer = new WstQRange(r, q);
                session.save(peer);
            }
            else {
                peer = wstQRanges.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
