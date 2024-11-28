/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;

import org.dive4elements.river.client.client.services.UserService;
import org.dive4elements.river.client.server.auth.UserClient;
import org.dive4elements.river.client.shared.exceptions.AuthenticationException;
import org.dive4elements.river.client.shared.model.DefaultUser;
import org.dive4elements.river.client.shared.model.User;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class UserServiceImpl
extends      RemoteServiceServlet
implements   UserService
{
    /** Private log. */
    private static final Logger log = LogManager.getLogger(UserServiceImpl.class);

    public static final String ERROR_NO_SUCH_USER = "error_no_such_user";

    public static final String ERROR_NO_USERS = "error_no_users";

    public User getCurrentUser(String locale)
    throws AuthenticationException
    {
        String url = getServletContext().getInitParameter("server-url");

        UserClient client = new UserClient(url);
        org.dive4elements.river.client.server.auth.User loginuser = getUser();

        if (loginuser == null) {
            log.debug("no session user");
            throw new AuthenticationException(ERROR_NO_SUCH_USER);
        }

        try {
            Element user = client.findUser(loginuser);

            if (user != null) {
                String uuid = user.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "uuid");
                String name = user.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "name");

                return new DefaultUser(uuid, name,
                                       loginuser.getSamlXMLBase64());
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        log.error("No users existing in the server.");
        throw new AuthenticationException(ERROR_NO_USERS);
    }

    public void logoutCurrentUser() {
        HttpSession session = this.getThreadLocalRequest().getSession();
        session.setAttribute("user", null);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
