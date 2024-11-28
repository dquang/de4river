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

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.SedimentLoadAccess;
import org.dive4elements.river.artifacts.states.DefaultState;


/** State in which to fetch years for sedminent load calculation. */
public class SedimentLoadYearSelect
extends DefaultState
{
    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(SedimentLoadYearSelect.class);


    /**
     * The default constructor that initializes an empty State object.
     */
    public SedimentLoadYearSelect() {
    }


    /** Year Select Widget. */
    @Override
    protected String getUIProvider() {
        return "minfo.sedimentload_year_select";
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        SedimentLoadAccess access =
            new SedimentLoadAccess((D4EArtifact)artifact);

        // Second year should be later than first.
        if (access.getYears() == null || access.getYears().length == 0)
           throw new IllegalArgumentException("error_years_wrong");

        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
