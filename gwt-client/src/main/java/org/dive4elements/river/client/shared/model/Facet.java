/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


/**
 * The interface that provides methods to retrieve information about a Facet.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Facet extends Serializable {

    /**
     * Returns the name of a facet.
     *
     * @return the name of a facet.
     */
    String getName();

    /**
     * Returns the index of this facet.
     *
     * @return the index.
     */
    int getIndex();

    /**
     * Returns the description of this facet.
     *
     * @return the description.
     */
    String getDescription();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
