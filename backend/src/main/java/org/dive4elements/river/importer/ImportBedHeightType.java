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

import org.dive4elements.river.model.BedHeightType;


public class ImportBedHeightType {

    private static final Logger log =
        LogManager.getLogger(ImportBedHeightType.class);

    protected String name;

    protected BedHeightType peer;

    public ImportBedHeightType(BedHeightType peer)  {
        this.peer = peer;
        name = peer.getName();
    }


    public ImportBedHeightType(String name) {
        this.name        = name;
    }


    public void storeDependencies() {
        getPeer();
    }


    public BedHeightType getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery("from BedHeightType "
                + "where name=:name and description=:description");

            query.setParameter("name", name);

            List<BedHeightType> types = query.list();

            if (types.isEmpty()) {
                peer = new BedHeightType(name);
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
