/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ToLoad;
import org.dive4elements.river.client.shared.model.User;


/**
 * Widget showing two Datacages and a add-this-button.
 * Insert a record into a listgrid when add-this-button clicked.
 */
public class DatacagePairWidget
extends      VLayout
{
    /** i18n resource. */
    protected FLYSConstants MSG =
        GWT.create(FLYSConstants.class);

    /** The "remote" ListGrid to insert data to when add-button is clicked. */
    protected ListGrid grid;

    /** First (upper) DataCage Grid. */
    protected DatacageWidget firstDatacageWidget;

    /** Second (lower) DataCage Grid. */
    protected DatacageWidget secondDatacageWidget;


    /**
     *
     * @param artifact Artifact to query datacage with.
     * @param user     User to query datacage with.
     * @param outs     outs to query datacage with.
     * @param grid     Grid into which to insert selection of pairs.
     */
    public DatacagePairWidget(Artifact artifact,
         User user,
         String outs,
         ListGrid grid) {
        this.grid = grid;

        HLayout hLayout      = new HLayout();
        firstDatacageWidget  = new DatacageWidget(
            artifact,
            user,
            outs,
            "load-system:true",
            false);
        secondDatacageWidget = new DatacageWidget(
            artifact,
            user,
            outs,
            "load-system:true",
            false);
        firstDatacageWidget.setIsMutliSelectable(false);
        secondDatacageWidget.setIsMutliSelectable(false);

        hLayout.addMember(firstDatacageWidget);
        hLayout.addMember(secondDatacageWidget);

        // TODO: icon
        Button plusBtn = new Button(MSG.datacage_add_pair());
        plusBtn.setAutoFit(true);
        plusBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                plusClicked();
            }
        });

        addMember(hLayout);
        addMember(plusBtn);
    }


    /**
     * Callback for add-button.
     * Fires to load for every selected element and handler.
     */
    public void plusClicked() {
        ToLoad toLoad1 = firstDatacageWidget.getSelection();
        ToLoad toLoad2 = secondDatacageWidget.getSelection();

        if (toLoad1 == null || toLoad2 == null ||
            toLoad1.toRecommendations().isEmpty() ||
            toLoad2.toRecommendations().isEmpty()) {
            SC.say(MSG.warning_select_two_values());
            return;
        }

        grid.addData(new RecommendationPairRecord(
            toLoad1.toRecommendations().get(0),
            toLoad2.toRecommendations().get(0)));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
