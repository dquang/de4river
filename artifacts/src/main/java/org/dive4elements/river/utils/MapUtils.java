/* Copyright (C) 2011, 2012, 2013, 2020 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.impl.SessionFactoryImpl;

import org.dive4elements.river.backend.SessionFactoryProvider;


public class MapUtils
{
    private static final Logger log = LogManager.getLogger(MapUtils.class);

    private static final String JDBC_SCHEME = "^jdbc:";

    private static final String JDBC_DRV_PATTERN =
        JDBC_SCHEME + "(postgresql|oracle:(thin|oci)):.*";

    /**
     * This method returns a connection string for databases used by
     * Mapserver's Mapfile.
     *
     * @return A connection string for Mapserver.
     */
    public static String getConnection() {
        SessionFactoryImpl sf = (SessionFactoryImpl)
        SessionFactoryProvider.getSessionFactory();

        String user = SessionFactoryProvider.getUser(sf);
        String pass = SessionFactoryProvider.getPass(sf);
        String url  = SessionFactoryProvider.getURL(sf);

        return getConnection(user, pass, url);
    }

    public static String getConnection(String user, String pass, String url) {
        log.info("Parse connection string: " + url);

        if (!url.matches(JDBC_DRV_PATTERN)) {
            log.error("Could not parse connection string: "
                + "Not a JDBC URL with PostgreSQL or Oracle driver");
            return null;
        }

        URI uri = null;
        try {
            // Strip JDBC_SCHEME to let the driver be parsed as scheme
            uri = new URI(url.replaceFirst(JDBC_SCHEME, ""));
        }
        catch (URISyntaxException e) {
            log.error("Could not parse connection string: " + e.getMessage());
            return null;
        }

        String drv = uri.getScheme();
        log.debug("Driver: " + drv);

        String connection = null;
        if (drv.equals("oracle")) {
            try {
                // Work-around the extra colon in the driver part of the scheme
                String con = new URI(uri.getSchemeSpecificPart())
                    .getSchemeSpecificPart().replaceFirst("^@(//)?", "");
                log.debug("Database specifier: " + con);
                connection = user + "/" + pass + "@" + con;
            }
            catch (URISyntaxException e) {
                log.error("Could not parse Oracle connection string: "
                    + e.getMessage());
                return null;
            }
        }
        else { // assume PostgreSQL
            String host = uri.getHost();
            if (host == null && uri.getSchemeSpecificPart().startsWith("//")) {
                // invalid hostnames (e.g. containing '_') are not parsed!
                log.error("Could not parse PostgreSQL connection string: "
                    + "invalid host name");
                return null;
            }
            String db = host == null
                ? uri.getSchemeSpecificPart()
                : uri.getPath();
            int port = uri.getPort();
            connection = createConnectionString(user, pass, host, db, port);
        }

        return connection;
    }

    private static String createConnectionString(
        String user,
        String pass,
        String host,
        String db,
        int port
    ) {
        StringBuilder sb = new StringBuilder();
        // Required parameters
        // defaults to user name in PostgreSQL JDBC:
        if (db != null) {
            db = db.replaceFirst("/", "");
        }
        sb.append("dbname=").append(db == null || db.equals("") ? user : db);
        sb.append(" user=").append(user);

        // Optional parameters
        if (host != null) {
            sb.append(" host='").append(host).append("'");
        }
        if (port != -1) {
            sb.append(" port=").append(port);
        }
        // XXX: We need to escape this somehow.
        sb.append(" password='").append(pass).append("'");
        sb.append(" sslmode=disable");
        return sb.toString();
    }

    public static String getConnectionType() {
        return RiverUtils.isUsingOracle() ? "oraclespatial" : "postgis";
    }
}
