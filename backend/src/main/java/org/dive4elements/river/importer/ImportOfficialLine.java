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
import org.dive4elements.river.model.OfficialLine;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.WstColumn;
import org.hibernate.Query;
import org.hibernate.Session;

public class ImportOfficialLine
{
    protected String name;
    protected ImportWstColumn wstColumn;

    protected OfficialLine peer;

    public ImportOfficialLine() {
    }

    public ImportOfficialLine(String name, ImportWstColumn wstColumn) {
        this.name = name;
        this.wstColumn = wstColumn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OfficialLine getPeer(River river) {
        if (peer == null) {
            // XXX: This is a bit odd. We do not have not enough infos here
            // to create a new NamedMainValue.
            // So we just look for existing ones.
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            NamedMainValue nmv = NamedMainValue.fetchByNameAndType(
                name, "Q", session);
            if (nmv == null) {
                // failed -> failed to create OfficialLine
                return null;
            }
            WstColumn wc = wstColumn.getPeer(river);
            Query query = session.createQuery(
                "from OfficialLine " +
                "where namedMainValue = :nmv and wstColumn = :wc");
            query.setParameter("nmv", nmv);
            query.setParameter("wc", wc);
            List<OfficialLine> lines = query.list();
            if (lines.isEmpty()) {
                peer = new OfficialLine(wc, nmv);
                session.save(peer);
            }
            else {
                peer = lines.get(0);
            }

        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

