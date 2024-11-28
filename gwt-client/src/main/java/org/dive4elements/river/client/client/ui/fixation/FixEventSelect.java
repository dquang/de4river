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

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.IntDataItem;
import org.dive4elements.river.client.shared.model.IntegerArrayData;
import org.dive4elements.river.client.shared.model.FixingsOverviewInfo.FixEvent;

import org.dive4elements.river.client.client.services.FixingsOverviewService;
import org.dive4elements.river.client.client.services.FixingsOverviewServiceAsync;

/**
 * This UIProvider lets you select events.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixEventSelect
extends      FixationPanel
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    public static final int MAX_DISPLAYED_ITEMS = 5;

    protected FixingsOverviewServiceAsync overviewService =
        GWT.create(FixingsOverviewService.class);

    protected List<String> events;

    public FixEventSelect() {
        htmlOverview = "";
        events = new ArrayList<String>();
    }

    public Canvas createWidget(DataList data) {
        instances.put(this.artifact.getUuid(), this);

        VLayout layout = new VLayout();

        Canvas title = new Label(MESSAGES.eventselect());
        title.setHeight("25px");

        layout.addMember(title);
        return layout;
    }

    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> list = dataList.getAll();

        Data data = getData(list, "events");

        VLayout dataLayout = new VLayout();
        dataLayout.setWidth(130);

        DataItem[] items = data.getItems();

        if (items.length > MAX_DISPLAYED_ITEMS) {
            for (int i = 0; i < MAX_DISPLAYED_ITEMS-2; ++i) {
                Label l = new Label(items[i].getLabel());
                l.setHeight(25);
                dataLayout.addMember(l);
            }
            Label l = new Label("...");
            l.setHeight(25);
            dataLayout.addMember(l);
            l = new Label(items[items.length-1].getLabel());
            l.setHeight(25);
            dataLayout.addMember(l);
        }
        else {
            for (int i = 0; i < items.length; i++) {
                Label l = new Label(items[i].getLabel());
                l.setHeight(25);
                dataLayout.addMember(l);
            }
        }

        HLayout layout = new HLayout();
        layout.setWidth("400px");

        Label   label  = new Label(dataList.getLabel());
        label.setWidth("200px");

        Canvas back = getBackButton(dataList.getState());

        layout.addMember(label);
        layout.addMember(dataLayout);
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

        if (events.size() > 0) {
            IntDataItem[] arr = new IntDataItem[events.size()];
            for (int i = 0, E = events.size(); i < E; i++) {
                try {
                    Integer v = new Integer(events.get(i));
                    arr[i] = new IntDataItem("id", "id", v.intValue());
                }
                catch (NumberFormatException nfe) {
                    return  data.toArray(new Data[data.size()]);
                }
            }

            IntegerArrayData iad =
                new IntegerArrayData("events", "events", arr);

            data.add(iad);
        }

        return data.toArray(new Data[data.size()]);
    }


    @Override
    public void setValues(String cid, boolean checked) {
        if (checked) {
            events.add(cid);
        }
        else {
            if (events.contains(cid)) {
                events.remove(cid);
            }
        }
    }


    @Override
    public boolean renderCheckboxes() {
        return true;
    }


    public void success() {
        for (FixEvent fe: fixInfo.getEvents()) {
            events.add(fe.getCId());
        }
    }

    public void dumpGWT(String cid) {
        GWT.log("Setting values for cId: " + cid);
        GWT.log("River: " + fixInfo.getRiver());
        GWT.log("Date: " + fixInfo.getEventByCId(cid).getDate());
        GWT.log("Name: " + fixInfo.getEventByCId(cid).getDescription());
    }
}
