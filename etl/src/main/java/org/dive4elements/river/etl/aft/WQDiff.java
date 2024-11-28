/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import org.dive4elements.river.etl.db.ConnectedStatements;
import org.dive4elements.river.etl.db.SymbolicStatement;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class WQDiff
{
    protected Set<WQ> toAdd;
    protected Set<WQ> toDelete;

    public WQDiff() {
    }

    public WQDiff(Collection<WQ> a, Collection<WQ> b) {
        toAdd    = new TreeSet<WQ>(WQ.EPS_CMP);
        toDelete = new TreeSet<WQ>(WQ.EPS_CMP);
        build(a, b);
    }

    public void build(Collection<WQ> a, Collection<WQ> b) {
        toAdd.addAll(b);
        toAdd.removeAll(a);

        toDelete.addAll(a);
        toDelete.removeAll(b);
    }

    public void clear() {
        toAdd.clear();
        toDelete.clear();
    }

    public Set<WQ> getToAdd() {
        return toAdd;
    }

    public void setToAdd(Set<WQ> toAdd) {
        this.toAdd = toAdd;
    }

    public Set<WQ> getToDelete() {
        return toDelete;
    }

    public void setToDelete(Set<WQ> toDelete) {
        this.toDelete = toDelete;
    }

    public boolean hasChanges() {
        return !(toAdd.isEmpty() && toDelete.isEmpty());
    }

    public void writeChanges(
        SyncContext context,
        int         tableId
    )
    throws SQLException
    {
        ConnectedStatements flysStatements = context.getFlysStatements();

        // Delete the old entries
        if (!toDelete.isEmpty()) {
            SymbolicStatement.Instance deleteDTV =
                flysStatements.getStatement("delete.discharge.table.value");
            for (WQ wq: toDelete) {
                deleteDTV
                    .clearParameters()
                    .setInt("id", wq.getId())
                    .execute();
            }
        }

        // Add the new entries.
        if (!toAdd.isEmpty()) {
            SymbolicStatement.Instance nextId =
                flysStatements.getStatement("next.discharge.table.values.id");

            SymbolicStatement.Instance insertDTV =
                flysStatements.getStatement("insert.discharge.table.value");

            // Recycle old ids as much as possible.
            Iterator<WQ> oldIds = toDelete.iterator();

            // Create ids for new entries.
            for (WQ wq: toAdd) {
                if (oldIds.hasNext()) {
                    wq.setId(oldIds.next().getId());
                }
                else {
                    ResultSet rs = nextId.executeQuery();
                    rs.next();
                    wq.setId(rs.getInt("discharge_table_values_id"));
                    rs.close();
                }
            }

            // Write the new entries.
            for (WQ wq: toAdd) {
                insertDTV
                    .clearParameters()
                    .setInt("id", wq.getId())
                    .setInt("table_id", tableId)
                    .setDouble("w", wq.getW())
                    .setDouble("q", wq.getQ())
                    .execute();
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
