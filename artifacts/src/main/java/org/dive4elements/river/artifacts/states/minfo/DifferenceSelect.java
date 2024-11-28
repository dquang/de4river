/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.states.WaterlevelPairSelectState;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DifferenceSelect extends WaterlevelPairSelectState {

    /**
     * The default constructor that initializes an empty State object.
     */
    public DifferenceSelect() {
    }

    @Override
    protected String getUIProvider() {
        return "bedheights_twin_panel";
    }

    /**
     * Creates the data element used for the static part of DESCRIBE document.
     */
    @Override
    protected Element createStaticData(
        D4EArtifact   flys,
        ElementCreator creator,
        CallContext    cc,
        String         name,
        String         value,
        String         type
    ) {
        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        Element itemElement = creator.create("item");
        creator.addAttr(itemElement, "value", value, true);

        String[] labels = getLabels(cc, value);

        creator.addAttr(
            itemElement,
            "label",
            labels[0],
            true);
        dataElement.appendChild(itemElement);

        return dataElement;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
