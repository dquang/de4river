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

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.access.RiverAccess;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ComputationRangeState
extends      RangeState
implements   FacetTypes
{
    private static Logger log =
        LogManager.getLogger(ComputationRangeState.class);

    /** The default step width. */
    public static final int DEFAULT_STEP = 100;


    public ComputationRangeState() {
    }


    @Override
    protected Element createData(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        StateData   data,
        CallContext context)
    {
        Element select = ProtocolUtils.createArtNode(
            cr, "select", null, null);

        cr.addAttr(select, "name", data.getName(), true);

        Element label = ProtocolUtils.createArtNode(
            cr, "label", null, null);

        label.setTextContent(Resources.getMsg(
            context.getMeta(),
            data.getName(),
            data.getName()));

        select.appendChild(label);

        return select;
    }


    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        double[] minmax = getMinMax(artifact);

        double minVal = Double.MIN_VALUE;
        double maxVal = Double.MAX_VALUE;

        if (minmax != null) {
            minVal = minmax[0];
            maxVal = minmax[1];
        }
        else {
            log.warn("Could not read min/max distance values!");
        }

        if (name.equals("ld_from")) {
            Element min = createItem(
                cr,
                new String[] {"min", new Double(minVal).toString()});

            return new Element[] { min };
        }
        else if (name.equals("ld_to")) {
            Element max = createItem(
                cr,
                new String[] {"max", new Double(maxVal).toString()});

            return new Element[] { max };
        }
        else {
            Element step = createItem(
                cr,
                new String[] {"step", String.valueOf(getDefaultStep())});
            return new Element[] { step };
        }

    }

    @Override
    protected double[] getMinMax(Artifact artifact) {
        return new RiverAccess((D4EArtifact)artifact).getRiver()
            .determineMinMaxDistance();
    }


    protected double getDefaultStep() {
        return DEFAULT_STEP;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
