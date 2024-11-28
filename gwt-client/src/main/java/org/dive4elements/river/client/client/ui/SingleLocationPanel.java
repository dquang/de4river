/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.data.Record;

import com.smartgwt.client.widgets.grid.events.CellClickEvent;

import org.dive4elements.river.client.shared.model.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * This UIProvider creates a widget to enter a single location (km).
 */
public class SingleLocationPanel
extends      MultipleLocationPanel
{
    private static final long serialVersionUID = -300641333561787454L;


    /**
     * Creates a new SingleLocationPanel instance.
     */
    public SingleLocationPanel() {
        picker = new LocationPicker(this);
    }


    /** Overridden to restrict to one entered value. */
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

        // We want just one value to be allowed.
        if (values.length > 1) {
            errors.add(MSG.too_many_values());
        }

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
     * This method returns the selected data.
     *
     * @return the selected/inserted data.
     */
    @Override
    public Data[] getData() {
        saveLocationValues(locationPanel);
        double[] values = getLocationValues();
        Data[] data = new Data[values.length+1];

        for (int i = 0; i < values.length; i++) {
            data[i] = createDataArray(getDataItemName(),
                Double.valueOf(values[i]).toString());
        }

        data[values.length] = createDataArray("ld_mode", "locations");

        return data;
    }


    /* This is a copy of super.super.onRecordClick. Straighten out
       this weird family. */
    /**
     * Callback when an item from the input helper was clicked.
     * Set the respective km-value in the location value field.
     * @param e event passed.
     */
    @Override
    public void onCellClick (CellClickEvent e) {
        Record record = e.getRecord();
        double[] selected = new double[1];
        try {
            selected[0] =
                Double.parseDouble(record.getAttribute("from"));
        }
        catch(NumberFormatException nfe) {
            // Is there anything else to do here?
        }
        setLocationValues(selected);
    }


    @Override
    protected String getLabelString() {
        return MSG.single_location();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
