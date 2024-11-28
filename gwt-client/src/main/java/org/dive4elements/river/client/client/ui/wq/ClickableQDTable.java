/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.wq;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;

import org.dive4elements.river.client.client.FLYSConstants;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ClickableQDTable extends ListGrid {

    public static enum ClickMode {
        NONE, SINGLE, RANGE
    }

    public static interface QClickedListener {

        void clickedLower(double value);

        void clickedUpper(double value);
    }

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);

    private QClickedListener qClickedListener;
    private ClickMode clickMode;

    protected boolean lockClick;

    public ClickableQDTable() {
        this.clickMode = ClickMode.NONE;
        init();
    }

    public ClickableQDTable(QClickedListener qClickedListener,
        ClickMode clickMode) {
        this.qClickedListener = qClickedListener;
        this.clickMode = clickMode;
        init();
    }

    private void init() {
        setWidth100();
        setHeight100();
        setSelectionType(SelectionStyle.SINGLE);
        setSelectionType(SelectionStyle.SINGLE);
        setShowHeaderContextMenu(false);
        setShowRecordComponents(true);
        setShowRecordComponentsByCell(true);
        setEmptyMessage(MESSAGE.empty_table());

        ListGridField name = new ListGridField("name", MESSAGE.discharge());
        name.setType(ListGridFieldType.TEXT);
        name.setWidth("*");

        ListGridField type = new ListGridField("type", MESSAGE.type());
        type.setType(ListGridFieldType.TEXT);
        type.setWidth("20%");

        final NumberFormat nf = NumberFormat.getDecimalFormat();

        ListGridField value = new ListGridField("value", MESSAGE.wq_value_q());
        value.setType(ListGridFieldType.FLOAT);
        value.setCellFormatter(new CellFormatter() {

            @Override
            public String format(Object v, ListGridRecord r, int row, int col) {
                if (v == null) {
                    return null;
                }

                try {
                    double value = Double.valueOf(v.toString());
                    return nf.format(value);
                }
                catch (NumberFormatException nfe) {
                    return v.toString();
                }
            }
        });
        value.setWidth("20%");

        switch (clickMode) {
        case NONE:
            setFields(name, type, value);
            break;
        case SINGLE:
            initSingleClickMode(name, type, value);
            break;
        case RANGE:
            initRangeClickMode(name, type, value);
            break;
        }
    }

    private void initSingleClickMode(ListGridField name, ListGridField type,
        ListGridField value) {
        ListGridField select = new ListGridField("select", MESSAGE.selection());
        select.setType(ListGridFieldType.ICON);
        select.setWidth(70);
        select.setCellIcon(GWT.getHostPageBaseURL() + MESSAGE.markerGreen());

        addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                if (event.getColNum() == 0) {
                    ListGridRecord r = event.getRecord();
                    fireLowerClickEvent(r.getAttributeAsDouble("value"));
                }
            }
        });

        setFields(select, name, type, value);
    }

    private void initRangeClickMode(ListGridField name, ListGridField type,
        ListGridField value) {
        ListGridField addMin = new ListGridField("min", MESSAGE.to());
        addMin.setType(ListGridFieldType.ICON);
        addMin.setWidth(30);
        addMin.setCellIcon(GWT.getHostPageBaseURL() + MESSAGE.markerGreen());

        ListGridField addMax = new ListGridField("max", MESSAGE.from());
        addMax.setType(ListGridFieldType.ICON);
        addMax.setWidth(30);
        addMax.setCellIcon(GWT.getHostPageBaseURL() + MESSAGE.markerRed());

        addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                if (event.getColNum() == 0) {
                    ListGridRecord r = event.getRecord();
                    fireLowerClickEvent(r.getAttributeAsDouble("value"));
                }

                if (event.getColNum() == 1) {
                    ListGridRecord r = event.getRecord();
                    fireUpperClickEvent(r.getAttributeAsDouble("value"));
                }
            }
        });

        setFields(addMin, addMax, name, type, value);
    }

    private void fireLowerClickEvent(double value) {
        if (qClickedListener != null) {
            qClickedListener.clickedLower(value);
        }
    }

    private void fireUpperClickEvent(double value) {
        if (qClickedListener != null) {
            qClickedListener.clickedUpper(value);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
