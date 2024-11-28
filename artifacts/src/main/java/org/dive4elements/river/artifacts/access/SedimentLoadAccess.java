/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import gnu.trove.TIntArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.D4EArtifact;


public class SedimentLoadAccess
extends      RangeAccess
{
    private static final Logger log = LogManager.getLogger(
        SedimentLoadAccess.class);

    private String time;
    private String unit;

    private int [][] epochs;

    private int [] years;

    private Integer sqTiId;

    public SedimentLoadAccess(D4EArtifact artifact) {
        super(artifact);
        years = null;
    }

    public Double getLowerKM() {
        // TODO update callers
        return getFrom();
    }

    public Double getUpperKM() {
        // TODO update callers
        return getTo();
    }

    public String getYearEpoch() {
        if (time == null) {
            time =  getString("ye_select");
        }
        return time;
    }

    /** [year1, years2,..] if its about years. */
    public int[] getYears() {
        if (years != null) {
            return years;
        }
        if (getYearEpoch().equals("year") ) {
            TIntArrayList ints = new TIntArrayList();
            String yearsData = getString("years");
            if (yearsData == null || yearsData.isEmpty()) {
                log.warn("No years provided");
                return null;
            }
            for (String sValue :yearsData.split(" ")) {
                try {
                    ints.add(Integer.parseInt(sValue));
                } catch (NumberFormatException e) {
                    /* Client should prevent this */
                    log.warn("Invalid year value: " + sValue);
                    continue;
                }
            }

            if (!ints.isEmpty()) {
                ints.sort();
                years = ints.toNativeArray();
            }
            return years;
        }
        return null;
    }

    public int[][] getEpochs() {

        if (epochs != null) {
            return epochs;
        }

        if (!getYearEpoch().equals("epoch") &&
            !getYearEpoch().equals("off_epoch")) {
            return null;
        }

        String data = getString("epochs");

        if (data == null) {
            log.warn("No 'epochs' parameter specified!");
            return null;
        }

        String[] parts = data.split(";");

        epochs = new int[parts.length][];

        for (int i = 0; i < parts.length; i++) {
            String[] values = parts[i].split(",");
            TIntArrayList ints = new TIntArrayList();
            try {
                ints.add(Integer.parseInt(values[0]));
                ints.add(Integer.parseInt(values[1]));
                epochs[i] = ints.toNativeArray();
            }
            catch (NumberFormatException nfe) {
                log.warn("Cannot parse int from string: '" + values + "'");
            }
        }
        return epochs;
    }

    /** Returns the selected unit (t/a or m3/a). */
    public String getUnit () {
        if (unit == null) {
            unit = getString("unit");
        }
        return unit;
    }

    /** Returns the selected time interval id */
    public Integer getSQTiId () {
        if (sqTiId == null) {
            sqTiId = getInteger("sq_ti_id");
        }
        return sqTiId;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
