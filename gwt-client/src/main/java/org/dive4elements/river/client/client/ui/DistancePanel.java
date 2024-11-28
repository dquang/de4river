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
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
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
import org.dive4elements.river.client.client.ui.range.DistanceInfoDataSource;
import org.dive4elements.river.client.client.ui.range.LocationsTable;
import org.dive4elements.river.client.client.ui.range.RangeTable;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/** Panel to allow input of distance for calculation range. */
public class DistancePanel
    extends AbstractUIProvider implements BlurHandler, FilterHandler
{

    private static final long serialVersionUID = -883142387908664588L;

    public static final int DEFAULT_STEP_WIDTH = 100;

    public static final String FIELD_LOWER = "ld_from";
    public static final String FIELD_UPPER = "ld_to";
    public static final String FIELD_STEP  = "ld_step";


    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected RangeTable     distancesTable;
    protected LocationsTable locationsTable;

    protected DoubleRangePanel distancePanel;

    protected TableFilter filterDescription;
    protected RangeTableFilter filterRange;

    protected TabSet tabs;

    protected double min;
    protected double max;

    protected StaticTextItem filterResultCount;
    protected ListGrid currentFiltered;

    public DistancePanel() {
        this("right");
    }


    public DistancePanel(String labelOrientation) {
        distancePanel  = new DoubleRangePanel(
            labelFrom(), labelTo(), labelStep(),
            0d, 0d, 0d, 250, this, labelOrientation);
    }


    @Override
    public Canvas create(DataList data) {
        VLayout layout = new VLayout();
        layout.setMembersMargin(10);

        Label label = new Label(getLabel());

        Canvas submit = getNextButton();

        label.setHeight(25);
        distancePanel.setHeight(50);

        layout.addMember(label);
        layout.addMember(distancePanel);
        layout.addMember(submit);

        initMinMaxValues(data);
        initDefaultValues(data);
        initHelperPanel();

        return layout;
    }


    @Override
    public Canvas createOld(DataList dataList) {
        String s = getOldSelectionString(dataList);
        String l = dataList.getLabel();

        Label label    = new Label(l);
        Label selected = new Label(s);

        HLayout layout = new HLayout();

        layout.setWidth(400);
        label.setWidth(200);
        selected.setWidth(130);

        layout.addMember(label);
        layout.addMember(selected);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }


    protected String getOldSelectionString(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data dFrom = getData(items, getLowerField());
        Data dTo   = getData(items, getUpperField());
        Data dStep = getData(items, getStepField());

        DataItem[] from = dFrom.getItems();
        DataItem[] to   = dTo.getItems();
        DataItem[] step = dStep.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append(from[0].getLabel());
        sb.append(" " + getUnitFrom() + " - ");
        sb.append(to[0].getLabel());
        sb.append(" " + getUnitTo() + " - ");
        sb.append(step[0].getLabel());
        sb.append(" " + getUnitStep());

        return sb.toString();
    }


    protected String getLabel() {
        return MSG.distance_state();
    }


    protected String labelFrom() {
        return getLabelFrom() + " [" + getUnitFrom() + "]";
    }


    protected String getLabelFrom() {
        return MSG.dpLabelFrom();
    }


    protected String getUnitFrom() {
        return MSG.dpUnitFrom();
    }


    protected String labelTo() {
        return getLabelTo() + " [" + getUnitTo() + "]";
    }


    protected String getLabelTo() {
        return MSG.dpLabelTo();
    }


    protected String getUnitTo() {
        return MSG.dpUnitTo();
    }


    protected String labelStep() {
        return getLabelStep() + " [" + getUnitStep() + "]";
    }


    protected String getLabelStep() {
        return MSG.dpLabelStep();
    }


    protected String getUnitStep() {
        return MSG.dpUnitStep();
    }


    protected String getLowerField() {
        return FIELD_LOWER;
    }


    protected String getUpperField() {
        return FIELD_UPPER;
    }


    protected String getStepField() {
        return FIELD_STEP;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        if (!distancePanel.validateForm()) {
            errors.add(MSG.wrongFormat());

            return errors;
        }

        validateFrom(errors);
        validateTo(errors);

        return errors;
    }


    protected void validateFrom(List<String> errors) {
        double from = distancePanel.getFrom();

        if (from < min || from > max) {
            NumberFormat nf = NumberFormat.getDecimalFormat();

            String tmp = MSG.error_validate_lower_range();
            tmp        = tmp.replace("$1", nf.format(from));
            tmp        = tmp.replace("$2", nf.format(min));

            distancePanel.setFrom(min);
            errors.add(tmp);
        }
    }


    protected void validateTo(List<String> errors) {
        double to = distancePanel.getTo();

        if (to < min || to > max) {
            NumberFormat nf = NumberFormat.getDecimalFormat();

            String tmp = MSG.error_validate_upper_range();
            tmp        = tmp.replace("$1", nf.format(to));
            tmp        = tmp.replace("$2", nf.format(max));

            distancePanel.setTo(max);
            errors.add(tmp);
        }
    }


    @Override
    public Data[] getData() {
        Data[] data = new Data[4];

        data[0] = getDataFrom();
        data[1] = getDataTo();
        data[2] = getDataStep();

        DataItem item = new DefaultDataItem("ld_mode","ld_mode", "distance");
        data[3]       = new DefaultData(
            "ld_mode", null, null, new DataItem[] { item });

        return data;
    }


    protected Data getDataFrom() {
        String value = String.valueOf(distancePanel.getFrom());
        String field = getLowerField();

        DataItem item = new DefaultDataItem(field, field, value);
        return new DefaultData(
            field, null, null, new DataItem[] { item });
    }


    protected Data getDataTo() {
        String value = String.valueOf(distancePanel.getTo());
        String field = getUpperField();

        DataItem item = new DefaultDataItem(field, field, value);
        return new DefaultData(
            field, null, null, new DataItem[] { item });
    }


    protected Data getDataStep() {
        String value = String.valueOf(distancePanel.getStep());
        String field = getStepField();

        DataItem item = new DefaultDataItem(field, field, value);
        return new DefaultData(
            field, null, null, new DataItem[] { item });
    }


    @Override
    public void onBlur(BlurEvent event) {
        distancePanel.validateForm();
    }


    protected void initMinMaxValues(DataList data) {
        Data f = getData(data.getAll(), getLowerField());
        Data t = getData(data.getAll(), getUpperField());

        DataItem[] fItems = f.getItems();
        DataItem[] tItems = t.getItems();

        try {
            min = Double.valueOf(fItems[0].getStringValue());
            max = Double.valueOf(tItems[0].getStringValue());
        }
        catch (NumberFormatException nfe) {
            min = -Double.MAX_VALUE;
            max =  Double.MAX_VALUE;
        }
    }


    protected void initDefaultValues(DataList data) {
        initDefaultFrom(data);
        initDefaultTo(data);
        initDefaultStep(data);
    }


    protected void initDefaultFrom(DataList data) {
        Data f = getData(data.getAll(), getLowerField());

        double from = getDefaultFrom();

        try {
            from = getDefaultValue(f);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        distancePanel.setFrom(from);
    }


    protected double getDefaultFrom() {
        return min;
    }


    protected void initDefaultTo(DataList data) {
        Data t = getData(data.getAll(), getUpperField());

        double to = getDefaultTo();

        try {
            to = getDefaultValue(t);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        distancePanel.setTo(to);
    }


    protected double getDefaultTo() {
        return max;
    }


    protected void initDefaultStep(DataList data) {
        Data s = getData(data.getAll(), getStepField());

        double step = getDefaultStep();

        try {
            step = getDefaultValue(s);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        distancePanel.setStep(step);
    }


    protected double getDefaultStep() {
        return DEFAULT_STEP_WIDTH;
    }


    /** Gets the double from default in data, null if none. */
    protected double getDefaultValue(Data data)
    throws NumberFormatException
    {
        DataItem def      = data.getDefault();
        String   defValue = def != null ? def.getStringValue() : null;

        return Double.valueOf(defValue);
    }


    protected void initHelperPanel() {
        distancesTable = new RangeTable();
        locationsTable = new LocationsTable();

        Config config = Config.getInstance();
        String url    = config.getServerUrl();
        String river  = getRiverName();

        distancesTable.setAutoFetchData(true);
        locationsTable.setAutoFetchData(true);
        distancesTable.setDataSource(new DistanceInfoDataSource(
            url, river, "distances"));
        locationsTable.setDataSource(new DistanceInfoDataSource(
            url, river, "locations"));

        distancesTable.addRecordClickHandler(new RecordClickHandler() {
            @Override
            public void onRecordClick(RecordClickEvent e) {
                Record r = e.getRecord();

                String from = r.getAttribute("from");
                String to   = r.getAttribute("to");

                try {
                    distancePanel.setFrom(Double.valueOf(from));
                    distancePanel.setTo(Double.valueOf(to));
                }
                catch (NumberFormatException nfe) {
                    SC.warn(MSG.wrongFormat());
                }
            }
        });

        locationsTable.addRecordClickHandler(new RecordClickHandler() {
            @Override
            public void onRecordClick(RecordClickEvent e) {
                Record  r = e.getRecord();
                int field = e.getFieldNum();

                try {
                    String value = r.getAttribute("from");

                    switch (field) {
                    case 0:
                        distancePanel.setFrom(Double.valueOf(value));
                        break;
                    case 1:
                        distancePanel.setTo(Double.valueOf(value));
                        break;
                    }
                }
                catch (NumberFormatException nfe) {
                    SC.warn(MSG.wrongFormat());
                }
            }
        });

        tabs = new TabSet();
        tabs.setWidth100();
        tabs.setHeight100();

        Tab locations = new Tab(MSG.locations());
        Tab distances = new Tab(MSG.distance());

        locations.setPane(locationsTable);
        distances.setPane(distancesTable);

        tabs.addTab(locations, 0);
        tabs.addTab(distances, 1);

        filterResultCount = new StaticTextItem(MSG.resultCount());
        filterResultCount.setTitleAlign(Alignment.LEFT);
        filterResultCount.setTitleStyle("color: #000");

        filterDescription = new TableFilter();
        filterDescription.setHeight("30px");
        filterDescription.addFilterHandler(this);

        filterRange = new RangeTableFilter();
        filterRange.setHeight("30px");
        filterRange.addFilterHandler(this);
        filterRange.setVisible(false);

        SelectItem filterCriteria = new SelectItem();
        filterCriteria.setShowTitle(false);
        filterCriteria.setWidth(100);
        filterCriteria.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent e) {
                if(e.getValue().toString().equals("range")) {
                    filterRange.setVisible(true);
                    filterDescription.setVisible(false);
                    filterDescription.clear();
                }
                else {
                    filterRange.setVisible(false);
                    filterRange.clear();
                    filterDescription.setVisible(true);
                }
            }
        });

        LinkedHashMap<String, String> filterMap =
            new LinkedHashMap<String, String>();
        filterMap.put("description", MSG.description());
        filterMap.put("range", MSG.range());
        filterCriteria.setValueMap(filterMap);
        filterCriteria.setValue("description");

        DynamicForm form = new DynamicForm();
        form.setFields(filterCriteria);

        DynamicForm form2 = new DynamicForm();
        form2.setFields(filterResultCount);

        HLayout filterLayout = new HLayout();
        filterLayout.addMember(form);
        filterLayout.addMember(filterDescription);
        filterLayout.addMember(filterRange);
        filterLayout.setHeight(30);
        tabs.addTabSelectedHandler(new TabSelectedHandler() {
            @Override
            public void onTabSelected(TabSelectedEvent evt) {
                filterDescription.clear();
                filterRange.clear();
                filterResultCount.setValue("");

                Canvas c = evt.getTabPane();
                if(c instanceof ListGrid) {
                    currentFiltered = (ListGrid)c;
                }
            }
        });

        helperContainer.addMember(tabs);
        helperContainer.addMember(filterLayout);
        helperContainer.addMember(form2);
    }


    @Override
    public void onFilterCriteriaChanged(StringFilterEvent event) {
        String search = event.getFilter();

        if (search != null && search.length() > 0) {
            Criteria c = new Criteria("description", search);
            locationsTable.filterData(c);
            distancesTable.filterData(c);
            filterResultCount.setValue(currentFiltered.getRecords().length);
        }
        else {
            locationsTable.clearCriteria();
            distancesTable.clearCriteria();
            filterResultCount.setValue("");
        }
    }


    @Override
    public void onFilterCriteriaChanged(RangeFilterEvent event) {
        Float from = event.getFrom() - 0.001f;
        Float to = event.getTo() + 0.001f;

        Criterion combinedFilter = null;
        Criterion locationFilter = null;

        if (from.equals(Float.NaN) && to.equals(Float.NaN)) {
            locationsTable.clearCriteria();
            distancesTable.clearCriteria();
            filterResultCount.setValue("");
            return;
        }

        if (from.equals(Float.NaN)) {
            combinedFilter =
                new Criterion("to", OperatorId.LESS_OR_EQUAL, to);

            locationFilter =
                new Criterion("from", OperatorId.LESS_OR_EQUAL, to);

            locationsTable.filterData(locationFilter);
            distancesTable.filterData(combinedFilter);
            filterResultCount.setValue(currentFiltered.getRecords().length);
            return;
        }

        if (to.equals(Float.NaN)) {
            combinedFilter =
                new Criterion("from", OperatorId.GREATER_OR_EQUAL, from);
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
        distancesTable.filterData(combinedFilter);
        filterResultCount.setValue(currentFiltered.getRecords().length);

    }


    protected String getRiverName() {
        ArtifactDescription adescr = artifact.getArtifactDescription();
        return adescr.getRiver();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
