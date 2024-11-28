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
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.client.event.DatacageHandler;
import org.dive4elements.river.client.client.event.DatacageDoubleClickHandler;
import org.dive4elements.river.client.client.event.HasRedrawRequestHandlers;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.event.RedrawRequestEvent;
import org.dive4elements.river.client.client.event.RedrawRequestEvent.Type;

import org.dive4elements.river.client.client.services.LoadArtifactService;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.ToLoad;
import org.dive4elements.river.client.shared.model.User;
import org.dive4elements.river.client.shared.model.Recommendation;


/** Window that access the datacageservice and shows a datacagewidget. */
public class DatacageWindow
extends      Window
implements   DatacageHandler,
             DatacageDoubleClickHandler,
             HasRedrawRequestHandlers
{
    /** i18ner. */
    protected FLYSConstants messages =
        GWT.create(FLYSConstants.class);

    /** Service to create/clone artifacts. */
    protected LoadArtifactServiceAsync loadService =
        GWT.create(LoadArtifactService.class);

    protected CollectionView view;

    protected List<RedrawRequestHandler> handlers;

    protected int inProgress;


    public DatacageWindow(
        Artifact       artifact,
        User           user,
        String         outs,
        CollectionView view
    ) {
        this.view       = view;
        this.handlers   = new ArrayList<RedrawRequestHandler>();
        this.inProgress = 0;

        setWidth(400);
        setHeight(500);

        DatacageWidget dw = new DatacageWidget(
            artifact,
            user,
            outs,
            "load-system:true",
            true);
        dw.addDatacageHandler(this);
        dw.addDatacageDoubleClickHandler(this);

        addItem(dw);

        String river =  findRiver(artifact);
        // TODO: i18n
        setTitle("Datenkorb: " + river);
        setShowMinimizeButton(false);
        setIsModal(true);
        setShowModalMask(true);
        setCanDragResize(true);

        centerInPage();
    }


    @Override
    public void toLoad(ToLoad toLoad) {
        destroy();
        List<Recommendation> recs = toLoad.toRecommendations();
        loadArtifacts(recs);
    }


    @Override
    public void onDoubleClick(ToLoad toLoad) {
        destroy();
        List<Recommendation> recs = toLoad.toRecommendations();
        loadArtifacts(recs);
    }


    @Override
    public void addRedrawRequestHandler(RedrawRequestHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    protected String findRiver(Artifact artifact) {
        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList [] data = adescr.getOldData();

        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                DataList dl = data[i];
                if (dl.getState().equals("state.winfo.river")) {
                    for (int j = dl.size()-1; j >= 0; --j) {
                        Data d = dl.get(j);
                        DataItem [] di = d.getItems();
                        if (di != null && di.length == 1) {
                           return d.getItems()[0].getStringValue();
                        }
                    }
                }
            }
        }

        return "";
    }


    protected void decreateInProgress() {
        if (this.inProgress > 0) {
            this.inProgress--;
        }

        if (this.inProgress == 0) {
            fireRedrawRequest();
        }
    }


    protected void fireRedrawRequest() {
        RedrawRequestEvent evt = new RedrawRequestEvent(Type.DEFAULT);

        for (RedrawRequestHandler handler: handlers) {
            handler.onRedrawRequest(evt);
        }
    }


    protected void loadArtifacts(List<Recommendation> recommendations) {
        Config cfg = Config.getInstance();

        final Collection collection     = view.getCollection();
        final Artifact   masterArtifact = view.getArtifact();
        final String     locale         = cfg.getLocale();

        this.inProgress = recommendations.size();

        for (final Recommendation recommendation: recommendations) {
            // XXX: UGLY! If no reference artifact given use uuid of
            //      current artifact as reference.
            if (recommendation.getMasterArtifact() == null) {
                recommendation.setMasterArtifact(masterArtifact.getUuid());
            }

            final String factory = recommendation.getFactory();

            GWT.log("Load new artifact with factory: " + factory);

            loadService.load(
                collection,
                recommendation,
                factory,
                locale,
                new AsyncCallback<Artifact>() {
                    public void onFailure(Throwable caught) {
                        decreateInProgress();
                        GWT.log("Create-artifact failed: "
                            + caught.getMessage());
                        SC.warn(caught.getMessage());
                    }

                    public void onSuccess(Artifact artifact) {
                        decreateInProgress();
                        GWT.log("Created new artifact: " + artifact.getUuid());
                    }
            });
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
