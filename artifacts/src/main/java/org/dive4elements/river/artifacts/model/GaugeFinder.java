/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.hibernate.type.StandardBasicTypes;

/** Find Gauges and respective Q main values. */
public class GaugeFinder
implements   Serializable
{
    private static Logger log = LogManager.getLogger(GaugeFinder.class);

    public static final String SQL_DISCHARGE_SECTORS =
        "SELECT" +
        "    g.id                            AS gauge_id," +
        "    nmv.name                        AS name," +
        "    CAST(mv.value AS NUMERIC(38,2)) AS value " +
        "FROM gauges g" +
        "    JOIN main_values       mv  ON g.id = mv.gauge_id" +
        "    JOIN named_main_values nmv ON nmv.id = mv.named_value_id" +
        "    JOIN main_value_types  mvt ON nmv.type_id = mvt.id " +
        "WHERE" +
        "    mvt.name = 'Q' AND (" +
        "        nmv.name = 'MNQ'      OR" +
        "        nmv.name LIKE 'MNQ(%' OR" +
        "        nmv.name = 'MQ'       OR" +
        "        nmv.name LIKE 'MQ(%'  OR" +
        "        nmv.name = 'MHQ'      OR" +
        "        nmv.name LIKE 'MHQ(%' OR" +
        "        nmv.name = 'HQ5'      OR" +
        "        nmv.name LIKE 'HQ5(%') AND" +
        "    g.river_id = :river_id " +
        "ORDER BY" +
        "    g.id";

    protected List<GaugeRange> gauges;
    protected boolean          isKmUp;

    public GaugeFinder(List<GaugeRange> gauges) {
        this(gauges, true);
    }

    public GaugeFinder(
        List<GaugeRange> gauges,
        boolean          isKmUp
    ) {
        this.gauges = gauges;
        this.isKmUp = isKmUp;
    }

    public boolean getIsKmUp() {
        return isKmUp;
    }

    public void setIsKmUp(boolean isKmUp) {
        this.isKmUp = isKmUp;
    }


    /** Find GaugeRange at kilometer. */
    public GaugeRange find(double km) {
        for (GaugeRange gauge: gauges) {
            if (gauge.inside(km)) {
                return gauge;
            }
        }
        return null;
    }

    public GaugeRange find(Range range) {
        return find(isKmUp ? range.start : range.end);
    }

    public GaugeRange find(int gaugeId) {
        for (GaugeRange gauge: gauges) {
            if (gauge.gaugeId == gaugeId) {
                return gauge;
            }
        }
        return null;
    }

    public List<GaugeRange> getGauges() {
        return gauges;
    }

    public boolean loadDischargeSectors(Session session, int riverId) {

        SQLQuery query = session.createSQLQuery(SQL_DISCHARGE_SECTORS)
            .addScalar("gauge_id", StandardBasicTypes.INTEGER)
            .addScalar("name",     StandardBasicTypes.STRING)
            .addScalar("value",    StandardBasicTypes.DOUBLE);

        query.setInteger("river_id", riverId);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.warn("River " + riverId + " has no discharge sectors.");
            return false;
        }

        GaugeRange gauge = null;

        for (Object [] row: list) {
            int    gaugeId = (Integer)row[0];
            String label   = (String) row[1];
            Double value   = (Double) row[2];

            if (gauge == null || gauge.gaugeId != gaugeId) {
                if ((gauge = find(gaugeId)) == null) {
                    log.warn("Cannot find gauge for id " + gaugeId + ".");
                    continue;
                }
            }

            gauge.addMainValue(label, value);
        }

        for (GaugeRange g: gauges) {
            g.buildClasses();
        }

        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
