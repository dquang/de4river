/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.OnMoveEvent;
import org.dive4elements.river.client.client.event.OnMoveHandler;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ThemeNavigationPanel extends Canvas {

    public static final int PANEL_MARGIN  = 5;
    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_MARGIN = 5;
    private static final int BOTTON_WIDTH = 20;

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected List<OnMoveHandler> handlers;


    public ThemeNavigationPanel() {
        this.handlers = new ArrayList<OnMoveHandler>();

        setWidth100();
        setHeight(BUTTON_HEIGHT);
        setMargin(PANEL_MARGIN);

        HLayout layout = new HLayout();
        layout.setWidth100();
        layout.setHeight(BUTTON_HEIGHT);
        layout.setMembersMargin(BUTTON_MARGIN);
        layout.setDefaultLayoutAlign(VerticalAlignment.CENTER);
        layout.setDefaultLayoutAlign(Alignment.CENTER);

        Canvas cu = createButton(MSG.theme_top(), OnMoveEvent.TOP);
        Canvas u  = createButton(MSG.theme_up(), OnMoveEvent.UP);
        Canvas d  = createButton(MSG.theme_down(), OnMoveEvent.DOWN);
        Canvas cd = createButton(MSG.theme_bottom(), OnMoveEvent.BOTTOM);

        HLayout left = new HLayout();
        left.setMembersMargin(BUTTON_MARGIN);
        left.setLayoutAlign(Alignment.LEFT);
        left.setDefaultLayoutAlign(Alignment.LEFT);
        left.setAlign(Alignment.LEFT);
        left.addMember(cu);
        left.addMember(u);

        HLayout right = new HLayout();
        right.setMembersMargin(BUTTON_MARGIN);
        right.setLayoutAlign(Alignment.RIGHT);
        right.setDefaultLayoutAlign(Alignment.RIGHT);
        right.setAlign(Alignment.RIGHT);
        right.addMember(d);
        right.addMember(cd);

        layout.addMember(left);
        layout.addMember(right);

        addChild(layout);
    }


    protected Canvas createButton(final String title, final int moveType) {
        String url = GWT.getHostPageBaseURL() + title;

        ImgButton b = new ImgButton();
        b.setSrc(url);
        b.setWidth(BOTTON_WIDTH);
        b.setHeight(BUTTON_HEIGHT);
        b.setIconHeight(BUTTON_HEIGHT);
        b.setIconWidth(BOTTON_WIDTH);
        b.setShowDown(false);
        b.setShowRollOver(false);
        b.setShowDisabled(false);
        b.setShowDisabledIcon(true);
        b.setShowDownIcon(false);
        b.setShowFocusedIcon(false);
        b.setValign(VerticalAlignment.CENTER);

        b.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                fireOnMoveEvent(moveType);
            }
        });

        return b;
    }


    protected void addOnMoveHandler(OnMoveHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    protected void fireOnMoveEvent(int type) {
        OnMoveEvent event = new OnMoveEvent(type);

        for (OnMoveHandler handler: handlers) {
            handler.onMove(event);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
