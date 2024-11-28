/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.HYK;
import org.dive4elements.river.model.HYKFlowZone;
import org.dive4elements.river.model.HYKFormation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;


/**
 * Factory to access HYKs (hydrographic values).
 */
public class HYKFactory
{
    private static Logger log = LogManager.getLogger(HYKFactory.class);

    public static String HYK_CACHE_NAME = "hykache";


    /** Do not instantiate a HYKFactory. */
    private HYKFactory() {
    }


    /**
     * Get List of Zones for given river and km.
     */
    public static Object getHYKs(int hykid, double km) {
        log.debug("HYKFactory.getHYKs");

        Cache cache = CacheFactory.getCache(HYK_CACHE_NAME);

        String cacheKey;

        if (cache != null) {
            cacheKey = "" + hykid + "_" + km;
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got hyk from cache");
                return element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        List<Zone> zones = getZonesUncached(hykid, km);

        if (zones != null && cacheKey != null) {
            log.debug("Store hykzones in cache.");
            Element element = new Element(cacheKey, zones);
            cache.put(element);
        }

        return zones;
    }


    /** Return name for hyk with given id. */
    public static String getHykName(int hykid) {
        log.debug("HYKFactory.getHykName " + hykid);

        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "select description from HYK where id = :hykid ");
        query.setParameter("hykid", hykid);

        return (String) query.uniqueResult();
    }


    /**
     * Ask DB for hyk zones.
     * @param hykid ID of the 'main' HYK to query.
     * @param km for which to get the hyk-zones.
     * @return according zones.
     */
    public static List<Zone> getZonesUncached(int hykid, double km) {
        if (log.isDebugEnabled()) {
            log.debug("HYKFactory.getZoneUncached " + hykid + " km " + km);
        }

        Session session = SessionHolder.HOLDER.get();

        // Find out flow-direction of river.
        // OPTIMIZE: 1) query kmUp directly 2) merge queries.
        Query rQuery = session.createQuery("from HYK where id = :hykid");
        rQuery.setParameter("hykid", hykid);
        rQuery.setMaxResults(1);
        HYK hyk = (HYK) rQuery.uniqueResult();

        double flowDir = hyk.getRiver().getKmUp() ? 1 : -1;

        List<HYKFormation> forms = getHYKFormations(hykid, km, flowDir);
        List<Zone>         zones = new ArrayList<Zone>();

        // Take the first one.
        if (forms.size() >= 1) {
            HYKFormation form = forms.get(0);
            // Create respective zones.
            for (HYKFlowZone flowZone: form.getZones()) {
                Zone z = new Zone(flowZone.getA().doubleValue(),
                    flowZone.getB().doubleValue(),
                    flowZone.getType().getName());
                zones.add(z);
            }
        }

        return zones;
    }


    protected static List<HYKFormation> getHYKFormations(
        int        hykid,
        double     km,
        double     flowDir
    ) {
        Session session = SessionHolder.HOLDER.get();

        String SQL = "SELECT " +
            "   f.id          AS FID, " +
            "   f.distance_vl AS DIST, " +
            "   e.hyk_id      AS HID, " +
            "   e.km          AS KM " +
            " FROM hyk_formations f INNER JOIN hyk_entries e " +
            "   ON e.id = f.hyk_entry_id " +
            " WHERE e.hyk_id = :hykid " +
            "   AND :km between " +
            "     LEAST(e.km, e.km + :flowDir*(f.distance_vl/1000.0-0.001)) " +
            "   AND " +
            "     GREATEST(e.km, e.km + :flowDir*(f.distance_vl/1000.0-0.001))";

        SQLQuery sqlQuery = session.createSQLQuery(SQL)
            .addScalar("FID", StandardBasicTypes.INTEGER)
            .addScalar("DIST", StandardBasicTypes.DOUBLE)
            .addScalar("HID", StandardBasicTypes.INTEGER)
            .addScalar("KM", StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("hykid", hykid);
        sqlQuery.setDouble("flowDir", flowDir);
        sqlQuery.setDouble("km", km);

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("HYK SQL: " + sqlQuery.getQueryString());
        }

        List<Object[]> results = sqlQuery.list();

        if (debug) {
            log.debug("Found " + results.size() + " HYKFormation IDs in DB.");
        }

        if (results == null || results.isEmpty()) {
            if (debug) {
                log.debug("No HYK found for ID " + hykid + " at km " + km);
            }
            return new ArrayList<HYKFormation>();
        }

        Object[] resultSet      = results.get(0);
        Integer  hykFormationId = (Integer) resultSet[0];

        Query query = session.createQuery("from HYKFormation where id = :id");
        query.setParameter("id", hykFormationId);
        query.setMaxResults(1);

        return query.list();
    }


    /** Labeled section. */
    public static class Zone implements Serializable {
        /** Lower end of segment. */
        protected double  from;
        /** Upper end of segment. */
        protected double  to;
        /** The label. */
        protected String name;

        /** Constructor for labelled section. */
        public Zone (double from, double to, String name) {
            this.from = from;
            this.to   = to;
            this.name = name;
        }

        /** Get upper value. */
        public double getTo() {
            return to;
        }

        /** Get lower value. */
        public double getFrom() {
            return from;
        }

        /** Get name (type). */
        public String getName() {
            return name;
        }
    } // public static class Zone
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
