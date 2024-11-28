/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.backend.SessionHolder;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.hibernate.Session;
import org.hibernate.SQLQuery;

import org.hibernate.type.StandardBasicTypes;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FixingsColumnFactory
{
    private static Logger log = LogManager.getLogger(FixingsColumnFactory.class);

    public static final String CACHE_NAME = "fixings-columns";

    public static final String SQL_COLUMN_WS =
        "SELECT wcv.position AS km, wcv.w AS w " +
        "FROM wst_column_values wcv " +
        "WHERE wst_column_id = :column_id " +
        "ORDER by wcv.position";

    public static final String SQL_COLUMN_QS =
        "SELECT wqr.q AS q, r.a AS a, r.b AS b " +
        "FROM wst_column_q_ranges wcqr " +
        "JOIN wst_q_ranges wqr ON wcqr.wst_q_range_id = wqr.id " +
        "JOIN ranges r         ON wqr.range_id        = r.id " +
        "WHERE wcqr.wst_column_id = :column_id ORDER by r.a";

    public static final FixingsColumnFactory INSTANCE =
        new FixingsColumnFactory();

    private FixingsColumnFactory() {
    }

    public static FixingsColumnFactory getInstance() {
        return INSTANCE;
    }

    public FixingsColumn getColumnData(Fixing.Column column) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("FixingsColumnFactory.getColumnData");
        }

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            if (debug) {
                log.debug("Cache unconfigured.");
            }
            return getUncached(column);
        }

        Integer cacheKey = Integer.valueOf(column.getId());
        Element element  = cache.get(cacheKey);

        if (element != null) {
            if (debug) {
                log.debug("Column " + cacheKey + " found in cache.");
            }
            return (FixingsColumn)element.getValue();
        }
        else {
            FixingsColumn result = getUncached(column);
            if (result != null) {
                if (debug) {
                    log.debug("Store column " + cacheKey + " into cache.");
                }
                cache.put(new Element(cacheKey, result));
            }
            return result;
        }
    }

    protected FixingsColumn getUncached(Fixing.Column column) {
        Session session = SessionHolder.HOLDER.get();

        SQLQuery sqlQuery = session.createSQLQuery(SQL_COLUMN_WS)
            .addScalar("km", StandardBasicTypes.DOUBLE)
            .addScalar("w",  StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("column_id", column.getId());

        List<Object []> results = sqlQuery.list();

        if (results.isEmpty()) {
            return null;
        }

        double [] kms = new double[results.size()];
        double [] ws  = new double[kms.length];

        for (int i = 0; i < kms.length; ++i) {
            Object [] row = results.get(i);
            kms[i] = ((Double)row[0]).doubleValue();
            ws [i] = ((Double)row[1]).doubleValue();
        }

        sqlQuery = session.createSQLQuery(SQL_COLUMN_QS)
            .addScalar("q", StandardBasicTypes.DOUBLE)
            .addScalar("a", StandardBasicTypes.DOUBLE)
            .addScalar("b", StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("column_id", column.getId());

        results = sqlQuery.list();

        if (results.isEmpty()) {
            return null;
        }

        QRangeTree qs = new QRangeTree(
            results, QRangeTree.WITHOUT_COLUMN, 0, results.size());

        return new FixingsColumn(kms, ws, qs);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
