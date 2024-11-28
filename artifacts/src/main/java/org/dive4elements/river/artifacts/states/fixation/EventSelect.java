/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.utils.RiverUtils;

import org.dive4elements.river.artifacts.model.FixingsOverview;
import org.dive4elements.river.artifacts.model.FixingsOverviewFactory;
import org.dive4elements.river.artifacts.model.FixingsOverview.IdFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class EventSelect extends DefaultState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(EventSelect.class);


    /**
     * The default constructor that initializes an empty State object.
     */
    public EventSelect() {
    }

    @Override
    protected String getUIProvider() {
        return "fix.event_panel";
    }

    @Override
    protected Element createStaticData(
        D4EArtifact   flys,
        ElementCreator creator,
        CallContext    cc,
        String         name,
        String         value,
        String         type
    ) {
        int[] array = RiverUtils.intArrayFromString(value);

        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        String river = RiverUtils.getRiver(flys).getName();

        FixingsOverview overview = FixingsOverviewFactory.getOverview(river);

        for (int i = 0; i < array.length; i++) {
            Element itemElement = creator.create("item");
            creator.addAttr(
                itemElement,
                "value",
                String.valueOf(array[i]),
                true);

            creator.addAttr(
                itemElement,
                "label",
                getLabel(cc, array[i], overview),
                true);
            dataElement.appendChild(itemElement);
        }
        return dataElement;
    }


    public static String getLabel(
        CallContext cc,
        int value,
        FixingsOverview overview
    ) {
        log.debug("Create label for value: " + value);

        IdFilter filter = new IdFilter(value);
        List<Fixing.Column> columns = overview.filter(null, filter);
        return  columns.isEmpty()
            ? ""
            : columns.get(0).getDescription();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
