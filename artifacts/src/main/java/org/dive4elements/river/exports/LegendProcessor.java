/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.XYPlot;


/** Class to process Plots legends. */
public abstract class LegendProcessor {

    /** (Empty) shape for aggregated Legend Items. */
    private static final Line2D.Double SPACE = new Line2D.Double(0,0,0,0);


    /** Prevent instantiations. */
    private LegendProcessor() {
    }


    /**
     * Create a hash from a legenditem.
     * This hash can then be used to merge legend items labels.
     * @return hash for given legenditem to identify mergeables.
     */
    protected static String legendItemHash(LegendItem li) {
        // TODO Do proper implementation.
        // Ensure that only mergable sets are created.
        // getFillPaint()
        // getFillPaintTransformer()
        // getLabel()
        // getLine()
        // getLinePaint()
        // getLineStroke()
        // getOutminePaint()
        // getOutlineStroke()
        // Shape getShape()
        // String getToolTipText()
        // String getURLText()
        // boolean isLineVisible()
        // boolean isShapeFilled()
        // boolean isShapeOutlineVisible()
        // boolean isShapeVisible()
        String hash = li.getLinePaint().toString();
        // XXX: DEAD CODE // String label = li.getLabel();
        /*if (label.startsWith("W (") || label.startsWith("W(")) {
            hash += "-W-";
        }
        else if (label.startsWith("Q(") || label.startsWith("Q (")) {
            hash += "-Q-";
        }*/

        // WQ.java holds example of using regex Matcher/Pattern.

        return hash;
    }


    /**
     * Create new legend entries, dependent on settings.
     * @param plot The plot for which to modify the legend.
     * @param threshold How many items are needed for aggregation to
     *                  be triggered?
     */
    public static void aggregateLegendEntries(XYPlot plot, int threshold) {
        LegendItemCollection old = plot.getLegendItems();
        // Find "similar" entries if aggregation is enabled.

        int maxListSize = 0;
        int AGGR_THRESHOLD = threshold;

        if (AGGR_THRESHOLD > old.getItemCount() || AGGR_THRESHOLD <= 0){
            return;
        }

        HashMap<String, List<LegendItem>> entries =
            new LinkedHashMap<String, List<LegendItem>>();
        for (Iterator<LegendItem> i = old.iterator(); i.hasNext();) {
            LegendItem item = i.next();
            String hash = legendItemHash(item);
            List<LegendItem> itemList = entries.get(hash);
            if (itemList == null) {
                itemList = new ArrayList<LegendItem>();
                entries.put(hash, itemList);
            }
            itemList.add(item);

            if (itemList.size() > maxListSize) {
                maxListSize = itemList.size();
            }
        }

        if (maxListSize < AGGR_THRESHOLD) {
            // No need to do anything.
            return;
        }

        // Run over collected entries, merge their names and create new
        // entry if needed.
        LegendItemCollection newLegend = new LegendItemCollection();
        for (List<LegendItem> itemList: entries.values()) {
            if (itemList.size() >= AGGR_THRESHOLD) {
                // Now do merging.
                // XXX: DEAD CODE // LegendItem item = itemList.get(0);
                // Unfortunately we cannot clone and just setDescription,
                // as this method was added in JFreeChart 1.0.14
                // (we are at .13).

                // Remove the shapes of all but the first items,
                // to prevent "overfill" of legenditemblock.
                for (int i = 0, I = itemList.size(); i < I; i++) {
                    if (i != 0) {
                        LegendItem litem = itemList.get(i);

                        // Make shape and line really small.
                        LegendItem merged = new LegendItem(
                            "," + litem.getLabel(),
                            litem.getDescription(),
                            litem.getToolTipText(),
                            litem.getURLText(),
                            false,
                            SPACE,
                            false,
                            litem.getFillPaint(),
                            false,
                            litem.getOutlinePaint(),
                            litem.getOutlineStroke(),
                            false,
                            SPACE,
                            litem.getLineStroke(),
                            litem.getLinePaint());
                        newLegend.add(merged);
                    }
                    else {
                        newLegend.add(itemList.get(i));
                    }
                }
            }
            else {
                // Do not merge entries.
                for (LegendItem li: itemList) {
                    newLegend.add(li);
                }
            }
        }

        plot.setFixedLegendItems (newLegend);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
