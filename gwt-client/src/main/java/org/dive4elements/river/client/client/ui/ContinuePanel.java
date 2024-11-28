/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.AdvanceHandler;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;

import java.util.ArrayList;
import java.util.List;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ContinuePanel extends AbstractUIProvider {

    private static final long serialVersionUID = -5882814816875137397L;

    protected static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected List<AdvanceHandler> advHandlers;


    @Override
    public Canvas create(DataList dataList) {
        DynamicForm form = new DynamicForm();
        form.setWidth(200);
        form.setHeight(35);

        LinkItem next = new LinkItem();
        next.setShowTitle(false);
        next.setLinkTitle(MSG.next());

        final ArtifactDescription desc = artifact.getArtifactDescription();
        final String[] reachable       = desc.getReachableStates();

        next.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                fireOnAdvance(reachable[0]);
            }
        });

        form.setFields(next);

        return form;
    }


    @Override
    public Canvas createOld(DataList dataList) {
        return null;
    }


    @Override
    protected Data[] getData() {
        return new Data[0];
    }


    public void addAdvanceHandler(AdvanceHandler handler) {
        if (advHandlers == null) {
            advHandlers = new ArrayList<AdvanceHandler>();
        }

        if (handler != null) {
            advHandlers.add(handler);
        }
    }


    public void fireOnAdvance(String target) {
        if (advHandlers == null || advHandlers.isEmpty()) {
            return;
        }

        for (AdvanceHandler handler: advHandlers) {
            handler.onAdvance(target);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
