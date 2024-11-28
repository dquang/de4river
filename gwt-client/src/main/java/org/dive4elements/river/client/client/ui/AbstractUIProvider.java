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
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.event.HasStepBackHandlers;
import org.dive4elements.river.client.client.event.HasStepForwardHandlers;
import org.dive4elements.river.client.client.event.StepBackEvent;
import org.dive4elements.river.client.client.event.StepBackHandler;
import org.dive4elements.river.client.client.event.StepForwardEvent;
import org.dive4elements.river.client.client.event.StepForwardHandler;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.List;


/**
 * An abstract UIProvider that provides some basic methods.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class AbstractUIProvider
implements   UIProvider, HasStepForwardHandlers, ClickHandler,
             HasStepBackHandlers
{
    private static final long serialVersionUID = -1610874613377494184L;

    /** The message class that provides i18n strings. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** The StepForwardHandlers. */
    protected List<StepForwardHandler> forwardHandlers;

    /** The StepForwardHandlers. */
    protected List<StepBackHandler> backHandlers;

    /** The container that is used to position helper widgets. */
    protected VLayout helperContainer;

    /** The artifact that contains status information. */
    protected Artifact artifact;

    /** The Collection. */
    protected Collection collection;

    /** The ParameterList. */
    protected ParameterList parameterList;

    /**
     * Creates a new UIProvider instance of this class.
     */
    public AbstractUIProvider() {
        forwardHandlers = new ArrayList<StepForwardHandler>();
        backHandlers    = new ArrayList<StepBackHandler>();
    }


    /**
     * Appends a StepBackHandler that wants to listen to StepBackEvents.
     *
     * @param handler A new StepBackHandler.
     */
    @Override
    public void addStepBackHandler(StepBackHandler handler) {
        if (handler != null) {
            backHandlers.add(handler);
        }
    }


    /**
     * Appends a StepForwardHandler that wants to listen to StepForwardEvents.
     *
     * @param handler A new StepForwardHandler.
     */
    @Override
    public void addStepForwardHandler(StepForwardHandler handler) {
        if (handler != null) {
            forwardHandlers.add(handler);
        }
    }


    /**
     * This method is called after the user has clicked one of the buttons to
     * step back to a previous state.
     *
     * @param e The StepBackEvent.
     */
    protected void fireStepBackEvent(StepBackEvent e) {
        GWT.log("AbstractUIProvider - fireStepBackEvent() handlers: "
            + backHandlers.size());
        for (StepBackHandler handler: backHandlers) {
            handler.onStepBack(e);
        }
    }


    /**
     * This method is called after the user has clicked on the 'next' button to
     * step to the next state.
     *
     * @param e The StepForwardEvent.
     */
    protected void fireStepForwardEvent(StepForwardEvent e) {
        GWT.log("AbstractUIProvider - fireStepForwardEvent() handlers: "
            + forwardHandlers.size());
        for (StepForwardHandler handler: forwardHandlers) {
            handler.onStepForward(e);
        }
    }


    /**
     * This method is used to listen to click events on the 'next' button. The
     * fireStepForwardEvent() method is called here.
     *
     * @param e The click event.
     */
    @Override
    public void onClick(ClickEvent e) {
        List<String> errors = validate();
        if (errors == null || errors.isEmpty()) {
            Data[] data = getData();
            fireStepForwardEvent(new StepForwardEvent(data));
        }
        else {
            showErrors(errors);
        }
    }


    protected void showErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder();

        for (String error: errors) {
            sb.append(error);
            sb.append("<br>");
        }

        SC.warn(sb.toString());
    }


    /**
     * Creates the 'next' button to step forward to the next state.
     *
     * @return the 'next' button.
     */
    protected Canvas getNextButton() {
        Button next = new Button(MSG.buttonNext());
        next.addClickHandler(this);

        return next;
    }


    @Override
    public Canvas createHelpLink(DataList dataList, Data data, FLYS instance) {
        String iUrl    = GWT.getHostPageBaseURL() + MSG.getFeatureInfo();
        String helpUrl = dataList.getHelpText();

        return new WikiImgLink(iUrl, helpUrl, 30, 30, instance);
    }


    /**
     * Creates the 'back' button to step back to a previous state.
     *
     * @param targetState The identifier of the target state.
     *
     * @return the 'back' button.
     */
    protected Canvas getBackButton(final String targetState) {
        String url = GWT.getHostPageBaseURL() + MSG.imageBack();
        Img back   = new Img(url, 16, 16);

        back.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireStepBackEvent(new StepBackEvent(targetState));
            }
        });

        return back;
    }


    /**
     * This method injects a container that is used to position helper widgets.
     *
     * @param helperContainer A container that is used to position helper
     * widgets.
     */
    @Override
    public void setContainer(VLayout helperContainer) {
        this.helperContainer = helperContainer;
    }


    /**
     * This method injects an artifact that contains the status information.
     *
     * @param art An artifact containing status information.
     */
    @Override
    public void setArtifact(Artifact art) {
        this.artifact = art;
    }


    @Override
    public void setCollection(Collection collection) {
        this.collection = collection;
    }


    @Override
    public void setParameterList(ParameterList list) {
        this.parameterList = list;
    }


    public Collection getCollection() {
        return collection;
    }


    /**
     * This method greps the Data with name <i>name</i> from the list and
     * returns it.
     *
     * @param items A list of Data.
     * @param name The name of the Data that we are searching for.
     *
     * @return the Data with the name <i>name</i>.
     */
    protected Data getData(List<Data> data, String name) {
        for (Data d: data) {
            if (name.equals(d.getLabel())) {
                return d;
            }
        }

        return null;
    }


    protected String getDataValue(String state, String name) {
        ArtifactDescription desc = artifact.getArtifactDescription();

        DataList[] old = desc.getOldData();

        for (DataList list: old) {
            if (list == null) {
                continue;
            }
            Data d = getData(list.getAll(), name);

            if (d != null) {
                return d.getItems()[0].getStringValue();
            }
        }

        return null;
    }

    /**
     * This method greps the DataItem with name <i>name</i> from the list and
     * returns it.
     *
     * @param items A list of DataItems.
     * @param name The name of the DataItem that we are searching for.
     *
     * @return the DataItem with the name <i>name</i>.
     */
    protected DataItem getDataItem(DataItem[] items, String name) {
        for (DataItem item: items) {
            if (name.equals(item.getLabel())) {
                return item;
            }
        }

        return null;
    }


    public List<String> validate() {
        return new ArrayList<String>(); // FIXME: What's this?
    }


    /** Create simple DefaultData with single DataItem inside. */
    public static DefaultData createDataArray(String name, String value) {
        DataItem item = new DefaultDataItem(
            name,
            name,
            value);

        return new DefaultData(name,
            null,
            null,
            new DataItem[] {item});
    }


    /**
     * This method needs to be implemented by concrete subclasses. It should
     * create a new Canvas object with a representation of <i>data</i>.
     *
     * @param data The data that should be displayed.
     *
     * @return a Canvas object that displays <i>data</i>.
     */
    @Override
    public abstract Canvas create(DataList data);


    /**
     * This method needs to be implemented by concrete subclasses. It should
     * return the selected data.
     *
     * @return the selected data.
     */
    protected abstract Data[] getData();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
