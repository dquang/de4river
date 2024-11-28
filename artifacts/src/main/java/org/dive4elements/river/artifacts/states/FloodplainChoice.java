/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.ProtocolUtils;

import org.dive4elements.river.artifacts.resources.Resources;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FloodplainChoice extends DefaultState {

    public static final String OPTION   = "floodplain.option";
    public static final String ACTIVE   = "floodplain.active";
    public static final String INACTIVE = "floodplain.inactive";

    private static final Logger log =
        LogManager.getLogger(FloodplainChoice.class);


    @Override
    protected String getUIProvider() {
        return "boolean_panel";
    }


    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        CallMeta meta = context.getMeta();

        Element option = createItem(
            cr,
            new String[] { Resources.getMsg(meta, OPTION, OPTION), "true" });

        return new Element[] { option };
    }


    @Override
    protected String getLabelFor(
        CallContext cc,
        String      name,
        String      value,
        String      type
    ) {
        log.debug("GET LABEL FOR '" + name + "' / '" + value + "'");
        if (value != null && value.equals("true")) {
            return Resources.getMsg(cc.getMeta(), ACTIVE, ACTIVE);
        }
        else {
            return Resources.getMsg(cc.getMeta(), INACTIVE, INACTIVE);
        }
    }


    protected Element createItem(XMLUtils.ElementCreator cr, Object obj) {
        Element item  = ProtocolUtils.createArtNode(cr, "item", null, null);
        Element label = ProtocolUtils.createArtNode(cr, "label", null, null);
        Element value = ProtocolUtils.createArtNode(cr, "value", null, null);

        String[] arr = (String[]) obj;

        label.setTextContent(arr[0]);
        value.setTextContent(arr[1]);

        item.appendChild(label);
        item.appendChild(value);

        return item;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
