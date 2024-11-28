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

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This UIProvider creates a panel for location or distance input.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixMultiPeriodPanel
extends      FixPeriodPanel
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected ListGrid elements;

    protected String values;

    public FixMultiPeriodPanel() {
        this("", "");
    }

    public FixMultiPeriodPanel(String startName, String endName) {
        super(startName, endName);
    }

    @Override
    public Canvas createWidget(DataList data) {
        HLayout input = new HLayout();
        VLayout root = new VLayout();
        VLayout grid = new VLayout();
        VLayout layout = (VLayout) super.createWidget(data);
        Button add = new Button(MESSAGES.add());
        elements = new ListGrid();

        add.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent ce) {
                Date f = inputPanel.getValue().getStartDate();
                Date t = inputPanel.getValue().getEndDate();
                if (f == null || t == null) {
                    return;
                }
                DateRangeRecord drr = new DateRangeRecord(f, t);
                elements.addData(drr);
            }
        });
        layout.addMember(add);

        Label sel = new Label("Selected");
        sel.setHeight(25);
        elements.setWidth(185);
        elements.setHeight(120);
        elements.setShowHeaderContextMenu(false);
        elements.setCanReorderFields(false);
        elements.setCanSort(false);
        elements.setCanEdit(false);
        ListGridField from = new ListGridField("from", "From");
        ListGridField to = new ListGridField("to", "To");
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

        grid.addMember(sel);
        grid.addMember(elements);
        input.addMember(layout);
        input.addMember(grid);
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
        Data str = getData(items, "ana_data");
        DataItem[] strItems = str.getItems();

        String[] pairs = strItems[0].getLabel().split(";");
        for (int i = 0; i < pairs.length; i++) {
            String[] vals = pairs[i].split(",");
            try {
                long f = Long.valueOf(vals[0]).longValue();
                long t = Long.valueOf(vals[1]).longValue();
                Date from = new Date(f);
                Date to = new Date(t);
                String fromString =
                    DateTimeFormat.getMediumDateFormat().format(from);
                String toString =
                    DateTimeFormat.getMediumDateFormat().format(to);

                Label dateLabel = new Label(fromString + " - " + toString);
                dateLabel.setHeight(20);
                vLayout.addMember(dateLabel);
            }
            catch(NumberFormatException nfe) {
            }
        }
        Canvas back = getBackButton(dataList.getState());
        layout.addMember(label);
        layout.addMember(vLayout);
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
            DataItem item = new DefaultDataItem("ana_data", null, this.values);
            data.add(new DefaultData(
                        "ana_data",
                        null,
                        null,
                        new DataItem[] { item }));
        }
        return data.toArray(new Data[data.size()]);
    }


    @Override
    protected boolean saveDateValues() {
        ListGridRecord[] lgr = elements.getRecords();
        if (lgr.length == 0) {
            return false;
        }
        String data = "";
        for (int i = 0; i < lgr.length; i++) {
            DateRangeRecord drr = (DateRangeRecord) lgr[i];
            data += drr.getFrom() + "," + drr.getTo();
            data += ";";
        }
        values = data;
        return true;
    }


    protected static class DateRangeRecord extends ListGridRecord {
        protected Date from;
        protected Date to;

        protected final static String FROM_FIELD = "from";
        protected final static String TO_FIELD = "to";

        public DateRangeRecord (Date from, Date to) {
            setFrom(from);
            setTo(to);
        }

        public void setFrom(Date from) {
            this.from = from;
            setAttribute(
                FROM_FIELD,
                DateTimeFormat.getMediumDateFormat().format(from));
        }


        public void setTo(Date to) {
            this.to = to;
            setAttribute(
                TO_FIELD,
                DateTimeFormat.getMediumDateFormat().format(to));
        }


        public long getFrom() {
            return this.from.getTime();
        }


        public long getTo() {
            return this.to.getTime();
        }
    }
}
