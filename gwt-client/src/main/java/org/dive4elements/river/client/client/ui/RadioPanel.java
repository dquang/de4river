/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.LinkedHashMap;

public class RadioPanel extends AbstractUIProvider {

    private static final long serialVersionUID = 3357071563224461043L;

    protected String dataName;
    protected DynamicForm form;

    @Override
    public Canvas createOld(DataList dataList) {
        Data       data  = dataList.get(0);
        DataItem[] items = data.getItems();

        HLayout layout = new HLayout();
        Label   label  = new Label(dataList.getLabel());
        Label   value  = new Label(items[0].getLabel());

        layout.setHeight(35);
        layout.setWidth(400);
        label.setWidth(200);

        layout.addMember(label);
        layout.addMember(value);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }

    @Override
    public Canvas create(DataList dataList) {
        Data       data  = dataList.get(0);
        DataItem[] items = data.getItems();

        this.dataName = data.getLabel();

        VLayout layout = new VLayout();
        Label   label  = new Label(data.getDescription());
        RadioGroupItem rgi = new RadioGroupItem("selection");
        rgi.setShowTitle(false);
        GWT.log("items: " + items.length);
        LinkedHashMap<String, String> elems =
            new LinkedHashMap<String, String>();
        for (int i = 0; i < items.length; i++) {
            GWT.log(items[i].getStringValue() + "; " + items[i].getLabel());
            elems.put(items[i].getStringValue(), items[i].getLabel());
        }
        rgi.setValueMap(elems);
        rgi.setDefaultValue(items[0].getStringValue());

        form = new DynamicForm();
        form.setFields(rgi);
        layout.setMembersMargin(10);
        layout.setHeight(35);
        label.setHeight(35);

        layout.addMember(label);
        layout.addMember(form);
        layout.addMember(getNextButton());
        layout.setMembersMargin(10);

        //initDefaultValues(dataList);

        return layout;
    }

    @Override
    protected Data[] getData() {
        String value = form.getValueAsString("selection");
        DataItem item = new DefaultDataItem(
            this.dataName, this.dataName, value);
        return new Data[] { new DefaultData(
            this.dataName, null, null, new DataItem[]{item})};
    }

    protected String getTitle(DataItem item) {
        return item.getLabel();
    }
}
