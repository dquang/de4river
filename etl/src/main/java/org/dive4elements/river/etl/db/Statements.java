/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.db;

import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Statements
{
    private static Logger log = LogManager.getLogger(Statements.class);

    public static final String RESOURCE_PATH = "/sql/";
    public static final String COMMON_PROPERTIES = "-common.properties";

    protected String type;
    protected String driver;

    protected Map<String, SymbolicStatement> statements;

    public Statements(String type, String driver) {
        this.type   = type;
        this.driver = driver;
    }

    public SymbolicStatement getStatement(String key) {
        return getStatements().get(key);
    }

    public Map<String, SymbolicStatement> getStatements() {
        if (statements == null) {
            statements = loadStatements();
        }
        return statements;
    }

    protected Map<String, SymbolicStatement> loadStatements() {
        Map<String, SymbolicStatement> statements =
            new HashMap<String, SymbolicStatement>();

        Properties properties = loadProperties();

        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String value = properties.getProperty(key);
            SymbolicStatement symbolic = new SymbolicStatement(value);
            statements.put(key, symbolic);
        }

        return statements;
    }

    protected String driverToProperties() {
        return
            type + "-" +
            driver.replace('.', '-').toLowerCase() + ".properties";
    }

    protected Properties loadCommon() {
        Properties common = new Properties();

        String path = RESOURCE_PATH + type + COMMON_PROPERTIES;

        InputStream in = Statements.class.getResourceAsStream(path);

        if (in != null) {
            try {
                common.load(in);
            }
            catch (IOException ioe) {
                log.error("cannot load defaults: " + path, ioe);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            }
        }
        else {
            log.warn("cannot find: " + path);
        }

        return common;
    }

    protected Properties loadProperties() {

        Properties common = loadCommon();

        Properties properties = new Properties(common);

        String path = RESOURCE_PATH + driverToProperties();

        InputStream in = Statements.class.getResourceAsStream(path);

        if (in != null) {
            try {
                properties.load(in);
            }
            catch (IOException ioe) {
                log.error("cannot load statements: " + path, ioe);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            }
        }
        else {
            log.warn("cannot find: " + path);
        }

        return properties;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
