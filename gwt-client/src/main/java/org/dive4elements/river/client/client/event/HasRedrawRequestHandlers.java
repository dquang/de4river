/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;


/**
 * Implements function to add a RedrawRequestHandler.
 */
public interface HasRedrawRequestHandlers {

    /**
     * Adds a new RedrawRequestHandler.
     *
     * @param handler The new RedrawRequestHandler
     */
    public void addRedrawRequestHandler(RedrawRequestHandler rrh);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

