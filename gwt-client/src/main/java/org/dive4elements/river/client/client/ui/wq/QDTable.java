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

import org.dive4elements.river.client.client.FLYSConstants;


/**
 * Table showing Q and D main values, allowing for selection, if
 * showSelect is called. In that case, a CellClickHandler should
 * be registered.
 *
 * TODO becomes very similiar to WTable. Probably mergeable.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class QDTable extends ListGrid {

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);


    protected boolean lockClick;

    public QDTable() {
        String baseUrl = GWT.getHostPageBaseURL();

        setWidth100();
        setHeight100();
        setSelectionType(SelectionStyle.SINGLE);
        setSelectionType(SelectionStyle.SINGLE);
        setShowHeaderContextMenu(false);
        setShowRecordComponents(true);
        setShowRecordComponentsByCell(true);
        setEmptyMessage(MESSAGE.empty_table());

        ListGridField addMax = new ListGridField("max", MESSAGE.from());
        addMax.setType(ListGridFieldType.ICON);
        addMax.setWidth(30);
        addMax.setCellIcon(baseUrl + MESSAGE.markerRed());

        ListGridField addMin = new ListGridField("min", MESSAGE.to());
        addMin.setType(ListGridFieldType.ICON);
        addMin.setWidth(30);
        addMin.setCellIcon(baseUrl + MESSAGE.markerGreen());

        ListGridField select = new ListGridField("select", MESSAGE.selection());
        select.setType(ListGridFieldType.ICON);
        select.setWidth(70);
        select.setCellIcon(baseUrl + MESSAGE.markerGreen());

        ListGridField name = new ListGridField("name", MESSAGE.discharge());
        name.setType(ListGridFieldType.TEXT);
        name.setWidth("*");

        ListGridField type = new ListGridField("type", MESSAGE.type());
        type.setType(ListGridFieldType.TEXT);
        type.setWidth("10%");

        ListGridField startTime = WTable.createYearListGridField(
            "starttime", MESSAGE.starttime());

        ListGridField stopTime = WTable.createYearListGridField(
            "stoptime", MESSAGE.stoptime());

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
        value.setWidth("15%");

        ListGridField official = new ListGridField(
            "official", MESSAGE.official_regulation());
        official.setType(ListGridFieldType.TEXT);
        official.setWidth("25%");

        setFields(addMax, addMin, select, name,
            startTime, stopTime, type, value, official);
    }

    public void hideIconFields () {
        hideField("max");
        hideField("min");
        hideField("select");
        lockClick = true;
    }


    public void showIconFields() {
        showField("max");
        showField("min");
        hideField("select");
        lockClick = false;
    }

    public void showSelect() {
        showField("select");
        hideField("max");
        hideField("min");
    }

    /** Whether or not can be clicked on. */
    public boolean isLocked() {
        return lockClick;
    }

    /**
     * Search all records for one with attribute name equals to given name.
     * @return null if none found.
     * */
    public Double findRecordValue(String name) {
        for (ListGridRecord record : getRecords()) {
            if (record.getAttribute("name").equals(name)) {
                return record.getAttributeAsDouble("value");
            }
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
