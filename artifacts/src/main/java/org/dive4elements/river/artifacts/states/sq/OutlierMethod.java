/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.sq;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;


public class OutlierMethod
extends DefaultState
{
    /** The log that is used in this class.*/
    private static Logger log = LogManager.getLogger(OutlierMethod.class);

    public static final String STD_DEV   = "outlier.method.std-dev";
    public static final String GRUBBS = "outlier.method.grubbs";

    public static final String[] METHODS = {
        STD_DEV,
        GRUBBS,
  };


    @Override
    protected String getUIProvider() {
        return "";
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        CallMeta meta = context.getMeta();

        Element[] methods = new Element[METHODS.length];

        int i = 0;

        for (String method: METHODS) {
            methods[i++] = createItem(
                cr, new String[] {
                    Resources.getMsg(meta, method, method),
                    method
                });
        }

        return methods;
    }


    @Override
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
