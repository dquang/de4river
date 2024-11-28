/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.RangeAccess;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * State in which km range is set.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class RangeState extends DefaultState {

    /** The log that is used in this class. */
    private Logger log = LogManager.getLogger(RangeState.class);


    public RangeState() {
    }

    protected abstract double[] getMinMax(Artifact artifact);


    protected boolean validateBounds(
        double fromValid, double toValid,
        double from,      double to)
    throws IllegalArgumentException
    {
        if (from < fromValid) {
            log.error(
                "Invalid 'from'. " + from + " is smaller than " + fromValid);
            // error message used in client to resolve i18n
            throw new IllegalArgumentException("error_feed_from_out_of_range");
        }
        else if (to > toValid) {
            log.error(
                "Invalid 'to'. " + to + " is bigger than " + toValid);
            // error message used in client to resolve i18n
            throw new IllegalArgumentException("error_feed_to_out_of_range");
        }

        return true;
    }


    /**
     * Validates a given range with a given valid range.
     *
     * @param fromValid Valid lower value of the range.
     * @param toValid Valid upper value of the range.
     * @param from The lower value.
     * @param to The upper value.
     * @param step The step width.
     *
     * @return true, if everything was fine, otherwise an exception is thrown.
     */
    protected boolean validateBounds(
        double fromValid, double toValid,
        double from,      double to,      double step)
    throws IllegalArgumentException
    {
        log.debug("RangeState.validateRange");

        // XXX The step width is not validated at the moment!
        return validateBounds(fromValid, toValid, from, to);
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        D4EArtifact flys = (D4EArtifact) artifact;

        try {
            RangeAccess rangeAccess = new RangeAccess(flys);
            double from = rangeAccess.getFrom();
            double to   = rangeAccess.getTo();
            double step = rangeAccess.getStep();

            double[] minmax = getMinMax(flys);

            return validateBounds(minmax[0], minmax[1], from, to, step);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("error_invalid_double_value");
        }
        catch (NullPointerException npe) {
            throw new IllegalArgumentException("error_empty_state");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
