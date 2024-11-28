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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.server.auth.User;
import org.dive4elements.river.client.server.auth.UserClient;

/**
 * Base class for servlets performing authentication and login.
 */
public class AuthenticationServlet extends HttpServlet {

    private static Logger log = LogManager.getLogger(AuthenticationServlet.class);

    private static final String FLYS_PAGE = "FLYS.html";
    private static final String LOGIN_PAGE = "login.jsp";

    protected void redirectFailure(HttpServletResponse resp, String path)
        throws IOException {
        resp.sendRedirect(path + "/" + LOGIN_PAGE);
    }

    protected void redirectFailure(HttpServletResponse resp, String path,
            Exception e) throws IOException {
        this.redirectFailure(resp, path, e.getMessage());
    }

    protected void redirectFailure(HttpServletResponse resp, String path,
            String message) throws IOException {
        resp.sendRedirect(path + "/" + LOGIN_PAGE + "?error=" + message);
    }

    protected void redirectSuccess(HttpServletResponse resp, String path,
            String uri) throws IOException {
        if (uri == null) {
            String redirecturl = getServletContext().getInitParameter(
                "redirect-url");
            if (redirecturl == null) {
                redirecturl = FLYS_PAGE;
            }
            uri = "/" + redirecturl;
        }
        resp.sendRedirect(uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        log.debug("Processing get request");
        this.redirectFailure(resp, req.getContextPath());
    }

    protected void performLogin(HttpServletRequest req,
                                HttpServletResponse resp, User user)
                                    throws ServletException, IOException {
        String url = getServletContext().getInitParameter("server-url");
        UserClient client = new UserClient(url);
        if (!client.userExists(user)) {
            log.debug("Creating db user");
            if (!client.createUser(user)) {
                this.redirectFailure(resp, req.getContextPath(),
                                     "Could not create new user");
                return;
            }
        }

        HttpSession session = req.getSession();
        session.setAttribute("user", user);

        String uri = (String)session.getAttribute("requesturi");

        this.redirectSuccess(resp, req.getContextPath(), uri);
    }
}
