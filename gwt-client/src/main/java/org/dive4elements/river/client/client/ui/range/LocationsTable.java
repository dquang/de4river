/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.range;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import com.smartgwt.client.widgets.grid.CellFormatter;

import org.dive4elements.river.client.client.FLYSConstants;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LocationsTable extends ListGrid {

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);


    public LocationsTable() {
        String baseUrl = GWT.getHostPageBaseURL();

        setWidth100();
        setHeight100();
        setSelectionType(SelectionStyle.SINGLE);
        setSelectionType(SelectionStyle.SINGLE);
        setShowHeaderContextMenu(false);
        setShowRecordComponents(true);
        setShowRecordComponentsByCell(true);
        setEmptyMessage(MSG.empty_filter());
        setCanReorderFields(false);

        ListGridField addfrom = new ListGridField ("fromIcon", MSG.from());
        addfrom.setType(ListGridFieldType.ICON);
        addfrom.setWidth(30);
        addfrom.setCellIcon(baseUrl + MSG.markerGreen());

        ListGridField addto = new ListGridField("toIcon", MSG.to());
        addto.setType(ListGridFieldType.ICON);
        addto.setWidth(30);
        addto.setCellIcon(baseUrl + MSG.markerRed());

        ListGridField ldescr = new ListGridField(
            "description", MSG.description());
        ldescr.setType(ListGridFieldType.TEXT);
        ldescr.setWidth("*");

        ListGridField lside = new ListGridField("riverside", MSG.riverside());
        lside.setType(ListGridFieldType.TEXT);
        lside.setWidth("12%");

        ListGridField loc = new ListGridField("from", MSG.locations());
        loc.setType(ListGridFieldType.FLOAT);
        loc.setCellFormatter(new CellFormatter() {
            public String format(
                Object value,
                ListGridRecord record,
                int rowNum, int colNum) {
                    if (value == null) return null;
                    try {
                        NumberFormat nf;
                        double v = Double.parseDouble((String)value);
                        nf = NumberFormat.getFormat("###0.00##");
                        return nf.format(v);
                    }
                    catch (Exception e) {
                        return value.toString();
                    }
                }
            }
        );

        loc.setWidth("12%");

        ListGridField bottom = new ListGridField("bottom", MSG.bottom_edge());
        bottom.setType(ListGridFieldType.TEXT);
        bottom.setWidth("10%");

        ListGridField top = new ListGridField("top", MSG.top_edge());
        top.setType(ListGridFieldType.TEXT);
        top.setWidth("10%");

        setFields(addfrom, addto, ldescr, loc, lside, bottom, top);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
