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

import org.dive4elements.river.model.Unit;


public class ImportUnit
{
    private static final Logger log = LogManager.getLogger(ImportUnit.class);

    protected String name;

    protected Unit peer;


    public ImportUnit(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public Unit getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery("from Unit where name=:name");
            query.setParameter("name", name);

            List<Unit> units = query.list();
            if (units.isEmpty()) {
                log.info("Store new unit '" + name + "'");

                peer = new Unit(name);
                session.save(peer);
            }
            else {
                peer = units.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
