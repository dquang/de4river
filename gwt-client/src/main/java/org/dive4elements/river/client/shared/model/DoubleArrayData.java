/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DoubleArrayData implements Data {

    public static final String TYPE = "doublearray";


    protected String label;
    protected String description;

    protected double[] values;


    public DoubleArrayData() {
    }


    public DoubleArrayData(String label, String description, double[] values) {
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
        return TYPE;
    }


    /**
     * Returns a DataItem which value is a string that consists of the double
     * values separated by a ';'.
     *
     * @return the DataItem.
     */
    public DataItem[] getItems() {
        if (values == null || values.length == 0) {
            return new DataItem[0];
        }

        StringBuilder sb    = new StringBuilder();
        boolean       first = true;

        for (double value: values) {
            if (first) {
                sb.append(String.valueOf(value));
            }
            else {
                sb.append(";" + String.valueOf(value));
            }
        }

        String  value = sb.toString();
        DataItem item = new DefaultDataItem(value, value, value);

        return new DataItem[] { item };
    }


    /**
     * Returns the values as array.
     *
     * @return the values as array.
     */
    public double[] getValues() {
        return values;
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
        for (int i = 0; i < values.length; i++) {
            if (!first) {
                data += ";";
            }
            data += String.valueOf(values[i]);
            first = false;
        }
        return data;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
