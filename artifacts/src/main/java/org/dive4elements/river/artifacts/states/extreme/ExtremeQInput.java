/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.extreme;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.access.ExtremeAccess;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.WstValueTable;
/*
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.Wst;
import org.dive4elements.river.utils.RiverUtils;
*/
import org.dive4elements.river.artifacts.model.Range;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.RangeWithValues;
import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.artifacts.model.WstValueTableFactory;


/** TODO Subclass WQAdapted. */

/**
 * State to input Q data in segments for extreme value calculations..
 * The data item ranges is expected to have this format
 * <from1>;<to1>;<value1>:<from2>;<to2>;<value2>:...
 * (;;;:;;;:;;;:...)
 */
public class ExtremeQInput extends DefaultState {

    /** The log used in this state.*/
    private static Logger log = LogManager.getLogger(ExtremeQInput.class);


    /** Trivial, empty constructor. */
    public ExtremeQInput() {
    }


    /**
     * Create one element for each 'segment' of the selected river that
     * is within the given kilometer range (TODO). Each element is a tuple of
     * (from;to) where <i>from</i> is the lower bounds of the segment or the
     * lower kilometer range. <i>to</i> is the upper bounds of the segment or
     * the upper kilometer range.
     *
     * @param cr The ElementCreator.
     * @param artifact The FLYS artifact.
     * @param name The name of the data item.
     * @param context The CallContext.
     *
     * @return a list of elements that consist of tuples of the intersected
     *         segments of the selected river.
     */
    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        log.debug("ExtremeQInput.createItems: " + name);

        D4EArtifact flysArtifact = (D4EArtifact) artifact;

        ExtremeAccess access = new ExtremeAccess(flysArtifact);
        River river = access.getRiver();
        WstValueTable wstValueTable = WstValueTableFactory.getTable(river);

        List<Range> ranges   = wstValueTable.findSegments(access.getFrom(),
            access.getTo());

        int num = ranges != null ? ranges.size() : 0;

        if (num == 0) {
            log.warn("Selected distance matches no segments.");
            return null;
        }

        List<Element> elements = new ArrayList<Element>();

        for (Range range: ranges) {
            elements.add(createItem(
                    cr,
                    new String[] { range.getStart() + ";" + range.getEnd(),
                                   ""},
                    new double[] {0,100000}));
        }

        Element[] els = new Element[elements.size()];

        return elements.toArray(els);
    }


    /** Create sub-item ('row') of data thing. */
    protected Element createItem(
        XMLUtils.ElementCreator cr,
        Object   obj,
        double[] q
        )
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

        return item;
    }


    /**
     * Create elements to set min and max values of segments q (just min makes
     * sense for extremes.
     */
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


    @Override
    protected String getUIProvider() {
        return "q_segmented_panel";
    }


    /** Validate given data (return true). */
    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("ExtremeQInput.validate");

        D4EArtifact flys = (D4EArtifact) artifact;
        log.debug("ExtremeQInput: " + getData(flys, "ranges"));

        /*
        // TODO sort out what has to be validated (prevent negative values?).
        RangeWithValues[] rwvs = extractInput(getData(flys, "ranges"));

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
        */

        return true;
    }


    /** Form RangeWithValue-Array from state data. */
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

            String[] values = parts[2].split(",");

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
