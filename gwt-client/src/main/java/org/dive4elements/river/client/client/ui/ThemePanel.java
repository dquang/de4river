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

import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.RowContextClickEvent;
import com.smartgwt.client.widgets.grid.events.RowContextClickHandler;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.HasOutputParameterChangeHandlers;
import org.dive4elements.river.client.client.event.HasRedrawRequestHandlers;
import org.dive4elements.river.client.client.event.OnMoveEvent;
import org.dive4elements.river.client.client.event.OnMoveHandler;
import org.dive4elements.river.client.client.event.OutputParameterChangeEvent;
import org.dive4elements.river.client.client.event.OutputParameterChangeHandler;
import org.dive4elements.river.client.client.event.RedrawRequestEvent;
import org.dive4elements.river.client.client.event.RedrawRequestEvent.Type;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.services.CollectionAttributeService;
import org.dive4elements.river.client.client.services.CollectionAttributeServiceAsync;
import org.dive4elements.river.client.client.services.CollectionItemAttributeService;
import org.dive4elements.river.client.client.services.CollectionItemAttributeServiceAsync;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItemAttribute;
import org.dive4elements.river.client.shared.model.FacetRecord;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.ThemeList;

import java.util.ArrayList;
import java.util.List;

/**
 * ThemePanel on the left in CollectionView.
 * Contains control widgets for "themes", which are plotted in
 * a diagram (chart).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class ThemePanel
extends               Canvas
implements            OnMoveHandler,
                      EditCompleteHandler,
                      HasOutputParameterChangeHandlers,
                      HasRedrawRequestHandlers
{
    protected CollectionAttributeServiceAsync updater =
        GWT.create(CollectionAttributeService.class);

    /** The service used to get collection item attributes. */
    protected CollectionItemAttributeServiceAsync itemAttributeService =
        GWT.create(CollectionItemAttributeService.class);

    /** i18ner. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** List of OutParameterChangedHandler. */
    protected List<OutputParameterChangeHandler> outHandlers;

    /** List of ChartShallRedrawHandler. */
    protected List<RedrawRequestHandler> redrawRequestHandlers;

    protected OutputMode mode;

    protected ThemeNavigationPanel navigation;
    protected ListGrid list;

    /** The collection view*/
    protected CollectionView view;

    /**
     * Setup Grid, navigation bar.
     * @param collection Collection for which to show themes.
     */
    public ThemePanel(
        OutputMode mode,
        CollectionView view
    ) {
        this.mode       = mode;
        this.list       = createGrid();
        this.view       = view;
        list.addRowContextClickHandler(new RowContextClickHandler() {
            @Override
            public void onRowContextClick(RowContextClickEvent event) {
                ListGridRecord[] records = list.getSelectedRecords();

                Menu menu = null;

                if (records == null || records.length == 0) {
                    return;
                }
                else if (records.length == 1) {
                    menu = getSingleContextMenu(records);
                }
                else if (records.length > 1) {
                    menu = getMultiContextMenu(records);
                }

                if (menu != null) {
                    list.setContextMenu(menu);
                    menu.showContextMenu();

                    event.cancel();
                }
            }
        });

        this.redrawRequestHandlers = new ArrayList<RedrawRequestHandler>();
        this.outHandlers = new ArrayList<OutputParameterChangeHandler>();
        this.navigation  = new ThemeNavigationPanel();
        this.navigation.addOnMoveHandler(this);

        this.setShowResizeBar(true);
    }


    public abstract void activateTheme(Theme theme, boolean active);


    /**
     * Replace the current collection with a new one. <b>NOTE: this operation
     * triggers updateGrid() which modifies the themes in the grid.</b>
     *
     * @param collection The new collection object.
     */
    protected void setCollection(Collection collection) {
        // Set collection of view, but do not trigger event shooting.
        this.view.setCollection(collection, true);

        updateGrid();
    }


    /** Get Collection. */
    public Collection getCollection() {
        return view.getCollection();
    }


    /**
     * Returns the ThemeList of the current collection and output mode.
     *
     * @return the current ThemeList.
     */
    public ThemeList getThemeList() {
        return getCollection().getThemeList(mode.getName());
    }

    public ListGridRecord[] getSelectedRecords() {
        return list.getSelectedRecords();
    }

    /**
     * Registers a new OutputParameterChangeHandler.
     *
     * @param h The new handler.
     */
    @Override
    public void addOutputParameterChangeHandler(OutputParameterChangeHandler h){
        if (h != null) {
            outHandlers.add(h);
        }
    }


    /**
     * Registers a RedrawRequestHandler.
     *
     * @param h The new handler.
     */
    @Override
    public void addRedrawRequestHandler(RedrawRequestHandler h){
        if (h != null) {
            redrawRequestHandlers.add(h);
        }
    }


    /**
     * Request a redraw of e.g. a Chart.
     */
    final public void requestRedraw() {
        for (RedrawRequestHandler handler: redrawRequestHandlers) {
            handler.onRedrawRequest(new RedrawRequestEvent(Type.DEFAULT));
        }
    }


    /**
     * Called when the attribution of an output changed. It informs the
     * registered handlers about the changes.
     */
    protected void fireOutputParameterChanged() {
        OutputParameterChangeEvent evt = new OutputParameterChangeEvent();

        for (OutputParameterChangeHandler handler: outHandlers) {
            handler.onOutputParameterChanged(evt);
        }
    }


    /** Registers the CollectionView associated to this ThemePanel. */
    public void setCollectionView(CollectionView view) {
        this.view = view;
    }


    /**
     * This method is used to clear the current theme grid and add new updated
     * data.
     */
    protected void updateGrid() {
        GWT.log("ThemePanel.updateGrid");

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


    /** Adds given Record to the list (table). */
    protected void addFacetRecord(FacetRecord rec) {
        list.addData(rec);
    }


    /** Create a FacetRecord that wraps given theme. */
    protected FacetRecord createRecord(Theme theme) {
        return new FacetRecord(theme);
    }


    /**
     * This method triggers the CollectionAttributeService. Based on the current
     * collectin settings, the attribute of the collection is modified or not.
     * But in every case, we will get a new collection object - which might be
     * the same as the current one.
     */
    public void updateCollection() {
        final Config config = Config.getInstance();
        final String loc    = config.getLocale();

        GWT.log("ThemePanel.updateCollection via RPC now");

        // Don't forget to enable the panel after the request has finished!
        disable();

        updater.update(getCollection(), loc, new AsyncCallback<Collection>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not update collection attributes.");
                SC.warn(MSG.getString(caught.getMessage()));

                enable();
            }


            @Override
            public void onSuccess(Collection collection) {
                setCollection(collection);

                enable();
            }
        });
    }


    /**
     * Create and configure the Grid to display.
     */
    protected ListGrid createGrid() {
        ListGrid grid = createNewGrid();
        grid.setLeaveScrollbarGap(false);

        return grid;
    }


    protected ListGrid createNewGrid() {
        return new ListGrid();
    }


    /**
     * A method that removes all records from theme grid.
     */
    protected void clearGrid() {
        ListGridRecord[] records = list.getRecords();

        if (records == null || records.length == 0) {
            return;
        }

        for (ListGridRecord record: records) {
            list.removeData(record);
        }
    }

    /** Return 'separator'- menu-item. */
    protected MenuItem createSeparator() {
        MenuItem separator = new MenuItem();
        separator.setIsSeparator(true);
        return separator;
    }


    /**
     * Get the context menu for a (right mouse button)click on a single item.
     */
    protected Menu getSingleContextMenu(final ListGridRecord[] records) {
        Menu menu = new Menu();

        menu.addItem(createActivateItem(records));
        menu.addItem(createDeactivateItem(records));
        menu.addItem(createRemoveItem(records));
        menu.addItem(createSeparator());
        menu.addItem(createPropertiesItem(records));

        return menu;
    }


    protected Menu getMultiContextMenu(final ListGridRecord[] records) {
        Menu menu = new Menu();

        menu.addItem(createActivateItem(records));
        menu.addItem(createDeactivateItem(records));
        menu.addItem(createRemoveItem(records));

        return menu;
    }


    /** The properties menu item (opens style editor on click). */
    protected MenuItem createPropertiesItem(final ListGridRecord[] records) {
        MenuItem properties = new MenuItem(MSG.properties());

        properties.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                GWT.log("clicked properties");
                for (ListGridRecord record: records) {
                    openStyleEditor((FacetRecord) record);
                }
            }
        });

        return properties;
    }


    protected MenuItem createActivateItem(final ListGridRecord[] records) {
        MenuItem activate = new MenuItem(MSG.activateTheme());

        activate.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                for (ListGridRecord record: records) {
                    FacetRecord facet = (FacetRecord) record;
                    activateTheme(facet.getTheme(), true);
                }

                updateCollection();
            }
        });

        return activate;
    }


    protected MenuItem createDeactivateItem(final ListGridRecord[] records) {
        MenuItem deactivate = new MenuItem(MSG.deactivateTheme());

        deactivate.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                for (ListGridRecord record: records) {
                    FacetRecord facet = (FacetRecord) record;
                    activateTheme(facet.getTheme(), false);
                }

                updateCollection();
            }
        });

        return deactivate;
    }


    /** Remove given themes (not asking for confirmation). */
    protected void removeThemes(final ListGridRecord[] records) {
        for (ListGridRecord record: records) {
            FacetRecord facet = (FacetRecord) record;
            Theme theme = facet.getTheme();
            theme.setVisible(0);
            theme.setActive(0);
            updateCollection();
        }
    }


    /** Create menu item for removing theme(s). Will ask for confirmation. */
    protected MenuItem createRemoveItem(final ListGridRecord[] records) {
        MenuItem remove = new MenuItem(MSG.removeTheme());

        remove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                SC.ask(MSG.askThemeRemove(), new BooleanCallback() {
                    @Override
                    public void execute(Boolean value) {
                        if (value) {
                            removeThemes(records);
                        }
                    }
                });
            }
        });

        return remove;
    }


    /**
     * This method is called after a cell in the theme grid has been modified.
     *
     * @param event The event that stores information about the modified record.
     */
    @Override
    public void onEditComplete(EditCompleteEvent event) {
        GWT.log("Edited record.");

        int         row = event.getRowNum();
        FacetRecord rec = (FacetRecord) list.getRecord(row);

        Theme theme = rec.getTheme();

        theme.setDescription(rec.getName());
        activateTheme(theme, rec.getActive());

        updateCollection();
    }


    /**
     * This method should be defined in subclasses that wants to listen to this
     * event.
     *
     * @param theme The theme that is moved.
     * @param oldIdx The index of the theme before it was moved.
     * @param newIdx The index of the theme after it was moved.
     */
    protected void fireThemeMoved(Theme theme, int oldIdx, int newIdx) {
        // Do nothing
    }


    @Override
    public void onMove(OnMoveEvent event) {
        int type = event.getType();

        GWT.log("ThemePanel.onMove: " + type);

        ListGridRecord[] records = list.getSelectedRecords();

        if (records == null || records.length == 0) {
            GWT.log("ThemePanel.onMove: No records selected.");
            return;
        }

        switch (type) {
            case 0: moveRecordsTop(records); break;
            case 1: moveRecordsUp(records); break;
            case 2: moveRecordsDown(records); break;
            case 3: moveRecordsBottom(records); break;
        }

        updateCollection();
    }


    /**
     * Moves the selected grid records (themes) to the top of the grid.
     *
     * @param records The selected themes in the list. Null not permitted.
     */
    protected void moveRecordsTop(ListGridRecord[] records) {
        ThemeList themeList = getThemeList();

        int idx = 1;

        for (ListGridRecord record: records) {
            Theme theme = ((FacetRecord) record).getTheme();
            fireThemeMoved(theme, theme.getPosition(), idx);
            themeList.setThemePosition(theme, idx++);
        }

        updateGrid();
    }


    /**
     * Moves the selected grid records (themes) one step up.
     *
     * @param records The selected themes in the list. Null not permitted.
     */
    protected void moveRecordsUp(ListGridRecord[] records) {
        ThemeList themeList = getThemeList();

        int[] newPos = new int[records.length];

        for (int i = 0; i < records.length ; i++) {
            Theme theme = ((FacetRecord) records[i]).getTheme();
            newPos[i]   = theme.getPosition() - 1;
        }

        for (int i = 0; i < records.length ; i++) {
            Theme theme = ((FacetRecord) records[i]).getTheme();
            fireThemeMoved(theme, theme.getPosition(), newPos[i]);
            themeList.setThemePosition(theme, newPos[i]);
        }

        updateGrid();
    }


    /**
     * Moves the selected grid records (themes) one step down.
     *
     * @param records The selected themes in the list. Null not permitted.
     */
    protected void moveRecordsDown(ListGridRecord[] records) {
        ThemeList themeList = getThemeList();

        int[] newPos = new int[records.length];

        for (int i = records.length-1; i >= 0; i--) {
            Theme theme = ((FacetRecord) records[i]).getTheme();
            newPos[i] = theme.getPosition()+1;
        }

        for (int i = records.length-1; i >= 0; i--) {
            Theme theme = ((FacetRecord) records[i]).getTheme();
            fireThemeMoved(theme, theme.getPosition(), newPos[i]);
            themeList.setThemePosition(theme, newPos[i]);
        }

        updateGrid();
    }


    /**
     * Moves the selected grid records (themes) to the bottom of the grid.
     *
     * @param records The selected themes in the list. Null not permitted.
     */
    protected void moveRecordsBottom(ListGridRecord[] records) {
        ThemeList themeList = getThemeList();

        int idx = themeList.getThemeCount();

        for (int i = records.length-1; i >= 0; i--) {
            Theme theme = ((FacetRecord) records[i]).getTheme();
            fireThemeMoved(theme, theme.getPosition(), idx);
            themeList.setThemePosition(theme, idx--);
        }

        updateGrid();
    }


    protected void openStyleEditor(final FacetRecord record) {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        String artifact = record.getTheme().getArtifact();

        itemAttributeService.getCollectionItemAttribute(
            this.getCollection(),
            artifact,
            locale,
            new AsyncCallback<CollectionItemAttribute>() {
                @Override
                public void onFailure (Throwable caught) {
                    SC.warn(MSG.getString(caught.getMessage()));
                }
                @Override
                public void onSuccess(CollectionItemAttribute cia) {
                    GWT.log("Successfully loaded collectionitem attributes.");
                    showStyleEditor(cia, record);
                }
            });
    }


    protected void showStyleEditor(
        CollectionItemAttribute cia,
        FacetRecord record)
    {
        StyleEditorWindow win = new StyleEditorWindow(
            getCollection(),
            cia,
            record);
        win.setThemePanel(this);
        win.centerInPage();
        win.show();
    }


    /** Get OutputMode of this Panel. */
    public OutputMode getMode() {
        return this.mode;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
