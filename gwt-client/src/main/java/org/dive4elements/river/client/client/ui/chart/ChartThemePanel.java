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
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.FeedServiceAsync;
import org.dive4elements.river.client.client.services.LoadArtifactService;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.ThemePanel;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DefaultArtifact;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.FacetRecord;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.ThemeList;


/**
 * ThemePanel on the left in CollectionView.
 * Contains control widgets for "themes", which are plotted
 * in a diagram (chart).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartThemePanel extends ThemePanel {
    /** Artifact Clone/Creation service. */
    protected LoadArtifactServiceAsync loadService =
                GWT.create(LoadArtifactService.class);

    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public static final String GRID_FIELD_ACTIVE  = "active";
    public static final String GRID_FIELD_NAME    = "name";
    public static final String GRID_FIELD_ACTIONS = "actions";

    FeedServiceAsync feedService = GWT.create(
        org.dive4elements.river.client.client.services.FeedService.class);


    /** Constructor for a ChartThemePanel. */
    public ChartThemePanel(
        OutputMode mode,
        CollectionView view
    ) {
        super(mode, view);

        initGrid();
        initLayout();

        updateGrid();
    }


    /** Creates Layout with theme list and navigation bar inside. */
    protected VLayout createLayout() {
        VLayout layout = new VLayout();
        layout.setWidth100();
        layout.setHeight100();

        layout.addMember(list);
        layout.addMember(navigation);

        return layout;
    }


    /**
     * Initializes the layout of this panel.
     */
    protected void initLayout() {
        setWidth100();
        setHeight100();

        addChild(createLayout());
    }


    /**
     * Initializes the components (columns) of the theme grid.
     */
    protected void initGrid() {
        list.setCanEdit(true);
        list.setCanSort(false);
        list.setShowRecordComponents(false);
        list.setShowRecordComponentsByCell(true);
        list.setShowHeader(true);
        list.setShowHeaderContextMenu(false);
        list.setWidth100();
        list.setHeight100();

        list.addEditCompleteHandler(this);

        ListGridField active = new ListGridField(GRID_FIELD_ACTIVE, " ", 20);
        active.setType(ListGridFieldType.BOOLEAN);

        ListGridField name = new ListGridField(
            GRID_FIELD_NAME, MSG.chart_themepanel_header_themes());
        name.setType(ListGridFieldType.TEXT);

        list.setFields(active, name);
    }


    /** Set theme active/inactive. */
    @Override
    public void activateTheme(Theme theme, boolean active) {
        theme.setActive(active ? 1 : 0);
    }


    /** Returns name of longitudinal section area facets. */
    protected String getAreaFacetName() {
        return "longitudinal_section.area";
    }


    /** Create the DataProvider ('Blackboard') key for a theme. */
    public static String areaKey(Theme theme) {
        return theme.getArtifact() + ":" + theme.getFacet() + ":"
            + theme.getIndex();
    }


    /**
     * Tell an area artifact where to get the upper and lower curve from.
     * @param artifact UUID of area-artifact.
     */
    public void feedTellArea(
        final String artifact,
        Theme under,
        Theme over,
        boolean between
    ) {
        Data[] feedData;

        if (over != null && under != null) {
            feedData = new Data[] {
                DefaultData.createSimpleStringData("area.curve_under",
                    areaKey(under)),
                DefaultData.createSimpleStringData("area.curve_over",
                    areaKey(over)),
                DefaultData.createSimpleStringData("area.name",
                    over.getDescription() + " / " + under.getDescription()),
                DefaultData.createSimpleStringData("area.facet",
                    getAreaFacetName()),
                DefaultData.createSimpleStringData("area.between",
                    (between)? "true" : "false")
            };
            GWT.log("Have 'over' and 'under' curve");
        }
        else if (over == null && under != null) {
            feedData = new Data[] {
                DefaultData.createSimpleStringData("area.curve_under",
                    areaKey(under)),
                DefaultData.createSimpleStringData("area.name",
                    under.getDescription() + " / " + MSG.getString("x_axis")),
                DefaultData.createSimpleStringData("area.facet",
                    getAreaFacetName()),
                DefaultData.createSimpleStringData("area.between",
                    (between)? "true" : "false")
            };
            GWT.log("Have 'under' curve only");
        }
        else if (over != null && under == null) {
            feedData = new Data[] {
                DefaultData.createSimpleStringData("area.curve_over",
                    areaKey(over)),
                DefaultData.createSimpleStringData("area.name",
                    MSG.getString("x_axis") + " / " + over.getDescription()),
                DefaultData.createSimpleStringData("area.facet",
                    getAreaFacetName()),
                DefaultData.createSimpleStringData("area.between",
                    (between)? "true" : "false")
            };
            GWT.log("Have 'over' curve only");
        }
        else {
            GWT.log("Missing Data for area painting.");
            return;
        }

        feedService.feed(
            Config.getInstance().getLocale(),
            new DefaultArtifact(artifact, "TODO:hash"),
            feedData,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not feed artifact (" + artifact
                            + ") with area info: " + caught.getMessage());
                    SC.warn(MSG.getString(caught.getMessage()));
                    enable();
                }
                @Override
                public void onSuccess(Artifact fartifact) {
                    GWT.log("Successfully set area params to " + artifact);
                    requestRedraw();
                    updateCollection();
                    updateGrid();
                    enable();
                }
            });
    }


    /**
     * Create and parameterize a new area artifact.
     * @param under
     * @param over if null, against axis.
     * @param between if true, ignore under/over order.
     */
    public void createAreaArtifact(
        final Theme   over,
        final Theme   under,
        final boolean between
    ) {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        Recommendation area = new Recommendation(
            "area",
            "",
            "",
            null);

        // Set target out dynamically.
        area.setTargetOut(getMode().getName());

        Recommendation[] recommendations = new Recommendation[] {area};

        loadService.loadMany(
            this.getCollection(),
            recommendations,
            null, //use individual factories.
            locale,
            new AsyncCallback<Artifact[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failed, no area artifact: " + caught.getMessage());
                    enable();
                    // TODO i18n
                    SC.warn("Failed, no area artifact: " + caught.getMessage());
                }
                @Override
                public void onSuccess(Artifact[] artifacts) {
                    GWT.log("Success, created area artifact: "
                        + artifacts[0].getUuid());
                    // Now, feed the artifact with the relevant data.
                    feedTellArea(artifacts[0].getUuid(), under, over, between);
                }
            }
        );
    }


    /**
     * Return true if two themes are canditates for an area being
     * rendered between them.
     * TODO join with canArea, generalize to allow easier modification
     *      in subclasses.
     */
    protected boolean areAreaCompatible(Theme a, Theme b) {
        if (a.equals(b)) {
            return false;
        }
        if (a.getFacet().equals("w_differences") &&
            b.getFacet().equals("w_differences")) {
            return true;
        }
        if (a.getFacet().equals("longitudinal_section.w") ||
            a.getFacet().equals("other.wqkms.w") ||
            a.getFacet().equals("other.wqkms") ||
            a.getFacet().equals("discharge_longitudinal_section.w") ||
            a.getFacet().equals("discharge_longitudinal_section.c") ||
            a.getFacet().equals("other.wkms")) {
            return b.getFacet().equals("longitudinal_section.w")
                || b.getFacet().equals("other.wqkms")
                || b.getFacet().equals("other.wqkms.w")
                || b.getFacet().equals("discharge_longitudinal_section.w")
                || b.getFacet().equals("discharge_longitudinal_section.c")
                || b.getFacet().equals("other.wkms");
        }
        else if (a.getFacet().equals("longitudinal_section.q") ||
                 a.getFacet().equals("discharge_longitudinal_section.q") ||
                 a.getFacet().equals("other.wqkms.q")) {
            return b.getFacet().equals("longitudinal_section.q")
                    || b.getFacet().equals("discharge_longitudinal_section.q")
                    || b.getFacet().equals("other.wqkms.q");
        }
        return false;
    }


    /**
     * True if context menu should contain 'create area' submenu on
     * this theme.
     */
    protected boolean canArea(Theme a) {
        return a.getFacet().equals("longitudinal_section.q")
            || a.getFacet().equals("longitudinal_section.w")
            || a.getFacet().equals("discharge_longitudinal_section.w")
            || a.getFacet().equals("discharge_longitudinal_section.q")
            || a.getFacet().equals("discharge_longitudinal_section.c")
            || a.getFacet().startsWith("other.wqkms")
            || a.getFacet().equals("other.wkms")
            || a.getFacet().equals("w_differences");
    }


    /** Attach menu/item to open editor for Manual Points. */
    protected void attachManualPointsMenu(Menu menu) {
        menu.addItem(createSeparator());
        MenuItem editManualPoints = new MenuItem(MSG.editpoints());

        editManualPoints.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(MenuItemClickEvent evt) {
                    if(mode.getName().equals("historical_discharge")) {
                        new ManualDatePointsEditor(view.getCollection(),
                            redrawRequestHandlers.get(0),
                            mode.getName()).show();
                    }
                    else {
                        new ManualPointsEditor(view.getCollection(),
                            redrawRequestHandlers.get(0),
                            mode.getName()).show();
                    }
                }
            });
        menu.addItem(editManualPoints);
    }


    /**
     * Include area specific menu items and manual point editor, depending
     * on facet.
     */
    @Override
    protected Menu getSingleContextMenu(final ListGridRecord[] records) {
        Menu menu = super.getSingleContextMenu(records);

        final Theme facetTheme = ((FacetRecord)records[0]).getTheme();

        if (!canArea(facetTheme)) {
            if (facetTheme.getFacet().endsWith("manualpoints")) {
                attachManualPointsMenu(menu);
                return menu;
            }
            else {
                return menu;
            }
        }

        menu.addItem(createSeparator());

        MenuItem areaMenuItem = new MenuItem(MSG.chart_themepanel_new_area());
        Menu areaMenu         = new Menu();

        ThemeList themes = getThemeList();
        int nThemes      = themes.getThemeCount();

        // Create the "under..." submenu.
        MenuItem underMenuItem = new MenuItem(
            MSG.chart_themepanel_area_under());
        Menu underMenu = new Menu();
        for (int i = 0; i < nThemes; i++)  {
            final Theme theme = themes.getThemeAt(i+1);

            if (theme.getVisible() == 0) {
                continue;
            }

            if (!areAreaCompatible(facetTheme, theme)) {
                continue;
            }

            MenuItem againster = new MenuItem(theme.getDescription());
            underMenu.addItem(againster);

            againster.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(MenuItemClickEvent evt) {
                    disable();
                    createAreaArtifact(theme, facetTheme, false);
                }
            });
        }

        // Create the "over..." submenu.
        MenuItem overMenuItem = new MenuItem(MSG.chart_themepanel_area_over());
        Menu overMenu = new Menu();
        for (int i = 0; i < nThemes; i++)  {
            final Theme theme = themes.getThemeAt(i+1);
            if (theme.getVisible() == 0) {
                continue;
            }
            if (!areAreaCompatible(facetTheme, theme)) {
                continue;
            }
            MenuItem againster = new MenuItem(theme.getDescription());
            overMenu.addItem(againster);

            againster.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(MenuItemClickEvent evt) {
                    disable();
                    createAreaArtifact(facetTheme, theme, false);
                }
            });
        }
        overMenu.addItem(createSeparator());
        MenuItem againstAxis = new MenuItem(MSG.getString("x_axis"));
        againstAxis.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                disable();
                createAreaArtifact(null, facetTheme, false);
            }
        });
        overMenu.addItem(againstAxis);

        // Create the "between..." submenu.
        MenuItem betweenMenuItem = new MenuItem(
            MSG.chart_themepanel_area_between());
        Menu betweenMenu = new Menu();
        for (int i = 0; i < nThemes; i++)  {
            final Theme theme = themes.getThemeAt(i+1);
            if (theme.getVisible() == 0) {
                continue;
            }
            if (!areAreaCompatible(facetTheme, theme)) {
                continue;
            }
            MenuItem againster = new MenuItem(theme.getDescription());
            betweenMenu.addItem(againster);

            againster.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(MenuItemClickEvent evt) {
                    disable();
                    createAreaArtifact(facetTheme, theme, true);
                }
            });
        }
        betweenMenu.addItem(createSeparator());
        betweenMenu.addItem(againstAxis);

        overMenuItem.setSubmenu(overMenu);
        underMenuItem.setSubmenu(underMenu);
        betweenMenuItem.setSubmenu(betweenMenu);

        areaMenu.addItem(betweenMenuItem);
        areaMenu.addItem(overMenuItem);
        areaMenu.addItem(underMenuItem);
        areaMenu.addItem(createSeparator());
        MenuItem standAloneAgainstAxis = new MenuItem(
            MSG.getString("against_x_axis"));
        standAloneAgainstAxis.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                disable();
                createAreaArtifact(null, facetTheme, false);
            }
        });
        areaMenu.addItem(standAloneAgainstAxis);

        areaMenuItem.setSubmenu(areaMenu);
        menu.addItem(areaMenuItem);

        return menu;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
