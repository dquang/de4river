/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.artifacts.resources.Resources;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RiverSelect extends DefaultState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(RiverSelect.class);

    /** Error message that is thrown if no river was found based on a given
     * name.*/
    public static final String ERROR_NO_SUCH_RIVER =
        "error_feed_no_such_river";

    /** Error message that is thrown if no river was found based on a given
     * name.*/
    public static final String ERROR_NO_RIVER_SELECTED =
        "error_feed_no_river_selected";


    /**
     * The default constructor that initializes an empty State object.
     */
    public RiverSelect() {
    }


    /**
     * Initialize the state based on the state node in the configuration.
     *
     * @param config The state configuration node.
     */
    public void setup(Node config) {
        super.setup(config);
    }


    protected Element createData(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        StateData   data,
        CallContext context)
    {
        Element select = ProtocolUtils.createArtNode(
            cr, "select",
            new String[] { "uiprovider" },
            new String[] { "select_with_map" });
        cr.addAttr(select, "name", data.getName(), true);

        Element label = ProtocolUtils.createArtNode(
            cr, "label", null, null);


        // XXX: DEAD CODE
        /*
        Element choices = ProtocolUtils.createArtNode(
            cr, "choices", null, null);
        */

        select.appendChild(label);

        label.setTextContent(Resources.getMsg(
            context.getMeta(),
            getID(),
            getID()));

        return select;
    }


    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        List<River> rivers = RiverFactory.getRivers();
        Element[] items    = new Element[rivers.size()];

        int idx = 0;
        for (River river: rivers) {
            items[idx++] = createRiverItem(cr, river);
        }

        return items;
    }


    /**
     * This method creates a node that represents a river item. This node
     * contains the label and the value that describe the river.
     *
     * @param cr The ElementCreator.
     * @param river The river.
     *
     * @return the element that contains the information about the river.
     */
    protected Element createRiverItem(XMLUtils.ElementCreator cr, River river) {
        Element item  = ProtocolUtils.createArtNode(cr, "item", null, null);
        Element label = ProtocolUtils.createArtNode(cr, "label", null, null);
        Element value = ProtocolUtils.createArtNode(cr, "value", null, null);

        label.setTextContent(river.getName());
        log.debug("model uuid: " + river.getModelUuid());
        value.setTextContent(river.getModelUuid());

        item.appendChild(label);
        item.appendChild(value);

        return item;
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("RiverSelect.validate");

        D4EArtifact flys = (D4EArtifact) artifact;

        StateData dRiver = getData(flys, "river");

        if (dRiver == null || dRiver.getValue() == null) {
            throw new IllegalArgumentException(ERROR_NO_RIVER_SELECTED);
        }

        River river = RiverFactory.getRiver((String) dRiver.getValue());

        if (river == null) {
            throw new IllegalArgumentException(ERROR_NO_SUCH_RIVER);
        }

        return true;
    }


    @Override
    protected String getUIProvider() {
        return "river_panel";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
