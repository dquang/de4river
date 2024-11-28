/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;


/**
 * Vertically speaking the main part of the ui (containing projectlist
 * and workspace).
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FLYSView extends Canvas {

    /** The project list displaying the projects of a user.*/
    protected ProjectList projectList;

    /** The workspace that handles the artifact collection views.*/
    protected FLYSWorkspace workspace;

    /** The layout provided by this widget.*/
    protected HLayout layout;


    /**
     * The default constructor for creating a new empty FLYSView. After creating
     * the components, {@link init()} is called to do the layout work.
     */
    public FLYSView() {
        layout = new HLayout();

        init();
    }


    /**
     * This method is called to do the layout work.
     */
    protected void init() {
        setWidth("100%");
        setHeight("*");

        setStyleName("bgWhite");
        setBorder("1px solid #808080");

        layout.setHeight("100%");
        layout.setWidth("100%");

        addChild(layout);
    }


    /**
     * Set the current project list. Previous ProjectLists are replaced by the
     * new one.
     */
    public void setProjectList(ProjectList projectList) {
        if (this.projectList != null) {
            removeChild(this.projectList);
        }

        this.projectList = projectList;
        layout.addMember(this.projectList);
    }


    /**
     * Set the current FLYSWorkspace. Previous workspaces are replaced by the
     * new one.
     *
     * @param workspaces The new FLYSWorkspace.
     */
    public void setFLYSWorkspace(FLYSWorkspace workspace) {
        if (this.workspace != null) {
            removeChild(this.workspace);
        }

        this.workspace = workspace;
        layout.addMember(this.workspace);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
