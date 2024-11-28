/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.minfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.ui.AbstractUIProvider;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

public class CheckboxPanel extends AbstractUIProvider {

    private String dataName;
    HashMap<String, Boolean> values;

    protected DynamicForm form;

    public CheckboxPanel() {
        super();
        values = new HashMap<String, Boolean>();
    }

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
        form = new DynamicForm();

        VLayout layout = new VLayout();
        Label   label  = new Label(data.getDescription());
        LinkedList<CheckboxItem> cbItems = new LinkedList<CheckboxItem>();
        for (int i = 0; i < items.length; i++) {
            CheckboxItem item = new CheckboxItem(items[i].getLabel());
            GWT.log(items[i].getStringValue() + "; " + items[i].getLabel());
            item.addChangedHandler(new ChangedHandler() {
                @Override
                public void onChanged(ChangedEvent event) {
                    values.put(
                        event.getItem().getName(),
                        (Boolean)event.getItem().getValue());
                }
            });
            cbItems.add(item);
        }

        form.setFields(cbItems.toArray(new CheckboxItem[cbItems.size()]));
        layout.setMembersMargin(10);
        layout.setHeight(35);
        label.setHeight(35);

        layout.addMember(label);
        layout.addMember(form);
        layout.addMember(getNextButton());
        layout.setMembersMargin(10);

        return layout;
    }

    @Override
    protected Data[] getData() {
        String value = "";
        Set<String> entries = values.keySet();
        boolean first = true;
        for (String s: values.keySet()) {
            if (!first) {
                value += ";";
            }
            if ((Boolean)values.get(s) == true) {
                value += s;
            }
            first = false;
        }
        DataItem item = new DefaultDataItem("diameter", "diameter", value);

        return new Data[] {new DefaultData(
            "diameter",
            null,
            null,
            new DataItem[]{item})};
    }

}
