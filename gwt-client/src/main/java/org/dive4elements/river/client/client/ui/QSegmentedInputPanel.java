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

import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
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
import java.util.List;
import java.util.Map;


/**
 * This UIProvider creates a widget to enter Q values per segment.
 */
public class QSegmentedInputPanel
extends      AbstractUIProvider
implements   ChangeHandler, BlurHandler
{

    private static final long serialVersionUID = -8627825064071479905L;

    public static final String FIELD_WQ_MODE = "wq_isq";
    public static final String FIELD_WQ_Q    = "Q";

    public static final String GAUGE_SEPARATOR = ":";

    public static final String GAUGE_PART_SEPARATOR = ";";

    public static final String VALUE_SEPARATOR = ",";

    public static final int ROW_HEIGHT = 20;

    /** The constant field name for choosing single values or range.*/
    public static final String FIELD_MODE = "mode";

    /** The constant field value for range input mode.*/
    public static final String FIELD_MODE_RANGE = "range";

    protected WQInfoServiceAsync wqInfoService =
        GWT.create(WQInfoService.class);

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** Stores the input panels related to their keys.*/
    protected Map<String, DoubleArrayPanel> wqranges;

    /** Stores the min/max values for each q range.*/
    protected Map<String, double[]> qranges;

    protected QDTable qdTable;

    protected WTable wTable;

    protected TabSet tabs;


    public QSegmentedInputPanel() {
        wqranges = new HashMap<String, DoubleArrayPanel>();
        qranges  = new HashMap<String, double[]>();
        qdTable  = new QDTable();
        wTable   = new WTable();
    }


    /** Create main UI Canvas. */
    @Override
    public Canvas create(DataList data) {
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

        return layout;
    }


    protected void initHelperPanel() {
        tabs = new TabSet();
        tabs.setWidth100();
        tabs.setHeight100();

        // TODO i18n
        Tab qTab = new Tab("Q / D");

        qTab.setPane(qdTable);
        qdTable.hideIconFields();

        tabs.addTab(qTab, 1);

        helperContainer.addMember(tabs);

        // TODO Q only would suffice.
        fetchWQData();
    }


    /** Create display for passive mode. */
    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> all = dataList.getAll();
        Data    wqData = getData(all, "ranges");

        Canvas back = getBackButton(dataList.getState());

        HLayout valLayout  = new HLayout();
        VLayout vlayout    = new VLayout();
        Label wqLabel      = new Label(dataList.getLabel());

        wqLabel.setValign(VerticalAlignment.TOP);

        wqLabel.setWidth(200);
        wqLabel.setHeight(25);

        valLayout.addMember(wqLabel);
        valLayout.addMember(createOldWQValues(wqData));
        valLayout.addMember(back);

        vlayout.addMember(valLayout);

        return vlayout;
    }


    /** Create canvas showing previously entered values. */
    protected Canvas createOldWQValues(Data wqData) {
        VLayout layout = new VLayout();

        //TODO: Sort by first field, numerically.

        DataItem item  = wqData.getItems()[0];
        String   value = item.getStringValue();

        String[] gauges = value.split(GAUGE_SEPARATOR);

        for (String gauge: gauges) {
            HLayout h = new HLayout();

            String[] parts  = gauge.split(GAUGE_PART_SEPARATOR);
            String[] values = parts[3].split(VALUE_SEPARATOR);

            Label l = new Label(parts[0] + " - " + parts[1] + ": ");

            StringBuilder sb = new StringBuilder();
            boolean    first = true;

            for (String v: values) {
                if (!first) {
                    sb.append(", ");
                }

                sb.append(v);

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


    protected Canvas createWidget(DataList dataList) {
        VLayout layout = new VLayout();

        Canvas list = createList(dataList);

        DataItem[] items = getWQItems(dataList);
        int listHeight   = ROW_HEIGHT * items.length;

        layout.addMember(list);

        layout.setHeight(25 + listHeight);
        layout.setWidth(350);

        return layout;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        for (Map.Entry<String, DoubleArrayPanel> entry: wqranges.entrySet()) {

            String           key = entry.getKey();
            DoubleArrayPanel dap = entry.getValue();

            if (!dap.validateForm()) {
                errors.add(MSG.error_invalid_double_value());
                return errors;
            }

            double[] mm = qranges.get(key);
            if (mm == null) {
                SC.warn(MSG.error_read_minmax_values());
                continue;
            }

            List<String> tmpErrors = new ArrayList<String>();
            double[] values = dap.getInputValues();
            // might geht npe here if one field not filled
            double[] good   = new double[values.length];

            int idx = 0;

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


    protected Canvas createList(DataList dataList) {
        VLayout layout = new VLayout();

        DataItem[] items = getWQItems(dataList);

        for (DataItem item: items) {
            String title = item.getLabel();

            DoubleArrayPanel dap = new DoubleArrayPanel(
                createLineTitle(title),
                null,
                this,
                null,
                TitleOrientation.LEFT);

            wqranges.put(title, dap);

            if (item instanceof WQDataItem) {
                WQDataItem wq = (WQDataItem) item;
                double[] mmQ = wq.getQRange();

                qranges.put(title, mmQ);
            }

            layout.addMember(dap);
        }

        layout.setHeight(items.length * ROW_HEIGHT);

        return layout;
    }


    protected DataItem[] getWQItems(DataList dataList) {
        List<Data> data = dataList.getAll();

        for (Data d: data) {
            String name = d.getLabel();

            // TODO to be gone
            if (name.equals(FIELD_WQ_MODE)) {
                continue;
            }

            return d.getItems();
        }

        return null;
    }



    public String createLineTitle(String key) {
        String[] splitted = key.split(";");

        return splitted[0] + " - " + splitted[1];
    }


    @Override
    public Data[] getData() {
        Data values = getWQValues();

        return new Data[] { values };
    }


    protected Data getWQValues() {
        String wqvalue = null;

        for (Map.Entry<String, DoubleArrayPanel> entry: wqranges.entrySet()) {
            String           key = entry.getKey();
            DoubleArrayPanel dap = entry.getValue();

            double[] values = dap.getInputValues();
            if (wqvalue == null) {
                wqvalue = createValueString(key + "; ", values);
            }
            else {
                wqvalue += GAUGE_SEPARATOR
                    + createValueString(key + "; ", values);
            }
        }

        // TODO probably ranges
        DataItem valueItem = new DefaultDataItem(
            "ranges", "ranges", wqvalue);
        Data values = new DefaultData(
            "ranges", null, null, new DataItem[] { valueItem });

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


    @Override
    public void onBlur(BlurEvent event) {
        DoubleArrayPanel dap = (DoubleArrayPanel) event.getForm();
        dap.validateForm(event.getItem());
    }


    protected void fetchWQData() {
        Config config    = Config.getInstance();
        String locale    = config.getLocale ();

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        double[]  mm = getMinMaxKM(data);
        String river = getRiverName(data);

        wqInfoService.getWQInfo(locale, river, mm[0], mm[0],
            new AsyncCallback<WQInfoObject[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not receive wq informations.");
                    SC.warn(caught.getMessage());
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
                qdTable.addData(rec);
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
