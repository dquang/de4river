/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.fixation;

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.ui.DoubleRangePanel;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
/**
 * This UIProvider creates a panel for location or distance input.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixLocationPanel
extends      FixationPanel
implements   BlurHandler
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    /** The constant name of the input field to enter locations.*/
    public static final String FIELD_VALUE_LOCATION = "location";

    /** The constant name of the input field to enter distance.*/
    public static final String FIELD_VALUE_DISTANCE = "distance";

    DoubleRangePanel inputPanel;

    double from;
    double to;
    double step;

    public FixLocationPanel() {
        htmlOverview = "";
    }

    public Canvas createWidget(DataList data) {
        instances.put(this.artifact.getUuid(), this);

        VLayout layout = new VLayout();

        Canvas title = new Label(MESSAGES.distance());
        title.setHeight("25px");

        inputPanel = new DoubleRangePanel(
                MESSAGES.unitFrom(),
                MESSAGES.unitTo(),
                MESSAGES.unitWidth(),
                0d,
                0d,
                0d,
                240,
                this);

        layout.addMember(title);
        layout.addMember(inputPanel);
        return layout;
    }

    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data       f      = getData(items, "ld_from");
        Data       t      = getData(items, "ld_to");
        Data       s      = getData(items, "ld_step");
        DataItem[] fItems = f.getItems();
        DataItem[] tItems = t.getItems();
        DataItem[] sItems = s.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append(fItems[0].getLabel());
        sb.append(" " + MESSAGES.unitFrom() + " ");
        sb.append(tItems[0].getLabel());
        sb.append(" " + MESSAGES.unitTo() + " ");
        sb.append(sItems[0].getLabel());
        sb.append(" " + MESSAGES.unitWidth());

        Label old = new Label(sb.toString());
        old.setWidth(130);

        HLayout layout = new HLayout();
        layout.setWidth("400px");

        Label   label  = new Label(dataList.getLabel());
        label.setWidth("200px");

        Canvas back = getBackButton(dataList.getState());

        layout.addMember(label);
        layout.addMember(old);
        layout.addMember(back);

        return layout;
    }


    /**
     * This method returns the selected data.
     *
     * @return the selected/inserted data.
     */
    public Data[] getData() {
        List<Data> data = new ArrayList<Data>();

        boolean valid = saveRangeValues(inputPanel);
        if (valid) {
            String f = Double.valueOf(this.from).toString();
            String t = Double.valueOf(this.to).toString();
            String s = Double.valueOf(this.step).toString();
            DataItem fi = new DefaultDataItem("ld_from", "ld_from", f);
            DataItem ti = new DefaultDataItem("ld_to", "ld_to", t);
            DataItem si = new DefaultDataItem("ld_step", "ld_step", s);
            data.add(new DefaultData(
                    "ld_from", null, null, new DataItem[]{ fi }));
            data.add(new DefaultData(
                    "ld_to", null, null, new DataItem[]{ ti }));
            data.add(new DefaultData(
                    "ld_step", null, null, new DataItem[]{ si }));
        }
        // what else?
        return data.toArray(new Data[data.size()]);
    }


    protected boolean saveRangeValues(DoubleRangePanel p) {
        FormItem[] items = p.getFields();
        boolean valid = p.validateForm();

        if(valid) {
            this.from = p.getFrom();
            this.to = p.getTo();
            this.step = p.getStep();
        }
        return valid;
    }


    @Override
    public void setValues(String cid, boolean checked) {
        // No user interaction, do nothing.
    }


    @Override
    public boolean renderCheckboxes() {
        // No selection, return false.
        return false;
    }


    public void success() {
        inputPanel.setValues(fixInfo.getFrom(), fixInfo.getTo(), 100d);
    }

    /**
     * This method is used to validate the inserted data in the form fields.
     *
     * @param event The BlurEvent that gives information about the FormItem that
     * has been modified and its value.
     */
    public void onBlur(BlurEvent event) {
        FormItem item = event.getItem();
        String  field = item.getFieldName();

        if (field == null) {
            return;
        }
        DoubleRangePanel p = (DoubleRangePanel) event.getForm();
    }


    public void dumpGWT(String cid) {
        GWT.log("Setting values for cId: " + cid);
        GWT.log("River: " + fixInfo.getRiver());
        GWT.log("Date: " + fixInfo.getEventByCId(cid).getDate());
        GWT.log("Name: " + fixInfo.getEventByCId(cid).getDescription());
    }
}
