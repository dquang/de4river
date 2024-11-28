/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * Long Range (e.g. storing dates).
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class LongRangeData implements RangeData {

    public static final String TYPE = "longrange";

    protected String label;
    protected String description;

    protected long lower;
    protected long upper;

    protected Long defLower;
    protected Long defUpper;


    public LongRangeData() {
    }


    public LongRangeData(String label, String desc, long lower, long upper) {
        this(label, desc, lower, upper, null, null);
    }


    /**
     * @param label
     * @param desc
     * @param lower
     * @param upper
     * @param defLower
     * @param defUpper
     */
    public LongRangeData(
        String  label,
        String  desc,
        long    lower,
        long    upper,
        Long    defLower,
        Long    defUpper
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
        return "longrange";
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
