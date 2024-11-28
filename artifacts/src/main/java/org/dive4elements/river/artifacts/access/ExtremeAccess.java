/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.RangeWithValues;

import org.dive4elements.river.utils.DoubleUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Access data used for extreme value analysis. */
public class ExtremeAccess
extends      RangeAccess
{
    /** Our private log. */
    private static Logger log = LogManager.getLogger(ExtremeAccess.class);

    protected Long start;
    protected Long end;

    protected Double percent;

    protected String function;

    protected List<RangeWithValues> ranges;


    public ExtremeAccess(D4EArtifact artifact) {
        super(artifact);
    }


    /** Returns the percent given. */
    public Double getPercent() {

        if (percent == null) {
            percent = getDouble("percent");
        }

        if (log.isDebugEnabled()) {
            log.debug("percent: '" + percent + "'");
        }

        return percent;
    }

    public String getFunction() {
        if (function == null) {
            function = getString("function");
        }

        if (log.isDebugEnabled()) {
            log.debug("function: '" + function + "'");
        }

        return function;
    }


    /** Find first RangeWithValues for which the given location
     * is within the range and return its values.
     * @return values of first suitable rangewithvalues or null.
     */
    public double[] getValuesForRange(double location) {
        log.debug("ExtemeAcces.getValuesForRange");
        for (RangeWithValues rangeValues: getRanges()) {
            if (rangeValues.inside(location)) {
                return rangeValues.getValues();
            }
        }
        return null;
    }


    public List<RangeWithValues> getRanges() {

        if (ranges == null) {
            String rangesS = getString("ranges");
            if (rangesS == null) {
                return null;
            }
            ranges = new ArrayList<RangeWithValues>();
            DoubleUtil.parseSegments(
                rangesS,
                new DoubleUtil.SegmentCallback() {
                    @Override
                    public void newSegment(
                        double from,
                        double to,
                        double [] values
                    ) {
                        ranges.add(new RangeWithValues(from, to, values));
                    }
                });
        }

        if (log.isDebugEnabled()) {
            log.debug("ranges: " + ranges);
        }

        return ranges;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
