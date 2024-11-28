/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.access.RangeAccess;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Wst;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.RangeWithValues;
import org.dive4elements.river.artifacts.model.WstFactory;
import org.dive4elements.river.utils.RiverUtils;


/**
 * State to input W/Q data.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQAdapted extends DefaultState {

    /** The log used in this state.*/
    private static Logger log = LogManager.getLogger(WQAdapted.class);

    public static final String FIELD_WQ_MODE = "wq_isq";

    public static final String FIELD_WQ_VALUES = "wq_values";

    public static final class GaugeOrder implements Comparator<Gauge> {
        private int order;

        public GaugeOrder(boolean up) {
            order = up ? 1 : -1;
        }

        public int compare(Gauge a, Gauge b) {
            return order * a.getRange().getA().compareTo(b.getRange().getA());
        }
    } // class GaugeOrder

    public static final GaugeOrder GAUGE_UP   = new GaugeOrder(true);
    public static final GaugeOrder GAUGE_DOWN = new GaugeOrder(false);


    /** Trivial, empty constructor. */
    public WQAdapted() {
    }


    /**
     * This method creates one element for each gauge of selected river that
     * is intersected by the given kilometer range. Each element is a tuple of
     * (from;to) where <i>from</i> is the lower bounds of the gauge or
     * the lower
     * kilometer range. <i>to</i> is the upper bounds of the gauge or the upper
     * kilometer range.
     *
     * @param cr The ElementCreator.
     * @param artifact The FLYS artifact.
     * @param name The name of the data item.
     * @param context The CallContext.
     *
     * @return a list of elements that consist of tuples of the intersected
     * gauges of the selected river.
     */
    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        log.debug("WQAdapted.createItems");

        if (name != null && name.equals(FIELD_WQ_MODE)) {
            return createModeItems(cr, artifact, name, context);
        }
        else if (name != null && name.equals(FIELD_WQ_VALUES)) {
            return createValueItems(cr, artifact, name, context);
        }
        else {
            log.warn("Unknown data object: " + name);
            return null;
        }
    }


    /** Creates "Q" and "W" items. */
    protected Element[] createModeItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        log.debug("WQAdapted.createModeItems");

        Element w = createItem(cr, new String[] { "w", "W" });
        Element q = createItem(cr, new String[] { "q", "Q" });

        return new Element[] { w, q };
    }


    /** Create the items for input to the ranges per mode. */
    protected Element[] createValueItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        log.debug("WQAdapted.createValueItems");

        D4EArtifact flysArtifact = (D4EArtifact) artifact;

        RangeAccess rangeAccess = new RangeAccess(flysArtifact);
        double[]    dist   = rangeAccess.getKmRange();
        // TODO use Access to get River and gauges.
        River       river  = RiverUtils.getRiver(flysArtifact);
        Wst         wst    = WstFactory.getWst(river);
        List<Gauge> gauges = RiverUtils.getGauges(flysArtifact);

        int num = gauges != null ? gauges.size() : 0;

        if (num == 0) {
            log.warn("Selected distance matches no gauges.");
            return null;
        }

        List<Element> elements = new ArrayList<Element>();

        double rangeFrom = dist[0];
        double rangeTo   = dist[1];

        if (rangeFrom < rangeTo) {
            Collections.sort(gauges, GAUGE_UP);
            for (Gauge gauge: gauges) {
                Range range = gauge.getRange();
                double lower = range.getA().doubleValue();
                double upper = range.getB().doubleValue();

                // If gauge out of range, skip it.
                if (upper <= rangeFrom || lower >= rangeTo) {
                    continue;
                }

                double from = lower < rangeFrom ? rangeFrom : lower;
                double to   = upper > rangeTo   ? rangeTo   : upper;

                double[] mmQ = determineMinMaxQ(gauge, wst);
                double[] mmW = gauge.determineMinMaxW();

                elements.add(createItem(
                        cr,
                        new String[] { from + ";" + to,
                                       gauge.getName()},
                        mmQ,
                        mmW));
            }
        }
        else {
            Collections.sort(gauges, GAUGE_DOWN);
            rangeFrom = dist[1];
            rangeTo   = dist[0];
            for (Gauge gauge: gauges) {
                Range range = gauge.getRange();
                double lower = range.getA().doubleValue();
                double upper = range.getB().doubleValue();

                double from = lower < rangeFrom ? rangeFrom : lower;
                double to   = upper > rangeTo   ? rangeTo   : upper;

                // TODO probably need to continue out if oof range (see above).

                double[] mmQ = determineMinMaxQ(gauge, wst);
                double[] mmW = gauge.determineMinMaxW();

                elements.add(createItem(
                        cr,
                        new String[] { to + ";" + from,
                                       gauge.getName()},
                        mmQ,
                        mmW));
            }
        }

        Element[] els = new Element[elements.size()];
        return elements.toArray(els);
    }


    protected Element createItem(XMLUtils.ElementCreator cr, Object obj) {
        return createItem(cr, obj, null, null);
    }


    /** In obj: 0 is label, 1 is value. */
    protected Element createItem(
        XMLUtils.ElementCreator cr,
        Object   obj,
        double[] q,
        double[] w)
    {
        Element item  = ProtocolUtils.createArtNode(cr, "item", null, null);
        Element label = ProtocolUtils.createArtNode(cr, "label", null, null);
        Element value = ProtocolUtils.createArtNode(cr, "value", null, null);

        String[] arr = (String[]) obj;

        label.setTextContent(arr[0]);
        value.setTextContent(arr[1]);

        item.appendChild(label);
        item.appendChild(value);

        if (q != null) {
            Element qRange = createRangeElement(cr, q, "Q");
            item.appendChild(qRange);
        }

        if (w != null) {
            Element wRange = createRangeElement(cr, w, "W");
            item.appendChild(wRange);
        }

        return item;
    }


    protected Element createRangeElement(
        XMLUtils.ElementCreator cr,
        double[] mm,
        String   type)
    {
        Element range = ProtocolUtils.createArtNode(
            cr, "range",
            new String[] {"type"},
            new String[] {type});

        Element min = ProtocolUtils.createArtNode(cr, "min", null, null);
        min.setTextContent(String.valueOf(mm[0]));

        Element max = ProtocolUtils.createArtNode(cr, "max", null, null);
        max.setTextContent(String.valueOf(mm[1]));

        range.appendChild(min);
        range.appendChild(max);

        return range;
    }


    /**
     * Determines the min and max Q value for the given gauge. If no min and
     * max values could be determined, this method will return
     * [Double.MIN_VALUE, Double.MAX_VALUE].
     *
     * @param gauge
     * @param wst
     *
     * @return the min and max Q values for the given gauge.
     */
    protected double[] determineMinMaxQ(Gauge gauge, Wst wst) {
        log.debug("WQAdapted.determineMinMaxQ");

        double[] minmaxQ = gauge != null
            ? wst.determineMinMaxQ(gauge.getRange())
            : null;

        double minQ = minmaxQ != null ? minmaxQ[0] : Double.MIN_VALUE;
        double maxQ = minmaxQ != null ? minmaxQ[1] : Double.MAX_VALUE;

        return new double[] { minQ, maxQ };
    }


    /** Indicate client which input elements to use. */
    @Override
    protected String getUIProvider() {
        return "wq_panel_adapted";
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("WQAdapted.validate");

        D4EArtifact flys = (D4EArtifact) artifact;
        StateData    data = getData(flys, FIELD_WQ_MODE);

        String mode = data != null ? (String) data.getValue() : null;
        boolean isQ = mode != null
            ? Boolean.valueOf(mode)
            : false;

        if (!isQ) {
            return validateW(artifact);
        }
        else if (isQ) {
            return validateQ(artifact);
        }
        else {
            throw new IllegalArgumentException(
                "error_feed_no_wq_mode_selected");
        }
    }


    protected boolean validateW(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("WQAdapted.validateW");
        D4EArtifact flys = (D4EArtifact) artifact;

        RangeWithValues[] rwvs = extractInput(getData(flys, "wq_values"));

        if (rwvs == null) {
            throw new IllegalArgumentException("error_missing_wq_data");
        }

        List<Gauge> gauges = RiverUtils.getGauges((D4EArtifact) artifact);

        for (Gauge gauge: gauges) {
            Range range  = gauge.getRange();
            double lower = range.getA().doubleValue();
            double upper = range.getB().doubleValue();

            for (RangeWithValues rwv: rwvs) {
                if (lower <= rwv.getStart() && upper >= rwv.getEnd()) {
                    compareWsWithGauge(gauge, rwv.getValues());
                }
            }
        }

        return true;
    }


    protected boolean validateQ(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("WQAdapted.validateQ");
        D4EArtifact flys = (D4EArtifact) artifact;

        RangeWithValues[] rwvs = extractInput(getData(flys, "wq_values"));

        if (rwvs == null) {
            throw new IllegalArgumentException("error_missing_wq_data");
        }

        List<Gauge> gauges = RiverUtils.getGauges(flys);
        River        river = RiverUtils.getRiver(flys);
        Wst            wst = WstFactory.getWst(river);

        for (Gauge gauge: gauges) {
            Range range  = gauge.getRange();
            double lower = range.getA().doubleValue();
            double upper = range.getB().doubleValue();

            for (RangeWithValues rwv: rwvs) {
                if (lower <= rwv.getStart() && upper >= rwv.getEnd()) {
                    compareQsWithGauge(wst, gauge, rwv.getValues());
                }
            }
        }

        return true;
    }


    protected boolean compareQsWithGauge(Wst wst, Gauge gauge, double[] qs)
    throws IllegalArgumentException
    {
        double[] minmax = gauge != null
            ? wst.determineMinMaxQ(gauge.getRange())
            : null;

        if (minmax == null) {
            log.warn("Could not determine min/max Q of gauge.");
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Validate Qs with:");
            log.debug("-- Gauge: " + gauge.getName());
            log.debug("-- Gauge min: " + minmax[0]);
            log.debug("-- Gauge max: " + minmax[1]);
        }

        for (double q: qs) {
            if (q < minmax[0] || q > minmax[1]) {
                throw new IllegalArgumentException(
                    "error_feed_q_values_invalid");
            }
        }

        return true;
    }


    protected boolean compareWsWithGauge(Gauge gauge, double[] ws)
    throws IllegalArgumentException
    {
        double[] minmax = gauge != null
            ? gauge.determineMinMaxW()
            : null;

        if (minmax == null) {
            log.warn("Could not determine min/max W of gauge.");
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Validate Ws with:");
            log.debug("-- Gauge: " + gauge.getName());
            log.debug("-- Gauge min: " + minmax[0]);
            log.debug("-- Gauge max: " + minmax[1]);
        }

        for (double w: ws) {
            if (w < minmax[0] || w > minmax[1]) {
                throw new IllegalArgumentException(
                    "error_feed_w_values_invalid");
            }
        }

        return true;
    }


    protected RangeWithValues[] extractInput(StateData data) {
        if (data == null) {
            return null;
        }

        String dataString = (String) data.getValue();
        String[]   ranges = dataString.split(":");

        List<RangeWithValues> rwv = new ArrayList<RangeWithValues>();

        for (String range: ranges) {
            String[] parts = range.split(";");

            double lower = Double.parseDouble(parts[0]);
            double upper = Double.parseDouble(parts[1]);

            String[] values = parts[3].split(",");

            int      num = values.length;
            double[] res = new double[num];

            for (int i = 0; i < num; i++) {
                try {
                    res[i] = Double.parseDouble(values[i]);
                }
                catch (NumberFormatException nfe) {
                    log.warn(nfe, nfe);
                }
            }

            rwv.add(new RangeWithValues(lower, upper, res));
        }

        return rwv.toArray(new RangeWithValues[rwv.size()]);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
