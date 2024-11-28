/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend;

public abstract class Credentials
{
    protected String   user;
    protected String   password;
    protected String   dialect;
    protected String   driver;
    protected String   url;
    protected String   connectionInitSqls;
    protected String   validationQuery;
    protected String   maxWait;
    protected Class [] classes;

    public Credentials() {
    }

    public Credentials(
        String   user,
        String   password,
        String   dialect,
        String   driver,
        String   url,
        String   connectionInitSqls,
        String   validationQuery,
        String    maxWait,
        Class [] classes
    ) {
        this.user               = user;
        this.password           = password;
        this.dialect            = dialect;
        this.driver             = driver;
        this.url                = url;
        this.connectionInitSqls = connectionInitSqls;
        this.validationQuery    = validationQuery;
        this.maxWait            = maxWait;
        this.classes            = classes;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(String maxWait) {
        this.maxWait = maxWait;
    }

    public String getConnectionInitSqls() {
        return connectionInitSqls;
    }

    public void setConnectionInitSqls(String connectionInitSqls) {
        this.connectionInitSqls = connectionInitSqls;
    }

    public Class [] getClasses() {
        return classes;
    }

    public void setClasses(Class [] classes) {
        this.classes = classes;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
