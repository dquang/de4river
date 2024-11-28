/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth;

import java.util.List;

/**
 * User representation after a succesfull login
 */
public interface User {

    /**
     * Returns the username as String
     */
    public String getName();

    /**
     * Returns the password of the user as String
     */
    public String getPassword();

    /**
     * Returns True if the authentication for the user
     * has expired.
     */
    public boolean hasExpired();

    /**
     * Returns a list of roles corresponsing the the user
     */
    public List<String> getRoles();


    /**
     * Returns true if the user is allowed access the feature
     */
    public boolean canUseFeature(String feature);

    /**
     * Returns the users account name
     */
    public String getAccount();

    /**
     * Returns the SAML ticket for single sign-on.
     * @return The SAML ticket in base64 encoded XML. null if no ticket
     * is available.
     */
    public String getSamlXMLBase64();
}
// vim:set ts=4 sw=4 si et fenc=utf8 tw=80:
