/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DataList implements Serializable, Cloneable {

    /** The list of Data objects managed by this list. */
    protected List<Data> data;

    /** The name of the state that this list belongs to. */
    protected String state;

    /** The name of a UIProvider that is recommended to render this DataList. */
    protected String uiprovider;

    /** The label that should be used to label data objects. */
    protected String label;

    /** The help text (URL) that should be displayed for this data object. */
    protected String helpText;


    /**
     * The default constructor that creates a new DataList without Data objects
     * and no UIProvider.
     */
    public DataList() {
        data = new ArrayList<Data>();
    }


    /**
     * Constructor.
     *
     * @param state The name of the state that this list belongs to.
     * @param size The initial size of the list.
     */
    public DataList(String state, int size) {
        this.state = state;
        this.data  = new ArrayList<Data>(size);
    }


    /**
     * A constructor that creates a new DataList without Data objects and no
     * UIProvider. Size defines the initial size of the list.
     *
     * @param state The name of the state that this list belongs to.
     * @param size The initial size of the list.
     * @param uiprovider The UIProvider that should be used to render this list.
     */
    public DataList(String state, int size, String uiprovider) {
        this(state, size);
        this.uiprovider = uiprovider;
    }


    /**
     * A constructor that creates a new DataList without Data objects and no
     * UIProvider. Size defines the initial size of the list.
     *
     * @param state The name of the state that this list belongs to.
     * @param size The initial size of the list.
     * @param uiprovider The UIProvider that should be used to render this list.
     * @param label The label.
     */
    public DataList(String state, int size, String uiprovider, String label) {
        this(state, size, uiprovider);
        this.label = label;
    }


    /**
     * A constructor that creates a new DataList without Data objects and no
     * UIProvider. Size defines the initial size of the list.
     *
     * @param state The name of the state that this list belongs to.
     * @param size The initial size of the list.
     * @param uiprovider The UIProvider that should be used to render this list.
     * @param label The label.
     * @param helpText The help text (should be an URL).
     */
    public DataList(
        String state,
        int    size,
        String uiprovider,
        String label,
        String helpText
    ) {
        this(state, size, uiprovider, label);
        this.helpText = helpText;
    }


    /**
     * Adds a new Data object to the list.
     *
     * @param obj The Data object.
     */
    public void add(Data obj) {
        if (obj != null) {
            data.add(obj);
        }
    }


    /**
     * Adds a new Data objects to the list.
     *
     * @param obj The Data object.
     */
    public void add(Data[] obj) {
        if (obj != null) {
            for (Data o: obj) {
                data.add(o);
            }
        }
    }


    /**
     * Returns the Data element at position <i>idx</i>.
     *
     * @param idx The position of an element that should be returned.
     *
     * @return the Data element at position <i>idx</i>.
     */
    public Data get(int idx) {
        if (idx < size()) {
            return data.get(idx);
        }

        return null;
    }


    /**
     * Returns the whole list of Data objects.
     *
     * @return the whole list of Data objects.
     */
    public List<Data> getAll() {
        return data;
    }

    /**
     * Returns the number of Data objects in the list.
     *
     * @param the number of Data objects in the list.
     */
    public int size() {
        return data.size();
    }


    /**
     * Returns the name of the state that this list belongs to.
     *
     * @return the name of the state that this list belongs to.
     */
    public String getState() {
        return state;
    }


    /**
     * Returns the label for this list.
     *
     * @return the label of this list.
     */
    public String getLabel() {
        return label;
    }


    /**
     * Retrieves the name of a UIProvider or null if no one is recommended.
     *
     * @return the name of a UIProvider or null if no one is recommended.
     */
    public String getUIProvider() {
        return uiprovider;
    }


    /**
     * Returns the help text which should be an URL.
     *
     * @return the help text.
     */
    public String getHelpText() {
        return helpText;
    }


    public Object clone() {
        DataList clone = new DataList(
            this.state,
            this.data.size(),
            this.uiprovider,
            this.label,
            this.helpText);
        clone.data = (List<Data>) ((ArrayList<Data>)data).clone();

        return clone;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
