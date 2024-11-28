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
 * A Panel to show info about the MeasurementStations of a river
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class MeasurementStationPanel extends InfoPanel {

    /**
     * MeasurementStationPanel loads the MeasurementStations from the
     * RiverInfoService and displays them in a tree underneath a RiverInfoPanel
     *
     * @param flys The FLYS object
     */
    public MeasurementStationPanel(FLYS flys) {
        super(flys, new MeasurementStationListGrid(flys));
    }

    /**
     * Returns the title which should be displayed in the section
     */
    @Override
    public String getSectionTitle() {
        return MSG.measurementStationPanelTitle();
    }

    /**
     * Loads the river info and renders it afterwards
     */
    @Override
    public void refresh() {
        GWT.log("MeasurementStationPanel - refresh");

        riverInfoService.getMeasurementStations(this.river,
            new AsyncCallback<RiverInfo>() {
                @Override
                public void onFailure(Throwable e) {
                    GWT.log("Could not load the river info." + e);
                }

                @Override
                public void onSuccess(RiverInfo riverinfo) {
                    GWT.log("MeasurementStationPanel - Loaded river info");
                    render(riverinfo);
                }
        });
    }
}
