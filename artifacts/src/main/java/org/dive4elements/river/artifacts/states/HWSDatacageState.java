/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.D4EArtifact;


public class HWSDatacageState
extends DefaultState
{

    private static final Logger log = LogManager.getLogger(HWSDatacageState.class);

    @Override
    protected String getUIProvider() {
        return "hws_datacage_panel";
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
        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        Element itemElement = creator.create("item");
        creator.addAttr(itemElement, "value", value, true);

        creator.addAttr(itemElement, "label", getLabel(cc, value), true);
        dataElement.appendChild(itemElement);

        return dataElement;
    }


    public static String getLabel(CallContext cc, String value) {
        log.debug("Create label for value: " + value);

        return value;
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        D4EArtifact flys = (D4EArtifact) artifact;
        String hws = flys.getDataAsString("uesk.hws");
        log.debug("hws: " + hws);
        return true;
    }


    /**
     * Returns the DGM specified in the parameters of <i>flys</i>.
     *
     * @param flys The D4EArtifact that knows the ID of a DGM.
     *
     * @throws IllegalArgumentException If the D4EArtifact doesn't know the ID
     * of a DGM.
     *
     * @return the DGM specified by D4EArtifact's parameters.
     */
    public static String getHWS(D4EArtifact flys)
    throws IllegalArgumentException
    {
        String hws= flys.getDataAsString("uesk.hws");
        if (hws == null) {
            return null;
        }

        log.debug("Found selected hws: '" + hws + "'");

        return hws;
    }



}
