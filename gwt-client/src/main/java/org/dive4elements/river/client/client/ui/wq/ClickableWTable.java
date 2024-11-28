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
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;

import org.dive4elements.river.client.client.FLYSConstants;


public class ClickableWTable extends ListGrid {

    public static enum ClickMode {
        NONE, SINGLE, RANGE
    }

    private boolean useWaterlevelLabel = false;

    public static interface WClickedListener {

        void clickedLower(double value);

        void clickedUpper(double value);
    }

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);

    private WClickedListener wClickedListener;
    private ClickMode clickMode;

    public ClickableWTable() {
        this.clickMode = ClickMode.NONE;
        init();
    }

    public ClickableWTable(WClickedListener lowerListener,
        ClickMode selectionMode, boolean alternativeLabel) {
        this.wClickedListener = lowerListener;
        this.clickMode = selectionMode;
        this.useWaterlevelLabel = alternativeLabel;
        init();
    }

    private void init() {
        setWidth100();
        setHeight100();
        setSelectionType(SelectionStyle.NONE);
        setSelectionType(SelectionStyle.NONE);
        setShowHeaderContextMenu(false);
        setShowRecordComponents(true);
        setShowRecordComponentsByCell(true);
        setEmptyMessage(MESSAGE.empty_table());

        ListGridField name = new ListGridField("name",
            useWaterlevelLabel
            ? MESSAGE.wq_waterlevel_label()
            : MESSAGE.name() );
        name.setType(ListGridFieldType.TEXT);
        name.setWidth("*");

        ListGridField type = new ListGridField("type", MESSAGE.type());
        type.setType(ListGridFieldType.TEXT);
        type.setWidth("50");

        final NumberFormat nf = NumberFormat.getDecimalFormat();

        ListGridField value = new ListGridField("value", MESSAGE.wq_value_w());
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
        ListGridField lower = new ListGridField("selection",
            MESSAGE.selection());
        lower.setType(ListGridFieldType.ICON);
        lower.setWidth("65");
        lower.setCellIcon(GWT.getHostPageBaseURL() + MESSAGE.markerGreen());
        addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                if (event.getColNum() == 0) {
                    Record r = event.getRecord();
                    double val = r.getAttributeAsDouble("value");
                    fireLowerClickEvent(val);
                }
            }
        });

        setFields(lower, name, type, value);
    }

    private void initRangeClickMode(ListGridField name, ListGridField type,
        ListGridField value) {
        ListGridField lower = new ListGridField("lower", MESSAGE.lower());
        lower.setType(ListGridFieldType.ICON);
        lower.setWidth("50");
        lower.setCellIcon(GWT.getHostPageBaseURL() + MESSAGE.markerRed());
        addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                if (event.getColNum() == 0) {
                    Record r = event.getRecord();
                    double val = r.getAttributeAsDouble("value");
                    fireLowerClickEvent(val);
                }
            }
        });

        ListGridField upper = new ListGridField("upper", MESSAGE.upper());
        upper.setType(ListGridFieldType.ICON);
        upper.setWidth("50");
        upper.setCellIcon(GWT.getHostPageBaseURL() + MESSAGE.markerGreen());
        addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                if (event.getColNum() == 1) {
                    Record r = event.getRecord();
                    double val = r.getAttributeAsDouble("value");
                    fireUpperClickEvent(val);
                }
            }
        });

        setFields(lower, upper, name, type, value);
    }

    private void fireLowerClickEvent(double value) {
        if (wClickedListener != null) {
            wClickedListener.clickedLower(value);
        }
    }

    private void fireUpperClickEvent(double value) {
        if (wClickedListener != null) {
            wClickedListener.clickedUpper(value);
        }
    }
}
