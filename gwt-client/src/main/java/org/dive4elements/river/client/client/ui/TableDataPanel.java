/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.CSVExportService;
import org.dive4elements.river.client.client.services.CSVExportServiceAsync;
import org.dive4elements.river.client.shared.model.DataList;

import java.util.List;

/**
 * This UIProvider creates a widget that displays calculated data in a table.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class TableDataPanel
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected CSVExportServiceAsync exportService =
        GWT.create(CSVExportService.class);

    /** A container that will contain the location or the distance panel. */
    protected VLayout container;

    /** The export type. */
    protected String name;

    /** The UUID of the collection. */
    protected String uuid;

    /** The table. */
    protected ListGrid dataTable;


    /**
     * Creates a new TableDataPanel instance.
     */
    public TableDataPanel() {
        container = new VLayout();
        dataTable = new ListGrid();
        name      = "";
    }


    /**
     * This method creates a widget that contains a table.
     *
     * @return a panel.
     */
    public Canvas create() {
        Config config    = Config.getInstance();
        String locale    = config.getLocale ();
        dataTable.setEmptyMessage(MESSAGES.empty_table());
        dataTable.setShowHeaderContextMenu(false);
        dataTable.setCanDragSelectText(true);

        exportService.getCSV(locale, uuid, name,
            new AsyncCallback<List<String[]>>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not receive csv.");
                    SC.warn(caught.getMessage());
                }

                @Override
                public void onSuccess(List<String[]> l) {
                    GWT.log("Recieved csv with " + l.size() + " lines.");
                    setData(l);
                }
            }
        );

        container.addMember(dataTable);

        return container;
    }


    public void setName(String name) {
      this.name = name;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }


    public Canvas createOld(DataList dataList) {
        return null;
    }


    protected Canvas createWidget(DataList data) {
        return null;
    }


    /**
     * This method sets the data to a dynamic table.
     *
     * @param list List of String[] containing the data.
     */
    public void setData(List<String[]> list) {
        if (list == null || list.size() < 2) {
            dataTable.setEmptyMessage(MESSAGES.error_no_calc_result());
            dataTable.redraw();
            return;
        }

        Config config = Config.getInstance();
        String locale = config.getLocale();

        NumberFormat nf;
        if (locale.equals("de")) {
            nf = NumberFormat.getFormat("#,##");
        }
        else {
            nf = NumberFormat.getFormat("#.##");
        }

        String[] header      = list.get(0);
        String[] displayField = new String[header.length];

        ListGridField[] fields = new ListGridField[header.length];

        for(int i = 0; i < header.length; i++) {
            ListGridField f = new ListGridField(String.valueOf(i));
            fields[i] = f;
            f.setTitle(header[i]);

            try {
                /* Try to determine the type with the first
                 * non empty element. */
                for (int j = 1; j < list.size(); j++) {
                    if (!list.get(j)[i].isEmpty()) {
                        nf.parse(list.get(j)[i]);
                        f.setType(ListGridFieldType.FLOAT);
                        break;
                    }
                }
            }
            catch (NumberFormatException nfe) {
                f.setType(ListGridFieldType.TEXT);
            }

            // To keep server-side formatting and i18n also of
            // float values, we will store the value once formatted 'as is'
            // to be displayed and once as e.g. float to allow functions like
            // sorting on it.
            displayField[i] = i + "_displayField";
            f.setDisplayField(displayField[i]);
            f.setValueField(String.valueOf(i));
            f.setSortByDisplayField(false);
        }

        dataTable.setFields(fields);

        for(int i = 1; i < list.size(); i++) {
            String[] sItem = list.get(i);
            ListGridRecord r = new ListGridRecord();
            for(int j = 0; j < sItem.length; j++) {
                // See above, display 'as is' from server, but keep value
                // in machine-usable way (float), to allow numeric sorting.
                r.setAttribute(displayField[j], sItem[j]);
                if (fields[j].getType() == ListGridFieldType.TEXT) {
                    r.setAttribute(String.valueOf(j), sItem[j]);
                }
                else {
                    try {
                        r.setAttribute(String.valueOf(j), nf.parse(sItem[j]));
                    }
                    catch (NumberFormatException nfe) {
                        r.setAttribute(String.valueOf(j), sItem[j]);
                    }
                }
            }
            dataTable.addData(r);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
