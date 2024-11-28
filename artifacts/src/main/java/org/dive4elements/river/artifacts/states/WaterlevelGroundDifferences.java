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

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifactdatabase.ProtocolUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WaterlevelGroundDifferences extends RangeState {

    public static final String LOWER_FIELD  = "diff_from";
    public static final String UPPER_FIELD  = "diff_to";
    public static final String DIFF_FIELD   = "diff_diff";

    public static final double DEFAULT_STEP = 0d;


    private static Logger log =
        LogManager.getLogger(WaterlevelGroundDifferences.class);


    protected String getLowerField() {
        return LOWER_FIELD;
    }


    protected String getUpperField() {
        return UPPER_FIELD;
    }


    protected String getStepField() {
        return DIFF_FIELD;
    }


    @Override
    protected double[] getMinMax(Artifact artifact) {
        return new double[] { -Double.MAX_VALUE, Double.MAX_VALUE };
    }


    @Override
    protected String getUIProvider() {
        return "waterlevel_ground_panel";
    }


    protected double getDefaultStep() {
        return DEFAULT_STEP;
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

        if (name.equals(LOWER_FIELD)) {
            Element min = createItem(
                cr,
                new String[] {"min", new Double(minVal).toString()});

            return new Element[] { min };
        }
        else if (name.equals(UPPER_FIELD)) {
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

    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        D4EArtifact flys = (D4EArtifact) artifact;

        StateData dFrom = getData(flys, getLowerField());
        StateData dTo   = getData(flys, getUpperField());
        StateData dStep = getData(flys, getStepField());

        String fromStr = dFrom != null ? (String) dFrom.getValue() : null;
        String toStr   = dTo   != null ? (String) dTo.getValue()   : null;
        String stepStr = dStep != null ? (String) dStep.getValue() : null;

        if (fromStr == null || toStr == null || stepStr == null) {
            throw new IllegalArgumentException("error_empty_state");
        }

        try {
            double from = Double.parseDouble(fromStr);
            double to   = Double.parseDouble(toStr);
            double step = Double.parseDouble(stepStr);

            double[] minmax = getMinMax(flys);

            return validateBounds(minmax[0], minmax[1], from, to, step);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("error_invalid_double_value");
        }
    }

    @Override
    protected boolean validateBounds(
        double fromValid, double toValid,
        double from,      double to
    ) throws IllegalArgumentException {
        if (to < 0d) {
            log.error(
                "Invalid 'to' " + to + " is lesser than zero.");
            throw new IllegalArgumentException("error_feed_from_out_of_range");
        }
        return super.validateBounds(fromValid, toValid, from, to);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
