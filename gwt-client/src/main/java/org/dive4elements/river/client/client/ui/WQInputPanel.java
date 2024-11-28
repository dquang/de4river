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
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
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
import org.dive4elements.river.client.shared.model.WQInfoObject;
import org.dive4elements.river.client.shared.model.WQInfoRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * This UIProvider creates a widget to enter W or Q data.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQInputPanel
extends      AbstractUIProvider
implements   ChangeHandler, BlurHandler
{
    private static final long serialVersionUID = 4797387993390350341L;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);

    protected WQInfoServiceAsync wqInfoService =
        GWT.create(WQInfoService.class);

    /** The constant field name for choosing w or q mode.*/
    public static final String FIELD_WQ_W_FREE = "WFREE";

    /** The constant field name for choosing w or q mode.*/
    public static final String FIELD_WQ = "wq";

    /** The constant field value for W input mode.*/
    public static final String FIELD_WQ_W = "W";

    /** The constant field value for Q input mode.*/
    public static final String FIELD_WQ_Q = "Q";

    /** The constant field value for Q input mode.*/
    public static final String FIELD_WQ_Q_FREE = "QFREE";

    /** The constant field name for choosing single values or range.*/
    public static final String FIELD_MODE = "mode";

    /** The constant field value for single input mode.*/
    public static final String FIELD_MODE_SINGLE = "single";

    /** The constant field value for range input mode.*/
    public static final String FIELD_MODE_RANGE = "range";

    /** The constant value that determines the width of the left panel.*/
    public static final int WIDTH_LEFT_UPPER = 400;

    public static final int WIDTH_LEFT_LOWER = 223;

    /** The container that manages the w and q panels.*/
    protected HLayout container;

    /** The RadioGroupItem that determines the w/q input mode.*/
    protected DynamicForm modes;

    /** The min values for the 'from' property in the W-Range input mode.*/
    protected double minW;

    /** The min values for the 'from' property in the Q-Range input mode.*/
    protected double minQ;

    /** The min value for the 'from' property in the free Q-Range input mode.*/
    protected double minQFree;

    /** The min value for the 'from' property in the free W-Range input mode.*/
    protected double minWFree;

    /** The max values for the 'from' property in the W-Range input mode.*/
    protected double maxW;

    /** The max values for the 'from' property in the Q-Range input mode.*/
    protected double maxQ;

    /** The max value for the 'from' property in the free Q-Range input mode.*/
    protected double maxQFree;

    /** The max value for the 'from' property in the free W-Range input mode.*/
    protected double maxWFree;

    /** The 'from' value entered in the range W mode.*/
    protected double fromW;

    /** The 'to' value entered in the range W mode.*/
    protected double toW;

    /** The 'step' value entered in the range W mode.*/
    protected double stepW;

    /** The values entered in the single W mode.*/
    protected double[] valuesW;

    /** The values entered in the single W mode.*/
    protected double[] valuesWFree;

    /** The 'from' value entered in the range Q mode.*/
    protected double fromQ;

    /** The 'from' value entered in the range free Q mode.*/
    protected double fromQFree;

    /** The 'from' value entered in the range free W mode.*/
    protected double fromWFree;

    /** The 'to' value entered in the range Q mode.*/
    protected double toQ;

    /** The 'to' value entered in the range free Q mode.*/
    protected double toQFree;

    /** The 'to' value entered in the range free W mode.*/
    protected double toWFree;

    /** The 'step' value entered in the range Q mode.*/
    protected double stepQ;

    /** The 'step' value entered in the range free Q mode.*/
    protected double stepQFree;

    /** The 'step' value entered in the range free W mode.*/
    protected double stepWFree;

    /** The values entered in the single Q mode.*/
    protected double[] valuesQ;

    /** The values entered in the single free Q mode.*/
    protected double[] valuesQFree;

    /** The input panel for W values*/
    protected DoubleArrayPanel wArrayPanel;

    /** The input panel for q values*/
    protected DoubleArrayPanel qArrayPanel;

    /** The input panel for free q values*/
    protected DoubleArrayPanel qFreeArrayPanel;

    /** The input panel for free w values*/
    protected DoubleArrayPanel wFreeArrayPanel;

    /** The input panel for w range*/
    protected DoubleRangePanel wRangePanel;

    /** The input panel for q range*/
    protected DoubleRangePanel qRangePanel;

    /** The input panel for free q range*/
    protected DoubleRangePanel qFreeRangePanel;

    /** The input panel for free w range*/
    protected DoubleRangePanel wFreeRangePanel;

    protected QDTable qdTable;

    protected WTable wTable;

    protected TabSet tabs;

    /**
     * Creates a new WQInputPanel instance.
     */
    public WQInputPanel() {
        qdTable      = new QDTable();
        wTable       = new WTable();

        initTableListeners();
    }


    /**
     * Initializes the listeners of the WQD tables.
     */
    protected void initTableListeners() {
        CellClickHandler handler = new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent e) {
                if (isWMode() || qdTable.isLocked()) {
                    return;
                }

                int    idx = e.getColNum();
                Record r   = e.getRecord ();
                double val = r.getAttributeAsDouble("value");

                if (idx == 0) {
                    if (isRangeMode()) {
                        qRangePanel.setFrom(val);
                    }
                    else {
                        qArrayPanel.addValue(val);
                    }
                }
                else if (idx == 1) {
                    if (isRangeMode()) {
                        qRangePanel.setTo(val);
                    }
                    else {
                        qArrayPanel.addValue(val);
                    }
                }
            }
        };

        qdTable.addCellClickHandler(handler);
    }


    /**
     * This method calls createWidget and puts a 'next' button to the bottom.
     *
     * @param data The data that is displayed.
     *
     * @return the widget.
     */
    @Override
    public Canvas create(DataList data) {
        initDefaults(data);

        Canvas  widget = createWidget(data);
        Canvas  submit = getNextButton();
        Label   label  = new Label(MESSAGE.wqTitle());

        label.setHeight(25);

        VLayout layout = new VLayout();
        layout.setMembersMargin(10);

        layout.addMember(label);
        layout.addMember(widget);
        layout.addMember(submit);

        initHelperPanel();
        initUserDefaults(data);

        return layout;
    }


    /** Inits the helper panel. */
    // TODO duplicate in WQAdaptedInputPanel
    protected void initHelperPanel() {
        tabs = new TabSet();
        tabs.setWidth100();
        tabs.setHeight100();

        Tab wTab = new Tab(MESSAGE.wq_table_w());
        Tab qTab = new Tab(MESSAGE.wq_table_q());

        qdTable.showSelect();
        wTab.setPane(wTable);
        qTab.setPane(qdTable);

        tabs.addTab(wTab, 0);
        tabs.addTab(qTab, 1);

        helperContainer.addMember(tabs);

        fetchWQData();
    }


    @Override
    public Canvas createOld(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data dMode      = getData(items, "wq_isq");
        Data dFree      = getData(items, "wq_isfree");
        Data dSelection = getData(items, "wq_isrange");
        Data dSingle    = getData(items, "wq_single");
        Data dFrom      = getData(items, "wq_from");
        Data dTo        = getData(items, "wq_to");
        Data dStep      = getData(items, "wq_step");

        DataItem[] mode = dMode.getItems();
        String strMode  = mode[0].getStringValue();
        boolean isQMode   = Boolean.valueOf(strMode);

        DataItem[] free = dFree.getItems();
        String  strFree = free[0].getStringValue();
        boolean isFree  = Boolean.valueOf(strFree);

        HLayout layout = new HLayout();
        layout.setWidth("400px");

        Label label  = new Label(dataList.getLabel());
        label.setWidth("200px");

        VLayout vLabel = null;

        DataItem[] selItem = dSelection.getItems();
        boolean    isRange = selItem != null
            ? Boolean.valueOf(selItem[0].getStringValue())
            : false;

        if (!isRange) {
            DataItem[] single = dSingle.getItems();

            vLabel = !isQMode
                ? createWString(single[0], isFree)
                : createQString(single[0]);
        }
        else {
            DataItem[] from = dFrom.getItems();
            DataItem[] to   = dTo.getItems();
            DataItem[] step = dStep.getItems();

            vLabel = !isQMode
                ? createWString(from[0], to[0], step[0])
                : createQString(from[0], to[0], step[0]);
        }

        VLayout selectedLayout = new VLayout();
        String  wqMode         = null;

        if (!isQMode) {
            wqMode = isFree ? MESSAGE.wqWFree() : MESSAGE.wqW();
        }
        else {
            wqMode = isFree ? MESSAGE.wqQ() : MESSAGE.wqQGauge();
        }

        Label mLabel = new Label(wqMode);
        mLabel.setWidth(175);
        mLabel.setHeight(20);

        selectedLayout.addMember(mLabel);
        selectedLayout.addMember(vLabel);
        selectedLayout.setHeight(40);

        Canvas back = getBackButton(dataList.getState());

        layout.addMember(label);
        layout.addMember(selectedLayout);
        layout.addMember(back);

        return layout;
    }


    /**
     * This method reads the default values defined in the DataItems of the Data
     * objects in <i>list</i>.
     *
     * @param list The DataList container that stores the Data objects.
     */
    protected void initDefaults(DataList list) {
        Data f = getData(list.getAll(), "wq_from");
        Data t = getData(list.getAll(), "wq_to");
        Data s = getData(list.getAll(), "wq_step");

        DataItem fQItem  = getDataItem(f.getItems(), "minQ");
        DataItem fWItem  = getDataItem(f.getItems(), "minW");
        DataItem tQItem  = getDataItem(t.getItems(), "maxQ");
        DataItem tWItem  = getDataItem(t.getItems(), "maxW");
        DataItem sQItem  = getDataItem(s.getItems(), "stepQ");
        DataItem sWItem  = getDataItem(s.getItems(), "stepW");
        DataItem fQFItem = getDataItem(f.getItems(), "minQFree");
        DataItem tQFItem = getDataItem(t.getItems(), "maxQFree");
        DataItem sQFItem = getDataItem(s.getItems(), "stepQFree");
        DataItem fWFItem = getDataItem(f.getItems(), "minWFree");
        DataItem tWFItem = getDataItem(t.getItems(), "maxWFree");
        DataItem sWFItem = getDataItem(s.getItems(), "stepWFree");

        minW  = Double.valueOf(fWItem.getStringValue());
        maxW  = Double.valueOf(tWItem.getStringValue());
        stepW = Double.valueOf(sWItem.getStringValue());

        minQ  = Double.valueOf(fQItem.getStringValue());
        maxQ  = Double.valueOf(tQItem.getStringValue());
        stepQ = Double.valueOf(sQItem.getStringValue());

        minQFree  = Double.valueOf(fQFItem.getStringValue());
        maxQFree  = Double.valueOf(tQFItem.getStringValue());
        stepQFree = Double.valueOf(sQFItem.getStringValue());

        minWFree  = Double.valueOf(fWFItem.getStringValue());
        maxWFree  = Double.valueOf(tWFItem.getStringValue());
        stepWFree = Double.valueOf(sWFItem.getStringValue());

        this.fromW = minW;
        this.toW   = maxW;

        this.fromQ = minQ;
        this.toQ   = maxQ;

        this.fromQFree = minQFree;
        this.toQFree   = maxQFree;

        this.fromWFree = minWFree;
        this.toWFree   = maxWFree;
    }


    /**
     * Initializes the form items with former inserted user data.
     *
     * @param list The DataList that contains the user data.
     */
    protected void initUserDefaults(DataList list) {
        List<Data> allData = list.getAll();

        Data     m        = getData(allData, "wq_isq");
        DataItem modeItem = m != null ? m.getDefault() : null;
        boolean isQ  = modeItem != null
            ? Boolean.valueOf(modeItem.getStringValue())
            : false;

        Data     f        = getData(allData, "wq_isfree");
        DataItem freeItem = f != null ? f.getDefault() : null;
        boolean  isFree   = freeItem != null
            ? Boolean.valueOf(freeItem.getStringValue())
            : false;

        Data     s            = getData(allData, "wq_isrange");
        DataItem sI           = s != null ? s.getDefault() : null;
        boolean   isRange      = sI != null
            ? Boolean.valueOf(sI.getStringValue())
            : false;

        initUserSingleValues(list, isQ);
        initUserRangeValues(list, isQ);

        if (isQ) {
            modes.setValue(FIELD_WQ, isQ);
        }
        else {
            modes.setValue(FIELD_WQ, isFree ? FIELD_WQ_Q_FREE : FIELD_WQ_Q);
        }

        if(isRange) {
            modes.setValue(FIELD_MODE, FIELD_MODE_RANGE);
        }
        else {
            modes.setValue(FIELD_MODE, FIELD_MODE_SINGLE);
        }
        updatePanels(isQ, isFree, isRange);
    }


    /**
     * Initializes the single values of W or Q from DataList.
     *
     * @param list The DataList that contains the 'wq_single' object.
     * @param isQ W or Q mode?
     */
    protected void initUserSingleValues(DataList list, boolean isQ) {
        List<Data> allData = list.getAll();


        Data     s = getData(allData, "wq_single");
        DataItem i = s != null ? s.getDefault() : null;
        GWT.log("init values: " + i.getStringValue());

        if (i != null) {
            String   value = i.getStringValue();
            String[] split = value.split(" ");

            int num = split != null ? split.length : 0;

            double[] values = new double[num];

            for (int j = 0; j < num; j++) {
                try {
                    values[j] = Double.valueOf(split[j]);
                }
                catch (NumberFormatException nfe) {
                    // nothing to do
                }
            }

            if (!isQ) {
                setSingleW(values);
            }
            else {
                setSingleQ(values);
            }
        }
    }


    /**
     * Initializes the range values of W or Q from DataList.
     *
     * @param list The DataList that contains the 'wq_single' object.
     * @param isQ W or Q mode?
     */
    protected void initUserRangeValues(DataList list, boolean isQ) {
        List<Data> allData = list.getAll();

        // init range mode values
        Data f = getData(allData, "wq_from");
        Data t = getData(allData, "wq_to");
        Data s = getData(allData, "wq_step");

        if (f != null && t != null && s != null) {
            DataItem dF = f.getDefault();
            DataItem dT = t.getDefault();
            DataItem dS = s.getDefault();

            String fS = dF != null ? dF.getStringValue() : null;
            String tS = dT != null ? dT.getStringValue() : null;
            String sS = dS != null ? dS.getStringValue() : null;

            try {
                double from = Double.valueOf(fS);
                double to   = Double.valueOf(tS);
                double step = Double.valueOf(sS);

                if (!isQ) {
                    setWRangeValues(from, to, step);
                }
                else {
                    setQRangeValues(from, to, step);
                }
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }
    }


    protected void setQRangeValues(double f, double t, double s) {
        setFromQ(f);
        setToQ(t);
        setStepQ(s);
    }


    protected void setWRangeValues(double f, double t, double s) {
        setFromW(f);
        setToW(t);
        setStepW(s);
    }


    protected VLayout createWString(DataItem from, DataItem to, DataItem step) {
        VLayout v = new VLayout();

        StringBuilder sb = new StringBuilder();
        sb.append(from.getLabel());
        sb.append(" " + MESSAGE.unitWFrom() + " ");
        sb.append(to.getLabel());
        sb.append(" " + MESSAGE.unitWTo() + " ");
        sb.append(step.getLabel());
        sb.append(" " + MESSAGE.unitWStep());

        v.addMember(new Label(sb.toString()));

        return v;
    }


    protected VLayout createWString(DataItem single, boolean isFree) {
        String  label = single.getLabel().trim();
        String[] cols = label.split(";");

        VLayout v = new VLayout();

        for (String col: cols) {
            Label l = new Label(col + " "
                + (isFree ? MESSAGE.unitWFree() : MESSAGE.unitWSingle()));
            l.setHeight(20);

            v.addMember(l);
        }

        return v;
    }


    protected VLayout createQString(DataItem from, DataItem to, DataItem step) {
        VLayout v = new VLayout();

        StringBuilder sb = new StringBuilder();
        sb.append(from.getLabel());
        sb.append(" " + MESSAGE.unitQFrom() + " ");
        sb.append(to.getLabel());
        sb.append(" " + MESSAGE.unitQTo() + " ");
        sb.append(step.getLabel());
        sb.append(" " + MESSAGE.unitQStep());

        v.addMember(new Label(sb.toString()));

        return v;
    }


    protected VLayout createQString(DataItem single) {
        String  label = single.getLabel().trim();
        String[] cols = label.split(";");

        VLayout v = new VLayout();

        for (String col: cols) {
            Label l = new Label(col + " " + MESSAGE.unitQSingle());
            l.setHeight(20);

            v.addMember(l);
        }

        return v;
    }


    /**
     * This method creates the whole widget. There is a panel on the left, that
     * allows the user to enter values manually by keyboard. On the right, there
     * is a table that allows the user to enter values by mouse click.
     *
     * @param data The data that is displayed in the table on the right.
     *
     * @return the widget.
     */
    protected Canvas createWidget(DataList data) {
        VLayout layout  = new VLayout();
        container       = new HLayout();
        Canvas modeForm = createModePanel();

        container.setMembersMargin(30);

        // the initial panel is the Single-W panel.
        double[] values = getSingleQ();
        qArrayPanel = new DoubleArrayPanel(
            MESSAGE.unitQSingle(), values, this);
        container.addMember(qArrayPanel);

        layout.addMember(modeForm);
        layout.addMember(container);

        return layout;
    }


    /**
     * This method creates the mode panel. It contains two radio button panels
     * that allows the user to switch the input mode between w/q and
     * single/range input.
     *
     * @return a panel.
     */
    protected Canvas createModePanel() {
        RadioGroupItem wq = new RadioGroupItem(FIELD_WQ);
        wq.setShowTitle(false);
        wq.setVertical(true);
        wq.setWidth(WIDTH_LEFT_UPPER);
        wq.setWrap(false);

        RadioGroupItem mode = new RadioGroupItem(FIELD_MODE);
        mode.setShowTitle(false);
        mode.setVertical(false);
        mode.setWidth(WIDTH_LEFT_LOWER);

        LinkedHashMap wqValues = new LinkedHashMap();
        wqValues.put(FIELD_WQ_W, MESSAGE.wqW());
        wqValues.put(FIELD_WQ_W_FREE, MESSAGE.wqWFree());
        wqValues.put(FIELD_WQ_Q_FREE, MESSAGE.wqQ());
        wqValues.put(FIELD_WQ_Q, MESSAGE.wqQGauge());

        LinkedHashMap modeValues = new LinkedHashMap();
        modeValues.put(FIELD_MODE_SINGLE, MESSAGE.wqSingle());
        modeValues.put(FIELD_MODE_RANGE, MESSAGE.wqRange());

        wq.setValueMap(wqValues);
        mode.setValueMap(modeValues);

        wq.addChangeHandler(this);
        mode.addChangeHandler(this);

        modes = new DynamicForm();
        modes.setFields(wq, mode);
        modes.setWidth(WIDTH_LEFT_UPPER);
        modes.setNumCols(1);

        LinkedHashMap initial = new LinkedHashMap();
        initial.put(FIELD_WQ, FIELD_WQ_Q);
        initial.put(FIELD_MODE, FIELD_MODE_SINGLE);
        modes.setValues(initial);

        return modes;
    }


    @Override
    public List<String> validate() {
        if (isRangeMode()) {
            return validateRangeValues();
        }
        else {
            return validateSingleValues();
        }
    }


    protected List<String> validateRangeValues() {
        if (isWFree()) {
            return validateRange(wFreeRangePanel, minWFree, maxWFree);
        }
        else if (isQFree()) {
            return validateRange(qFreeRangePanel, minQFree, maxQFree);
        }
        else if (isWMode()) {
            return validateRange(wRangePanel, minW, maxW);
        }
        else {
            return validateRange(qRangePanel, minQ, maxQ);
        }
    }

    protected List<String> validateSingleValues() {
        if (isWFree()) {
            return validateSingle(wFreeArrayPanel, minWFree, maxWFree);
        }
        else if (isWMode()) {
            return validateSingle(wArrayPanel, minW, maxW);
            //return validateSingle(wArrayPanel, 0, 100000);
        }
        else if (isQFree()) {
            return validateSingle(qFreeArrayPanel, minQFree, maxQFree);
        }
        else {
            return validateSingle(qArrayPanel, minQ, maxQ);
        }
    }


    protected List<String> validateRange(
        DoubleRangePanel panel,
        double min, double max)
    {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        if (!panel.validateForm()) {
            errors.add(MESSAGE.wrongFormat());
        }

        double from;
        double to;
        double step;

        try {
            from = panel.getFrom();
            to   = panel.getTo();
            step = panel.getStep();
        }
        catch (NullPointerException npe) {
            errors.add(MESSAGE.missingInput());
            return errors;
        }

        if (from < min || from > max) {
            String tmp = MESSAGE.error_validate_lower_range();
            tmp = tmp.replace("$1", nf.format(from));
            tmp = tmp.replace("$2", nf.format(min));
            errors.add(tmp);
            from = min;
        }

        if (to < min || to > max) {
            String tmp = MESSAGE.error_validate_upper_range();
            tmp = tmp.replace("$1", nf.format(to));
            tmp = tmp.replace("$2", nf.format(max));
            errors.add(tmp);
            to = max;
        }

        if (!errors.isEmpty()) {
            panel.setValues(from, to, step);
        }

        return errors;
    }


    protected List<String> validateSingle(
        DoubleArrayPanel panel,
        double min, double max)
    {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        if (!panel.validateForm()) {
            errors.add(MESSAGE.wrongFormat());
        }

        double[] values = panel.getInputValues();

        if (values == null || values.length == 0) {
            errors.add(MESSAGE.atLeastOneValue());
            return errors;
        }

        double[] good   = new double[values.length];
        int      idx    = 0;

        for (double value: values) {
            if (value < min || value > max) {
                String tmp = MESSAGE.error_validate_range();
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
            panel.setValues(justGood);
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
        // XXX If we have entered a value and click right afterwards on the
        // 'next' button, the BlurEvent is not fired, and the values are not
        // saved. So, we gonna save those values explicitly.
        if (!isRangeMode()) {
            Canvas member = container.getMember(0);
            if (member instanceof DoubleArrayPanel) {
                DoubleArrayPanel form = (DoubleArrayPanel) member;
                if (isWFree()) {
                    saveSingleWFreeValues(form);
                }
                else if (isWMode()) {
                    saveSingleWValues(form);
                }
                else if (isQFree()) {
                    saveSingleQFreeValues(form);
                }
                else {
                    saveSingleQValues(form);
                }
            }

            return getSingleData();
        }
        else {
            Canvas member = container.getMember(0);
            if (member instanceof DoubleRangePanel) {
                DoubleRangePanel form = (DoubleRangePanel) member;

                if (isWFree()) {
                    saveRangeWFreeValues(form);
                }
                else if (isWMode()) {
                    saveRangeWValues(form);
                }
                else if (isQFree()) {
                    saveRangeQFreeValues(form);
                }
                else {
                    saveRangeQValues(form);
                }
            }

            return getRangeData();
        }
    }


    /**
     * Collects the required data for single mode and resets the data for range
     * mode.
     */
    protected Data[] getSingleData() {
        DataItem from = new DefaultDataItem("wq_from", "wq_from", "");
        DataItem to   = new DefaultDataItem("wq_to", "wq_to", "");
        DataItem step = new DefaultDataItem("wq_step", "wq_step", "");

        return new Data[] {
                getDataMode(),
                getFree(),
                getDataSelectionMode(),
                getDataSingle(),
                new DefaultData(
                    "wq_from",
                    null,
                    null,
                    new DataItem[] {from}),
                new DefaultData(
                    "wq_to",
                    null,
                    null,
                    new DataItem[] {to}),
                new DefaultData(
                    "wq_step",
                    null,
                    null,
                    new DataItem[] {step}) };
    }


    /**
     * Collects the required data for range mode and resets the data for single
     * mode.
     */
    protected Data[] getRangeData() {
        DataItem item = new DefaultDataItem("wq_single", "wq_single", "");

        return new Data[] {
                getDataMode(),
                getFree(),
                getDataSelectionMode(),
                getDataFrom(),
                getDataTo(),
                getDataStep(),
                new DefaultData(
                    "wq_single",
                    null,
                    null,
                    new DataItem[] {item}) };
    }


    /**
     * Returns the Data object for the 'mode' attribute.
     *
     * @return the Data object for the 'mode' attribute.
     */
    protected Data getDataMode() {
        String wqMode = modes.getValueAsString(FIELD_WQ);

        String value = null;
        if (wqMode.equals(FIELD_WQ_Q_FREE) || wqMode.equals(FIELD_WQ_Q)) {
            GWT.log("getData: FIELD_WQ_Q || FIELD_WQ_Q_FREE");
            value = "true";
        }
        else {
            GWT.log("getData: FIELD_WQ_W || FIELD_WQ_W_FREE");
            value = "false";
        }

        DataItem item = new DefaultDataItem("wq_isq", "wq_isq", value);
        return new DefaultData(
            "wq_isq", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Q mode. The Q mode can be "true" or "false". True means, the
     * calculation is not based on a gauge, false means the calculation should
     * be based on a gauge.
     *
     * @return the Data object for the 'wq_free' attribute.
     */
    protected Data getFree() {
        String value = "";
        if(!isWMode()) {
            value = isQFree() ? "true" : "false";
        }
        else {
            value = isWFree() ? "true" : "false";
        }
        DataItem item = new DefaultDataItem("wq_isfree", "wq_isfree", value);
        return new DefaultData(
            "wq_isfree", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'mode' attribute.
     *
     * @return the Data object for the 'mode' attribute.
     */
    protected Data getDataSelectionMode() {
        String wqSelection = modes.getValueAsString(FIELD_MODE);

        String isRange = "true";
        if (wqSelection.equals(FIELD_MODE_SINGLE)) {
            isRange = "false";
        }
        DataItem item = new DefaultDataItem(
            "wq_isrange", "wq_isrange", isRange);

        return new DefaultData(
            "wq_isrange", null, null, new DataItem[] { item });
    }


    /**
     * Returns the data object for the 'single' attribute.
     *
     * @return the Data object for the 'single' attribute.
     */
    protected Data getDataSingle() {
        double[] values  = getFinalSingle();
        StringBuilder sb = new StringBuilder();
        for (double value: values) {
            sb.append(Double.toString(value));
            sb.append(" ");
        }

        DataItem item = new DefaultDataItem(
            "wq_single", "wq_single", sb.toString());

        return new DefaultData(
            "wq_single", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'from' attribute.
     *
     * @return the Data object for the 'from' attribute.
     */
    protected Data getDataFrom() {
        String value  = Double.valueOf(getFinalFrom()).toString();
        DataItem item = new DefaultDataItem("wq_from", "wq_from", value);
        return new DefaultData(
            "wq_from", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'to' attribute.
     *
     * @return the Data object for the 'to' attribute.
     */
    protected Data getDataTo() {
        String value  = Double.valueOf(getFinalTo()).toString();
        DataItem item = new DefaultDataItem("wq_to", "wq_to", value);
        return new DefaultData(
            "wq_to", null, null, new DataItem[] { item });
    }


    /**
     * Returns the Data object for the 'step' attribute.
     *
     * @return the Data object for the 'step' attribute.
     */
    protected Data getDataStep() {
        String value  = Double.valueOf(getFinalStep()).toString();
        DataItem item = new DefaultDataItem("wq_step","wq_step", value);
        return new DefaultData(
            "wq_step", null, null, new DataItem[] { item });
    }


    protected double[] getFinalSingle() {
        if (isWFree()) {
            return getSingleWFree();
        }
        else if (isWMode()) {
            return getSingleW();
        }
        else if (isQFree()) {
            return getSingleQFree();
        }
        else {
            return getSingleQ();
        }
    }


    /**
     * Returns the value of 'from' depending on the selected input mode.
     *
     * @return the value of 'from' depending on the selected input mode.
     */
    protected double getFinalFrom() {
        if (isRangeMode()) {
            if (isWFree()) {
                return getFromWFree();
            }
            else if (isWMode()) {
                return getFromW();
            }
            else if (isQFree()) {
                return getFromQFree();
            }
            else {
                return getFromQ();
            }
        }
        else {
            double[] values = null;

            if (isWFree()) {
                values = getSingleWFree();
            }
            else if (isWMode()) {
                values = getSingleW();
            }
            else if (isQFree()) {
                values = getSingleQFree();
            }
            else {
                values = getSingleQ();
            }

            double value = Double.MAX_VALUE;
            for (double v: values) {
                value = value < v ? value : v;
            }

            return value;
        }
    }


    /**
     * Returns the value of 'to' depending on the selected input mode.
     *
     * @return the value of 'to' depending on the selected input mode.
     */
    protected double getFinalTo() {
        if (isRangeMode()) {
            if (isWFree()) {
                return getToWFree();
            }
            else if (isWMode()) {
                return getToW();
            }
            else if (isQFree()) {
                return getToQFree();
            }
            else {
                return getToQ();
            }
        }
        else {
            double[] values = null;

            if (isWFree()) {
                values = getSingleWFree();
            }
            else if (isWMode()) {
                values = getSingleW();
            }
            else if (isQFree()) {
                values = getSingleQFree();
            }
            else {
                values = getSingleQ();
            }

            double value = Double.MIN_VALUE;
            for (double v: values) {
                value = value > v ? value : v;
            }

            return value;
        }
    }


    /**
     * Returns the value of 'step' depending on the selected input mode.
     *
     * @return the value of 'step' depending on the selected input mode.
     */
    protected double getFinalStep() {
        if (isRangeMode()) {
            if (isWFree()) {
                return getStepWFree();
            }
            else if (isWMode()) {
                return getStepW();
            }
            else if (isQFree()) {
                return getStepQFree();
            }
            else {
                return getStepQ();
            }
        }
        else {
            // we have no field to enter the 'step' attribute in the
            // single mode
            return 0d;
        }
    }


    /**
     * Determines the range/single mode.
     *
     * @return true if the range mode is activated.
     */
    public boolean isRangeMode() {
        String rMode = modes.getValueAsString(FIELD_MODE);

        return rMode.equals(FIELD_MODE_RANGE);
    }


    /**
     * Determines the w/q mode.
     *
     * @return true, if the W mode is activated.
     */
    public boolean isWMode() {
        String wq = modes.getValueAsString(FIELD_WQ);
        return wq.contains("W");
    }


    public boolean isQFree() {
        String wqMode = modes.getValueAsString(FIELD_WQ);
        return wqMode.equals(FIELD_WQ_Q_FREE);
    }

    protected boolean isWFree() {
        String wqMode = modes.getValueAsString(FIELD_WQ);
        return wqMode.equals(FIELD_WQ_W_FREE);
    }


    /**
     * This method changes the lower panel with the input fields depending on
     * the combination of the two radio button panels.
     *
     * @param event The ChangeEvent.
     */
    @Override
    public void onChange(ChangeEvent event) {
        DynamicForm form = event.getForm();
        FormItem    item = event.getItem();

        boolean isQ     = false;
        boolean isFree  = false;
        boolean isRange = false;

        if (item.getFieldName().equals(FIELD_MODE)) {
            String wq = form.getValueAsString(FIELD_WQ);
            isQ     = wq.contains("Q");
            isFree  = wq.contains("FREE");
            isRange = ((String) event.getValue()).equals(FIELD_MODE_RANGE);
        }
        else {
            String wq = ((String) event.getValue());
            isQ       = wq.contains("Q");
            isFree    = wq.contains("FREE");
            isRange   =
                form.getValueAsString(FIELD_MODE).equals(FIELD_MODE_RANGE);
        }

        if (isQ && isFree) {
            qdTable.hideIconFields();
        }
        else {
            qdTable.showIconFields();
        }

        if (!isRange) {
            qdTable.showSelect();
        }
        else {
            qdTable.showIconFields();
        }

        updatePanels(isQ, isFree, isRange);
    }


    protected void updatePanels(boolean isQ, boolean isFree, boolean isRange) {
        container.removeMembers(container.getMembers());

        if (!isQ && isFree) {
            if (!isRange) {
                // Single W mode
                double[] values = getSingleWFree();

                wFreeArrayPanel = new DoubleArrayPanel(
                    MESSAGE.unitWFree(), values, this);

                container.addMember(wFreeArrayPanel);
            }
            else {
                // Range W mode
                double from = getFromWFree();
                double to   = getToWFree();
                double step = getStepWFree();

                wFreeRangePanel = new DoubleRangePanel(
                    MESSAGE.unitWFrom(), MESSAGE.unitWTo(), MESSAGE.unitWStep(),
                    from, to, step,
                    250,
                    this);
                container.addMember(wFreeRangePanel);
            }

            tabs.selectTab(0);
        }
        else if (!isQ) {
            if (!isRange) {
                // Single W mode
                double[] values = getSingleW();

                wArrayPanel = new DoubleArrayPanel(
                    MESSAGE.unitWSingle(), values, this);

                container.addMember(wArrayPanel);
            }
            else {
                // Range W mode
                double from = getFromW();
                double to   = getToW();
                double step = getStepW();

                wRangePanel = new DoubleRangePanel(
                    MESSAGE.unitWFrom(), MESSAGE.unitWTo(), MESSAGE.unitWStep(),
                    from, to, step,
                    250,
                    this);
                container.addMember(wRangePanel);
            }

            tabs.selectTab(0);
        }
        else if (isQ && isFree) {
            if (!isRange) {
                // Single Q mode
                double[] values = getSingleQFree();

                qFreeArrayPanel = new DoubleArrayPanel(
                    MESSAGE.unitQSingle(), values, this);
                container.addMember(qFreeArrayPanel);
            }
            else {
                // Range Q mode
                double from = getFromQFree();
                double to   = getToQFree();
                double step = getStepQFree();

                qFreeRangePanel = new DoubleRangePanel(
                    MESSAGE.unitQFrom(), MESSAGE.unitQTo(), MESSAGE.unitQStep(),
                    from, to, step,
                    250,
                    this);
                container.addMember(qFreeRangePanel);
            }

            tabs.selectTab(1);
        }
        else {
            if (!isRange) {
                // Single Q mode
                double[] values = getSingleQ();

                qArrayPanel = new DoubleArrayPanel(
                    MESSAGE.unitQSingle(), values, this);
                container.addMember(qArrayPanel);
            }
            else {
                // Range Q mode
                double from = getFromQ();
                double to   = getToQ();
                double step = getStepQ();

                qRangePanel = new DoubleRangePanel(
                    MESSAGE.unitQFrom(), MESSAGE.unitQTo(), MESSAGE.unitQStep(),
                    from, to, step,
                    250,
                    this);
                container.addMember(qRangePanel);
            }

            tabs.selectTab(1);
        }
    }

    /**
     * This method is called if the value of one of the input fields might have
     * changed. The entered values are validated and stored.
     *
     * @param event The BlurEvent.
     */
    @Override
    public void onBlur(BlurEvent event) {
        DynamicForm form = event.getForm();
        FormItem    item = event.getItem();

        String wqMode    = (String) modes.getValue(FIELD_WQ);
        String inputMode = (String) modes.getValue(FIELD_MODE);

        if (wqMode.equals(FIELD_WQ_W)) {
            if (inputMode.equals(FIELD_MODE_SINGLE)) {
                DoubleArrayPanel p = (DoubleArrayPanel) form;
                saveSingleWValue(p, item);
            }
            else {
                DoubleRangePanel p = (DoubleRangePanel) form;
                saveRangeWValue(p, item);
            }
        }
        else if (wqMode.equals(FIELD_WQ_W_FREE)) {
            if (inputMode.equals(FIELD_MODE_SINGLE)) {
                DoubleArrayPanel p = (DoubleArrayPanel) form;
                saveSingleWFreeValue(p, item);
            }
            else {
                DoubleRangePanel p = (DoubleRangePanel) form;
                saveRangeWFreeValue(p, item);
            }
        }
        else if (wqMode.equals(FIELD_WQ_Q_FREE)) {
            if (inputMode.equals(FIELD_MODE_SINGLE)) {
                DoubleArrayPanel p = (DoubleArrayPanel) form;
                saveSingleQFreeValue(p, item);
            }
            else {
                DoubleRangePanel p = (DoubleRangePanel) form;
                saveRangeQFreeValue(p, item);
            }
        }
        else {
            if (inputMode.equals(FIELD_MODE_SINGLE)) {
                DoubleArrayPanel p = (DoubleArrayPanel) form;
                saveSingleQValue(p, item);
            }
            else {
                DoubleRangePanel p = (DoubleRangePanel) form;
                saveRangeQValue(p, item);
            }
        }
    }


    protected void saveSingleWValues(DoubleArrayPanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            if (item.getFieldName().equals(DoubleArrayPanel.FIELD_NAME)) {
                saveSingleWValue(p, item);
            }
        }
    }


    protected void saveSingleWFreeValues(DoubleArrayPanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            if (item.getFieldName().equals(DoubleArrayPanel.FIELD_NAME)) {
                saveSingleWFreeValue(p, item);
            }
        }
    }


    protected void saveSingleQValues(DoubleArrayPanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            if (item.getFieldName().equals(DoubleArrayPanel.FIELD_NAME)) {
                saveSingleQValue(p, item);
            }
        }
    }


    protected void saveSingleQFreeValues(DoubleArrayPanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            if (item.getFieldName().equals(DoubleArrayPanel.FIELD_NAME)) {
                saveSingleQFreeValue(p, item);
            }
        }
    }


    protected void saveSingleWValue(DoubleArrayPanel p, FormItem item) {
        if (p.validateForm(item)) {
            setSingleW(p.getInputValues(item));
        }
    }


    protected void saveSingleWFreeValue(DoubleArrayPanel p, FormItem item) {
        if (p.validateForm(item)) {
            setSingleWFree(p.getInputValues(item));
        }
    }


    protected void saveSingleQValue(DoubleArrayPanel p, FormItem item) {
        if (p.validateForm(item)) {
            setSingleQ(p.getInputValues(item));
        }
    }


    protected void saveSingleQFreeValue(DoubleArrayPanel p, FormItem item) {
        if (p.validateForm(item)) {
            setSingleQFree(p.getInputValues(item));
        }
    }


    protected void saveRangeWValues(DoubleRangePanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            saveRangeWValue(p, item);
        }
    }


    protected void saveRangeWFreeValues(DoubleRangePanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            saveRangeWFreeValue(p, item);
        }
    }


    protected void saveRangeQValues(DoubleRangePanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            saveRangeQValue(p, item);
        }
    }


    protected void saveRangeQFreeValues(DoubleRangePanel p) {
        FormItem[] formItems = p.getFields();

        for (FormItem item: formItems) {
            saveRangeQFreeValue(p, item);
        }
    }


    protected void saveRangeWValue(DoubleRangePanel p, FormItem item) {
        if (p.validateForm()) {
            setFromW(p.getFrom());
            setToW(p.getTo());
            setStepW(p.getStep());
        }
    }


    protected void saveRangeWFreeValue(DoubleRangePanel p, FormItem item) {
        if (p.validateForm()) {
            setFromWFree(p.getFrom());
            setToWFree(p.getTo());
            setStepWFree(p.getStep());
        }
    }


    protected void saveRangeQValue(DoubleRangePanel p, FormItem item) {
        if (p.validateForm()) {
            setFromQ(p.getFrom());
            setToQ(p.getTo());
            setStepQ(p.getStep());
        }
    }


    protected void saveRangeQFreeValue(DoubleRangePanel p, FormItem item) {
        if (p.validateForm()) {
            setFromQFree(p.getFrom());
            setToQFree(p.getTo());
            setStepQFree(p.getStep());
        }
    }


    protected double[] getSingleQ() {
        return valuesQ;
    }


    protected double[] getSingleQFree() {
        return valuesQFree;
    }


    protected void setSingleQ(double[] values) {
        valuesQ = values;
    }


    protected void setSingleQFree(double[] values) {
        valuesQFree = values;
    }


    protected double getFromQ() {
        return fromQ;
    }


    protected double getFromQFree() {
        return fromQFree;
    }


    protected void setFromQ(double fromQ) {
        this.fromQ = fromQ;
    }


    protected void setFromQFree(double fromQ) {
        this.fromQFree = fromQ;
    }


    protected double getToQ() {
        return toQ;
    }


    protected double getToQFree() {
        return toQFree;
    }


    protected void setToQ(double toQ) {
        this.toQ = toQ;
    }


    protected void setToQFree(double toQ) {
        this.toQFree = toQ;
    }


    protected double getStepQ() {
        return stepQ;
    }


    protected double getStepQFree() {
        return stepQFree;
    }


    protected void setStepQ(double stepQ) {
        this.stepQ = stepQ;
    }


    protected void setStepQFree(double stepQ) {
        this.stepQFree = stepQ;
    }

    protected double[] getSingleW() {
        return valuesW;
    }

    protected double[] getSingleWFree() {
        return valuesWFree;
    }

    protected void setSingleW(double[] values) {
        valuesW = values;
    }

    protected void setSingleWFree(double[] values) {
        valuesWFree = values;
    }

    protected double getFromW() {
        return fromW;
    }

    protected double getFromWFree() {
        return fromWFree;
    }

    protected void setFromW(double fromW) {
        this.fromW = fromW;
    }

    protected void setFromWFree(double fromW) {
        this.fromW = fromW;
    }

    protected double getToW() {
        return toW;
    }

    protected double getToWFree() {
        return toWFree;
    }

    protected void setToW(double toW) {
        this.toW = toW;
    }

    protected void setToWFree(double toW) {
        this.toWFree = toW;
    }

    protected double getStepW() {
        return stepW;
    }

    protected double getStepWFree() {
        return stepWFree;
    }

    protected void setStepW(double stepW) {
        this.stepW = stepW;
    }

    protected void setStepWFree(double stepW) {
        this.stepWFree = stepW;
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
    protected String getRiverName() {
        ArtifactDescription adesc = artifact.getArtifactDescription();
        return adesc.getRiver();
    }


    protected void fetchWQData() {
        Config config = Config.getInstance();
        String locale = config.getLocale ();

        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList[] data = adescr.getOldData();

        double[]  mm = getMinMaxKM(data);
        String river = getRiverName();

        wqInfoService.getWQInfo(locale, river, mm[0], mm[1],
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

                    String wq = (String) modes.getValue(FIELD_WQ);
                    String sr = (String) modes.getValue(FIELD_MODE);
                    GWT.log("sending: " + wq + ", " + sr);
                    boolean isQ = wq.contains("Q");
                    boolean isFree = wq.contains("FREE");
                    boolean isRange = sr.equals(FIELD_MODE_RANGE);

                    updatePanels(isQ, isFree, isRange);
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
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
