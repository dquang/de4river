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
 * A Data object represents the necessary data of a single state of the
 * artifact. It might provide several DataItems or just a single DataItem. The
 * <code>type</code> makes it possible to validate the input in the client.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Data extends Serializable {

    /**
     * Returns the label of the item.
     *
     * @return the label.
     */
    public String getLabel();


    /**
     * Returns the description of the item.
     *
     * @return the description.
     */
    public String getDescription();


    /**
     * Returns the type of the item.
     *
     * @return the type.
     */
    public String getType();


    /**
     * Returns the DataItems provided by this Data object.
     *
     * @return the DataItems.
     */
    public DataItem[] getItems();


    /**
     * Returns the default value of this data object.
     *
     * @return the default value.
     */
    public DataItem getDefault();


    /**
     * Returns the values as colon separated string.
     *
     * @return colon separated string.
     */
    public String getStringValue();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
