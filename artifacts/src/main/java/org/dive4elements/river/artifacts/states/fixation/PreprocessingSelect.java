/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class PreprocessingSelect extends DefaultState {

    /**
     * The default constructor that initializes an empty State object.
     */
    public PreprocessingSelect() {
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator ec,
        Artifact                artifact,
        String                  name,
        CallContext             context)
    {
        CallMeta meta = context.getMeta();
        Element[] elements = new Element[1];
        elements[0] = createItem(
                ec,
                new String[] {
                    Resources.getMsg(meta,
                        "state.fix.preprocess",
                        "state.fix.preprocess"),
                    "preprocess"});

        return elements;
    }


    @Override
    protected String getUIProvider() {
        return "fix.preprocessing_panel";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
