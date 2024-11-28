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
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
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

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.RedrawRequestEvent;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.services.FeedServiceAsync;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItem;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DefaultArtifact;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.Property;
import org.dive4elements.river.client.shared.model.PropertyGroup;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Settings;
import org.dive4elements.river.client.shared.model.StringProperty;

import java.util.Date;
import java.util.List;


/**
 * UI to enter point data and save it to an PointArtifact.
 */
public class ManualPointsEditor
extends      Window
implements   ClickHandler
{
    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** Part of name of the main data item to be fed. */
    public static final String POINT_DATA = "manualpoints.data";

    /** When we chaged something, we need a RedrawRequest(Handler). */
    protected RedrawRequestHandler redrawRequestHandler;

    /** The collection */
    protected Collection collection;

    /** The listGrid showing point entries. */
    protected ListGrid listGrid;

    protected ListGridFieldType fieldTypeX = ListGridFieldType.FLOAT;

    /** Service handle to clone and add artifacts to collection. */
    LoadArtifactServiceAsync loadArtifactService = GWT.create(
        org.dive4elements.river.client.client.services
        .LoadArtifactService.class);

    /** Service to feed the artifact with new point-data. */
    FeedServiceAsync feedService = GWT.create(
        org.dive4elements.river.client.client.services.FeedService.class);

    /** UUID of artifact to feed. */
    protected String uuid;

    /** Name of the outputmode, important when feeding data. */
    protected String outputModeName;

    /** Name of the point data item. */
    protected String pointDataItemName;


    /**
     * Setup editor dialog.
     * @param collection The collection to use.
     */
    public ManualPointsEditor(Collection collection,
        RedrawRequestHandler handler, String outputModeName
    ) {
        this.collection = collection;
        this.redrawRequestHandler = handler;
        this.outputModeName = outputModeName;
        this.pointDataItemName = outputModeName + "." + POINT_DATA;
        init();
    }


    /** Searches collection for first artifact to serve (manual) point data. */
    public String findManualPointsUUID() {
        // TODO Need to be more picky (different points in different diagrams)
        int size = collection.getItemLength();

        for (int i = 0; i < size; i++) {
            CollectionItem item = collection.getItem(i);
            String dataValue = item.getData().get(pointDataItemName);
            if (dataValue != null) {
                // Found it.
                uuid = item.identifier();
                return uuid;
            }
        }

        return null;
    }


    /**
     * Initialize the editor window and its components.
     */
    protected void init() {
        setTitle(MSG.addpoints());
        setCanDragReposition(true);
        setCanDragResize(true);

        // If no manualpoints artifact found, create it now.
        if(findManualPointsUUID() == null) {
            addArtifactCreateUI();
        }
        else {
            createUI();
        }
    }


    /** Create and setup/add the ui. */
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

        // Use X and Y as default fallback.
        String xAxis = "X";
        String yAxis = "Y";

        // Get header text from collection settings
        Settings settings = this.collection.getSettings(outputModeName);
        List<Property> axes = settings.getSettings("axes");
        if(axes != null) {
            for (Property p: axes) {
                PropertyGroup pg = (PropertyGroup)p;
                GWT.log(pg.toString());
                StringProperty id =
                    (StringProperty)pg.getPropertyByName("id");
                if(id.getValue().equals("X")) {
                    StringProperty name =
                        (StringProperty)pg.getPropertyByName("label");
                    xAxis = name.getValue();
                }
                else if (yAxis.equals("Y")) {
                    StringProperty name =
                        (StringProperty)pg.getPropertyByName("label");
                    yAxis = name.getValue();
                }
            }
        }

        CellFormatter format = createCellFormatter();
        CellEditValueParser cevp = createCellEditValueParser();
        CellEditValueFormatter cevf = createCellEditValueFormatter();

        ListGridField xField =
                new ListGridField(PointRecord.ATTRIBUTE_X, xAxis);
        if(xAxis.equalsIgnoreCase("date") || xAxis.equalsIgnoreCase("Datum")) {
            // FIXME: This is a hack for Timeseries charts
            // with Date types on the x axis
            xField.setType(ListGridFieldType.DATE);
            this.fieldTypeX = ListGridFieldType.DATE;
        }
        else {
            xField.setType(ListGridFieldType.FLOAT);
            xField.setCellFormatter(format);
            xField.setEditValueParser(cevp);
            xField.setEditValueFormatter(cevf);
        }

        ListGridField yField =
            new ListGridField(PointRecord.ATTRIBUTE_Y, yAxis);
        yField.setType(ListGridFieldType.FLOAT);
        yField.setCellFormatter(format);
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

        // Find the artifacts uuid
        findManualPointsUUID();
        CollectionItem item = collection.getItem(uuid);

        // Add points to grid
        if (item != null) {
            // TODO store this from findPointUUID instead (we touched these).
            String jsonData = item.getData().get(pointDataItemName);
            JSONArray jsonArray = (JSONArray) JSONParser.parse(jsonData);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray point = (JSONArray) jsonArray.get(i);
                listGrid.addData(PointRecord.fromJSON(point));
            }
        }
        else {
           GWT.log("ManualPointsEditor: No item found for " + uuid);
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


    protected CellFormatter createCellFormatter() {
        return new CellFormatter() {
            @Override
            public String format(
                Object value, ListGridRecord record, int rowNum, int colNum) {
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
    }


    protected CellEditValueParser createCellEditValueParser() {
        return new CellEditValueParser() {
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
    }


    protected CellEditValueFormatter createCellEditValueFormatter() {
        return new CellEditValueFormatter() {
            @Override
            public Object format(
                Object value, ListGridRecord record, int rowNum, int colNum) {
                if (value == null) {
                    return "";
                }
                NumberFormat nf = NumberFormat.getDecimalFormat();
                try {
                    double d = Double.valueOf(value.toString()).doubleValue();
                    return nf.format(d);
                }
                catch(NumberFormatException nfe) {
                    return value;
                }
            }
        };
    }

    protected String getLocaleDateFormat() {
        String loc = Config.getInstance().getLocale();
        if ("de".equals(loc)) {
            return "yy.MM.yyyy";
        }
        else {
            return "MM/dd/yyyy";
        }
    }

    protected String formatDate(Date date) {
        DateTimeFormat dtf = DateTimeFormat.getFormat(getLocaleDateFormat());
        return dtf.format(date);
    }

    /** Create JSON representation of the points present in the list grid. */
    protected JSONArray jsonArrayFromListGrid() {
        JSONArray list = new JSONArray();
        int idx = 0;

        for(ListGridRecord record : listGrid.getRecords()) {
            if (record instanceof PointRecord) {
                JSONArray data = new JSONArray();

                PointRecord point = (PointRecord) record;
                if(point.isTimeseriesPoint()) {
                    data.set(0, new JSONString(point.getXAsDate()));
                    GWT.log("Date: " + point.getXAsDate());
                }
                else {
                    data.set(0, new JSONNumber(point.getX()));
                }
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

                if(fieldTypeX.equals(ListGridFieldType.DATE)) {
                    Date date = record.getAttributeAsDate(
                        PointRecord.ATTRIBUTE_X);
                    data.set(0, new JSONString(formatDate(date)));
                    GWT.log("Date: " + formatDate(date));
                }
                else {
                    data.set(0, new JSONNumber(record.
                            getAttributeAsDouble(PointRecord.ATTRIBUTE_X)));
                }
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


    /**
     * Called when OK Button was clicked. Then, if entered values are valid,
     * fire a RedrawRequest and destroy.
     */
    protected void okClicked() {
        if(isDialogValid()) {
            // Feed JSON-encoded content of listgrid.
            JSONArray list = jsonArrayFromListGrid();

            Data[] feedData = new Data[] {
                DefaultData.createSimpleStringData(pointDataItemName,
                    list.toString())
            };

            feedService.feed(
                Config.getInstance().getLocale(),
                new DefaultArtifact(uuid, "TODO:hash"),
                feedData,
                new AsyncCallback<Artifact>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not feed artifact with points.");
                        SC.warn(MSG.getString(caught.getMessage()));
                        enable();
                    }
                    @Override
                    public void onSuccess(Artifact fartifact) {
                        GWT.log("Successfully set points");
                        redrawRequestHandler.onRedrawRequest(
                            new RedrawRequestEvent());
                        destroy();
                    }
                });
        }
        else {
            GWT.log("Dialog not valid");
            SC.warn(MSG.error_dialog_not_valid());
        }
    }


    /** Add a ManualPointArtifact to Collection. */
    public void addArtifactCreateUI() {
        final Label standByLabel = new Label(MSG.standby());
        addItem(standByLabel);

        setWidth(380);
        setHeight(470);
        centerInPage();

        Config config = Config.getInstance();
        String locale = config.getLocale();

        loadArtifactService.load(
            this.collection,
            new Recommendation("manualpoints", ""),
            "manualpoints",
            locale,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Creating manualpoint artifact failed!");
                }
                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully created artifact.");
                    removeItem(standByLabel);
                    uuid = artifact.getUuid();
                    createUI();
                }
            });
    }


    /**
     * This method is called when the user aborts point editing.
     * @param event The event.
     */
    @Override
    public void onClick(ClickEvent event) {
        this.destroy();
    }


    /** Return false if x or y attribute is missing. */
    protected boolean isDialogValid() {
        boolean valid = true;
        for (ListGridRecord record : listGrid.getRecords()) {
            try {
                if (record.getAttribute(PointRecord.ATTRIBUTE_X) == null
                    || record.getAttribute(PointRecord.ATTRIBUTE_Y) == null) {
                    return false;
                }
            }
            catch(IllegalArgumentException ex) {

            }
        }
        if (listGrid.hasErrors()) {
            valid = false;
        }
        return valid;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
