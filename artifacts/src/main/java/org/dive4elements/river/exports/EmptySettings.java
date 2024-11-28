/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.dive4elements.artifactdatabase.state.Settings;
import org.dive4elements.artifactdatabase.state.Section;


/**
 * An implementation of <i>Settings</i> that doesn't take new <i>Section</i>s
 * and that always creates an empty <b>settings</b> DOM node in its
 * <i>toXML()</i> operation.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class EmptySettings implements Settings {

    public EmptySettings() {
    }


    /**
     * This method has no function. It is not implemented!
     *
     * @param section A Section.
     */
    @Override
    public void addSection(Section section) {
        // do nothing
    }


    /**
     * Always returns 0.
     *
     * @return 0.
     */
    @Override
    public int getSectionCount() {
        return 0;
    }


    /**
     * This method always returns null. It is not implemented!
     *
     * @param pos A position.
     *
     * @return null.
     */
    @Override
    public Section getSection(int pos) {
        return null;
    }


    /**
     * This method has no function. It is not implemented!
     */
    @Override
    public void removeSection(Section section) {
        // do nothing
    }


    /**
     * This method creates an empty <i>settings</i> DOM node.
     *
     * @param parent A parent DOM node.
     */
    @Override
    public void toXML(Node parent) {
        Document owner = parent.getOwnerDocument();
        parent.appendChild(owner.createElement("settings"));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
