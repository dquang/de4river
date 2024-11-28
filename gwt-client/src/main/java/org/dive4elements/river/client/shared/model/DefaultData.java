/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

/**
 * The default implementation of a {@link Data} item. This class just implements
 * constructors to create instances and the necessary methods of the interface.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultData implements Data {

    /** The label of this Data object. */
    protected String label;

    /** The description. */
    protected String description;

    /** The type. */
    protected String type;

    /** The DataItems. */
    protected DataItem[] items;

    /** The default DataItem. */
    protected DataItem defaultItem;


    public DefaultData() {
    }


    /**
     * The default constructor to create new DefaultData objects.
     *
     * @param label The label.
     * @param description The description.
     * @param type The type.
     * @param items The DataItems.
     */
    public DefaultData(
        String label,
        String description,
        String type,
        DataItem[] items)
    {
        this(label, description, type, items, null);
    }


    /**
     * The constructor to create new DefaultData objects with a default value.
     *
     * @param label The label.
     * @param description The description.
     * @param type The type.
     * @param items The DataItems.
     * @param defaultItem The default DataItem.
     */
    public DefaultData(
        String label,
        String description,
        String type,
        DataItem[] items,
        DataItem defaultItem)
    {
        this.label       = label;
        this.description = description;
        this.type        = type;
        this.items       = items;
        this.defaultItem = defaultItem;
    }


    public String getLabel() {
        return label;
    }


    public String getDescription() {
        return description;
    }


    public String getType() {
        return type;
    }


    public DataItem[] getItems() {
        return items;
    }


    public DataItem getDefault() {
        return defaultItem;
    }


    /** Conveniently create simplistic data. */
    public static DefaultData createSimpleStringData(
        String name,
        String value
    ) {
        DefaultDataItem d = new DefaultDataItem(name, name, value);
        return new DefaultData(name, null, null, new DataItem[] {d});
    }

    /** Conveniently create simplistic data array. */
    public static Data[] createSimpleStringDataArray(
        String name,
        String value
    ) {
        DefaultDataItem d = new DefaultDataItem(name, name, value);
        return new Data[]
            { new DefaultData(name, null, null, new DataItem[] {d})};
    }

    /**
     * Returns the values as colon separated string.
     *
     * @return colon separated string.
     */
    public String getStringValue() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < items.length; i++) {
            if (!first) {
                sb.append(';');
            } else {
                first = false;
            }
            sb.append(items[i].getStringValue());
        }
        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
