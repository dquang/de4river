/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * The integer implementation of a {@link DataItem}.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class IntDataItem implements DataItem {

    /** The label. */
    protected String label;

    /** The description. */
    protected String description;

    /** The value. */
    protected int value;


    public IntDataItem() {
    }


    /**
     * The default constructor to create new instances.
     *
     * @param label The label.
     * @param description The description.
     * @param value The value.
     */
    public IntDataItem(String label, String description, int value) {
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
        return String.valueOf(value);
    }

    public int getValue() {
        return value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
