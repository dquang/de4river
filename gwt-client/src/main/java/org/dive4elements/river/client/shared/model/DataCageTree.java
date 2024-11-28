/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.List;

public class DataCageTree implements Serializable
{

    public interface Visitor {
        boolean accept(DataCageNode node);
    } // interface

    protected DataCageNode root;

    public DataCageTree() {
    }

    public DataCageTree(DataCageNode root) {
        this.root = root;
    }

    public void setRoot(DataCageNode root) {
        this.root = root;
    }

    public DataCageNode getRoot() {
        return root;
    }


    protected boolean recursivePrune(DataCageNode node, Visitor visitor) {
        if (!node.hasChildren()) {
            return visitor.accept(node);
        }

        List<DataCageNode> children = node.getChildren();

        for (int i = children.size()-1; i >= 0; --i) {
            if (!recursivePrune(children.get(i), visitor)) {
                children.remove(i);
            }
        }

        return !children.isEmpty();
    }

    public boolean prune(Visitor visitor) {
        return root == null || !root.hasChildren()
            ? true
            : recursivePrune(root, visitor);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
