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
import org.dive4elements.river.model.Porosity;


public class ImportPorosity {

    private static Logger log = LogManager.getLogger(ImportPorosity.class);

    protected Porosity peer;

    protected ImportDepth depth;

    protected String description;

    protected ImportTimeInterval timeInterval;

    protected List<ImportPorosityValue> values;

    public ImportPorosity(String description) {
        this.description = description;
        this.values = new ArrayList<ImportPorosityValue>();
    }

    public String getDescription() {
        return description;
    }

    public void setDepth(ImportDepth depth) {
        this.depth = depth;
    }

    public void setTimeInterval(ImportTimeInterval importTimeInterval) {
        this.timeInterval = importTimeInterval;
    }

    public void addValue(ImportPorosityValue value) {
        values.add(value);
    }

    public void storeDependencies(River river) {
        log.info("store dependencies");

        if (depth != null) {
            depth.storeDependencies();
        }

        Porosity peer = getPeer(river);

        if (peer != null) {
            log.info("store porosity values.");
            for (ImportPorosityValue value : values) {
                value.storeDependencies(peer);
            }
        }
    }

    public Porosity getPeer(River river) {
        log.info("get peer");

        if (depth == null) {
            log.warn("cannot store porosity '" + description
                + "': no depth");
            return null;
        }

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery("from Porosity where "
                + "   river=:river and "
                + "   depth=:depth and "
                + "   description=:description");

            query.setParameter("river", river);
            query.setParameter("depth", depth.getPeer());
            query.setParameter("description", description);

            List<Porosity> porosity = query.list();

            if (porosity.isEmpty()) {
                log.debug("Create new Porosity DB instance.");

                peer = new Porosity(river, depth.getPeer(),
                    description, timeInterval.getPeer());

                session.save(peer);
            }
            else {
                peer = porosity.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
