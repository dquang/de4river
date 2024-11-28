/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.IntegerOptionsData;
import org.dive4elements.river.client.shared.model.MultiAttributeData;
import org.dive4elements.river.client.shared.model.MultiDataItem;
import org.dive4elements.river.client.shared.model.StringOptionsData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Some parameters take the form of on/off options that can also be seen
 * as a matrix.
 *
 * This class helps to survive the underlying objects and create a visual
 * representation of this matrix. Later can happen in two ways to overcome
 * shortcomings of GWT/SmartGWT combination.
 */
public class ParameterMatrix {

    protected ListGrid listGrid = null;

    public static class Column implements Serializable {

        private static final long serialVersionUID = -3493426383086860118L;

        protected String              name;
        protected Map<String, String> values;

        private Column() {
            this.values = new HashMap<String, String>();
        }

        public Column(String name) {
            this();
            this.name = name;
        }

        public void addValue(String label, String value) {
            values.put(label, value);
        }

        public String getValue(String label) {
            return values.get(label);
        }
    } // end of class Column

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);

    public static final int CELL_HEIGHT = 25;

    private Map<String, Column> columns;
    private List<String>        columnNames;
    private List<String>        valueNames;
    private Map<String, List<String>> attributes;

    /** Maps column names to list of rows' first fields. */
    private Map<String, List<String>> selected;

    public ParameterMatrix() {
        super();
        this.columns     = new HashMap<String, Column>();
        this.columnNames = new ArrayList<String>();
        this.valueNames  = new ArrayList<String>();
        this.selected    = new HashMap<String, List<String>>();
        this.attributes  = new HashMap<String, List<String>>();
    }


    public void addColumn(IntegerOptionsData group) {
        String groupTitle = group.getLabel();

        Column     col   = new Column(groupTitle);
        DataItem[] items = group.getItems();

        if (items == null) {
            GWT.log("No items found in StringOptionsData '" + groupTitle + "'");
            return;
        }

        for (DataItem item: items) {
            String title = item.getLabel();

            if (valueNames.indexOf(title) < 0) {
                valueNames.add(title);
            }

            col.addValue(item.getLabel(), item.getStringValue());
        }

        columnNames.add(groupTitle);
        columns.put(groupTitle, col);
    }


    public List<String> getColumnNames() {
        return columnNames;
    }


    public void addColumn(StringOptionsData options) {
        String groupTitle = options.getLabel();

        Column     col   = new Column(groupTitle);
        DataItem[] items = options.getItems();

        if (items == null) {
            GWT.log("No items found in StringOptionsData '"
                + groupTitle + "'");
            return;
        }

        for (DataItem item: items) {
            String title = item.getLabel();

            if (valueNames.indexOf(title) < 0) {
                valueNames.add(title);
            }

            col.addValue(item.getLabel(), item.getStringValue());
        }

        columnNames.add(groupTitle);
        columns.put(groupTitle, col);
    }

    public void addColumn(MultiAttributeData options) {
        GWT.log("Add Columns for MultiAttribute data");
        String groupTitle = options.getLabel();

        Column     col   = new Column(groupTitle);
        DataItem[] items = options.getItems();

        if (items == null) {
            GWT.log("No items found in StringOptionsData '"
                + groupTitle + "'");
            return;
        }

        MultiDataItem mItem = (MultiDataItem)items[0];
        for (Map.Entry<String, String> entry: mItem.getValue().entrySet()) {
            if (entry.getKey().equals("art:value") ||
                entry.getKey().equals("art:label")) {
                continue;
            }
            attributes.put(entry.getKey(), new ArrayList<String>());
        }
        for (DataItem item: items) {
            GWT.log("multidataitem: " + item.getLabel());
            String title = item.getLabel();

            if (valueNames.indexOf(title) < 0) {
                valueNames.add(title);
            }
            MultiDataItem mi = (MultiDataItem)item;
            Map<String, String> vs = mi.getValue();
            for (Map.Entry<String, String>e: vs.entrySet()) {
                if (e.getKey().equals("art:value") ||
                    e.getKey().equals("art:label")) {
                    continue;
                }
                List<String> data = attributes.get(e.getKey());
                data.add(e.getValue());
            }
            col.addValue(item.getLabel(), mi.getValue().get("art:value"));
        }

        columnNames.add(groupTitle);
        columns.put(groupTitle, col);
    }

    public Widget createParameterGrid() {
        listGrid = new ListGrid();
        listGrid.setShowAllRecords(true);
        listGrid.setWrapCells(true);
        listGrid.setShowHeaderContextMenu(false);
        listGrid.setCanReorderFields(false);
//        listGrid.setCanSort(false);
        //listGrid.setAutoFitData(Autofit.VERTICAL);
        listGrid.setFixedRecordHeights(false);
        // TODO: Then also need "autofit" (when wrapping)

        ListGridField itemNameField = new ListGridField("itemname", " ");
        ArrayList<ListGridField> fields = new ArrayList<ListGridField>();
        fields.add(itemNameField);

        for (Map.Entry<String, List<String>> entry: attributes.entrySet()) {
            ListGridField attrField = new ListGridField(
                entry.getKey(), MESSAGE.getString(entry.getKey()));
            fields.add(attrField);
        }

        for (int i = 0, n = columnNames.size(); i < n; i++) {
            ListGridField field = new ListGridField(
                columnNames.get(i), MESSAGE.getString(columnNames.get(i)));
            field.setType(ListGridFieldType.BOOLEAN);
            field.setCanEdit(true);
            fields.add(field);
            selected.put(columnNames.get(i), new ArrayList<String>());
        }

        ListGridField[] fieldsArray = fields.toArray(
            new ListGridField[fields.size()]);
        listGrid.setFields(fieldsArray);

        int nVals = valueNames.size();

        ArrayList<ListGridRecord> records = new ArrayList<ListGridRecord>();
        for (int j = 0; j < nVals; j++) {
            String valueName  = valueNames.get(j);
            ListGridRecord record = new ListGridRecord();
            record.setAttribute("itemname", valueName);
            for (int i = 0, n = columnNames.size(); i < n; i++) {
                String columnName = columnNames.get(i);
                Column col        = columns.get(columnName);
                String value      = col.getValue(valueName);
                record.setAttribute(columnName, false);
                record.setAttribute(columnName+"-value", value);
            }
            for (Map.Entry<String, List<String>> entry: attributes.entrySet()) {
                record.setAttribute(entry.getKey(), entry.getValue().get(j));
            }
            records.add(record);
        }

        listGrid.setData(records.toArray(new ListGridRecord[records.size()]));

        return listGrid;

    }


    /**
     * Returns a widget with matrix of checkboxes and labels.
     * @param asListGrid if true, use a ListGrid (for inclusion in SmartGWT
     *                   containers, avoiding scrollbar-issues.
     */
    public Widget create(boolean asListGrid) {
        if (asListGrid) {
            return createParameterGrid();
        }
        Grid grid = new Grid(valueNames.size() + 1, columnNames.size() + 1);

        for (int i = 0, n = columnNames.size(); i < n; i++) {
            String columnName = columnNames.get(i);
            Column col        = columns.get(columnName);

            selected.put(columnName, new ArrayList<String>());

            grid.setWidget(0, i+1, createLabel(MESSAGE.getString(columnName)));

            for (int j = 0, o = valueNames.size(); j < o; j++) {
                String valueName = valueNames.get(j);
                String value     = col.getValue(valueName);

                if (i == 0) {
                    grid.setWidget(j+1, 0, createLabel(valueName));
                }

                if (value != null && value.length() > 0) {
                    grid.setWidget(j+1, i+1, createCheckBox(columnName, value));
                }
            }
        }

        return grid;
    }


    /** Creates label with given text. */
    protected Label createLabel(String text) {
        Label label = new Label(text);
        label.setHeight(CELL_HEIGHT);

        return label;
    }


    /** Create Checkbox for column/value. */
    protected Canvas createCheckBox(final String colName, final String value) {
        CheckBox box = new CheckBox();
        box.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Map<String, List<String>> selection = getSelection();

                List<String> values = selection.get(colName);
                if (values.indexOf(value) >= 0) {
                    values.remove(value);
                }
                else {
                    values.add(value);
                }
            }
        });

        Canvas c = new Canvas();
        c.addChild(box);
        return c;
    }


    public Map<String, List<String>> getSelection() {
        if (listGrid == null) {
            return selected;
        }

        ListGridRecord[] records = listGrid.getRecords();
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (ListGridRecord record : records) {
            for (int i = 0, n = columnNames.size(); i < n; i++) {
                String columnName = columnNames.get(i);
                if (Boolean.valueOf(record.getAttribute(columnName)) == true) {
                    if (result.containsKey(columnName)) {
                        result.get(columnName).add(
                            record.getAttribute(columnName + "-value"));
                    }
                    else {
                        List<String> items = new ArrayList<String>();
                        items.add(record.getAttribute(columnName + "-value"));
                        result.put(columnName, items);
                    }
                }
            }
        }
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
