/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.artifactdatabase.db.SQL;
import org.dive4elements.artifactdatabase.db.DBConnection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DBConfig
{
    private static Logger log = LogManager.getLogger(DBConfig.class);

     /**
     * XPath to access the database driver within the global configuration.
     */
    public static final String DB_DRIVER =
        "/artifact-database/datacage/driver/text()";
    /**
     * XPath to access the database URL within the global configuration.
     */
    public static final String DB_URL =
        "/artifact-database/datacage/url/text()";
    /**
     * XPath to access the database use within the global configuration.
     */
    public static final String DB_USER =
        "/artifact-database/datacage/user/text()";
    /**
     * XPath to access the database password within the global configuration.
     */
    public static final String DB_PASSWORD =
        "/artifact-database/datacage/password/text()";

    /**
     * The default database driver: H2
     */
    public static final String DEFAULT_DRIVER =
        "org.h2.Driver";

    /**
     * The default database user: ""
     */
    public static final String DEFAULT_USER = "";

    /**
     * The default database password: ""
     */
    public static final String DEFAULT_PASSWORD = "";


    public static final String DEFAULT_URL =
        "jdbc:h2:mem:datacage;"
        + "INIT=RUNSCRIPT FROM '${artifacts.config.dir}/datacage.sql'";

    public static final String RESOURCE_PATH = "/datacage-sql";

    private static DBConfig instance;

    protected DBConnection dbConnection;
    protected SQL          sql;

    public DBConfig() {
    }

    public DBConfig(DBConnection dbConnection, SQL sql) {
        this.dbConnection = dbConnection;
        this.sql          = sql;
    }

    public static synchronized DBConfig getInstance() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    protected static DBConfig createInstance() {
        String driver = Config.getStringXPath(
            DB_DRIVER, DEFAULT_DRIVER);

        String url = Config.getStringXPath(
            DB_URL, DEFAULT_URL);

        url = Config.replaceConfigDir(url);

        String user = Config.getStringXPath(
            DB_USER, DEFAULT_USER);

        String password = Config.getStringXPath(
            DB_PASSWORD, DEFAULT_PASSWORD);

        DBConnection dbConnection = new DBConnection(
            driver, url, user, password);

        SQL sql = new SQL(DBConfig.class, RESOURCE_PATH, driver);

        return new DBConfig(dbConnection, sql);
    }

    public DBConnection getDBConnection() {
        return dbConnection;
    }

    public SQL getSQL() {
        return sql;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
