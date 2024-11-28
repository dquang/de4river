/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class YearEpochSelect extends DefaultState {

    /**
     * The default constructor that initializes an empty State object.
     */
    public YearEpochSelect() {
    }

    @Override
    protected String getUIProvider() {
        return "minfo.bed.year_epoch";
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator ec,
        Artifact                artifact,
        String                  name,
        CallContext             context)
    {
        CallMeta meta = context.getMeta();
        Element[] elements;

        /* Disable calculation of official epochs
        if (((D4EArtifact)artifact).getCurrentStateId()
            .equals("state.minfo.sediment.load.year_epoch")
        ) {
            elements = new Element[3];
            elements[2] = createItem(
                ec,
                new String[] {
                    Resources.getMsg(meta,
                        "state.minfo.off_epoch",
                        "state.minfo.off_epoch"),
                    "off_epoch"});
        }
        else {
            elements = new Element[2];
            } */
        elements = new Element[2];
        elements[0] = createItem(
                ec,
                new String[] {
                    Resources.getMsg(meta,
                        "state.minfo.year",
                        "state.minfo.year"),
                    "year"});

        elements[1] = createItem(
            ec,
            new String[] {
                Resources.getMsg(meta,
                    "state.minfo.epoch",
                    "state.minfo.epoch"),
                "epoch"});

       return elements;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
