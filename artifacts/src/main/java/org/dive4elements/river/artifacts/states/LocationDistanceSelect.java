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

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.access.RangeAccess;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LocationDistanceSelect
extends      ComputationRangeState
{

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(LocationDistanceSelect.class);

    /** The name of the 'mode' field. */
    public static final String MODE = "ld_mode";

    /** The name of the 'locations' field. */
    public static final String LOCATIONS = "ld_locations";


    /**
     * The default constructor that initializes an empty State object.
     */
    public LocationDistanceSelect() {
    }


    @Override
    protected String getUIProvider() {
        return "location_distance_panel";
    }


    /** Validates the range (or location). */
    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("LocationDistanceSelect.validate");

        D4EArtifact flys = (D4EArtifact)artifact;
        StateData mode = getData(flys, MODE);
        String mValue = mode != null ? (String)mode.getValue() : null;
        if (mValue != null) {
            if (mValue.equals("distance")) {
                return super.validate(flys);
            }
            else {
                return validateLocations(flys);
            }
        }
        return false;
    }


    /** Validate selected locations. */
    protected boolean validateLocations(D4EArtifact flys)
    throws    IllegalArgumentException
    {
        StateData dValues = getData(flys, LOCATIONS);
        String    values  = dValues != null ? (String)dValues.getValue() : null;

        if (values == null || values.length() == 0) {
            throw new IllegalArgumentException("error_empty_state");
        }

        double[] absMinMax = getMinMax(flys);
        double[] relMinMax = getMinMaxFromString(values);

        if (relMinMax[0] < absMinMax[0] || relMinMax[0] > absMinMax[1]) {
            throw new IllegalArgumentException("error_feed_from_out_of_range");
        }

        if (relMinMax[1] > absMinMax[1] || relMinMax[1] < absMinMax[0]) {
            throw new IllegalArgumentException("error_feed_to_out_of_range");
        }

        return true;
    }


    /**
     * Extracts the min/max values from String <i>s</i>. An
     * IllegalArgumentException is thrown if there is a value that throws a
     * NumberFormatException.
     *
     * @param s String that contains whitespace separated double values.
     *
     * @return a 2dmin array [min,max].
     */
    public static double[] getMinMaxFromString(String s)
    throws IllegalArgumentException
    {
        String[] values = s.split(" ");

        double[] minmax = new double[] {
            Double.MAX_VALUE,
            -Double.MAX_VALUE };

        for (String v: values) {
            try {
                double value = Double.valueOf(v);

                minmax[0] = minmax[0] < value ? minmax[0] : value;
                minmax[1] = minmax[1] > value ? minmax[1] : value;
            }
            catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(
                    "error_invalid_double_value");
            }
        }

        return minmax;
    }


    public static double[] getLocations(WINFOArtifact flys) {
        RangeAccess ra = new RangeAccess(flys);
        return ra.getLocations();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
