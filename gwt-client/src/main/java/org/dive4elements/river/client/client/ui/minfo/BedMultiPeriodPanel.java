/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.minfo;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.ui.MultiPeriodPanel;

public class BedMultiPeriodPanel
extends MultiPeriodPanel
implements ResizedHandler {
    protected BedCampaignChart chartContainer1;
    protected BedloadCampaignChart chartContainer2;

    public BedMultiPeriodPanel() {
    }

    @Override
    protected Canvas createHelper() {
        chartContainer1 = new BedCampaignChart(artifact, this);
        chartContainer2 = new BedloadCampaignChart(artifact, this);
        HLayout layout = new HLayout();
        layout.addMember(chartContainer1);
        layout.addMember(chartContainer2);
        return layout;
    }

    @Override
    public void onResized(ResizedEvent re) {
        chartContainer1.update();
        chartContainer2.update();
    }
}
