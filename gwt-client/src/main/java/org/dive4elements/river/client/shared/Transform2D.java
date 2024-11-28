/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;


/**
 * This object supports a linear transformation to transform xy coordinates into
 * an other coordinate system based on scale and translation values.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Transform2D implements Serializable {

    protected String xType;
    protected String yType;

    protected double sx;
    protected double sy;

    protected double tx;
    protected double ty;


    public Transform2D() {
    }


    /**
     * Creates a new transformation with scale and translation factors.
     *
     * @param sx The scale factor for the x axis.
     * @param sy The scale factor for the y axis.
     * @param tx The translation factor for the x axis.
     * @param ty The translation factor for the y axis.
     */
    public Transform2D(double sx, double sy, double tx, double ty) {
        this(sx, sy, tx, ty, "number", "number");
    }


    public Transform2D(
        double sx, double sy,
        double tx, double ty,
        String xType,
        String yType
    ) {
        this.xType = xType;
        this.yType = yType;

        this.sx  = sx;
        this.sy  = sy;
        this.tx  = tx;
        this.ty  = ty;
    }


    /**
     * Transforms the pixel x and y into a new coordinate system based on the
     * scale and translation values specified in the constructor.
     */
    public double[] transform(double x, double y) {
        double resX = sx * x + tx;
        double resY = sy * y + ty;

        return new double[] { resX, resY };
    }


    public String[] format(Number[] xy) {
        String x = null;
        String y = null;

        if (xType.equals("date")) {
            x = formatDate(xy[0].longValue());
        }
        else {
            x = formatNumber(xy[0].doubleValue());
        }

        if (yType.equals("date")) {
            y = formatDate(xy[1].longValue());
        }
        else {
            y = formatNumber(xy[1].doubleValue());
        }

        return new String[] { x, y };
    }


    protected String formatDate(long time) {
        Date date = new Date(time);
        DateTimeFormat df = getDateTimeFormat();

        return df.format(date);
    }


    protected String formatNumber(double number) {
        NumberFormat nf = getNumberFormat();

        return nf.format(number);
    }


    public DateTimeFormat getDateTimeFormat() {
        return DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
    }


    public NumberFormat getNumberFormat() {
        return NumberFormat.getDecimalFormat();
    }


    public void dumpGWT() {
        GWT.log("SX = " + sx);
        GWT.log("SY = " + sy);
        GWT.log("TX = " + tx);
        GWT.log("TY = " + ty);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
