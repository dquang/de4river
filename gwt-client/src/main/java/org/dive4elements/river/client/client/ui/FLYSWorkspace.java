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
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;

import java.util.HashMap;
import java.util.Map;


/**
 * "Workspace" canvas showing the CollectionViews (subwindows).
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FLYSWorkspace extends Canvas {

    /** The maximal number of windows that fit into the browser view when an
     * offset is used to move windows initially.*/
    public static int MAX_WINDOWS = 10;

    /** The number of pixels used to move windows.*/
    public static int WINDOW_OFFSET = 20;

    /** A map that contains the open CollectionViews. */
    protected Map<String, CollectionView> views;

    /** The interface that provides the message resources. */
    private FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    /** Application instance. */
    private FLYS flys;


    /**
     * The default constructor creates an empty FLYSWorkspace with no
     * CollectionViews opened.
     */
    public FLYSWorkspace(FLYS flys) {
        this.flys = flys;
        views = new HashMap<String, CollectionView>();

        setWidth("100%");
        setHeight("100%");

        addBackgroundWorkspace();
    }


    /**
     * This method adds a new CollectionView to this workspace and stores a
     * reference in {@link views}.
     *
     * @param collectionView A new CollectionView.
     */
    public void addView(String uuid, CollectionView collectionView) {
        collectionView.moveTo(0, 0);
        collectionView.setMaximized(true);

        views.put(uuid, collectionView);
        addChild(collectionView);
    }


    public void removeProject(String uuid) {
        views.remove(uuid);
    }


    public void bringUp(String uuid) {
        CollectionView view = views.get(uuid);

        if (view != null) {
            view.show();
            view.restore();
        }
        else {
            GWT.log("FLYSWorkspace.bringUp() failed!");
        }
    }


    /**
     * Removes a project from workspace (view) and clears its reference from
     * hash map.
     *
     * @param uuid The project's uuid.
     */
    public void destroyProject(String uuid) {
        CollectionView project = views.get(uuid);

        if (project != null) {
            removeProject(uuid);
            project.destroy();
        }
    }


    public void updateTitle(String uuid, String title) {
        CollectionView view = views.get(uuid);
        view.setTitle(title);
    }


    public boolean hasView(String uuid) {
        if(views.get(uuid) != null) {
            return true;
        }
        return false;
    }

    private void addBackgroundWorkspace() {
        HLayout backgroundlayout = new HLayout();
        backgroundlayout.setHeight100();
        backgroundlayout.setWidth100();
        backgroundlayout.setDefaultLayoutAlign(Alignment.CENTER);
        backgroundlayout.setDefaultLayoutAlign(VerticalAlignment.CENTER);

        Canvas spacer = new Canvas();
        spacer.setWidth("33%");

        VLayout infobox = new VLayout();
        infobox.setHeight("*");
        infobox.setWidth("*");
        infobox.setDefaultLayoutAlign(Alignment.CENTER);

        Label welcome = new Label(MESSAGES.welcome());
        welcome.setAlign(Alignment.CENTER);
        welcome.setStyleName("fontNormalBig");

        Label lcreate = new Label(MESSAGES.welcome_open_or_create());
        lcreate.setStyleName("welcomeCreateText");
        lcreate.setWidth100();
        lcreate.setAlign(Alignment.CENTER);

        Button addbutton = new Button(MESSAGES.new_project());
        addbutton.setStyleName("projectsAddButton");
        addbutton.setAlign(Alignment.CENTER);
        addbutton.setTitle(MESSAGES.new_project());
        addbutton.setAutoFit(true);
        addbutton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                flys.newProject();
            }
        });


        infobox.addMember(welcome);
        infobox.addMember(lcreate);
        infobox.addMember(addbutton);

        backgroundlayout.addMember(spacer);
        backgroundlayout.addMember(infobox);
        backgroundlayout.addMember(spacer);

        addChild(backgroundlayout);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
