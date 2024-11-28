/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.List;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Facet for W-over-Ws. */
public class ReferenceCurveFacet
extends      DataFacet
{
    private static Logger log = LogManager.getLogger(ReferenceCurveFacet.class);

    public static final String CONTEXT_KEY = "reference.curve.axis.scale";

    /** Blackboard data provider key for reference curves start km. */
    public static final String BB_REFERENCECURVE_STARTKM =
        "reference_curve.startkm";

    /** Blackboard data provider key for reference curves end kms. */
    public static final String BB_REFERENCECURVE_ENDKMS =
        "reference_curve.endkms";


    public ReferenceCurveFacet() {
    }


    public ReferenceCurveFacet(int index, String name, String description) {
        super(index, name, description, ComputeType.ADVANCE, null, null);
    }


    public ReferenceCurveFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateID,
        String      hash
    ) {
        super(index, name, description, type, hash, stateID);
    }


    public Object getData(Artifact artifact, CallContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Get data for reference curve at index: " + index +
                " /stateId: " + stateId);
        }

        return getWWQQ(artifact, context);
    }


   /**
     * Can provide parameters of reference curve
     * @param key      will respond on BB_REFERENCECURVE START/ENDKMS
     * @param param    ignored
     * @param context  ignored
     * @return whatever parameters for reference curve
     */
    @Override
    public Object provideBlackboardData(Artifact artifact,
        Object key,
        Object param,
        CallContext context
    ) {
        WINFOArtifact winfo = (WINFOArtifact) artifact;
        if (key.equals(BB_REFERENCECURVE_STARTKM)) {
            return winfo.getReferenceStartKm();
        }
        else if (key.equals(BB_REFERENCECURVE_ENDKMS)) {
            return winfo.getReferenceEndKms();
        }
        else {
            return null;
        }
    }


    protected WWQQ getWWQQ(Artifact artifact, CallContext context) {
        D4EArtifact winfo = (D4EArtifact)artifact;

        CalculationResult res = (CalculationResult)
            winfo.compute(context, hash, stateId, type, false);

        return ((WWQQ [])res.getData())[index];
    }


    @Override
    public void set(Facet other) {
        super.set(other);
        ReferenceCurveFacet o = (ReferenceCurveFacet)other;
        type                  = o.type;
        hash                  = o.hash;
        stateId               = o.stateId;
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        ReferenceCurveFacet copy = new ReferenceCurveFacet();
        copy.set(this);
        return copy;
    }


    @Override
    public List getStaticDataProviderKeys(Artifact art) {
        List list = new ArrayList();
        list.add(BB_REFERENCECURVE_STARTKM);
        list.add(BB_REFERENCECURVE_ENDKMS);
        return list;
    }


    @Override
    public List getDataProviderKeys(Artifact art, CallContext context) {

        // compute / get data
        Object obj = context.getContextValue(CONTEXT_KEY);

        if (!(obj instanceof WWAxisTypes)) {
            obj = new WWAxisTypes(getWWQQ(art, context));
            context.putContextValue(CONTEXT_KEY, obj);
        }
        else {
            ((WWAxisTypes)obj).classify(getWWQQ(art, context));
        }

        return getStaticDataProviderKeys(art);//Collections.emptyList();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
