/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Query;
import org.hibernate.Session;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.SedimentDensity;


public class ImportSedimentDensity {

    private static Logger log = LogManager.getLogger(ImportSedimentDensity.class);

    protected SedimentDensity peer;

    protected ImportDepth depth;

    protected String description;

    protected List<ImportSedimentDensityValue> values;

    public ImportSedimentDensity(String description) {
        this.description = description;
        this.values = new ArrayList<ImportSedimentDensityValue>();
    }

    public String getDescription() {
        return description;
    }

    public void setDepth(ImportDepth depth) {
        this.depth = depth;
    }

    public void addValue(ImportSedimentDensityValue value) {
        values.add(value);
    }

    public void storeDependencies(River river) {
        log.info("store dependencies");

        if (depth != null) {
            depth.storeDependencies();
        }

        SedimentDensity peer = getPeer(river);

        if (peer != null) {
            log.info("store sediment density values.");
            for (ImportSedimentDensityValue value : values) {
                value.storeDependencies(peer);
            }
        }
    }

    public SedimentDensity getPeer(River river) {
        log.info("get peer");

        if (depth == null) {
            log.warn("cannot store sediment density '" + description
                + "': no depth");
            return null;
        }

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery("from SedimentDensity where "
                + "   river=:river and " + "   depth=:depth");

            query.setParameter("river", river);
            query.setParameter("depth", depth.getPeer());

            List<SedimentDensity> density = query.list();

            if (density.isEmpty()) {
                log.debug("Create new SedimentDensity DB instance.");

                peer = new SedimentDensity(river, depth.getPeer(),
                    description);

                session.save(peer);
            }
            else {
                peer = density.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
