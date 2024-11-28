/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.saml;

import java.util.List;

import org.dive4elements.river.client.server.auth.DefaultUser;

public class User
extends DefaultUser
implements org.dive4elements.river.client.server.auth.User {

    private Assertion assertion;

    public User(Assertion assertion, String samlXML, List<String> features,
                String password) {
        this.setName(assertion.getNameID());
        this.setAccount(assertion.getNameID());
        this.setRoles(assertion.getRoles());
        this.assertion = assertion;
        this.setSamlXMLBase64(samlXML);
        this.setAllowedFeatures(features);
        this.setPassword(password);
    }

    @Override
    public boolean hasExpired() {
        // We could check the validity dates of the assertion here, but
        // when using this for Single-Sign-On this would lead to the
        // code in GGInAFilter to re-authenticate with the password
        // stored in the User object, which isn't known in the case of
        // Single-Sign-On.
        return false;
    }
}

// vim:set ts=4 sw=4 si et fenc=utf8 tw=80:
