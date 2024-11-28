/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** SQL Statement, create PreparedStatement. */
public class CompiledStatement
{
    private static Logger log = LogManager.getLogger(CompiledStatement.class);

    public static final String DATACAGE_DB_CACHE =
        "datacage.db";

    public static final Pattern VAR =
        Pattern.compile("\\$\\{([a-zA-Z0-9_-]+)\\}");

    protected String original;
    protected String statement;

    protected Map<String, List<Integer>> positions;

    protected int numVars;

    public class Instance {

        protected PreparedStatement preparedStatement;

        public Instance() {
        }

        /** Executes a Statement. */
        protected ResultData executeCached(
            Cache       cache,
            Connection  connection,
            StackFrames frames
        )
        throws SQLException
        {
            log.debug("executeCached");
            Object [] values = new Object[numVars];

            StringBuilder sb = new StringBuilder(original);

            for (Map.Entry<String, List<Integer>> entry: positions.entrySet()) {
                String key   = entry.getKey();
                Object value = frames.get(key);
                sb.append(';').append(key).append(':').append(value);
                for (Integer index: entry.getValue()) {
                    values[index] = value;
                }
            }

            // XXX: Maybe too many collisions?
            // String key = original + Arrays.hashCode(values);
            String key = sb.toString();

            Element element = cache.get(key);

            if (element != null) {
                return (ResultData)element.getValue();
            }

            if (preparedStatement == null) {
                preparedStatement = connection.prepareStatement(statement);
            }

            for (int i = 0; i < values.length; ++i) {
                preparedStatement.setObject(i+1, values[i]);
            }

            ResultData data;

            if (log.isDebugEnabled()) {
                log.debug("executing: " + statement);
            }

            ResultSet result = preparedStatement.executeQuery();
            try {
                data = new ResultData(preparedStatement.getMetaData())
                    .addAll(result);
            }
            finally {
                result.close();
            }

            element = new Element(key, data);
            cache.put(element);

            return data;
        }

        protected ResultData executeUncached(
            Connection  connection,
            StackFrames frames
        )
        throws SQLException
        {
            log.debug("executeUncached");
            if (preparedStatement == null) {
                if (log.isDebugEnabled()) {
                    log.debug("preparing statement: " + statement);
                }
                preparedStatement = connection.prepareStatement(statement);
            }

            for (Map.Entry<String, List<Integer>> entry: positions.entrySet()) {
                Object value = frames.get(entry.getKey());
                for (Integer index: entry.getValue()) {
                    preparedStatement.setObject(index+1, value);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("executing: " + statement);
            }

            ResultSet result = preparedStatement.executeQuery();
            try {
                return new ResultData(preparedStatement.getMetaData())
                    .addAll(result);
            }
            finally {
                result.close();
            }
        }

        public ResultData execute(
            Connection  connection,
            StackFrames frames,
            boolean     cached
        )
        throws SQLException
        {
            if (!cached) {
                return executeUncached(connection, frames);
            }

            Cache cache = CacheFactory.getCache(DATACAGE_DB_CACHE);

            return cache != null
                ? executeCached(cache, connection, frames)
                : executeUncached(connection, frames);
        }

        public void close() {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                }
                catch (SQLException sqle) {
                }
                preparedStatement = null;
            }
        }
    } // class Instance

    public CompiledStatement() {
    }

    public CompiledStatement(String original) {
        this.original = original;
        // TreeMap to ensure order
        positions = new TreeMap<String, List<Integer>>();
        compile();
    }

    protected void compile() {

        StringBuffer sb = new StringBuffer();

        Matcher m = VAR.matcher(original);

        int index = 0;

        // Find variables like ${varname}.
        while (m.find()) {
            String key = m.group(1).toUpperCase();
            List<Integer> indices = positions.get(key);
            if (indices == null) {
                indices = new ArrayList<Integer>();
                positions.put(key, indices);
            }
            indices.add(index);
            m.appendReplacement(sb, "?");
            ++index;
        }

        m.appendTail(sb);

        numVars = index;

        statement = sb.toString();
    }

    public String getStatement() {
        return statement;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
