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

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.List;


public class HWSDatacagePanel
extends DatacagePanel
{
    public static final String OUT        = "floodmap_hws_panel";
    public static final String PARAMETERS = "hws:true;load-system:true";


    public HWSDatacagePanel() {
        super();
    }


    public HWSDatacagePanel(User user) {
        super(user);
    }


    @Override
    protected void createWidget() {
        super.createWidget();
        widget.setIsMutliSelectable(true);
    }


    @Override
    public String getOuts() {
        return OUT;
    }


    @Override
    public String getParameters() {
        return PARAMETERS;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        return errors;
    }

    @Override
    public Canvas createOld(DataList dataList) {
        GWT.log("old datacage##########################################");
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


    @Override
    protected Data[] getData() {
        String[] selection = this.widget.getSelectionTitles();
        String result = "";
        boolean first = true;
        if (selection != null) {
            for (String record: selection) {
                if (first) {
                    result += record;
                    first = false;
                }
                else {
                    result += ";" + record;
                }
            }
        }
        if (result.length() == 0) {
            result = MSG.notselected();
        }
        Data[] data = new Data[1];
        DataItem item = new DefaultDataItem(
                "uesk.hws", "uesk.hws", result);
        data[0] = new DefaultData(
            "uesk.hws", null, null, new DataItem[] {item});

        return data;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
