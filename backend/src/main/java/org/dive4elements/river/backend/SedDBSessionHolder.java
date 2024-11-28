/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class SedDBSessionHolder
{
    private static Logger log =
        LogManager.getLogger(SedDBSessionHolder.class);

    public static final ThreadLocal<Session> HOLDER =
        new ThreadLocal<Session>() {
            @Override
            protected Session initialValue() {
                Session session = create();
                log.debug("Initial session value: " + session.hashCode());
                return session;
            }
        };

    public synchronized static Session create() {
        log.debug("create");
        SessionFactory sessionFactory =
            SessionFactoryProvider.getSedDBSessionFactory();
        return sessionFactory.openSession();
    }

    public static Session acquire() {
        Session session = create();
        log.debug("acquired session: " + session.hashCode());
        HOLDER.set(session);
        return session;
    }

    public static void release() {
        Session session = HOLDER.get();
        if (session != null) {
            log.debug("releasing session: " + session.hashCode());
            session.close();
        } else {
            log.error("release() called on NULL session.");
        }
        HOLDER.remove();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
