/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


public class StringOptionsData implements Data {

    public static final String TYPE = "options";

    protected String label;
    protected String description;

    public DataItem[] opts;


    public StringOptionsData() {
    }


    public StringOptionsData(String label, String desc, DataItem[] opts) {
        this.label       = label;
        this.description = desc;
        this.opts        = opts;
    }


    /**
     * Returns the label of the item.
     *
     * @return the label.
     */
    public String getLabel() {
        return label;
    }


    /**
     * Returns the description of the item.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Returns the type of the item.
     *
     * @return the type.
     */
    public String getType() {
        return TYPE;
    }


    /**
     * Returns the data items which represent the allowed options for this Data.
     *
     * @return the allowed options as DataItem array.
     */
    public DataItem[] getItems() {
        return opts;
    }


    /**
     * @return always null.
     */
    public DataItem getDefault() {
        return null;
    }


    /**
     * Returns the values as colon separated string.
     *
     * @return colon separated string.
     */
    public String getStringValue() {
        String data = "";
        boolean first = true;
        for (int i = 0; i < opts.length; i++) {
            if (!first) {
                data += ";";
            }
            data += opts[i].getStringValue();
            first = false;
        }
        return data;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
