/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.XMLParser;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.dive4elements.river.client.client.event.CollectionChangeEvent;
import org.dive4elements.river.client.client.event.CollectionChangeHandler;
import org.dive4elements.river.client.client.services.ArtifactService;
import org.dive4elements.river.client.client.services.ArtifactServiceAsync;
import org.dive4elements.river.client.client.services.CreateCollectionService;
import org.dive4elements.river.client.client.services.CreateCollectionServiceAsync;
import org.dive4elements.river.client.client.services.DescribeCollectionService;
import org.dive4elements.river.client.client.services.DescribeCollectionServiceAsync;
import org.dive4elements.river.client.client.services.GetArtifactService;
import org.dive4elements.river.client.client.services.GetArtifactServiceAsync;
import org.dive4elements.river.client.client.services.RiverService;
import org.dive4elements.river.client.client.services.RiverServiceAsync;
import org.dive4elements.river.client.client.services.ServerInfoService;
import org.dive4elements.river.client.client.services.ServerInfoServiceAsync;
import org.dive4elements.river.client.client.services.UserService;
import org.dive4elements.river.client.client.services.UserServiceAsync;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.FLYSHeader;
import org.dive4elements.river.client.client.ui.FLYSView;
import org.dive4elements.river.client.client.ui.FLYSWorkspace;
import org.dive4elements.river.client.client.ui.ProjectList;
import org.dive4elements.river.client.client.ui.wq.WQAutoTabSet;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItem;
import org.dive4elements.river.client.shared.model.GaugeInfo;
import org.dive4elements.river.client.shared.model.River;
import org.dive4elements.river.client.shared.model.User;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FLYS implements EntryPoint, CollectionChangeHandler {

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** The UserService used to retrieve information about the current user. */
    protected UserServiceAsync userService = GWT.create(UserService.class);

    protected ServerInfoServiceAsync serverInfoService =
        GWT.create(ServerInfoService.class);

    /** The RiverService used to retrieve the supported rivers of the server.*/
    protected RiverServiceAsync riverService = GWT.create(RiverService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected ArtifactServiceAsync artifactService =
        GWT.create(ArtifactService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected DescribeCollectionServiceAsync describeCollectionService =
        GWT.create(DescribeCollectionService.class);

    /** The GetArtifactService used to open an existing collection. */
    protected GetArtifactServiceAsync getArtifactService =
        GWT.create(GetArtifactService.class);

    /** The CreateCollectionServiceAsync used to create a new collection */
    protected CreateCollectionServiceAsync collectionService =
        GWT.create(CreateCollectionService.class);

    /** The content window. It takes the whole space beneath the header. */
    protected FLYSView view;

    /** The project list that displays the projects of the user. */
    protected ProjectList projectList;

    /** The FLYSWorkspace. */
    protected FLYSWorkspace workspace;

    /** The user who is currently logged in. */
    protected User currentUser;

    /** The list of rivers supported by the server. */
    protected River[] rivers;

    /** This list is used to track the opened projects. */
    protected List<String> openProjects;

    private FLYSHeader header;


    public static String getExceptionString(
        FLYSConstants msg,
        Throwable caught
    ) {
        try {
            return msg.getString(caught.getMessage());
        }
        catch(MissingResourceException ex) {
            // There are some server error exceptions with
            // varying text messages that cannot be localized
            // easily. In this rare cases, use the plain
            // exception message.
            GWT.log("Missing resource for: " + caught.getMessage());
            return caught.getLocalizedMessage();
        }
    }

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        openProjects = new ArrayList<String>();

        //GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
        //    public void onUncaughtException(Throwable e) {
        //        showWarning(e);
        //    }
        //});

        initConfiguration();

        VLayout vertical = new VLayout();
        vertical.setLayoutMargin(1);
        vertical.setWidth100();
        vertical.setHeight100();

        view = new FLYSView();
        header = new FLYSHeader(this);

        vertical.addMember(header);
        vertical.addMember(view);

        vertical.draw();

        Config config = Config.getInstance();
        final String locale = config.getLocale();

        serverInfoService.getConfig(
            locale, new AsyncCallback<Map<String,String>>() {

            @Override
            public void onSuccess(Map<String, String> result) {
                GWT.log("serverInfoService.callBack.onSuccess");
                GWT.log("help-url=" + result.get("help-url"));
                Config.getInstance().setWikiUrl(result.get("help-url"));

                // Start user service; somewhat nested here...
                startUserService(locale);
            }

            @Override
            public void onFailure(Throwable caught) {
               GWT.log("Could not read server information.");
               String msg = getExceptionString(MSG, caught);
               SC.warn(msg);
               startUserService(locale);
            }
        });
    }

    protected void startUserService(String locale) {
        userService.getCurrentUser(locale, new AsyncCallback<User>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not find a logged in user.");
                String msg = getExceptionString(MSG, caught);
                SC.warn(msg);
            }

            @Override
            public void onSuccess(User user) {
                GWT.log("Found a user. Set '"+ user.getName() + "'");
                setCurrentUser(user);

                header.setCurrentUser(user);

                projectList = new ProjectList(FLYS.this, user);
                workspace   = new FLYSWorkspace(FLYS.this);
                view.setProjectList(projectList);
                view.setFLYSWorkspace(workspace);

                readRivers();
            }
        });
    }


    public void showWarning(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tt>");

        if (e instanceof UmbrellaException) {
            UmbrellaException u = (UmbrellaException) e;
            Set<Throwable> throwables = u.getCauses();

            for (Throwable t: throwables) {
                sb.append(t.getLocalizedMessage());
                sb.append("<br>");
            }
        }
        else {
            sb.append(e.getLocalizedMessage());
        }

        sb.append("</tt>");

        Window w = new Window();
        w.setTitle(MSG.unexpected_exception());
        w.setWidth(550);
        w.setHeight(300);
        w.centerInPage();
        w.setCanDragResize(true);

        HTMLPane p = new HTMLPane();
        p.setContents(sb.toString());

        w.addItem(p);
        w.show();
    }


    /**
     * This method should be called at system start. It initialzes the client
     * configuration.
     */
    protected void initConfiguration() {
        String xml = FLYSResources.INSTANCE.initialConfiguration().getText();
        Config.getInstance(XMLParser.parse(xml));
    }


    /**
     * Returns the user that is currently logged in.
     *
     * @return the current user.
     */
    public User getCurrentUser() {
        return currentUser;
    }


    /**
     * Sets the current user.
     */
    public void setCurrentUser(User user) {
        currentUser = user;
    }


    /**
     * Returns the project list.
     */
    public ProjectList getProjectList() {
        return projectList;
    }


    /**
     * Returns the projects workspace that contains all project windows.
     *
     * @return the FLYSWorkspace.
     */
    public FLYSWorkspace getWorkspace() {
        return workspace;
    }


    /**
     * Returns a list of rivers supported by the artifact server.
     *
     * @return a list of rivers supported by the artifact server.
     */
    public River[] getRivers() {
        return rivers;
    }


    protected void readRivers() {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        riverService.list(locale, new AsyncCallback<River[]>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not recieve a list of rivers.");
                SC.warn(getExceptionString(MSG, caught));
            }

            @Override
            public void onSuccess(River[] newRivers) {
                GWT.log("Retrieved " + newRivers.length + " new rivers.");
                rivers = newRivers;
                newProject();
            }
        });
    }


    /**
     * This method creates a new CollectionView and adds it to the workspace.
     * <b>NOTE</b>The user needs to be logged in and there need to at least one
     * river - otherwise a warning is displayed and no CollectionView is
     * created.
     */
    public void newProject() {
        if (getCurrentUser() == null) {
            SC.warn(MSG.error_not_logged_in());
            return;
        }

        if (getRivers() == null) {
            SC.warn(MSG.error_no_rivers_found());
            readRivers();

            return;
        }

        CollectionView view = new CollectionView(this);
        workspace.addView("new-project", view);

        view.addCollectionChangeHandler(getProjectList());
    }


    protected void lockProject(String uuid) {
        if (isProjectLocked(uuid)) {
            return;
        }

        openProjects.add(uuid);
    }


    protected void unlockProject(String uuid) {
        openProjects.remove(uuid);
    }


    /** Whether project uuid is currently opened. */
    protected boolean isProjectLocked(String uuid) {
        return openProjects.contains(uuid);
    }


    /** Opens (or bring into foreground) project with given id. */
    public void openProject(final String collectionID) {
        if (collectionID == null) {
            return;
        }

        if (isProjectLocked(collectionID)) {
            workspace.bringUp(collectionID);
            return;
        }

        lockProject(collectionID);

        GWT.log("Open existing project: " + collectionID);

        Config config       = Config.getInstance();
        final String locale = config.getLocale();

        describeCollectionService.describe(collectionID, locale,
            new AsyncCallback<Collection>() {
                @Override
                public void onFailure(Throwable caught) {
                    SC.warn(getExceptionString(MSG, caught));
                }

                @Override
                public void onSuccess(Collection c) {
                    final Collection collection = c;

                    if (collection.getItemLength() == 0) {
                        CollectionView view = new CollectionView(
                            FLYS.this, collection, null);

                        view.addCollectionChangeHandler(
                            getProjectList());
                        view.addCloseClickHandler(
                            new CloseCollectionViewHandler(
                                FLYS.this, collectionID));

                        workspace.addView(collectionID, view);

                        return;
                    }

                    final CollectionItem item = c.getItem(0);

                    if (item == null) {
                        SC.warn(MSG.error_load_parameterization());
                        return;
                    }

                    getArtifactService.getArtifact(
                        locale,
                        item.identifier(),
                        item.hash(),
                        new AsyncCallback<Artifact>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                unlockProject(collectionID);
                                SC.warn(getExceptionString(MSG, caught));
                            }

                            @Override
                            public void onSuccess(Artifact artifact) {
                                CollectionView view = new CollectionView(
                                    FLYS.this, collection, artifact);

                                view.addCollectionChangeHandler(
                                    getProjectList());
                                view.addCloseClickHandler(
                                    new CloseCollectionViewHandler(
                                        FLYS.this, collectionID));

                                workspace.addView(collectionID, view);
                            }
                    });

                }
        });
    }


    public void closeProject(String uuid) {
        unlockProject(uuid);
        workspace.destroyProject(uuid);
    }


    /**
     * Create a new Artifact.
     */
    public void newArtifact(String factory) {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        artifactService.create(locale, factory, null,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not create the new artifact.");
                    SC.warn(getExceptionString(MSG, caught));
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully created a new artifact.");
                }
        });
    }


    /** Opens a window with Main Values from gauge. */
    public void newGaugeMainValueTable(GaugeInfo gauge) {
        Window mainValueView = new Window();

        // Take middle to avoid issues at borders.
        double km = (gauge.getKmEnd() + gauge.getKmStart())/2d;
        mainValueView.addItem(new WQAutoTabSet(gauge.getRiverName(),
            new double[] {km, km}));
        mainValueView.setWidth(450);
        mainValueView.setHeight(600);

        mainValueView.setMaximized(false);
        mainValueView.centerInPage();
        mainValueView.setCanDragReposition(true);
        mainValueView.setCanDragResize(true);
        mainValueView.setShowMaximizeButton(true);
        mainValueView.setKeepInParentRect(true);

        mainValueView.setTitle(MSG.mainvalues() + " "
            + gauge.getName() + " (" + gauge.getRiverName() + ")" );
        workspace.addChild(mainValueView);
    }


    @Override
    public void onCollectionChange(CollectionChangeEvent event) {
        Collection oldC = event.getOldValue();

        if (oldC == null) {
            Collection newC = event.getNewValue();
            lockProject(newC.identifier());
        }
    }



    /**
     * This CloseClickHandler is used to remove lock on a specific Collection
     * so that is might be opened again.
     */
    public class CloseCollectionViewHandler implements CloseClickHandler {
        protected FLYS   flys;
        protected String uuid;

        public CloseCollectionViewHandler(FLYS flys, String uuid) {
            this.flys = flys;
            this.uuid = uuid;
        }

        @Override
        public void onCloseClick(CloseClickEvent event) {
            flys.closeProject(uuid);
        }
    }

    public boolean isProjectListVisible() {
        if (this.projectList == null) {
            return true;
        }
        return this.projectList.isVisible();
    }

    public void hideProjectList() {
        if (this.projectList != null) {
            this.projectList.hide();
        }
    }

    public void openProjectList() {
        if (this.projectList != null) {
            this.projectList.show();
        }
    }

    public void hideHeaderProjectButton() {
        this.header.hideProjectButton();
    }

    public void shoHeaderProjectButton() {
        this.header.showProjectButton();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
