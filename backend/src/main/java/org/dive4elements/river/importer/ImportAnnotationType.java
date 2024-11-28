/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.AnnotationType;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportAnnotationType
implements   Comparable<ImportAnnotationType>
{
    protected String         name;
    protected AnnotationType peer;

    public ImportAnnotationType() {
    }

    public ImportAnnotationType(String name) {
        this.name = name;
    }

    public int compareTo(ImportAnnotationType other) {
        return name.compareTo(other.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public AnnotationType getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from AnnotationType where name=:name");
            query.setParameter("name", name);
            List<AnnotationType> types = query.list();
            if (types.isEmpty()) {
                peer = new AnnotationType(name);
                session.save(peer);
            }
            else {
                peer = types.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
