/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;

import org.dive4elements.river.client.shared.model.Collection;


/**
 * This events stores references to the old collection and the new one.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CollectionChangeEvent {

    protected Collection oldCollection;
    protected Collection newCollection;

    public CollectionChangeEvent(Collection old, Collection newArt) {
        oldCollection = old;
        newCollection = newArt;
    }

    public Collection getOldValue() {
        return oldCollection;
    }

    public Collection getNewValue() {
        return newCollection;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
