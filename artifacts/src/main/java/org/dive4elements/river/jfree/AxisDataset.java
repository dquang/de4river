/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.util.List;
import java.util.ArrayList;

import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.time.TimeSeriesCollection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Axis datasets.
 */
public class AxisDataset
{
    private static Logger log = LogManager.getLogger(AxisDataset.class);

    /** Symbolic integer, but also coding the priority (0 goes first). */
    protected int axisSymbol;

    /** List of assigned datasets (in order). */
    protected List<XYDataset> datasets;

    /** Range to use to include all given datasets. */
    protected Range range;

    /** Index of axis in plot. */
    protected int plotAxisIndex;

    protected boolean rangeDirty;

    /** Create AxisDataset. */
    public AxisDataset(int symb) {
        axisSymbol = symb;
        datasets   = new ArrayList<XYDataset>();
    }

    /** Add a dataset to internal list for this axis. */
    public void addDataset(XYDataset dataset) {
        datasets.add(dataset);
        rangeDirty = true;
    }

    /** Add a dataset. */
    public void addDataset(XYSeries series) {
        addDataset(new XYSeriesCollection(series));
    }

    public void setRange(Range val) {
        range = val;
    }

    /** Get Range for the range axis of this dataset. */
    public Range getRange() {
        if (range != null && !rangeDirty) {
            return range;
        }
        /* Calculate the min / max of all series */
        for (XYDataset dataset: datasets) {
            Range newRange = null;
            if (dataset instanceof StyledAreaSeriesCollection) {
                /* We do not include areas in the range calculation because
                 * they are used with very large / small values to draw areas
                 * with axis boundaries */
                continue;
            } else if (dataset instanceof RangeInfo) {
                /* The usual case for most series */
                newRange = ((RangeInfo) dataset).getRangeBounds(false);
            } else if (dataset instanceof TimeSeriesCollection) {
                /* Lalala <3 Jfreechart's class hirarchy */
                newRange = ((TimeSeriesCollection)dataset)
                    .getRangeBounds(false);
            }

            /* Now we only expand as we also only add new data */
            if (range == null) {
                range = newRange;
            } else {
                range = Range.combine(range, newRange);
            }
        }
        rangeDirty = false;
        return range;
    }

    /** Get Array of Datasets. */
    public XYDataset[] getDatasets() {
        return datasets.toArray(new XYDataset[datasets.size()]);
    }

    /** True if to be rendered as area. */
    public boolean isArea(XYDataset series) {
        return (series instanceof StyledAreaSeriesCollection);
    }

    /** True if no datasets given. */
    public boolean isEmpty() {
        return datasets.isEmpty();
    }

    /** Set the 'real' axis index that this axis is mapped to. */
    public void setPlotAxisIndex(int axisIndex) {
        plotAxisIndex = axisIndex;
    }

    /** Get the 'real' axis index that this axis is mapped to. */
    public int getPlotAxisIndex() {
        return plotAxisIndex;
    }

    /** Add a Dataset that describes an area. */
    public void addArea(StyledAreaSeriesCollection series) {
        addDataset(series);
    }

}
