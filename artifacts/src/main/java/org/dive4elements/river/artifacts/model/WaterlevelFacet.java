/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.math.Linear;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.artifacts.model.extreme.ExtremeResult;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Facet of a Waterlevel (WQKms).
 */
public class WaterlevelFacet extends DataFacet {

    private static Logger log = LogManager.getLogger(WaterlevelFacet.class);

    public WaterlevelFacet(int index, String name, String description) {
        super(index, name, description, ComputeType.ADVANCE, null, null);
    }

    public WaterlevelFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateID,
        String      hash
    ) {
        // Pretty weird to change order of the two String params
        // hash and stateID in comparison to parent. Its difficult to fix, now.
        super(index, name, description, type, hash, stateID);
    }

    public WaterlevelFacet() {
    }

    protected WQKms [] getWQKms(CalculationResult res) {
        if (res.getData() instanceof ExtremeResult)
            return ((ExtremeResult) res.getData()).getWQKms();
        else if (res.getData() instanceof WQKms[]) {
            return (WQKms []) res.getData();
        }
        else {
            log.error("WaterlevelFacet got wrong data type " + res.getData());
            return null;
        }
    }

    /**
     * Get waterlevel data.
     * @return a WQKms at given index.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Get data for waterlevels at index: " + index +
                " /stateId: " + stateId);
        }

        if (artifact == null) {
            log.error("WaterlevelFacet.getData: artifact is null");
            return null;
        }

        D4EArtifact winfo = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult)
            winfo.compute(context, hash, stateId, type, false);

        if (res == null) {
            log.error("WaterlevelFacet.getData: null result");
            return null;
        }

        WQKms [] wqkms = getWQKms(res);
        Object KM = context.getContextValue("currentKm");

        // Interpolation.
        if (KM != null) {
            linearInterpolate(wqkms[index], (Double) KM);
        }
        else {
            log.debug("Do not interpolate.");
        }

        return wqkms != null ? wqkms[index] : null;
    }


    /**
     * Linear interpolation of WQKms.
     * TODO rewrite.
     * @return [w, q, km]
     */
    public WQKms linearInterpolate(WQKms wqkms, double km) {
        log.debug("interpolate at given km (" + km + ")");

        WQKms resultWQKms = new WQKms();
        int size = wqkms.size();
        boolean kmIncreasing = wqkms.getKm(0) < wqkms.getKm(size-1);
        int mod = kmIncreasing ? +1 : -1;
        int idx = 0;
        // Move idx to closest from one direction, check for match.
        if (!kmIncreasing) {
            while (idx < size && wqkms.getKm(idx) < km) {
                if (Math.abs(wqkms.getKm(idx) - km) < 0.01d) {
                    resultWQKms.add(
                        wqkms.getW(idx), wqkms.getQ(idx), wqkms.getKm(idx));
                    return resultWQKms;
                }
                idx++;
            }
        }
        else {
            idx = size-1;
            while (idx > 0 && wqkms.getKm(idx) > km) {
                if (Math.abs(wqkms.getKm(idx) - km) < 0.01d) {
                    resultWQKms.add(
                        wqkms.getW(idx), wqkms.getQ(idx), wqkms.getKm(idx));
                    return resultWQKms;
                }
                idx--;
            }
        }
        if (Math.abs(wqkms.getKm(idx) - km) < 0.01d) {
            resultWQKms.add(
                wqkms.getW(idx), wqkms.getQ(idx), wqkms.getKm(idx));
            return resultWQKms;
        }

        if ((idx != -1)
            && (idx < size)
            && (idx - mod != -1)
            && (idx - mod < size)
        ) {
            double inW = Linear.linear(
                km,
                wqkms.getKm(idx), wqkms.getKm(idx - mod),
                wqkms.getW(idx), wqkms.getW(idx - mod));
            double inQ = Linear.linear(
                km,
                wqkms.getKm(idx), wqkms.getKm(idx - mod),
                wqkms.getQ(idx), wqkms.getQ(idx - mod));
            resultWQKms.add(inW, inQ, km);
        }
        else {
            log.debug("waterlevelfacet stuff " + idx
                + " size " + size + " mod: " + mod);
        }

        return resultWQKms;
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        WaterlevelFacet copy = new WaterlevelFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
