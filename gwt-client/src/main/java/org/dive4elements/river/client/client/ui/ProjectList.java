/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortArrow;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IconButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.VisibilityChangedEvent;
import com.smartgwt.client.widgets.events.VisibilityChangedHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.HeaderDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.HeaderDoubleClickHandler;
import com.smartgwt.client.widgets.grid.events.RowContextClickEvent;
import com.smartgwt.client.widgets.grid.events.RowContextClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.CollectionChangeEvent;
import org.dive4elements.river.client.client.event.CollectionChangeHandler;
import org.dive4elements.river.client.client.event.FilterHandler;
import org.dive4elements.river.client.client.event.RangeFilterEvent;
import org.dive4elements.river.client.client.event.StringFilterEvent;
import org.dive4elements.river.client.client.services.AddArtifactService;
import org.dive4elements.river.client.client.services.AddArtifactServiceAsync;
import org.dive4elements.river.client.client.services.ArtifactService;
import org.dive4elements.river.client.client.services.ArtifactServiceAsync;
import org.dive4elements.river.client.client.services.CreateCollectionService;
import org.dive4elements.river.client.client.services.CreateCollectionServiceAsync;
import org.dive4elements.river.client.client.services.DeleteCollectionService;
import org.dive4elements.river.client.client.services.DeleteCollectionServiceAsync;
import org.dive4elements.river.client.client.services.DescribeCollectionService;
import org.dive4elements.river.client.client.services.DescribeCollectionServiceAsync;
import org.dive4elements.river.client.client.services.GetArtifactService;
import org.dive4elements.river.client.client.services.GetArtifactServiceAsync;
import org.dive4elements.river.client.client.services.SetCollectionNameService;
import org.dive4elements.river.client.client.services.SetCollectionNameServiceAsync;
import org.dive4elements.river.client.client.services.SetCollectionTTLService;
import org.dive4elements.river.client.client.services.SetCollectionTTLServiceAsync;
import org.dive4elements.river.client.client.services.UserCollectionsService;
import org.dive4elements.river.client.client.services.UserCollectionsServiceAsync;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItem;
import org.dive4elements.river.client.shared.model.CollectionRecord;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;


/**
 * The project list shows a list of projects of a specific user.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ProjectList
extends      VLayout
implements   CollectionChangeHandler, EditCompleteHandler, FilterHandler,
             VisibilityChangedHandler
{
    /** Interval to refresh the user's projects.*/
    public static final int UPDATE_INTERVAL = 30000;

    /** Min Interval to refresh the user's projects.*/
    public static final int MIN_UPDATE_INTERVAL = 5000;

    /** The initial width of this panel.*/
    public static final int MIN_WIDTH = 300;

    /** The max length for new project names.*/
    public static final int MAX_NAME_LENGTH = 50;

    public static final String COLUMN_DATE_WIDTH = "100px";

    public static final String COLUMN_TITLE_WIDTH = "*";

    public static final String COLUMN_FAVORITE_WIDTH = "75px";

    /** The interface that provides i18n messages. */
    private final FLYSConstants messages = GWT.create(FLYSConstants.class);

    /** The UserService used to retrieve information about the current user. */
    protected UserCollectionsServiceAsync userCollectionsService =
        GWT.create(UserCollectionsService.class);

    /** The service used to set the name of a project.*/
    protected SetCollectionNameServiceAsync nameService =
        GWT.create(SetCollectionNameService.class);

    /** The service used to set the name of a project.*/
    protected SetCollectionTTLServiceAsync ttlService =
        GWT.create(SetCollectionTTLService.class);

    /** The service used to set the name of a project.*/
    protected DeleteCollectionServiceAsync deleteService =
        GWT.create(DeleteCollectionService.class);

    /** The DescribeCollectionService used to update the existing collection. */
    protected DescribeCollectionServiceAsync describeCollectionService =
        GWT.create(DescribeCollectionService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected ArtifactServiceAsync createArtifactService =
        GWT.create(ArtifactService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected CreateCollectionServiceAsync createCollectionService =
        GWT.create(CreateCollectionService.class);

    /** The AddArtifactService used to add an artifact to a collection. */
    protected AddArtifactServiceAsync addArtifactService =
        GWT.create(AddArtifactService.class);

    /** The GetArtifactService used to open an existing collection. */
    protected GetArtifactServiceAsync getArtifactService =
        GWT.create(GetArtifactService.class);

    /** A pointer to the FLYS instance.*/
    protected FLYS flys;

    /** The user whose projects should be displayed.*/
    protected User user;

    /** The grid that contains the project rows.*/
    protected ListGrid grid;

    /** All user collections.*/
    protected List<Collection> collections;

    /** The collection to clone*/
    protected Collection cloneCollection;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    private String filter;


    /**
     * The default constructor that creates a new ProjectList for a specific
     * user.
     *
     * @param user The user.
     */
    public ProjectList(FLYS flys, User user) {
        super();
        this.flys = flys;
        this.user = user;

        collections = new ArrayList<Collection>();
        grid = new ListGrid();
        initGrid();
        init();
        initTimer();

        grid.addEditCompleteHandler(this);

        addVisibilityChangedHandler(this);

        updateUserCollections();
    }


    protected void initGrid() {
        grid.setWidth100();
        grid.setAutoFitData(Autofit.VERTICAL);
        grid.setAutoFitMaxWidth(500);
        grid.setEmptyMessage(messages.no_projects());
        grid.setLoadingDataMessage(messages.load_projects());
        grid.setCanEdit(false);
        grid.setEditEvent(ListGridEditEvent.NONE);
        grid.setShowHeaderContextMenu(false);
        grid.setShowSortArrow(SortArrow.NONE);
        grid.setSortDirection(SortDirection.DESCENDING);
        grid.setSortField(0);
        grid.setSelectionType(SelectionStyle.SINGLE);
        grid.setCanReorderFields(false);
        grid.setLeaveScrollbarGap(false);
        grid.setBorder("0px");

        ListGridField date = buildDateField();
        ListGridField name = buildNameField();
        ListGridField fav  = buildFavoriteField();

        grid.setFields(date, name, fav);

        grid.addHeaderDoubleClickHandler(new HeaderDoubleClickHandler() {
            @Override
            public void onHeaderDoubleClick(HeaderDoubleClickEvent event) {
                // Cancel the event.
                return;
            }
        });

        // Add a handler to set / unset the favorite state of a project.
        grid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                if (event.getColNum() != 2) {
                    return;
                }

                CollectionRecord r = (CollectionRecord) event.getRecord();
                Collection       c = r.getCollection();

                c.setTTL(c.getTTL() == 0 ? -1 : 0);
                updateCollectionTTL(c);
            }
        });

        // Add a handler to open a project.
        grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
            @Override
            public void onCellDoubleClick(CellDoubleClickEvent e) {
                CollectionRecord record = (CollectionRecord) e.getRecord();
                String uuid = record != null
                    ? record.getCollection().identifier()
                    : "";
                getFlys().openProject(uuid);
            }
        });

        // Add a handler to open a context menu.
        grid.addRowContextClickHandler(new RowContextClickHandler() {
            @Override
            public void onRowContextClick(RowContextClickEvent event) {
                CollectionRecord record = (CollectionRecord) event.getRecord();

                Menu menu = createContextMenu(record);
                grid.setContextMenu(menu);
                menu.showContextMenu();

                event.cancel();
            }
        });
    }


    /**
     * Initializes a repeating timer that updates the user's collections. The
     * interval is specified by the constant <i>UPDATE_INTERVAL</i>.
     */
    protected void initTimer() {
        Config config   = Config.getInstance();
        int    interval = config.getProjectListUpdateInterval();

        interval = interval > MIN_UPDATE_INTERVAL ? interval : UPDATE_INTERVAL;

        GWT.log("Update project list every " + interval + " milliseconds.");

        Timer t = new Timer() {
            @Override
            public void run() {
                updateUserCollections();
            }
        };

        t.scheduleRepeating(interval);
    }


    public FLYS getFlys() {
        return flys;
    }


    /**
     * Creates a new context menu that interacts with a CollectionRecord.
     *
     * @param record The selected record.
     *
     * @return the context menu with operations that interact with
     * <i>record</i>.
     */
    protected Menu createContextMenu(final CollectionRecord record) {
        Menu menu = new Menu();

        MenuItem open = new MenuItem(messages.open_project());
        open.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                getFlys().openProject(record.getCollection().identifier());
            }
        });

        MenuItem del = new MenuItem(messages.delete_project());
        del.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                SC.ask(messages.really_delete(), new BooleanCallback() {
                    @Override
                    public void execute(Boolean value) {
                        if (value) {
                            deleteCollection(record.getCollection());
                        }
                    }
                });
            }
        });

        MenuItem rename = new MenuItem(messages.rename_project());
        rename.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                int row = grid.getRecordIndex(record);
                grid.startEditing(row, 1, false);
            }
        });

        MenuItem clone = new MenuItem(messages.clone_project());
        clone.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                cloneProject(record.getCollection());
            }
        });

        menu.addItem(open);
        menu.addItem(rename);
        menu.addItem(clone);
        menu.addItem(new MenuItemSeparator());
        menu.addItem(del);

        return menu;
    }


    /**
     * The init() method handles the layout stuff for this widget.
     */
    protected void init() {
        setWidth(MIN_WIDTH);
        setMinWidth(MIN_WIDTH);
        setHeight100();
        setShowResizeBar(true);
        setShowEdges(false);
        setLayoutMargin(0);
        setLayoutAlign(VerticalAlignment.TOP);
        setOverflow(Overflow.AUTO);

        Label title = new Label(messages.projects());
        title.setHeight("20");
        title.setMargin(5);
        title.setWidth100();
        title.setStyleName("projectHeader");

        HLayout buttonWrapper = new HLayout();

        IconButton addButton = new IconButton("");
        addButton.setIcon(messages.projectListAdd());
        addButton.setTooltip(messages.new_project());
        addButton.setWidth("30px");

        IconButton closeButton = new IconButton("");
        closeButton.setIcon(messages.projectListMin());
        closeButton.setTooltip(messages.projectlist_close());
        closeButton.setWidth("30px");

        buttonWrapper.addMember(addButton);
        buttonWrapper.addMember(closeButton);
        buttonWrapper.setAlign(Alignment.RIGHT);
        buttonWrapper.setAutoWidth();

        HLayout titleWrapper = new HLayout();
        titleWrapper.setStyleName("bgBlueMid");
        titleWrapper.setWidth100();
        titleWrapper.setHeight("20px");
        titleWrapper.addMember(title);
        titleWrapper.addMember(buttonWrapper);

        Canvas gridWrapper = new Canvas();
        gridWrapper.setPadding(0);
        titleWrapper.setWidth100();
        gridWrapper.addChild(grid);

        TableFilter filterpanel = new TableFilter();
        filterpanel.setHeight("30px");
        filterpanel.addFilterHandler(this);
        filterpanel.setBorder("1px solid gray");

        addMember(titleWrapper);
        addMember(gridWrapper);
        addMember(filterpanel);

        addButton.addClickHandler(
                new com.smartgwt.client.widgets.events.ClickHandler() {

                    @Override
                    public void onClick(ClickEvent ev) {
                        flys.newProject();
                    }
                });

        closeButton.addClickHandler(
                new com.smartgwt.client.widgets.events.ClickHandler() {

            @Override
            public void onClick(ClickEvent ev) {
                hide();
            }
        });
    }


    @Override
    public void onFilterCriteriaChanged(StringFilterEvent event) {
        String search = event.getFilter();
        // Filter the records.
        setFilter(search);
        updateGrid();
    }


    @Override
    public void onFilterCriteriaChanged(RangeFilterEvent event) {
        //Empty. No Ranges to filter.
    }


    /** On collection change, update list (probably name change or similar). */
    @Override
    public void onCollectionChange(CollectionChangeEvent event) {
        if (event.getOldValue() == null) {
            updateUserCollections();
        }
    }


    @Override
    public void onEditComplete(EditCompleteEvent event) {
        if (event.getColNum() != 1) {
            return;
        }

        int row = event.getRowNum();

        CollectionRecord r = (CollectionRecord) grid.getRecord(row);
        Collection       c = r.getCollection();

        Map<?, ?> newValues = event.getNewValues();
        String name   = (String) newValues.get("name");

        int maxLength = getMaxNameLength();
        int length    = name != null ? name.length() : 0;

        if (length <= 0 || length > maxLength) {
            String msg = messages.project_name_too_long();
            msg        = msg.replace("$LEN", String.valueOf(maxLength));
            SC.warn(msg);

            ListGridRecord[] rs = grid.getRecords();
            rs[row] = (ListGridRecord) event.getOldValues();
            grid.setRecords(rs);

            return;
        }

        updateCollectionName(c, name);
    }


    /**
     * Set the name of the collection <i>c</i> to a new value. If the update
     * process succeeded, the project list is refreshed.
     *
     * @param c The Collection with a new name.
     * @param name Name to set on the collection
     */
    private void updateCollectionName(final Collection c, String name) {
        if (c == null) {
            return;
        }

        c.setName(name);

        GWT.log("Update Collection name: " + c.identifier());
        GWT.log("=> New name = " + c.getName());

        nameService.setName(c, new AsyncCallback<Void>(){
            @Override
            public void onFailure(Throwable caught) {
                String msg = caught.getMessage();

                try {
                    SC.warn(messages.getString(msg));
                }
                catch (MissingResourceException mre) {
                    SC.warn(msg);
                }
            }

            @Override
            public void onSuccess(Void v) {
                updateUserCollections();
                if(flys.getWorkspace().hasView(c.identifier())) {
                    flys.getWorkspace().updateTitle(
                        c.identifier(), c.getName());
                }
            }
        });
    }


    /**
     * Set the ttl of the collection <i>c</i> to a new value. If the update
     * process succeeded, the project list is refreshed.
     *
     * @param c The Collection with a new ttl.
     */
    protected void updateCollectionTTL(Collection c) {
        if (c == null) {
            return;
        }

        GWT.log("Update Collection TTL: " + c.identifier());
        GWT.log("=> New ttl = " + c.getTTL());

        ttlService.setTTL(c, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                String msg = caught.getMessage();

                try {
                    SC.warn(messages.getString(msg));
                }
                catch (MissingResourceException mre) {
                    SC.warn(msg);
                }
            }

            @Override
            public void onSuccess(Void v) {
                updateUserCollections();
            }
        });
    }


    /**
     * Delete the collection <i>c</i>.
     *
     * @param c The Collection that should be deleted.
     */
    public void deleteCollection(final Collection c) {
        if (c == null) {
            return;
        }

        GWT.log("Delete Collection: " + c.identifier());

        deleteService.delete(c, new AsyncCallback<Void>(){
            @Override
            public void onFailure(Throwable caught) {
                String msg = caught.getMessage();

                try {
                    SC.warn(messages.getString(msg));
                }
                catch (MissingResourceException mre) {
                    SC.warn(msg);
                }
            }

            @Override
            public void onSuccess(Void v) {
                flys.getWorkspace().destroyProject(c.identifier());
                updateUserCollections();
            }
        });
    }


    public void updateUserCollections() {
        GWT.log("==> ProjectList updates user collections!");

        Config config = Config.getInstance();
        String locale = config.getLocale();

        userCollectionsService.getUserCollections(locale, user.identifier(),
            new AsyncCallback<Collection[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    String msg = caught.getMessage();

                    try {
                        SC.warn(messages.getString(msg));
                    }
                    catch (MissingResourceException mre) {
                        SC.warn(msg);
                    }
                }

                @Override
                public void onSuccess(Collection[] collections) {
                    int num = collections != null ? collections.length : 0;

                    GWT.log("Received " + num + " user collections.");

                    updateGridDataSource(collections);
                }
            }
        );
    }


    /**
     * Delete all entries in the ListGrid.
     */
    private void clearGrid() {
        ListGridRecord[] records = grid.getRecords();

        for (ListGridRecord record: records) {
            grid.removeData(record);
        }
    }


    /**
     * Update the collections data source.
     *
     * First removes all collections to avoid duplicates, then add new entries.
     *
     * @param c Collections to set to the data source.
     */
    private void updateGridDataSource(Collection[] c) {
        collections.clear();
        if(c == null) {
            clearGrid();
            return;
        }
        for (Collection coll : c) {
            this.collections.add(coll);
        }
        updateGrid();
    }


    /**
     * Updates the ListGrid.
     */
    private void updateGrid() {
        clearGrid();

        if (collections == null || collections.isEmpty()) {
            return;
        }

        for (Collection col: collections) {
            String name;

            name = col.getDisplayName().toLowerCase();

            // Add a collection to the filtered list if the search string
            // matches.
            if (filter == null || filter.isEmpty() ||
                    name.contains(filter.toLowerCase())) {
                grid.addData(new CollectionRecord(col));
            }
        }
    }

    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }


    /**
     * Builds the field in the grid that displays the creation time of a
     * project.
     *
     * @return the grid field.
     */
    protected ListGridField buildDateField() {
        ListGridField date = new ListGridField(
            "creationTime", messages.projectlist_creationTime());

        date.setType(ListGridFieldType.DATE);
        date.setCanEdit(false);

        date.setCellFormatter(new CellFormatter() {
            @Override
            public String format(
                Object value, ListGridRecord rec, int r, int c) {
                if (value == null) {
                    return null;
                }

                DateTimeFormat dtf = DateTimeFormat.getFormat(
                    messages.datetime_format());

                return dtf.format((Date)value);
            }
        });

        date.setWidth(COLUMN_DATE_WIDTH);
        date.setAlign(Alignment.CENTER);

        return date;
    }


    /**
     * Builds the field in the grid that displays the name of a project.
     *
     * @return the grid field.
     */
    protected ListGridField buildNameField() {
        ListGridField name = new ListGridField(
            "name", messages.projectlist_title());

        name.setType(ListGridFieldType.TEXT);
        name.setShowHover(true);
        name.setHoverCustomizer(new HoverCustomizer() {
            @Override
            public String hoverHTML(
                Object         value,
                ListGridRecord record,
                int            row,
                int            col)
            {
                CollectionRecord r = (CollectionRecord) record;
                Collection       c = r.getCollection();

                String name = r.getName();

                return name != null && name.length() > 0
                    ? name
                    : c.identifier();
            }
        });

        name.setWidth(COLUMN_TITLE_WIDTH);
        name.setAlign(Alignment.LEFT);

        return name;
    }


    protected ListGridField buildFavoriteField() {
        ListGridField fav = new ListGridField(
            "ttl", messages.projectlist_favorite());

        fav.setType(ListGridFieldType.IMAGE);
        String base = GWT.getHostPageBaseURL();
        fav.setImageURLPrefix(base + "images/");
        fav.setImageURLSuffix(".png");
        fav.setWidth(COLUMN_FAVORITE_WIDTH);
        fav.setAlign(Alignment.CENTER);
        fav.setCanEdit(false);

        return fav;
    }


    protected void cloneProject(Collection c) {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        cloneCollection = c;

        describeCollectionService.describe(c.identifier(), locale,
            new AsyncCallback<Collection>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not DESCRIBE collection.");
                    SC.warn(messages.getString(caught.getMessage()));
                }


                @Override
                public void onSuccess(Collection newCollection) {
                    GWT.log("Successfully DESCRIBED collection.");
                    String uuid = getMasterArtifact(newCollection);
                    cloneArtifact(uuid, newCollection);
                }
            }
        );
    }


    /** Get master artifacts UUID of a collection. */
    protected String getMasterArtifact(Collection newCollection) {
        String uuid = newCollection.getItem(0).identifier();
        // The master artifact uuid.
        return uuid;
    }


    /** Clone artifact/create collection, using the refArtifacts factory. */
    protected void cloneArtifact(String uuid, Artifact refArtifact,
        final String locale) {
        Recommendation recommendation = new Recommendation(
            refArtifact.getName(),
            null,
            uuid,
            null);

        String factory = recommendation.getFactory();
        createArtifactService.create(
            locale, factory, recommendation,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error loading recommendations: " +
                        caught.getMessage());
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Created new artifact: " + artifact.getUuid());
                    createClonedCollection(artifact);
                }
            }
        );
    }


    /**
     * Clone a project (collection).
     */
    private void cloneArtifact(final String uuid, Collection newCollection) {
        Config config       = Config.getInstance();
        final String locale = config.getLocale();

        // Find out which factory to use for cloning.
        CollectionItem master = newCollection.getItem(0);
        getArtifactService.getArtifact(
            locale,
            master.identifier(),
            master.hash(),
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    SC.warn(MSG.getString(caught.getMessage()));
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    cloneArtifact(uuid, artifact, locale);
                }
        });
    }


    /**
     * Creates a {@link Collection} with the passed {@link Artifact}
     * @param artifact {@link Artifact} to add to the new {@link Collection}
     */
    private void createClonedCollection(final Artifact artifact) {
        Config config        = Config.getInstance();
        final String locale  = config.getLocale();
        final String ownerid = user.identifier();

        createCollectionService.create(
            locale,
            ownerid,
            new AsyncCallback<Collection>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not create the new collection.");
                    SC.warn(messages.getString(caught.getMessage()));
                }

                @Override
                public void onSuccess(Collection collection) {
                    GWT.log("Successfully created a new collection.");
                    addArtifactToClonedCollection(artifact, collection);
                }
            }
        );
    }


    private void addArtifactToClonedCollection(Artifact a, Collection c) {
        Config config       = Config.getInstance();
        final String locale = config.getLocale();

        addArtifactService.add(
            c, a, locale,
            new AsyncCallback<Collection>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("An error occured while adding artifact.");
                    SC.warn(messages.getString(caught.getMessage()));
                }

                @Override
                public void onSuccess(Collection newColl) {
                    String name = cloneCollection.getName();
                    if(name == null || name.equals("")) {
                        name = cloneCollection.identifier();
                    }

                    String colname = messages.copy_of() + ": " + name;

                    updateCollectionName(newColl, colname);
                    if(cloneCollection.getTTL() == 0) {
                        newColl.setTTL(0);
                        updateCollectionTTL(newColl);
                    }
                }
            }
        );
    }


    @Override
    public void onVisibilityChanged(VisibilityChangedEvent event) {
        if (event.getIsVisible()) {
            this.flys.hideHeaderProjectButton();
        }
        else {
            this.flys.shoHeaderProjectButton();
        }
    }

    private void setFilter(String filter) {
        this.filter = filter;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
