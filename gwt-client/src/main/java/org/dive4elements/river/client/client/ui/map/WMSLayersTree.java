/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

import org.dive4elements.river.client.shared.model.Capabilities;
import org.dive4elements.river.client.shared.model.WMSLayer;


public class WMSLayersTree extends TreeGrid {

    /**
     * An internal TreeNode that stores besides some string attribute a WMSLayer
     * object.
     */
    public static class WMSLayerNode extends TreeNode {

        protected WMSLayer wms;

        public WMSLayerNode(WMSLayer wms) {
            super();
            this.wms = wms;

            setAttribute("name", wms.getName());
            setAttribute("title", wms.getTitle());
        }

        public WMSLayer getWMSLayer() {
            return wms;
        }
    } // end of class WMSLayerNode


    protected Capabilities capabilites;
    protected String       srs;


    public WMSLayersTree(Capabilities capabilites) {
        super();
        this.capabilites = capabilites;

        initTree();
    }


    public WMSLayersTree(Capabilities capabilites, String srs) {
        super();

        this.capabilites = capabilites;
        this.srs         = srs;

        initTree();
    }


    protected void initTree() {
        setLoadDataOnDemand(false);
        setWidth100();
        setHeight100();
        setShowRoot(false);
        setShowConnectors(true);
        setNodeIcon("[SKIN]/images/blank.gif");

        Tree tree = new Tree();
        tree.setChildrenProperty("children-nodes");
        tree.setNameProperty("title");
        tree.setIdField("title");
        tree.setModelType(TreeModelType.CHILDREN);
        tree.setShowRoot(false);

        TreeNode     root = new TreeNode("Root");
        TreeNode[] layers = buildTree(capabilites.getLayers());

        root.setAttribute("children-nodes", layers);
        tree.setRoot(root);

        setData(tree);

        if (layers != null && layers.length == 1) {
            tree.openFolder(layers[0]);
        }
    }


    protected TreeNode[] buildTree(List<WMSLayer> layers) {
        List<TreeNode> layerNodes = new ArrayList<TreeNode>();

        for (WMSLayer layer: layers) {
            WMSLayerNode tn = buildTreeNode(layer);

            if (tn != null) {
                TreeNode[] tns  = buildTree(layer.getLayers());

                if (tns != null && tns.length > 0) {
                    tn.setAttribute("children-nodes", tns);
                }

                layerNodes.add(tn);
            }
        }

        return layerNodes.toArray(new TreeNode[layerNodes.size()]);
    }


    protected WMSLayerNode buildTreeNode(WMSLayer wms) {
        if (srs != null && srs.length() > 0) {
            return wms.supportsSrs(srs) ? new WMSLayerNode(wms) : null;
        }
        else {
            GWT.log("No target SRS specified.");
            return new WMSLayerNode(wms);
        }
    }
}
