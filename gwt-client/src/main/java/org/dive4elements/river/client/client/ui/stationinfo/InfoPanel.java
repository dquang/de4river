/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.layout.VLayout;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.RiverInfoService;
import org.dive4elements.river.client.client.services.RiverInfoServiceAsync;
import org.dive4elements.river.client.client.ui.RiverInfoPanel;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.RiverInfo;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public abstract class InfoPanel extends VLayout {

    /** The instance of FLYS */
    protected FLYS flys;

    /** Name of the river */
    protected String river;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected RiverInfoServiceAsync riverInfoService = GWT.create(
        RiverInfoService.class);

    /** Panel to show the info about the river */
    protected RiverInfoPanel riverinfopanel;

    protected InfoListGrid listgrid;

    public final static String SECTION_ID = "InfoPanelSection";

    public InfoPanel(FLYS flys, InfoListGrid listgrid) {
        setOverflow(Overflow.HIDDEN);
        setStyleName("infopanel");

        this.flys = flys;

        this.listgrid = listgrid;
        this.addMember(listgrid);
    }

    /**
     * Sets and loads the river data if river is not the current set river.
     */
    public void setRiver(String river) {
        if (!river.equals(this.river)) {
            this.river = river;
            this.refresh();
        }
    }

    /**
     * Sets the data and closes not corresponding folds in the gauge tree.
     */
    public void setData(DataList[] data) {
        this.listgrid.setData(data);
    }

    protected void render(RiverInfo riverinfo) {
        if (this.riverinfopanel == null) {
            this.riverinfopanel = new RiverInfoPanel(this.flys, riverinfo);

            this.addMember(this.riverinfopanel, 0);
        }
        else {
            riverinfopanel.setRiverInfo(riverinfo);
        }
        this.listgrid.setRiverInfo(riverinfo);
    }

    protected void removeAllMembers() {
        removeMembers(getMembers());
    }

    protected abstract void refresh();

    public abstract String getSectionTitle();
}
