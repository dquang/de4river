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
 * Factory to access ready-made WQKms for other (than computed) 'kinds' of
 * WST-data.
 */
public class WQKmsFactory
{
    private static Logger log = LogManager.getLogger(WQKmsFactory.class);

    /** Query to get km and wqs for wst_id and column_pos. */
    public static final String SQL_SELECT_WQS =
        "SELECT position, w, q FROM wst_value_table " +
        "WHERE wst_id = :wst_id AND column_pos = :column_pos";

    /** Get wst_id and position from wst_columns. */
    public static final String SQL_SELECT_COLUMN =
        "SELECT wst_id, position FROM wst_columns WHERE id = :column_id";

    /** Query to get name for wst_id and column_pos. */
    public static final String SQL_SELECT_NAME =
        "SELECT name " +
        "FROM wst_columns "+
        "WHERE id = :column_id";


    /** Hidden constructor, use static methods instead. */
    private WQKmsFactory() {
    }


    /**
     * Get WKms for given column (pos) and wst_id, caring about the cache.
     */
    public static WQKms getWQKms(int columnPos, int wst_id) {
        log.debug("WQKmsFactory.getWQKms");
        Cache cache = CacheFactory.getCache(StaticWQKmsCacheKey.CACHE_NAME);

        StaticWQKmsCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticWQKmsCacheKey(wst_id, columnPos);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static wst values from cache");
                return (WQKms)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        WQKms values = getWQKmsUncached(columnPos, wst_id);

        if (values != null && cacheKey != null) {
            log.debug("Store static wst values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    /**
     * Get WKms for given column (id), caring about the cache.
     */
    public static WQKms getWQKmsCID(int columnID) {
        log.debug("WQKmsFactory.getWQKms");
        Cache cache = CacheFactory.getCache(StaticWQKmsCacheKey.CACHE_NAME);

        StaticWQKmsCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticWQKmsCacheKey(-columnID, -columnID);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static wst values from cache");
                return (WQKms)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        int[] cInfo = getColumn(columnID);
        if (cInfo == null) return null;
        WQKms values = getWQKmsUncached(cInfo[1], cInfo[0]);


        if (values != null && cacheKey != null) {
            log.debug("Store static wst values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }


    /**
     * Get WQKms from db.
     * @param column the position columns value
     * @param wst_id database id of the wst
     * @return respective WQKms.
     */
    public static WQKms getWQKmsUncached(int column, int wst_id) {

        if (log.isDebugEnabled()) {
            log.debug("WQKmsFactory.getWQKmsUncached, column "
                + column + ", wst_id " + wst_id);
        }

        WQKms wqkms = new WQKms(WKmsFactory.getWKmsName(column, wst_id));

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_WQS)
            .addScalar("position", StandardBasicTypes.DOUBLE)
            .addScalar("w",  StandardBasicTypes.DOUBLE)
            .addScalar("q",  StandardBasicTypes.DOUBLE);
        sqlQuery.setInteger("wst_id",     wst_id);
        sqlQuery.setInteger("column_pos", column);

        List<Object []> results = sqlQuery.list();

        for (int i = 0, N = results.size(); i < N; i++) {
            Object[] row = results.get(i);
            // add(w, q, km)
            if (row == null
                || row[0] == null
                || row[1] == null
                || row[2] == null
            ) {
                log.warn("A value in result for WQKms is null.");
                continue;
            }
            wqkms.add((Double) row[1], (Double) row[2], (Double) row[0]);
        }

        return wqkms;
    }


    /**
     * Get WQKms from db.
     * @param columnID the columns database id value
     * @param wst_id database id of the wst
     * @return respective WQKms.
     */
    public static int[] getColumn(int columnID) {

        if (log.isDebugEnabled()) {
            log.debug("WQKmsFactory.getColumn, columnID "
                + columnID);
        }

        Session session = SessionHolder.HOLDER.get();

        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_COLUMN)
            .addScalar("wst_id",  StandardBasicTypes.INTEGER)
            .addScalar("position", StandardBasicTypes.INTEGER);
        sqlQuery.setInteger("column_id", columnID);

        List<Object []> results = sqlQuery.list();

        for (int i = 0, N = results.size(); i < N; i++) {
            Object[] row = results.get(i);
            return new int[] {(Integer)row[0], (Integer)row[1]};
        }

        return null;
    }


    /** Get name for a WKms. */
    public static String getWQKmsName(int columnID) {
        log.debug("WQKmsFactory.getWQKmsName c/" + columnID);

        String name = null;
        Session session = SessionHolder.HOLDER.get();

        SQLQuery nameQuery = session.createSQLQuery(SQL_SELECT_NAME)
            .addScalar("name", StandardBasicTypes.STRING);
        nameQuery.setInteger("column_id", columnID);

        List<String> names = nameQuery.list();
        if (names.size() >= 1) {
            name = names.get(0);
        }

        return name;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
