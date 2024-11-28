/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.fixation;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This UIProvider creates a panel to select discharge classes / sectors
 * (german Abflussklassen).
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixGaugeSelectPanel
extends      FixationPanel
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected String first;
    protected String second;

    protected SelectItem from;
    protected SelectItem to;

    protected LinkedHashMap<String, String> mapValues;

    public FixGaugeSelectPanel() {
        htmlOverview = "";

        mapValues = new LinkedHashMap<String, String>();
        mapValues.put("0", MESSAGES.gauge_mnq());
        mapValues.put("1", MESSAGES.gauge_mq());
        mapValues.put("2", MESSAGES.gauge_mhq());
        mapValues.put("3", MESSAGES.gauge_hq5());
    }

    @Override
    public Canvas createWidget(DataList data) {
        instances.put(this.artifact.getUuid(), this);

        VLayout layout = new VLayout();

        Label title = new Label(MESSAGES.gauge_class());
        title.setHeight(25);

        from = new SelectItem(MESSAGES.from());
        to = new SelectItem(MESSAGES.to());

        from.setShowTitle(false);
        to.setShowTitle(false);
        from.setValueMap(mapValues);
        from.setDefaultValues("0");
        from.setWidth(160);
        to.setValueMap(mapValues);
        to.setDefaultValues("3");
        to.setWidth(160);

        DynamicForm form = new DynamicForm();
        StaticTextItem separator = new StaticTextItem("separator");
        separator.setShowTitle(false);
        separator.setValue(MESSAGES.to());
        form.setNumCols(5);
        form.setFields(from, separator, to);

        layout.addMember(title);
        layout.addMember(form);

        return layout;
    }

    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data f = getData(items, "q1");
        Data t = getData(items, "q2");
        DataItem[] fItems = f.getItems();
        DataItem[] tItems = t.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append(mapValues.get(fItems[0].getLabel()));
        sb.append(" " + MESSAGES.to() + " ");
        sb.append(mapValues.get(tItems[0].getLabel()));

        Label old = new Label(sb.toString());
        old.setWidth(130);

        HLayout layout = new HLayout();
        layout.setWidth("400px");
        Label label = new Label(dataList.getLabel());
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
    @Override
    public Data[] getData() {
        List<Data> data = new ArrayList<Data>();

        boolean valid = saveClassValues();
        if (valid) {
            DataItem firstItem = new DefaultDataItem("q1", "q1", this.first);
            DataItem secItem = new DefaultDataItem("q2", "q2", this.second);
            data.add(new DefaultData(
                "q1",
                null,
                null,
                new DataItem[] { firstItem }));
            data.add(new DefaultData(
                "q2",
                null,
                null,
                new DataItem[] { secItem }));
        }
        return data.toArray(new Data[data.size()]);
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


    @Override
    public void success() {}

    protected boolean saveClassValues() {
        String v1 = from.getValueAsString();
        String v2 = to.getValueAsString();
        try {
            int v1i = Integer.parseInt(v1);
            int v2i = Integer.parseInt(v2);
            if (v1i <= v2i) {
                this.first = v1;
                this.second = v2;
                return true;
            }
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return false;
    }
}
