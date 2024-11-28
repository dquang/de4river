/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;

/** Basic static functionality to show spinning wheel. */
public class ScreenLock {

    /** The message class that provides i18n strings. */
    protected static FLYSConstants messages = GWT.create(FLYSConstants.class);

    /** Disables input, grey out, show spinning wheel of joy. */
    public static VLayout lockUI(Layout layout, VLayout lockScreen) {
        if (lockScreen == null) {
            lockScreen = new VLayout();
            lockScreen.setWidth100();
            lockScreen.setHeight100();
            lockScreen.setBackgroundColor("#7f7f7f");
            lockScreen.setOpacity(50);
            lockScreen.setAlign(VerticalAlignment.CENTER);
            lockScreen.setDefaultLayoutAlign(VerticalAlignment.CENTER);

            HLayout inner = new HLayout();
            inner.setAlign(Alignment.CENTER);
            inner.setDefaultLayoutAlign(Alignment.CENTER);
            inner.setOpacity(100);

            Img img = new Img(
                GWT.getHostPageBaseURL() + messages.loadingImg(),
                25, 25);

            inner.addMember(img);

            lockScreen.addMember(inner);
        }

        layout.addChild(lockScreen);
        return lockScreen;
    }

    /** Enable input, remove grey, remove spinning wheel of joy. */
    public static void unlockUI(Layout layout, VLayout lockScreen) {
        layout.removeChild(lockScreen);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

