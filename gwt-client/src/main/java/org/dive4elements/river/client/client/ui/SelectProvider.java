/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.StepForwardEvent;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * This UIProvider displays the DataItems of the Data object in a combo box.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SelectProvider
extends      AbstractUIProvider
{
    private static final long serialVersionUID = 4696637534424070726L;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants messages = GWT.create(FLYSConstants.class);

    /** The combobox.*/
    protected DynamicForm form = new DynamicForm();

    public static final int COMBOBOX_THRESHOLD = 20;


    /**
     * This method currently returns a
     * {@link com.smartgwt.client.widgets.form.DynamicForm} that contains all
     * data items in a combobox stored in <i>data</i>.
     *
     * @param data The {@link Data} object.
     *
     * @return a combobox.
     */
    @Override
    public Canvas create(DataList data) {
        VLayout v = new VLayout();
        v.setMembersMargin(10);
        v.addMember(createWidget(data));
        if(data.size() > COMBOBOX_THRESHOLD) {
            v.addMember(getNextButton());
        }
        return v;
    }


    protected Canvas createWidget(DataList data) {
        if (data.size() > COMBOBOX_THRESHOLD) {
            return createComboboxWidget(data);
        }
        else {
            return createListWidget(data);
        }
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

                hLayout.addMember(label);
                hLayout.addMember(new Label(item.getLabel()));

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


    protected Canvas createListWidget(DataList data) {
        VLayout layout = new VLayout();
        layout.setAlign(VerticalAlignment.TOP);
        layout.setHeight(25);
        layout.setWidth("100%");

        VLayout formLayout = new VLayout();
        formLayout.setLayoutTopMargin(20);
        formLayout.setLayoutLeftMargin(50);

        ClickHandler handler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LinkItem li = (LinkItem)event.getItem();
                String attr = li.getAttribute(li.getName());
                GWT.log("li.getTarget: " + attr + " " + li.getName());
                DataItem item = new DefaultDataItem(
                    attr,
                    null,
                    attr);

                Data d = new DefaultData(
                    li.getName(),
                    null,
                    null,
                    new DataItem [] { item });

                Data [] odata = getData();
                Data [] ndata = new Data[odata.length+1];
                System.arraycopy(odata, 0, ndata, 0, odata.length);
                ndata[odata.length] = d;

                fireStepForwardEvent(new StepForwardEvent(ndata));
            }
        };

        for (int i = 0, size = data.size(); i < size; i++) {
            Data d = data.get(i);

            Label label = new Label(d.getDescription());
            label.setValign(VerticalAlignment.TOP);
            label.setHeight(20);
            label.setWidth(400);

            LinkedHashMap<String, String> initial =
                new LinkedHashMap<String, String>();
            ArrayList<FormItem> formItems = new ArrayList<FormItem>();

            for (DataItem item: d.getItems()) {
                initial.put(
                    item.getLabel().replace(' ', '_'), item.getStringValue());
                GWT.log("put: " + item.getLabel().replace(' ', '_')
                    + "=" + item.getStringValue());

                LinkItem link = new LinkItem(d.getLabel());
                link.setLinkTitle(item.getLabel());    // i18n text of the link
                link.setAttribute(d.getLabel(), item.getStringValue());
                // e.g. "calculation_mode":"foo"
                link.setShowTitle(false);
                link.setEndRow(true);
                link.setWidth("350px");

                SpacerItem space = new SpacerItem();
                space.setWidth(15);
                formItems.add(space);
                formItems.add(link);

                link.addClickHandler(handler);
            }

            form.setFields(formItems.toArray(new FormItem[0]));
            form.setValues(initial);

            layout.addMember(label);
            layout.addMember(form);
        }
        return layout;
    }

    protected Canvas createComboboxWidget(DataList data) {
        GWT.log("SelectProvider.createComboboxWidget()");

        VLayout layout   = new VLayout();
        layout.setAlign(VerticalAlignment.TOP);
        layout.setHeight(25);

        LinkedHashMap<String, String> initial =
            new LinkedHashMap<String, String>();

        int size = data.size();

        for (int i = 0; i < size; i++) {
            Data d = data.get(i);

            Label label = new Label(d.getDescription());
            label.setValign(VerticalAlignment.TOP);
            label.setHeight(20);
            label.setWidth(400);

            SelectItem combobox = new SelectItem(d.getLabel());
            combobox.setWidth(250);

            LinkedHashMap<String, String> it =
                new LinkedHashMap<String, String>();

            boolean  defaultSet = false;
            boolean  first      = true;

            DataItem def      = d.getDefault();
            String   defValue = def != null ? def.getStringValue() : null;

            if (defValue != null && defValue.length() > 0) {
                initial.put(d.getLabel(), def.getStringValue());
                defaultSet = true;
            }

            for (DataItem item: d.getItems()) {
                if (!defaultSet && first) {
                    initial.put(d.getLabel(), item.getStringValue());
                    first = false;
                }

                it.put(item.getStringValue(), item.getLabel());
            }

            label.setWidth(50);
            combobox.setValueMap(it);
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
    protected Data[] getData() {
        Map<?,?> values  = form.getValues();

        Data[] list = new Data[values.size()];
        int       i = 0;

        for (Map.Entry<?, ?> entry: values.entrySet()) {
            String fieldname = ((String)entry.getKey()).replace('_', ' ');
            String selection = (String)entry.getValue();

            DataItem item    = new DefaultDataItem(fieldname, null, selection);

            list[i++] = new DefaultData(
                fieldname, null, null, new DataItem[] { item });
        }

        return list;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
