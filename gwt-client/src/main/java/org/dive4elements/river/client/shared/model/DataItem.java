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
 * A DataItem represents a concrete item that might be selected, chosen or
 * inserted by the user.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface DataItem extends Serializable {

    /**
     * Returns the label of the item.
     *
     * @return the label.
     */
    public String getLabel();


    /**
     * Returns the description of the item.
     *
     * @return the description;
     */
    public String getDescription();


    /**
     * Returns the value of the item.
     *
     * @return the value.
     */
    public String getStringValue();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
