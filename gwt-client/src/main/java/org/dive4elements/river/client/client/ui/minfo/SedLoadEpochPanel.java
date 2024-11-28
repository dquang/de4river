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
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.validator.IsIntegerValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.SedimentLoadInfoService;
import org.dive4elements.river.client.client.services.SedimentLoadInfoServiceAsync;
import org.dive4elements.river.client.client.ui.AbstractUIProvider;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.SedimentLoadInfoObject;
import org.dive4elements.river.client.shared.model.SedimentLoadInfoRecord;


public class SedLoadEpochPanel
extends AbstractUIProvider
{
    protected SedimentLoadInfoServiceAsync sedLoadInfoService =
        GWT.create(SedimentLoadInfoService.class);

    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected ListGrid elements;
    private TextItem start;
    private TextItem end;
    private ListGrid sedLoadTable;

    protected List<String> validYears;

    public Canvas createWidget(DataList data) {
        HLayout input = new HLayout();
        VLayout root = new VLayout();
        VLayout grid = new VLayout();
        VLayout intFields = new VLayout();
        Button add = new Button(MSG.add_date());
        elements = new ListGrid();

        Label title = new Label(data.get(0).getDescription());
        title.setHeight("25px");

        DynamicForm form = new DynamicForm();
        form.setNumCols(4);
        start = new TextItem(MSG.from());
        start.setWidth(60);
        start.setValidators(new IsIntegerValidator());
        end = new TextItem(MSG.to());
        end.setWidth(60);
        end.setValidators(new IsIntegerValidator());
        form.setFields(start, end);
        add.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                String v1 = start.getValueAsString();
                String v2 = end.getValueAsString();
                if (v1 == null || v2 == null) {
                    return;
                }
                if (!isValidEpoch(v1, v2)) {
                    return;
                }
                ListGridRecord r = new ListGridRecord();
                r.setAttribute("from", v1);
                r.setAttribute("to", v2);
                elements.addData(r);
            }
        });

        Label sel = new Label(MSG.select());
        sel.setHeight(25);
        elements.setWidth(185);
        elements.setHeight(120);
        elements.setShowHeaderContextMenu(false);
        elements.setCanReorderFields(false);
        elements.setCanSort(false);
        elements.setCanEdit(false);
        ListGridField from = new ListGridField("from", MSG.from());
        ListGridField to = new ListGridField("to", MSG.to());
        from.setWidth(70);
        to.setWidth(70);

        final ListGridField removeField  =
            new ListGridField("_removeRecord", "Remove Record"){{
                setType(ListGridFieldType.ICON);
                setIcon(GWT.getHostPageBaseURL() + MSG.removeFeature());
                setCanEdit(false);
                setCanFilter(false);
                setCanSort(false);
                setCanGroupBy(false);
                setCanFreeze(false);
                setWidth(25);
        }};

        elements.addRecordClickHandler(new RecordClickHandler() {
                @Override
                public void onRecordClick(final RecordClickEvent event) {
                    // Just handle remove-clicks
                    if(!event.getField().getName()
                        .equals(removeField.getName())
                    ) {
                        return;
                    }
                    event.getViewer().removeData(event.getRecord());
                }
            });

        elements.setFields(from, to, removeField);

        intFields.addMember(form);
        intFields.addMember(add);
        grid.addMember(sel);
        grid.addMember(elements);
        input.addMember(intFields);
        input.addMember(grid);
        root.addMember(title);
        root.addMember(input);

        return root;
    }

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
        Data str = getData(items, "epochs");
        DataItem[] strItems = str.getItems();

        String[] pairs = strItems[0].getLabel().split(";");
        for (int i = 0; i < pairs.length; i++) {
            String[] vals = pairs[i].split(",");
            Label dateLabel = new Label(vals[0] + " - " + vals[1]);
            dateLabel.setHeight(20);
            vLayout.addMember(dateLabel);
        }
        Canvas back = getBackButton(dataList.getState());
        layout.addMember(label);
        layout.addMember(vLayout);
        layout.addMember(back);

        return layout;
    }

    @Override
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

    private Canvas createHelper() {
        sedLoadTable = new ListGrid();
        sedLoadTable.setShowHeaderContextMenu(false);
        sedLoadTable.setWidth100();
        sedLoadTable.setShowRecordComponents(true);
        sedLoadTable.setShowRecordComponentsByCell(true);
        sedLoadTable.setHeight100();
        sedLoadTable.setEmptyMessage(MSG.empty_table());
        sedLoadTable.setCanReorderFields(false);

        /* Input support pins */
        String baseUrl = GWT.getHostPageBaseURL();
        ListGridField pinFrom = new ListGridField ("fromIcon", MESSAGES.from());
        pinFrom.setWidth (30);
        pinFrom.setType (ListGridFieldType.ICON);
        pinFrom.setCellIcon(baseUrl + MESSAGES.markerGreen());

        ListGridField pinTo = new ListGridField ("toIcon", MESSAGES.to());
        pinTo.setType (ListGridFieldType.ICON);
        pinTo.setWidth (30);
        pinTo.setCellIcon(baseUrl + MESSAGES.markerRed());

        pinFrom.addRecordClickHandler (new RecordClickHandler () {
            @Override
            public void onRecordClick (RecordClickEvent e) {
                Record r = e.getRecord();
                start.setValue(r.getAttribute("date"));
            }
        });

        pinTo.addRecordClickHandler (new RecordClickHandler () {
            @Override
            public void onRecordClick (RecordClickEvent e) {
                Record r = e.getRecord();
                end.setValue(r.getAttribute("date"));
            }
        });


        ListGridField date = new ListGridField("date", MSG.year());
        date.setType(ListGridFieldType.TEXT);
        date.setWidth(100);

        ListGridField descr =
            new ListGridField("description", MSG.description());
        descr.setType(ListGridFieldType.TEXT);
        descr.setWidth("*");

        sedLoadTable.setFields(pinFrom, pinTo, date, descr);
        return sedLoadTable;
    }

    @Override
    protected Data[] getData() {
        List<Data> data = new ArrayList<Data>();

        ListGridRecord[] lgr = elements.getRecords();
        if (lgr.length == 0) {
            return new Data[0];
        }
        String d = "";
        for (int i = 0; i < lgr.length; i++) {
            Record r = (Record) lgr[i];
            d += r.getAttribute("from") + "," + r.getAttribute("to");
            d += ";";
        }

        DataItem item = new DefaultDataItem("epochs", null, d);
            data.add(new DefaultData(
                        "epochs",
                        null,
                        null,
                        new DataItem[] { item }));
        return data.toArray(new Data[data.size()]);
    }

    protected void fetchSedimentLoadData() {
        Config config    = Config.getInstance();
        String locale    = config.getLocale ();

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        double[] km = artifact.getArtifactDescription().getKMRange();
        String river = artifact.getArtifactDescription().getRiver();

        String sq_ti_id = "";
        validYears = new ArrayList<String>(data.length);
        for (int i = 0; i < data.length; i++) {
            Data str = getData(data[i].getAll(), "sq_ti_id");
            if (str != null) {
                DataItem[] strItems = str.getItems();
                sq_ti_id = strItems[0].getStringValue();
                break;
            }
        }

        if (sq_ti_id.isEmpty()){
            GWT.log("Failed to find sq time interval id in data.");
        }

        sedLoadInfoService.getSedimentLoadInfo(
            locale, river, "single", km[0], km[1], sq_ti_id,
            new AsyncCallback<SedimentLoadInfoObject[]>() {
                public void onFailure(Throwable caught) {
                    GWT.log("Could not recieve sediment load informations.");
                    SC.warn(MSG.getString(caught.getMessage()));
                }

                public void onSuccess(SedimentLoadInfoObject[] sedLoad) {
                    int num = sedLoad != null ? sedLoad.length :0;
                    GWT.log("Recieved " + num + " sediment load informations.");

                    if (num == 0) {
                        return;
                    }

                    addSedimentLoadInfo(sedLoad);
                }
            }
        );
    }


    protected void addSedimentLoadInfo (SedimentLoadInfoObject[] sedLoad) {
        for(SedimentLoadInfoObject sl: sedLoad) {
            SedimentLoadInfoRecord rec = new SedimentLoadInfoRecord(sl);
            sedLoadTable.addData(rec);
            validYears.add(rec.getDate());
        }
    }

    /* Validate the epoch input. We do this here and not in an overridden
     * validate method as we want to validate before an epoch is added
     * to the list of epochs. */
    protected boolean isValidEpoch(String y1, String y2) {
        // First check that both are integer
        int iY1;
        int iY2;
        List<String> errors = new ArrayList<String>();
        try {
            iY1 = Integer.parseInt(y1);
        } catch (NumberFormatException e) {
            errors.add(MESSAGES.wrongFormat() + ": " + y1);
        }
        try {
            iY2 = Integer.parseInt(y2);
        } catch (NumberFormatException e) {
            errors.add(MESSAGES.wrongFormat() + ": " + y2);
        }
        if (!errors.isEmpty()) {
            showErrors(errors);
            return false;
        }
        boolean startIsGood = false;
        boolean endIsGood = false;
        for (String validYear: validYears) {
            if (startIsGood || y1.equals(validYear)) {
                startIsGood = true;
            }
            if (endIsGood || y2.equals(validYear)) {
                endIsGood = true;
            }
            if (startIsGood && endIsGood) {
                break;
            }
            /* alternative check if data lies in between
            int aYear = Integer.parseInt(validYear);
            if (aYear >= iY1 && aYear <= iY2) {
                isGood = true;
                break;
            }
         */
        }
        if (!startIsGood) {
            String tmp = MESSAGES.no_data_for_year();
            tmp = tmp.replace("$1", y1);
            errors.add(tmp);
        }
        if (!endIsGood) {
            String tmp = MESSAGES.no_data_for_year();
            tmp = tmp.replace("$1", y2);
            errors.add(tmp);
        }
        if (!errors.isEmpty()) {
            showErrors(errors);
            return false;
        }
        return true;
    }
}
