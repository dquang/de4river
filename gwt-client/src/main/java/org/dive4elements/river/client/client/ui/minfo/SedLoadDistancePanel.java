/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.minfo;

import java.util.List;

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
import org.dive4elements.river.client.client.ui.AbstractUIProvider;
import org.dive4elements.river.client.client.ui.DoubleRangeOnlyPanel;
import org.dive4elements.river.client.client.ui.DoubleRangePanel;
import org.dive4elements.river.client.client.ui.LocationPicker;
import org.dive4elements.river.client.client.ui.range.DistanceInfoDataSource;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;


public class SedLoadDistancePanel
extends AbstractUIProvider
implements BlurHandler, CellClickHandler
{
    public static final String FIELD_LOWER = "ld_from";
    public static final String FIELD_UPPER = "ld_to";

    protected DoubleRangePanel distancePanel;
    protected double min;
    protected double max;
    protected LocationPicker picker;

    @Override
    public Canvas createOld(DataList dataList) {
        String s = getOldSelectionString(dataList);
        String l = dataList.getLabel();

        Label label    = new Label(l);
        Label selected = new Label(s);

        HLayout layout = new HLayout();

        layout.setWidth(400);
        label.setWidth(200);
        selected.setWidth(130);

        layout.addMember(label);
        layout.addMember(selected);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }

    protected String getOldSelectionString(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data dFrom = getData(items, FIELD_LOWER);
        Data dTo   = getData(items, FIELD_UPPER);

        DataItem[] from = dFrom.getItems();
        DataItem[] to   = dTo.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append(from[0].getLabel());
        sb.append(" " + MSG.dpUnitFrom() + " - ");
        sb.append(to[0].getLabel());
        sb.append(" " + MSG.dpUnitTo());

        return sb.toString();
    }

    @Override
    public Canvas create(DataList data) {
        picker = new LocationPicker(this);
        distancePanel = new DoubleRangeOnlyPanel(
            MSG.dpUnitFrom() + " - ",
            MSG.dpUnitTo(), 0d, 0d, 250, this, "right");
        VLayout layout = new VLayout();
        layout.setMembersMargin(10);

        Label label = new Label(MSG.distance_state());

        Canvas submit = getNextButton();

        label.setHeight(25);
        distancePanel.setHeight(50);

        layout.addMember(label);
        layout.addMember(distancePanel);
        layout.addMember(submit);

        initMinMaxValues(data);
        initDefaultValues(data);


        picker.setIsDistance(true);
        picker.getLocationTable().setAutoFetchData(true);
        picker.prepareFilter();

        helperContainer.addMember(picker.getLocationTable());
        helperContainer.addMember(picker.getFilterLayout());
        helperContainer.addMember(picker.getResultCountForm());

        setPickerDataSource();
        picker.createLocationTable();

        return layout;
    }

    protected void initMinMaxValues(DataList data) {
        Data f = getData(data.getAll(), FIELD_LOWER);
        Data t = getData(data.getAll(), FIELD_UPPER);

        DataItem[] fItems = f.getItems();
        DataItem[] tItems = t.getItems();

        try {
            min = Double.valueOf(fItems[0].getStringValue());
            max = Double.valueOf(tItems[0].getStringValue());
        }
        catch (NumberFormatException nfe) {
            min = -Double.MAX_VALUE;
            max =  Double.MAX_VALUE;
        }
    }

    protected void initDefaultValues(DataList data) {
        initDefaultFrom(data);
        initDefaultTo(data);
    }

    protected void initDefaultFrom(DataList data) {
        Data f = getData(data.getAll(), FIELD_LOWER);

        double from = getDefaultFrom();

        try {
            from = getDefaultValue(f);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        distancePanel.setFrom(from);
    }


    protected double getDefaultFrom() {
        return min;
    }


    protected void initDefaultTo(DataList data) {
        Data t = getData(data.getAll(), FIELD_UPPER);

        double to = getDefaultTo();

        try {
            to = getDefaultValue(t);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        distancePanel.setTo(to);
    }


    protected double getDefaultTo() {
        return max;
    }

    protected double getDefaultValue(Data data)
    throws NumberFormatException
    {
        DataItem def      = data.getDefault();
        String   defValue = def != null ? def.getStringValue() : null;

        return Double.valueOf(defValue);
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

    @Override
    protected Data[] getData() {
        Data[] data = new Data[2];

        data[0] = getDataFrom();
        data[1] = getDataTo();

        return data;
    }

    protected Data getDataFrom() {
        String value = String.valueOf(distancePanel.getFrom());
        String field = FIELD_LOWER;

        DataItem item = new DefaultDataItem(field, field, value);
        return new DefaultData(
            field, null, null, new DataItem[] { item });
    }

    protected Data getDataTo() {
        String value = String.valueOf(distancePanel.getTo());
        String field = FIELD_UPPER;

        DataItem item = new DefaultDataItem(field, field, value);
        return new DefaultData(
            field, null, null, new DataItem[] { item });
    }

    @Override
    public void onBlur(BlurEvent event) {
        distancePanel.validateForm();
    }

    @Override
    public void onCellClick(CellClickEvent e) {
        Record record = e.getRecord();
        int ndx = e.getColNum();
        String from   = record.getAttribute("from");
        try {
            double value = Double.valueOf(from);
            switch (ndx) {
                case 0: distancePanel.setFrom(value); break;
                case 1: distancePanel.setTo(value); break;
            }
        }
        catch(NumberFormatException nfe) {
            SC.warn(MSG.wrongFormat());
        }
    }
}
