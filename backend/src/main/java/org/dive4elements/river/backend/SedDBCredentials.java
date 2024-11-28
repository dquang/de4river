/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend;

import org.dive4elements.artifacts.common.utils.Config;


public class SedDBCredentials
extends      Credentials
{
    public static final String XPATH_USER =
        "/artifact-database/seddb-database/user/text()";

    public static final String XPATH_PASSWORD =
        "/artifact-database/seddb-database/password/text()";

    public static final String XPATH_DIALECT =
        "/artifact-database/seddb-database/dialect/text()";

    public static final String XPATH_DRIVER =
        "/artifact-database/seddb-database/driver/text()";

    public static final String XPATH_URL =
        "/artifact-database/seddb-database/url/text()";

    public static final String XPATH_CONNECTION_INIT_SQLS =
        "/artifact-database/seddb-database/connection-init-sqls/text()";

    public static final String XPATH_VALIDATION_QUERY =
        "/artifact-database/seddb-database/validation-query/text()";

    public static final String XPATH_MAX_WAIT =
        "/artifact-database/seddb-database/max-wait/text()";

    public static final String DEFAULT_USER =
        System.getProperty("flys.seddb.user", "seddb");

    public static final String DEFAULT_PASSWORD =
        System.getProperty("flys.seddb.password", "seddb");

    public static final String DEFAULT_DIALECT =
        System.getProperty(
            "flys.seddb.dialect",
            "org.hibernate.dialect.PostgreSQLDialect");

    public static final String DEFAULT_DRIVER =
        System.getProperty(
            "flys.seddb.driver",
            "org.postgresql.Driver");

    public static final String DEFAULT_URL =
        System.getProperty(
            "flys.seddb.url",
            "jdbc:postgresql://localhost:5432/seddb");

    public static final String DEFAULT_CONNECTION_INIT_SQLS =
        System.getProperty(
            "flys.seddb.connection.init.sqls");

    public static final String DEFAULT_VALIDATION_QUERY =
        System.getProperty(
            "flys.seddb.connection.validation.query");

    public static final String DEFAULT_MAX_WAIT =
        System.getProperty("flys.seddb.connection.max.wait");

    public static final Class [] CLASSES = {};

    private static Credentials instance;

    public SedDBCredentials() {
    }

    public SedDBCredentials(
        String user,
        String password,
        String dialect,
        String driver,
        String url,
        String connectionInitSqls,
        String validationQuery,
        String maxWait
    ) {
        super(
            user, password, dialect, driver, url,
            connectionInitSqls, validationQuery, maxWait, CLASSES);
    }

    public static synchronized Credentials getInstance() {
        if (instance == null) {
            String user =
                Config.getStringXPath(XPATH_USER, DEFAULT_USER);
            String password =
                Config.getStringXPath(XPATH_PASSWORD, DEFAULT_PASSWORD);
            String dialect =
                Config.getStringXPath(XPATH_DIALECT, DEFAULT_DIALECT);
            String driver =
                Config.getStringXPath(XPATH_DRIVER, DEFAULT_DRIVER);
            String url =
                Config.getStringXPath(XPATH_URL, DEFAULT_URL);
            String connectionInitSqls =
                Config.getStringXPath(
                    XPATH_CONNECTION_INIT_SQLS,
                    DEFAULT_CONNECTION_INIT_SQLS);
            String validationQuery =
                Config.getStringXPath(
                    XPATH_VALIDATION_QUERY,
                    DEFAULT_VALIDATION_QUERY);
            String maxWait =
                Config.getStringXPath(XPATH_MAX_WAIT, DEFAULT_MAX_WAIT);

            instance = new SedDBCredentials(
                user, password, dialect, driver, url,
                connectionInitSqls, validationQuery, maxWait);
        }
        return instance;
    }

    public static Credentials getDefault() {
        return new SedDBCredentials(
            DEFAULT_USER,
            DEFAULT_PASSWORD,
            DEFAULT_DIALECT,
            DEFAULT_DRIVER,
            DEFAULT_URL,
            DEFAULT_CONNECTION_INIT_SQLS,
            DEFAULT_VALIDATION_QUERY,
            DEFAULT_MAX_WAIT
        );
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
