/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Attribute;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportAttribute
implements   Comparable<ImportAttribute>
{
    protected String value;

    protected Attribute peer;

    public ImportAttribute() {
    }

    public ImportAttribute(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int compareTo(ImportAttribute other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof ImportAttribute)) return false;
        return value.equals(((ImportAttribute)other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public Attribute getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from Attribute where value=:value");
            query.setString("value", value);
            List<Attribute> attributes = query.list();
            if (attributes.isEmpty()) {
                peer = new Attribute(value);
                session.save(peer);
            }
            else {
                peer = attributes.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
