/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;


public class SedimentLoadUnit
extends DefaultState
{
   /** The log used in this class. */
    private static Logger log = LogManager.getLogger(SedimentLoadUnit.class);


    /**
     * The default constructor that initializes an empty State object.
     */
    public SedimentLoadUnit() {
    }

    @Override
    protected String getUIProvider() {
        return "minfo.sedimentload_unit_select";
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator ec,
        Artifact                artifact,
        String                  name,
        CallContext             context)
    {
        CallMeta meta = context.getMeta();
        Element[] elements = new Element[2];
        elements[0] = createItem(
                ec,
                new String[] {
                    Resources.getMsg(meta,
                        "state.minfo.t_per_a",
                        "state.minfo.t_per_a"),
                    "t_per_a"});

        elements[1] = createItem(
            ec,
            new String[] {
                Resources.getMsg(meta,
                    "state.minfo.m3_per_a",
                    "state.minfo.m3_per_a"),
                "m3_per_a"});

       return elements;
    }
}
