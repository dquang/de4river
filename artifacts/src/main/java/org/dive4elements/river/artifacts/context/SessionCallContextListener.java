/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.context;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.hibernate.Session;

import org.dive4elements.river.backend.SessionHolder;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallContext.Listener;


/**
 * This CallContextListener is used to initialize a ThreadLocal variable in
 * each CallContext (for each request) that holds Sessions.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SessionCallContextListener implements Listener {

    public static final String SESSION_KEY = "context.session";

    /** The log that is used in this class.*/
    private static Logger log =
        LogManager.getLogger(SessionCallContextListener.class);


    public SessionCallContextListener() {
    }


    public void setup(Document config, Node listenerConfig) {
        // nothing to do here
    }


    /**
     * Initializes a ThreadLocal variable that is used to hold sessions.
     *
     * @param context The CallContext.
     */
    public void init(CallContext context) {
        log.debug("SessionCallContextListener.init");

        Session session = SessionHolder.acquire();

        context.putContextValue(SESSION_KEY, session);
    }


    /**
     * Closes open sessions of the ThreadLocal variable opened in init().
     *
     * @param context The CallContext.
     */
    public void close(CallContext context) {
        log.debug("SessionCallContextListener.close");

        SessionHolder.release();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
