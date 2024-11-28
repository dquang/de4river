/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.services.CrossSectionKMServiceAsync;
import org.dive4elements.river.client.client.services.LoadArtifactService;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.widgets.KMSpinner;
import org.dive4elements.river.client.client.widgets.KMSpinnerChangeListener;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DefaultArtifact;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.FacetRecord;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.ThemeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * ThemePanel much like ChartThemePanel, but shows an "Actions" column,
 * needed for interaction in the CrossSection Charts and a selector to
 * declare which cross section profile is "master" (waterlevels refer to the
 * chosen kilometer of that cross section profile).
 * Also can show 'area creation' context menus.
 */
public class CrossSectionChartThemePanel
extends ChartThemePanel
implements KMSpinnerChangeListener
{
    /** Artifact Clone/Creation service. */
    protected LoadArtifactServiceAsync loadService =
                GWT.create(LoadArtifactService.class);

    /** Service to query measurement points of cross sections. */
    CrossSectionKMServiceAsync kmService = GWT.create(
        org.dive4elements.river.client.client.services
        .CrossSectionKMService.class);

    /** UUID of the current "master" cross section. */
    protected String currentCSMasterUUID;

    /** The layout (used for visual active/inactive feedback). */
    protected VLayout layout;

    /** Data item name for CrossSections selected km. */
    protected static String CS_KM = "cross_section.km";

    /** Data item name for CrossSections reference ('master') flag. */
    protected static String CS_IS_MASTER = "cross_section.master?";

    /** List of cross-section themes through which is moved
     * through synchronously. */
    protected HashSet synchronCrossSectionThemes = new HashSet();

    /** Data for master artifact combobox.*/
    protected LinkedHashMap<String, String> masters;

    /** Combobox for master artifacts.*/
    protected SelectItem masterCb;


    /**
     * Trivial constructor.
     */
    public CrossSectionChartThemePanel(
        OutputMode mode,
        CollectionView view)
    {
        super(mode, view);
    }


    /** Create DefaultArtifact. */
    public static DefaultArtifact artifactReference(String uuid) {
        return new DefaultArtifact(uuid, "TODO:hash");
    }


    /** Access data of collection item of theme. */
    public static String dataOf(Theme theme, String dataItemName) {
        if (theme != null && theme.getCollectionItem() != null
            && theme.getCollectionItem().getData() != null
        ) {
            return theme.getCollectionItem().getData().get(dataItemName);
        }

        return null;
    }


    /**
     * Feed an artifact to let it know that it is master wrt cross-sections.
     * @param artifact uuid of an artifact.
     */
    public void feedTellMaster(final String artifact) {
        Data[] feedData = DefaultData.createSimpleStringDataArray(
            CS_IS_MASTER, "1");

        feedService.feed(
            Config.getInstance().getLocale(),
            artifactReference(artifact),
            feedData,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not feed artifact (" + artifact
                            + ") with master marker: " + caught.getMessage());
                    SC.warn(MSG.getString(caught.getMessage()));
                    enable();
                }
                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully injected master mark to " + artifact);
                    setCurrentCSMaster(artifact.getUuid());
                    requestRedraw();
                    enable();
                }
            });
    }


    /**
     * Sets currentCSMasterUUID.
     */
    public String findCurrentCSMaster() {
        ThemeList themeList = getThemeList();
        int count = getThemeList().getThemeCount();
        String firstCSUuid = null;

        for (int i = 1; i <= count; i++) {
            Theme theme = themeList.getThemeAt(i);
            String value = dataOf(theme, CS_IS_MASTER);

            if (value != null) {
                if (firstCSUuid == null) {
                    firstCSUuid = theme.getArtifact();
                }
                if (!value.equals("0")) {
                    setCurrentCSMaster(theme.getArtifact());
                    GWT.log("found a master: " + currentCSMasterUUID
                        + "/" + theme.getDescription());
                    return theme.getDescription();
                }
            }
        }
        // There is none selected. Take the first one.
        if (firstCSUuid != null) {
            // TODO better take the one closest to first km!
            // issue1157, query next/prev kms, select the one which is closest.
            setCurrentCSMaster(firstCSUuid);
            feedTellMaster(firstCSUuid);
        }
        return null;
    }


    /**
     * Create Layout, add a master selection box beneath.
     */
    @Override
    protected VLayout createLayout() {
        layout = super.createLayout();

        // Create "set master" combobox.
        masterCb = new SelectItem();

        masterCb.setTitle(MSG.chart_themepanel_set_master());
        masterCb.setType("comboBox");
        masters = getThemeList().toMapArtifactUUIDDescription("cross_section");
        masterCb.setValueMap(masters);

        final DynamicForm form = new DynamicForm();
        form.setWidth(200);
        form.setFields(masterCb);
        layout.addMember(form, 0);

        Config config       = Config.getInstance();
        final String locale = config.getLocale();
        findCurrentCSMaster();
        masterCb.setValue(getCurrentCSMaster());

        // Add Change Handler to first unset the old master and then set the
        // new master.
        masterCb.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String selectedItem   = (String) event.getValue();
                final String artifact = selectedItem;

                disable();

                // Tell current master that he is not master anymore.
                if (getCurrentCSMaster() != null) {
                    Data[] feedData = DefaultData.createSimpleStringDataArray(
                            CS_IS_MASTER, "0");
                    feedService.feed(
                        locale,
                        artifactReference(getCurrentCSMaster()),
                        feedData,
                        new AsyncCallback<Artifact>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("Could not un-master artifact ("
                                    + getCurrentCSMaster() + "): " +
                                    caught.getMessage());
                                SC.warn(MSG.getString(caught.getMessage()));
                                enable();
                            }
                            @Override
                            public void onSuccess(Artifact oldMaster) {
                                GWT.log("Successfully un-mastered artifact.");
                                feedTellMaster(artifact);
                            }
                        });
                }
                else {
                    feedTellMaster(artifact);
                }
            }
        });

        return layout;
    }


    /** Disable the UI (becomes gray, inresponsive to user input). */
    @Override
    public void disable() {
        this.layout.setDisabled(true);
    }


    /** DisDisable the UI (becomes ungray, responsive to user input). */
    @Override
    public void enable() {
        this.layout.setDisabled(false);
    }


    /**
     * Returns a double from the list that has the smallest distance to the
     * given to value. In case of multiple values with the same difference,
     * the last one is taken.
     * @param in possible return values.
     * @param to the value to be as close to as possible.
     * @param up if true, prefer numerically higher values in case of two
     *           values with equal distance to \param to.
     * @return value from in that is closest to to, NaN if none.
     */
    public static Double closest(Double[] in, double to, boolean up) {
        if (in == null || in.length == 0) {
            return Double.NaN;
        }
        Arrays.sort(in);
        GWT.log ("Closest match for " + (up ? "next" : "previous")
            + " value to: " + to + " candidates: " + Arrays.toString(in));
        if (up) {
            double max = in[in.length - 1];
            for (int i = 0; i < in.length; i++) {
                if (in[i] >= to || in[i] == max) {
                    return in[i];
                }
            }
        } else {
            double min = in[0];
            for (int i = in.length - 1; i >= 0; i--) {
                if (in[i] <= to || in[i] == min) {
                    return in[i];
                }
            }
        }
        GWT.log("Failed to find closest match");
        return Double.NaN;
    }


    /**
     * Feed artifacts with the km of the crosssection to display.
     * If its the selected master, also feed the collectionmaster.
     *
     * @param artifacts List of artifacts to feed.
     * @param kmD       The km to set.
     */
    public void sendFeed(final List<Artifact> artifacts, final double kmD) {
        Config config       = Config.getInstance();
        final String locale = config.getLocale();

        Data[] feedData =
            DefaultData.createSimpleStringDataArray(CS_KM,
                Double.valueOf(kmD).toString());

        disable();
        // TODO
        // The ones who do not have data for this km should not show line!
        feedService.feedMany(
            locale,
            artifacts,
            feedData,
            new AsyncCallback<List<Artifact>>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not feed many artifacts "
                        + caught.getMessage());
                    SC.warn(MSG.getString(caught.getMessage()));
                    enable();
                }
                @Override
                public void onSuccess(List<Artifact> artifact) {
                    GWT.log("Successfully fed many with km");
                    requestRedraw();
                    enable();
                }
        });
    }


    /**
     * Feed a single artifact with the km of the crosssection to display.
     * If its the selected master, also feed the collectionmaster.
     * @param artUUID The UUID of the artifact to feed.
     * @param kmD     The km to set.
     */
    public void sendFeed(final String artUUID, final double kmD) {
        Config config       = Config.getInstance();
        final String locale = config.getLocale();

        Data[] feedData =
            DefaultData.createSimpleStringDataArray(CS_KM,
                Double.valueOf(kmD).toString());

        disable();
        feedService.feed(
            locale,
            artifactReference(artUUID),
            feedData,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not feed artifact " + caught.getMessage());
                    SC.warn(MSG.getString(caught.getMessage()));
                    enable();
                }
                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully fed with km");
                    requestRedraw();
                    enable();
                }
        });
    }


    /** Remove the themes, also from the master (reference) select box. */
    @Override
    protected void removeThemes(final ListGridRecord[] records) {
        // TODO take care of what happens if that was the last
        // cross section and/or the cross section currently selected as master.
        for (ListGridRecord record: records) {
            FacetRecord facet = (FacetRecord) record;
            Theme theme = facet.getTheme();
            masters.remove(theme.getArtifact());
        }
        masterCb.setValueMap(masters);
        super.removeThemes(records);
    }


    /**
     * Callback for when a value has been accepted in the km-spinner
     * of a Cross-Section Profile theme.
     * @param item        The SpinnerItem which was manipulated.
     * @param enteredKm   The double-parsed value that has been entered.
     * @param facetRecord The underlying datastores record.
     * @param up          If true, numerically higher values are preferred if
     *                    two values in \param in are in the same distance to
     *                    \param to.
     */
    @Override
    public void spinnerValueEntered(KMSpinner spinner,
        final double enteredKm,
        final FacetRecord facetRecord,
        final boolean up
    ) {
        disable();
        Config config       = Config.getInstance();
        final String locale = config.getLocale();

        Map<Integer, Double> map = new HashMap<Integer,Double>();
        int _dbid = -1;
        try {
            _dbid = Integer.valueOf(facetRecord.getTheme()
                .getCollectionItem()
                .getData().get("cross_section.dbid"));
        }
        catch (NumberFormatException nfe) {
            GWT.log("Could not extract cross-section db id from data.");
        }
        final int dbid = _dbid;

        map.put(dbid, enteredKm);

        // Query the available cross section measurements.
        kmService.getCrossSectionKMs(locale, map, 2,
            new AsyncCallback<Map<Integer, Double[]>>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not get single km for "
                        + dbid + ": "+ caught.getMessage());
                    SC.warn(MSG.getString(caught.getMessage()));
                    updateCollection();
                    //updateGrid();
                    enable();
                }

                @Override
                public void onSuccess(Map<Integer, Double[]> obj) {
                    Double[] kms = obj.get(dbid);
                    Double closest =
                        CrossSectionChartThemePanel.closest(kms, enteredKm, up);
                    if (Double.isNaN(closest)) {
                        GWT.log("Failed to find a closest km. "
                            + "Staying on current km.");
                        updateCollection();
                        enable();
                        return;
                    }

                    GWT.log("Got single km close to "
                        + enteredKm + " for " + dbid + ", it is "
                        + closest);

                    // Do not set value, as it will trigger strange
                    // "javascript" bugs. /*item.setValue(closest);*/
                    if (synchronCrossSectionThemes.contains (themeHash
                        (facetRecord.getTheme()))) {
                        // Move all other synchrons
                        ThemeList themes = getThemeList();
                        int nThemes      = themes.getThemeCount();
                        List<Artifact> artifacts = new ArrayList<Artifact>();
                        for (int i = 0; i < nThemes; i++) {
                            final Theme theme = themes.getThemeAt(i+1);
                            if (theme.getFacet().equals("cross_section") &&
                                theme.getActive() == 1 &&
                                synchronCrossSectionThemes.contains(
                                    themeHash(theme))
                                ) {
                                artifacts.add(
                                    artifactReference(theme.getArtifact()));
                            }
                        }
                        sendFeed(artifacts, closest);
                    }
                    else {
                        sendFeed(facetRecord.getTheme().getArtifact(),
                             closest);
                    }
                }
            });
    }


    /**
     * Create and configure the Grid to display.
     * @return ListGrid with Themes and related controls inside.
     */
    @Override
    protected ListGrid createGrid() {
        final CrossSectionChartThemePanel parent = this;

        ListGrid list = new ListGrid() {
            @Override
            protected Canvas createRecordComponent(
                final ListGridRecord record,
                Integer colNum)
            {
                    // Only cross_section Facets display an action widget.
                    final FacetRecord facetRecord = (FacetRecord) record;
                    if (!facetRecord.getTheme().getFacet().equals(
                        "cross_section"))
                    {
                        return null;
                    }

                    String fieldName = this.getFieldName(colNum);

                    // Place KMSpinner in Grid with currently chosen km value.
                    if (fieldName.equals(GRID_FIELD_ACTIONS)) {
                        double currentValue =
                            Double.valueOf(
                                facetRecord.getTheme().getCollectionItem()
                                .getData().get(CS_KM));
                        KMSpinner kmSpinner = new KMSpinner(
                            currentValue, facetRecord);
                        kmSpinner.addChangeListener(parent);
                        return kmSpinner;
                    }
                    else {
                        return null;
                    }
                }
            };
        list.setCanResizeFields(true);
        list.setShowRecordComponents(true);
        list.setShowRecordComponentsByCell(true);
        list.setShowAllRecords(true);
        list.setShowHeaderContextMenu(false);
        list.setLeaveScrollbarGap(false);

        return list;
    }


    /**
     * Initializes the components (columns) of the theme grid.
     */
    @Override
    protected void initGrid() {
        list.setCanEdit(true);
        list.setCanSort(false);
        list.setShowRecordComponents(true);
        list.setShowRecordComponentsByCell(true);
        list.setShowHeader(true);
        list.setWidth100();
        list.setHeight100();

        list.addEditCompleteHandler(this);

        ListGridField active = new ListGridField(GRID_FIELD_ACTIVE, " ", 20);
        active.setType(ListGridFieldType.BOOLEAN);

        ListGridField name = new ListGridField(
            GRID_FIELD_NAME, MSG.chart_themepanel_header_themes());
        name.setType(ListGridFieldType.TEXT);

        ListGridField actions = new ListGridField(GRID_FIELD_ACTIONS,
             MSG.chart_themepanel_header_actions(), 100);

        list.setFields(active, name, actions);
    }


    /** Get Current Cross-section Masters uuid. */
    public String getCurrentCSMaster() {
        return currentCSMasterUUID;
    }


    /** Set Current Cross-section Masters uuid. */
    public void setCurrentCSMaster(String currentMasterUuid) {
        this.currentCSMasterUUID = currentMasterUuid;
    }


    /** Returns name of cross section area facets. */
    @Override
    protected String getAreaFacetName() {
        return "cross_section.area";
    }


    /**
     * Return true if two themes are canditates for an area being
     * rendered between them.
     * TODO join with canArea, generalize to allow easier modification
     *      in subclasses.
     */
    @Override
    protected boolean areAreaCompatible(Theme a, Theme b) {
        if (a.equals(b)) {
            return false;
        }
        return (a.getFacet().equals("cross_section")
                || a.getFacet().endsWith("line"))
            && (b.getFacet().equals("cross_section")
                || b.getFacet().endsWith("line"));
    }


    /**
     * True if context menu should contain 'create area' submenu on
     * this theme.
     */
    @Override
    protected boolean canArea(Theme a) {
        return a.getFacet().equals("cross_section")
            || a.getFacet().equals("cross_section_water_line")
            || a.getFacet().endsWith("line");
    }


    protected String themeHash(Theme theme) {
        return theme.getArtifact() + theme.getFacet() + theme.getIndex();
    }


    /**
     * Include synchron navigation item.
     */
    @Override
    protected Menu getSingleContextMenu(final ListGridRecord[] records) {
        Menu contextMenu = super.getSingleContextMenu(records);

        Theme facetTheme = ((FacetRecord)records[0]).getTheme();
        String item = facetTheme.getFacet();

        if (item.equals("cross_section")) {
            // Synchron checking.
            MenuItem synchronNavigationMenuItem = new MenuItem();
            final String themeHash = themeHash(facetTheme);
            if (synchronCrossSectionThemes.contains(themeHash)) {
                synchronNavigationMenuItem.setTitle(
                    MSG.chart_themepanel_asynchron());
                synchronNavigationMenuItem.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(MenuItemClickEvent evt) {
                        synchronCrossSectionThemes.remove (themeHash);
                    }
                });
            }
            else {
                synchronNavigationMenuItem.setTitle(
                    MSG.chart_themepanel_synchron());
                synchronNavigationMenuItem.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(MenuItemClickEvent evt) {
                        synchronCrossSectionThemes.add (themeHash);
                    }
                });
            }
            contextMenu.addItem(synchronNavigationMenuItem);
        }

        return contextMenu;
    }


    /**
     * This method is used to clear the current theme grid and add new updated
     * data.
     */
    @Override
    protected void updateGrid() {
        GWT.log("CrossSectionChartThemePanel.updateGrid");

        ListGridRecord[] selected = list.getSelectedRecords();

        clearGrid();

        ThemeList themeList = getThemeList();

        if (themeList == null) {
            GWT.log("ERROR: No theme list.");
            return;
        }

        int count = themeList.getThemeCount();

        for (int i = 1; i <= count; i++) {
            Theme theme = themeList.getThemeAt(i);

            if (theme == null) {
                continue;
            }

            if (theme.getFacet().equals("empty.facet")) {
                theme.setVisible(0);
            }

            if (theme.getVisible() == 0) {
                continue;
            }

            if (theme.getFacet().equals("cross_section")) {
                addToReferences(theme);
            }

            FacetRecord newRecord = createRecord(theme);
            addFacetRecord(newRecord);

            String newArtifact = theme.getArtifact();
            String newFacet    = theme.getFacet();
            int    newIndex    = theme.getIndex();

            for (ListGridRecord r: selected) {
                FacetRecord sel = (FacetRecord) r;
                Theme oldTheme  = sel.getTheme();

                if (oldTheme.getArtifact().equals(newArtifact)
                    && oldTheme.getFacet().equals(newFacet)
                    && oldTheme.getIndex() == newIndex) {
                    list.selectRecord(newRecord);
                }
            }
        }

        fireOutputParameterChanged();

    }


    /**
     * Adds a cross section theme to the master artifacts combobox and finds
     * a new master if necessary.
     *
     * @param theme The cross section theme.
     */
    protected void addToReferences(Theme theme) {
        if (theme.getVisible() != 0) {
            masters.put(theme.getArtifact(), theme.getDescription());
            masterCb.setValueMap(masters);
        }
        findCurrentCSMaster();
        if (masterCb.getSelectedRecord() == null) {
            masterCb.setValue(getCurrentCSMaster());
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
