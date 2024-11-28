/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.D4EArtifact;


/**
 * This state is used to realize the input of multiple locations as string.
 *
 * The string should be a whitespace separated list of double values where each
 * double value represents a location.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LocationSelect extends LocationDistanceSelect {

    /** The log used in this class.*/
    private static Logger log = LogManager.getLogger(LocationSelect.class);


    public LocationSelect() {
    }


    /** UI Provider (which input method should the client provide to user. */
    @Override
    protected String getUIProvider() {
        return "location_panel";
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

        if (name.equals(LOCATIONS)) {
            Element min = createItem(
                cr,
                new String[] {"min", new Double(minVal).toString()});

            Element max = createItem(
                cr,
                new String[] {"max", new Double(maxVal).toString()});

            return new Element[] { min, max };
        }

        return null;
    }


    /** Validates data from artifact. */
    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("LocationSelect.validate");

        D4EArtifact flys = (D4EArtifact) artifact;
        StateData    data = getData(flys, LOCATIONS);

        String locationStr = data != null
            ? (String) data.getValue()
            : null;

        if (locationStr == null || locationStr.length() == 0) {
            log.error("No locations given.");
            throw new IllegalArgumentException("error_empty_state");
        }

        double[] minmax = getMinMax(artifact);
        double[] mm     = extractLocations(locationStr);

        log.debug("Inserted min location: " + mm[0]);
        log.debug("Inserted max location: " + mm[mm.length-1]);

        return validateBounds(minmax[0], minmax[1], mm[0], mm[mm.length-1], 0d);
    }


    /**
     * This method takes a string that consist of whitespace separated double
     * values and returns the double values as array.
     *
     * @param locationStr The locations inserted in this state.
     *
     * @return the locations as array.
     */
    protected double[] extractLocations(String locationStr) {
        String[] tmp               = locationStr.split(" ");
        TDoubleArrayList locations = new TDoubleArrayList();

        for (String l: tmp) {
            try {
                locations.add(Double.parseDouble(l));
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe, nfe);
            }
        }

        locations.sort();

        return locations.toNativeArray();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
