/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.model.minfo;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.backend.SessionHolder;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

public class SedimentLoadDataFactory
{
    private static Logger log = LogManager.getLogger(
        SedimentLoadDataFactory.class);

    public static final String CACHE_NAME = "sediment-load-data";

    public static final String SUSPENDED_STRING = "Schwebstoff";

    public static final String SQL_LOAD_RIVER_SEDIMENT_LOADS =
        "WITH load_at_river AS (" +
          "SELECT DISTINCT sl.id, " +
            "sl.kind, " +
            "sl.description, " +
            "sl.time_interval_id, " +
            "sl.grain_fraction_id, " +
            "sl.sq_time_interval_id " +
          "FROM sediment_load sl " +
            "JOIN sediment_load_values slv ON sl.id = slv.sediment_load_id " +
            "JOIN measurement_station ms " +
              "ON ms.id = slv.measurement_station_id " +
            "JOIN ranges rs ON rs.id = ms.range_id " +
            "JOIN rivers r ON r.id = rs.river_id " +
          "WHERE r.name = :river) " +
        "SELECT " +
          "sl.id AS sl_id, " +
          "sl.kind AS sl_kind, " +
          "sl.description AS sl_description, " +
          "ti.start_time AS ti_start_time, " +
          "ti.stop_time AS ti_stop_time, " +
          "sqti.start_time AS sq_start_time, " +
          "sqti.stop_time AS sq_stop_time, " +
          "sqti.id AS sq_ti_id, " +
          "slv.value AS slv_value, " +
          "gf.name AS gf_name, " +
          "ms.id AS ms_id, " +
          "CASE WHEN r.km_up = 1 THEN rs.b ELSE rs.a END AS ms_station, " +
          "ms.measurement_type AS ms_type " +
        "FROM load_at_river sl " +
          "CROSS JOIN measurement_station ms " +
          "JOIN ranges rs ON ms.range_id = rs.id " +
          "JOIN rivers r ON rs.river_id = r.id " +
          "JOIN time_intervals ti ON sl.time_interval_id = ti.id " +
          "LEFT JOIN time_intervals sqti " +
            "ON sl.sq_time_interval_id = sqti.id " +
          "JOIN grain_fraction gf ON sl.grain_fraction_id = gf.id " +
          "LEFT JOIN sediment_load_values slv " +
             "ON ms.id=slv.measurement_station_id " +
               "AND sl.id=slv.sediment_load_id " +
        "WHERE (" +
          "(ms.measurement_type='Geschiebe' AND gf.name IN " +
             "('coarse', 'fine_middle', 'sand', 'susp_sand', " +
              "'susp_sand_bed', 'bed_load', 'bed_load_susp_sand', " +
              "'suspended_load', 'total')) " +
          "OR " +
          "(ms.measurement_type='Schwebstoff' AND gf.name IN " +
             "('suspended_sediment', 'suspended_load', 'total'))) " +
        "AND rs.b IS NOT NULL " +
        "AND r.name = :river " +
        "ORDER BY sl.id";

    public static final SedimentLoadDataFactory INSTANCE =
        new SedimentLoadDataFactory();

    private SedimentLoadDataFactory() {
    }

    public synchronized SedimentLoadData getSedimentLoadData(String river) {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "Looking for sediment load data for river '" + river + "'");
        }

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            if (debug) {
                log.debug("Cache not configured.");
            }
            return getUncached(river);
        }

        String key = "sediment-load-" + river;

        Element element = cache.get(key);

        if (element != null) {
            if (debug) {
                log.debug("Sediment load data found in cache");
            }
            return (SedimentLoadData)element.getValue();
        }

        SedimentLoadData sedimentLoad = getUncached(river);

        if (sedimentLoad != null) {
            if (debug) {
                log.debug("Store sediment load data in cache.");
            }
            cache.put(new Element(key, sedimentLoad));
        }

        return sedimentLoad;
    }

    public SedimentLoadData getUncached(String river) {

        Session session = SessionHolder.HOLDER.get();

        SQLQuery sqlQuery = session.createSQLQuery(
            SQL_LOAD_RIVER_SEDIMENT_LOADS)
            .addScalar("sl_id",          StandardBasicTypes.INTEGER)
            .addScalar("sl_kind",        StandardBasicTypes.INTEGER)
            .addScalar("sl_description", StandardBasicTypes.STRING)
            .addScalar("ti_start_time",  StandardBasicTypes.TIMESTAMP)
            .addScalar("ti_stop_time",   StandardBasicTypes.TIMESTAMP)
            .addScalar("sq_start_time",  StandardBasicTypes.TIMESTAMP)
            .addScalar("sq_stop_time",   StandardBasicTypes.TIMESTAMP)
            .addScalar("sq_ti_id",       StandardBasicTypes.INTEGER)
            .addScalar("slv_value",      StandardBasicTypes.DOUBLE)
            .addScalar("gf_name",        StandardBasicTypes.STRING)
            .addScalar("ms_id",          StandardBasicTypes.INTEGER)
            .addScalar("ms_station",     StandardBasicTypes.DOUBLE)
            .addScalar("ms_type",        StandardBasicTypes.STRING);

        sqlQuery.setString("river", river);

        SedimentLoadData.Load load = null;
        int grainFractionIndex = SedimentLoadData.GF_UNKNOWN;

        HashMap<Integer, SedimentLoadData.Station> id2station
            = new HashMap<Integer, SedimentLoadData.Station>();

        List<Object[]> list = sqlQuery.list();

        for (Object [] row: list) {

            // Load
            Integer   sl_id            = (Integer)row[0];
            Integer   sl_kind          = (Integer)row[1];
            String    sl_description   = (String)row[2];
            Timestamp ti_start_time    = (Timestamp)row[3];
            Timestamp ti_stop_time     = (Timestamp)row[4];
            Timestamp sq_start_time    = (Timestamp)row[5];
            Timestamp sq_stop_time     = (Timestamp)row[6];
            Integer   sq_id            = (Integer)row[7];

            // Value
            Double    slv_value        = (Double)row[8];
            String    gf_name          = (String)row[9];

            // Station
            Integer   ms_id            = (Integer)row[10];
            Double    ms_station       = (Double)row[11];
            String    ms_type          = (String)row[12];

            if (load == null || load.getId() != sl_id) {
                load = new SedimentLoadData.Load(
                    sl_id, sl_kind, sl_description,
                    ti_start_time, ti_stop_time, sq_id,
                    sq_start_time, sq_stop_time);

                // Grain fractions only change when a new sediment load starts.
                grainFractionIndex =
                    SedimentLoadData.grainFractionIndex(gf_name);

                if (grainFractionIndex == SedimentLoadData.GF_UNKNOWN) {
                    log.error("Unknown grain fraction type: " + gf_name);
                    break;
                }
            }

            SedimentLoadData.Station station = id2station.get(ms_id);
            if (station == null) {
                int type = ms_type.equalsIgnoreCase(SUSPENDED_STRING)
                    ? SedimentLoadData.Station.SUSPENDED
                    : SedimentLoadData.Station.BED_LOAD;

                station = new SedimentLoadData.Station(type, ms_station);
                id2station.put(ms_id, station);
            }

            station.addValue(
                grainFractionIndex,
                new SedimentLoadData.Value(load, slv_value == null
                    ? Double.NaN
                    : slv_value));
        }

        SedimentLoadData sld = new SedimentLoadData(id2station.values(),
            RiverFactory.getRiver(river).getKmUp());

        return sld;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
