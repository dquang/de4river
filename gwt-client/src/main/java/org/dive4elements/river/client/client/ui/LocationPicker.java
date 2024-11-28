/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.data.AdvancedCriteria;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Criterion;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.FilterHandler;
import org.dive4elements.river.client.client.event.RangeFilterEvent;
import org.dive4elements.river.client.client.event.StringFilterEvent;

/**
 * Bundle widgets and handler for a lacation input helper.
 *
 * Note that the construction is weird and driven by issues that arose due to
 * reasons not understood.
 */
public class LocationPicker
implements   FilterHandler
{
    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** The locations table. */
    protected ListGrid locationTable;

    protected HLayout filterLayout;

    DynamicForm resultCountForm;

    CellClickHandler handler;

    boolean isDistance = false;

    /** Text to show number of matched items when filtered. */
    protected StaticTextItem filterResultCount;


    public LocationPicker(CellClickHandler handler) {
        locationTable = new ListGrid();
        locationTable.setShowHeaderContextMenu(false);
        this.handler = handler;
    }

    public void prepareFilter() {

        filterResultCount = new StaticTextItem(MSG.resultCount());
        filterResultCount.setTitleAlign(Alignment.LEFT);
        filterResultCount.setTitleStyle("color: #000");

        final TableFilter filter = new TableFilter();
        filter.setHeight("30px");
        filter.addFilterHandler(this);

        final RangeTableFilter filterRange = new RangeTableFilter();
        filterRange.setHeight("30px");
        filterRange.addFilterHandler(this);
        filterRange.setVisible(false);

        SelectItem filterCriteria = new SelectItem();
        filterCriteria.setShowTitle(false);
        filterCriteria.setWidth(100);
        filterCriteria.addChangedHandler(new ChangedHandler() {
            public void onChanged(ChangedEvent e) {
                if(e.getValue().toString().equals("range")) {
                    filterRange.setVisible(true);
                    filter.setVisible(false);
                    filter.clear();
                    filterResultCount.setValue("");
                }
                else {
                    filterRange.setVisible(false);
                    filterRange.clear();
                    filter.setVisible(true);
                    filterResultCount.setValue("");
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

        resultCountForm = new DynamicForm();
        resultCountForm.setFields(filterResultCount);

        filterLayout = new HLayout();
        filterLayout.addMember(form);
        filterLayout.addMember(filter);
        filterLayout.addMember(filterRange);
    }


    /** Access the main widget, a table in which locations can be chosen. */
    public ListGrid getLocationTable() {
        return locationTable;
    }


    /** Access the 'form' that shows the filter result count. */
    public DynamicForm getResultCountForm() {
        return resultCountForm;
    }


    /** Access the layout containing filter stuff. */
    public HLayout getFilterLayout() {
        return filterLayout;
    }


    /**
     * This method creates a table that contains the location values.
     */
    public void createLocationTable(/*RecordClickHandler handler*/) {
        GWT.log("Create Location Table in LocationPicker");

        String baseUrl = GWT.getHostPageBaseURL();

        locationTable.setWidth100();
        locationTable.setShowRecordComponents(true);
        locationTable.setShowRecordComponentsByCell(true);
        locationTable.setHeight100();
        locationTable.setEmptyMessage(MSG.empty_filter());
        locationTable.setCanReorderFields(false);

        ListGridField addLocation = new ListGridField ("addlocation", "");
        addLocation.setType (ListGridFieldType.ICON);
        addLocation.setWidth (30);
        addLocation.setCellIcon (baseUrl + MSG.markerGreen());
        ListGridField addTo = new ListGridField ("addto", "");
        addTo.setType (ListGridFieldType.ICON);
        addTo.setWidth (30);
        addTo.setCellIcon (baseUrl + MSG.markerRed());

        ListGridField ldescr = new ListGridField("description",
                MSG.description());
        ldescr.setType(ListGridFieldType.TEXT);
        ldescr.setWidth("*");
        ListGridField lside = new ListGridField("riverside",
                MSG.riverside());
        lside.setType(ListGridFieldType.TEXT);
        lside.setWidth("10%");

        ListGridField loc = new ListGridField("from", MSG.location());
        loc.setCellFormatter(new CellFormatter() {
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
            }
        );
        loc.setType(ListGridFieldType.FLOAT);

        loc.setWidth("10%");

        ListGridField bottom =
            new ListGridField("bottom", MSG.bottom_edge());
        bottom.setType(ListGridFieldType.TEXT);
        bottom.setWidth("10%");

        ListGridField top =
            new ListGridField("top", MSG.top_edge());
        top.setType(ListGridFieldType.TEXT);
        top.setWidth("10%");
        locationTable.addCellClickHandler(handler);
        if (isDistance) {
            locationTable.setFields(
                addLocation, addTo, ldescr, loc, lside, bottom, top);
        }
        else {
            locationTable.setFields(
                addLocation, ldescr, loc, lside, bottom, top);
        }
    }


    @Override
    public void onFilterCriteriaChanged(StringFilterEvent event) {
        String search = event.getFilter();

        if (search != null && search.length() > 0) {
            Criteria c = new Criteria("description", search);
            locationTable.filterData(c);
            filterResultCount.setValue(locationTable.getRecords().length);
        }
        else {
            locationTable.clearCriteria();
            filterResultCount.setValue("");
        }
    }


    @Override
    public void onFilterCriteriaChanged(RangeFilterEvent event) {
        Float from = event.getFrom() - 0.001f;
        Float to = event.getTo() + 0.001f;

        Criterion combinedFilter = null;
        if (from.equals(Float.NaN) && to.equals(Float.NaN)) {
            locationTable.clearCriteria();
            filterResultCount.setValue("");
            return;
        }
        else if (from.equals(Float.NaN)) {
            combinedFilter =
                new Criterion("from", OperatorId.LESS_OR_EQUAL, to);
        }
        else if (to.equals(Float.NaN)) {
            combinedFilter =
                new Criterion("from", OperatorId.GREATER_OR_EQUAL, from);
        }
        else {
            combinedFilter =
                new AdvancedCriteria(OperatorId.AND, new Criterion[] {
                    new Criterion("from", OperatorId.GREATER_OR_EQUAL, from),
                    new Criterion("from", OperatorId.LESS_OR_EQUAL, to)
                });
        }
        locationTable.filterData(combinedFilter);
        filterResultCount.setValue(locationTable.getRecords().length);
    }

    public void setIsDistance(boolean value) {
        this.isDistance = value;
    }

    public boolean isDistance() {
        return this.isDistance;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
