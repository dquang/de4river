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

import org.dive4elements.river.model.ElevationModel;


public class ImportElevationModel {

    private static final Logger log =
        LogManager.getLogger(ImportElevationModel.class);

    protected String name;

    protected ImportUnit unit;

    protected ElevationModel peer;


    public ImportElevationModel(String name, ImportUnit unit) {
        this.name = name;
        this.unit = unit;
    }


    public void storeDependencies() {
        ElevationModel model = getPeer();
    }

    public ElevationModel getPeer() {
        if (unit == null) {
            log.warn("No elevation model specified.");
            return null;
        }

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from ElevationModel where " +
                "name=:name and unit=:unit");
            query.setParameter("name", name);
            query.setParameter("unit", unit.getPeer());
            List<ElevationModel> models = query.list();

            if (models.isEmpty()) {
                log.info("Create new ElevationModel DB instance.");

                peer = new ElevationModel(name, unit.getPeer());
                session.save(peer);
            }
            else {
                peer = models.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
