/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Position;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportPosition
implements   Comparable<ImportPosition>
{
    protected String value;

    protected Position peer;

    public ImportPosition() {
    }

    public ImportPosition(String value) {
        this.value = value;
    }

    public int compareTo(ImportPosition other) {
        return value.compareTo(other.value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Position getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from Position where value=:value");
            query.setString("value", value);
            List<Position> positions = query.list();
            if (positions.isEmpty()) {
                peer = new Position(value);
                session.save(peer);
            }
            else {
                peer = positions.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

