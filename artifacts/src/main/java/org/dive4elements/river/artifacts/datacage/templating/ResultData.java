/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.io.Serializable;

import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Result Data from a DB/SQL query. */
public class ResultData
implements   Serializable
{
    private static Logger log = LogManager.getLogger(ResultData.class);

    protected String [] columns;

    protected List<Object []> rows;

    public ResultData() {
        rows = new ArrayList<Object []>();
    }

    public ResultData(String [] columns) {
        this(columns, new ArrayList<Object []>());
    }

    public ResultData(String [] columns, List<Object []> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public ResultData(ResultSetMetaData meta)
    throws SQLException
    {
        this();

        boolean debug = log.isDebugEnabled();

        int N = meta.getColumnCount();

        columns = new String[N];

        if (debug) {
            log.debug("ResultSet column names:");
        }

        for (int i = 1; i <= N; ++i) {
            columns[i-1] = meta.getColumnLabel(i).toUpperCase();
            if (debug) {
                log.debug("    " + i + ": " + columns[i-1]);
            }
        }
    }

    public String [] getColumnLabels() {
        return columns;
    }

    public ResultData addAll(ResultSet result) throws SQLException {
        while (result.next()) {
            add(result);
        }
        return this;
    }

    public int indexOfColumn(String column) {
        for (int i = 0; i < columns.length; ++i) {
            if (columns[i].equalsIgnoreCase(column)) {
                return i;
            }
        }
        return -1;
    }

    public void add(Object [] result) {
        rows.add(result);
    }

    public void add(ResultSet result) throws SQLException {
        Object [] row = new Object[columns.length];
        for (int i = 0; i < columns.length; ++i) {
            row[i] = result.getObject(i+1);
        }
        rows.add(row);
    }

    public List<Object []> getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
