/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.MainValueType;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportMainValueType
implements   Comparable<ImportMainValueType>
{
    protected String name;

    protected MainValueType peer;

    public ImportMainValueType() {
    }

    public ImportMainValueType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(ImportMainValueType other) {
        return name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof ImportMainValueType)) return false;
        return name.equals(((ImportMainValueType)other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public MainValueType getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from MainValueType where name=:name");
            query.setString("name", name);
            List<MainValueType> values = query.list();
            if (values.isEmpty()) {
                peer = new MainValueType(name);
                session.save(peer);
            }
            else {
                peer = values.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
