/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;

import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.backend.SessionHolder;


/**
 * Factory to access ready-made WKms for other (than computed) 'kinds' of
 * WST-data.
 */
public class WKmsFactory
{
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(WKmsFactory.class);

    /** Query to get km and ws for wst_id and column_pos. */
    public static final String SQL_SELECT_WS =
        "SELECT km, w FROM wst_w_values " +
        "WHERE wst_id = :wst_id AND column_pos = :column_pos";

    public static final String SQL_SELECT_WS_FOR_RANGE =
        "SELECT km, w FROM wst_w_values " +
        "WHERE wst_id = :wst_id AND column_pos = :column_pos " +
        "AND km BETWEEN :kmfrom AND :kmto";

    /** Query to get name for wst_id and column_pos. */
    public static final String SQL_SELECT_NAME =
        "SELECT name " +
        "FROM wst_columns "+
        "WHERE wst_id = :wst_id AND position = :column_pos";

    /** Query to get name and kind for wst_id and column_pos. */
    public static final String SQL_SELECT_Q_NAME =
        "SELECT wqr.q, wc.name " +
        "FROM wst_column_q_ranges wcqr " +
        "JOIN wst_q_ranges wqr ON wcqr.wst_q_range_id = wqr.id " +
        "JOIN wst_columns wc ON wcqr.wst_column_id = wc.id " +
        "JOIN wsts ON wc.wst_id = wsts.id " +
        "WHERE wc.wst_id = :wst_id AND wc.position = :column_pos";
/*
 Test statement:
    SELECT wqr.q, wc.name
    FROM wst_column_q_ranges wcqr
    JOIN wst_q_ranges wqr ON wcqr.wst_q_range_id = wqr.id
    JOIN wst_columns wc ON wcqr.wst_column_id = wc.id
    JOIN wsts ON wc.wst_id = wsts.id
    WHERE wc.wst_id = 1817 AND wc.position = 29;
*/



    /** Query to get name (description) for wst_id. */
    public static final String SQL_SELECT_WST_NAME =
        "SELECT description from wsts "+
        "WHERE id = :wst_id";

    /** Query to get name (description) and kind for wst_id. */
    public static final String SQL_SELECT_WST_Q_NAME =
        "SELECT wqr.q, wc.name " +
        "FROM wst_column_q_ranges wcqr " +
        "JOIN wst_q_ranges wqr ON wcqr.wst_q_range_id = wqr.id " +
        "JOIN wst_columns wc ON wcqr.wst_column_id = wc.id " +
        "JOIN wsts ON wc.wst_id = wsts.id " +
        "WHERE wc.wst_id = :wst_id";

    private WKmsFactory() {
    }

    public static WKms getWKms(
        int column,
        int wst_id,
        double from,
        double to
    ) {
        log.debug("WKmsFactory.getWKms");
        Cache cache = CacheFactory.getCache(StaticWQKmsCacheKey.CACHE_NAME);

        String cacheKey = Integer.toString(column) + ":"
            + Integer.toString(wst_id);

        if (cache != null) {
            if (!Double.isNaN(from) && ! Double.isNaN(to)) {
                cacheKey += ":" + Double.toString(from) + ":"
                    + Double.toString(to);
            }
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static wst values from cache");
                return (WKms)element.getValue();
            }
        }

        WKms values = getWKmsUncached(column, wst_id, from, to);

        if (values != null && cache != null) {
            log.debug("Store static wst values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    /**
     * Get WKms for given column and wst_id, caring about the cache.
     */
    public static WKms getWKms(int column, int wst_id) {
        return getWKms(column, wst_id, Double.NaN, Double.NaN);
    }

    /** Get name for a WKms wrapped in W, if suitable. */
    public static String getWKmsNameWWrapped(int wst_id) {
        return getWKmsNameWWrapped(-1, wst_id);
    }


    /** Get name for a WKms wrapped in W, if suitable. */
    public static String getWKmsNameWWrapped(int column, int wst_id) {
        log.debug("WKmsFactory.getWKmsNameWWrapped c/"
            + column + ", wst_id/" + wst_id);

        String name = null;
        Session session = SessionHolder.HOLDER.get();

        SQLQuery nameQuery;
        if (column != -1) {
            nameQuery = session.createSQLQuery(SQL_SELECT_Q_NAME);
            nameQuery.setInteger("column_pos", column);
        } else {
            nameQuery = session.createSQLQuery(SQL_SELECT_WST_Q_NAME);
        }

        nameQuery.addScalar("q", StandardBasicTypes.DOUBLE)
                 .addScalar("name", StandardBasicTypes.STRING);
        nameQuery.setInteger("wst_id",     wst_id);

        List<Object[]> names = nameQuery.list();

        if (names.size() >= 1) {
            Object[] row = names.get(0);
            Double q = (Double) row[0];
            name = (String) row[1];
            if (q >= 0) {
                name = "W(" + name + ")";
            }
        } else {
            // This should handle the case of Q = NULL
            if (column != -1) {
                name = getWKmsName(column, wst_id);
            } else {
                name = getWKmsName(wst_id);
            }
        }

        log.debug("WKmsFactory.getWKmsNameWWrapped c/" + column +
                ", wst_id/" + wst_id + " = name/ " + name);

        return name;
    }


    /** Get name for a WKms. */
    public static String getWKmsName(int wst_id) {
        log.debug("WKmsFactory.getWKmsName wst_id/" + wst_id);

        String name = null;
        Session session = SessionHolder.HOLDER.get();

        SQLQuery nameQuery = session.createSQLQuery(SQL_SELECT_WST_NAME)
            .addScalar("description", StandardBasicTypes.STRING);
        nameQuery.setInteger("wst_id",     wst_id);

        List<String> names = nameQuery.list();
        if (names.size() >= 1) {
            name = names.get(0);
        }

        return name;
    }

    /** Get name for a WKms. */
    public static String getWKmsName(int column, int wst_id) {
        log.debug("WKmsFactory.getWKmsName c/"
            + column + ", wst_id/" + wst_id);

        String name = null;
        Session session = SessionHolder.HOLDER.get();

        SQLQuery nameQuery = session.createSQLQuery(SQL_SELECT_NAME)
            .addScalar("name", StandardBasicTypes.STRING);
        nameQuery.setInteger("wst_id",     wst_id);
        nameQuery.setInteger("column_pos", column);

        List<String> names = nameQuery.list();
        if (names.size() >= 1) {
            name = names.get(0);
        }

        return name;
    }


    /**
     * Get WKms from db.
     * @param column the position columns value
     * @param wst_id database id of the wst
     * @return according WKms.
     */
    public static WKms getWKmsUncached(
        int column,
        int wst_id,
        double from,
        double to
    ) {
        if (log.isDebugEnabled()) {
            log.debug("WKmsFactory.getWKmsUncached c/"
                + column + ", wst_id/" + wst_id);
        }

        WKmsImpl wkms = new WKmsImpl(getWKmsName(column, wst_id));

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery;
        if (Double.isNaN(from) || Double.isNaN(to)) {
            sqlQuery = session.createSQLQuery(SQL_SELECT_WS);
        } else {
            sqlQuery = session.createSQLQuery(SQL_SELECT_WS_FOR_RANGE);
            sqlQuery.setDouble("kmfrom", from);
            sqlQuery.setDouble("kmto", to);
        }

        sqlQuery.addScalar("km", StandardBasicTypes.DOUBLE)
                .addScalar("w",  StandardBasicTypes.DOUBLE);
        sqlQuery.setInteger("wst_id",     wst_id);
        sqlQuery.setInteger("column_pos", column);

        List<Object []> results = sqlQuery.list();

        for (int i = 0, N = results.size(); i < N; i++) {
            Object[] row = results.get(i);
            wkms.add((Double) row[0], (Double) row[1]);
        }

        return wkms;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
