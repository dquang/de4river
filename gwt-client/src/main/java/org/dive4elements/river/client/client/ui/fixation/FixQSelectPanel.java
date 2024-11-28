/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.fixation;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;

/**
 * This UIProvider creates a panel for location or distance input.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixQSelectPanel
extends      FixationPanel
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    public FixQSelectPanel() {
        htmlOverview = "";
    }

    public Canvas createWidget(DataList data) {
        instances.put(this.artifact.getUuid(), this);

        return new Canvas();
    }

    @Override
    public Canvas createOld(DataList dataList) {
        return new Canvas();
    }


    /**
     * This method returns the selected data.
     *
     * @return the selected/inserted data.
     */
    public Data[] getData() {
        return new Data[0];
    }


    @Override
    public void setValues(String cid, boolean checked) {
        // No user interaction, do nothing.
    }


    @Override
    public boolean renderCheckboxes() {
        // No selection, return false.
        return false;
    }


    public void success() {}
}
