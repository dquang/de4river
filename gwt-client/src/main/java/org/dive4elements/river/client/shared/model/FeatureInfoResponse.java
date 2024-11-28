/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

import java.io.Serializable;


/**
 * @author <a href="mailto:aheinecke@intevation.de">Andre Heinecke</a>
 */
public class FeatureInfoResponse implements Serializable {

    // Wrapper class to transport the response of a feature info call

    protected String featureInfoHTML;

    protected List<FeatureInfo> features;

    public FeatureInfoResponse() {
    }

    public FeatureInfoResponse(
        List<FeatureInfo> features,
        String featureInfoHTML
    ) {
        this.featureInfoHTML = featureInfoHTML;
        this.features = features;
    }

    public List<FeatureInfo> getFeatures() {
        return features;
    }

    public String getFeatureInfoHTML() {
        return featureInfoHTML;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
