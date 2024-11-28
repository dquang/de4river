/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;
/**
 * Facet to access the current Km from the context safely
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class FixingsFacet extends DataFacet {

    public static final Double INVALID_KM = Double.valueOf(-1d);

    public FixingsFacet() {
    }

    public  FixingsFacet(String name, String description) {
        super(0, name, description, ComputeType.ADVANCE, null, null);
    }

    public FixingsFacet(
            int         index,
            String      name,
            String      description,
            ComputeType type,
            String      hash,
            String      stateId
            ) {
        super(index, name, description, type, hash, stateId);
    }

    /**
     * Returns the current km from the context.
     * If the context is null or doesn't contain a currentKm
     * then a double value of -1 will be returned.
     * @param context The CallContext instance
     * @return the current km as double
     */
    protected double getCurrentKm(CallContext context) {
        if (context == null) {
            return INVALID_KM;
        }
        Double dkm = (Double)context.getContextValue(CURRENT_KM);
        if (dkm == null) {
            return INVALID_KM;
        }
        return dkm.doubleValue();
    }
}
