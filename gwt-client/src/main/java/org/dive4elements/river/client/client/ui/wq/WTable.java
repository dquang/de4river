/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.wq;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.dive4elements.river.client.client.FLYSConstants;

/**
 * Table showing W main values.
 * TODO becomes very similiar to QDTable. Probably mergeable.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WTable extends ListGrid {

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);


    public WTable() {
        String baseUrl = GWT.getHostPageBaseURL();

        setWidth100();
        setHeight100();
        setSelectionType(SelectionStyle.NONE);
        setSelectionType(SelectionStyle.NONE);
        setShowHeaderContextMenu(false);
        setShowRecordComponents(true);
        setShowRecordComponentsByCell(true);
        setEmptyMessage(MESSAGE.empty_table());

        ListGridField name = new ListGridField("name", MESSAGE.name());
        name.setType(ListGridFieldType.TEXT);
        name.setWidth("*");

        ListGridField type = new ListGridField("type", MESSAGE.type());
        type.setType(ListGridFieldType.TEXT);
        type.setWidth("50");

        ListGridField startTime = createYearListGridField(
            "starttime", MESSAGE.starttime());

        ListGridField stopTime = createYearListGridField(
            "stoptime", MESSAGE.stoptime());

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

        ListGridField select = new ListGridField("select", MESSAGE.selection());
        select.setType(ListGridFieldType.ICON);
        select.setWidth(60);
        select.setCellIcon(baseUrl + MESSAGE.markerGreen());

        setFields(select, name, startTime, stopTime, type, value);
        hideField("select");
    }

    public void showSelect() {
        showField("select");
    }

    public static ListGridField createYearListGridField(
        final String propertyName, String displayName) {
        ListGridField listGridField = new ListGridField(
            propertyName, displayName);
        listGridField.setType(ListGridFieldType.DATE);
        listGridField.setWidth("50");
        listGridField.setCellFormatter(createYearDateFormatter(propertyName));
        return listGridField;
    }

    /** Create CellFormatter that prints just the year of a date
     * stored in attributeName. */
    private static CellFormatter createYearDateFormatter(
        final String attributeName
    ) {
        return new CellFormatter() {
            @Override
            public String format(
                Object arg0, ListGridRecord record, int arg2, int arg3) {
                Date date = record.getAttributeAsDate(attributeName);
                if (date == null) {
                    return "";
                }
                DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy");
                return fmt.format(date);
            }
        };
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
