/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.db;

import org.dive4elements.river.etl.utils.XML;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ConnectionBuilder
{
    private static Logger log = LogManager.getLogger(ConnectionBuilder.class);

    public static final String XPATH_DRIVER     = "/sync/side[@name=$type]/db/driver/text()";
    public static final String XPATH_USER       = "/sync/side[@name=$type]/db/user/text()";
    public static final String XPATH_PASSWORD   = "/sync/side[@name=$type]/db/password/text()";
    public static final String XPATH_URL        = "/sync/side[@name=$type]/db/url/text()";
    public static final String XPATH_EXEC_LOGIN = "/sync/side[@name=$type]/db/execute-login/statement";

    protected String       type;
    protected String       driver;
    protected String       user;
    protected String       password;
    protected String       url;
    protected List<String> loginStatements;

    public ConnectionBuilder(String type, Document document) {
        this.type = type;
        extractCredentials(document);
    }

    protected static List<String> extractStrings(NodeList nodes) {
        int N = nodes.getLength();
        List<String> result = new ArrayList<String>(N);
        for (int i = 0; i < N; ++i) {
            result.add(nodes.item(i).getTextContent());
        }
        return result;
    }

    protected void extractCredentials(Document document) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", type);

        driver = (String)XML.xpath(
            document, XPATH_DRIVER, XPathConstants.STRING, null, map);
        user = (String)XML.xpath(
            document, XPATH_USER, XPathConstants.STRING, null, map);
        password = (String)XML.xpath(
            document, XPATH_PASSWORD, XPathConstants.STRING, null, map);
        url = (String)XML.xpath(
            document, XPATH_URL, XPathConstants.STRING, null, map);
        loginStatements = extractStrings((NodeList)XML.xpath(
            document, XPATH_EXEC_LOGIN, XPathConstants.NODESET, null, map));

        if (log.isDebugEnabled()) {
            log.debug("driver: " + driver);
            log.debug("user: " + user);
            log.debug("password: *******");
            log.debug("url: " + url);
            log.debug("number of login statements: " + loginStatements.size());
        }
    }

    public Connection getConnection() throws SQLException {

        if (driver != null && driver.length() > 0) {
            try {
                Class.forName(driver);
            }
            catch (ClassNotFoundException cnfe) {
                throw new SQLException(cnfe);
            }
        }

        Connection connection =
            DriverManager.getConnection(url, user, password);

        connection.setAutoCommit(false);

        DatabaseMetaData metaData = connection.getMetaData();

        if (metaData.supportsTransactionIsolationLevel(
            Connection.TRANSACTION_READ_UNCOMMITTED)) {
            connection.setTransactionIsolation(
                Connection.TRANSACTION_READ_UNCOMMITTED);
        }

        for (String sql: loginStatements) {
            Statement stmnt = connection.createStatement();
            try {
                stmnt.execute(sql);
            }
            finally {
                stmnt.close();
            }
        }

        return connection;
    }

    public ConnectedStatements getConnectedStatements() throws SQLException {
        return new ConnectedStatements(
            getConnection(),
            new Statements(type, driver != null ? driver : "")
                .getStatements());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
