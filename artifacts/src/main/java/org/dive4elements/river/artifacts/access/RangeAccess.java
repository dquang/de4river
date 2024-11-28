/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.DoubleUtil;


/** For the moment, light-weight wrapper around RiverUtils. */
// TODO employ 'Caching' like other Accesses, remove usage of RiverUtils.
public class RangeAccess
extends RiverAccess
{
    private static Logger log = LogManager.getLogger(RangeAccess.class);

    public static enum KM_MODE { RANGE, LOCATIONS, NONE };

    /** The default step width between the start end end kilometer. */
    public static final double DEFAULT_KM_STEPS = 0.1;

    double[] kmRange;

    Double from;

    Double to;

    Double step;

    private KM_MODE mode;

    public RangeAccess() {
    }

    public RangeAccess(D4EArtifact artifact) {
        super(artifact);
    }


    /** Evaluate the ld_mode data of artifact. */
    public KM_MODE getKmRangeMode() {
        if (mode != null) {
            return mode;
        }
        String modeData = getString("ld_mode");

        if (modeData == null || modeData.length() == 0) {
            mode = KM_MODE.NONE;
        }
        else if (modeData.equals("distance"))  {
            mode = KM_MODE.RANGE;
        }
        else if (modeData.equals("locations")) {
            mode = KM_MODE.LOCATIONS;
        }
        else {
            mode = KM_MODE.NONE;
        }

        return mode;
    }

    /** Check if the calculation mode is Range. */
    public boolean isRange() {
        return getKmRangeMode() == KM_MODE.RANGE;
    }

    /**
     * Return sorted array of locations at which stuff was calculated
     * (from ld_locations data), null if not parameterized this way.
     */
    public double[] getLocations() {
        String locationStr = getString("ld_locations");

        if (locationStr == null || locationStr.length() == 0) {
            if (getArtifact() instanceof WINFOArtifact) {
                WINFOArtifact winfo = (WINFOArtifact) getArtifact();
                if (winfo.getReferenceStartKm() != null
                    && winfo.getReferenceEndKms() != null
                ) {
                    return new double[]
                        {
                            winfo.getReferenceStartKm().doubleValue(),
                            winfo.getReferenceEndKms()[0]
                        };
                }
                else if (winfo.getReferenceStartKm() != null) {
                    return new double[]
                        {
                            winfo.getReferenceStartKm().doubleValue(),
                            winfo.getReferenceStartKm().doubleValue()
                        };
                }
            }
            return null;
        }

        String[] tmp               = locationStr.split(" ");
        TDoubleArrayList locations = new TDoubleArrayList();

        for (String l: tmp) {
            try {
                locations.add(Double.parseDouble(l));
            }
            catch (NumberFormatException nfe) {
                log.debug(nfe.getLocalizedMessage(), nfe);
            }
        }

        locations.sort();

        return locations.toNativeArray();
    }

    public boolean hasFrom() {
        return from != null || (from = getDouble("ld_from")) != null;
    }

    public boolean hasTo() {
        return to != null || (to = getDouble("ld_to")) != null;
    }

    /* If left_to_right is set to true this returns
     * the smaller value of from and to. */
    public double getFrom(boolean left_to_right) {
        if (!left_to_right) {
            return getFrom();
        }
        double from = getFrom();
        double to = getTo();
        return from > to ? to : from;
    }

    /** Return ld_from data (in km). If not found, the min. */
    public double getFrom() {
        if (from == null) {
            from = getDouble("ld_from");
        }

        if (log.isDebugEnabled()) {
            log.debug("from from data: '" + from + "'");
        }

        if (from == null) {
            log.warn("No 'from' found. Assume min of river.");
            return getRiver().determineMinMaxDistance()[0];
        }

        return from.doubleValue();
    }

    /* If left_to_right is set to true this returns
     * the larger value of from and to. */
    public double getTo(boolean left_to_right) {
        if (!left_to_right) {
            return getTo();
        }
        double from = getFrom();
        double to = getTo();
        return from > to ? from : to;
    }

    /** Return ld_to data (in km), if not found, the max. */
    public double getTo() {
        if (to == null) {
            to = getDouble("ld_to");
        }

        if (log.isDebugEnabled()) {
            log.debug("to from data: '" + to + "'");
        }

        if (to == null) {
            log.warn("No 'to' found. Assume max of river.");
            return getRiver().determineMinMaxDistance()[1];
        }

        return to.doubleValue();
    }


    /** Step width for calculation. */
    public Double getStep() {

        if (step == null) {
            step = getDouble("ld_step");
        }

        if (log.isDebugEnabled()) {
            log.debug("step: '" + step + "'");
        }

        return step;
    }


    /**
     * Get min and max kilometer, independent of parametization
     * (ld_from/to vs ld_locations).
     */
    public double[] getKmRange() {
        // TODO store kmRange in field.
        switch (getKmRangeMode()) {
            case RANGE: {
                return getKmFromTo();
            }

            case LOCATIONS: {
                double[] locs = getLocations();
                // if no locations, nPE.
                if (locs == null) {
                    log.warn("no locations to get km range from.");
                    return new double[] { Double.NaN, Double.NaN };
                }
                return new double[] { locs[0], locs[locs.length-1] };
            }

            case NONE: {
                double[] locs = getLocations();
                if (locs != null) {
                    return new double[] { locs[0], locs[locs.length-1] };
                }
                else {
                    return getKmFromTo();
                }
            }
        }

        return new double[] { Double.NaN, Double.NaN };
    }


    public double[] getKmFromTo() {
         return RiverUtils.getKmFromTo(this.getArtifact());
    }

    /**
     * Returns the selected Kms in steps as specified.
     *
     * @return Each step for this range.
     */
    public double[] getKmSteps() {
        double step = getStep();

        // transform step from 'm' into 'km'
        step = step / 1000;

        if (step == 0d) {
            step = DEFAULT_KM_STEPS;
        }

        return DoubleUtil.explode(getFrom(), getTo(), step);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
