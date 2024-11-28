/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.CellEditValueFormatter;
import com.smartgwt.client.widgets.grid.CellEditValueParser;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItem;
import org.dive4elements.river.client.shared.model.Property;
import org.dive4elements.river.client.shared.model.PropertyGroup;
import org.dive4elements.river.client.shared.model.Settings;
import org.dive4elements.river.client.shared.model.StringProperty;

import java.util.Date;
import java.util.List;

/**
 * UI to enter point data and save it to an PointArtifact.
 */
public class ManualDatePointsEditor
extends      ManualPointsEditor
{

    public ManualDatePointsEditor(Collection collection,
        RedrawRequestHandler handler, String outputModeName
    ) {
        super (collection, handler, outputModeName);
    }


    /** Create and setup/add the ui. */
    @Override
    public void createUI() {
        Button accept = new Button(MSG.label_ok());
        Button cancel = new Button(MSG.label_cancel());
        cancel.addClickHandler(this);

        accept.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                okClicked();
            }
        });

        HLayout buttons = new HLayout();
        buttons.addMember(accept);
        buttons.addMember(cancel);
        buttons.setAlign(Alignment.CENTER);
        buttons.setHeight(30);

        VLayout layout = new VLayout();
        listGrid = new ListGrid();
        listGrid.setWidth100();
        listGrid.setHeight("*");
        listGrid.setCanSort(false);
        listGrid.setCanEdit(true);
        listGrid.setShowHeaderContextMenu(false);

        CellFormatter doubleFormat = new CellFormatter() {
            @Override
            public String format(
                Object value,
                ListGridRecord record,
                int rowNum,
                int colNum
            ) {
                if(value != null) {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    try {
                        double d = Double.valueOf(
                            value.toString()).doubleValue();
                        return nf.format(d);
                    } catch (Exception e) {
                        return value.toString();
                    }
                } else {
                   return null;
                }
            }};

        CellFormatter dateFormat = new CellFormatter() {
            @Override
            public String format(
                Object value,
                ListGridRecord record,
                int rowNum,
                int colNum
            ) {
                if(value != null && !value.toString().equals("")) {
                    try {
                        DateTimeFormat df =
                            DateTimeFormat.getFormat("dd.MM.yyyy");
                        Date d = df.parse(value.toString());
                        DateTimeFormat df2 =
                            DateTimeFormat.getFormat(
                                DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
                        return df2.format(d);

                    }
                    catch(IllegalArgumentException iae) {
                        SC.warn(MSG.error_invalid_date());
                        record.setAttribute(DatePointRecord.ATTRIBUTE_X, "");
                        return "";
                    }
                }
                else {
                   return null;
                }
            }};


        CellEditValueParser cevp = new CellEditValueParser() {
            @Override
            public Object parse(
                Object value, ListGridRecord record, int rowNum, int colNum) {
                if (value == null)
                    return null;
                try {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    double d = nf.parse(value.toString());
                    return (new Double(d)).toString();
                }
                catch(NumberFormatException nfe) {
                    return value;
                }
            }
        };

        CellEditValueFormatter cevf = new CellEditValueFormatter() {
            @Override
            public Object format(
                Object value, ListGridRecord record, int rowNum, int colNum) {
                if (value != null) {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    try {
                        double d = Double.valueOf(
                            value.toString()).doubleValue();
                        return nf.format(d);
                    } catch (Exception e) {
                        return value.toString();
                    }
                }
                return null;
            }
        };

        // Use X and Y as default fallback.
        String xAxis = "X";
        String yAxis = "Y";

        // Get header text from collection settings.
        Settings settings = this.collection.getSettings(outputModeName);
        List<Property> axes = settings.getSettings("axes");
        if(axes != null) {
            for (Property p: axes) {
                PropertyGroup pg = (PropertyGroup)p;
                StringProperty id =
                    (StringProperty)pg.getPropertyByName("id");
                if(id.getValue().equals("X")) {
                    StringProperty name =
                        (StringProperty)pg.getPropertyByName("label");
                    xAxis = name.getValue();
                }
                else if (yAxis.equals("Y")) {
                    yAxis = MSG.manual_date_points_y();
                }
            }
        }
        ListGridField xField =
            new ListGridField(PointRecord.ATTRIBUTE_X, xAxis);
        xField.setType(ListGridFieldType.TEXT);
        xField.setCellFormatter(dateFormat);

        ListGridField yField =
            new ListGridField(PointRecord.ATTRIBUTE_Y, yAxis);
        yField.setType(ListGridFieldType.FLOAT);
        yField.setCellFormatter(doubleFormat);
        yField.setEditValueParser(cevp);
        yField.setEditValueFormatter(cevf);

        ListGridField nameField = new ListGridField(PointRecord.ATTRIBUTE_NAME,
            MSG.pointname());
        final ListGridField removeField  =
            new ListGridField("_removeRecord", MSG.removepoint()){{
                setType(ListGridFieldType.ICON);
                setIcon(GWT.getHostPageBaseURL() + MSG.removeFeature());
                setCanEdit(false);
                setCanFilter(false);
                setCanSort(false);
                setCanGroupBy(false);
                setCanFreeze(false);
                setWidth(25);
        }};

        ListGridField activeField = new ListGridField(
            PointRecord.ATTRIBUTE_ACTIVE, MSG.selection());
        activeField.setType(ListGridFieldType.BOOLEAN);
        activeField.setDefaultValue(true);

        listGrid.setFields(new ListGridField[] {activeField, xField, yField,
            nameField, removeField});

        listGrid.addRecordClickHandler(new RecordClickHandler() {
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

        // Find the artifacts uuid.
        findManualPointsUUID();
        CollectionItem item = collection.getItem(uuid);

        // Add points to grid.
        if (item != null) {
            String jsonData = item.getData().get(
                outputModeName + "." + POINT_DATA);
            JSONArray jsonArray = (JSONArray) JSONParser.parse(jsonData);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray point = (JSONArray) jsonArray.get(i);
                listGrid.addData(datePointRecordFromJSON(point));
            }
        }
        else {
            GWT.log("No item found for " + uuid);
        }

        IButton button = new IButton(MSG.newpoint());
        button.setTop(250);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listGrid.startEditingNew();
            }
        });

        layout.addMember(listGrid);
        layout.addMember(button);

        addItem(layout);

        addItem(buttons);
        setWidth(380);
        setHeight(470);
        centerInPage();
    }


    /** Create JSON representation of the points present in the list grid. */
    @Override
    protected JSONArray jsonArrayFromListGrid() {
        JSONArray list = new JSONArray();
        int idx = 0;

        for(ListGridRecord record : listGrid.getRecords()) {
            if (record instanceof DatePointRecord) {
                JSONArray data = new JSONArray();

                DatePointRecord point = (DatePointRecord) record;
                String dateString = point.getX();
                DateTimeFormat df = DateTimeFormat.getFormat(
                    DateTimeFormat.PredefinedFormat.DATE_MEDIUM);

                Date d = df.parse(dateString);
                double dv = d.getTime();

                data.set(0, new JSONNumber(dv));
                data.set(1, new JSONNumber(point.getY()));
                data.set(2, new JSONString(point.getName()));
                data.set(3, JSONBoolean.getInstance(point.isActive()));

                list.set(idx, data);
                idx++;
            }
            else {
                JSONArray data = new JSONArray();

                String nameString = record.getAttributeAsString(
                    PointRecord.ATTRIBUTE_NAME);
                // Apply default name if none set.
                if (nameString == null || nameString.equals("")) {
                    String xString = record.getAttributeAsString(
                        PointRecord.ATTRIBUTE_X);
                    String yString = record.getAttributeAsString(
                        PointRecord.ATTRIBUTE_Y);
                    nameString = xString + "/" + yString;
                }

                String dateString = record.getAttributeAsString(
                    PointRecord.ATTRIBUTE_X);
                DateTimeFormat df = DateTimeFormat.getFormat(
                    DateTimeFormat.PredefinedFormat.DATE_MEDIUM);

                Date d = df.parse(dateString);
                double dv = d.getTime();
                data.set(0, new JSONNumber(dv));
                data.set(1, new JSONNumber(record.
                    getAttributeAsDouble(PointRecord.ATTRIBUTE_Y)));
                data.set(2, new JSONString(nameString));
                data.set(3, JSONBoolean.getInstance(
                        record.getAttributeAsBoolean(
                            PointRecord.ATTRIBUTE_ACTIVE)));

                list.set(idx, data);
                idx++;
            }
        }
        return list;
    }

    /** From a JSON-encoded point, create a PointRecord. */
    public DatePointRecord datePointRecordFromJSON(JSONArray jsonArray) {
        JSONNumber  x = (JSONNumber)  jsonArray.get(0);
        JSONNumber  y = (JSONNumber)  jsonArray.get(1);
        JSONString  s = (JSONString)  jsonArray.get(2);
        JSONBoolean b = (JSONBoolean) jsonArray.get(3);

        Date d = new Date (Long.valueOf(x.toString()).longValue());
        DateTimeFormat df = DateTimeFormat.getFormat(
            DateTimeFormat.PredefinedFormat.DATE_MEDIUM);

        return new DatePointRecord(b.booleanValue(), df.format(d),
            y.doubleValue(), s.stringValue());
    }


    /** Return false if x or y attribute is missing. */
    @Override
    protected boolean isDialogValid() {
        boolean valid = true;
        for (ListGridRecord record : listGrid.getRecords()) {
            if (record.getAttributeAsString(PointRecord.ATTRIBUTE_X) == null ||
                record.getAttributeAsString(
                    DatePointRecord.ATTRIBUTE_X).equals("") ||
                record.getAttributeAsDouble(PointRecord.ATTRIBUTE_Y) == null) {
                return false;
            }
        }
        if (listGrid.hasErrors()) {
            valid = false;
        }
        return valid;
    }


    /** Simple record to store points. */
    public class DatePointRecord extends ListGridRecord {
        protected static final String ATTRIBUTE_X = "X";
        protected static final String ATTRIBUTE_Y = "Y";
        protected static final String ATTRIBUTE_NAME = "name";
        protected static final String ATTRIBUTE_ACTIVE = "active";

        private DatePointRecord() {;}

        public DatePointRecord(boolean b, String x, double y, String name) {
            setActive(b);
            setName(name);
            setX(x);
            setY(y);
        }

        public void setActive(boolean b) {
            setAttribute(ATTRIBUTE_ACTIVE, b);
        }

        public boolean isActive() {
            return getAttributeAsBoolean(ATTRIBUTE_ACTIVE);
        }

        public void setName(String name) {
            setAttribute(ATTRIBUTE_NAME, name);
        }

        public String getName() {
            return getAttributeAsString(ATTRIBUTE_NAME);
        }

        public void setX(String x) {
            setAttribute(ATTRIBUTE_X, x);
        }

        public void setY(double y) {
            setAttribute(ATTRIBUTE_Y, y);
        }

        public String getX() {
            return getAttributeAsString(ATTRIBUTE_X);
        }

        public double getY() {
            return getAttributeAsDouble(ATTRIBUTE_Y);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

