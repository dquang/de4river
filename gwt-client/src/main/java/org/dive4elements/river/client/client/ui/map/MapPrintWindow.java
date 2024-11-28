/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.smartgwt.client.widgets.Window;
import com.google.gwt.core.client.GWT;

import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.client.FLYSConstants;

public class MapPrintWindow extends Window {
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected MapPrintPanel panel;

    public MapPrintWindow(Collection collection, MapToolbar mapToolbar) {
        setWidth(255);
        setHeight(300);

        setTitle(MSG.printWindowTitle());
        centerInPage();

        this.panel = new MapPrintPanel(collection, mapToolbar, this);
        this.panel.setPadding(20);
        addItem(this.panel);
    }
}
