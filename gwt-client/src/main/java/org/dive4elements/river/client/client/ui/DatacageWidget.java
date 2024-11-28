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

import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.TreeModelType;

import com.smartgwt.client.util.SC;

import com.smartgwt.client.widgets.Button;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler;

import com.smartgwt.client.widgets.layout.VLayout;

import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Stack;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.client.event.DatacageDoubleClickHandler;
import org.dive4elements.river.client.client.event.DatacageHandler;

import org.dive4elements.river.client.client.services.MetaDataService;
import org.dive4elements.river.client.client.services.MetaDataServiceAsync;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.AttrList;
import org.dive4elements.river.client.shared.model.DataCageNode;
import org.dive4elements.river.client.shared.model.DataCageTree;
import org.dive4elements.river.client.shared.model.ToLoad;
import org.dive4elements.river.client.shared.model.User;

// TODO: refactor, extract ~DataCageGrid
/**
 * Display tree of, for example, previous calculations and allows
 * selection in order to access/clone these.
 */
public class DatacageWidget
extends      VLayout
{
    public static final int MAX_OPEN = 30;

    protected MetaDataServiceAsync metaDataService =
        GWT.create(MetaDataService.class);

    protected FLYSConstants messages =
        GWT.create(FLYSConstants.class);

    protected Artifact artifact;
    protected User     user;
    protected String   outs;
    protected String   parameters;

    protected TreeGrid treeGrid;
    protected Tree     tree;

    protected ToLoad   toLoad;

    protected List<DatacageHandler>            handlers;
    protected List<DatacageDoubleClickHandler> doubleHandlers;

    /** Layout to show spinning wheel of joy. */
    protected VLayout lockScreen;


    public DatacageWidget() {
        handlers       = new ArrayList<DatacageHandler>();
        doubleHandlers = new ArrayList<DatacageDoubleClickHandler>();
    }


    public DatacageWidget(Artifact artifact, User user) {
        this(artifact, user, null);
    }

    public DatacageWidget(Artifact artifact, User user, String outs) {
        this(artifact, user, outs, true);
    }

    public DatacageWidget(
        Artifact   artifact,
        User       user,
        String     outs,
        boolean    showButton
    ) {
        this(artifact, user, outs, null, showButton);
    }


    public DatacageWidget(
        Artifact   artifact,
        User       user,
        String     outs,
        String     parameters,
        boolean    showButton
    ) {
        this();

        this.artifact   = artifact;
        this.user       = user;
        this.outs       = outs;
        this.parameters = parameters;

        toLoad = new ToLoad();

        setWidth100();

        tree = new Tree();
        tree.setModelType(TreeModelType.CHILDREN);
        tree.setNameProperty("name");
        tree.setIdField("id");
        tree.setChildrenProperty("children-nodes");
        tree.setShowRoot(false);

        treeGrid = new TreeGrid();
        treeGrid.setLoadDataOnDemand(false);
        treeGrid.setWidth100();
        treeGrid.setHeight100();
        treeGrid.setShowRoot(false);
        treeGrid.setNodeIcon("[SKIN]/../blank.gif");
        treeGrid.setShowConnectors(true);
        treeGrid.setLoadingMessage(messages.databasket_loading());
        treeGrid.setEmptyMessage(messages.databasket_loading());
        treeGrid.setLoadingDataMessage(messages.databasket_loading());

        treeGrid.setHoverMoveWithMouse(true);
        treeGrid.setCanHover(true);
        treeGrid.setShowHover(true);
        treeGrid.setHoverOpacity(75);
        treeGrid.setHoverWidth(120);

        treeGrid.setHoverCustomizer(new HoverCustomizer() {
            @Override
            public String hoverHTML(Object value,
                ListGridRecord record,
                int rowNum,
                int colNum
                ) {
                if(record instanceof TreeNode) {
                    TreeNode hoveredTreeNode = (TreeNode)record;
                    String info = hoveredTreeNode.getAttribute("info");
                    if (info == null) {
                        info = hoveredTreeNode.getName();
                    }
                    return info;
                }
                else {
                    return "";// should not happen
                }
            }
        });

        treeGrid.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
            @Override
            public void onRecordDoubleClick(RecordDoubleClickEvent event) {
                doubleClickedOnTree(event);
            }
        });

        addMember(treeGrid);

        if (showButton) {
            addMember(createPlusButton());
        }

        triggerTreeBuilding();
    }

    /** Disable input, show spinning wheel of joy. */
    public void lockUI() {
        lockScreen = ScreenLock.lockUI(this, lockScreen);
    }

    /** Enable input, remove grey, remove spinning wheel of joy. */
    public void unlockUI() {
        ScreenLock.unlockUI(this, lockScreen);
    }

    /**
     * @param handler Handler to be added (notified on add-action).
     */
    public DatacageWidget(Artifact artifact, User user, String outs,
        DatacageHandler handler) {
        this(artifact, user, outs);
        this.addDatacageHandler(handler);
    }


    public DatacageWidget(
        Artifact        artifact,
        User            user,
        String          outs,
        DatacageHandler handler,
        String          parameters
    ) {
        this(artifact, user, outs, handler);
        this.parameters = parameters;
    }


    /**
     * Sets whether more than one item can be selected.
     * @param multi if true, allow mutliple selections.
     */
    public void setIsMutliSelectable(boolean multi) {
        if (multi) {
            treeGrid.setSelectionType(SelectionStyle.MULTIPLE);
        }
        else {
            treeGrid.setSelectionType(SelectionStyle.SINGLE);
        }
    }


    /**
     * @param handler Handler to be added (notified on add-action).
     */
    public void addDatacageHandler(DatacageHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }


    /**
     * @param h Handler to be added (notified on Double click on node).
     */
    public void addDatacageDoubleClickHandler(DatacageDoubleClickHandler h) {
        if (!doubleHandlers.contains(h)) {
            doubleHandlers.add(h);
        }
    }


    /**
     * @param handler Handler to remove from list.
     */
    public void removeDatacageHandler(DatacageHandler handler) {
        handlers.remove(handler);
    }


    public ToLoad getToLoad() {
        return toLoad;
    }


    public ToLoad getSelection() {
        // Reset content of toLoads.
        toLoad = new ToLoad();

        if (treeGrid == null) {
            return toLoad;
        }

        ListGridRecord [] selection = treeGrid.getSelectedRecords();

        if (selection != null) {
            for (ListGridRecord record: selection) {
                if (record instanceof TreeNode) {
                    collectToLoads((TreeNode)record);
                }
            }
        }

        return toLoad;
    }


    public List<TreeNode> getPlainSelection() {
        ListGridRecord [] selection = treeGrid.getSelectedRecords();
        List<TreeNode> nodes = new ArrayList<TreeNode>();
        if (selection != null) {
            for (ListGridRecord record: selection) {
                if (record instanceof TreeNode) {
                    nodes.add((TreeNode)record);
                }
            }
        }
        return nodes;
    }


    /**
     * Returns the titles of selected items (if any).
     */
    public String[] getSelectionTitles() {
        if (treeGrid == null) {
            return new String[] {};
        }

        ListGridRecord [] selection = treeGrid.getSelectedRecords();

        if (selection == null) {
            return new String[] {};
        }

        List<String> titleList = new ArrayList<String>();
        for (ListGridRecord record: selection) {
            if (record instanceof TreeNode) {
                titleList.add(((TreeNode)record).getAttribute("name"));
            }
        }

        return titleList.toArray(new String[titleList.size()]);
    }


    /**
     * Callback for add-button.
     * Fires to load for every selected element and handler.
     */
    public void plusClicked() {
        if (!getSelection().isEmpty()) {
            fireToLoad();
        }
    }


    protected Button createPlusButton() {
        Button plusBtn = new Button(messages.datacageAdd());
        plusBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                plusClicked();
            }
        });
        return plusBtn;
    }


    protected void fireToLoad() {
        for (DatacageHandler handler: handlers) {
            handler.toLoad(toLoad);
        }
    }


    /** Notify DatacageDoubleClickHandlers that a doubleclick happened. */
    protected void fireOnDoubleClick() {
        for (DatacageDoubleClickHandler handler: doubleHandlers) {
            handler.onDoubleClick(toLoad);
        }
    }


    protected void doubleClickedOnTree(RecordDoubleClickEvent event) {
        TreeNode node = (TreeNode)event.getRecord();
        collectToLoads(node);
        fireOnDoubleClick();
    }


    /**
     * Adds to toLoad, from info in node.
     * Afterwards, add all children of node to stack to parse (next time
     * collectToLoads is called).
     */
    protected void collectToLoads(TreeNode node) {
        Stack<TreeNode> stack = new Stack<TreeNode>();

        stack.push(node);

        while (!stack.isEmpty()) {
            node = stack.pop();
            String factory = node.getAttribute("factory");
            if (factory != null) { // we need at least a factory
                String artifact = node.getAttribute("artifact-id");
                String out      = node.getAttribute("out");
                String name     = node.getAttribute("facet");
                String ids      = node.getAttribute("ids");
                String displayname = node.getAttribute("name");
                String targetOut = node.getAttribute("target_out");
                String debugAttributeValues = "";
                for (String attr: node.getAttributes()) {
                    debugAttributeValues += ("[" + attr +": "
                        + node.getAttributeAsString(attr) + "] ");
                }
                GWT.log("DatacageWidget.collectToLoad, attributes are "
                    + debugAttributeValues);

                toLoad.add(artifact,
                     factory,
                     out,
                     name,
                     ids,
                     displayname,
                     targetOut);
            }
            TreeNode [] children = tree.getChildren(node);
            if (children != null) {
                for (TreeNode child: children) {
                    stack.push(child);
                }
            }
        }
    }


    /** Get meta-data and populate tree with it. */
    protected void triggerTreeBuilding() {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        String artifactId = artifact.getUuid();
        String userId     = (user != null) ? user.identifier() : null;

        lockUI();

        metaDataService.getMetaData(
            locale,
            artifactId,
            userId,
            outs,
            parameters,
            new AsyncCallback<DataCageTree>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not load meta data.");
                    SC.warn(caught.getMessage());
                    unlockUI();
                }

                @Override
                public void onSuccess(DataCageTree dcTree) {
                    GWT.log("Successfully loaded meta data.");
                    IdGenerator idGenerator = new IdGenerator();
                    DataCageNode dcRoot = dcTree.getRoot();
                    TreeNode root = buildRecursiveChildren(
                        dcRoot, idGenerator);
                    tree.setRoot(root);

                    TreeNode[] nodes = tree.getChildren(root);
                    for (TreeNode node: nodes) {
                        if (node.getAttribute("factory") == null &&
                                !tree.hasChildren(node)) {
                            node.setIsFolder(true);
                        }
                    }

                    if (idGenerator.current() < MAX_OPEN) {
                        tree.openAll();
                    }
                    treeGrid.setData(tree);
                    unlockUI();
                }
            });
    }

    private static final class IdGenerator {
        protected int current;

        public IdGenerator() {
        }

        public int next() {
            return current++;
        }

        public int current() {
            return current;
        }
    } // class IdGenerator

    private String i18n(String s) {
        if (!(s.startsWith("${") && s.endsWith("}"))) {
            return s;
        }

        s = s.substring(2, s.length()-1);

        try {
            return messages.getString(s);
        }
        catch (MissingResourceException mre) {
            GWT.log("cannot find i18n for + '" + s + "'");
            return s;
        }
    }

    protected TreeNode buildRecursiveChildren(
        DataCageNode   node,
        IdGenerator    idGenerator
    ) {
        TreeNode tn = new TreeNode();
        tn.setAttribute("id", idGenerator.next());

        List<DataCageNode> children = node.getChildren();

        if (children != null) {
            TreeNode [] tns = new TreeNode[children.size()];
            for (int i = 0; i < tns.length; ++i) {
                DataCageNode child = children.get(i);
                tns[i] = buildRecursiveChildren(child, idGenerator);
            }
            tn.setAttribute("children-nodes", tns);
        }

        tn.setAttribute("name", i18n(node.getDescription()));
        tn.setAttribute("facet", node.getName());

        AttrList attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0, N = attrs.size(); i < N; ++i) {
                String key   = attrs.getKey(i);
                String value = attrs.getValue(i);
                tn.setAttribute(key, value);
            }
        }

        return tn;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
