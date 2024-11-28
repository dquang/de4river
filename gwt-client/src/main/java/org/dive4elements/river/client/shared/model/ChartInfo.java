/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;

import org.dive4elements.river.client.shared.Transform2D;


/**
 * Give information about chart dimension and transform of chart<->pixel
 * space.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartInfo implements Serializable {

    protected Axis[] xAxes;
    protected Axis[] yAxes;

    protected Transform2D[] transformer;


    public ChartInfo() {
    }


    public ChartInfo(Axis[] xAxes, Axis[] yAxes, Transform2D[] transformer) {
        this.xAxes       = xAxes;
        this.yAxes       = yAxes;
        this.transformer = transformer;
    }


    public Transform2D getTransformer(int pos) {
        if (pos >= 0 && pos < transformer.length) {
            return transformer[pos];
        }

        return null;
    }


    public int getTransformerCount() {
        return transformer.length;
    }


    public int getXAxisCount() {
        return xAxes.length;
    }


    public int getYAxisCount() {
        return yAxes.length;
    }


    public Axis getXAxis(int pos) {
        if (pos >= 0 && pos < xAxes.length) {
            return xAxes[pos];
        }

        return null;
    }


    public Axis getYAxis(int pos) {
        if (pos >= 0 && pos < yAxes.length) {
            return yAxes[pos];
        }

        return null;
    }


    public void dumpGWT() {
        StringBuilder sb = new StringBuilder();

        Axis x = getXAxis(0);

        GWT.log("X axis:");
        GWT.log("... from " + x.getFrom() + " to " + x.getTo());
        GWT.log("... min " + x.getMin() + " max " + x.getMax());

        for (int i = 0, count = getYAxisCount(); i < count; i++) {
            Axis y = getYAxis(i);

            GWT.log("Y axis " + i + ":");
            GWT.log("... from " + y.getFrom() + " to " + y.getTo());
            GWT.log("... min " + y.getMin() + " max " + y.getMax());
        }

        for (Transform2D t: transformer) {
            t.dumpGWT();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
