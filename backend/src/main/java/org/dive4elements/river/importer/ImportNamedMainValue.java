/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;

import org.dive4elements.river.model.NamedMainValue;
import org.dive4elements.river.model.MainValueType;

import org.hibernate.Session;
import org.hibernate.Query;

public class ImportNamedMainValue
{
    protected ImportMainValueType mainValueType;
    protected String              name;

    protected NamedMainValue      peer;

    public ImportNamedMainValue() {
    }

    public ImportNamedMainValue(
        ImportMainValueType mainValueType,
        String              name
    ) {
        this.mainValueType = mainValueType;
        this.name          = name;
    }

    public ImportMainValueType getMainValueType() {
        return mainValueType;
    }

    public void setMainValueType(ImportMainValueType mainValueType) {
        this.mainValueType = mainValueType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NamedMainValue getPeer() {
        if (peer == null) {
            MainValueType type = mainValueType.getPeer();
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from NamedMainValue where " +
                "name=:name and type.id=:id");
            query.setString("name", name);
            query.setParameter("id", type.getId());
            List<NamedMainValue> named = query.list();
            if (named.isEmpty()) {
                peer = new NamedMainValue(name, type);
                session.save(peer);
            }
            else {
                peer = named.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
