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
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.validator.IsIntegerValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;

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


public class SedLoadPeriodPanel
extends AbstractUIProvider
{
    protected SedimentLoadInfoServiceAsync sedLoadInfoService =
        GWT.create(SedimentLoadInfoService.class);

    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    private TextItem yearsItem;

    private ListGrid sedLoadTable;

    protected List<String> validYears;

    public SedLoadPeriodPanel () {
    }

    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();
        Data years = getData(items, "years");
        DataItem[] yearsItems = years.getItems();

        String v1 = yearsItems[0].getStringValue().replace(" ", ", ");

        Label old = new Label(v1);
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

    @Override
    public List<String> validate() {
        return validateYears();
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

        ListGridField date = new ListGridField("date", MSG.year());
        date.setType(ListGridFieldType.TEXT);
        date.setWidth(100);

        ListGridField descr =
            new ListGridField("description", MSG.description());
        descr.setType(ListGridFieldType.TEXT);
        descr.setWidth("*");

        String baseUrl = GWT.getHostPageBaseURL();
        ListGridField pinFrom = new ListGridField(
            "fromIcon",  MESSAGES.selection());
        pinFrom.setWidth (60);
        pinFrom.setType (ListGridFieldType.ICON);
        pinFrom.setCellIcon(baseUrl + MESSAGES.markerGreen());

        pinFrom.addRecordClickHandler (new RecordClickHandler () {
            @Override
            public void onRecordClick (RecordClickEvent e) {
                Record r = e.getRecord();
                appendYear(r.getAttribute("date"));
            }
        });

        sedLoadTable.setFields(pinFrom, date, descr);
        return sedLoadTable;
    }

    public Canvas createWidget(DataList data) {
        VLayout layout = new VLayout();

        Label title = new Label(data.get(0).getDescription());
        title.setHeight("25px");

        DynamicForm form = new DynamicForm();
        form.setNumCols(4);
        yearsItem = new TextItem(MSG.years());
        yearsItem.setValidators(new IsIntegerValidator());
        form.setFields(yearsItem);

        layout.addMember(title);
        layout.addMember(form);

        return layout;
    }

    @Override
    protected Data[] getData() {
        validateYears();
        if (yearsItem != null && !yearsItem.getValueAsString().isEmpty()) {
            List<Data> data = new ArrayList<Data>();

            DataItem yearsdata = new DefaultDataItem(
                "years", "years", yearsItem.getValueAsString().trim());
            data.add(new DefaultData(
                "years",
                null,
                null,
                new DataItem[] { yearsdata }));

            return data.toArray(new Data[data.size()]);
        }
        return new Data[0];
    }

    protected List<String> validateYears() {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        if (yearsItem.getValueAsString() == null ||
                yearsItem.getValueAsString().trim().isEmpty()) {
            errors.add(MESSAGES.empty_filter());
            return errors;
        }

        String [] sValues = yearsItem.getValueAsString().trim().split(" ");
        String filtered = "";
        int goodValues = 0;
        for (String sValue: sValues) {
            int value;
            try {
                value = Integer.parseInt(sValue);
            } catch (NumberFormatException e) {
                errors.add(MESSAGES.wrongFormat() + ": " + sValue);
                continue;
            }
            boolean isGood = false;
            for (String validYear: validYears) {
                /* No list contains for strings? */
                if (sValue.equals(validYear)) {
                    isGood = true;
                    break;
                }
            }
            if (!isGood) {
                String tmp = MESSAGES.no_data_for_year();
                tmp = tmp.replace("$1", sValue);
                errors.add(tmp);
                continue;
            }
            goodValues++;
            if (goodValues > 1) {
                filtered += " " + Integer.toString(value);
            } else {
                filtered = Integer.toString(value);
            }
        }

        return errors;
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

    protected void appendYear (String year) {
        String oldYears = yearsItem.getValueAsString();
        if (oldYears != null && !oldYears.isEmpty()) {
            yearsItem.setValue(oldYears.trim() + " " + year);
        } else {
            yearsItem.setValue(year);
        }
    }
}
