/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SymbolicStatement {

    private static Logger log = LogManager.getLogger(SymbolicStatement.class);

    public static final Pattern VAR = Pattern.compile(":([a-zA-Z0-9_]+)");

    protected String statement;
    protected String compiled;
    protected Map<String, List<Integer>> positions;

    public class Instance {

        /** TODO: Support more types. */

        protected PreparedStatement stmnt;

        public Instance(Connection connection) throws SQLException {
            stmnt = connection.prepareStatement(compiled);
        }

        public void close() {
            try {
                stmnt.close();
            }
            catch (SQLException sqle) {
                log.error("cannot close statement", sqle);
            }
        }

        public Instance setInt(String key, int value)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setInt(p, value);
                }
            }

            return this;
        }

        public Instance setString(String key, String value)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setString(p, value);
                }
            }
            return this;
        }

        public Instance setObject(String key, Object value)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setObject(p, value);
                }
            }
            return this;
        }

        public Instance setTimestamp(String key, Timestamp value)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setTimestamp(p, value);
                }
            }
            return this;
        }

        public Instance setDouble(String key, double value)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setDouble(p, value);
                }
            }
            return this;
        }

        public Instance setLong(String key, long value)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setLong(p, value);
                }
            }
            return this;
        }

        public Instance setNull(String key, int sqlType)
        throws SQLException
        {
            List<Integer> pos = positions.get(key.toLowerCase());
            if (pos != null) {
                for (Integer p: pos) {
                    stmnt.setNull(p, sqlType);
                }
            }
            return this;
        }

        public Instance set(Map<String, Object> map) throws SQLException {
            for (Map.Entry<String, Object> entry: map.entrySet()) {
                setObject(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Instance clearParameters() throws SQLException {
            stmnt.clearParameters();
            return this;
        }

        public boolean execute() throws SQLException {
            if (log.isDebugEnabled()) {
                log.debug("execute: " + compiled);
            }
            return stmnt.execute();
        }

        public ResultSet executeQuery() throws SQLException {
            if (log.isDebugEnabled()) {
                log.debug("query: " + compiled);
            }
            return stmnt.executeQuery();
        }

        public int executeUpdate() throws SQLException {
            if (log.isDebugEnabled()) {
                log.debug("update: " + compiled);
            }
            return stmnt.executeUpdate();
        }

    } // class Instance

    public SymbolicStatement(String statement) {
        this.statement = statement;
        compile();
    }

    public String getStatement() {
        return statement;
    }

    protected void compile() {
        positions = new HashMap<String, List<Integer>>();

        StringBuffer sb = new StringBuffer();
        Matcher m = VAR.matcher(statement);
        int index = 1;
        while (m.find()) {
            String key = m.group(1).toLowerCase();
            List<Integer> list = positions.get(key);
            if (list == null) {
                list = new ArrayList<Integer>();
                positions.put(key, list);
            }
            list.add(index++);
            m.appendReplacement(sb, "?");
        }
        m.appendTail(sb);
        compiled = sb.toString();
    }
} // class SymbolicStatement
