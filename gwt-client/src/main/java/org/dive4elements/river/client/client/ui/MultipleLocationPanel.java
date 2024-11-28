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
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.services.DistanceInfoService;
import org.dive4elements.river.client.client.services.DistanceInfoServiceAsync;
import org.dive4elements.river.client.client.ui.range.DistanceInfoDataSource;
import org.dive4elements.river.client.shared.DoubleUtils;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DistanceInfoObject;
import org.dive4elements.river.client.shared.model.RangeData;

import java.util.ArrayList;
import java.util.List;


/**
 * This UIProvider creates a widget to enter a single location (km).
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class MultipleLocationPanel
extends      LocationPanel
implements   CellClickHandler
{
    private static final long serialVersionUID = -3359966826794082718L;

    /** The DistanceInfoService used to retrieve locations about rivers. */
    protected DistanceInfoServiceAsync distanceInfoService =
        GWT.create(DistanceInfoService.class);

    /** The table data. */
    protected DistanceInfoObject[] tableData;

    /** The input helper (usually right side, table to click on, values are
     * then entered in the texfield. */
    protected LocationPicker picker;


    /**
     * Creates a new LocationDistancePanel instance.
     */
    public MultipleLocationPanel() {
        picker = new LocationPicker(this);
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

        // Take translated data item name as label, if translation available.
        String labelString;
        try {
            labelString = MSG.getString(getDataItemName());
        }
        catch(java.util.MissingResourceException mre) {
            GWT.log("Cannot find translation for data item name : "
                + getDataItemName());
            labelString = getLabelString();
        }
        Label label   = new Label(labelString);
        Canvas widget = createWidget(data);
        Canvas submit = getNextButton();

        initDefaults(data);

        picker.createLocationTable();

        widget.setHeight(50);
        label.setHeight(25);

        layout.addMember(label);
        layout.addMember(widget);
        layout.addMember(submit);

        return layout;
    }


    /**
     * This method reads the default values defined in the DataItems of the Data
     * objects in <i>list</i>.
     *
     * @param list The DataList container that stores the Data objects.
     */
    @Override
    protected void initDefaults(DataList list) {
        Data data = list.get(0);

        // Compatibility with MinMax- DataItems:
        RangeData rangeData = null;

        for (int i = 0, n = list.size(); i < n; i++) {
            Data tmp = list.get(i);

            if (tmp instanceof RangeData) {
                rangeData = (RangeData) tmp;
            }
        }

        if (rangeData != null) {
            min = Double.parseDouble(rangeData.getDefaultLower().toString());
            max = Double.parseDouble(rangeData.getDefaultUpper().toString());
            // catch ..?
        }
        else {
            DataItem[] items = data.getItems();
            DataItem   iMin  = getDataItem(items, "min");
            DataItem   iMax  = getDataItem(items, "max");

            try {
                min = Double.parseDouble(iMin.getStringValue());
                max = Double.parseDouble(iMax.getStringValue());
            }
            catch (NumberFormatException nfe) {
                SC.warn(MSG.error_read_minmax_values());
                min = -Double.MAX_VALUE;
                max = Double.MAX_VALUE;
            }
        }

        DataItem def = data.getDefault();
        if (def != null) {
            String value = def.getStringValue();

            try {
                double d = Double.parseDouble(value);
                setLocationValues(new double[] { d } );
            }
            catch (NumberFormatException nfe) {
                // could not parse, dont know what to do else
            }
        }
    }


    @Override
    protected Canvas createWidget(DataList data) {
        VLayout layout = new VLayout();
        inputLayout    = new HLayout();

        // The initial view will display the location input mode.
        locationPanel = new DoubleArrayPanel(
            MSG.unitLocation(),
            getLocationValues(),
            new BlurHandler(){@Override
            public void onBlur(BlurEvent be) {validate();}});

        picker.getLocationTable().setAutoFetchData(true);

        inputLayout.addMember(locationPanel);

        layout.addMember(inputLayout);

        inputLayout.setMembersMargin(30);

        picker.prepareFilter();

        helperContainer.addMember(picker.getLocationTable());
        helperContainer.addMember(picker.getFilterLayout());
        helperContainer.addMember(picker.getResultCountForm());
        setPickerDataSource();
        return layout;
    }


    /** Overridden to restrict to one entered value. */
    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        DataList[] ref = artifact.getArtifactDescription().getOldData();
        String mode = ref[1].get(0).getStringValue();

        saveLocationValues(locationPanel);

        if (!locationPanel.validateForm()) {
            errors.add(MSG.wrongFormat());
            return errors;
        }

        double[] lValues = getLocationValues();
        double[] good   = new double[lValues.length];
        int      idx    = 0;

        double reference =
            Double.valueOf(ref[2].get(0).getStringValue()).doubleValue();
        for (double value: lValues) {
            if (mode.equals("calc.reference.curve") &&
                value == reference) {
                errors.add(MSG.error_contains_same_location());
                return errors;
            }
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
     * This method returns the selected data (to feed).
     *
     * @return the selected/inserted data in feedable form.
     */
    @Override
    public Data[] getData() {
        saveLocationValues(locationPanel);
        double[] lValues     = getLocationValues();
        Data[]   data        = new Data[2];
        boolean  first       = true;
        String   valueString = "";

        for (int i = 0; i < lValues.length; i++) {
            if (!first) valueString += " ";
            else first = false;
            valueString += Double.valueOf(lValues[i]).toString();
        }

        data[0] = createDataArray(getDataItemName(), valueString);

        data[1] = createDataArray("ld_mode", "locations");

        return data;
    }


    /** Hook service to the listgrid with possible input values. */
    protected void setPickerDataSource() {
        Config config = Config.getInstance();
        String url    = config.getServerUrl();
        String river  = "";

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        // Try to find a "river" data item to set the source for the
        // list grid.
        String dataFilter = "locations";
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                DataList dl = data[i];
                if (dl.getState().equals("state.minfo.river")) {
                    dataFilter = "measuringpoint";
                }
                if (dl.getState().equals("state.winfo.river") ||
                    dl.getState().equals("state.chart.river") ||
                    dl.getState().equals("state.minfo.river")) {
                    for (int j = 0; j < dl.size(); j++) {
                        Data d = dl.get(j);
                        DataItem[] di = d.getItems();
                        if (di != null && di.length == 1) {
                           river = d.getItems()[0].getStringValue();
                           break;
                        }
                    }
                }
            }
        }

        picker.getLocationTable().setDataSource(new DistanceInfoDataSource(
            url, river, dataFilter));
    }


    // TODO allow multiple selections here or in LocationPanel
    /**
     * Callback when an item from the input helper was clicked.
     * Set the respective km-value in the location value field.
     * @param e event passed.
     */
    @Override
    public void onCellClick (CellClickEvent e) {
        Record record     = e.getRecord();
        double[] old      = getLocationValues();
        double[] selected = DoubleUtils.copyOf(old, old.length + 1);
        try {
            selected[old.length] =
                Double.parseDouble(record.getAttribute("from"));
        }
        catch(NumberFormatException nfe) {
            // Is there anything else to do here?
            GWT.log(nfe.getMessage());
        }

        // compare reference location and target location.
        DataList[] ref = artifact.getArtifactDescription().getOldData();
        String mode = ref[1].get(0).getStringValue();
        if (mode.equals("calc.reference.curve") &&
            ref[2].get(0).getStringValue().equals(record.getAttribute("from")))
        {
            SC.warn(MSG.error_same_location());
            return;
        }

        setLocationValues(selected);
    }


    /**
     * Returns the label string for the input panel.
     */
    protected String getLabelString() {
        return MSG.location();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
