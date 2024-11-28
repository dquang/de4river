/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;

import java.util.ArrayList;
import java.util.List;

/**
 * This UIProvider serves as base for UI Providers to enter
 * a single location (km).
 */
public abstract class LocationPanel
extends               AbstractUIProvider
{
    private static final long serialVersionUID = -5306604428440015046L;

    /** A container that will contain the location or the distance panel. */
    protected HLayout inputLayout;

    /** The minimal value that the user is allowed to enter. */
    protected double min;

    /** The maximal value that the user is allowed to enter. */
    protected double max;

    /** The values entered in the location mode. */
    protected double[] values;

    /** Name of the data item that keeps this location(s). */
    protected String dataItemName;

    /** The input panel for locations. */
    protected DoubleArrayPanel locationPanel;


    /**
     * Creates a new LocationDistancePanel instance.
     */
    public LocationPanel() {
        values = new double[0];
    }


    /**
     * This method creates a widget that contains a label, a panel with
     * checkboxes to switch the input mode between location and distance input,
     * and a mode specific panel.
     *
     * @param data The data that might be inserted.
     *
     * @return a panel.
     */
    @Override
    public Canvas create(DataList data) {
        findDataItemName(data);

        VLayout layout = new VLayout();
        layout.setMembersMargin(10);

        // Subclass uses translated data items name as label.
        Label label   = new Label(MSG.location());
        Canvas widget = createWidget(data);
        Canvas submit = getNextButton();

        initDefaults(data);

        widget.setHeight(50);
        label.setHeight(25);

        layout.addMember(label);
        layout.addMember(widget);
        layout.addMember(submit);

        return layout;
    }


    /** Store label of first data item in list. */
    public void findDataItemName(DataList list) {
        this.dataItemName = list.getAll().get(0).getLabel();
    }


    /** Get label of first data item that this uiprovider has seen. */
    public String getDataItemName() {
        return this.dataItemName;
    }


    /**
     * This method creates a Canvas element showing the old Data objects in the
     * DataList <i>data</i>.
     */
    @Override
    public Canvas createOld(DataList dataList) {
        findDataItemName(dataList);

        List<Data> items = dataList.getAll();
        Data dLocation   = getData(items, getDataItemName());
        DataItem[] loc   = dLocation.getItems();

        HLayout layout = new HLayout();
        layout.setWidth("400px");

        Label label = new Label(dataList.getLabel());
        label.setWidth("200px");

        Canvas back = getBackButton(dataList.getState());

        // TODO evaluate: isn't this what findDataItemName is doing?
        Label selected = new Label(loc[0].getLabel());
        selected.setWidth("130px");

        layout.addMember(label);
        layout.addMember(selected);
        layout.addMember(back);

        return layout;
    }


    /**
     * This method reads the default values defined in the DataItems of the Data
     * objects in <i>list</i>.
     *
     * @param list The DataList container that stores the Data objects.
     */
    protected void initDefaults(DataList list) {
        Data data = list.get(0);

        DataItem[] items = data.getItems();
        DataItem iMin = getDataItem(items, "min");
        DataItem iMax = getDataItem(items, "max");

        try {
            min = Double.parseDouble(iMin.getStringValue());
            max = Double.parseDouble(iMax.getStringValue());
        }
        catch (NumberFormatException nfe) {
            SC.warn(MSG.error_read_minmax_values());
            min = -Double.MAX_VALUE;
            max = Double.MAX_VALUE;
        }

        DataItem def   = data.getDefault();
        String   value = def.getStringValue();

        try {
            double d = Double.parseDouble(value);
            setLocationValues(new double[] { d } );
        }
        catch (NumberFormatException nfe) {
            // could not parse, don't know what else to do
            GWT.log("LocationPanel", nfe);
        }
    }


    /**
     * This method grabs the Data with name <i>name</i> from the list and
     * returns it.
     *
     * @param items A list of Data.
     * @param name The name of the Data that we are searching for.
     *
     * @return the Data with the name <i>name</i>.
     */
    @Override
    protected Data getData(List<Data> data, String name) {
        for (Data d: data) {
            if (name.equals(d.getLabel())) {
                return d;
            }
        }

        return null;
    }


    protected Canvas createWidget(DataList data) {
        VLayout layout = new VLayout();
        inputLayout    = new HLayout();

        // The initial view will display the location input mode.
        locationPanel = new DoubleArrayPanel(
            MSG.unitLocation(),
            getLocationValues(),
            new BlurHandler(){@Override
            public void onBlur(BlurEvent be) {}});

        // TODO Remove picker references, refactor such that subclasses can
        // easily use their picker if they want.
        //picker.getLocationTable().setAutoFetchData(true);

        inputLayout.addMember(locationPanel);

        layout.addMember(inputLayout);

        inputLayout.setMembersMargin(30);

        /*
        //picker.prepareFilter();
        helperContainer.addMember(picker.getLocationTable());
        helperContainer.addMember(picker.getFilterLayout());
        helperContainer.addMember(picker.getResultCountForm());
        */
        return layout;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        saveLocationValues(locationPanel);

        if (!locationPanel.validateForm()) {
            errors.add(MSG.wrongFormat());
            return errors;
        }

        double[] values = getLocationValues();
        double[] good   = new double[values.length];
        int      idx    = 0;

        for (double value: values) {
            if (value < min || value > max) {
                String tmp = MSG.error_validate_range();
                tmp = tmp.replace("$1", nf.format(value));
                tmp = tmp.replace("$2", nf.format(min));
                tmp = tmp.replace("$3", nf.format(max));
                errors.add(tmp);
            }
            else {
                good[idx++] = value;
            }
        }

        double[] justGood = new double[idx];
        for (int i = 0; i < justGood.length; i++) {
            justGood[i] = good[i];
        }

        if (!errors.isEmpty()) {
            locationPanel.setValues(justGood);
        }

        return errors;
    }


    /**
     * Validates and stores all values entered in the location mode.
     *
     * @param p The DoubleArrayPanel.
     */
    protected void saveLocationValues(DoubleArrayPanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            if (item.getFieldName().equals(DoubleArrayPanel.FIELD_NAME)) {
                saveLocationValue(p, item);
            }
        }
    }


    /**
     * Validates and stores a value entered in the location mode.
     *
     * @param p The DoubleArrayPanel.
     * @param item The item that needs to be validated.
     */
    protected void saveLocationValue(DoubleArrayPanel p, FormItem item) {
        if (p.validateForm(item)) {
            setLocationValues(p.getInputValues(item));
        }
    }


    /** Get the location values. */
    protected double[] getLocationValues() {
        return values;
    }


    /** Sets Location values and updates the panel. */
    protected void setLocationValues(double[] values) {
        this.values = values;
        locationPanel.setValues(values);
    }


    /**
     * Callback when an item from the input helper was clicked.
     * Set the respective km-value in the location value field.
     * @param e event passed.
     */
    public void onRecordClick (RecordClickEvent e) {
        Record record = e.getRecord();
        double[] selected = new double[1];
        try {
            selected[0] =
                Double.parseDouble(record.getAttribute("from"));
        }
        catch(NumberFormatException nfe) {
            GWT.log("onRecordClick", nfe);
        }
        setLocationValues(selected);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
