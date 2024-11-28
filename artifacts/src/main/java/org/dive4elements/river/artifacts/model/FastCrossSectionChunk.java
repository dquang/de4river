/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;
import java.util.Collections;

import java.io.Serializable;

import org.dive4elements.river.model.CrossSection;

import org.dive4elements.river.model.FastCrossSectionLine;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Number of FastCrossSectionLines, e.g. to store in cache and retrieve
 * a single contain CrossSectionLine by its km.
 */
public class FastCrossSectionChunk
implements   Serializable
{
    private static Logger log = LogManager.getLogger(FastCrossSectionChunk.class);

    public static final String PREFIX = "FCSC:";
    public static final double KM_RANGE = 1.0;

    protected double startKm;
    protected int    crossSectionId;

    protected List<FastCrossSectionLine> crossSectionLines;

    public FastCrossSectionChunk() {
    }

    public FastCrossSectionChunk(CrossSection cs, double km) {

        crossSectionId = cs.getId();
        startKm = Math.floor(km);
        double stopKm = startKm + KM_RANGE;

        long startTime = System.currentTimeMillis();

        crossSectionLines = cs.getFastLines(startKm, stopKm);

        long stopTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Fetching cross section lines took " +
                (float)(stopTime-startTime)/1000f + " secs.");
        }
    }

    /** Finds the FastCrossSectionLine at km (null if not found). */
    public FastCrossSectionLine getCrossSectionLine(double km) {
        FastCrossSectionLine key = new FastCrossSectionLine(km);
        int pos = Collections.binarySearch(
            crossSectionLines, key, FastCrossSectionLine.KM_CMP);
        return pos < 0 ? null : crossSectionLines.get(pos);
    }

    public static String createHashKey(CrossSection cs, double km) {
        return PREFIX + cs.getId() + ":" + (int)Math.floor(km);
    }

    public String getHashKey() {
        return PREFIX + crossSectionId + ":" + (int)Math.floor(startKm);
    }

    public double getStartKm() {
        return startKm;
    }

    public void setStartKm(double startKm) {
        this.startKm = startKm;
    }

    public double getStopKm() {
        return startKm + KM_RANGE;
    }

    public int getCrossSectionId() {
        return crossSectionId;
    }

    public void setCrossSectionId(int crossSectionId) {
        this.crossSectionId = crossSectionId;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
