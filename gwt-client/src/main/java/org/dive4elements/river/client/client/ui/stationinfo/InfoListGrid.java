/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.ui.WikiLinks;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.RiverInfo;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public abstract class InfoListGrid extends ListGrid {

    protected FLYS flys;
    protected DataList[] data;
    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);


    public InfoListGrid(FLYS flys) {
        super();
        this.flys = flys;
        this.setCanExpandRecords(true);
        this.setCanExpandMultipleRecords(true);
    }

    @Override
    protected Canvas createRecordComponent(
        final ListGridRecord record,
        Integer colNum
    ) {
        String name = this.getFieldName(colNum);
        if (name.equals("infolink")) {
            return WikiLinks.linkDynamicForm(
                flys,
                record.getAttribute("link"),
                record.getLinkText());
        }
        else {
            return null;
        }
    }

    public void openAll() {
        GWT.log("InfoListGrid - openAll");
        for (ListGridRecord record: this.getRecords()) {
            expandRecord(record);
        }
    }

    public void setData(DataList[] data) {
        GWT.log("InfoListGrid - setData");
        this.data = data;
        this.open();
    }

    protected Double getDoubleValue(Data d) {
        String tmp = d.getStringValue();
        if (tmp != null) {
            return Double.valueOf(tmp);
        }
        return null;
    }

    @Override
    protected Canvas getExpansionComponent(ListGridRecord record) {
        return this.getExpandPanel(record);
    }

    public abstract void open();

    public abstract void setRiverInfo(RiverInfo riverinfo);

    protected abstract Canvas getExpandPanel(ListGridRecord record);
}
