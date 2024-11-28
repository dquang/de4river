/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.filter;

import org.dive4elements.river.client.server.auth.Authentication;
import org.dive4elements.river.client.server.auth.AuthenticationException;
import org.dive4elements.river.client.server.auth.AuthenticationFactory;
import org.dive4elements.river.client.server.auth.User;
import org.dive4elements.river.client.server.features.Features;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** ServletFilter used for GGInA authentification and certain authorisation. */
public class GGInAFilter implements Filter {

    /** Private log. */
    private static Logger log = LogManager.getLogger(GGInAFilter.class);

    private boolean deactivate = false;
    private boolean loginDisabled = false;
    private String authmethod;
    private String redirecturl;
    private String noAuthRedir;
    private ServletContext sc;

    private static final String LOGIN_JSP     = "/login.jsp";
    private static final String LOGIN_SERVLET = "/flys/login";
    private static final String SAML_SERVLET = "/flys/saml";
    private static final String FLYS_CSS      = "/FLYS.css";


    /**
     * Initialize.
     *
     * Read FilterConfig parameter deactivate
     */
    @Override
    public void init(FilterConfig config)
    throws ServletException
    {
        String deactivate = config.getInitParameter("deactivate");
        this.sc = config.getServletContext();
        log.debug("GGInAFilter context " + this.sc.getContextPath());
        this.authmethod = sc.getInitParameter("authentication");
        this.redirecturl = sc.getInitParameter("redirect-url");

        noAuthRedir = sc.getInitParameter("unauth-redirect-url");
        if (noAuthRedir == null) {
            noAuthRedir = LOGIN_JSP;
        }

        String disableLogin = sc.getInitParameter("disable-login");
        if (disableLogin != null && disableLogin.equalsIgnoreCase("true")) {
            loginDisabled = true;
        }

        if (deactivate != null && deactivate.equalsIgnoreCase("true")) {
            this.deactivate = true;
        }

    }


    /**
     * Called when filter in chain invoked.
     * @param req request to servlet
     * @param resp response of servlet
     * @param chain the filter chain
     */
    @Override
    public void doFilter(
        ServletRequest req,
        ServletResponse resp,
        FilterChain chain
    )
    throws IOException, ServletException
    {
        if (this.deactivate) {
            log.debug("GGinAFilter is deactivated");
            chain.doFilter(req, resp);
            return;
        }

        HttpServletRequest sreq = (HttpServletRequest) req;

        String requesturi = sreq.getRequestURI();
        if (log.isDebugEnabled()) {
            for (Enumeration e = req.getAttributeNames();
                 e.hasMoreElements();
            ) {
                log.debug(e.nextElement());
            }
        }

        log.debug("Request for: " + requesturi);

        // Allow access to localhost
        if (isLocalAddress(req)) {
            String noAuth = sreq.getHeader("X_NO_GGINA_AUTH");
            if (noAuth != null && noAuth.equals("TRUE")) {
                log.debug("Request to localhost");
                chain.doFilter(req, resp);
                return;
            }
        }

        // Allow access to login pages
        String path = this.sc.getContextPath();
        if (requesturi.equals(path + LOGIN_JSP)
                || requesturi.equals(path + LOGIN_SERVLET)
                || requesturi.equals(path + SAML_SERVLET)
                || requesturi.equals(path + FLYS_CSS)) {
            log.debug("Request for login " + requesturi);
            if (loginDisabled && requesturi.equals(path + LOGIN_JSP)) {
                log.debug("Login disabled. Redirecting.");
                if (noAuthRedir.equals(LOGIN_JSP)
                    || noAuthRedir.equals(path + LOGIN_JSP)
                ) {
                    handleResponse(resp, false);
                    /* Dont redirect to the same page */
                } else {
                    handleResponse(resp, true);
                }
                return;
            }
            chain.doFilter(req, resp);
            return;
        }

        boolean redirect = false;

        HttpSession session = sreq.getSession();

        String uri = path + "/" + this.redirecturl;

        /* Redirect if uri is root or redirecturl */
        if (requesturi.equals(uri) || requesturi.equals(path + "/")) {
            redirect = true;
        }

        String queryString = sreq.getQueryString();

        if (queryString != null) {
            uri += "?" + queryString;
        }
        session.setAttribute("requesturi", uri);

        User user = (User)session.getAttribute("user");
        if (user == null) {
            log.debug("No user in session: " + requesturi);
            this.handleResponse(resp, redirect);
            return;
        }
        if (user.hasExpired()) {
            // try to re-authenticate the user
            log.debug("User ticket has expired: " + requesturi);
            String encoding = sreq.getCharacterEncoding();
            try {
                Authentication auth = this.auth(user, encoding);
                if (auth == null || !auth.isSuccess()) {
                    log.debug("Re-athentication not successful");
                    this.handleResponse(resp, redirect);
                }
            }
            catch(AuthenticationException e) {
                log.error("Failure during re-authentication", e);
                this.handleResponse(resp, redirect);
                return;
            }
        }

        chain.doFilter(req, resp);
        return;
    }

    private void redirect(ServletResponse resp) throws IOException {
        if (noAuthRedir.startsWith("http")) {
            log.debug("Redirect to external page: " + noAuthRedir);
            ((HttpServletResponse) resp).sendRedirect(noAuthRedir);
        } else {
            log.debug("Redirect to: " + noAuthRedir);
            ((HttpServletResponse) resp).sendRedirect(
                this.sc.getContextPath() + noAuthRedir);
        }
    }

    private void sendNotAuthenticated(ServletResponse resp) throws IOException {
        log.debug("Send not authenticated");
        ((HttpServletResponse)resp).sendError(
            HttpServletResponse.SC_FORBIDDEN, "User not authenticated");
    }

    private void handleResponse(
        ServletResponse resp,
        boolean redirect
    ) throws IOException {
        if (redirect) {
            this.redirect(resp);
        }
        else {
            this.sendNotAuthenticated(resp);
        }
    }


    /**
     * Do nothing at destruction.
     */
    @Override
    public void destroy() {
    }

    private Authentication auth(User user, String encoding)
        throws AuthenticationException, IOException {
        Features features = (Features)sc.getAttribute(
            Features.CONTEXT_ATTRIBUTE);
        return AuthenticationFactory.getInstance(this.authmethod).auth(
                user.getName(), user.getPassword(), encoding, features, sc);
    }

    /**
     * Returns true if the request is from our machine
     * @param req The ServletRequest
     * @return true if the request is from a loopback interface or from one of
     *  the interface addresses of the machine
     */
    private boolean isLocalAddress(ServletRequest req) {
        try {
            InetAddress addr = InetAddress.getByName(req.getRemoteAddr());
            return addr.isAnyLocalAddress() || addr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            log.error(e, e);
            return false;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
