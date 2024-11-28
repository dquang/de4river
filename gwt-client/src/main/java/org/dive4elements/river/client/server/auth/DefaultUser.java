/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth;

import java.util.List;

public class DefaultUser
implements   User
{
    protected String  name;
    protected String  account;
    protected String  password;
    protected String  samlXML;
    protected boolean expired;
    protected List<String> roles;
    protected List<String> features;

    public DefaultUser() {
    }

    public DefaultUser(
        String       name,
        String       password,
        String       samlXML,
        boolean      expired,
        List<String> roles,
        List<String> features
    ) {
        this.name     = name;
        this.password = password;
        this.samlXML  = samlXML;
        this.expired  = expired;
        this.roles    = roles;
        this.features = features;
        this.account  = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean hasExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    @Override
    public List<String> getRoles() {
        // XXX: return clone of the list?
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean canUseFeature(String feature) {
        return this.features.contains(feature);
    }

    public void setAllowedFeatures(List<String> features) {
        this.features = features;
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getSamlXMLBase64() {
        return this.samlXML;
    }

    public void setSamlXMLBase64(String samlXML) {
        this.samlXML = samlXML;
    }
}
// vim:set ts=4 sw=4 si et fenc=utf8 tw=80:
