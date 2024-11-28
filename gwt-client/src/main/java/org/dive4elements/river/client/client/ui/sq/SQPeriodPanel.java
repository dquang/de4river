/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.sq;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;

import org.dive4elements.river.client.client.ui.PeriodPanel;

public class SQPeriodPanel extends PeriodPanel implements ResizedHandler {

    private SQCampaignChart chartLayout;

    @Override
    protected Canvas createHelper() {
        GWT.log("Create new SQCampaignChart as Helper Widget");
        chartLayout = new SQCampaignChart(artifact, this);
        return chartLayout;
    }


    @Override
    public void onResized(ResizedEvent re) {
        chartLayout.update();
    }
}
