/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.dive4elements.river.artifacts.model.fixings.QWI;

import org.dive4elements.river.java2d.ShapeUtils;

import org.dive4elements.river.jfree.ShapeRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;

import java.awt.geom.Rectangle2D;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;

import org.jfree.chart.labels.XYItemLabelGenerator;

import org.jfree.chart.renderer.xy.StandardXYItemRenderer;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class QWSeriesCollection
extends      XYSeriesCollection
implements   XYItemLabelGenerator
{
    public interface LabelGenerator {
        String createLabel(QWI qw);
    } // interface LabelGenerator

    public static class DateFormatLabelGenerator
    implements          LabelGenerator
    {
        protected DateFormat format;

        public DateFormatLabelGenerator() {
            this("dd.MM.yyyy");
        }

        public DateFormatLabelGenerator(String format) {
            this(new SimpleDateFormat(format));
        }

        public DateFormatLabelGenerator(DateFormat format) {
            this.format = format;
        }

        @Override
        public String createLabel(QWI qw) {
            Date date = qw.getDate();
            return date != null ? format.format(date) : "";
        }
    } // class DateFormatLabelGenerator

    public static final LabelGenerator SIMPLE_GENERATOR =
        new DateFormatLabelGenerator();

    protected Date minDate;
    protected Date maxDate;

    protected List<List<QWI>> labels;

    protected Rectangle2D area;

    protected LabelGenerator labelGenerator;

    protected Map<ShapeRenderer.Entry, Integer> knownShapes =
        new HashMap<ShapeRenderer.Entry, Integer>();

    public QWSeriesCollection() {
        labels = new ArrayList<List<QWI>>();
        labelGenerator = SIMPLE_GENERATOR;
    }

    public QWSeriesCollection(LabelGenerator labelGenerator) {
        this();
        this.labelGenerator = labelGenerator;
    }

    protected static ShapeRenderer.Entry classify(QWI qw) {
        boolean interpolated = qw.getInterpolated();

        Shape shape = interpolated
            ? ShapeUtils.INTERPOLATED_SHAPE
            : ShapeUtils.MEASURED_SHAPE;

        boolean filled = !interpolated;
        Color color = Color.blue;

        return new ShapeRenderer.Entry(shape, color, filled);
    }

    public void add(QWI qw) {

        ShapeRenderer.Entry key = classify(qw);

        Integer seriesNo = knownShapes.get(key);

        XYSeries series;

        if (seriesNo == null) {
            seriesNo = Integer.valueOf(getSeriesCount());
            knownShapes.put(key, seriesNo);
            series = new XYSeries(seriesNo, false);
            addSeries(series);
            labels.add(new ArrayList<QWI>());
        }
        else {
            series = getSeries(seriesNo);
        }

        series.add(qw.getQ(), qw.getW());

        labels.get(seriesNo).add(qw);

        extendDateRange(qw);
        extendArea(qw);
    }

    protected void extendDateRange(QWI qw) {
        Date date = qw.getDate();
        if (date != null) {
            if (minDate == null) {
                minDate = maxDate = date;
            }
            else {
                if (date.compareTo(minDate) < 0) {
                    minDate = date;
                }
                if (date.compareTo(maxDate) > 0) {
                    maxDate = date;
                }
            }
        }
    }

    protected void extendArea(QWI qw) {
        if (area == null) {
            area = new Rectangle2D.Double(
                qw.getQ(), qw.getW(), 0d, 0d);
        }
        else {
            area.add(qw.getQ(), qw.getW());
        }
    }

    public Rectangle2D getArea() {
        return area;
    }

    public Date getMinDate() {
        return minDate;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public LabelGenerator getLabelGenerator() {
        return labelGenerator;
    }

    @Override
    public String generateLabel(XYDataset dataset, int series, int item) {
        return labelGenerator.createLabel(labels.get(series).get(item));
    }

    public StandardXYItemRenderer createRenderer() {
        StandardXYItemRenderer renderer = new ShapeRenderer(knownShapes);
        renderer.setBaseItemLabelGenerator(this);
        renderer.setBaseSeriesVisibleInLegend(false);
        renderer.setBaseItemLabelsVisible(true);
        return renderer;
    }

    public static final LegendItem legendItem(
        String  label,
        Paint   paint,
        Shape   shape,
        boolean filled
    ) {
        BasicStroke stroke = new BasicStroke();
        return new LegendItem(
            label,  // label
            null,   // description
            null,   // tooltip
            null,   // url
            true,   // shape visible
            shape,  // shape
            filled, // shape filled
            filled ? paint : Color.white, // fill paint
            true,   // shape outline
            paint,  // outline paint
            stroke, // outline stroke
            false,  // line visible
            shape,  // line
            stroke, // line stroke
            Color.white);
    }

    public void addLegendItems(
        LegendItemCollection         lic,
        ShapeRenderer.LabelGenerator lg
    ) {
        for (ShapeRenderer.Entry entry: knownShapes.keySet()) {
            lic.add(legendItem(
                lg.createLabel(entry),
                entry.getPaint(),
                entry.getShape(),
                entry.getFilled()));
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
