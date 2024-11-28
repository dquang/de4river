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
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.FocusEvent;
import com.smartgwt.client.widgets.form.fields.events.FocusHandler;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.WQInfoService;
import org.dive4elements.river.client.client.services.WQInfoServiceAsync;
import org.dive4elements.river.client.client.ui.wq.QDTable;
import org.dive4elements.river.client.client.ui.wq.WTable;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.WQDataItem;
import org.dive4elements.river.client.shared.model.WQInfoObject;
import org.dive4elements.river.client.shared.model.WQInfoRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * This UIProvider creates a widget to enter W or Q data for discharge
 * longitudinal section computations.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQAdaptedInputPanel
extends      AbstractUIProvider
implements   ChangeHandler, BlurHandler, FocusHandler
{
    private static final long serialVersionUID = -3218827566805476423L;

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);

    public static final String FIELD_WQ_MODE = "wq_isq";
    public static final String FIELD_WQ_W    = "W";
    public static final String FIELD_WQ_Q    = "Q";

    public static final String GAUGE_SEPARATOR = ":";

    public static final String GAUGE_PART_SEPARATOR = ";";

    public static final String VALUE_SEPARATOR = ",";

    public static final int ROW_HEIGHT = 20;

    /** The constant field name for choosing w or q mode. */
    public static final String FIELD_WQ = "wq";

    /** The constant field name for choosing single values or range. */
    public static final String FIELD_MODE = "mode";

    /** The constant field value for range input mode. */
    public static final String FIELD_MODE_RANGE = "range";

    /** Service to fetch W/Q MainValues. */
    protected WQInfoServiceAsync wqInfoService =
        GWT.create(WQInfoService.class);

    /** The message class that provides i18n strings. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** Stores the input panels related to their keys. */
    protected Map<String, DoubleArrayPanel> wqranges;

    /** List of doubleArrayPanels shown. */
    protected ArrayList<DoubleArrayPanel> doubleArrayPanels;

    /** [startkm,endkm] per gauge in selected range. */
    protected double[][] gaugeRanges;

    /** Stores the min/max values for each q range (gauge). */
    protected Map<String, double[]> qranges;

    /** Stores the min/max values for each w range (gauge). */
    protected Map<String, double[]> wranges;

    /** The RadioGroupItem that determines the w/q input mode. */
    protected DynamicForm modes;

    /** List of wTables in inputhelper section. */
    protected List<WTable> wTables;

    /** List of QDTables in inputhelper section. */
    protected List<QDTable> qdTables;

    /** Tabs in inputhelper area. */
    protected TabSet tabs;

    /** The currently focussed Input element. */
    protected DoubleArrayPanel itemWithFocus;


    public WQAdaptedInputPanel() {
        wqranges = new HashMap<String, DoubleArrayPanel>();
        doubleArrayPanels = new ArrayList<DoubleArrayPanel>();
        qranges  = new HashMap<String, double[]>();
        wranges  = new HashMap<String, double[]>();
        wTables  = new ArrayList<WTable>();
        qdTables = new ArrayList<QDTable>();
    }


    /** Create labels, canvasses, layouts. */
    @Override
    public Canvas create(DataList data) {
        readGaugeRanges(data);
        initHelperPanel();

        Canvas submit = getNextButton();
        Canvas widget = createWidget(data);
        Label  label  = new Label(MSG.wqadaptedTitle());

        label.setHeight(25);

        VLayout layout = new VLayout();
        layout.setMembersMargin(10);
        layout.setWidth(350);

        layout.addMember(label);
        layout.addMember(widget);
        layout.addMember(submit);

        fetchWQData();

        initTableListeners();

        // We actually want the first Q tab to be selected and all
        // Q tabs to be enabled. I sense a bug in TabSet here, as
        // the code in the W/Q radiogroup-changehandler behaves
        // exactly vice versa (enabling Q, selecting tab 0).
        enableWTabs();
        tabs.selectTab(1);

        return layout;
    }


    /** Inits the helper panel. */
    // TODO duplicate in WQInputPanel
    protected void initHelperPanel() {
        tabs = new TabSet();
        tabs.setWidth100();
        tabs.setHeight100();

        // For each gauge, add two tabs with helper tables.

        for (int i = 0; i< gaugeRanges.length; i++) {
            // Later the tabs title will get adjusted to include gauges name.
            // TODO the tabs title becomes rather long through that (i18n).
            Tab wTab = new Tab(MESSAGE.wq_table_w());
            Tab qTab = new Tab(MESSAGE.wq_table_q());

            QDTable qdTable = new QDTable();
            WTable  wTable  = new WTable();

            wTables.add(wTable);
            qdTables.add(qdTable);

            qdTable.showSelect();
            wTable.showSelect();
            wTab.setPane(wTable);
            qTab.setPane(qdTable);

            tabs.addTab(wTab, i*2+0);
            tabs.addTab(qTab, i*2+1);
            //tabs.disableTab(i*2+1);
        }

        // Defaults at "Q", first input field.
        tabs.selectTab(0);
        enableQTabs();

        helperContainer.addMember(tabs);
    }


    /**
     * Initializes the listeners of the WQD tables.
     */
    // TODO dupe from WQInputPanel
    protected void initTableListeners() {
        int i = 0;
        for (QDTable qdTable: qdTables) {
            // Register listener such that values are filled in on click.
            final QDTable table = qdTable;
            final int fi = i;
            CellClickHandler handler = new CellClickHandler() {
                @Override
                public void onCellClick(CellClickEvent e) {
                    if (isWMode() || table.isLocked()) {
                        return;
                    }

                    Record r   = e.getRecord();
                    double val = r.getAttributeAsDouble("value");

                    doubleArrayPanels.get(fi).setValues(new double[]{val});
                    // If a named value for first gauge is chosen,
                    // try to find and set
                    // the values to the other panels too.
                    if (fi == 0) {
                        String valueName = r.getAttribute("name");
                        int oi = 0;
                        // TODO instead of oi use random access.
                        for (QDTable otherQDTable: qdTables) {
                            if (oi == 0) {
                                oi++;
                                continue;
                            }
                            Double value = otherQDTable.findRecordValue(
                                valueName);
                            if (value == null) {
                                SC.warn(MSG.noMainValueAtGauge());
                            }
                            else {
                                doubleArrayPanels.get(oi).setValues(
                                    new double[]{value});
                            }
                            oi++;
                        }
                    }
                    else {
                        // Focus next.
                        if (fi != doubleArrayPanels.size()-1) {
                            doubleArrayPanels.get(fi+1).focusInItem(1);
                        }
                    }
                }
            };

            qdTable.addCellClickHandler(handler);
            i++;
        }

        i = 0;
        for (WTable wTable: wTables) {
            // Register listener such that values are filled in on click.
            final WTable table = wTable;
            final int fi = i;
            CellClickHandler handler = new CellClickHandler() {
                @Override
                public void onCellClick(CellClickEvent e) {
                    if (!isWMode() /*|| table.isLocked()*/) {
                        return;
                    }

                    Record r   = e.getRecord();
                    double val = r.getAttributeAsDouble("value");

                    doubleArrayPanels.get(fi).setValues(new double[]{val});
                    // If a named value for first gauge is chosen,
                    // try to find and set
                    // the values to the other panels too.
                    if (fi == 0) {
                        String valueName = r.getAttribute("name");
                        int oi = 0;
                        // TODO instead of oi use random access.
                        for (WTable otherWTable: wTables) {
                            if (oi == 0) {
                                oi++;
                                continue;
                            }
                            Double value = otherWTable.findRecordValue(
                                valueName);
                            if (value == null) {
                                // TODO: afterwards it freaks out
                                SC.warn(MSG.noMainValueAtGauge());
                            }
                            else {
                                doubleArrayPanels.get(oi).setValues(
                                    new double[]{value});
                            }
                            oi++;
                        }
                    }
                    else {
                        // Focus next.
                        if (fi != doubleArrayPanels.size()-1) {
                            doubleArrayPanels.get(fi+1).focusInItem(1);
                        }
                    }
                }
            };

            wTable.addCellClickHandler(handler);
            i++;
        }
    }


    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> all = dataList.getAll();
        Data    wqData = getData(all, "wq_values");
        Data    wqMode = getData(all, "wq_isq");
        boolean isQ = wqMode.getItems()[0].getStringValue().equals("true");
        Canvas back = getBackButton(dataList.getState());

        HLayout valLayout  = new HLayout();
        HLayout modeLayout = new HLayout();
        VLayout vlayout    = new VLayout();

        Label wqLabel   = new Label(dataList.getLabel());
        Label modeLabel = new Label("");

        wqLabel.setValign(VerticalAlignment.TOP);

        wqLabel.setWidth(200);
        wqLabel.setHeight(25);
        modeLabel.setHeight(25);
        modeLabel.setWidth(200);

        valLayout.addMember(wqLabel);
        valLayout.addMember(createOldWQValues(wqData, isQ));

        valLayout.addMember(back);
        modeLayout.addMember(modeLabel);

        vlayout.addMember(valLayout);
        vlayout.addMember(modeLayout);

        return vlayout;
    }


    /** Create area showing previously entered w or q data. */
    protected Canvas createOldWQValues(Data wqData, boolean isQ) {
        VLayout layout = new VLayout();

        DataItem item  = wqData.getItems()[0];
        String   value = item.getStringValue();

        String[] gauges = value.split(GAUGE_SEPARATOR);

        String unit = isQ ? "m³/s" : "cm";

        for (String gauge: gauges) {
            HLayout h = new HLayout();

            String[] parts  = gauge.split(GAUGE_PART_SEPARATOR);
            String[] values = parts[3].split(VALUE_SEPARATOR);

            Label l = new Label(parts[2] + ": ");

            StringBuilder sb = new StringBuilder();
            boolean    first = true;

            for (String v: values) {
                if (!first) {
                    sb.append(", ");
                }

                sb.append(v);
                sb.append(" ");
                sb.append(unit);

                first = false;
            }

            Label v = new Label(sb.toString());

            l.setWidth(65);
            v.setWidth(65);

            h.addMember(l);
            h.addMember(v);

            layout.addMember(h);
        }

        return layout;
    }


    /** Create non-input helper part of the UI. */
    protected Canvas createWidget(DataList dataList) {
        VLayout layout = new VLayout();

        Canvas mode = createMode(dataList);
        Canvas list = createList(dataList);

        DataItem[] items = getWQItems(dataList);
        int listHeight   = ROW_HEIGHT * items.length;

        mode.setHeight(25);
        mode.setWidth(200);

        layout.addMember(mode);
        layout.addMember(list);

        layout.setHeight(25 + listHeight);
        layout.setWidth(350);

        initUserDefaults(dataList);

        return layout;
    }


    @Override
    public List<String> validate() {
        if (isWMode()) {
            return validateW();
        }
        else {
            return validateQ();
        }
    }


    protected List<String> validateRange(Map<String, double[]> ranges) {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        for (Map.Entry<String, DoubleArrayPanel> entry: wqranges.entrySet()) {

            String           key = entry.getKey();
            DoubleArrayPanel dap = entry.getValue();

            if (!dap.validateForm()) {
                errors.add(MSG.error_invalid_double_value());
                return errors;
            }

            double[] mm  = ranges.get(key);
            if (mm == null) {
                SC.warn(MSG.error_read_minmax_values());
                continue;
            }

            double[] values = dap.getInputValues();
            double[] good   = new double[values.length];

            int idx = 0;

            List<String> tmpErrors = new ArrayList<String>();
            for (double value: values) {
                if (value < mm[0] || value > mm[1]) {
                    String tmp = MSG.error_validate_range();
                    tmp = tmp.replace("$1", nf.format(value));
                    tmp = tmp.replace("$2", nf.format(mm[0]));
                    tmp = tmp.replace("$3", nf.format(mm[1]));
                    tmpErrors.add(tmp);
                }
                else {
                    good[idx++] = value;
                }
            }

            double[] justGood = new double[idx];
            for (int i = 0; i < justGood.length; i++) {
                justGood[i] = good[i];
            }

            if (!tmpErrors.isEmpty()) {
                dap.setValues(justGood);

                errors.addAll(tmpErrors);
            }
        }

        return errors;
    }


    protected List<String> validateW() {
        return validateRange(wranges);
    }


    protected List<String> validateQ() {
        return validateRange(qranges);
    }


    protected void initUserDefaults(DataList dataList) {
        initUserWQValues(dataList);
        initUserWQMode(dataList);
    }


    protected void initUserWQMode(DataList dataList) {
        List<Data> allData = dataList.getAll();

        Data     dDef  = getData(allData, "wq_mode");
        DataItem def   = dDef != null ? dDef.getDefault() : null;
        String   value = def != null ? def.getStringValue() : null;

        if (value != null && value.equals(FIELD_WQ_W)) {
            modes.setValue(FIELD_WQ_MODE, FIELD_WQ_W);
        }
        else {
            modes.setValue(FIELD_WQ_MODE, FIELD_WQ_Q);
        }
    }


    protected void initUserWQValues(DataList dataList) {
        List<Data> allData = dataList.getAll();

        Data     dDef  = getData(allData, "wq_values");
        DataItem def   = dDef != null ? dDef.getDefault() : null;
        String   value = def != null ? def.getStringValue() : null;

        if (value == null || value.length() == 0) {
            return;
        }

        String[] lines = value.split(GAUGE_SEPARATOR);

        if (lines == null || lines.length == 0) {
            return;
        }

        for (String line: lines) {
            String[] cols  = line.split(GAUGE_PART_SEPARATOR);
            String   title = createLineTitle(line);

            if (cols == null || cols.length < 3) {
                continue;
            }

            String[] strValues = cols[2].split(VALUE_SEPARATOR);
            double[] values    = new double[strValues.length];

            int idx = 0;

            for (String strValue: strValues) {
                try {
                    values[idx++] = Double.valueOf(strValue);
                }
                catch (NumberFormatException nfe) {
                    // do nothing
                }
            }

            String           key = cols[0] + GAUGE_PART_SEPARATOR + cols[1];
            DoubleArrayPanel dap = wqranges.get(key);

            if (dap == null) {
                continue;
            }

            dap.setValues(values);
        }
    }

    /** Populate Gauge Ranges array. */
    private void readGaugeRanges(DataList dataList) {
        DataItem[] items = getWQItems(dataList);
        gaugeRanges = new double[items.length][2];

        int i = 0;

        for (DataItem item: items) {
            String[] startEndKm = item.getLabel().split(";");

            gaugeRanges[i][0] = Double.parseDouble(startEndKm[0]);
            gaugeRanges[i][1] = Double.parseDouble(startEndKm[1]);
            i++;
        }
    }


    protected Canvas createList(DataList dataList) {
        VLayout layout = new VLayout();

        DataItem[] items = getWQItems(dataList);

        int i = 0;

        for (DataItem item: items) {
            String title = item.getLabel(); // of form: 70.5;112.0
            String label = item.getStringValue();

            // Rename W and Q tab to include gauges name.
            tabs.getTab(i*2).setTitle(tabs.getTab(i*2).getTitle()
                + " (" + label + ")");
            tabs.getTab(i*2+1).setTitle(tabs.getTab(i*2+1).getTitle()
                + " (" + label + ")");

            DoubleArrayPanel dap = new DoubleArrayPanel(
                label, null, this, this, TitleOrientation.LEFT);

            wqranges.put(title, dap);
            doubleArrayPanels.add(dap);

            if (item instanceof WQDataItem) {
                WQDataItem wq = (WQDataItem) item;
                double[] mmQ = wq.getQRange();
                double[] mmW = wq.getWRange();

                qranges.put(title, mmQ);
                wranges.put(title, mmW);
            }

            layout.addMember(dap);
            i++;
        }

        layout.setHeight(items.length * ROW_HEIGHT);

        return layout;
    }


    /** Get items which are not WQ_MODE. */
    protected DataItem[] getWQItems(DataList dataList) {
        List<Data> data = dataList.getAll();

        for (Data d: data) {
            String name = d.getLabel();

            if (name.equals(FIELD_WQ_MODE)) {
                continue;
            }

            return d.getItems();
        }

        return null;
    }


    /**
     * Create radio button for switching w and q input.
     * Radiobutton-change also triggers helper panel tab selection.
     */
    protected Canvas createMode(DataList dataList) {
        RadioGroupItem wq = new RadioGroupItem(FIELD_WQ_MODE);
        wq.setShowTitle(false);
        wq.setVertical(false);
        wq.setWidth(200);

        LinkedHashMap wqValues = new LinkedHashMap();
        wqValues.put(FIELD_WQ_W, MSG.wqW());
        wqValues.put(FIELD_WQ_Q, MSG.wqQatGauge());

        wq.setValueMap(wqValues);

        modes = new DynamicForm();
        modes.setFields(wq);
        modes.setWidth(200);
        wq.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent e) {
                DynamicForm form = e.getForm();

                if(form.getValueAsString(FIELD_WQ_MODE).contains("Q")) {
                    tabs.selectTab(0);
                    enableQTabs();
                }
                else {
                    tabs.selectTab(1);
                    enableWTabs();
                }
            }
        });


        LinkedHashMap initial = new LinkedHashMap();
        initial.put(FIELD_WQ_MODE, FIELD_WQ_Q);
        modes.setValues(initial);
        tabs.selectTab(1);
        return modes;
    }


    public void enableWTabs() {
        for (int i = 0; i < doubleArrayPanels.size(); i++) {
            tabs.disableTab(2*i);
            tabs.enableTab(2*i+1);
        }
    }


    public void enableQTabs() {
        for (int i = 0; i < doubleArrayPanels.size(); i++) {
            tabs.enableTab(2*i);
            tabs.disableTab(2*i+1);
        }
    }


    public String createLineTitle(String key) {
        String[] splitted = key.split(";");

        return splitted[0] + " - " + splitted[1];
    }


    @Override
    public Data[] getData() {
        Data mode   = getWQMode();
        Data values = getWQValues();

        return new Data[] { mode, values };
    }


    public boolean isWMode() {
        String mode = (String) modes.getValue(FIELD_WQ_MODE);

        return FIELD_WQ_W.equals(mode);
    }


    protected Data getWQMode() {
        String wqMode = modes.getValueAsString(FIELD_WQ_MODE);
        String value = "false";
        if (wqMode.equals("Q")) {
            value = "true";
        }
        DataItem item = new DefaultDataItem("wq_isq", "wq_isq", value);
        Data mode = new DefaultData(
            "wq_isq", null, null, new DataItem[] { item });

        return mode;
    }


    protected Data getWQValues() {
        String wqvalue = null;

        for (Map.Entry<String, DoubleArrayPanel> entry: wqranges.entrySet()) {
            String           key = entry.getKey();
            DoubleArrayPanel dap = entry.getValue();
            String label = dap.getItemTitle();

            double[] values = dap.getInputValues();
            if (wqvalue == null) {
                wqvalue = createValueString(key + ";" + label, values);
            }
            else {
                wqvalue += GAUGE_SEPARATOR
                    + createValueString(key + ";" + label, values);
            }
        }

        DataItem valueItem = new DefaultDataItem(
            "wq_values", "wq_values", wqvalue);
        Data values = new DefaultData(
            "wq_values", null, null, new DataItem[] { valueItem });

        return values;
    }


    protected String createValueString(String key, double[] values) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;

        for (double value: values) {
            if (!first) {
                sb.append(",");
            }

            sb.append(Double.toString(value));

            first = false;
        }

        return key + ";" + sb.toString();
    }


    @Override
    public void onChange(ChangeEvent event) {
        // TODO IMPLEMENT ME
    }


    /** Store the currently focussed DoubleArrayPanel and focus helper tab. */
    @Override
    public void onFocus(FocusEvent event) {
        itemWithFocus = (DoubleArrayPanel) event.getForm();
        // Switch to respective tab.
        // TODO which makes a focus loss
        int inputIndex = doubleArrayPanels.indexOf(itemWithFocus);
        tabs.selectTab(inputIndex*2 + (isWMode() ? 0 : 1));
    }


    @Override
    public void onBlur(BlurEvent event) {
        DoubleArrayPanel dap = (DoubleArrayPanel) event.getForm();
        dap.validateForm(event.getItem());
    }


    /** Get the WQD data from service and stuck them up that tables. */
    protected void fetchWQData() {
        Config config    = Config.getInstance();
        String locale    = config.getLocale ();

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        double[]  mm = getMinMaxKM(data);
        String river = getRiverName(data);

        int i = 0;

        // Get Data for respective gauge.
        for (double[] range : gaugeRanges){
            // Gauge ranges overlap, move start and end a bit closer
            // to each other.
            final double rDiff = (range[1] - range[0]) / 10d;
            final int fi = i;
            wqInfoService.getWQInfo(
                locale, river, range[0]+rDiff, range[1]-rDiff,
                new AsyncCallback<WQInfoObject[]>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not recieve wq informations.");
                        SC.warn(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(WQInfoObject[] wqi) {
                        int num = wqi != null ? wqi.length :0;
                        GWT.log("Received " + num
                            + " wq informations (" + fi + ".");

                        if (num == 0) {
                            return;
                        }

                        addWQInfo(wqi, fi);
                    }
                }
            );
            i++;
        }
    }


    /** Add Info to helper table for gauge at index gaugeIdx. */
    protected void addWQInfo (WQInfoObject[] wqi, int gaugeIdx) {
        for(WQInfoObject wi: wqi) {
            WQInfoRecord rec = new WQInfoRecord(wi);

            if (wi.getType().equals("W")) {
                wTables.get(gaugeIdx).addData(rec);
            }
            else {
                qdTables.get(gaugeIdx).addData(rec);
            }
        }
    }


    /**
     * Determines the min and max kilometer value selected in a former state. A
     * bit silly, but we need to run over each value of the "old data" to find
     * such values because it is not available here.
     *
     * @param data The DataList which contains the whole data inserted for the
     * current artifact.
     *
     * @return a double array with [min, max].
     */
    protected double[] getMinMaxKM(DataList[] data) {
        ArtifactDescription adesc = artifact.getArtifactDescription();
        return adesc.getKMRange();
    }


    /**
     * Returns the name of the selected river.
     *
     * @param data The DataList with all data.
     *
     * @return the name of the current river.
     */
    protected String getRiverName(DataList[] data) {
        ArtifactDescription adesc = artifact.getArtifactDescription();
        return adesc.getRiver();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
