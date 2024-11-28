/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.FlowVelocityMeasurement;
import org.dive4elements.river.model.FlowVelocityMeasurementValue;


public class ImportFlowVelocityMeasurementValue {

    private static final Logger log =
        LogManager.getLogger(ImportFlowVelocityMeasurementValue.class);


    private Date datetime;

    private String description;

    private BigDecimal station;
    private BigDecimal w;
    private BigDecimal q;
    private BigDecimal v;

    private FlowVelocityMeasurementValue peer;


    public ImportFlowVelocityMeasurementValue(
        Date       datetime,
        BigDecimal station,
        BigDecimal w,
        BigDecimal q,
        BigDecimal v,
        String     description
    ) {
        this.datetime    = datetime;
        this.station     = station;
        this.w           = w;
        this.q           = q;
        this.v           = v;
        this.description = description;
    }



    public void storeDependencies(FlowVelocityMeasurement measurement) {
        log.debug("store dependencies");

        getPeer(measurement);
    }


    public FlowVelocityMeasurementValue getPeer(FlowVelocityMeasurement m) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from FlowVelocityMeasurementValue where " +
                "   measurement=:measurement and " +
                "   station=:station and " +
                "   datetime=:datetime"
            );

            query.setParameter("measurement", m);
            query.setParameter("station", station);
            query.setParameter("datetime", datetime);

            List<FlowVelocityMeasurementValue> values = query.list();

            if (values.isEmpty()) {
                peer = new FlowVelocityMeasurementValue(
                    m,
                    datetime,
                    station,
                    w,
                    q,
                    v,
                    description);

                session.save(peer);
            }
            else {
                peer = values.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
