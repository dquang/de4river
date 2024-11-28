/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.river.client.server.auth.User;

import javax.servlet.http.HttpSession;

public class RemoteServiceServlet
extends      com.google.gwt.user.server.rpc.RemoteServiceServlet
{
    /**
     * Return the current logged in user from the HTTP Session.
     */
    public User getUser() {
        HttpSession session = this.getThreadLocalRequest().getSession();
        return (User)session.getAttribute("user");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
