/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.WstColumnQRange;
import org.dive4elements.river.model.WstQRange;
import org.dive4elements.river.model.WstColumn;
import org.dive4elements.river.model.River;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportWstColumnQRange
{
    protected ImportWstColumn wstColumn;
    protected ImportWstQRange qRange;

    protected WstColumnQRange peer;

    public ImportWstColumnQRange() {
    }

    public ImportWstColumnQRange(
        ImportWstColumn wstColumn,
        ImportWstQRange qRange
    ) {
        this.wstColumn = wstColumn;
        this.qRange    = qRange;
    }

    public ImportWstColumn getWstColumn() {
        return wstColumn;
    }

    public void setWstColumn(ImportWstColumn wstColumn) {
        this.wstColumn = wstColumn;
    }

    public ImportWstQRange getQRange() {
        return qRange;
    }

    public void setQRange(ImportWstQRange qRange) {
        this.qRange = qRange;
    }

    public WstColumnQRange getPeer(River river) {
        if (peer == null) {
            WstColumn c = wstColumn.getPeer(river);
            WstQRange q = qRange.getPeer(river);
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from WstColumnQRange where " +
                "wstColumn=:c and wstQRange=:q");
            query.setParameter("c", c);
            query.setParameter("q", q);
            List<WstColumnQRange> cols = query.list();
            if (cols.isEmpty()) {
                peer = new WstColumnQRange(c, q);
                session.save(peer);
            }
            else {
                peer = cols.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
