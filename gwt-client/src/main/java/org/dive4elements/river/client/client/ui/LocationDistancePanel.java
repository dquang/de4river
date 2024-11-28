/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.data.AdvancedCriteria;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Criterion;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
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
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.FilterHandler;
import org.dive4elements.river.client.client.event.RangeFilterEvent;
import org.dive4elements.river.client.client.event.StringFilterEvent;
import org.dive4elements.river.client.client.services.DistanceInfoService;
import org.dive4elements.river.client.client.services.DistanceInfoServiceAsync;
import org.dive4elements.river.client.client.ui.range.DistanceInfoDataSource;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.DistanceInfoObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * This UIProvider creates a widget to enter locations or a distance.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LocationDistancePanel
extends      AbstractUIProvider
implements   ChangeHandler, BlurHandler, FilterHandler
{
    private static final long serialVersionUID = -10820092176039372L;

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    /** The DistanceInfoService used to retrieve locations about rivers. */
    protected DistanceInfoServiceAsync distanceInfoService =
        GWT.create(DistanceInfoService.class);

    public static final String FIELD_MODE = "mode";

    /** The constant name of the input field to enter the start of a distance.*/
    public static final String FIELD_FROM = "from";

    /** The constant name of the input field to enter the end of a distance.*/
    public static final String FIELD_TO = "to";

    /** The constant name of the input field to enter locations.*/
    public static final String FIELD_VALUE_LOCATION = "location";

    /** The constant name of the input field to enter distance.*/
    public static final String FIELD_VALUE_DISTANCE = "distance";

    /** The constant name of the input field to enter the step width of a
     * distance.*/
    public static final String FIELD_WIDTH = "width";

    public static final int WIDTH = 250;


    /** The radio group for input mode selection.*/
    protected DynamicForm mode;

    /** A container that will contain the location or the distance panel.*/
    protected HLayout container;

    /** The min value for a distance.*/
    protected double min;

    /** The max value for a distance.*/
    protected double max;

    /** The 'from' value entered in the distance mode.*/
    protected double from;

    /** The 'to' value entered in the distance mode.*/
    protected double to;

    /** The 'step' value entered in the distance mode.*/
    protected double step;

    /** The values entered in the location mode.*/
    protected double[] values;

    /** The input panel for locations. */
    protected DoubleArrayPanel locationPanel;

    /** The input panel for distances. */
    protected DoubleRangePanel distancePanel;

    /** The tab set containing the location and distance table. */
    protected TabSet inputTables;

    /** The distance table. */
    protected ListGrid distanceTable;

    /** The locations table. */
    protected ListGrid locationsTable;

    /** The table data. */
    protected DistanceInfoObject[] tableData;

    /** The table filter.*/
    protected TableFilter filterDescription;
    protected RangeTableFilter filterRange;

    /** The Combobox for table filter criteria. */
    protected SelectItem filterCriteria;
    protected StaticTextItem filterResultCount;
    protected ListGrid currentFiltered;

    /**
     * Creates a new LocationDistancePanel instance.
     */
    public LocationDistancePanel() {
        distanceTable  = new ListGrid();
        distanceTable.setAutoFetchData(true);

        locationsTable = new ListGrid();
        locationsTable.setAutoFetchData(true);

        distanceTable.setShowHeaderContextMenu(false);
        locationsTable.setShowHeaderContextMenu(false);
    }


    /**
     * This method creates a widget that contains a label, a panel with
     * checkboxes to switch the input mode between location and distance input,
     * and a the mode specific panel.
     *
     * @param data The data that might be inserted.
     *
     * @return a panel.
     */
    @Override
    public Canvas create(DataList data) {
        VLayout layout = new VLayout();
        layout.setMembersMargin(10);

        Label label   = new Label(MESSAGES.location_distance_state());
        Canvas widget = createWidget(data);
        Canvas submit = getNextButton();
        createDistanceInputPanel();

        initDefaults(data);

        widget.setHeight(50);
        label.setHeight(25);

        layout.addMember(label);
        layout.addMember(widget);
        layout.addMember(submit);

        return layout;
    }


    /**
     * Setup a table for a DistanceInfoDataSource.
     *
     * Sets up a table to for input completion. The table
     * can be used either for single locations or distances.
     * Depending on the value of isDistance the table will
     * have a to and a from column or a single location column.
     *
     * @param table the ListGrid to set up.
     * @param doublePins wether or not to have.
     * @param isDistance wether or not to and from should be included.
     */
    protected void setupDistanceInfoTable(ListGrid table,
                                          boolean doublePins,
                                          boolean isDistance) {

        String baseUrl = GWT.getHostPageBaseURL();

        table.setWidth100();
        table.setShowRecordComponents(true);
        table.setShowRecordComponentsByCell(true);
        table.setHeight100();
        table.setEmptyMessage(MESSAGES.empty_filter());
        table.setCanReorderFields(false);

        CellFormatter cf = new CellFormatter() {
            @Override
            public String format(
                Object value,
                ListGridRecord record,
                int rowNum, int colNum) {
                    if (value == null) return null;
                    try {
                        NumberFormat nf;
                        double v = Double.parseDouble((String)value);
                        nf = NumberFormat.getFormat("###0.00##");
                        return nf.format(v);
                    }
                    catch (Exception e) {
                        return value.toString();
                    }
            }
        };
        ListGridField pin1 = null;
        ListGridField pin2 = null;

        if (doublePins) {
            pin1 = new ListGridField ("fromIcon", MESSAGES.from());
            pin1.setWidth (30);
        } else {
            pin1 = new ListGridField ("fromIcon", MESSAGES.selection());
            pin1.setWidth (60);
        }
        pin1.setType (ListGridFieldType.ICON);
        pin1.setCellIcon(baseUrl + MESSAGES.markerGreen());

        if (doublePins) {
            pin2 = new ListGridField ("toIcon", MESSAGES.to());
            pin2.setType (ListGridFieldType.ICON);
            pin2.setWidth (30);
            pin2.setCellIcon(baseUrl + MESSAGES.markerRed());
        }

        if (isDistance) {
            /* We have from / to fields */
            pin1.addRecordClickHandler (new RecordClickHandler () {
                @Override
                public void onRecordClick (RecordClickEvent e) {
                    Record r = e.getRecord();
                    if (!isLocationMode ()) {
                        /* distance panel and distance mode */
                        setFrom(r.getAttribute("from"));
                        setTo(r.getAttribute("to"));
                    } else {
                        /* distance panel and location mode */
                        /* Pin 1 is the "from" pin */
                        appendLocation(r.getAttribute("from"));
                    }
                }
            });
            if (doublePins) {
                pin2.addRecordClickHandler (new RecordClickHandler () {
                    @Override
                    public void onRecordClick (RecordClickEvent e) {
                        Record r = e.getRecord();
                        if (isLocationMode ()) {
                            appendLocation(r.getAttribute("to"));
                        } else {
                            /* Distance and double pin behavior is only
                             * defined for location mode. */
                            GWT.log("Unhandled input state.");
                        }
                    }
                });
            }
        } else {
            /* We only have the from field */
            pin1.addRecordClickHandler (new RecordClickHandler () {
                @Override
                public void onRecordClick (RecordClickEvent e) {
                    Record r = e.getRecord();
                    if (!isLocationMode ()) {
                        /* Location panel and distance mode */
                        setFrom(r.getAttribute("from"));
                    } else {
                        /* Location panel and location mode */
                        appendLocation(r.getAttribute("from"));
                    }
                }
            });
            if (doublePins) {
                pin2.addRecordClickHandler (new RecordClickHandler () {
                    @Override
                    public void onRecordClick (RecordClickEvent e) {
                        Record r = e.getRecord();
                        if (!isLocationMode ()) {
                            setTo(r.getAttribute("from"));
                        } else {
                            /* Distance and double pin behavior is only
                             * defined for location mode. */
                            GWT.log("Unhandled input state.");
                        }
                    }
                });
            }
        }

        ListGridField ddescr = new ListGridField("description",
                MESSAGES.description());
        ddescr.setType(ListGridFieldType.TEXT);
        ddescr.setWidth("*");

        ListGridField from;
        ListGridField to = null;

        if (isDistance) {
            from = new ListGridField("from", MESSAGES.from());
            to = new ListGridField("to", MESSAGES.to());
            to.setType(ListGridFieldType.FLOAT);
            to.setCellFormatter(cf);

            to.setWidth("12%");
            to.setAlign(Alignment.LEFT);
        } else {
            from = new ListGridField("from", MESSAGES.locations());
        }
        from.setCellFormatter(cf);
        from.setWidth("12%");

        ListGridField dside = new ListGridField("riverside",
                MESSAGES.riverside());
        dside.setType(ListGridFieldType.TEXT);
        dside.setWidth("12%");

        ListGridField bottom =
            new ListGridField("bottom", MESSAGES.bottom_edge());
        bottom.setType(ListGridFieldType.TEXT);
        bottom.setWidth("10%");
        bottom.setCellFormatter(cf);

        ListGridField top =
            new ListGridField("top", MESSAGES.top_edge());
        top.setType(ListGridFieldType.TEXT);
        top.setWidth("10%");
        top.setCellFormatter(cf);

        if (doublePins && isDistance) {
            table.setFields(pin1, pin2, ddescr, from, to, dside, bottom, top);
        } else if (doublePins) {
            table.setFields(pin1, pin2, ddescr, from, dside, bottom, top);
        } else if (isDistance) {
            table.setFields(pin1, ddescr, from, to, dside, bottom, top);
        } else {
            table.setFields(pin1, ddescr, from, dside, bottom, top);
        }
    }

    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data       dMode  = getData(items, "ld_mode");
        DataItem[] dItems = dMode.getItems();

        boolean rangeMode = true;
        if (dItems != null && dItems[0] != null) {
            rangeMode = FIELD_VALUE_DISTANCE.equals(dItems[0].getStringValue());
        }

        HLayout layout = new HLayout();
        layout.setWidth("400px");

        Label   label  = new Label(dataList.getLabel());
        label.setWidth("200px");

        Canvas back = getBackButton(dataList.getState());

        layout.addMember(label);

        if (rangeMode) {
            layout.addMember(getOldRangeSelection(dataList));
        }
        else {
            layout.addMember(getOldLocationSelection(dataList));
        }

        layout.addMember(back);

        return layout;
    }


    /**
     * Creates a label for the selected range.
     *
     * @param dataList The DataList containing all values for this state.
     *
     * @return A label displaying the selected values.
     */
    protected Label getOldRangeSelection(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data dFrom = getData(items, "ld_from");
        Data dTo   = getData(items, "ld_to");
        Data dStep = getData(items, "ld_step");

        DataItem[] from = dFrom.getItems();
        DataItem[] to   = dTo.getItems();
        DataItem[] step = dStep.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append(from[0].getLabel());
        sb.append(" " + MESSAGES.unitFrom() + " ");
        sb.append(to[0].getLabel());
        sb.append(" " + MESSAGES.unitTo() + " ");
        sb.append(step[0].getLabel());
        sb.append(" " + MESSAGES.unitWidth());

        Label selected = new Label(sb.toString());
        selected.setWidth("130px");

        return selected;
    }


    /**
     * Creates a label for the selected locations.
     *
     * @param dataList The DataList containing all values for this state.
     *
     * @return A label displaying the selected values.
     */
    protected Label getOldLocationSelection(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data       dLocations = getData(items, "ld_locations");
        DataItem[] lItems     = dLocations.getItems();

        String[] splitted = lItems[0].getStringValue().split(" ");
        String value = "";
        for (int i = 0; i < splitted.length; i++) {
            try {
                NumberFormat nf = NumberFormat.getDecimalFormat();
                double dv = Double.parseDouble(splitted[i]);
                value += nf.format(dv) + " ";
            }
            catch(NumberFormatException nfe) {
                value += splitted[i] + " ";
            }
        }

        Label selected = new Label(value);
        selected.setWidth(130);

        return selected;
    }


    /**
     * This method reads the default values defined in the DataItems of the Data
     * objects in <i>list</i>.
     *
     * @param list The DataList container that stores the Data objects.
     */
    protected void initDefaults(DataList list) {
        Data m = getData(list.getAll(), "ld_mode");
        Data l = getData(list.getAll(), "ld_locations");
        Data f = getData(list.getAll(), "ld_from");
        Data t = getData(list.getAll(), "ld_to");
        Data s = getData(list.getAll(), "ld_step");

        DataItem[] fItems = f.getItems();
        DataItem[] tItems = t.getItems();
        DataItem[] sItems = s.getItems();

        min  = Double.valueOf(fItems[0].getStringValue());
        max  = Double.valueOf(tItems[0].getStringValue());
        step = Double.valueOf(sItems[0].getStringValue());

        DataItem   mDef   = m.getDefault();
        DataItem   lDef   = l.getDefault();
        DataItem   fDef   = f.getDefault();
        DataItem   tDef   = t.getDefault();
        DataItem   sDef   = s.getDefault();

        String mDefValue = mDef != null ? mDef.getStringValue() : null;
        String theMode = mDefValue != null && mDefValue.length() > 0
            ? mDef.getStringValue()
            : FIELD_VALUE_DISTANCE;

        mode.setValue(FIELD_MODE, theMode);

        String fDefValue = fDef != null ? fDef.getStringValue() : null;
        setFrom(fDefValue != null && fDefValue.length() > 0
            ? Double.valueOf(fDef.getStringValue())
            : min);

        String tDefValue = tDef != null ? tDef.getStringValue() : null;
        setTo(tDefValue != null && tDefValue.length() > 0
            ? Double.valueOf(tDef.getStringValue())
            : max);

        String sDefValue = sDef != null ? sDef.getStringValue() : null;
        setStep(sDefValue != null && sDefValue.length() > 0
            ? Double.valueOf(sDef.getStringValue())
            : step);

        if (lDef != null) {
            String lDefValue = lDef != null ? lDef.getStringValue() : null;

            if (lDefValue != null && lDefValue.length() > 0) {
                setLocationValues(lDef.getStringValue());
            }
        }

        if (theMode.equals(FIELD_VALUE_DISTANCE)) {
            enableDistanceMode();
            inputTables.selectTab(1);
        } else {
            enableLocationMode();
        }
        currentFiltered = (ListGrid)inputTables.getSelectedTab().getPane();

        distancePanel.setValues(getFrom(), getTo(), getStep());
    }


    protected Canvas createWidget(DataList data) {
        VLayout layout       = new VLayout();
        container            = new HLayout();
        Canvas checkboxPanel = createRadioButtonPanel();

        locationPanel = new DoubleArrayPanel(
                MESSAGES.unitLocation(),
                getLocationValues(),
                this);

        distancePanel = new DoubleRangePanel(
                MESSAGES.unitFrom(), MESSAGES.unitTo(), MESSAGES.unitWidth(),
                0, 0, 0, /* initDefaults set the default values for this. */
                400,
                this);

        container.addMember(locationPanel);
        container.addMember(distancePanel);
        container.hideMember(locationPanel);

        layout.addMember(checkboxPanel);
        layout.addMember(container);

        container.setMembersMargin(30);

        inputTables   = new TabSet();
        inputTables.addTabSelectedHandler(new TabSelectedHandler() {
            @Override
            public void onTabSelected(TabSelectedEvent evt) {
                filterDescription.clear();
                filterRange.clear();
                filterResultCount.setValue("");

                // The assumption is that location is tab 0 and distance tab 1

                Canvas c = evt.getTabPane();
                if(c instanceof ListGrid) {
                    currentFiltered = (ListGrid)c;
                }
            }
        });

        Tab locations = new Tab(MESSAGES.locations());
        Tab distances = new Tab(MESSAGES.distance());

        inputTables.setWidth100();
        inputTables.setHeight100();

        locations.setPane(locationsTable);
        distances.setPane(distanceTable);

        inputTables.addTab(locations);
        inputTables.addTab(distances);

        filterResultCount = new StaticTextItem(MESSAGES.resultCount());
        filterResultCount.setTitleAlign(Alignment.LEFT);
        filterResultCount.setTitleStyle("color: #000");

        filterDescription = new TableFilter();
        filterDescription.setHeight("30px");
        filterDescription.addFilterHandler(this);

        filterRange = new RangeTableFilter();
        filterRange.setHeight("30px");
        filterRange.addFilterHandler(this);
        filterRange.setVisible(false);

        filterCriteria = new SelectItem();
        filterCriteria.setShowTitle(false);
        filterCriteria.setWidth(100);
        filterCriteria.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent e) {
                if(e.getValue().toString().equals("range")) {
                    filterRange.setVisible(true);
                    filterDescription.setVisible(false);
                    filterDescription.clear();
                    filterResultCount.setValue("");
                }
                else {
                    filterRange.setVisible(false);
                    filterRange.clear();
                    filterDescription.setVisible(true);
                    filterResultCount.setValue("");
                }
            }
        });

        LinkedHashMap<String, String> filterMap =
            new LinkedHashMap<String, String>();
        filterMap.put("description", MESSAGES.description());
        filterMap.put("range", MESSAGES.range());
        filterCriteria.setValueMap(filterMap);
        filterCriteria.setValue("description");

        DynamicForm form = new DynamicForm();
        form.setFields(filterCriteria);
        inputTables.setHeight("*");
        DynamicForm form2 = new DynamicForm();
        form2.setFields(filterResultCount);

        VLayout helper = new VLayout();
        HLayout filterLayout = new HLayout();

        filterLayout.addMember(form);
        filterLayout.addMember(filterDescription);
        filterLayout.addMember(filterRange);
        filterLayout.setHeight("30px");
        helper.addMember(inputTables);
        helper.addMember(filterLayout);
        helper.addMember(form2);
        helper.setHeight100();
        helper.setWidth100();

        helperContainer.addMember(helper);
        filterLayout.setWidth("200");

        return layout;
    }


    @Override
    public void onFilterCriteriaChanged(StringFilterEvent event) {
        String search = event.getFilter();

        if (search != null && search.length() > 0) {
            Criteria c = new Criteria("description", search);

            locationsTable.filterData(c);
            distanceTable.filterData(c);
            filterResultCount.setValue(currentFiltered.getRecords().length);
        }
        else {
            locationsTable.clearCriteria();
            distanceTable.clearCriteria();
            filterResultCount.setValue("");
        }
    }


    @Override
    public void onFilterCriteriaChanged(RangeFilterEvent event) {
        Float from = event.getFrom() - 0.001f;
        Float to = event.getTo() + 0.001f;
        GWT.log("filtering range: " + from + " to " + to);


        Criterion combinedFilter = null;
        Criterion locationFilter = null;
        if (from.equals(Float.NaN) && to.equals(Float.NaN)) {
            locationsTable.clearCriteria();
            distanceTable.clearCriteria();
            filterResultCount.setValue("");
            return;
        }
        else if (from.equals(Float.NaN)) {
            combinedFilter = new Criterion("to", OperatorId.LESS_OR_EQUAL, to);
            locationFilter =
                new Criterion("from", OperatorId.LESS_OR_EQUAL, to);
            locationsTable.filterData(locationFilter);
            distanceTable.filterData(combinedFilter);
            filterResultCount.setValue(currentFiltered.getRecords().length);
            return;
        }
        else if (to.equals(Float.NaN)) {
            combinedFilter =
                new Criterion("from", OperatorId.GREATER_OR_EQUAL, from);
             locationsTable.filterData(combinedFilter);
            distanceTable.filterData(combinedFilter);
        }
        else {
            AdvancedCriteria c1 =
                new AdvancedCriteria(OperatorId.AND, new Criterion[] {
                    new Criterion("from", OperatorId.GREATER_OR_EQUAL, from),
                    new Criterion("from", OperatorId.LESS_OR_EQUAL, to)
                });

            AdvancedCriteria c2 =
                new AdvancedCriteria(OperatorId.AND, new Criterion[] {
                    new Criterion("to", OperatorId.GREATER_OR_EQUAL, from),
                    new Criterion("to", OperatorId.LESS_OR_EQUAL, to)
                });

            AdvancedCriteria c3 =
                new AdvancedCriteria(OperatorId.AND, new Criterion[] {
                    new Criterion("from", OperatorId.LESS_OR_EQUAL, to),
                    new Criterion("to", OperatorId.GREATER_OR_EQUAL, from)
                });

            combinedFilter =
                new AdvancedCriteria(OperatorId.OR, new Criterion[] {
                    c1, c2, c3
                });
        }
        locationsTable.filterData(combinedFilter);
        distanceTable.filterData(combinedFilter);
        filterResultCount.setValue(currentFiltered.getRecords().length);
    }


    @Override
    public List<String> validate() {
        if (isLocationMode()) {
            return validateLocations();
        }
        else {
            return validateRange();
        }
    }


    protected List<String> validateLocations() {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        try {
            saveLocationValues(locationPanel);
        }
        catch (Exception e) {
            errors.add(MESSAGES.wrongFormat());
        }

        double[] values = getLocationValues();
        double[] good   = new double[values.length];
        int      idx    = 0;

        for (double value: values) {
            if (value < min || value > max) {
                String tmp = MESSAGES.error_validate_range();
                tmp = tmp.replace("$1", nf.format(value));
                tmp = tmp.replace("$2", nf.format(min));
                tmp = tmp.replace("$3", nf.format(max));
                errors.add(tmp);
            }
            else {
                good[idx++] = value;
            }
        }

        double[] justGood = new double[idx];
        for (int i = 0; i < justGood.length; i++) {
            justGood[i] = good[i];
        }

        if (!errors.isEmpty()) {
            locationPanel.setValues(justGood);
        }

        return errors;
    }


    protected List<String> validateRange() {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        try {
            saveDistanceValues(distancePanel);
        }
        catch (Exception e) {
            errors.add(MESSAGES.wrongFormat());
        }

        double from = getFrom();
        double to   = getTo();
        double step = getStep();

        if (from < min || from > max) {
            String tmp = MESSAGES.error_validate_range();
            tmp = tmp.replace("$1", nf.format(from));
            tmp = tmp.replace("$2", nf.format(min));
            tmp = tmp.replace("$3", nf.format(max));
            errors.add(tmp);
            from = min;
        }

        if (to < min || to > max) {
            String tmp = MESSAGES.error_validate_range();
            tmp = tmp.replace("$1", nf.format(to));
            tmp = tmp.replace("$2", nf.format(min));
            tmp = tmp.replace("$3", nf.format(max));
            errors.add(tmp);
            to = max;
        }

        if (!errors.isEmpty()) {
            distancePanel.setValues(from, to, step);
        }

        return errors;
    }


    /**
     * This method returns the selected data.
     *
     * @return the selected/inserted data.
     */
    @Override
    public Data[] getData() {
        List<Data> data = new ArrayList<Data>();

        // If we have entered a value and click right afterwards on the
        // 'next' button, the BlurEvent is not fired, and the values are not
        // saved. So, we gonna save those values explicitly.
        if (isLocationMode()) {
            Canvas member = container.getMember(0);
            if (member instanceof DoubleArrayPanel) {
                DoubleArrayPanel form = (DoubleArrayPanel) member;
                saveLocationValues(form);
            }

            Data dLocations = getDataLocations();
            DataItem dFrom  = new DefaultDataItem("ld_from", "ld_from", "");
            DataItem dTo    = new DefaultDataItem("ld_to", "ld_to", "");
            DataItem dStep  = new DefaultDataItem("ld_step", "ld_step", "");

            data.add(dLocations);
            data.add(new DefaultData(
                "ld_from", null, null, new DataItem[] { dFrom } ));
            data.add(new DefaultData(
                "ld_to", null, null, new DataItem[] { dTo } ));
            data.add(new DefaultData(
                "ld_step", null, null, new DataItem[] { dStep } ));
        }
        else {
            Canvas member = container.getMember(0);
            if (member instanceof DoubleRangePanel) {
                DoubleRangePanel form = (DoubleRangePanel) member;
                saveDistanceValues(form);
            }

            Data dFrom   = getDataFrom();
            Data dTo     = getDataTo();
            Data dStep   = getDataStep();
            DataItem loc = new DefaultDataItem(
                "ld_locations", "ld_locations","");

            data.add(dFrom);
            data.add(dTo);
            data.add(dStep);
            data.add(new DefaultData(
                "ld_locations", null, null, new DataItem[] { loc } ));
        }

        Data dMode = getDataMode();
        if (dMode != null) {
            data.add(dMode);
        }

        return data.toArray(new Data[data.size()]);
    }


    /**
     * Returns the Data object for the 'mode' attribute.
     *
     * @return the Data object for the 'mode' attribute.
     */
    protected Data getDataMode() {
        String   value = mode.getValueAsString(FIELD_MODE);
        DataItem item  = new DefaultDataItem("ld_mode", "ld_mode", value);
        return new DefaultData("ld_mode", null, null, new DataItem[] { item });
    }


    protected Data getDataLocations() {
        double[] locations = getLocationValues();
        boolean  first     = true;

        if (locations == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (double l: locations) {
            if (!first) {
                sb.append(" ");
            }

            sb.append(l);

            first = false;
        }

        DataItem item = new DefaultDataItem(
            "ld_locations",
            "ld_locations",
            sb.toString());

        return new DefaultData(
            "ld_locations",
            null,
            null,
            new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'from' attribute.
     *
     * @return the Data object for the 'from' attribute.
     */
    protected Data getDataFrom() {
        String value  = Double.valueOf(getFrom()).toString();
        DataItem item = new DefaultDataItem("ld_from", "ld_from", value);
        return new DefaultData(
            "ld_from", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'to' attribute.
     *
     * @return the Data object for the 'to' attribute.
     */
    protected Data getDataTo() {
        String value  = Double.valueOf(getTo()).toString();
        DataItem item = new DefaultDataItem("ld_to", "ld_to", value);
        return new DefaultData(
            "ld_to", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'step' attribute.
     *
     * @return the Data object for the 'step' attribute.
     */
    protected Data getDataStep() {
        String value  = Double.valueOf(getStep()).toString();
        DataItem item = new DefaultDataItem("ld_step","ld_step", value);
        return new DefaultData(
            "ld_step", null, null, new DataItem[] { item });
    }


    /**
     * Determines the current input mode.
     *
     * @return true, if 'location' is the current input mode, otherwise false.
     */
    public boolean isLocationMode() {
        String inputMode = mode.getValueAsString(FIELD_MODE);

        return inputMode.equals(FIELD_VALUE_LOCATION) ? true : false;
    }


    /**
     * Activates the location panel.
     */
    protected void enableLocationMode() {
        mode.setValue(FIELD_MODE, FIELD_VALUE_LOCATION);
        container.hideMember(distancePanel);
        container.showMember(locationPanel);
        setupDistanceInfoTable(locationsTable, false, false);
        setupDistanceInfoTable(distanceTable, true, true);
        inputTables.updateTab(0, locationsTable);
        inputTables.updateTab(1, distanceTable);
    }


    /**
     * Activates the distance panel.
     */
    protected void enableDistanceMode() {
        mode.setValue(FIELD_MODE, FIELD_VALUE_DISTANCE);
        container.hideMember(locationPanel);
        container.showMember(distancePanel);
        setupDistanceInfoTable(locationsTable, true, false);
        setupDistanceInfoTable(distanceTable, false, true);
        inputTables.updateTab(0, locationsTable);
        inputTables.updateTab(1, distanceTable);
    }


    /**
     * This method switches the input mode between location and distance input.
     *
     * @param event The click event fired by a RadioButtonGroupItem.
     */
    @Override
    public void onChange(ChangeEvent event) {
        String value = (String) event.getValue();

        if (value == null) {
            return;
        }
        if (value.equals(FIELD_VALUE_LOCATION)) {
            enableLocationMode();
            filterDescription.clear();
            filterRange.clear();
            filterResultCount.setValue("");

            // Bring this tab to front.
            inputTables.selectTab(0);
        }
        else {
            enableDistanceMode();
            filterDescription.clear();
            filterRange.clear();
            filterResultCount.setValue("");

            // Bring the distanceTable tab to front.
            inputTables.selectTab(1);
        }
    }


    /**
     * This method is used to validate the inserted data in the form fields.
     *
     * @param event The BlurEvent that gives information about the FormItem that
     * has been modified and its value.
     */
    @Override
    public void onBlur(BlurEvent event) {
        FormItem item = event.getItem();
        String  field = item.getFieldName();

        if (field == null) {
            return;
        }

        if (field.equals(DoubleArrayPanel.FIELD_NAME)) {
            DoubleArrayPanel p = (DoubleArrayPanel) event.getForm();

            saveLocationValue(p, item);
        }
        else {
            DoubleRangePanel p = (DoubleRangePanel) event.getForm();

            saveDistanceValue(p, item);
        }
    }



    /**
     * Validates and stores all values entered in the location mode.
     *
     * @param p The DoubleArrayPanel.
     */
    protected void saveLocationValues(DoubleArrayPanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            if (item.getFieldName().equals(DoubleArrayPanel.FIELD_NAME)) {
                saveLocationValue(p, item);
            }
        }
    }


    /**
     * Validates and stores all values entered in the distance mode.
     *
     * @param p The DoubleRangePanel.
     */
    protected void saveDistanceValues(DoubleRangePanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            saveDistanceValue(p, item);
        }
    }


    /**
     * Validates and stores a value entered in the location mode.
     *
     * @param p The DoubleArrayPanel.
     * @param item The item that needs to be validated.
     */
    protected void saveLocationValue(DoubleArrayPanel p, FormItem item) {
        if (p.validateForm(item)) {
            setLocationValues(p.getInputValues(item));
        }
    }


    /**
     * Validates and stores value entered in the distance mode.
     *
     * @param p The DoubleRangePanel.
     * @param item The item that needs to be validated.
     */
    protected void saveDistanceValue(DoubleRangePanel p, FormItem item) {
        if (p.validateForm(item)) {
            setFrom(p.getFrom());
            setTo(p.getTo());
            setStep(p.getStep());
        }
    }


    /**
     * This method creates the panel that contains the checkboxes to switch
     * between the input mode 'location' and 'distance'.
     *
     * @return the checkbox panel.
     */
    protected Canvas createRadioButtonPanel() {
        mode = new DynamicForm();

        RadioGroupItem radio = new RadioGroupItem(FIELD_MODE);
        radio.setShowTitle(false);
        radio.setVertical(false);
        radio.setWrap(false);

        LinkedHashMap<String, String> values =
            new LinkedHashMap<String, String>();
        values.put(FIELD_VALUE_LOCATION, MESSAGES.location());
        values.put(FIELD_VALUE_DISTANCE, MESSAGES.distance());

        LinkedHashMap<String, String> initial =
            new LinkedHashMap<String, String>();
        initial.put(FIELD_MODE, FIELD_VALUE_DISTANCE);

        radio.setValueMap(values);
        radio.addChangeHandler(this);

        mode.setFields(radio);
        mode.setValues(initial);

        return mode;
    }


    protected void createDistanceInputPanel() {
        Config config = Config.getInstance();
        String url    = config.getServerUrl();
        String river  = "";

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                DataList dl = data[i];
                if (dl.getState().endsWith("river")) {
                    for (int j = 0; j < dl.size(); j++) {
                        Data d = dl.get(j);
                        DataItem[] di = d.getItems();
                        if (di != null && di.length == 1) {
                           river = d.getItems()[0].getStringValue();
                        }
                    }
                }
            }
        }

        distanceTable.setDataSource(new DistanceInfoDataSource(
            url, river, "distances"));
        locationsTable.setDataSource(new DistanceInfoDataSource(
            url, river, "locations"));
    }

    protected double getFrom() {
        return from;
    }

    protected void setTo(String to) {
       try {
            double toValue = Double.parseDouble(to);
            setTo(toValue);
        }
        catch(NumberFormatException nfe) {
            // Is there anything to do?
        }
    }

    protected void setFrom(String from) {
       try {
            double fromValue = Double.parseDouble(from);
            setFrom(fromValue);
        }
        catch(NumberFormatException nfe) {
            // Is there anything to do?
        }
    }

    protected void setFrom(double from) {
        this.from = from;
        /* The doubling should be removed and this.from abolished */
        distancePanel.setFrom(from);
    }


    protected double getTo() {
        return to;
    }


    protected void setTo(double to) {
        this.to = to;
        /* The doubling should be removed and this.to abolished */
        distancePanel.setTo(to);
    }


    protected double getStep() {
        return step;
    }


    protected void setStep(double step) {
        this.step = step;
    }


    protected double[] getLocationValues() {
        return values;
    }

    protected void appendLocation(String loc) {
        double[] selected;
        if (getLocationValues() != null) {
            double[] val = getLocationValues();
            selected = new double[val.length + 1];
            for(int i = 0; i < val.length; i++){
                selected[i] = val[i];
            }
            try {
                selected[val.length] = Double.parseDouble(loc);
            }
            catch(NumberFormatException nfe) {
                // Is there anything to do here?
            }
        }
        else {
            selected = new double[1];
            selected[0] = Double.parseDouble(loc);
        }
        setLocationValues(selected);
    }

    protected void setLocationValues(double[] values) {
        this.values = values;
        locationPanel.setValues(values);
    }


    protected void setLocationValues(String values) {
        String[] vs = values.split(" ");

        if (vs == null) {
            return;
        }

        double[] ds  = new double[vs.length];
        int      idx = 0;

        for (String s: vs) {
            try {
                ds[idx++] = Double.valueOf(s);
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        setLocationValues(ds);
    }


    protected void setDistanceValues (double from, double to) {
        setFrom(from);
        setTo(to);
        distancePanel.setValues(from, to, getStep());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
