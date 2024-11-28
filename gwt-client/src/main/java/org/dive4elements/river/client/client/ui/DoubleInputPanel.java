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

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This UIProvider creates a panel for location or distance input.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DoubleInputPanel
extends      AbstractUIProvider
{

    private static final long serialVersionUID = 2006773072352563622L;

    /** The message class that provides i18n strings. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    private TextItem inputPanel;

    private double value;

    protected String dataName;


    public DoubleInputPanel() {
        dataName = "outliers";
    }


    public DoubleInputPanel(String dataName) {
        this.dataName = dataName;
    }


    @Override
    public Canvas create(DataList list) {
        VLayout layout = new VLayout();

        Canvas helper = createHelper();
        this.helperContainer.addMember(helper);

        Canvas submit = getNextButton();
        Canvas widget = createWidget(list);

        layout.addMember(widget);
        layout.addMember(submit);
        return layout;
    }


    /** Create a labelled input-panel. */
    public Canvas createWidget(DataList data) {
        VLayout layout = new VLayout();

        Label title = new Label(data.get(0).getDescription());
        title.setHeight("25px");

        DataItem defaultItem = data.get(0).getDefault();

        DynamicForm form = new DynamicForm();
        inputPanel = new TextItem();
        inputPanel.setTitle(dataName);
        inputPanel.setShowTitle(false);

        if (defaultItem != null) {
            inputPanel.setValue(defaultItem.getStringValue());
        }

        form.setFields(inputPanel);

        layout.addMember(title);
        layout.addMember(form);

        return layout;
    }


    protected Canvas createHelper() {
        return new VLayout();
    }


    /** Create canvas to show previously entered value. */
    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data d = getData(items, dataName);
        DataItem[] item = d.getItems();

        String v = item[0].getLabel();

        Label old = new Label(v);
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

        boolean valid = saveDataValues();
        if(valid) {

            String vs = Double.valueOf(this.value).toString();
            DataItem item = new DefaultDataItem(dataName, dataName, vs);
            data.add(new DefaultData(
                dataName,
                null,
                null,
                new DataItem[] { item }));
        }

        return data.toArray(new Data[data.size()]);
    }


    protected boolean saveDataValues() {
        String st = inputPanel.getValueAsString();
        if (st == null) {
            SC.warn("fehler... TODO");
            return false;
        }

        try {
            NumberFormat nf = NumberFormat.getDecimalFormat();
            double d = nf.parse(st);
            this.value = d;
        }
        catch(NumberFormatException nfe) {
            SC.warn("fehler... nfe... TODO");
            return false;
        }
        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
