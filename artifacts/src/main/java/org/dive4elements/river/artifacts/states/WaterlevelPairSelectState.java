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

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.backend.utils.StringUtil;

/**
 * State in which the user selects 1 to n pairs of Waterlevels and alikes.
 */
public class WaterlevelPairSelectState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(
         WaterlevelPairSelectState.class);


    /** Trivial constructor. */
    public WaterlevelPairSelectState() {
    }


    /** Specify to display a datacage_twin_panel. */
    @Override
    protected String getUIProvider() {
        return "datacage_twin_panel";
    }


    /**
     * Overridden to do nothing.
     */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        //Get data and do stuff, do not calculate
        return "";
    }


    /**
     * Create elements for document (prepopulated with data, if any).
     * @param artifact D4EArtifact to get data from.
     * @param name DataName, expceted to be "diffids".
     */
    @Override
    protected Element[] createItems(
        ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        log.debug("createItems: " + name);
        if (name.equals("diffids")) {
            Element item  = ProtocolUtils.createArtNode(
                cr, "item", null, null);
            Element label = ProtocolUtils.createArtNode(
                cr, "label", null, null);
            Element value = ProtocolUtils.createArtNode(
                cr, "value", null, null);
            D4EArtifact flys = (D4EArtifact) artifact;
            String s = flys.getDataAsString("diffids");
            value.setTextContent(s);
            item.appendChild(label);
            item.appendChild(value);
            return new Element[] { item };
        }
        return new Element[] {};
    }


    /**
     * Creats the data element used for the static part of DESCRIBE document.
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
        Object[] obj    = new Object[] { labels[0] };

        // TODO own i18n
        String attrValue = Resources.getMsg(
            cc.getMeta(), "wsp.selected.string", "wsp.selected.string", obj);
        //I18N_STATIC_KEY, I18N_STATIC_KEY, obj);

        creator.addAttr(itemElement, "label", attrValue, true);
        dataElement.appendChild(itemElement);

        return dataElement;
    }


    /**
     * Get name to display for selected watelerlevels (for example "Q=123")
     * from the CalculationResult.
     */
    public static String[] getLabels(CallContext cc, String value) {
        String[] recommendations = value.split("#");
        String displayString = "";

        // Walk over all selected recommendations and create label
        // like "W (Q=1) - W (Q=2)".
        for (int i = 0; i < recommendations.length; i+=2) {
            String[] minuendParts = StringUtil
                .unbracket(recommendations[i+0])
                .split(";");
            if(minuendParts.length >= 4) {
                displayString += "(" + minuendParts[3];
            }
            else {
                displayString += "([error]";
            }

            displayString += " - ";

            String[] subtrahendParts = StringUtil
                .unbracket(recommendations[i+1])
                .split(";");
            if(subtrahendParts.length >= 4) {
                displayString += subtrahendParts[3] + ") ";
            }
            else {
                displayString += "[error])";
            }
        }

        return new String[] { displayString };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
