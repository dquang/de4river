/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.jfree.Bounds;


/**
 * This class helps generating chart info documents.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class InfoGeneratorHelper {

    /** Private logging instance. */
    private static final Logger log =
        LogManager.getLogger(InfoGeneratorHelper.class);

    protected ChartGenerator generator;

    public InfoGeneratorHelper(ChartGenerator generator) {
        this.generator = generator;
    }

    /**
     * Triggers the creation of the chart info document.
     *
     * @param chart The JFreeChart chart.
     * @param info An info object that has been created while chart creation.
     *
     * @return the info document.
     */
    public Document createInfoDocument(
        JFreeChart         chart,
        ChartRenderingInfo info)
    {
        log.debug("InfoGeneratorHelper.createInfoDocument");

        Document doc = XMLUtils.newDocument();

        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element chartinfo = cr.create("chartinfo");

        chartinfo.appendChild(createAxesElements(cr, chart));
        chartinfo.appendChild(createTransformationElements(cr, chart, info));

        doc.appendChild(chartinfo);

        return doc;
    }


    /**
     * This method create a axes element that contains all domain and range
     * axes of the given chart.
     *
     * @param cr The ElementCreator.
     * @param chart The chart that provides range information of its axes.
     *
     * @return an element with axes information.
     */
    protected Element createAxesElements(
        ElementCreator     cr,
        JFreeChart         chart)
    {
        log.debug("InfoGeneratorHelper.createRangeElements");

        Element axes = cr.create("axes");

        XYPlot plot = (XYPlot) chart.getPlot();

        int dAxisCount = plot.getDomainAxisCount();
        for (int i = 0; i < dAxisCount; i++) {
            ValueAxis axis = plot.getDomainAxis(i);
            XYDataset data = plot.getDataset(i);

            if (axis != null) {
                Element e = createAxisElement(cr, axis, data, "domain", i);
                axes.appendChild(e);
            }
        }

        int rAxisCount = plot.getRangeAxisCount();
        for (int i = 0; i < rAxisCount; i++) {
            ValueAxis axis = plot.getRangeAxis(i);
            XYDataset data = plot.getDataset(i);

            if (axis == null || data == null) {
                log.warn("Axis or dataset is empty at pos: " + i);
                continue;
            }

            Element e = createAxisElement(cr, axis, data, "range", i);
            axes.appendChild(e);
        }

        return axes;
    }


    /**
     * This method create a axis element for a given <i>axis</i> and
     * <i>type</i>. Type can be one of 'domain' or 'range'.
     *
     * @param cr The ElementCreator
     * @param axis The axis that provides range information.
     * @param dataset The dataset for min/max determination.
     * @param type The axis type ('domain' or 'range').
     * @param pos The position in the chart.
     *
     * @return An element that contains range information of a given axis.
     */
    protected Element createAxisElement(
        ElementCreator cr,
        ValueAxis      axis,
        XYDataset      dataset,
        String         type,
        int            pos)
    {
        log.debug("createAxisElement " + pos);
        log.debug("Axis is from type: " + axis.getClass());

        Element e = cr.create(type);
        cr.addAttr(e, "pos",  String.valueOf(pos), true);

        if (axis instanceof DateAxis) {
            prepareDateAxisElement(
                e, cr, (DateAxis) axis, dataset, type, pos);
        }
        else {
            prepareNumberAxisElement(
                e, cr, (NumberAxis) axis, dataset, type, pos);
        }

        return e;
    }


    protected Element prepareNumberAxisElement(
        Element        e,
        ElementCreator cr,
        NumberAxis     axis,
        XYDataset      dataset,
        String         type,
        int            pos
    ) {
        Range range = axis.getRange();

        cr.addAttr(e, "from", String.valueOf(range.getLowerBound()), true);
        cr.addAttr(e, "to",   String.valueOf(range.getUpperBound()), true);
        cr.addAttr(e, "axistype", "number", true);

        Range[] rs = generator.getRangesForAxis(pos);
        Range   r  = null;

        if (type.equals("range")) {
            r = rs[1];
        }
        else {
            r = rs[0];
        }

        cr.addAttr(e, "min", String.valueOf(r.getLowerBound()), true);
        cr.addAttr(e, "max", String.valueOf(r.getUpperBound()), true);

        return e;
    }


    protected Element prepareDateAxisElement(
        Element        e,
        ElementCreator cr,
        DateAxis       axis,
        XYDataset      dataset,
        String         type,
        int            pos
    ) {
        Date from = axis.getMinimumDate();
        Date to   = axis.getMaximumDate();

        Bounds bounds = null;
        if (type.equals("range")) {
            bounds = generator.getYBounds(pos);
        }
        else {
            bounds = generator.getXBounds(pos);
        }

        cr.addAttr(e, "axistype", "date", true);
        cr.addAttr(e, "from", String.valueOf(from.getTime()), true);
        cr.addAttr(e, "to", String.valueOf(to.getTime()), true);

        cr.addAttr(e, "min", bounds.getLower().toString(), true);
        cr.addAttr(e, "max", bounds.getUpper().toString(), true);

        return e;
    }


    /**
     * This method appends the values of a transformation matrix to transform
     * image pixel coordinates into chart coordinates.
     *
     * @param cr The ElementCreator.
     * @param chart The chart object.
     * @param info The ChartRenderingInfo that is filled while chart creation.
     *
     * @return an element that contains one or more transformation matrix.
     */
    protected Element createTransformationElements(
        ElementCreator     cr,
        JFreeChart         chart,
        ChartRenderingInfo info)
    {
        log.debug("InfoGeneratorHelper.createTransformationElements");

        Element tf = cr.create("transformation-matrix");

        Rectangle2D dataArea = info.getPlotInfo().getDataArea();

        XYPlot    plot  = (XYPlot) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();

        if (xAxis == null) {
            log.error("There is no x axis in the chart!");
            return null;
        }

        for (int i  = 0, num = plot.getRangeAxisCount(); i < num; i++) {
            ValueAxis yAxis = plot.getRangeAxis(i);

            if (yAxis == null) {
                log.warn("No y axis at pos " + i + " existing.");
                continue;
            }

            Element matrix = createTransformationElement(
                cr, xAxis, yAxis, dataArea, i);

            tf.appendChild(matrix);
        }

        return tf;
    }


    /**
     * Creates an element that contains values used to transform coordinates
     * of a coordinate system A into a coordinate system B.
     *
     * @param cr The ElementCreator.
     * @param xAxis The x axis of the target coordinate system.
     * @param yAxis The y axis of the target coordinate system.
     * @param dataArea The pixel coordinates of the chart image.
     * @param pos The dataset position.
     *
     * @return an element that contains transformation matrix values.
     */
    protected Element createTransformationElement(
        ElementCreator cr,
        ValueAxis      xAxis,
        ValueAxis      yAxis,
        Rectangle2D    dataArea,
        int            pos)
    {
        double[] tm = createTransformationMatrix(dataArea, xAxis, yAxis);

        Element matrix = cr.create("matrix");

        cr.addAttr(matrix, "pos", String.valueOf(pos), true);
        cr.addAttr(matrix, "sx", String.valueOf(tm[0]), true);
        cr.addAttr(matrix, "sy", String.valueOf(tm[1]), true);
        cr.addAttr(matrix, "tx", String.valueOf(tm[2]), true);
        cr.addAttr(matrix, "ty", String.valueOf(tm[3]), true);

        if (xAxis instanceof DateAxis) {
            cr.addAttr(matrix, "xtype", "date", true);
        }
        else {
            cr.addAttr(matrix, "xtype", "number", true);
        }

        if (yAxis instanceof DateAxis) {
            cr.addAttr(matrix, "ytype", "date", true);
        }
        else {
            cr.addAttr(matrix, "ytype", "number", true);
        }

        return matrix;
    }


    /**
     * This method determines a transformation matrix to transform pixel
     * coordinates of the chart image into chart coordinates.
     *
     * @param dataArea The rectangle that contains the data points of the chart.
     * @param xAxis The x axis.
     * @param yAxis The y axis.
     *
     * @return a double array as follows: [sx, sy, tx, ty].
     */
    protected static double[] createTransformationMatrix(
        Rectangle2D dataArea,
        ValueAxis   xAxis,
        ValueAxis   yAxis)
    {
        log.debug("InfoGeneratorHelper.createTransformationMatrix");

        double offsetX = dataArea.getX();
        double width   = dataArea.getWidth();
        double offsetY = dataArea.getY();
        double height  = dataArea.getHeight();

        Range xRange = getRangeFromAxis(xAxis);
        Range yRange = getRangeFromAxis(yAxis);

        double lowerX  = xRange.getLowerBound();
        double upperX  = xRange.getUpperBound();
        double lowerY  = yRange.getLowerBound();
        double upperY  = yRange.getUpperBound();

        if (xAxis.isInverted()) {
            log.info("X-Axis is inverted!");

            double tmp = upperX;
            upperX = lowerX;
            lowerX = tmp;
        }

        double dMoveX = upperX - lowerX;
        double fMoveX = width * lowerX;
        double dMoveY = lowerY - upperY;
        double fMoveY = height * upperY;

        AffineTransform t1 = AffineTransform.getTranslateInstance(
                offsetX - ( fMoveX / dMoveX ),
                offsetY - ( fMoveY / dMoveY ) );

        AffineTransform t2 = AffineTransform.getScaleInstance(
                width / (upperX - lowerX),
                height / (lowerY - upperY));

        t1.concatenate(t2);

        try {
            t1.invert();

            double[] c = new double[6];
            t1.getMatrix(c);

            return new double[] { c[0], c[3], c[4], c[5] };
        }
        catch (NoninvertibleTransformException e) {
            // do nothing
            log.warn("Matrix is not invertible.");
        }

        return new double[] { 1d, 1d, 0d, 0d };
    }


    protected static Range getRangeFromAxis(ValueAxis axis) {
        if  (axis instanceof DateAxis) {
            DateAxis dAxis = (DateAxis) axis;
            Date     min   = dAxis.getMinimumDate();
            Date     max   = dAxis.getMaximumDate();

            return new Range(min.getTime(), max.getTime());
        }
        else {
            return axis.getRange();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
