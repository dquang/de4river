/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.fixation;

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FixFunctionSelect extends FixationPanel {
    private static final Map<String, String> funcDesc =
        new HashMap<String, String>();

    static {
        funcDesc.put("log", "W(Q) = m*ln(Q + b)");
        funcDesc.put("linear", "W(Q) = m * Q + b");
        funcDesc.put("log-linear", "W(Q) = a*ln(m*Q+b)");
        funcDesc.put("exp", "W(Q) = m * a^Q + b");
        funcDesc.put("quad", "W(Q) = n*Q^2+m*Q+b");
        funcDesc.put("pow", "W(Q) = a * Q^c + d");
        funcDesc.put("sq-pow", "S(Q) = a * Q^b");
    }

    /** The combobox.*/
    protected DynamicForm form;

    @Override
    public Canvas createWidget(DataList data) {
        VLayout layout   = new VLayout();
        layout.setAlign(VerticalAlignment.TOP);
        layout.setHeight(25);

        LinkedHashMap initial = new LinkedHashMap();

        form = new DynamicForm();

        int size = data.size();

        for (int i = 0; i < size; i++) {
            Data d = data.get(i);

            Label label = new Label(d.getDescription());
            label.setValign(VerticalAlignment.TOP);
            label.setHeight(20);
            label.setWidth(400);

            SelectItem combobox = new SelectItem(d.getLabel());
            combobox.setWidth(250);

            LinkedHashMap<String, String> funcTypes =
                new LinkedHashMap<String, String>();

            boolean  defaultSet = false;
            boolean  first      = true;

            DataItem def      = d.getDefault();
            String   defValue = def != null ? def.getStringValue() : null;

            if (defValue != null && defValue.length() > 0) {
                initial.put(d.getLabel(), def.getStringValue());
                defaultSet = true;
            }

            // I was here. Me 2.
            for (DataItem item: d.getItems()) {
                if (!defaultSet && first) {
                    initial.put(d.getLabel(), item.getStringValue());
                    first = false;
                }

                funcTypes.put(item.getStringValue(), item.getLabel());
            }

            label.setWidth(50);
            combobox.setValueMap(funcTypes);
            combobox.setShowTitle(false);
            form.setItems(combobox);

            layout.addMember(label);
            layout.addMember(form);
        }

        form.setValues(initial);

        layout.setAlign(VerticalAlignment.TOP);

        return layout;
    }


    @Override
    public Canvas createOld(DataList dataList) {
        HLayout layout  = new HLayout();
        VLayout vLayout = new VLayout();
        layout.setWidth("400px");

        Label label = new Label(dataList.getLabel());
        label.setWidth("200px");

        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            Data data        = dataList.get(i);
            DataItem[] items = data.getItems();

            for (DataItem item: items) {
                HLayout hLayout = new HLayout();

                String desc = funcDesc.containsKey(item.getLabel()) ?
                        funcDesc.get(item.getLabel()) : item.getLabel();
                hLayout.addMember(label);
                hLayout.addMember(new Label(desc));

                vLayout.addMember(hLayout);
                vLayout.setWidth("130px");
            }
        }

        Canvas back = getBackButton(dataList.getState());

        layout.addMember(label);
        layout.addMember(vLayout);
        layout.addMember(back);

        return layout;
    }


    @Override
    public Data[] getData() {
        Map<?, ?> values = form.getValues();

        Data[] list = new Data[values.size()];
        int       i = 0;

        for (Map.Entry<?, ?>entry: values.entrySet()) {
            String fieldname = (String)entry.getKey();
            String selection = (String)entry.getValue();

            DataItem item    = new DefaultDataItem(fieldname, null, selection);

            list[i++] = new DefaultData(
                fieldname, null, null, new DataItem[] { item });
        }

        return list;
    }


    @Override
    public void setValues(String cid, boolean checked) {
    }

    @Override
    public boolean renderCheckboxes() {
        return false;
    }

    @Override
    public void success() {
    }

}
