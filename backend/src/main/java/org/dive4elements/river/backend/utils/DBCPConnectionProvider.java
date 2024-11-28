/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dive4elements.river.backend.utils;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.util.Arrays;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.HibernateException;

import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.connection.ConnectionProvider;

import org.hibernate.cfg.Environment;

/**
 * A connection provider that uses an Apache commons DBCP connection pool.
 *
 * To use this connection provider set hibernate.connection.provider_class
 * to org.hibernate.connection.DBCPConnectionProvider
 *
 * <pre>Supported Hibernate properties:
 *   hibernate.connection.driver_class
 *   hibernate.connection.url
 *   hibernate.connection.username
 *   hibernate.connection.password
 *   hibernate.connection.isolation
 *   hibernate.connection.autocommit
 *   hibernate.connection.pool_size
 *   hibernate.connection (JDBC driver properties)</pre>
 * <br>
 * All DBCP properties are also supported by using the hibernate.dbcp prefix.
 * A complete list can be found on the DBCP configuration page:
 * http://jakarta.apache.org/commons/dbcp/configuration.html
 * <br>
 *
 * <p>More information about configuring/using DBCP can be found on the
 * <a href="http://jakarta.apache.org/commons/dbcp/">DBCP website</a>.
 * There you will also find the DBCP wiki, mailing lists, issue tracking
 * and other support facilities</p>
 *
 * @see org.hibernate.connection.ConnectionProvider
 * @author Dirk Verbeeck
 */
public class DBCPConnectionProvider
implements   ConnectionProvider
{
    private static Logger log = LogManager.getLogger(DBCPConnectionProvider.class);

    private static final String PREFIX = "hibernate.dbcp.";

    private BasicDataSource ds;

    // Old Environment property for backward-compatibility
    // (property removed in Hibernate3)
    private static final String DBCP_PS_MAXACTIVE =
        "hibernate.dbcp.ps.maxActive";

    // Property doesn't exists in Hibernate2
    private static final String AUTOCOMMIT =
        "hibernate.connection.autocommit";

    public void configure(Properties props) throws HibernateException {
        try {
            log.debug("Configure DBCPConnectionProvider");

            // DBCP properties used to create the BasicDataSource
            Properties dbcpProperties = new Properties();

            // DriverClass & url
            String jdbcDriverClass = props.getProperty(Environment.DRIVER);
            String jdbcUrl = props.getProperty(Environment.URL);
            dbcpProperties.put("driverClassName", jdbcDriverClass);
            dbcpProperties.put("url", jdbcUrl);

            // Username / password
            String username = props.getProperty(Environment.USER);
            String password = props.getProperty(Environment.PASS);
            dbcpProperties.put("username", username);
            dbcpProperties.put("password", password);

            // Isolation level
            String isolationLevel = props.getProperty(Environment.ISOLATION);
            if (isolationLevel != null
            && (isolationLevel = isolationLevel.trim()).length() > 0) {
                dbcpProperties.put(
                    "defaultTransactionIsolation",
                    isolationLevel);
            }

            // Turn off autocommit (unless autocommit property is set)
            String autocommit = props.getProperty(AUTOCOMMIT);
            if (autocommit != null
            && (autocommit = autocommit.trim()).length() > 0) {
                dbcpProperties.put("defaultAutoCommit", autocommit);
            } else {
                dbcpProperties.put(
                    "defaultAutoCommit",
                    String.valueOf(Boolean.FALSE));
            }

            // Pool size
            String poolSize = props.getProperty(Environment.POOL_SIZE);
            if (poolSize != null
            && (poolSize = poolSize.trim()).length() > 0
            && Integer.parseInt(poolSize) > 0)  {
                dbcpProperties.put("maxActive", poolSize);
            }

            // Copy all "driver" properties into "connectionProperties"
            Properties driverProps =
                ConnectionProviderFactory.getConnectionProperties(props);

            if (driverProps.size() > 0) {
                StringBuilder connectionProperties = new StringBuilder();
                for (Iterator iter = driverProps.entrySet().iterator();
                    iter.hasNext();
                ) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    String    key   = (String)entry.getKey();
                    String    value = (String)entry.getValue();
                    connectionProperties
                        .append(key)
                        .append('=')
                        .append(value);
                    if (iter.hasNext()) {
                        connectionProperties.append(';');
                    }
                }
                dbcpProperties.put(
                    "connectionProperties", connectionProperties.toString());
            }

            // Copy all DBCP properties removing the prefix
            for (Iterator iter = props.entrySet().iterator();
                 iter.hasNext();
            ) {
                Map.Entry entry = (Map.Entry)iter.next();
                String    key   = (String)entry.getKey();
                if (key.startsWith(PREFIX)) {
                    String property = key.substring(PREFIX.length());
                    String value    = (String)entry.getValue();
                    dbcpProperties.put(property, value);
                }
            }

            // Backward-compatibility
            if (props.getProperty(DBCP_PS_MAXACTIVE) != null) {
                dbcpProperties.put(
                    "poolPreparedStatements",
                    String.valueOf(Boolean.TRUE));
                dbcpProperties.put(
                    "maxOpenPreparedStatements",
                    props.getProperty(DBCP_PS_MAXACTIVE));
            }

            // Some debug info
            /* // commented out, because it leaks the password
            if (log.isDebugEnabled()) {
                log.debug("Creating a DBCP BasicDataSource" +
                          " with the following DBCP factory properties:");
                StringWriter sw = new StringWriter();
                dbcpProperties.list(new PrintWriter(sw, true));
                log.debug(sw.toString());
            }
            */

            // Let the factory create the pool
            ds = (BasicDataSource)BasicDataSourceFactory
                .createDataSource(dbcpProperties);

            // This needs to be done manually as it is somehow ignored
            // by the BasicDataSourceFactory if you set it as a dbcpProperty
            String connectionInitSqls = props.getProperty(
                "connectionInitSqls");
            if (connectionInitSqls != null) {
                String[] statements = connectionInitSqls.split(";");
                ds.setConnectionInitSqls(Arrays.asList(statements));
            }

            String validationQuery = props.getProperty("validationQuery");
            if (validationQuery != null) {
                ds.setValidationQuery(validationQuery);
            }

            String maxWait = props.getProperty("maxWait");
            if (maxWait != null) {
                try {
                    ds.setMaxWaitMillis(Integer.parseInt(maxWait));
                }
                catch (NumberFormatException nfe) {
                    log.error(
                        "Property maxWait could not be parsed as integer."
                    );
                }
            }

            // The BasicDataSource has lazy initialization
            // borrowing a connection will start the DataSource
            // and make sure it is configured correctly.

            // Connection conn = ds.getConnection();
            // conn.close();
        }
        catch (Exception e) {
            String message = "Could not create a DBCP pool";
            log.fatal(message, e);
            if (ds != null) {
                BasicDataSource x = ds; ds = null;
                try {
                    x.close();
                }
                catch (SQLException sqle) {
                }
            }
            throw new HibernateException(message, e);
        }
        log.debug("Configure DBCPConnectionProvider complete");
    }

    public Connection getConnection() throws SQLException {
        log.trace("Connection pool parameters:");
        log.trace("_ active connections: " + ds.getNumActive());
        log.trace("_ idle connections: " + ds.getNumIdle());
        log.trace("_ max active: " + ds.getMaxTotal());
        if (ds.getNumActive() == ds.getMaxTotal()) {
            log.warn("Maximum number of database connections in pool in use!");
        }

        try {
            Connection conn = ds.getConnection();
            log.trace("Return connection with hash: " + conn.hashCode());
            return conn;
        }
        catch (SQLException sqle) {
            throw new SQLException(
                "Connecting to database " + ds.getUrl() + " failed",
                sqle);
        }
    }

    public void closeConnection(Connection conn) throws SQLException {
        log.trace("Close connection with hash: " + conn.hashCode());
        conn.close();
    }

    public void close() throws HibernateException {
        try {
            if (ds != null) {
                BasicDataSource x = ds; ds = null;
                x.close();
            }
        }
        catch (SQLException sqle) {
            throw new HibernateException("Could not close DBCP pool", sqle);
        }
    }

    public boolean supportsAggressiveRelease() {
        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
