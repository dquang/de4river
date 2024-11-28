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
import org.dive4elements.river.model.Wst;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WstFactory {

    public static final int DEFAULT_KIND = 0;

    /** We don't need to instantiate concrete objects of this class. */
    private WstFactory() {
    }


    /**
     * Returns the Wst object for a given river.
     *
     * @param river The river.
     *
     * @return the Wst of <i>river</i>.
     */
    public static Wst getWst(River river) {
        return getWst(river, DEFAULT_KIND);
    }

    public static Wst getWst(River river, int kind) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Wst where river=:river and kind = :kind");
        query.setParameter("river", river);
        query.setInteger("kind", kind);

        List<Wst> wsts = query.list();

        return wsts.isEmpty() ? null : wsts.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
