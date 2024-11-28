/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface HasCollectionChangeHandlers {

    /**
     * Adds a new CollectionChangeHandler.
     *
     * @param handler The new CollectionChangeHandler.
     */
    public void addCollectionChangeHandler(CollectionChangeHandler handler);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
