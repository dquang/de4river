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

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.DefaultFacet;


/**
 * Facet that writes artifact-uuid facet name and facet index on the blackboard,
 * delivers data if asked so.
 */
public class BlackboardDataFacet extends DefaultFacet {

    public BlackboardDataFacet() {}

    /** Do not instantiate a BlackboardDataFacet, subclass it instead. */
    public BlackboardDataFacet(int idx, String name, String description) {
        super(idx, name, description);
    }


    /** Do not instantiate a BlackboardDataFacet, subclass it instead. */
    public BlackboardDataFacet(String name, String description) {
        super(0, name, description);
    }


    /** Define key to which to respond when asked for 'blackboard'
     * (DataProvider)- data. */
    public String areaDataKey(Artifact art) {
        return art.identifier() + ":" + getName() + ":" + getIndex();
    }


    /** Hey, We can ArtifactUUID+:+FacetName+:+FacetIndex (i.e. getData)! */
    @Override
    public List getStaticDataProviderKeys(Artifact art) {
        List list = new ArrayList();
        list.add(areaDataKey(art));
        return list;
    }


    /**
     * Can provide whatever getData returns.
     * @param key      will respond on uuid+facetname+index
     * @param param    ignored
     * @param context  ignored
     * @return whatever getData delivers when asked for the 'right' key.
     */
    @Override
    public Object provideBlackboardData(Artifact artifact,
        Object key,
        Object param,
        CallContext context
    ) {
        if (key.equals(areaDataKey(artifact))) {
            return getData(artifact, context);
        }
        else {
            return null;
        }
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        BlackboardDataFacet copy = new BlackboardDataFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
