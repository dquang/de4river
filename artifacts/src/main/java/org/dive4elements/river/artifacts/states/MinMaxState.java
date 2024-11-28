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

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.ProtocolUtils;

import org.dive4elements.river.artifacts.D4EArtifact;


/**
 * State that holds minimun and maximum (for validation).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class MinMaxState extends DefaultState {

    private static final Logger log = LogManager.getLogger(MinMaxState.class);

    @Override
    protected void appendItems(
        Artifact       artifact,
        ElementCreator creator,
        String         name,
        CallContext    context,
        Element        select
    ) {
        D4EArtifact flys = (D4EArtifact) artifact;

        select.setAttributeNS(
            ArtifactNamespaceContext.NAMESPACE_URI,
            "art:type",
            getType());

        String[] defMinMax = getMinMaxDefaults(artifact, name);

        Element min = ProtocolUtils.createArtNode(
            creator,
            "min",
            new String[] { "value", "default" },
            new String[] { String.valueOf(getLower(flys)), defMinMax[0] });

        Element max = ProtocolUtils.createArtNode(
            creator,
            "max",
            new String[] { "value", "default" },
            new String[] { String.valueOf(getUpper(flys)), defMinMax[1] });

        select.appendChild(min);
        select.appendChild(max);
    }


    /**
     * @param cc
     * @param name
     * @param value
     * @param type
     *
     * @return
     */
    @Override
    protected String getLabelFor(
        CallContext cc,
        String      name,
        String      value,
        String      type
    ) {
        if (type.indexOf("range") > 0) {
            String[] minmax = extractRangeAsString(value);

            if (minmax != null) {
                return minmax[0] + " - " + minmax[1];
            }
        }

        return super.getLabelFor(cc, name, value, type);
    }


    /**
     * Returns a string array with [min,max] from <i>rawValue</i>.
     * <i>rawValue</i> should be a string like "1999;2001".
     *
     * @param rawValue A string with min and max separated by a ';'.
     *
     * @return the min and max as string array ([min,max]).
     */
    protected String[] extractRangeAsString(String rawValue) {
        return rawValue.split(";");
    }


    /**
     * Returns a string array with minimum and maximum inserted by the user as
     * [min,max].
     *
     * @param artifact The D4EArtifact that stores the parameter.
     * @param name The name of the parameter for raw min/max value string.
     *
     * @return a string array [min,max].
     */
    protected String[] getMinMaxByParameter(Artifact artifact, String name) {
        D4EArtifact flys     = (D4EArtifact) artifact;
        String       rawValue = flys.getDataAsString(name);

        if (rawValue == null) {
            log.debug("No value for '" + rawValue + "' existing.");
            return null;
        }

        log.debug("Raw value for '" + name + "' = " + rawValue);

        return extractRangeAsString(rawValue);
    }


    /**
     * Returns a string array with absolute minimum and maximum as [min,max].
     *
     * @param artifact The D4EArtifact (not used in this implementation).
     * @param name The parameter name (not used in this implementation).
     *
     * @return a string array [min,max].
     */
    protected String[] getMinMaxDefaults(Artifact artifact, String name) {
        D4EArtifact flys = (D4EArtifact) artifact;

        Object lower = getLower(flys);
        Object upper = getUpper(flys);

        return new String[] { String.valueOf(lower), String.valueOf(upper) };
    }


    protected abstract Object getLower(D4EArtifact flys);

    protected abstract Object getUpper(D4EArtifact flys);

    protected abstract String getType();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
