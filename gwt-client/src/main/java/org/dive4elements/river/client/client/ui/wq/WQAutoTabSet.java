/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.wq;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.dive4elements.river.client.client.services.WQInfoService;
import org.dive4elements.river.client.client.services.WQInfoServiceAsync;

import org.dive4elements.river.client.shared.model.WQInfoObject;
import org.dive4elements.river.client.shared.model.WQInfoRecord;

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.client.Config;

/** Tabset showing non-selectable W and Q/D values for a gauge. */
public class WQAutoTabSet extends TabSet {

    /** Service to fetch W/Q/D values. */
    WQInfoServiceAsync wqInfoService =
            GWT.create(WQInfoService.class);

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGE = GWT.create(FLYSConstants.class);

    /** Table showing Q/D values. */
    protected QDTable qdTable;

    /** Table showing W values. */
    protected WTable wTable;


    /** Set up two tabs showing W and Q/D values, fetch and populate tables. */
    public WQAutoTabSet(String riverName, double[] dist) {
        super();

        this.setWidth100();
        this.setHeight100();

        Tab wTab = new Tab(MESSAGE.wq_table_w());
        Tab qTab = new Tab(MESSAGE.wq_table_q());

        qdTable = new QDTable();
        qdTable.hideIconFields();
        wTable  = new WTable();

        wTab.setPane(wTable);
        qTab.setPane(qdTable);

        this.addTab(wTab, 0);
        this.addTab(qTab, 1);

        Config config = Config.getInstance();
        String locale = config.getLocale();
        wqInfoService.getWQInfo(locale, riverName, dist[0], dist[1],
            new AsyncCallback<WQInfoObject[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not receive wq informations.");
                    SC.warn(caught.getMessage());
                }

                @Override
                public void onSuccess(WQInfoObject[] wqi) {
                    int num = wqi != null ? wqi.length :0;
                    GWT.log("Received " + num + " wq informations.");

                    if (num == 0) {
                        return;
                    }

                    addWQInfo(wqi);
                }
            }
        );
    }


    /** Populate tables with one value. */
    private void addWQInfo (WQInfoObject[] wqi) {
        for(WQInfoObject wi: wqi) {
            WQInfoRecord rec = new WQInfoRecord(wi);

            if (wi.getType().equals("W")) {
                wTable.addData(rec);
            }
            else {
                qdTable.addData(rec);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
