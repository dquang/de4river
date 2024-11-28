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

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.FlowVelocityMeasurement;
import org.dive4elements.river.model.River;


public class ImportFlowVelocityMeasurement {

    private static final Logger log = LogManager
        .getLogger(ImportFlowVelocityMeasurement.class);

    private String description;

    private List<ImportFlowVelocityMeasurementValue> values;

    private FlowVelocityMeasurement peer;

    public ImportFlowVelocityMeasurement() {
        this(null);
    }

    public ImportFlowVelocityMeasurement(String description) {
        this.description = description;
        this.values = new ArrayList<ImportFlowVelocityMeasurementValue>();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addValue(ImportFlowVelocityMeasurementValue value) {
        this.values.add(value);
    }

    public void storeDependencies(River river) {
        log.debug("store dependencies");

        FlowVelocityMeasurement peer = getPeer(river);

        if (peer != null) {
            for (ImportFlowVelocityMeasurementValue value : values) {
                value.storeDependencies(peer);
            }
        }
    }

    public FlowVelocityMeasurement getPeer(River river) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session
                .createQuery("from FlowVelocityMeasurement where "
                    + "   river=:river and " + "   description=:description");

            query.setParameter("river", river);
            query.setParameter("description", description);

            List<FlowVelocityMeasurement> measurement = query.list();

            if (measurement.isEmpty()) {
                peer = new FlowVelocityMeasurement(river, description);

                session.save(peer);
            }
            else {
                peer = measurement.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
