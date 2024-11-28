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
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.artifacts.resources.Resources;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalDischargeState extends DefaultState {

    private static final Logger log =
        LogManager.getLogger(HistoricalDischargeState.class);

    public static final String I18N_MODE_W = "historical.mode.w";
    public static final String I18N_MODE_Q = "historical.mode.q";

    public static final String DATA_MODE   = "historical_mode";
    public static final String DATA_VALUES = "historical_values";
    public static final int    DATA_MODE_W = 0;
    public static final int    DATA_MODE_Q = 1;


    @Override
    protected String getUIProvider() {
        return "wq_simple_array";
    }

    @Override
    protected void appendItems(
        Artifact       artifact,
        ElementCreator creator,
        String         name,
        CallContext    context,
        Element        select
    ) {
        if (name != null && name.equals(DATA_VALUES)) {
            select.setAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI,
                "art:type",
                "doublearray");
        }
        else if (name != null && name.equals(DATA_MODE)) {
            select.setAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI,
                "art:type",
                "intoptions");
        }

        super.appendItems(artifact, creator, name, context, select);
    }


    @Override
    protected Element[] createItems(
        ElementCreator creator,
        Artifact       artifact,
        String         name,
        CallContext    context
    ) {
        log.debug("createItems()");

        if (name != null && name.equals(DATA_MODE)) {
            return createModeItem(creator, artifact, name, context);
        }
        else if (name != null && name.equals(DATA_VALUES)) {
            return createValuesItem(creator, artifact, name, context);
        }

        log.warn("Tried to create item for invalid data: " + name);

        return new Element[0];
    }


    /** Get label for display in client, depending on chosen W or Q input. */
    @Override
    protected String getLabelFor(
        CallContext cc,
        String      name,
        String      value,
        String      type
    ) {
        CallMeta meta = cc.getMeta();

        if (name.equals(DATA_MODE)) {
            if (value.equals(String.valueOf(DATA_MODE_W))) {
                return Resources.getMsg(meta, I18N_MODE_W, I18N_MODE_W);
            }
            else {
                return Resources.getMsg(meta, I18N_MODE_Q, I18N_MODE_Q);
            }
        }
        else {
            return value;
        }
    }


    protected Element[] createModeItem(
        ElementCreator creator,
        Artifact       artifact,
        String         name,
        CallContext    context
    ) {
        log.debug("createModeItem()");

        CallMeta meta = context.getMeta();

        Element modeW = createItem(
            creator,
            new String[] {
                Resources.getMsg(meta, I18N_MODE_W, I18N_MODE_W),
                String.valueOf(DATA_MODE_W) } );

        Element modeQ = createItem(
            creator,
            new String[] {
                Resources.getMsg(meta, I18N_MODE_Q, I18N_MODE_Q),
                String.valueOf(DATA_MODE_Q) } );

        return new Element[] { modeW, modeQ };
    }


    protected Element[] createValuesItem(
        ElementCreator creator,
        Artifact       artifact,
        String         name,
        CallContext    context
    ) {
        log.debug("createValuesItem()");

        Element valuesW = createItem(
            creator, new String[] { "ws", "" } );

        Element valuesQ = createItem(
            creator, new String[] { "qs", "" } );

        return new Element[] { valuesW, valuesQ };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
