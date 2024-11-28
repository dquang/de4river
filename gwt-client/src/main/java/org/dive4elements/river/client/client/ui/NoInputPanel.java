/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.Canvas;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;


public class NoInputPanel
extends      AbstractUIProvider
{
    private static final long serialVersionUID = -8789143404415288132L;


    @Override
    public Canvas create(DataList data) {
        return new Canvas();
    }


    @Override
    public Canvas createOld(DataList dataList) {
        return new Canvas();
    }


    @Override
    protected Data[] getData() {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
