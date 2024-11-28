/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.text.NumberFormat;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Wst;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.access.RangeAccess;

import org.dive4elements.river.artifacts.model.WstFactory;
import org.dive4elements.river.artifacts.model.WstValueTable;
import org.dive4elements.river.artifacts.model.WstValueTableFactory;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQSelect extends DefaultState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(WQSelect.class);

    /** The default step width for Qs. */
    public static final String DEFAULT_STEP_Q = "50";

    /** The default step width for Qs. */
    public static final String DEFAULT_STEP_W = "30";

    /** The max number of steps for Qs and Ws. */
    public static final int MAX_STEPS = 30;

    /** The name of the 'mode' field. */
    public static final String WQ_MODE = "wq_isq";

    /** Them name fo the 'free' field. */
    public static final String WQ_FREE = "wq_isfree";

    /** The name of the 'selection' field. */
    public static final String WQ_SELECTION = "wq_isrange";

    /** The name of the 'from' field. */
    public static final String WQ_FROM = "wq_from";

    /** The name of the 'to' field. */
    public static final String WQ_TO = "wq_to";

    /** The name of the 'step' field. */
    public static final String WQ_STEP = "wq_step";

    /** The name of the 'single' field. */
    public static final String WQ_SINGLE = "wq_single";


    /**
     * The default constructor that initializes an empty State object.
     */
    public WQSelect() {
    }


    @Override
    protected Element createStaticData(
        D4EArtifact   flys,
        ElementCreator creator,
        CallContext    cc,
        String         name,
        String         value,
        String         type
    ) {
        if (!name.equals(WQ_SINGLE)) {
            return super.createStaticData(flys, creator, cc, name, value, type);
        }

        Boolean isQ = flys.getDataAsBoolean(WQ_MODE);
        Boolean isFree = flys.getDataAsBoolean(WQ_FREE);

        WINFOArtifact winfo = (WINFOArtifact) flys;

        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        Element itemElement = creator.create("item");
        creator.addAttr(itemElement, "value", value, true);

        String label;

        if (!isQ || isFree) {
            label = getLabel(winfo, cc, value);
        }
        else {
            label = getSpecialLabel(winfo, cc, value);
        }

        creator.addAttr(itemElement, "label", label, true);

        dataElement.appendChild(itemElement);

        return dataElement;
    }


    protected static String getLabel(
        WINFOArtifact winfo,
        CallContext   cc,
        String        raw
    ) {
        String[] values = raw.split(" ");

        if (values.length < 1) {
            return null;
        }

        StringBuilder label = new StringBuilder();

        NumberFormat nf = NumberFormat.getInstance(
            Resources.getLocale(cc.getMeta()));

        for (String value: values) {
            try {
                double v = Double.parseDouble(value.trim());

                String formatted = nf.format(v);

                if (label.length() > 0) {
                    label.append(';');
                }
                label.append(formatted);
            }
            catch (NumberFormatException nfe) {
                // do nothing here
            }
        }

        return label.toString();
    }


    protected static String getSpecialLabel(
        WINFOArtifact winfo,
        CallContext   cc,
        String        raw
    ) {
        String[] values = raw.split(" ");

        if (values.length < 1) {
            return null;
        }

        NumberFormat nf = NumberFormat.getInstance(
            Resources.getLocale(cc.getMeta()));

        RangeAccess rangeAccess = new RangeAccess(winfo);
        Gauge gauge = rangeAccess.getRiver().determineRefGauge(
            rangeAccess.getKmRange(), rangeAccess.isRange());

        StringBuilder label = new StringBuilder();

        for (String value: values) {
            try {
                double v = Double.parseDouble(value.trim());

                String tmp = nf.format(v);
                String mv  = RiverUtils.getNamedMainValue(gauge, v);

                if (mv != null && mv.length() > 0) {
                    tmp = mv + ": " + tmp;
                    log.debug("Add main value: '" + mv + "'");
                }
                if (label.length() > 0) {
                    label.append(';');
                }
                label.append(tmp);
            }
            catch (NumberFormatException nfe) {
                // do nothing here
            }
        }

        return label.toString();
    }


    @Override
    protected Element createData(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        StateData   data,
        CallContext context)
    {
        Element select = ProtocolUtils.createArtNode(
            cr, "select", null, null);

        cr.addAttr(select, "name", data.getName(), true);

        Element label = ProtocolUtils.createArtNode(
            cr, "label", null, null);

        // XXX: DEAD CODE
        /*
        Element choices = ProtocolUtils.createArtNode(
            cr, "choices", null, null);
        */

        label.setTextContent(Resources.getMsg(
            context.getMeta(),
            data.getName(),
            data.getName()));

        select.appendChild(label);

        return select;
    }


    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context
    ){
        WINFOArtifact winfo = (WINFOArtifact) artifact;

        double[] minmaxW     = determineMinMaxW(winfo);
        double[] minmaxWFree = determineMinMaxWFree(winfo);
        double[] minmaxQ     = determineMinMaxQAtGauge(winfo);
        double[] minmaxQFree = determineMinMaxQ(winfo);

        if (name.equals("wq_from")) {
            Element minW = createItem(cr, new String[] {
                "minW",
                String.valueOf(minmaxW[0])});

            Element minQ = createItem(cr, new String[] {
                "minQ",
                String.valueOf(minmaxQ[0])});

            Element minQFree = createItem(cr, new String[] {
                "minQFree",
                String.valueOf(minmaxQFree[0])});

            Element minWFree = createItem(cr, new String[] {
                "minWFree",
                String.valueOf(minmaxWFree[0])});

            return new Element[] { minW, minQ, minQFree, minWFree };
        }
        else if (name.equals("wq_to")) {
            Element maxW = createItem(cr, new String[] {
                "maxW",
                String.valueOf(minmaxW[1])});

            Element maxQ = createItem(cr, new String[] {
                "maxQ",
                String.valueOf(minmaxQ[1])});

            Element maxQFree = createItem(cr, new String[] {
                "maxQFree",
                String.valueOf(minmaxQFree[1])});

            Element maxWFree = createItem(cr, new String[] {
                "maxWFree",
                String.valueOf(minmaxWFree[1])});

            return new Element[] { maxW, maxQ, maxQFree, maxWFree };
        }
        else {
            Element stepW = createItem(
                cr, new String[] {
                    "stepW",
                    String.valueOf(getStepsW(minmaxW[0], minmaxW[1]))});
            Element stepQ = createItem(
                cr, new String[] {
                    "stepQ",
                    String.valueOf(getStepsQ(minmaxQ[0], minmaxQ[1]))});
            Element stepQFree = createItem(
                cr, new String[] {
                    "stepQFree",
                    String.valueOf(getStepsQ(minmaxQFree[0], minmaxQFree[1]))});
            Element stepWFree = createItem(
                cr, new String[] {
                    "stepWFree",
                    String.valueOf(getStepsW(minmaxWFree[0], minmaxWFree[1]))});

            return new Element[] { stepW, stepQ, stepQFree, stepWFree };
        }
    }


    protected static double getStepsW(double min, double max) {
        double diff = min < max ? max - min : min - max;
        double step = diff / MAX_STEPS;

        if (step < 10) {
            return getSteps(step, 1);
        }
        else if (step < 100) {
            return getSteps(step, 10);
        }
        else if (step < 1000) {
            return getSteps(step, 100);
        }
        else {
            return step;
        }
    }


    protected static double getStepsQ(double min, double max) {
        double diff = min < max ? max - min : min - max;
        double step = diff / MAX_STEPS;

        if (step < 10) {
            return getSteps(step, 1);
        }
        else if (step < 100) {
            return getSteps(step, 10);
        }
        else if (step < 1000) {
            return getSteps(step, 100);
        }
        else {
            return step;
        }
    }


    protected static double getSteps(double steps, double factor) {
        int    fac  = (int) (steps / factor);
        double diff = steps - fac * factor;

        if (diff == 0) {
            return steps;
        }

        return factor * (fac + 1);
    }


    protected Element createItem(XMLUtils.ElementCreator cr, Object obj) {
        Element item  = ProtocolUtils.createArtNode(cr, "item", null, null);
        Element label = ProtocolUtils.createArtNode(cr, "label", null, null);
        Element value = ProtocolUtils.createArtNode(cr, "value", null, null);

        String[] arr = (String[]) obj;

        label.setTextContent(arr[0]);
        value.setTextContent(arr[1]);

        item.appendChild(label);
        item.appendChild(value);

        return item;
    }


    @Override
    protected String getUIProvider() {
        return "wq_panel";
    }


    /**
     * Determines the min and max W value for the current gauge. If no min and
     * max values could be determined, this method will return
     * [Double.MIN_VALUE, Double.MAX_VALUE].
     *
     * @param artifact The D4EArtifact.
     *
     * @return the min and max W values for the current gauge.
     */
    protected double[] determineMinMaxW(WINFOArtifact winfo) {
        log.debug("WQSelect.determineCurrentGauge");

        RangeAccess rangeAccess = new RangeAccess(winfo);
        Gauge gauge = rangeAccess.getRiver().determineRefGauge(
            rangeAccess.getKmRange(), rangeAccess.isRange());

        double[] minmaxW = gauge != null ? gauge.determineMinMaxW() : null;

        double minW = minmaxW != null ? minmaxW[0] : Double.MIN_VALUE;
        double maxW = minmaxW != null ? minmaxW[1] : Double.MAX_VALUE;

        return new double[] { minW, maxW };
    }


    /**
     * Determines the min and max W value. If no min and
     * max values could be determined, this method will return
     * [Double.MIN_VALUE, Double.MAX_VALUE].
     *
     * @param artifact The D4EArtifact.
     *
     * @return the min and max W values.
     */
    protected double[] determineMinMaxWFree(WINFOArtifact winfo) {
        log.debug("WQSelect.determineMinMaxWFree");

        WstValueTable valueTable = WstValueTableFactory.getTable(
                RiverUtils.getRiver(winfo));

        double[] minmaxW = null;
        if(valueTable != null) {
            double[] km = null;
            if(new RangeAccess(winfo).isRange()) {
                km = winfo.getFromToStep();
                // Use the start km to determine the min max values.
                minmaxW = valueTable.getMinMaxW(km[0]);
            }
            else {
                km = winfo.getKms();
                minmaxW = valueTable.getMinMaxW(km[0]);
            }
        }
        return minmaxW != null
            ? minmaxW
            : new double[] { Double.MIN_VALUE, Double.MAX_VALUE };
    }


    /**
     * Determines the min and max Q value for the current gauge. If no min and
     * max values could be determined, this method will return
     * [Double.MIN_VALUE, Double.MAX_VALUE].
     *
     * @param artifact The D4EArtifact.
     *
     * @return the min and max Q values for the current gauge.
     */
    protected double[] determineMinMaxQAtGauge(WINFOArtifact winfo) {
        log.debug("WQSelect.determineMinMaxQAtGauge");

        RangeAccess rangeAccess = new RangeAccess(winfo);
        River river = rangeAccess.getRiver();
        Gauge gauge = river.determineRefGauge(
            rangeAccess.getKmRange(), rangeAccess.isRange());

        Wst   wst   = WstFactory.getWst(river);

        double[] minmaxQ = gauge != null
            ? wst.determineMinMaxQ(gauge.getRange())
            : null;

        double minQ = minmaxQ != null ? minmaxQ[0] : Double.MIN_VALUE;
        double maxQ = minmaxQ != null ? minmaxQ[1] : Double.MAX_VALUE;

        return new double[] { minQ, maxQ };
    }


    /**
     * Determines the min and max Q value for the current kilometer range. If no
     * min and max values could be determined, this method will return
     *
     * @param artifact The D4EArtifact.
     *
     * @return the min and max Q values for the current kilometer range.
     */
    protected double[] determineMinMaxQ(WINFOArtifact winfo) {
        log.debug("WQSelect.determineMinMaxQ");

        WstValueTable valueTable = WstValueTableFactory.getTable(
                RiverUtils.getRiver(winfo));

        double[] minmaxQ = null;
        if(valueTable != null) {
            double[] km = null;
            if(new RangeAccess(winfo).isRange()) {
                km = winfo.getFromToStep();
                minmaxQ = valueTable.getMinMaxQ(km[0], km[1], km[2]);
            }
            else {
                km = winfo.getKms();
                minmaxQ = valueTable.getMinMaxQ(km[0]);
                for (int i = 1; i < km.length; i++) {
                    double[] tmp = valueTable.getMinMaxQ(km[i]);
                    if(tmp[0] < minmaxQ[0]) {
                        minmaxQ[0] = tmp[0];
                    }
                    if(tmp[1] > minmaxQ[1]) {
                        minmaxQ[1] = tmp[1];
                    }
                }
            }
        }
        return minmaxQ != null
            ? minmaxQ
            : new double[] { Double.MIN_VALUE, Double.MAX_VALUE };
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("WQSelect.validate");

        WINFOArtifact flys = (WINFOArtifact) artifact;

        StateData data       = getData(flys, WQ_SELECTION);
        boolean isRange = data != null
            ? Boolean.valueOf((String) data.getValue())
            : false;



        if (!isRange) {
            return validateSingle(flys);
        }
        else {
            return validateRange(flys);
        }
    }


    protected boolean validateBounds(
        double fromValid, double toValid,
        double from,      double to,      double step)
    throws IllegalArgumentException
    {
        log.debug("RangeState.validateRange");

        if (from < fromValid) {
            log.error(
                "Invalid 'from'. " + from + " is smaller than " + fromValid);
            throw new IllegalArgumentException("error_feed_from_out_of_range");
        }
        else if (to > toValid) {
            log.error(
                "Invalid 'to'. " + to + " is bigger than " + toValid);
            throw new IllegalArgumentException("error_feed_to_out_of_range");
        }

        return true;
    }


    protected boolean validateSingle(WINFOArtifact artifact)
    throws    IllegalArgumentException
    {
        log.debug("WQSelect.validateSingle");

        StateData    data = getData(artifact, WQ_SINGLE);

        String tmp = data != null ? (String) data.getValue() : null;

        if (tmp == null || tmp.length() == 0) {
            throw new IllegalArgumentException("error_empty_state");
        }

        String[] strValues = tmp.split(" ");
        TDoubleArrayList all = new TDoubleArrayList();

        for (String strValue: strValues) {
            try {
                all.add(Double.parseDouble(strValue));
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe, nfe);
            }
        }

        all.sort();

        RiverUtils.WQ_MODE mode = RiverUtils.getWQMode(artifact);

        log.debug("WQ Mode: " + mode);

        double[] minmax = null;

        if (mode == RiverUtils.WQ_MODE.WGAUGE) {
            minmax = determineMinMaxW(artifact);
        }
        else if (mode == RiverUtils.WQ_MODE.QGAUGE) {
            minmax = determineMinMaxQAtGauge(artifact);
        }
        else if (mode == RiverUtils.WQ_MODE.QFREE) {
            minmax = determineMinMaxQ(artifact);
        }
        else {
            minmax = determineMinMaxWFree(artifact);
        }

        double min = all.get(0);
        double max = all.get(all.size()-1);

        log.debug("Inserted min value = " + min);
        log.debug("Inserted max value = " + max);

        return validateBounds(minmax[0], minmax[1], min, max, 0d);
    }


    protected boolean validateRange(WINFOArtifact artifact)
    throws    IllegalArgumentException
    {
        log.debug("WQSelect.validateRange");

        RiverUtils.WQ_MODE mode = RiverUtils.getWQMode(artifact);

        if (mode == null) {
            throw new IllegalArgumentException("error_feed_invalid_wq_mode");
        }

        StateData dFrom = artifact.getData(WQ_FROM);
        StateData dTo   = artifact.getData(WQ_TO);
        StateData dStep = artifact.getData(WQ_STEP);

        String fromStr = dFrom != null ? (String) dFrom.getValue() : null;
        String toStr   = dTo != null ? (String) dTo.getValue() : null;
        String stepStr = dStep != null ? (String) dStep.getValue() : null;

        if (fromStr == null || toStr == null || stepStr == null) {
            throw new IllegalArgumentException("error_empty_state");
        }

        try {
            double from = Double.parseDouble(fromStr);
            double to   = Double.parseDouble(toStr);
            double step = Double.parseDouble(stepStr);

            if (mode == RiverUtils.WQ_MODE.WGAUGE) {
                return validateGaugeW(artifact, from, to, step);
            }
            else if (mode == RiverUtils.WQ_MODE.QGAUGE) {
                return validateGaugeQ(artifact, from, to, step);
            }
            else if (mode == RiverUtils.WQ_MODE.QFREE) {
                return validateFreeQ(artifact, from, to, step);
            }
            else if (mode == RiverUtils.WQ_MODE.WFREE) {
                return validateFreeW(artifact, from, to, step);
            }
            else {
                throw new IllegalArgumentException(
                    "error_feed_invalid_wq_mode");
            }
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("error_feed_number_format");
        }
    }


    /**
     * Validates the inserted W values.
     *
     * @param artifact The owner artifact.
     * @param from The lower value of the W range.
     * @param to The upper value of the W range.
     * @param step The step width.
     *
     * @return true, if everything was fine, otherwise an exception is thrown.
     */
    protected boolean validateGaugeW(
        WINFOArtifact    artifact,
        double from,
        double to,
        double step)
    throws    IllegalArgumentException
    {
        log.debug("WQSelect.validateGaugeW");

        double[] minmaxW = determineMinMaxW(artifact);

        return validateBounds(minmaxW[0], minmaxW[1], from, to, step);
    }


    /**
     * Validates the inserted Q values based on the Q range for the current
     * gauge.
     *
     * @param artifact The owner artifact.
     * @param from The lower value of the Q range.
     * @param to The upper value of the Q range.
     * @param step The step width.
     *
     * @return true, if everything was fine, otherwise an exception is thrown.
     */
    protected boolean validateGaugeQ(
        WINFOArtifact artifact,
        double   from,
        double   to,
        double   step)
    throws IllegalArgumentException
    {
        log.debug("WQSelect.validateGaugeQ");

        double[] minmaxQ = determineMinMaxQAtGauge(artifact);

        return validateBounds(minmaxQ[0], minmaxQ[1], from, to, step);
    }


    /**
     * Validates the inserted Q values based on the Q range for the current
     * kilometer range.
     *
     * @param artifact The owner artifact.
     * @param from The lower value of the Q range.
     * @param to The upper value of the Q range.
     * @param step The step width.
     *
     * @return true, if everything was fine, otherwise an exception is thrown.
     */
    protected boolean validateFreeQ(
        WINFOArtifact artifact,
        double   from,
        double   to,
        double   step)
    throws IllegalArgumentException
    {
        log.debug("WQSelect.validateFreeQ");

        double[] minmaxQ = determineMinMaxQ(artifact);

        return validateBounds(minmaxQ[0], minmaxQ[1], from, to, step);
    }


    /**
     * Validates the inserted W values based on the W range for the current
     * kilometer range.
     *
     * @param artifact The owner artifact.
     * @param from The lower value of the W range.
     * @param to The upper value of the W range.
     * @param step The step width.
     *
     * @return true, if everything was fine, otherwise an exception is thrown.
     */
    protected boolean validateFreeW(
        WINFOArtifact artifact,
        double   from,
        double   to,
        double   step)
    throws IllegalArgumentException
    {
        log.debug("WQSelect.validateFreeW");

        double[] minmaxW = determineMinMaxWFree(artifact);

        return validateBounds(minmaxW[0], minmaxW[1], from, to, step);
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
