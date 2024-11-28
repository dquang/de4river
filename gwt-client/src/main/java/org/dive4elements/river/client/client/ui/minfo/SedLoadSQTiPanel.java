/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.minfo;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.services.SedimentLoadInfoService;
import org.dive4elements.river.client.client.services.SedimentLoadInfoServiceAsync;
import org.dive4elements.river.client.client.ui.PeriodPanel;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.SedimentLoadInfoObject;
import org.dive4elements.river.client.shared.model.SedimentLoadInfoRecord;

/** Show input to select an official epoch. */
public class SedLoadSQTiPanel
extends PeriodPanel
{
    protected SedimentLoadInfoServiceAsync sedLoadInfoService =
        GWT.create(SedimentLoadInfoService.class);

    protected ListGrid sedLoadTable;

        /** Creates layout with title. */
    public Canvas createWidget(DataList data) {
        VLayout root = new VLayout();

        Label title = new Label(data.get(0).getDescription());
        title.setHeight("25px");

        root.addMember(title);

        return root;
    }

    /** Create layout for data entered previously. */
    @Override
    public Canvas createOld(DataList dataList) {
        HLayout layout = new HLayout();
        layout.setWidth("400px");
        VLayout vLayout = new VLayout();
        vLayout.setWidth(130);
        Label label = new Label(dataList.getLabel());
        label.setWidth("200px");
        label.setHeight(25);

        List<Data> items = dataList.getAll();
        Data str = getData(items, "sq_ti_date");
        DataItem[] strItems = str.getItems();

        String dateString = strItems[0].getStringValue();
        Label dateLabel = new Label(dateString);
        dateLabel.setHeight(20);
        vLayout.addMember(dateLabel);
        Canvas back = getBackButton(dataList.getState());
        layout.addMember(label);
        layout.addMember(vLayout);
        layout.addMember(back);

        GWT.log("Old data: " + strItems[0].getDescription()
            + " label " + strItems[0].getLabel()
            + " value: " + strItems[0].getStringValue());

        return layout;
    }

    public Canvas create(DataList data) {
        VLayout layout = new VLayout();
        Canvas helper = createHelper();
        this.helperContainer.addMember(helper);

        Canvas submit = getNextButton();
        Canvas widget = createWidget(data);

        layout.addMember(widget);
        layout.addMember(submit);

        fetchSedimentLoadData();

        return layout;
    }

    /** Creates the helper grid in which off epochs can be selected. */
    protected Canvas createHelper() {
        sedLoadTable = new ListGrid();
        sedLoadTable.setShowHeaderContextMenu(false);
        sedLoadTable.setWidth100();
        sedLoadTable.setShowRecordComponents(true);
        sedLoadTable.setShowRecordComponentsByCell(true);
        sedLoadTable.setHeight100();
        sedLoadTable.setEmptyMessage(MSG.empty_table());
        sedLoadTable.setCanReorderFields(false);
        sedLoadTable.setSelectionAppearance(SelectionAppearance.CHECKBOX);
        sedLoadTable.setSelectionType(SelectionStyle.SINGLE);

        ListGridField date = new ListGridField("sq_ti_date", MSG.year());
        date.setType(ListGridFieldType.TEXT);
        date.setWidth(100);

        sedLoadTable.setFields(date);
        return sedLoadTable;
    }

    /** Get data via listgrid selection. */
    @Override
    public Data[] getData() {
        List<Data> data = new ArrayList<Data>();

        ListGridRecord[] lgr = sedLoadTable.getSelectedRecords();
        if (lgr.length == 0) {
            GWT.log("returning empty data.");
            return new Data[0];
        }
        String d = "";
        String desc = "";
        for (int i = 0; i < lgr.length; i++) {
            /* Should only be one item as this is single selection */
            Record r = (Record) lgr[i];
            d = r.getAttribute("sq_ti_id");
            desc = r.getAttribute("sq_ti_date");
            GWT.log("Got attribute sq_ti_id : " + d + " desc: " + desc);
        }
        DataItem item = new DefaultDataItem("sq_ti_id", "this is ignored", d);
        DataItem dateLabel = new DefaultDataItem(
            "sq_ti_date", "this is ignored", desc);
        data.add(new DefaultData(
                    "sq_ti_id",
                    null,
                    null,
                    new DataItem[] { item }));
        data.add(new DefaultData(
                    "sq_ti_date",
                    null,
                    null,
                    new DataItem[] { dateLabel }));
        return data.toArray(new Data[data.size()]);
    }

    /** Fetch load info from service and populate table. */
    protected void fetchSedimentLoadData() {
        Config config = Config.getInstance();
        String locale = config.getLocale ();

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        double[] km = artifact.getArtifactDescription().getKMRange();
        String river = artifact.getArtifactDescription().getRiver();

        sedLoadInfoService.getSedimentLoadInfo(
            locale, river, "sq_time_intervals", km[0], km[1], "",
            new AsyncCallback<SedimentLoadInfoObject[]>() {
                public void onFailure(Throwable caught) {
                    GWT.log("Could not receive sediment load informations.");
                    SC.warn(MSG.getString(caught.getMessage()));
                }

                public void onSuccess(SedimentLoadInfoObject[] sedLoad) {
                    int num = sedLoad != null ? sedLoad.length :0;
                    GWT.log("Received " + num + " sediment load informations.");

                    if (num == 0) {
                        return;
                    }

                    addSedimentLoadInfo(sedLoad);
                }
            }
        );
    }

    /** Add record to input helper listgrid. */
    protected void addSedimentLoadInfo (SedimentLoadInfoObject[] sedLoad) {
        for(SedimentLoadInfoObject sl: sedLoad) {
            SedimentLoadInfoRecord rec = new SedimentLoadInfoRecord(sl);
            sedLoadTable.addData(rec);
        }
        if (sedLoad.length == 1) {
            /* Preselect lists with only one load. */
            sedLoadTable.selectRecords(new int[] {0});
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
