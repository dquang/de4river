/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Positioning;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.HasStepBackHandlers;
import org.dive4elements.river.client.client.event.StepBackEvent;
import org.dive4elements.river.client.client.event.StepBackHandler;
import org.dive4elements.river.client.client.services.DescribeArtifactService;
import org.dive4elements.river.client.client.services.DescribeArtifactServiceAsync;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.CalculationMessage;
import org.dive4elements.river.client.shared.model.DataList;

import java.util.ArrayList;
import java.util.List;


public class LoadingPanel extends Canvas implements HasStepBackHandlers {

    private static final long serialVersionUID = -7806425431408987601L;

    public static final int UPDATE_INTERVAL = 1000 * 3;

    public static final DescribeArtifactServiceAsync describe =
        GWT.create(DescribeArtifactService.class);

    private FLYSConstants MSG    = GWT.create(FLYSConstants.class);


    protected List<StepBackHandler> handlers;

    protected CollectionView parent;
    protected Artifact       artifact;

    protected VLayout dialog;
    protected HLayout cancelRow;
    protected Label   msg;
    protected Label   title;

    protected int i;


    public LoadingPanel(CollectionView parent, Artifact artifact) {
        super();

        this.handlers = new ArrayList<StepBackHandler>();
        this.parent   = parent;
        this.artifact = artifact;
        this.msg      = new Label("");
        this.title    = new Label("");
        this.dialog   = createDialog();

        this.i = 0;

        initLayout();
        startTimer();
    }


    private void initLayout() {
        setWidth("100%");
        setHeight("98%");
        setBackgroundColor("#7f7f7f");
        setOpacity(50);
        setPosition(Positioning.RELATIVE);

        parent.addChild(this);
        parent.addChild(dialog);
        dialog.moveTo(0, 20);
        moveTo(0, 7);
    }


    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }


    public Artifact getArtifact() {
        return artifact;
    }


    @Override
    public void addStepBackHandler(StepBackHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    /**
     * This method is called after the user has clicked the button to cancel the
     * current process.
     *
     * @param e The StepBackEvent.
     */
    protected void fireStepBackEvent(StepBackEvent e) {
        for (StepBackHandler handler: handlers) {
            handler.onStepBack(e);
        }
    }


    protected VLayout createDialog() {

        String baseUrl = GWT.getHostPageBaseURL();

        title.setStyleName("loading-title");
        title.setHeight(25);
        title.setWidth100();

        msg.setStyleName("loading-message");
        msg.setValign(VerticalAlignment.TOP);
        msg.setWidth100();
        msg.setHeight(100);

        Img img = new Img(baseUrl + MSG.loadingImg(), 25, 25);

        Label cancelLabel = new Label(MSG.cancelCalculationLabel());
        Img   cancel      = new Img(baseUrl + MSG.cancelCalculation(), 25, 25);
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                cancel();
            }
        });

        cancelRow = new HLayout();
        cancelRow.setHeight(27);
        cancelRow.setWidth100();
        cancelRow.addMember(cancel);
        cancelRow.addMember(cancelLabel);

        VLayout box = new VLayout();
        box.setStyleName("loading-box");
        box.setAlign(VerticalAlignment.TOP);
        box.setDefaultLayoutAlign(VerticalAlignment.TOP);
        box.addMember(title);
        box.addMember(msg);
        box.addMember(cancelRow);
        box.setMembersMargin(0);
        box.setHeight(125);
        box.setWidth(275);

        dialog = new VLayout();
        dialog.setAlign(Alignment.CENTER);
        dialog.setDefaultLayoutAlign(Alignment.CENTER);
        dialog.setMembersMargin(5);
        dialog.setHeight100();
        dialog.setWidth100();

        dialog.addMember(img);
        dialog.addMember(box);

        return dialog;
    }


    public String getTargetState() {
        ArtifactDescription desc = getArtifact().getArtifactDescription();
        DataList[]       oldData = desc.getOldData();

        return oldData[oldData.length -1].getState();
    }


    private void startTimer() {
        Timer t = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        t.schedule(UPDATE_INTERVAL);
    }


    protected void update() {
        updateMessage();

        final Config config = Config.getInstance();
        final String locale = config.getLocale();

        describe.describe(locale, artifact, new AsyncCallback<Artifact>() {
            @Override
            public void onFailure(Throwable t) {
                GWT.log("Error while DESCRIBE artifact: " + t.getMessage());

                startTimer();
            }

            @Override
            public void onSuccess(Artifact artifact) {
                GWT.log("Successfully DESCRIBE artifact.");

                setArtifact(artifact);

                if (artifact.isInBackground()) {
                    startTimer();
                }
                else {
                    finish();
                }
            }
        });
    }


    protected void updateMessage() {
        List<CalculationMessage> messages = artifact.getBackgroundMessages();
        if (messages != null && messages.size() > 0) {
            CalculationMessage calcMsg = messages.get(0);
            title.setContents(getStepTitle(calcMsg));
            msg.setContents(calcMsg.getMessage());
        }
        else {
            title.setContents(MSG.calculationStarted());
        }
    }


    protected String getStepTitle(CalculationMessage msg) {
        return MSG.step() + " " + msg.getCurrentStep() + "/" + msg.getSteps();
    }


    private void cancel() {
        fireStepBackEvent(new StepBackEvent(getTargetState()));
        parent.removeChild(dialog);
        parent.removeChild(this);
    }


    private void finish() {
        parent.removeChild(dialog);
        parent.removeChild(this);
        parent.setArtifact(artifact);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
