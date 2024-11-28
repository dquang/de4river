/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** ServletFilter to avoid caching for GWTs *.nocache.* files. */
public class NoCacheFilter implements Filter {

    private static final long DAY = 86400000L;

    private static final String NO_CACHE = ".nocache.";

    private static Logger log = LogManager.getLogger(NoCacheFilter.class);

    /**
     * Initialize.
     */
    @Override
    public void init(FilterConfig config)
    throws ServletException
    {
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
        HttpServletRequest httpreq = (HttpServletRequest)req;
        String uri = httpreq.getRequestURI();

        if (uri.contains(NO_CACHE)) {
            log.debug("Set no-cache for " + uri);

            Date now = new Date();
            HttpServletResponse httpresp = (HttpServletResponse)resp;
            httpresp.setDateHeader("Date", now.getTime());
            httpresp.setDateHeader("Expires", now.getTime() - DAY);
            httpresp.setHeader("Pragma", "no-cache");
            httpresp.setHeader("Cache-control",
                    "no-cache, no-store, must-revalidate");
        }

        chain.doFilter(req, resp);
    }


    /**
     * Do nothing at destruction.
     */
    @Override
    public void destroy() {
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
