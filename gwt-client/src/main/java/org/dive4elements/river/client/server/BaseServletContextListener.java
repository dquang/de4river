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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.server.features.Features;
import org.dive4elements.river.client.server.features.XMLFileFeatures;

/**
 * ServletContextListenter to initalize the Features globally for
 * all Servlets
 */
public class BaseServletContextListener implements ServletContextListener {

    private static final Logger log = LogManager.getLogger(
        BaseServletContextListener.class);

    @Override
    public void  contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        File file = new File(sc.getInitParameter("features-file"));
        String path = file.isAbsolute()
            ? file.getPath()
            : sc.getRealPath(file.getPath());

        log.debug("Initializing ServletContext");
        try {
            XMLFileFeatures features = new XMLFileFeatures(path);
            sc.setAttribute(Features.CONTEXT_ATTRIBUTE, features);
        } catch(IOException e) {
            log.error(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //DO NOTHING
    }
}
