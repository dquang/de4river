/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.model.FlowVelocityMeasurementValue;
import org.dive4elements.river.backend.SessionHolder;


public class FlowVelocityMeasurementFactory
{
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(
        FlowVelocityMeasurementFactory.class);

    /** Query to get description and start year, given name and a km range. */
    public static final String SQL_SELECT_ONE =
        "SELECT station, datetime, w, q, v, description " +
        "   FROM flow_velocity_measure_values" +
        "   WHERE id = :id";


    private FlowVelocityMeasurementFactory() {
    }


    public static FlowVelocityMeasurementValue.FastFlowVelocityMeasurementValue
        getFlowVelocityMeasurement(int id)
    {
        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = null;
        sqlQuery = session.createSQLQuery(SQL_SELECT_ONE)
            .addScalar("station", StandardBasicTypes.DOUBLE)
            .addScalar("datetime", StandardBasicTypes.DATE)
            .addScalar("w", StandardBasicTypes.DOUBLE)
            .addScalar("q", StandardBasicTypes.DOUBLE)
            .addScalar("v", StandardBasicTypes.DOUBLE)
            .addScalar("description", StandardBasicTypes.STRING);
        sqlQuery.setParameter("id", id);

        List<Object []> results = sqlQuery.list();
        if (results.size() > 0) {
            Object[] row = results.get(0);
            if (row == null || row.length < 6) {
                return null;
            }
            return FlowVelocityMeasurementValue.getUnmapped(
                (Double) row[0],
                (Double) row[2],
                (Double) row[3],
                (Double) row[4],
                (Date) row[1],
                (String) row[5]);
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
