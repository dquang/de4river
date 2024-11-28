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

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.AdvanceHandler;
import org.dive4elements.river.client.client.event.CollectionChangeEvent;
import org.dive4elements.river.client.client.event.CollectionChangeHandler;
import org.dive4elements.river.client.client.event.HasParameterChangeHandler;
import org.dive4elements.river.client.client.event.HasStepBackHandlers;
import org.dive4elements.river.client.client.event.HasStepForwardHandlers;
import org.dive4elements.river.client.client.event.OutputModesChangeEvent;
import org.dive4elements.river.client.client.event.OutputModesChangeHandler;
import org.dive4elements.river.client.client.event.ParameterChangeEvent;
import org.dive4elements.river.client.client.event.ParameterChangeHandler;
import org.dive4elements.river.client.client.event.StepBackEvent;
import org.dive4elements.river.client.client.event.StepBackHandler;
import org.dive4elements.river.client.client.event.StepForwardEvent;
import org.dive4elements.river.client.client.event.StepForwardHandler;
import org.dive4elements.river.client.client.services.AdvanceService;
import org.dive4elements.river.client.client.services.AdvanceServiceAsync;
import org.dive4elements.river.client.client.services.ArtifactService;
import org.dive4elements.river.client.client.services.ArtifactServiceAsync;
import org.dive4elements.river.client.client.services.ReportService;
import org.dive4elements.river.client.client.services.ReportServiceAsync;
import org.dive4elements.river.client.client.services.StepForwardService;
import org.dive4elements.river.client.client.services.StepForwardServiceAsync;
import org.dive4elements.river.client.client.ui.stationinfo.GaugePanel;
import org.dive4elements.river.client.client.ui.stationinfo.InfoPanel;
import org.dive4elements.river.client.client.ui.stationinfo.MeasurementStationPanel;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.ExportMode;
import org.dive4elements.river.client.shared.model.FixAnalysisArtifact;
import org.dive4elements.river.client.shared.model.MINFOArtifact;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.ReportMode;
import org.dive4elements.river.client.shared.model.River;
import org.dive4elements.river.client.shared.model.WINFOArtifact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class ParameterList
extends      Tab
implements   StepBackHandler, StepForwardHandler, ParameterChangeHandler,
             HasParameterChangeHandler, CollectionChangeHandler,
             OutputModesChangeHandler, AdvanceHandler
{
    private static final long serialVersionUID = 5204784727239299980L;

    public static final String STYLENAME_OLD_PARAMETERS = "oldParameters";

    /** The message class that provides i18n strings. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected ArtifactServiceAsync artifactService =
        GWT.create(ArtifactService.class);

    /** The StepForwardService used to put data into an existing artifact. */
    protected StepForwardServiceAsync forwardService =
        GWT.create(StepForwardService.class);

    /** The StepForwardService used to put data into an existing artifact. */
    protected AdvanceServiceAsync advanceService =
        GWT.create(AdvanceService.class);


    protected ReportServiceAsync reportService =
        GWT.create(ReportService.class);


    /** The list of ParameterizationChangeHandler. */
    protected List<ParameterChangeHandler> parameterHandlers;

    protected FLYS flys;

    protected CollectionView cView;

    protected Artifact artifact;

    protected List<DataList> old;
    protected Map<String, Canvas> oldStorage;
    protected DataList   current;

    protected UIProvider uiProvider;

    protected VLayout topLayout;
    protected VLayout oldItems;
    protected VLayout currentItems;
    protected VLayout exportModes;
    protected VLayout report;
    protected VLayout helperPanel;
    protected VLayout tablePanel;
    protected InfoPanel infoPanel;
    protected Canvas  reportPanel;

    private SectionStack stack;

    public ParameterList(FLYS flys, CollectionView cView, String title) {
        super(title);

        this.cView = cView;
        this.flys  = flys;

        parameterHandlers = new ArrayList<ParameterChangeHandler>();
        old               = new ArrayList<DataList>();
        oldStorage        = new TreeMap<String, Canvas>();
        topLayout         = new VLayout();
        oldItems          = new VLayout();
        currentItems      = new VLayout();
        exportModes       = new VLayout();
        report            = new VLayout();

        addParameterChangeHandler(this);

        init();
    }


    public ParameterList(
        FLYS           flys,
        CollectionView cView,
        String         title,
        Artifact       artifact)
    {
        super(title);

        this.cView    = cView;
        this.flys     = flys;
        this.artifact = artifact;

        parameterHandlers = new ArrayList<ParameterChangeHandler>();
        old               = new ArrayList<DataList>();
        oldStorage        = new TreeMap<String, Canvas>();
        topLayout         = new VLayout();
        oldItems          = new VLayout();
        currentItems      = new VLayout();
        exportModes       = new VLayout();
        report            = new VLayout();

        init();

        addParameterChangeHandler(this);

        setArtifact(artifact, false);
    }


    protected void init() {
        HLayout rootLayout = new HLayout();
        rootLayout.setMembersMargin(20);

        VLayout left = new VLayout();

        if (old == null || old.size() == 0) {
            oldItems.setHeight(1);
        }

        oldItems.setMembersMargin(10);
        oldItems.setStyleName(STYLENAME_OLD_PARAMETERS);
        currentItems.setAlign(VerticalAlignment.TOP);

        left.setMembersMargin(20);
        left.setWidth(300);

        left.addMember(oldItems);
        left.addMember(currentItems);
        left.addMember(exportModes);
        left.addMember(report);

        reportPanel = new Canvas();
        reportPanel.setHeight("*");
        report.addMember(reportPanel);

        rootLayout.addMember(left);
        rootLayout.addMember(createSectionStack());

        topLayout.addMember(rootLayout);
        if (artifact == null) {
            Canvas moduleSelection = renderNew();
            moduleSelection.setLayoutAlign(VerticalAlignment.TOP);
            currentItems.addMember(moduleSelection);
        }

        setPane(topLayout);
    }


    protected SectionStack createSectionStack() {
        stack = new SectionStack();
        stack.setHeight100();
        stack.setCanResizeSections(true);
        stack.setVisibilityMode(VisibilityMode.MULTIPLE);
        stack.setOverflow(Overflow.SCROLL);

        // This canvas is used to render helper widgets.
        final SectionStackSection helperSection = new SectionStackSection();
        helperSection.setExpanded(false);
        helperSection.setTitle(MSG.helperPanelTitle());
        helperPanel = new VLayout() {
            @Override
            public void addMember(Canvas component) {
                super.addMember(component);
                stack.expandSection(helperSection.getID());
            }

            @Override
            public void removeMembers(Canvas[] components) {
                super.removeMembers(components);
                helperSection.setExpanded(false);
            }
        };
        helperPanel.setWidth100();
        helperPanel.setHeight100();
        helperSection.setItems(helperPanel);

        // This canvas is used to render calculation results.
        final SectionStackSection tableSection = new SectionStackSection();
        tableSection.setExpanded(false);
        tableSection.setTitle(MSG.calcTableTitle());
        tablePanel = new VLayout() {
            @Override
            public void addMember(Canvas component) {
                super.addMember(component);
                tableSection.setExpanded(true);
                if (stack.getSection(InfoPanel.SECTION_ID) != null) {
                    stack.getSection(InfoPanel.SECTION_ID).setExpanded(false);
                }
            }

            @Override
            public void removeMembers(Canvas[] components) {
                super.removeMembers(components);
                tableSection.setExpanded(false);
            }
        };
        tablePanel.setHeight100();
        tablePanel.setWidth100();
        tableSection.setItems(tablePanel);

        stack.setSections(helperSection, tableSection);

        return stack;
    }


    /** Sets and forwards artifact. */
    protected void setArtifact(Artifact artifact) {
        setArtifact(artifact, true);
    }


    protected void setArtifact(Artifact artifact, boolean forward) {
        Artifact tmp  = this.artifact;
        this.artifact = artifact;

        if (forward) {
            fireParameterChangeEvent(
                tmp, this.artifact, ParameterChangeEvent.Type.FORWARD);
        }
        else {
            fireParameterChangeEvent(
                tmp, this.artifact, ParameterChangeEvent.Type.BACK);
        }
    }


    /**
     * This method registers a new ParameterChangeHandler.
     *
     * @param handler The new ParameterChangeHandler.
     */
    @Override
    public void addParameterChangeHandler(ParameterChangeHandler handler) {
        if (handler != null) {
            parameterHandlers.add(handler);
        }
    }


    /**
     * This method calls the <code>onParameterChange()</code> method of all
     * registered ParameterChangeHandler.
     */
    protected void fireParameterChangeEvent(
        Artifact old,
        Artifact newArt,
        ParameterChangeEvent.Type type)
    {
        ParameterChangeEvent e = new ParameterChangeEvent(old, newArt, type);

        for (ParameterChangeHandler handler: parameterHandlers) {
            handler.onParameterChange(e);
        }
    }


    /**
     * This method creates a Canvas displaying the plugins of FLYS combined with
     * a widget to select a river.
     *
     * @return a Canvas that displays the supported plugins and rivers of FLYS.
     */
    protected Canvas renderNew() {
        River[] rivers   = flys.getRivers();
        DataItem[] items = new DataItem[rivers.length];

        int i = 0;
        for (River river: rivers) {
            String name = river.getName();
            String mUuid = river.getModelUuid();
            items[i++]  = new DefaultDataItem(name, null, mUuid);
        }

        Data data = new DefaultData(
            "river",
            MSG.river_selection(),
            null,
            items);

        LinkSelection widget           = new LinkSelection();
        HasStepForwardHandlers handler = widget;

        widget.setContainer(helperPanel);

        handler.addStepForwardHandler(new StepForwardHandler() {
            private static final long serialVersionUID = -6210719844707004860L;

            @Override
            public void onStepForward(StepForwardEvent event) {
                lockUI();
                Data[] data = event.getData();

                DataItem[] moduleItems = data[0].getItems();
                DataItem[] riversItems = data[1].getItems();

                String module = moduleItems[0].getStringValue();
                String river  = riversItems[0].getStringValue();

                if (module == null || river == null) {
                    GWT.log("ParameterList.renderNew(): module == null "
                        + "|| river == null");
                    unlockUI();
                    return;
                }

                String newTitle = MSG.getString(module);
                setTitle(newTitle);

                Config config       = Config.getInstance();
                final String locale = config.getLocale();

                final Data[] feedData = new Data[] { data[1] };

                artifactService.create(
                    locale, module.toLowerCase(), null,
                    new AsyncCallback<Artifact>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            unlockUI();
                            GWT.log("Could not create the new artifact.");
                            SC.warn(FLYS.getExceptionString(MSG, caught));
                        }

                        @Override
                        public void onSuccess(Artifact artifact) {
                            GWT.log("Successfully created a new artifact.");

                            forwardService.go(locale, artifact, feedData,
                            new AsyncCallback<Artifact>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    unlockUI();
                                    GWT.log("Could not feed the artifact.");
                                    SC.warn(caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Artifact artifact) {
                                    GWT.log("Successfully feed the artifact.");
                                    old.clear();
                                    cView.addArtifactToCollection(artifact);
                                    setArtifact(artifact);
                                    unlockUI();
                                }
                            });
                        }
                });
            }
        });

        DataList list = new DataList();
        list.add(data);

        return widget.create(list);
    }


    protected void clearOldData() {
        old.clear();
    }


    public void addOldData(DataList old) {
        addOldData(old, true);
    }


    public void addOldData(DataList old, boolean redraw) {
        if (old != null) {
            this.old.add(old);
        }

        refreshOld(redraw);
    }


    public void addOldDatas(DataList[] old) {
        addOldDatas(old, true);
    }


    public void addOldDatas(DataList[] old, boolean redraw) {
        if (old != null && old.length > 0) {
            for (DataList o: old) {
                if (o == null) {
                    continue;
                }

                if (!exists(o)) {
                    GWT.log("Data '" + o.getLabel() + "' is new.");
                    addOldData(o, false);
                }
            }

            if (redraw) {
                addOldData(null, true);
            }

            return;
        }

        addOldData(null, true);
    }


    public boolean exists(DataList data) {
        if (data == null) {
            return false;
        }

        String stateName = data.getState();

        for (DataList o: old) {
            if (stateName.equals(o.getState())) {
                return true;
            }
        }

        return false;
    }


    public void setCurrentData(DataList current, UIProvider uiProvider) {
        this.current    = current;
        this.uiProvider = uiProvider;

        refreshCurrent();
    }


    public void refreshOld(boolean redrawAll) {
        if (redrawAll) {
            refreshAllOld();
        }
        else {
            DataList dataList = old.get(old.size()-1);
            String   state    = dataList.getState();

            if (oldStorage.get(state) == null) {
                String     provider   = dataList.getUIProvider();
                UIProvider uiprovider = UIProviderFactory.getProvider(
                    provider,
                    flys.getCurrentUser());
                ((HasStepBackHandlers) uiprovider).addStepBackHandler(this);

                Canvas c = uiprovider.createOld(dataList);
                if (c != null) {
                    oldStorage.put(dataList.getState(), c);
                    oldItems.addMember(c);
                }
            }
        }

        updateOldHeight();
    }


    protected void refreshAllOld() {
        List<String> not = new ArrayList<String>();

        for (DataList data: old) {
            String state = data.getState();

            Canvas c = oldStorage.get(state);

            if (c != null) {
                not.add(state);
            }
        }

        Map<String, Canvas> newOld = new TreeMap<String, Canvas>();

        Set<Map.Entry<String, Canvas>> entries = oldStorage.entrySet();
        for (Map.Entry<String, Canvas> entry: entries) {
            String state = entry.getKey();
            Canvas value = entry.getValue();

            if (not.indexOf(state) < 0) {
                oldItems.removeMember(value);
            }
            else {
                newOld.put(state, value);
            }
        }

        oldStorage = newOld;
    }


    protected void updateOldHeight() {
        int minHeight = oldItems.getMinHeight();
        if (minHeight <= 20) {
            oldItems.setHeight(20);
        }
        else {
            oldItems.setHeight(minHeight);
        }
    }


    /**
     * Refreshes the part displaying the data of the current state.
     * The UI is created using the UIProvider stored in the Data object.
     */
    public void refreshCurrent() {
        currentItems.removeMembers(currentItems.getMembers());

        if (current != null && uiProvider != null) {
            Canvas c = uiProvider.create(current);
            Canvas h = uiProvider.createHelpLink(current, null, this.flys);

            HLayout wrapper = new HLayout();
            wrapper.addMember(h);
            wrapper.addMember(c);

            currentItems.addMember(wrapper);
        }
        else if (uiProvider != null) {
            Canvas c = uiProvider.create(null);
            c.setLayoutAlign(VerticalAlignment.TOP);

            currentItems.addMember(c);
        }
        else {
            currentItems.setHeight(1);
        }

        Canvas[] members = currentItems.getMembers();
        if (members == null || members.length == 0) {
            currentItems.setHeight(1);
        }
        else {
            int height = 0;

            for (Canvas member: members) {
                height += member.getHeight();
            }

            currentItems.setHeight(height);
        }
    }


    /**
     * This method is called if the user clicks on the 'next' button to advance
     * to the next state.
     *
     * @param event The StepForwardEvent.
     */
    @Override
    public void onStepForward(StepForwardEvent event) {
        GWT.log("CollectionView - onStepForward()");
        lockUI();

        Config config = Config.getInstance();
        String locale = config.getLocale();

        forwardService.go(locale, artifact, event.getData(),
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    unlockUI();
                    GWT.log("Could not feed the artifact.");
                    SC.warn(FLYS.getExceptionString(MSG, caught));
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully feed the artifact.");
                    old.clear();

                    setArtifact(artifact, true);
                    unlockUI();
                }
        });
    }


    /**
     * This method is used to remove all old items from this list after the user
     * has clicked the step back button.
     *
     * @param e The StepBackEvent that holds the identifier of the target state.
     */
    @Override
    public void onStepBack(StepBackEvent e) {
        lockUI();
        final String target    = e.getTarget();

        Config config          = Config.getInstance();
        final String locale    = config.getLocale();

        advanceService.advance(locale, artifact, target,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    unlockUI();
                    GWT.log("Could not go back to '" + target + "'");
                    SC.warn(FLYS.getExceptionString(MSG, caught));
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully step back to '" + target + "'");

                    old.clear();

                    setArtifact(artifact, false);
                    unlockUI();
                }
            }
        );
    }


    @Override
    public void onAdvance(final String target) {
        Config config          = Config.getInstance();
        final String locale    = config.getLocale();

        advanceService.advance(locale, artifact, target,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not go to '" + target + "'");
                    SC.warn(FLYS.getExceptionString(MSG, caught));
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully advanced to '" + target + "'");

                    old.clear();

                    setArtifact(artifact, true);
                }
            }
        );
    }


    /**
     * Implements the onCollectionChange() method to do update the GUI after the
     * parameterization has changed.
     *
     * @param event The ParameterChangeEvent.
     */
    @Override
    public void onParameterChange(ParameterChangeEvent event) {
        GWT.log("ParameterList.onParameterChange");

        Canvas[] c = helperPanel.getMembers();
        if (c != null && c.length > 0) {
            helperPanel.removeMembers(c);
        }

        Artifact art             = event.getNewValue();
        ArtifactDescription desc = art.getArtifactDescription();

        DataList currentData = desc.getCurrentData();
        if (currentData != null) {
            // the user has to enter some attributes
            String uiProvider   = currentData.getUIProvider();
            UIProvider provider = UIProviderFactory.getProvider(
                uiProvider,
                flys.getCurrentUser());

            provider.setContainer(helperPanel);
            provider.setArtifact(art);
            provider.setCollection(cView.getCollection());
            provider.setParameterList(this);

            ((HasStepForwardHandlers) provider).addStepForwardHandler(this);
            ((HasStepBackHandlers) provider).addStepBackHandler(this);

            setCurrentData(currentData, provider);
        }
        else {
            String[] reachable = desc.getReachableStates();
            if (reachable != null && reachable.length > 0) {
                // We have reached a final state with the option to step to
                // further to a next state. But in the current state, no user
                // data is required.
                UIProvider ui = UIProviderFactory.getProvider("continue", null);
                ui.setArtifact(art);
                ui.setCollection(cView.getCollection());
                ui.setParameterList(this);

                ((ContinuePanel) ui).addAdvanceHandler(this);

                setCurrentData(null, ui);
            }
            else {
                // we have reached a final state with no more user input
                setCurrentData(null, null);
            }
        }
        if (art instanceof WINFOArtifact
                || art instanceof FixAnalysisArtifact) {
            createGaugePanel();
            renderInfo(desc.getRiver(), desc.getOldData());
        }
        else if (art instanceof MINFOArtifact) {
            createMeasurementStationPanel();
            renderInfo(desc.getRiver(), desc.getOldData());
        }
        else {
            removeInfoPanel();
        }

        addOldDatas(
            desc.getOldData(),
            event.getType() == ParameterChangeEvent.Type.BACK);
    }


    @Override
    public void onCollectionChange(CollectionChangeEvent event) {
        Collection                 c = event.getNewValue();
        Map<String, OutputMode> outs = c.getOutputModes();
        Set<String>             keys = outs.keySet();

        OutputMode[] outputs = new OutputMode[outs.size()];

        int idx = 0;
        for (String outname: keys) {
            outputs[idx++] = outs.get(outname);
        }

        updateExportModes(c, getExportModes(outputs));
        updateReportModes(c, getReportModes(outputs));
    }


    @Override
    public void onOutputModesChange(OutputModesChangeEvent event) {

        Collection c = cView.getCollection();

        if (c != null) {
            OutputMode [] outs = event.getOutputModes();
            updateExportModes(c, getExportModes(outs));
            updateReportModes(c, getReportModes(outs));
        }
    }


    protected List<ReportMode> getReportModes(OutputMode [] outs) {

        List<ReportMode> reports = new ArrayList<ReportMode>();

        if (outs == null || outs.length == 0) {
            return reports;
        }

        for (OutputMode out: outs) {
            if (out instanceof ReportMode) {
                reports.add((ReportMode)out);
            }
        }

        return reports;
    }


    protected List<ExportMode> getExportModes(OutputMode[] outs) {
        List<ExportMode> exports = new ArrayList<ExportMode>();

        if (outs == null || outs.length == 0) {
            return exports;
        }

        for (OutputMode out: outs) {
            if (out instanceof ExportMode) {
                exports.add((ExportMode) out);
            }
        }

        return exports;
    }


    protected void updateExportModes(Collection c, List<ExportMode> exports) {
        int num = exports != null ? exports.size() : 0;
        GWT.log("Update export modes: " + num);

        exportModes.removeMembers(exportModes.getMembers());

        if (exports.size() > 0) {
            exportModes.addMember(new ExportPanel(c, exports));
        }
        else {
            exportModes.setHeight(1);
        }
    }

    protected void updateReportModes(Collection c, List<ReportMode> reports) {
        int num = reports != null ? reports.size() : 0;
        GWT.log("Update report modes: " + num);

        if (num == 0) {
            reportPanel.setContents("");
            return;
        }

        Config config = Config.getInstance();
        String locale = config.getLocale();

        String cid = c.identifier();

        for (ReportMode report: reports) {
            GWT.log("report '" + report.toString() + "'");

            reportService.report(cid, locale, report.getName(),
                new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        SC.warn(FLYS.getExceptionString(MSG, caught));
                    }

                    @Override
                    public void onSuccess(String msg) {
                        setReportMessage(msg);
                    }
                });
        }
    }


    /** Sets content of reportPanel. */
    protected void setReportMessage(String msg) {
        GWT.log("returned from service: " + msg);
        if (msg == null) {
            msg = "";
        }
        reportPanel.setContents(msg);
    }


    /**
     * Adds a table to the parameterlist to show calculated data.
     *
     * @param table The table data panel.
     */
    public void setTable(TableDataPanel table) {
        removeTable();

        Canvas c = table.create();
        c.setHeight100();
        c.setWidth100();

        tablePanel.addMember(c);
    }


    public boolean hasTable() {
        Canvas[] members = tablePanel.getMembers();

        return members != null && members.length > 0;
    }


    /**
     * Removes the table from the parameter list.
     */
    public void removeTable() {
        Canvas[] members = tablePanel.getMembers();

        if (members != null && members.length > 0) {
            tablePanel.removeMembers(members);
        }
    }


    public void registerCollectionViewTabHandler(TabSelectedHandler tsh) {
        this.cView.registerTabHandler(tsh);
    }


    protected void lockUI() {
        cView.lockUI();
    }


    protected void unlockUI() {
        cView.unlockUI();
    }


    private void createGaugePanel() {
        GWT.log("ParameterList - createGaugePanel");
        if (infoPanel == null) {
            infoPanel = new GaugePanel(flys);
            infoPanel.setWidth100();
            infoPanel.setHeight100();
        }
    }

    private void createMeasurementStationPanel() {
        GWT.log("ParameterList - createMeasurementStationPanel");
        if (infoPanel == null) {
            infoPanel = new MeasurementStationPanel(flys);
            infoPanel.setWidth100();
            infoPanel.setHeight100();
        }
    }

    private void showInfoPanel() {
        GWT.log("ParameterList - showInfoPanel");

        /* Don't add InfoPanel twice */
        SectionStackSection info = stack.getSection(InfoPanel.SECTION_ID);
        if (info == null) {
            info = new SectionStackSection();
            info.setTitle(infoPanel.getSectionTitle());
            info.setID(InfoPanel.SECTION_ID);
            info.setName(InfoPanel.SECTION_ID);
            info.setItems(infoPanel);
            stack.addSection(info, 0);
        }

        info.setExpanded(true);
    }

    private void hideInfoPanel() {
        GWT.log("ParameterList - hideInfoPanel");

        if (infoPanel != null) {
            infoPanel.hide();
        }
    }

    private void removeInfoPanel() {
        GWT.log("ParameterList - removeInfoPanel");
        SectionStackSection exists = stack.getSection(InfoPanel.SECTION_ID);
        if (exists != null) {
            stack.removeSection(InfoPanel.SECTION_ID);
        }
    }


    private void renderInfo(String river, DataList[] data) {
        GWT.log("ParameterList - renderInfo");

        if (river != null) {
            showInfoPanel();
            infoPanel.setRiver(river);
            infoPanel.setData(data);
        }
        else {
            GWT.log("ParameterList - renderInfo no river");
            hideInfoPanel();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
