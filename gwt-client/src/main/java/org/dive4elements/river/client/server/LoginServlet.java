/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.server.auth.Authentication;
import org.dive4elements.river.client.server.auth.AuthenticationException;
import org.dive4elements.river.client.server.auth.AuthenticationFactory;
import org.dive4elements.river.client.server.features.Features;

public class LoginServlet extends AuthenticationServlet {

    private static Logger log = LogManager.getLogger(LoginServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        String encoding = req.getCharacterEncoding();
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        log.debug("Processing post request");

        if (username == null || password == null) {
            log.debug("No username or password provided");
            this.redirectFailure(resp, req.getContextPath());
            return;
        }

        try {
            Authentication aresp = this.auth(username, password, encoding);
            if (aresp == null || !aresp.isSuccess()) {
                log.debug("Authentication not successful");
                this.redirectFailure(resp, req.getContextPath());
                return;
            }
            log.info("Authentication successfull.");
            this.performLogin(req, resp, aresp.getUser());
        }
        catch(AuthenticationException e) {
            log.error(e.getMessage());
            this.redirectFailure(resp, req.getContextPath(), e);
        }
    }

    private Authentication auth(
        String username,
        String password,
        String encoding
    )
        throws AuthenticationException, IOException
    {
        ServletContext sc = this.getServletContext();
        Features features = (Features)sc.getAttribute(
            Features.CONTEXT_ATTRIBUTE);
        String auth = sc.getInitParameter("authentication");
        return AuthenticationFactory.getInstance(auth).auth(username, password,
                encoding, features, sc);
    }
}
