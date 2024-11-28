/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;

import java.util.List;


public class DistanceOnlyPanel extends DistancePanel {

    private static final long serialVersionUID = -5794138573892656947L;


    public DistanceOnlyPanel() {
        this("right");
    }


    public DistanceOnlyPanel(String labelOrientation) {
        distancePanel = new DoubleRangeOnlyPanel(
            labelFrom(), labelTo(), 0d, 0d, 250, this, labelOrientation);
    }


    @Override
    protected String getOldSelectionString(DataList dataList) {
        List<Data> items = dataList.getAll();

        Data dFrom = getData(items, getLowerField());
        Data dTo   = getData(items, getUpperField());

        DataItem[] from = dFrom.getItems();
        DataItem[] to   = dTo.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append(from[0].getLabel());
        sb.append(" " + getUnitFrom() + " - ");
        sb.append(to[0].getLabel());
        sb.append(" " + getUnitTo());

        return sb.toString();
    }


    @Override
    protected void initDefaultStep(DataList data) {
        // do nothing
    }


    @Override
    public Data[] getData() {
        Data[] data = new Data[2];

        data[0] = getDataFrom();
        data[1] = getDataTo();

        return data;
    }


    @Override
    protected String labelFrom() {
        return getUnitFrom() + " - ";
    }


    @Override
    protected String labelTo() {
        return getUnitTo();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
