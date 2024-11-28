/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;

import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.River;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RiverFactory {

    /** We don't need to instantiate concrete objects of this class. */
    private RiverFactory() {
    }


    /**
     * Returns all rivers that were found in the backend.
     *
     * @return all rivers.
     */
    public static List<River> getRivers() {
        Session session = SessionHolder.HOLDER.get();

        return session.createQuery("from River order by name").list();
    }


    /**
     * Returns a River object fetched from database based on its id.
     *
     * @param river_id The id of the desired river.
     *
     * @return the river.
     */
    public static River getRiver(int river_id) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery("from River where id=:river_id");
        query.setParameter("river_id", river_id);

        List<River> rivers = query.list();

        return rivers.isEmpty() ? null : rivers.get(0);
    }


    /**
     * Returns a River object fetched from database based on its name.
     *
     * @param river The name of a river.
     *
     * @return the River object.
     */
    public static River getRiver(String river) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from River where name =:name");
        query.setParameter("name", river);

        List<River> rivers = query.list();

        return rivers.isEmpty() ? null : rivers.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
