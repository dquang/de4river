/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64InputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.server.auth.AuthenticationException;
import org.dive4elements.river.client.server.auth.User;
import org.dive4elements.river.client.server.auth.saml.TicketValidator;
import org.dive4elements.river.client.server.auth.saml.Assertion;
import org.dive4elements.river.client.server.features.Features;


public class SamlServlet extends AuthenticationServlet {

    private static Logger log = LogManager.getLogger(SamlServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        String encoding = req.getCharacterEncoding();
        String samlTicketXML = req.getParameter("saml");

        log.debug("Processing post request");

        if (samlTicketXML == null) {
            log.debug("No saml ticket provided");
            this.redirectFailure(resp, req.getContextPath());
            return;
        }

        try {
            User user = this.auth(samlTicketXML);
            if (user == null) {
                log.debug("Authentication not successful");
                this.redirectFailure(resp, req.getContextPath());
                return;
            }
            this.performLogin(req, resp, user);
            log.info("Authentication with existing SAML ticket.");
        }
        catch(AuthenticationException e) {
            log.error(e, e);
            this.redirectFailure(resp, req.getContextPath(), e);
        }
    }

    private User auth(String samlTicketXML)
        throws AuthenticationException, IOException
    {
        ServletContext sc = this.getServletContext();

        Assertion assertion = null;
        try {
            File keyfile = new File(
                sc.getInitParameter("saml-trusted-public-key"));
            String path = keyfile.isAbsolute()
                ? keyfile.getPath()
                : sc.getRealPath(keyfile.getPath());
            int timeEps = Integer.parseInt(
                sc.getInitParameter("saml-time-tolerance"));
            TicketValidator validator = new TicketValidator(path, timeEps);

            InputStream in = new StringBufferInputStream(samlTicketXML);
            assertion = validator.checkTicket(new Base64InputStream(in));
        }
        catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        if (assertion == null) {
            throw new AuthenticationException("Login failed.");
        }

        Features features = (Features)sc.getAttribute(
            Features.CONTEXT_ATTRIBUTE);
        return new org.dive4elements.river.client.server.auth.saml.User(
            assertion, samlTicketXML,
            features.getFeatures(assertion.getRoles()), null);
    }
}
