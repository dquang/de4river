/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.fixation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.DateRangeItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.FixingsOverviewInfo.FixEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This UIProvider creates a panel for location or distance input.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixPeriodPanel
extends      FixationPanel
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    DateRangeItem inputPanel;

    long start;
    long end;

    protected String startName;
    protected String endName;

    public FixPeriodPanel() {
        this("start", "end");
    }

    public FixPeriodPanel(String startName, String endName) {
        this.startName = startName;
        this.endName   = endName;
        htmlOverview = "";
    }

    @Override
    public Canvas createWidget(DataList data) {
        instances.put(this.artifact.getUuid(), this);

        VLayout layout = new VLayout();

        Label title = new Label(data.get(0).getDescription());
        title.setHeight("25px");

        DynamicForm form = new DynamicForm();
        inputPanel = new DateRangeItem();
        inputPanel.setToTitle(MESSAGES.to());
        inputPanel.setFromTitle(MESSAGES.from());
        inputPanel.setShowTitle(false);
        form.setFields(inputPanel);

        layout.addMember(title);
        layout.addMember(form);

        return layout;
    }

    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data start = getData(items, startName);
        Data end   = getData(items, endName);
        DataItem[] startItem = start.getItems();
        DataItem[] endItem = end.getItems();

        String v1 = startItem[0].getStringValue();
        String v2 = endItem[0].getStringValue();

        long v1l = 0;
        long v2l = 0;
        try {
            v1l = Long.parseLong(v1);
            v2l = Long.parseLong(v2);
        }
        catch(NumberFormatException nfe) {
            GWT.log(nfe.toString());
        }
        Date d1 = new Date(v1l);
        Date d2 = new Date(v2l);

        DateTimeFormat f =
            DateTimeFormat.getFormat(
                DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
        StringBuilder sb = new StringBuilder();
        sb.append(f.format(d1) + " - ");
        sb.append(f.format(d2));

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

        boolean valid = saveDateValues();
        if(valid) {
            String start = Long.valueOf(this.start).toString();
            String end   = Long.valueOf(this.end).toString();
            DataItem startItem = new DefaultDataItem(
                startName, startName, start);
            DataItem endItem   = new DefaultDataItem(endName, endName, end);
            data.add(new DefaultData(
                startName,
                null,
                null,
                new DataItem[] { startItem }));
            data.add(new DefaultData(
                endName,
                null,
                null,
                new DataItem[] { endItem }));
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
    public void success() {
        List<FixEvent> list = fixInfo.getEvents();

        // The date in FixEvent is always "de" locale, so it seems...
        DateTimeFormat df = DateTimeFormat.getFormat("dd.MM.yyyy");

        if (!setFromAndToDate(list, df)) {
            GWT.log("FixPeriodPanel::success(): could not set "
                + "from and to dates!");
        }
    }

    protected boolean setFromAndToDate(List<FixEvent> list, DateTimeFormat df) {
        try {
            setFromDate(list.get(0).getDate(), df);
            setToDate(list.get(list.size() - 1).getDate(), df);
            return true;
        }
        catch(IllegalArgumentException ex) {
            GWT.log("FixPeriodPanel::setFromAndToDate(): " + ex.toString());
            return false;
        }
    }

    protected void setFromDate(String date, DateTimeFormat df)
        throws IllegalArgumentException
    {
        Date from = df.parse(date);
        this.inputPanel.setFromDate(from);
    }

    protected void setToDate(String date, DateTimeFormat df)
        throws IllegalArgumentException
    {
        Date to = df.parse(date);
        this.inputPanel.setToDate(to);
    }

    protected boolean saveDateValues() {
        Date st = inputPanel.getValue().getStartDate();
        Date en = inputPanel.getValue().getEndDate();
        if (st == null || en == null) {
            SC.warn(MESSAGES.error_wrong_date());
            return false;
        }

        long start = st.getTime();
        long end = en.getTime();

        if (start <= end) {
            this.start = start;
            this.end = end;
            return true;
        }
        return false;
    }
}
