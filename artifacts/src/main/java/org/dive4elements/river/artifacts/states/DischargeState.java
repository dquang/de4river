/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.model.KVP;

import org.dive4elements.river.model.DischargeZone;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.utils.RiverUtils;


public class DischargeState extends MultiIntArrayState {

    public static final String MAIN_CHANNEL  = "main_channel";
    public static final String TOTAL_CHANNEL = "total_channel";


    private static final Logger log = LogManager.getLogger(DischargeState.class);


    /** Let client display a matrix. */
    @Override
    public String getUIProvider() {
        return "parameter-matrix";
    }


    /**
     * This method fetches all DischargeZones for a given river (extracted from
     * <i>artifact</i>) and returns a KVP[] where the key is the ID of the
     * DischargeZone and the value is a string that consists of lower discharge
     * and upper discharge.
     *
     * @param artifact Needs to be a D4EArtifact that provides river
     * information.
     * @param parameterName The name of a parameter.
     *
     * @return a KVP[].
     */
    @Override
    protected KVP<Integer, String>[] getOptions(
        Artifact artifact,
        String   parameterName
    )
    throws IllegalArgumentException
    {
        if (!testParameterName(parameterName)) {
            throw new IllegalArgumentException(
                "Invalid parameter for state: '" + parameterName + "'");
        }

        List<DischargeZone> zones = getDischargeZones(artifact);

        KVP[] kvp = new KVP[zones.size()];

        Collections.sort(zones, new Comparator<DischargeZone>() {
            @Override
            public int compare(DischargeZone a, DischargeZone b) {
                return a.getValue().compareTo(b.getValue());
            }
        });

        int i = 0;

        for (DischargeZone zone: zones) {
            String lower = zone.getLowerDischarge();
            String upper = zone.getUpperDischarge();

            if (lower.equals(upper)) {
                kvp[i] = new KVP(zone.getId(), lower);
            }
            else {
                kvp[i] = new KVP(zone.getId(), lower + " - " + upper);
            }
            i++;
        }

        return kvp;
    }


    @Override
    protected String getLabelFor(
        CallContext cc,
        String      parameterName,
        int         value
    ) throws IllegalArgumentException
    {
        if (!testParameterName(parameterName)) {
            throw new IllegalArgumentException(
                "Invalid parameter for state: '" + parameterName + "'");
        }

        DischargeZone zone = DischargeZone.getDischargeZoneById(value);

        if (zone == null) {
            throw new IllegalArgumentException(
                "Invalid id for DischargeZone: '" + value + "'");
        }

        String lo = zone.getLowerDischarge();
        String hi = zone.getUpperDischarge();

        return hi != null && !lo.equals(hi)
            ? lo + " - " + hi
            : lo;
    }


    /**
     * This method might be used to test, if a parameter name is handled by this
     * state.
     *
     * @param parameterName The name of a parameter.
     *
     * @return true, if parameterName is one of <i>MAIN_CHANNEL</i> or
     * <i>TOTAL_CHANNEL</i>. Otherwise false.
     */
    protected boolean testParameterName(String parameterName) {
        if (parameterName == null || parameterName.length() == 0) {
            return false;
        }
        else if (parameterName.equals(MAIN_CHANNEL)) {
            return true;
        }
        else if (parameterName.equals(TOTAL_CHANNEL)) {
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * Returns all discharge zones for a given river. The river information is
     * extracted from <i>artifact</i> using RiverUtils.getRiver().
     *
     * @param artifact Needs to be a D4EArtifact that stores a rivername.
     *
     * @return a list of DischargeZones.
     *
     * @throws IllegalArgumentException if no river information is provided by
     * <i>artifact</i>.
     */
    protected List<DischargeZone> getDischargeZones(Artifact artifact)
    throws IllegalArgumentException
    {
        River river = RiverUtils.getRiver((D4EArtifact) artifact);

        if (river == null) {
            throw new IllegalArgumentException("No river found");
        }

        List<DischargeZone> zones = DischargeZone.getDischargeZones(river);

        log.debug("Found " + zones.size() + " DischargeZones.");

        return zones;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
