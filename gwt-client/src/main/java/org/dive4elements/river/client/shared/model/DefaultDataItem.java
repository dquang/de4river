/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * The default implementation of a {@link DataItem}. This class just implements
 * constructors to create instances and the necessary methods of the interface.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultDataItem implements DataItem {

    /** The label. */
    protected String label;

    /** The description. */
    protected String description;

    /** The value. */
    protected String value;


    public DefaultDataItem() {
    }


    /**
     * The default constructor to create new instances.
     *
     * @param label The label.
     * @param description The description.
     * @param value The value.
     */
    public DefaultDataItem(String label, String description, String value) {
        this.label       = label;
        this.description = description;
        this.value       = value;
    }


    public String getLabel() {
        return label;
    }


    public String getDescription() {
        return description;
    }


    public String getStringValue() {
        return value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
