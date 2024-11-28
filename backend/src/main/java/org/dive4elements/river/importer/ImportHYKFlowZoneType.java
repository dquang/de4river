/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.HYKFlowZoneType;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportHYKFlowZoneType
{
    private String          name;
    private HYKFlowZoneType peer;

    public ImportHYKFlowZoneType() {
    }

    public ImportHYKFlowZoneType(String name) {
        this.name = name;
    }

    public HYKFlowZoneType getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from HYKFlowZoneType where name=:name");
            query.setParameter("name", name);
            List<HYKFlowZoneType> flowZoneTypes = query.list();
            if (flowZoneTypes.isEmpty()) {
                peer = new HYKFlowZoneType(name);
                session.save(peer);
            }
            else {
                peer = flowZoneTypes.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
