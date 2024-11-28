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

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MeasurementStation;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.TimeInterval;


public class ImportMeasurementStation {

    private static final Logger log = LogManager
        .getLogger(ImportMeasurementStation.class);

    private MeasurementStation peer;

    public String name;
    public ImportRange range;
    public String measurementType;
    public String riverside;
    public String gauge;
    public ImportTimeInterval observationTimerange;
    public String operator;
    public String comment;

    public ImportMeasurementStation() {
    }

    public ImportMeasurementStation(MeasurementStation peer) {
        this.peer = peer;
    }

    private Gauge getGaugeFromDB() {
        Session session = ImporterSession.getInstance().getDatabaseSession();

        org.hibernate.Query query = session
            .createQuery("FROM Gauge WHERE name=:name");

        query.setParameter("name", gauge);
        List<Gauge> gauges = query.list();

        return gauges.isEmpty() ? null : gauges.get(0);
    }

    public boolean storeDependencies(River river) {
        return getPeer(river) != null;
    }

    public MeasurementStation getPeer(River river) {
        if (peer == null) {
            Gauge gauge = null;
            try {
                gauge = getGaugeFromDB();
                if (gauge == null) {
                    log.warn("No gauge found for measurement station '" + name
                        + "'");
                }
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }

            Range range = null;

            if (this.range != null) {
                range = this.range.getPeer(river);
            }

            if (range == null) {
                log.warn("No range found for measurement station '"
                    + name + "'");
            }

            TimeInterval observationTimerange = this.observationTimerange
                .getPeer();
            if (observationTimerange == null) {
                log.warn("No time range found for measurement station '"
                    + name + "'");
            }

            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            org.hibernate.Query query = session
                .createQuery(
                    "FROM MeasurementStation " +
                    "WHERE range=:range " +
                    "   AND measurement_type=:measurement_type ");

            query.setParameter("range", range);
            query.setParameter("measurement_type", measurementType);

            List<MeasurementStation> stations = query.list();

            if (stations.isEmpty()) {
                log.info("create new measurement station '" + name + "'");

                peer = new MeasurementStation(name, measurementType,
                    riverside, range, gauge, this.gauge,
                    observationTimerange, operator, comment);

                session.save(peer);
            }
        }

        return peer;
    }
}
