/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.LocationSystem;


public class ImportLocationSystem {

    private static final Logger log =
        LogManager.getLogger(ImportLocationSystem.class);


    protected String name;
    protected String description;

    protected LocationSystem peer;


    public ImportLocationSystem(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    public void storeDependencies() {
        log.info("store LocationSystem '" + name + "'");
        LocationSystem ls = getPeer();

        Session session = ImporterSession.getInstance().getDatabaseSession();
        session.flush();
    }

    public LocationSystem getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from LocationSystem where " +
                "name=:name and description=:description");
            query.setParameter("name", name);
            query.setParameter("description", description);

            List<LocationSystem> lss = query.list();
            if (lss.isEmpty()) {
                peer = new LocationSystem(name, description);
                session.save(peer);
            }
            else {
                peer = lss.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
