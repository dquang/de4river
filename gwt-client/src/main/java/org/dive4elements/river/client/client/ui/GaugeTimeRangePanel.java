/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.DateRangeItem;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.ui.range.DischargeInfoDataSource;
import org.dive4elements.river.client.client.widgets.DischargeTablesChart;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.LongRangeData;
import org.dive4elements.river.client.shared.model.RangeData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class GaugeTimeRangePanel extends RangePanel {

    private static final long serialVersionUID = -157571967010594739L;

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected ListGrid yearTable;

    protected DateRangeItem dateRange;

    protected Long maxUpper;
    protected Long maxLower;


    public GaugeTimeRangePanel() {
        GWT.log("Creating YearInputPanel");
        yearTable = new ListGrid();
        yearTable.setAutoFetchData(true);
        yearTable.setShowHeaderContextMenu(false);
    }

    protected void setMaxUpper(DataList dataList) {
        LongRangeData range = (LongRangeData) dataList.get(0);
        setMaxUpper((Long) range.getUpper());
    }

    protected void setMaxUpper(Long maxUpper) {
        this.maxUpper = maxUpper;
    }

    protected void setMaxLower(DataList dataList) {
        LongRangeData range = (LongRangeData) dataList.get(0);
        setMaxLower((Long) range.getLower());
    }

    protected void setMaxLower(Long maxLower) {
        this.maxLower = maxLower;
    }

    @Override
    public Canvas create(DataList data) {
        setDataName(data);

        VLayout root = new VLayout();

        root.addMember(createLabel(data));
        root.addMember(createForm(data));
        root.addMember(getNextButton());

        initDefaults(data);

        initYearTable();

        long gauge = getGaugeNumber();

        Config config = Config.getInstance();
        String url = config.getServerUrl();
        String river = artifact.getArtifactDescription().getRiver();
        yearTable.setDataSource(new DischargeInfoDataSource(url, gauge, river));

        helperContainer.addMember(createHelperPanel());

        setMaxUpper(data);
        setMaxLower(data);

        return root;
    }


    protected Canvas createHelperPanel() {
        TabSet tabs = new TabSet();

        Tab table = new Tab(MSG.discharge_timeranges());
        Tab chart = new Tab(MSG.discharge_chart());

        table.setPane(yearTable);
        chart.setPane(new DischargeTablesChart(artifact));

        tabs.addTab(table, 0);
        tabs.addTab(chart, 1);

        return tabs;
    }


    /** Put defaults in form. */
    @Override
    protected void initDefaults(DataList dataList) {
        RangeData data = findRangeData(dataList);

        if (data != null) {
            dateRange.setFromDate(new Date((Long)data.getLower()));
        }

        dateRange.setToDate(new Date());
    }


    @Override
    public Canvas createOld(DataList dataList) {
        GWT.log("create old date.");
        Data     data  = dataList.get(0);
        HLayout layout = new HLayout();

        Label label = new Label(dataList.getLabel());
        label.setWidth(200);
        label.setHeight(20);

        Date dl = new Date((Long)((LongRangeData)data).getLower());
        Date du = new Date((Long)((LongRangeData)data).getUpper());

        @SuppressWarnings("deprecation")
        DateTimeFormat dtf = DateTimeFormat.getMediumDateFormat();
        Label value = new Label(dtf.format(dl) + " - " + dtf.format(du));
        value.setHeight(20);

        layout.addMember(label);
        layout.addMember(value);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }


    @Override
    protected Data[] getData() {
        long lo = getLowerAsLong();
        long up = getUpperAsLong();

        return new Data[] { new LongRangeData(getDataName(), null, lo, up) };
    }


    @Override
    protected Canvas createForm(DataList dataList) {
        HLayout layout = new HLayout();
        DynamicForm form = new DynamicForm();
        dateRange = new DateRangeItem();
        dateRange.setToTitle(MESSAGES.to());
        dateRange.setFromTitle(MESSAGES.from());
        dateRange.setShowTitle(false);
        form.setFields(dateRange);

        layout.addMember(form);
        return layout;

    }


    @Override
    public Object getMaxLower() {
        return maxLower;
    }


    protected long getLowerAsLong() {
        Date d = dateRange.getValue().getStartDate();
        return d.getTime();
    }


    protected long getUpperAsLong() {
        Date d = dateRange.getValue().getEndDate();
        return d.getTime();
    }


    @Override
    public Object getMaxUpper() {
        Date d = dateRange.getValue().getEndDate();
        return new Long(d.getTime());
    }


    @Override
    public void setLower(String lower) {
        try {
            long value = Long.valueOf(lower);
            dateRange.setFromDate(new Date(value));
        }
        catch (NumberFormatException nfe) {
            GWT.log("could not parse lower date.");
            SC.warn(MESSAGES.warning_cannot_parse_date());
        }
    }


    @Override
    public void setUpper(String upper) {
        try {
            long value = Long.valueOf(upper);
            dateRange.setToDate(new Date(value));
        }
        catch (NumberFormatException nfe) {
            GWT.log("could not parse upper date.");
            SC.warn(MESSAGES.warning_cannot_parse_date());
        }
    }


    protected String buildDateString(String raw) {
        if (raw == null || raw.length() == 0) {
            return "";
        }

        long value = Long.valueOf(raw);
        Date date = new Date(value);
        @SuppressWarnings("deprecation")
        DateTimeFormat dtf = DateTimeFormat.getMediumDateFormat();

        return dtf.format(date);
    }


    protected ListGrid initYearTable() {
        String baseUrl = GWT.getHostPageBaseURL();

        yearTable.setWidth100();
        yearTable.setHeight100();
        yearTable.setShowRecordComponents(true);
        yearTable.setShowRecordComponentsByCell(true);
        yearTable.setEmptyMessage(MESSAGES.empty_filter());
        yearTable.setCanReorderFields(false);

        CellFormatter cf = new CellFormatter() {
            @Override
            public String format(
                Object value,
                ListGridRecord record,
                int rowNum, int colNum
            ) {
                    if (value == null) {
                        return null;
                    }
                    else if (value.toString().equals("-1")) {
                        return "";
                    }
                    else if (colNum == 4 || colNum == 5) {
                        return buildDateString(value.toString());
                    }
                    else {
                    return value.toString();
                    }
                }
        };


        ListGridField addstart = new ListGridField (
            "addstart", MESSAGES.from());
        addstart.setType (ListGridFieldType.ICON);
        addstart.setWidth (30);
        addstart.setCellIcon(baseUrl + MESSAGES.markerGreen());
        addstart.addRecordClickHandler(new RecordClickHandler() {
            @Override
            public void onRecordClick(RecordClickEvent e) {
                Record r = e.getRecord();
                if (r.getAttribute("start").equals("-1")) {
                    return;
                }
                else {
                    setLower(r.getAttribute("start"));
                }
            }
        });

        ListGridField addend = new ListGridField ("addend", MESSAGES.to());
        addend.setType (ListGridFieldType.ICON);
        addend.setWidth (30);
        addend.setCellIcon(baseUrl + MESSAGES.markerRed());
        addend.addRecordClickHandler(new RecordClickHandler() {
            @Override
            public void onRecordClick(RecordClickEvent e) {
                Record r = e.getRecord();
                if (r.getAttribute("end").equals("-1")) {
                    return;
                }
                else {
                    setUpper(r.getAttribute("end"));
                }
            }
        });

        ListGridField desc =
            new ListGridField(
                "description", MESSAGES.discharge_curve_gaugeless());
        desc.setType(ListGridFieldType.TEXT);
        desc.setWidth("*");

        ListGridField bfgid =
            new ListGridField("bfg-id", MESSAGES.bfg_id());
        bfgid.setType(ListGridFieldType.TEXT);
        bfgid.setWidth(50);

        ListGridField start =
            new ListGridField("start", MESSAGES.start_year());
        start.setType(ListGridFieldType.INTEGER);
        start.setWidth(75);
        start.setCellFormatter(cf);

        ListGridField end =
            new ListGridField("end", MESSAGES.end_year());
        end.setType(ListGridFieldType.INTEGER);
        end.setWidth(75);
        end.setCellFormatter(cf);

        yearTable.setFields(addstart, addend, desc, bfgid, start, end);

        return yearTable;
    }


    protected long getGaugeNumber() {
        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        String gauge = "";
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                DataList dl = data[i];
                if (dl.getState().equals(
                        "state.winfo.historicalq.reference_gauge")
                ) {
                    for (int j = 0; j < dl.size(); j++) {
                        Data d = dl.get(j);
                        DataItem[] di = d.getItems();
                        if (di != null && di.length == 1) {
                           gauge = d.getItems()[0].getStringValue();
                        }
                    }
                }
            }
        }
        try {
            return Long.parseLong(gauge);
        }
        catch (NumberFormatException nfe) {
            GWT.log("Error parsing gauge.");
            return 0;
        }
    }


    /** Return List of error messages, if not validated. */
    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        Date from = dateRange.getValue().getStartDate();
        Date to   = dateRange.getValue().getEndDate();

        if (from == null || to == null) {
            String msg = MSG.error_validate_date_range();
            errors.add(msg);
        }

        long maxLow = (Long) getMaxLower();
        long maxUpper = (Long) getMaxUpper();
        long inLow = from.getTime();
        long inUpper = to.getTime();

        if (inLow < maxLow) {
            errors.add(MSG.error_validate_date_range_invalid());
        }
        else if (inUpper > maxUpper) {
            errors.add(MSG.error_validate_date_range_invalid());
        }
        else if (inLow > inUpper) {
            errors.add(MSG.error_validate_date_range_invalid());
        }

        return errors;
    }
}
