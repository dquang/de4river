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

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.GaugeInfoService;
import org.dive4elements.river.client.client.services.GaugeInfoServiceAsync;
import org.dive4elements.river.client.client.services.WQInfoService;
import org.dive4elements.river.client.client.services.WQInfoServiceAsync;
import org.dive4elements.river.client.client.ui.wq.ClickableQDTable;
import org.dive4elements.river.client.client.ui.wq.ClickableWTable;
import org.dive4elements.river.client.client.ui.wq.ClickableWTable.ClickMode;
import org.dive4elements.river.client.client.widgets.DischargeTablesChart;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.DoubleArrayData;
import org.dive4elements.river.client.shared.model.Gauge;
import org.dive4elements.river.client.shared.model.IntegerOptionsData;
import org.dive4elements.river.client.shared.model.WQInfoObject;
import org.dive4elements.river.client.shared.model.WQInfoRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * An UIProvider for inserting a mode for W or Q and an array of values for each
 * mode.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQSimpleArrayPanel
extends      AbstractUIProvider
implements   BlurHandler
{
    private static final long serialVersionUID = 3223369196267895010L;

    public static final String FIELD_MODE   = "field_mode";
    public static final String FIELD_VALUES = "field_values";

    public static final int MODE_W = 0;

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected GaugeInfoServiceAsync gaugeService =
        GWT.create(GaugeInfoService.class);

    protected WQInfoServiceAsync wqInfoService =
        GWT.create(WQInfoService.class);

    protected String modeName;
    protected String valuesName;

    protected Canvas valuesWrapper;

    protected TabSet tabs;

    protected DynamicForm      modeForm;
    protected DoubleArrayPanel panelW;
    protected DoubleArrayPanel panelQ;

    protected ClickableWTable wTable;
    protected ClickableQDTable qTable;


    @Override
    public Canvas create(DataList data) {
        VLayout rootLayout = new VLayout();
        rootLayout.addMember(createLabel(data));
        rootLayout.addMember(createModeForm(data));
        rootLayout.addMember(createValuesForm(data));
        rootLayout.addMember(getNextButton());

        initializeMode(data);
        initializeTables();
        initializeHelperPanel();

        return rootLayout;
    }


    @Override
    public Canvas createOld(DataList dataList) {
        IntegerOptionsData modeData   = findOptionsData(dataList);
        DoubleArrayData    valuesData = findValuesData(dataList);

        DataItem[] modeItems   = modeData.getItems();

        HLayout layout         = new HLayout();
        VLayout valueContainer = new VLayout();

        Label label = new Label(dataList.getLabel());
        label.setWidth(200);
        label.setHeight(20);

        Label mode = new Label(modeItems[0].getLabel());
        mode.setHeight(20);
        mode.setWidth(150);

        Canvas values = createOldValues(modeData, valuesData);
        values.setWidth(150);

        valueContainer.addMember(mode);
        valueContainer.addMember(values);

        layout.addMember(label);
        layout.addMember(valueContainer);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }


    @Override
    protected Data[] getData() {
        Data mode = getModeAsData();

        if (getMode() == MODE_W) {
            return new Data[] { mode, getWValuesAsData() };
        }
        else {
            return new Data[] { mode, getQValuesAsData() };
        }
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        // TODO IMPLEMENT ME

        return errors;
    }


    @Override
    public void onBlur(BlurEvent event) {
        // TODO TRIGGER VALIDATION HERE
    }


    protected void initializeMode(DataList dataList) {
        IntegerOptionsData data  = findOptionsData(dataList);
        DataItem[]         items = data != null ? data.getItems() : null;

        if (items != null) {
            String value = items[0].getStringValue();
            modeForm.setValue(modeName, value);
            switchMode(value);
        }
    }


    /** Initialize the w/q/d tables for the helper area. */
    protected void initializeTables() {
        wTable = new ClickableWTable(new ClickableWTable.WClickedListener() {
            @Override
            public void clickedUpper(double value) {
                // nothing to do here
            }

            @Override
            public void clickedLower(double value) {
                panelW.addValue(value);
            }
        }, ClickMode.SINGLE, true);

        qTable = new ClickableQDTable(new ClickableQDTable.QClickedListener() {

            @Override
            public void clickedUpper(double value) {
                // nothing to do here
            }

            @Override
            public void clickedLower(double value) {
               panelQ.addValue(value);
            }
        }, ClickableQDTable.ClickMode.SINGLE);

        fetchWQData();
    }


    /** Put interactive tables to the helper area. */
    protected void initializeHelperPanel() {
        tabs = new TabSet();
        tabs.setWidth100();
        tabs.setHeight100();

        Tab w = new Tab(MSG.wq_table_w());
        Tab q = new Tab(MSG.wq_table_q());
        Tab c = new Tab(MSG.discharge_tables_chart());

        w.setPane(wTable);
        q.setPane(qTable);
        c.setPane(new DischargeTablesChart(this.artifact));

        tabs.addTab(w, 0);
        tabs.addTab(q, 1);
        tabs.addTab(c, 2);

        helperContainer.addMember(tabs);
    }


    protected void fetchWQData() {
        ArtifactDescription desc = artifact.getArtifactDescription();

        final String river    = desc.getRiver();
        final String refGauge = desc.getReferenceGauge();

        gaugeService.getGaugeInfo(
            river,
            refGauge,
            new AsyncCallback<List<Gauge>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    GWT.log("ERROR WHILE FETCHING GAUGES!");
                }

                @Override
                public void onSuccess(List<Gauge> gauges) {
                    Gauge g = gauges.get(0);
                    updateWQData(river, g.getLower(), g.getUpper());
                }
            }
        );
    }


    protected void updateWQData(String river, double lower, double upper) {
        GWT.log("FETCH WQ INFO FOR " + lower + " - " + upper + " now!");

        Config config = Config.getInstance();
        String locale = config.getLocale();

        wqInfoService.getWQInfo(locale, river, lower, upper,
            new AsyncCallback<WQInfoObject[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not receive wq informations.");
                }

                @Override
                public void onSuccess(WQInfoObject[] wqi) {
                    int num = wqi != null ? wqi.length :0;
                    GWT.log("Received " + num + " wq informations.");

                    if (num == 0) {
                        return;
                    }

                    addWQInfo(wqi);
                }
            }
        );
    }


    protected void addWQInfo (WQInfoObject[] wqi) {
        for(WQInfoObject wi: wqi) {
            WQInfoRecord rec = new WQInfoRecord(wi);

            if (wi.getType().equals("W")) {
                wTable.addData(rec);
            }
            else {
                qTable.addData(rec);
            }
        }
    }


    protected Canvas createLabel(DataList dataList) {
        Label label = new Label(MSG.wqHistorical());
        label.setWidth100();
        label.setHeight(25);

        return label;
    }


    protected Canvas createOldValues(
        IntegerOptionsData modeData,
        DoubleArrayData valuesData
    ) {
        NumberFormat nf = NumberFormat.getDecimalFormat();

        DataItem[] items = modeData.getItems();
        String unit = items[0].getStringValue().equals("0") ? "cm" : "m³/s";

        VLayout layout = new VLayout();

        for (double val: valuesData.getValues()) {
            Label tmp = new Label(nf.format(val) + " " + unit);
            tmp.setHeight(20);
            layout.addMember(tmp);
        }

        return layout;
    }


    protected DoubleArrayData findValuesData(DataList dataList) {
        for (int i = 0, n = dataList.size(); i < n; i++) {
            Data tmp = dataList.get(i);

            if (tmp instanceof DoubleArrayData) {
                return (DoubleArrayData) tmp;
            }
        }

        return null;
    }


    protected IntegerOptionsData findOptionsData(DataList dataList) {
        for (int i = 0, n = dataList.size(); i < n; i++) {
            Data tmp = dataList.get(i);

            if (tmp instanceof IntegerOptionsData) {
                return (IntegerOptionsData) tmp;
            }
        }

        return null;
    }


    protected Canvas createModeForm(DataList dataList) {
        IntegerOptionsData data = findOptionsData(dataList);
        DataItem[]         opts = data != null ? data.getItems() : null;

        if (data == null || opts == null) {
            return new Label("NO MODES FOUND");
        }

        modeName = data.getLabel();
        modeForm = new DynamicForm();

        RadioGroupItem items = new RadioGroupItem(data.getLabel());
        LinkedHashMap values = new LinkedHashMap();

        for (DataItem opt: opts) {
            values.put(opt.getStringValue(), opt.getLabel());
        }

        items.setValueMap(values);
        items.setVertical(false);
        items.setShowTitle(false);
        items.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                switchMode((String) event.getValue());
            }
        });

        modeForm.setFields(items);

        return modeForm;
    }


    protected Canvas createValuesForm(DataList dataList) {
        DoubleArrayData data = findValuesData(dataList);

        if (data == null) {
            return new Label("NO VALUES GIVEN!");
        }

        valuesName = data.getLabel();
        panelW     = new DoubleArrayPanel(MSG.unitWSingle(), null, this);
        panelQ     = new DoubleArrayPanel(MSG.unitQSingle(), null, this);

        valuesWrapper = new Canvas();
        valuesWrapper.setWidth100();
        valuesWrapper.setHeight(35);

        return valuesWrapper;
    }


    public void switchMode(String newMode) {
        for (Canvas child: valuesWrapper.getChildren()) {
            valuesWrapper.removeChild(child);
        }

        if (newMode.equals("0")) {
            valuesWrapper.addChild(panelW);
            showWTable();
        }
        else if (newMode.equals("1")) {
            valuesWrapper.addChild(panelQ);
            showQDTable();
        }
    }

    public void showWTable() {
        if (tabs != null) {
            tabs.selectTab(0);
        }
    }

    public void showQDTable() {
        if (tabs != null) {
            tabs.selectTab(1);
        }
    }


    public String getModeAsString() {
        return (String) modeForm.getValue(modeName);
    }


    public int getMode() {
        String modeValue = getModeAsString();

        try {
            return Integer.valueOf(modeValue);
        }
        catch (NumberFormatException nfe) {
            // do something
        }
        catch (NullPointerException npe) {
            // do something
        }

        return -1;
    }


    public Data getModeAsData() {
        String  value = getModeAsString();
        DataItem item = new DefaultDataItem(value, value, value);

        return new DefaultData(modeName, null, null, new DataItem[] { item });
    }


    public Data getWValuesAsData() {
        double[] values = panelW.getInputValues();
        String valueStr = getStringValue(values);

        DataItem item = new DefaultDataItem(valueStr, valueStr, valueStr);

        return new DefaultData(valuesName, null, null, new DataItem[] { item });
    }


    public Data getQValuesAsData() {
        double[] values = panelQ.getInputValues();
        String valueStr = getStringValue(values);

        DataItem item = new DefaultDataItem(valueStr, valueStr, valueStr);

        return new DefaultData(valuesName, null, null, new DataItem[] { item });
    }


    protected static String getStringValue(double[] values) {
        StringBuilder sb = new StringBuilder();
        boolean    first = true;

        for (double value: values) {
            if (first) {
                sb.append(String.valueOf(value));
                first = false;
            }
            else {
                sb.append(";" + String.valueOf(value));
            }
        }

        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
