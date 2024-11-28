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

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.artifacts.common.model.KVP;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.utils.RiverUtils;


/**
 * State that holds minimun and maximum (for validation).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class MultiIntArrayState extends DefaultState {

    private static final Logger log =
        LogManager.getLogger(MultiIntArrayState.class);


    @Override
    protected void appendItems(
        Artifact       artifact,
        ElementCreator creator,
        String         name,
        CallContext    context,
        Element        select
    ) {
        try {
            creator.addAttr(select, "type", "intoptions", true);

            for (KVP kvp: getOptions(artifact, name)) {
                Element item = creator.create("item");
                creator.addAttr(
                    item, "label", kvp.getValue().toString(), true);
                creator.addAttr(
                    item, "value", kvp.getKey().toString(), true);

                select.appendChild(item);
            }
        }
        catch (IllegalArgumentException iae) {
            log.warn("Illegal argument", iae);
        }
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
        Element data = creator.create("data");
        creator.addAttr(data, "name",  name, true);
        creator.addAttr(data, "type",  type, true);
        creator.addAttr(data, "label",
            Resources.getMsg(cc.getMeta(), name, name), true);

        int[] values = RiverUtils.intArrayFromString(value);

        for (int val: values) {
            try {
                Element item = creator.create("item");
                creator.addAttr(
                    item, "value", String.valueOf(val), true);
                creator.addAttr(
                    item, "label", getLabelFor(cc, name, val), true);

                data.appendChild(item);
            }
            catch (IllegalArgumentException iae) {
                log.warn("Cannot append item: " + val, iae);
            }
        }

        return data;
    }


    protected abstract KVP<Integer, String>[] getOptions(
        Artifact artifact,
        String   parameterName
    )
    throws IllegalArgumentException;


    protected abstract String getLabelFor(
        CallContext cc,
        String      parameterName,
        int         value)
    throws IllegalArgumentException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
