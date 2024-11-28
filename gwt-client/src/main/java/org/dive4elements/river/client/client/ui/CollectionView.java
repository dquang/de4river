/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.util.SC;

import com.smartgwt.client.widgets.Window;

import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;

import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

import com.smartgwt.client.widgets.tab.TabSet;

import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.client.event.CollectionChangeEvent;
import org.dive4elements.river.client.client.event.CollectionChangeHandler;
import org.dive4elements.river.client.client.event.HasCollectionChangeHandlers;
import org.dive4elements.river.client.client.event.HasOutputModesChangeHandlers;
import org.dive4elements.river.client.client.event.OutputModesChangeEvent;
import org.dive4elements.river.client.client.event.OutputModesChangeHandler;
import org.dive4elements.river.client.client.event.ParameterChangeEvent;
import org.dive4elements.river.client.client.event.ParameterChangeHandler;

import org.dive4elements.river.client.client.services.AddArtifactService;
import org.dive4elements.river.client.client.services.AddArtifactServiceAsync;
import org.dive4elements.river.client.client.services.ArtifactService;
import org.dive4elements.river.client.client.services.ArtifactServiceAsync;
import org.dive4elements.river.client.client.services.CollectionAttributeService;
import org.dive4elements.river.client.client.services.CollectionAttributeServiceAsync;
import org.dive4elements.river.client.client.services.CreateCollectionService;
import org.dive4elements.river.client.client.services.CreateCollectionServiceAsync;
import org.dive4elements.river.client.client.services.DescribeCollectionService;
import org.dive4elements.river.client.client.services.DescribeCollectionServiceAsync;
import org.dive4elements.river.client.client.services.LoadArtifactService;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.ExportMode;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.ReportMode;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CollectionView
extends      Window
implements   CollectionChangeHandler, HasCollectionChangeHandlers,
             OutputModesChangeHandler, HasOutputModesChangeHandlers,
             ParameterChangeHandler, CloseClickHandler
{
    /** The ArtifactService used to communicate with the Artifact server. */
    protected CreateCollectionServiceAsync createCollectionService =
        GWT.create(CreateCollectionService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected ArtifactServiceAsync createArtifactService =
        GWT.create(ArtifactService.class);

    /** The AddArtifactService used to add an artifact to a collection. */
    protected AddArtifactServiceAsync addArtifactService =
        GWT.create(AddArtifactService.class);

    /** The DescribeCollectionService used to update the existing collection. */
    protected DescribeCollectionServiceAsync describeCollectionService =
        GWT.create(DescribeCollectionService.class);

    protected CollectionAttributeServiceAsync updater =
        GWT.create(CollectionAttributeService.class);

    /** The LoadArtifactService used to load recommendations*/
    protected LoadArtifactServiceAsync loadArtifactService =
        GWT.create(LoadArtifactService.class);

    /** The message class that provides i18n strings. */
    protected FLYSConstants messages = GWT.create(FLYSConstants.class);

    /** The FLYS instance used to call services. */
    protected FLYS flys;

    /** The ParameterList. */
    protected ParameterList parameterList;

    /** The list of CollectionChangeHandlers. */
    protected List<CollectionChangeHandler> handlers;

    /** The list of OutputModesChangeHandlers. */
    protected List<OutputModesChangeHandler> outHandlers;

    /** The collection to be displayed. */
    protected Collection collection;

    /** The artifact that handles the parameterization. */
    protected Artifact artifact;

    protected TabSet tabs;

    /** The output tab. */
    protected Map<String, OutputTab> outputTabs;

    /** The layout. */
    protected Layout layout;

    /** Layout to show spinning wheel of joy. */
    protected VLayout lockScreen;

    protected int artifactsQueue;
    protected int recommendationQueue;
    protected Stack<Recommendation> newRecommendations;

    /** Values for fix analysis charts*/
    protected double currentKm;
    protected double minKm;
    protected double maxKm;
    protected double steps;

    /**
     * This constructor creates a new CollectionView that is used to display the
     * <i>collection</i>.
     */
    public CollectionView(FLYS flys) {
        this.flys          = flys;
        this.tabs          = new TabSet();
        this.outputTabs    = new HashMap<String, OutputTab>();
        this.handlers      = new ArrayList<CollectionChangeHandler>();
        this.outHandlers   = new ArrayList<OutputModesChangeHandler>();
        this.layout        = new VLayout();
        this.parameterList = new ParameterList(
            flys, this, messages.new_project());
        this.artifactsQueue      = 0;
        this.recommendationQueue = 0;
        this.newRecommendations  = new Stack<Recommendation>();

        this.currentKm = -1d;
        this.minKm = -1d;
        this.maxKm = -1d;
        this.steps = -1d;

        addCollectionChangeHandler(this);
        addCollectionChangeHandler(parameterList);
        addCollectionChangeHandler(flys);
        addOutputModesChangeHandler(this);
        addOutputModesChangeHandler(parameterList);
        addCloseClickHandler(this);

        parameterList.addParameterChangeHandler(this);

        init();
    }

    /**
     * @param collection The collection to be displayed.
     */
    public CollectionView(FLYS flys, Collection collection, Artifact artifact) {
        this.flys          = flys;
        this.artifact      = artifact;
        this.collection    = collection;
        this.tabs          = new TabSet();
        this.outputTabs    = new HashMap<String, OutputTab>();
        this.handlers      = new ArrayList<CollectionChangeHandler>();
        this.outHandlers   = new ArrayList<OutputModesChangeHandler>();
        this.layout        = new VLayout();

        this.currentKm = -1d;
        this.minKm = -1d;
        this.maxKm = -1d;
        this.steps = -1d;

        if (artifact != null) {
            this.parameterList = new ParameterList(
                flys,
                this,
                messages.getString(artifact.getName()),
                artifact);
        }
        else {
            this.parameterList = new ParameterList(
                flys, this, messages.new_project());
        }

        this.artifactsQueue     = 0;
        this.newRecommendations = new Stack<Recommendation>();

        addCollectionChangeHandler(this);
        addCollectionChangeHandler(parameterList);
        addCollectionChangeHandler(flys);
        addOutputModesChangeHandler(this);
        addOutputModesChangeHandler(parameterList);
        addCloseClickHandler(this);

        parameterList.addParameterChangeHandler(this);

        init();

        setCollection(collection);

        if (artifact != null) {
            setArtifact(artifact);
        }
    }


    /**
     * This method handles the initial layout stuff.
     */
    protected void init() {
        setWidth(1010);
        setHeight(700);

        setMaximized(true);

        layout.setWidth100();

        setCanDragReposition(true);
        setCanDragResize(true);
        setShowMaximizeButton(true);
        setKeepInParentRect(true);

        setTitle("");

        addItem(layout);

        layout.addMember(tabs);
        tabs.addTab(parameterList);
    }

    protected FLYS getFlys() {
        return flys;
    }


    /**
     * This method registers a new CollectionChangeHandler.
     *
     * @param handler The new CollectionChangeHandler.
     */
    @Override
    public void addCollectionChangeHandler(CollectionChangeHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    /**
     * This method registers a new OutputModesChangeHandler.
     *
     * @param handler The new OutputModesChangeHandler.
     */
    @Override
    public void addOutputModesChangeHandler(OutputModesChangeHandler handler) {
        if (handler != null) {
            outHandlers.add(handler);
        }
    }


    /**
     * This method calls the <code>onValueChange()</code> method of all
     * registered ValueChangeHanders.
     */
    protected void fireCollectionChangeEvent(
        Collection old, Collection newCol)
    {
        for (CollectionChangeHandler handler: handlers) {
            handler.onCollectionChange(new CollectionChangeEvent(old, newCol));
        }
    }


    protected void fireOutputModesChangeEvent(OutputMode[] outputs) {
        if (collection == null) {
            return;
        }

        for (OutputModesChangeHandler handler: outHandlers) {
            handler.onOutputModesChange(new OutputModesChangeEvent(outputs));
        }
    }


    /** Disables input, grey out, show spinning wheel of joy. */
    public void lockUI() {
        lockScreen = ScreenLock.lockUI(layout, lockScreen);
    }


    /** Enable input, remove grey, remove spinning wheel of joy. */
    public void unlockUI() {
        ScreenLock.unlockUI(layout, lockScreen);
    }


    /**
     * This method returns true, if the Collection is new and no plugins has
     * been chosen.
     *
     * @return true, if the Collection is new.
     */
    public boolean isNew() {
        return collection.hasItems();
    }


    /**
     * Returns the artifact that is used for the parameterization.
     *
     * @return the artifact that is used for the parameterization.
     */
    public Artifact getArtifact() {
        return artifact;
    }


    public User getUser() {
        return getFlys().getCurrentUser();
    }


    /**
     * Set the current artifact that is the master of the parameterization.
     *
     * @param artifact The new artifact.
     */
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;

        onArtifactChanged(artifact);
    }


    public void onArtifactChanged(Artifact artifact) {
        artifactChanged();

        if (artifact.isInBackground()) {
            LoadingPanel p = new LoadingPanel(this, artifact);
            p.addStepBackHandler(parameterList);
        }
    }


    /**
     * Implements the onCollectionChange() method to do update the GUI after the
     * parameterization has changed.
     *
     * @param event The ParameterChangeEvent.
     */
    @Override
    public void onParameterChange(ParameterChangeEvent event) {
        GWT.log("CollectionView.onParameterChange");
        setArtifact(event.getNewValue());
    }


    protected void artifactChanged() {
        Collection c = getCollection();

        if (c != null) {
            loadCollection(c);
        }
        else {
            updateView();
        }
    }

    /**
     * Loads all information of a collection.
     * If 'recommendations' present, load these.
     * @param c the Collection
     */
    private void loadCollection(Collection c) {
        ArtifactDescription desc = getArtifact().getArtifactDescription();
        final Recommendation[] recom = desc.getRecommendations();
        Config config = Config.getInstance();
        String locale = config.getLocale();

        describeCollectionService.describe(c.identifier(), locale,
            new AsyncCallback<Collection>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not DESCRIBE collection.");
                    SC.warn(FLYS.getExceptionString(messages, caught));
                }

                @Override
                public void onSuccess(Collection newCollection) {
                    GWT.log("Successfully DESCRIBED collection.");
                    boolean loaded = true;
                    for (Recommendation r: recom) {
                        if(!newCollection.loadedRecommendation(r)) {
                            loaded = false;
                        }
                    }
                    if  (!loaded) {
                        loadRecommendedArtifacts(recom);
                    }
                    else {
                        setCollection(newCollection);
                    }
                }
            }
        );
    }


    /**
     * Returns the collection of displayed by this view.
     *
     * @return the collection of this view.
     */
    public Collection getCollection() {
        return collection;
    }


    protected void setCollection(Collection collection) {
        setCollection(collection, false);
    }


    /**
     * Set the current collection.
     *
     * @param collection The new collection.
     * @param suppress Whether to fire a collectionchangeevent.
     */
    protected void setCollection(Collection collection, boolean suppress) {
        if (collection != null && this.collection == null) {
            flys.getWorkspace().addView(collection.identifier(), this);
        }

        Collection tmp  = this.collection;
        this.collection = collection;

        setTitle(collection.getDisplayName());

        if (!suppress) {
            fireCollectionChangeEvent(tmp, this.collection);
        }
    }


    @Override
    public void onCollectionChange(CollectionChangeEvent event) {
        if (artifactsQueue > 0) {
            GWT.log("Do not update UI because we are still loading Artifacts.");
            return;
        }

        Collection newCol = event.getNewValue();

        Map<String, OutputMode> outs = newCol.getOutputModes();

        Set<String>  keys     = outs.keySet();
        OutputMode[] prepared = new OutputMode[outs.size()];

        int idx = 0;
        for (String outname: keys) {
            prepared[idx++] = outs.get(outname);
        }

        fireOutputModesChangeEvent(prepared);

        updateView();
    }


    @Override
    public void onOutputModesChange(OutputModesChangeEvent event) {
        clearOutputTabs();
        OutputMode[] outs = event.getOutputModes();

        if (outs == null) {
            return;
        }

        boolean hasCSV = false;

        for (OutputMode out: outs) {
            addOutputTab(out.getName(), out);

            if (out instanceof ExportMode) {
                ExportMode export = (ExportMode) out;

                if (export.getFacet("csv") != null) {
                    hasCSV = true;
                }
            }
        }

        if (!hasCSV) {
            parameterList.removeTable();
        }
    }


    /**
     * Adds a new tab for the OutputMode <i>out</i>.
     *
     * @param name The name and title of the output.
     */
    protected void addOutputTab(String name, OutputMode out) {
        if (out instanceof ExportMode) {
            ExportMode export = (ExportMode) out;

            if (export.getFacet("csv") != null && !parameterList.hasTable()) {
                TableDataPanel p = new TableDataPanel();
                p.setUuid(collection.identifier());
                p.setName(out.getName());
                parameterList.setTable(p);
            }

            return;
        }

        if (out instanceof ReportMode) {
            // we don't want to display report modes at all
            return;
        }

        GWT.log("Add new output tab for '" + name + "'");

        String title  = messages.getString(name);
        OutputTab tab = out.createOutputTab(title, getCollection(), this);

        if (tab != null) {
            outputTabs.put(name, tab);
        }
    }


    /**
     * Removes all output mode tabs from tab bar.
     */
    protected void clearOutputTabs() {
        GWT.log("Clear OutputTabs.");

        int num = tabs.getNumTabs();

        for (int i = num-1; i >= 1; i--) {
            tabs.removeTab(i);
        }

        outputTabs.clear();
    }


    /**
     * Update the view (refresh the list of old and current data).
     */
    protected void updateView() {
        GWT.log("CollectionView.updateView()");
        updateOutputTabs();
    }


    /**
     * This method is used to update the tabs to show specific output modes.
     */
    protected void updateOutputTabs() {
        GWT.log("Update output tabs.");
        if (outputTabs != null) {
            Set<String> keys = outputTabs.keySet();

            for (String key: keys) {
                tabs.addTab(outputTabs.get(key));
            }
        }
    }


    @Override
    public void onCloseClick(CloseClickEvent event) {
        if (collection != null) {
            if(artifact != null) {
                flys.closeProject(collection.identifier());
            }
            else {
                flys.getProjectList().deleteCollection(collection);
            }
        }
        else {
            hide();
            destroy();
        }
    }


    public void addArtifactToCollection(Artifact artifact) {
        Config config               = Config.getInstance();
        final String locale         = config.getLocale();
        final Collection collection = getCollection();

        GWT.log("CollectionView.addArtifactToCollection " + collection);

        if (collection != null) {
            addArtifactService.add(
                collection, artifact, locale,
                new AsyncCallback<Collection>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("An error occured while adding artifact.");
                        SC.warn(FLYS.getExceptionString(messages, caught));
                    }

                    @Override
                    public void onSuccess(Collection newCollection) {
                        GWT.log("Successfully added artifacts.");
                        setCollection(newCollection);
                    }
                }
            );
        }
        else {
            // Create new collection and add artifact.
            final Artifact art = artifact;
            createCollectionService.create(
                locale,
                flys.getCurrentUser().identifier(),
                new AsyncCallback<Collection>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not create the new collection.");
                        SC.warn(FLYS.getExceptionString(messages, caught));
                    }

                    @Override
                    public void onSuccess(Collection collection) {
                        GWT.log("Successfully created a new collection.");
                        addArtifactService.add(
                            collection, art, locale,
                            new AsyncCallback<Collection>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    GWT.log("An error occured while "
                                        + "adding artifact.");
                                    SC.warn(FLYS.getExceptionString(
                                            messages, caught));
                                }

                                @Override
                                public void onSuccess(
                                    Collection newCollection) {
                                    GWT.log("Successfully added artifacts.");
                                    setCollection(newCollection);
                                }
                            }
                        );
                    }
                }
            );
        }
    }


    protected void addRecommendationsToCollection() {
        Config config               = Config.getInstance();
        final String locale         = config.getLocale();
        final Collection collection = getCollection();

        collection.addRecommendations(newRecommendations);

        updater.update(collection, locale,
            new AsyncCallback<Collection>() {
                @Override
                public void onFailure(Throwable caught) {
                    newRecommendations.removeAllElements();
                    setCollection(collection);

                    GWT.log("An error occured while saving recommendations.");
                    SC.warn(FLYS.getExceptionString(messages, caught));
                }

                @Override
                public void onSuccess(Collection newCol) {
                    GWT.log("Successfully saved recommendations.");
                    newRecommendations.removeAllElements();
                    setCollection(newCol);
                }
            }
        );
    }


    protected void loadRecommendedArtifacts(Recommendation[] recommendations) {
        Config config               = Config.getInstance();
        final String locale         = config.getLocale();
        final Collection collection = getCollection();

        Artifact masterArtifact = getArtifact();

        if (recommendations == null) {
            GWT.log("WARNING: Currently no recommendations.");
            return;
        }

        for (final Recommendation recommendation: recommendations) {
            if (collection.loadedRecommendation(recommendation)) {
                continue;
            }
            newRecommendations.push(recommendation);

            // XXX: UGLY! If no reference artifact given use uuid of
            //      current artifact as reference.
            if (recommendation.getMasterArtifact() == null) {
                recommendation.setMasterArtifact(masterArtifact.getUuid());
            }

        }

        loadArtifactService.loadMany(
            collection,
            recommendations,
            null,
            locale,
            new AsyncCallback<Artifact[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error loading recommendations: " +
                        caught.getMessage());
                    SC.warn(FLYS.getExceptionString(messages, caught));
                }

                @Override
                public void onSuccess(Artifact[] artifacts) {
                    GWT.log("Loaded artifacts: " + artifacts.length);
                    addRecommendationsToCollection();
                }
        });
    }


    public void registerTabHandler(TabSelectedHandler tse) {
        tabs.addTabSelectedHandler(tse);
    }


    public void setCurrentKm(double currentKm) {
        this.currentKm = currentKm;
    }

    public double getCurrentKm() {
        return this.currentKm;
    }

    public void setMinKm(double km) {
        this.minKm = km;
    }

    public double getMinKm() {
        return this.minKm;
    }

    public void setMaxKm(double km) {
        this.maxKm = km;
    }

    public double getMaxKm() {
        return this.maxKm;
    }

    public void setSteps(double step) {
        this.steps = step;
    }

    public double getSteps() {
        return this.steps;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
