/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.shared.model.RiverInfo;


/**
 * The GaugePanel is intended to be used within a SectionStackSection
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class GaugePanel extends InfoPanel {

    /**
     * GaugePanel loads the GaugeInfo from the RiverInfoService and
     * displays them in a tree underneath a RiverInfoPanel
     *
     * @param flys The FLYS object
     */
    public GaugePanel(FLYS flys) {
        super(flys, new GaugeListGrid(flys));
    }


    @Override
    public String getSectionTitle() {
        return MSG.gaugePanelTitle();
    }

    /**
     * Loads the river info and renders it afterwards.
     */
    public void refresh() {

        riverInfoService.getGauges(this.river, new AsyncCallback<RiverInfo>() {
            @Override
            public void onFailure(Throwable e) {
                GWT.log("Could not load the river info." + e);
            }

            @Override
            public void onSuccess(RiverInfo riverinfo) {
                GWT.log("Loaded river info");
                render(riverinfo);
            }
        });
    }
}
