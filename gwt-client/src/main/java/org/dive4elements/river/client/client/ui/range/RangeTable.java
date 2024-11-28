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
public class RangeTable extends ListGrid {

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);


    public RangeTable() {
        String baseUrl = GWT.getHostPageBaseURL();

        setWidth100();
        setHeight100();
        setSelectionType(SelectionStyle.SINGLE);
        setSelectionType(SelectionStyle.SINGLE);
        setShowHeaderContextMenu(false);
        setShowRecordComponents(true);
        setShowRecordComponentsByCell(true);
        setEmptyMessage(MESSAGES.empty_filter());
        setCanReorderFields(false);

        ListGridField addDistance = new ListGridField ("", "");
        addDistance.setType (ListGridFieldType.ICON);
        addDistance.setWidth (20);
        addDistance.setCellIcon(baseUrl + MESSAGES.markerGreen());

        ListGridField ddescr = new ListGridField(
            "description", MESSAGES.description());
        ddescr.setType(ListGridFieldType.TEXT);
        ddescr.setWidth("*");
        ListGridField from = new ListGridField("from", MESSAGES.from());
        from.setType(ListGridFieldType.FLOAT);
        from.setCellFormatter(new CellFormatter() {
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

        from.setWidth("12%");

        ListGridField to = new ListGridField("to", MESSAGES.to());
        to.setType(ListGridFieldType.FLOAT);
        to.setCellFormatter(new CellFormatter() {
            public String format(
                Object value,
                ListGridRecord record,
                int rowNum, int colNum) {
                    if (value == null) return null;
                    GWT.log((String)value);
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

        to.setWidth("12%");

        ListGridField dside = new ListGridField(
            "riverside", MESSAGES.riverside());
        dside.setType(ListGridFieldType.TEXT);
        dside.setWidth("12%");

        ListGridField bottom = new ListGridField(
            "bottom", MESSAGES.bottom_edge());
        bottom.setType(ListGridFieldType.TEXT);
        bottom.setWidth("10%");

        ListGridField top = new ListGridField("top", MESSAGES.top_edge());
        top.setType(ListGridFieldType.TEXT);
        top.setWidth("10%");

        setFields(addDistance, ddescr, from, to, dside, bottom, top);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
