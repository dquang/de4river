/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.Map;


public class MultiDataItem
implements DataItem
{
    /** The label. */
    protected String label;

    /** The description. */
    protected String description;

    /** The value. */
    protected Map<String, String> value;


    public MultiDataItem() {
    }

    /**
     * The default constructor to create new instances.
     *
     * @param label The label.
     * @param description The description.
     * @param value The value.
     */
    public MultiDataItem(
        String label,
        String description,
        Map<String, String> value
    ) {
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
        String v = "";
        for (Map.Entry<String, String> e: value.entrySet()) {
            v += e.getKey() + ":" + e.getValue() + ";";
        }
        return v;
    }

    public Map<String, String> getValue() {
        return value;
    }
}
