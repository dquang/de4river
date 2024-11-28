/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.exports;

import java.awt.Font;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.injector.InjectorConstants;
import org.dive4elements.river.jfree.AxisDataset;
import org.dive4elements.river.jfree.DoubleBounds;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

public class DischargeGenerator
extends      DiagramGenerator
implements   InjectorConstants
{
    private static Logger log = LogManager.getLogger(DischargeGenerator.class);

    private String I18N_AXIS_LABEL = "chart.discharge.curve.yaxis.cm.label";

    private int wAxisIndex;
    private int wInCmAxisIndex;
    double pnpValue;

    public DischargeGenerator() {
    }

    @Override
    public void addDatasets(XYPlot plot) {
        super.addDatasets(plot);

        Object pnp = context.getContextValue(PNP);
        if (!(pnp instanceof Number)) {
            return;
        }

        pnpValue = ((Number)pnp).doubleValue();

        wAxisIndex = diagramAttributes.getAxisIndex("W");
        if (wAxisIndex == -1) {
            log.warn("No W axis found.");
            return;
        }

        AxisDataset data = datasets.get(wAxisIndex);
        if (data == null) {
            // No W axis
            return;
        }

        if (data.getRange() == null) {
            // No active datasets
            return;
        }

        Range axisRange = inCm(
            plot.getRangeAxis(wAxisIndex).getRange(),
            pnpValue
        );
        Range dataRange = inCm(data.getRange(), pnpValue);

        // Do we have an index for W in cm?
        NumberAxis wInCmAxis = createWinCMAxis(wAxisIndex);
        wInCmAxis.setRange(axisRange);

        wInCmAxisIndex = plot.getRangeAxisCount();
        plot.setRangeAxis(wInCmAxisIndex, wInCmAxis);
        combineYBounds(new DoubleBounds(dataRange), wInCmAxisIndex);
    }

    private static Range inCm(Range r, double pnpValue) {
        double l = r.getLowerBound();
        double u = r.getUpperBound();
        l = (l - pnpValue)*100d;
        u = (u - pnpValue)*100d;
        return new Range(l, u);
    }

    private NumberAxis createWinCMAxis(int wAxisIndex) {

        Font labelFont = new Font(
            DEFAULT_FONT_NAME,
            Font.BOLD,
            getYAxisFontSize(wAxisIndex));

        String axisName = "W.in.cm";
        String axisLabel = Resources.getMsg(context.getMeta(),
            I18N_AXIS_LABEL, "W [cm]");

        IdentifiableNumberAxis axis = new IdentifiableNumberAxis(
            axisName, axisLabel);

        axis.setAutoRangeIncludesZero(false);
        axis.setLabelFont(labelFont);
        axis.setTickLabelFont(labelFont);

        return axis;
    }

     /** We need to override this to keep both axis synced. */
    @Override
    protected void autoZoom(XYPlot plot) {
        super.autoZoom(plot);

        ValueAxis wAxis = plot.getRangeAxis(wAxisIndex);
        if (wAxis instanceof IdentifiableNumberAxis) {
            IdentifiableNumberAxis idA = (IdentifiableNumberAxis)wAxis;
            Range fixedRange = getRangeForAxisFromSettings(idA.getId());
            if (fixedRange == null) {
                return;
            }

            log.debug("Adjusting helper centimeter axis to fixed range.");
            Range adjustedRange = inCm(
                fixedRange,
                pnpValue
            );
            IdentifiableNumberAxis wInCmAxis=
                (IdentifiableNumberAxis) plot.getRangeAxis(wInCmAxisIndex);
            wInCmAxis.setRange(adjustedRange);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
