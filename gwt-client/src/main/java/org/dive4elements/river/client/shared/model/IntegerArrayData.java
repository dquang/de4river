/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


public class IntegerArrayData implements Data {

    public static final String TYPE = "intarray";


    protected String label;
    protected String description;

    protected IntDataItem[] values;


    public IntegerArrayData() {
    }


    public IntegerArrayData(
        String label,
        String description,
        IntDataItem[] values
    ) {
        this.label       = label;
        this.description = description;
        this.values      = values;
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
        return "intarray";
    }


    /**
     * Returns a DataItem which value is a string that consists of the integer
     * values separated by a ';'.
     *
     * @return the DataItem.
     */
    public DataItem[] getItems() {
        return values;
    }


    /**
     * Returns the values as array.
     *
     * @return the values as array.
     */
    public int[] getValues() {
        int[] data = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            data[i] = values[i].getValue();
        }
        return data;
    }


    /**
     * Returns the values as colon separated string.
     *
     * @return colon separated string.
     */
    public String getStringValue() {
        String data = "";
        boolean first = true;
        for (int i = 0; i < values.length; i++) {
            if (!first) {
                data += ";";
            }
            data += values[i].getStringValue();
            first = false;
        }
        return data;
    }

    /**
     * @return always null.
     */
    public DataItem getDefault() {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
