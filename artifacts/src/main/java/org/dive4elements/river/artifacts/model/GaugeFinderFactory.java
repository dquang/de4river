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

import org.dive4elements.river.model.River;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.hibernate.type.StandardBasicTypes;

/** Get GaugeFinders. */
public class GaugeFinderFactory
{
    private static Logger log = LogManager.getLogger(GaugeFinderFactory.class);

    public static final String CACHE_NAME = "gauge-finders";

    public static final String SQL_GAUGES =
        "SELECT" +
        "    g.id AS gauge_id," +
        "    g.name AS name," +
        "    r.a  AS a," +
        "    r.b  AS b " +
        "FROM gauges g" +
        "    JOIN ranges r ON g.range_id = r.id " +
        "WHERE" +
        "    g.river_id = :river_id " +
        "ORDER BY r.a";

    private static GaugeFinderFactory INSTANCE;

    protected GaugeFinderFactory() {
    }

    public static synchronized GaugeFinderFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GaugeFinderFactory();
        }

        return INSTANCE;
    }

    public GaugeFinder getGaugeFinder(String riverName) {
        River river = RiverFactory.getRiver(riverName);
        return river != null
            ? getGaugeFinder(river.getId(), river.getKmUp())
            : null;
    }

    public synchronized GaugeFinder getGaugeFinder(
        int     riverId,
        boolean isKmUp
    ) {
        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            return getUncached(riverId, isKmUp);
        }

        String cacheKey = riverId + "-" + isKmUp;
        Element element = cache.get(cacheKey);

        if (element != null) {
            return (GaugeFinder)element.getValue();
        }

        GaugeFinder finder = getUncached(riverId, isKmUp);

        if (finder != null) {
            cache.put(new Element(cacheKey, finder));
        }

        return finder;
    }

    protected GaugeFinder loadGauges(
        Session session,
        int     riverId,
        boolean isKmUp
    ) {
        SQLQuery query = session.createSQLQuery(SQL_GAUGES)
            .addScalar("gauge_id", StandardBasicTypes.INTEGER)
            .addScalar("name",     StandardBasicTypes.STRING)
            .addScalar("a",        StandardBasicTypes.DOUBLE)
            .addScalar("b",        StandardBasicTypes.DOUBLE);

        query.setInteger("river_id", riverId);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.warn("River " + riverId + " has no gauges.");
            return null;
        }

        List<GaugeRange> gauges = new ArrayList<GaugeRange>();

        for (Object [] row: list) {
            int    gaugeId = (Integer)row[0];
            String name    = (String) row[1];
            double start   = (Double) row[2];
            double end     = (Double) row[3];
            GaugeRange gauge = new GaugeRange(start, end, name, gaugeId);
            gauges.add(gauge);
        }

        return new GaugeFinder(gauges, isKmUp);
    }

    protected GaugeFinder getUncached(int riverId, boolean isKmUp) {
        Session session = SessionHolder.HOLDER.get();

        GaugeFinder finder = loadGauges(session, riverId, isKmUp);

        if (finder == null
        || !finder.loadDischargeSectors(session, riverId)) {
            return null;
        }

        return finder;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
