/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ConnectedStatements
{
    private static Logger log = LogManager.getLogger(ConnectedStatements.class);

    protected Connection connection;

    protected Map<String, SymbolicStatement> statements;

    protected Map<String, SymbolicStatement.Instance> boundStatements;

    protected Deque<Savepoint> savepoints;

    public ConnectedStatements(
        Connection connection,
        Map<String, SymbolicStatement> statements
    )
    throws SQLException
    {
        this.connection = connection;
        this.statements = statements;
        checkSavePoints();

        boundStatements = new HashMap<String, SymbolicStatement.Instance>();
    }

    protected void checkSavePoints() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        if (metaData.supportsSavepoints()) {
            log.info("Driver '" + metaData.getDriverName() +
                "' does support savepoints.");
            savepoints = new ArrayDeque<Savepoint>();
        }
        else {
            log.info("Driver '" + metaData.getDriverName() +
                "' does not support savepoints.");
        }
    }

    public SymbolicStatement.Instance getStatement(String key)
    throws SQLException
    {
        SymbolicStatement.Instance stmnt = boundStatements.get(key);
        if (stmnt != null) {
            return stmnt;
        }

        SymbolicStatement ss = statements.get(key);
        if (ss == null) {
            return null;
        }

        stmnt = ss.new Instance(connection);
        boundStatements.put(key, stmnt);
        return stmnt;
    }

    public void beginTransaction() throws SQLException {
        if (savepoints != null) {
            savepoints.push(connection.setSavepoint());
        }
    }

    public void commitTransaction() throws SQLException {
        if (savepoints != null) {
            savepoints.pop();
        }
        connection.commit();
    }

    public void rollbackTransaction() throws SQLException {
        if (savepoints != null) {
            Savepoint savepoint = savepoints.pop();
            connection.rollback(savepoint);
        }
        else {
            connection.rollback();
        }
    }

    public void close() {
        for (SymbolicStatement.Instance s: boundStatements.values()) {
            s.close();
        }

        try {
            if (savepoints != null && !savepoints.isEmpty()) {
                Savepoint savepoint = savepoints.peekFirst();
                connection.rollback(savepoint);
            }
            connection.close();
        }
        catch (SQLException sqle) {
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
