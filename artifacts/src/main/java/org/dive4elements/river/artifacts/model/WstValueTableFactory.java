/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.backend.SessionHolder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Wst;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

import org.hibernate.type.StandardBasicTypes;

/**
 * Creates WstValueTable s from database.
 * WstValueTable s are used to interpolate given w/q/km values.
 */
public class WstValueTableFactory
{
    private static Logger log = LogManager.getLogger(WstValueTableFactory.class);

    public static final int DEFAULT_KIND = 0;

    // TODO: put this into a property file

    public static final String HQL_WST =
        "from Wst where river=:river and kind=:kind";

    public static final String SQL_SELECT_NAMES_POS =
        "SELECT position, name FROM wst_columns " +
        "WHERE wst_id = :wst_id ORDER BY position";

    /** Select Qs for wst (view sorted by column). */
    public static final String SQL_SELECT_QS =
        "SELECT column_pos, q, a, b FROM wst_q_values " +
        "WHERE wst_id = :wst_id";

    // (sorted by km)
    public static final String SQL_SELECT_WS =
        "SELECT km, w, column_pos FROM wst_w_values " +
        "WHERE wst_id = :wst_id";

    /** Statement to query qranges of a single column. */
    public static final String SQL_SELECT_QS_AT_COL =
        "SELECT q, a, b FROM wst_q_values " +
        "WHERE wst_id = :wst_id AND column_pos = :column_pos";

    // (sorted by km)
    public static final String SQL_SELECT_WS_AT_COL =
        "SELECT km, w FROM wst_w_values " +
        "WHERE wst_id = :wst_id AND column_pos = :column_pos";


    private WstValueTableFactory() {
    }


    public static WstValueTable getTable(River river) {
        return getTable(river, DEFAULT_KIND);
    }


    /**
     * Get WstValueTable to interpolate values of a given Wst.
     */
    public static WstValueTable getTable(int wst_id) {

        Cache cache = CacheFactory.getCache(WstValueTableCacheKey.CACHE_NAME);

        WstValueTableCacheKey cacheKey;

        if (cache != null) {
            // "-1" is the symbolic river-id for "no river, but wst_id".
            cacheKey = new WstValueTableCacheKey(-1, wst_id);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got specific wst value table from cache");
                return (WstValueTable) element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        Session session = SessionHolder.HOLDER.get();

        // Fetch data for one column only.

        WstValueTable.Column [] columns = loadColumns(session, wst_id);
        loadQRanges(session, columns, wst_id);
        List<WstValueTable.Row> rows = loadRows(
            session, wst_id, columns.length);

        WstValueTable valueTable = new WstValueTable(columns, rows);

        if (valueTable != null && cacheKey != null) {
            log.debug("Store wst value table in cache");
            Element element = new Element(cacheKey, valueTable);
            cache.put(element);
        }

        return valueTable;
    }

    /**
     * Get Table for a specific column of a wst.
     */
    public static WstValueTable getWstColumnTable(int wst_id, int col_pos) {

        Cache cache = CacheFactory.getCache(WstValueTableCacheKey.CACHE_NAME);

        WstValueTableCacheKey cacheKey;

        if (cache != null) {
            // A negaitve/negative number is the symbolic 'river-id' for
            // "no river and kind but wst_id and colpos".
            cacheKey = new WstValueTableCacheKey(-wst_id, -col_pos);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got specific wst value table from cache");
                return (WstValueTable) element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        Session session = SessionHolder.HOLDER.get();

        // Fetch data for one column only.

        WstValueTable.Column [] columns = loadColumn(session, wst_id, col_pos);
        loadQRanges(session, columns, wst_id, col_pos);
        List<WstValueTable.Row> rows = loadRowsOneColumn(
            session, wst_id, col_pos);

        WstValueTable valueTable = new WstValueTable(columns, rows);

        if (valueTable != null && cacheKey != null) {
            log.debug("Store wst value table in cache (wst: "
                + wst_id + "/ col: " + col_pos + ")");
            Element element = new Element(cacheKey, valueTable);
            cache.put(element);
        }

        return valueTable;
    }


    /**
     * Get table for first wst of given kind at given river.
     */
    public static WstValueTable getTable(River river, int kind) {

        Cache cache = CacheFactory.getCache(WstValueTableCacheKey.CACHE_NAME);

        WstValueTableCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new WstValueTableCacheKey(river.getId(), kind);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("got wst value table from cache");
                return (WstValueTable)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        WstValueTable valueTable = getTableUncached(river, kind);

        if (valueTable != null && cacheKey != null) {
            log.debug("store wst value table in cache");
            Element element = new Element(cacheKey, valueTable);
            cache.put(element);
        }

        return valueTable;
    }

    public static WstValueTable getTableUncached(River river) {
        return getTableUncached(river, DEFAULT_KIND);
    }

    public static WstValueTable getTableUncached(River river, int kind) {

        Session session = SessionHolder.HOLDER.get();

        Wst wst = loadWst(session, river, kind);

        if (wst == null) {
            return null;
        }

        WstValueTable.Column [] columns = loadColumns(session, wst);

        loadQRanges(session, columns, wst);

        List<WstValueTable.Row> rows = loadRows(session, wst, columns.length);

        return new WstValueTable(columns, rows);
    }

    /**
     * @param kind Kind of wst.
     */
    protected static Wst loadWst(Session session, River river, int kind) {
        Query query = session.createQuery(HQL_WST);
        query.setParameter("river", river);
        query.setInteger("kind",    kind);

        List<Wst> wsts = query.list();

        // TODO Multiple wsts can match, why return just the first one?
        return wsts.isEmpty() ? null : wsts.get(0);
    }


    /**
     * Load rows with a single columns result.
     *
     * @param session    session to use for querying db.
     * @param wstId     id of wst (in db).
     * @param column_pos the column_pos (within the db) of the wst_value_table
     *                   of which the values shall be fetched.
     *
     * @return resultant rows.
     */
    protected static List<WstValueTable.Row> loadRowsOneColumn(
        Session session,
        int     wstId,
        int     column_pos
    ) {
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_WS_AT_COL)
            .addScalar("km", StandardBasicTypes.DOUBLE)
            .addScalar("w",  StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("wst_id", wstId);
        sqlQuery.setInteger("column_pos", column_pos);

        List<Object []> results = sqlQuery.list();

        double [] ws = null;

        List<WstValueTable.Row> rows =
            new ArrayList<WstValueTable.Row>(results.size());

        // Walk over rows.
        for (Object [] result: results) {
            ws = new double[1];
            WstValueTable.Row row =
                new WstValueTable.Row((Double) result[0], ws);
            rows.add(row);

            Double w = (Double) result[1];
            ws[0] = w != null ? w : Double.NaN;
        }

        return rows;
    }

    protected static List<WstValueTable.Row> loadRows(
        Session session,
        int     wst_id,
        int     numColumns
    ) {
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_WS)
            .addScalar("km",         StandardBasicTypes.DOUBLE)
            .addScalar("w",          StandardBasicTypes.DOUBLE)
            .addScalar("column_pos", StandardBasicTypes.INTEGER);

        sqlQuery.setInteger("wst_id", wst_id);

        List<Object []> results = sqlQuery.list();

        int lastColumn = Integer.MAX_VALUE;
        double [] ws = null;

        ArrayList<WstValueTable.Row> rows = new ArrayList<WstValueTable.Row>();

        for (Object [] result: results) {
            int column = (Integer)result[2];
            if (column < lastColumn) {
                ws = new double[numColumns];
                Arrays.fill(ws, Double.NaN);
                WstValueTable.Row row =
                    new WstValueTable.Row((Double)result[0], ws);
                rows.add(row);
            }
            Double w = (Double)result[1];
            ws[column] = w != null ? w : Double.NaN;
            lastColumn = column;
        }

        rows.trimToSize();
        return rows;
    }

    protected static List<WstValueTable.Row> loadRows(
        Session session,
        Wst     wst,
        int     numColumns
    ) {
        return loadRows(session, wst.getId(), numColumns);
    }


    protected static WstValueTable.Column [] loadColumn(
        Session session,
        int wst_id,
        int col_pos
    ) {
        return new WstValueTable.Column [] {
            new WstValueTable.Column(WKmsFactory.getWKmsName(col_pos, wst_id))};
    }


    /**
     * Get columns from wst-id.
     */
    protected static WstValueTable.Column [] loadColumns(
        Session session,
        int wst_id
    ) {
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_NAMES_POS)
            .addScalar("position",   StandardBasicTypes.INTEGER)
            .addScalar("name",       StandardBasicTypes.STRING);

        sqlQuery.setInteger("wst_id", wst_id);

        List<Object []> columnNames = sqlQuery.list();

        WstValueTable.Column [] columns =
            new WstValueTable.Column[columnNames.size()];

        for (int i = 0; i < columns.length; ++i) {
            columns[i] = new WstValueTable.Column(
                (String)columnNames.get(i)[1]);
        }
        return columns;
    }

    /**
     * Get columns from Wst.
     */
    protected static WstValueTable.Column [] loadColumns(
        Session session,
        Wst     wst
    ) {
        return loadColumns(session, wst.getId());
    }


    /**
     * Build a QRange-Tree.
     */
    protected static void loadQRanges(
        Session                 session,
        WstValueTable.Column [] columns,
        int                     wst_id,
        int                     column_pos
    ) {
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_QS_AT_COL)
            .addScalar("q", StandardBasicTypes.DOUBLE)
            .addScalar("a", StandardBasicTypes.DOUBLE)
            .addScalar("b", StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("wst_id",     wst_id);
        sqlQuery.setInteger("column_pos", column_pos);

        List<Object []> qRanges = sqlQuery.list();

        int qSize = qRanges.size();

        QRangeTree qRangeTree = new QRangeTree(
            qRanges, QRangeTree.WITHOUT_COLUMN, 0, qSize);
        columns[0].setQRangeTree(qRangeTree);
    }

    protected static void loadQRanges(
        Session                 session,
        WstValueTable.Column [] columns,
        int                     wst_id
    ) {
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_QS)
            .addScalar("column_pos", StandardBasicTypes.INTEGER)
            .addScalar("q",          StandardBasicTypes.DOUBLE)
            .addScalar("a",          StandardBasicTypes.DOUBLE)
            .addScalar("b",          StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("wst_id", wst_id);

        List<Object []> qRanges = sqlQuery.list();

        int     start      = -1;
        int     Q          = qRanges.size();
        Integer lastColumn = null;

        for (int i = 0; i < Q; ++i) {
            Object [] qRange = qRanges.get(i);
            Integer columnId = (Integer)qRange[0];
            if (lastColumn == null) {
                lastColumn = columnId;
                start = i;
            }
            else if (!lastColumn.equals(columnId)) {
                QRangeTree qRangeTree = new QRangeTree(qRanges, start, i);
                columns[lastColumn].setQRangeTree(qRangeTree);
                lastColumn = columnId;
                start = i;
            }
        }

        if (start != -1) {
            QRangeTree qRangeTree = new QRangeTree(qRanges, start, Q);
            columns[lastColumn].setQRangeTree(qRangeTree);
        }

        /* This is debug code to visualize the q ranges trees

        java.io.PrintWriter out = null;
        try {
            out = new java.io.PrintWriter(
                new java.io.FileWriter(
                    "/tmp/qranges" + System.currentTimeMillis() + ".dot"));

            out.println("graph \"Q ranges trees\" {");

            for (int i = 0; i < columns.length; ++i) {
                QRangeTree tree = columns[i].getQRangeTree();
                out.println(tree.toGraph());
            }

            out.println("}");

            out.flush();
        }
        catch (java.io.IOException ioe) {
            log.error(ioe);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
        */
    }

    protected static void loadQRanges(
        Session                 session,
        WstValueTable.Column [] columns,
        Wst                     wst
    ) {
        loadQRanges(session, columns, wst.getId());
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
