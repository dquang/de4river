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


/**
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class MultiStringArrayState extends DefaultState {

    private static final Logger log =
        LogManager.getLogger(MultiStringArrayState.class);


    @Override
    protected void appendItems(
        Artifact       artifact,
        ElementCreator creator,
        String         name,
        CallContext    context,
        Element        select
    ) {
        try {
            creator.addAttr(select, "type", "options", true);

            for (KVP kvp: getOptions(artifact, name, context)) {
                Element item = creator.create("item");
                creator.addAttr(item, "label", kvp.getValue().toString(), true);
                creator.addAttr(item, "value", kvp.getKey().toString(), true);

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

        String[] values = value.split(";");

        for (String val: values) {
            Element item = creator.create("item");
            creator.addAttr(item, "value", val, true);
            creator.addAttr(item, "label", getLabelFor(cc, name, val), true);

            data.appendChild(item);
        }

        return data;
    }


    protected abstract KVP<String, String>[] getOptions(
        Artifact artifact,
        String   parameterName,
        CallContext context
    )
    throws IllegalArgumentException;


    protected abstract String getLabelFor(
        CallContext cc,
        String      parameterName,
        String      value
    )
    throws IllegalArgumentException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
