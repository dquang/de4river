/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.minfo;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.TreeNode;

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.client.services.RemoveArtifactServiceAsync;

import org.dive4elements.river.client.client.ui.DatacageTwinPanel;
import org.dive4elements.river.client.client.ui.DatacageWidget;
import org.dive4elements.river.client.client.ui.RecommendationPairRecord;

import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.ToLoad;

import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.List;

// TODO Probably better to branch off AbstractUIProvider.
public class BedHeightsDatacagePanel
extends      DatacageTwinPanel {

    protected static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /**
     * List to track previously selected but now removed pairs. (Needed to
     * be able to identify artifacts that can be removed from the collection.
     */
    protected List<RecommendationPairRecord> removedPairs =
        new ArrayList<RecommendationPairRecord>();

    /** Service handle to clone and add artifacts to collection. */
    LoadArtifactServiceAsync loadArtifactService = GWT.create(
        org.dive4elements.river.client.client.services
        .LoadArtifactService.class);

    /** Service to remove artifacts from collection. */
    RemoveArtifactServiceAsync removeArtifactService = GWT.create(
        org.dive4elements.river.client.client.services
        .RemoveArtifactService.class);

    protected DatacageWidget datacage;

    public BedHeightsDatacagePanel(User user) {
        super(user);
    }

    /**
     * Creates graphical representation and interaction widgets for the data.
     * @param dataList the data.
     * @return graphical representation and interaction widgets for data.
     */
    @Override
    public Canvas create(DataList dataList) {
        GWT.log("createData()");

        Canvas widget = createWidget();
        Canvas submit = getNextButton();
        datacage = new DatacageWidget(
            this.artifact, user, "minfo_diff_panel", "load-system:true", false);

        Button plusBtn = new Button(MSG.datacage_add_pair());
        plusBtn.setAutoFit(true);
        plusBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                plusClicked();
            }
        });

        VLayout layout       = new VLayout();
        VLayout helperLayout = new VLayout();
        helperLayout.addMember(datacage);
        helperLayout.addMember(plusBtn);

        layout.addMember(widget);
        layout.addMember(submit);
        layout.setMembersMargin(10);
        this.helperContainer.addMember(helperLayout);

        populateGrid(dataList, "bedheight");

        return layout;
    }

    public void adjustRecommendation(Recommendation recommendation) {
        recommendation.setFactory("bedheight");
    }

    @Override
    protected String createDataString(
        String artifact,
        Recommendation recommendation
    ) {
        return createDataString(artifact, recommendation, "bedheight");
    }

    /**
     * Callback for add-button.
     * Fires to load for every selected element and handler.
     */
    public void plusClicked() {
        List<TreeNode> selection = datacage.getPlainSelection();

        if (selection == null || selection.isEmpty()) {
            SC.say(MSG.warning());
            return;
        }

        for (TreeNode node : selection) {
            ToLoad toLoad1 = new ToLoad();
            ToLoad toLoad2 = new ToLoad();

            String factory = node.getAttribute("factory");
            if (factory != null) { // we need at least a factory
                String artifact    = node.getAttribute("artifact-id");
                String out         = node.getAttribute("out");
                String name        = node.getAttribute("facet");
                String ids         = node.getAttribute("ids");
                String info        = node.getAttribute("info");
                String targetOut   = node.getAttribute("target_out");

                String[] splitIds = ids.split("#");
                String[] splitInfo = info.split("#");
                toLoad1.add(artifact,
                     factory,
                     out,
                     name,
                     splitIds[0],
                     splitInfo[0],
                     targetOut);
                toLoad2.add(artifact,
                     factory,
                     out,
                     name,
                     splitIds[1],
                     splitInfo[1],
                     targetOut);
            }
            differencesList.addData(new RecommendationPairRecord(
                toLoad1.toRecommendations().get(0),
                toLoad2.toRecommendations().get(0)));
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
