/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


public class DoubleRangeData implements RangeData {

    public static final String TYPE = "doublerange";


    protected String label;
    protected String description;

    protected double lower;
    protected double upper;

    protected Double defLower;
    protected Double defUpper;


    public DoubleRangeData() {
    }


    public DoubleRangeData(
        String label,
        String desc,
        double lower,
        double upper
    ) {
        this(label, desc, lower, upper, null, null);
    }


    public DoubleRangeData(
        String  label,
        String  desc,
        double     lower,
        double     upper,
        Double defLower,
        Double defUpper
    ) {
        this.label       = label;
        this.description = desc;
        this.lower       = lower;
        this.upper       = upper;
        this.defLower    = defLower;
        this.defUpper    = defUpper;
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
        return "doublerange";
    }


    /**
     * Returns a DataItem which value is a string that consists of the min and
     * max value separated by a ';'.
     *
     * @return the DataItem.
     */
    public DataItem[] getItems() {
        String theMin = String.valueOf(lower);
        String theMax = String.valueOf(upper);

        String label = theMin + " - " + theMax;
        String value = theMin + ";" + theMax;

        DataItem item  = new DefaultDataItem(label, label, value);

        return new DataItem[] { item };
    }


    /**
     * @return always null.
     */
    public DataItem getDefault() {
        return null;
    }


    public Object getLower() {
        return lower;
    }


    public Object getUpper() {
        return upper;
    }


    public Object getDefaultLower() {
        return defLower;
    }


    public Object getDefaultUpper() {
        return defUpper;
    }


    /**
     * Returns the values as colon separated string.
     *
     * @return colon separated string.
     */
    public String getStringValue() {
        String data = lower + ";" + upper;
        return data;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
