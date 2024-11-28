/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Capabilities of a WMS.
 */
public class Capabilities implements Serializable {

    protected String title;
    protected String onlineResource;
    protected String fees;
    protected String accessConstraints;

    protected ContactInformation contactInformation;

    protected List<WMSLayer> layers;
    protected List<String> mapFormats;


    public Capabilities() {
        layers = new ArrayList<WMSLayer>();
    }


    /**
     * @param fees
     * @param accessConstraints
     * @param layers
     */
    public Capabilities(
        String             title,
        String             onlineResource,
        ContactInformation contactInformation,
        String             fees,
        String             accessConstraints,
        List<WMSLayer>     layers,
        List<String>       mapFormats
    ) {
        this.title              = title;
        this.onlineResource     = onlineResource;
        this.contactInformation = contactInformation;
        this.fees               = fees;
        this.accessConstraints  = accessConstraints;
        this.layers             = layers;
        this.mapFormats         = mapFormats;
    }


    public String getTitle() {
        return title;
    }


    public String getOnlineResource() {
        return onlineResource;
    }


    public ContactInformation getContactInformation() {
        return contactInformation;
    }


    public String getFees() {
        return fees;
    }


    public String getAccessConstraints() {
        return accessConstraints;
    }


    public List<WMSLayer> getLayers() {
        return layers;
    }


    public List<String> getMapFormats() {
        return mapFormats;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Capabilities ---\n");
        sb.append("Title:.............. " + title + "\n");
        sb.append("Online Resource:.... " + onlineResource + "\n");
        sb.append("Contact Information: " + contactInformation + "\n");
        sb.append("Fees:............... " + fees + "\n");
        sb.append("Access Constraints:. " + accessConstraints + "\n");
        sb.append("Layers: ");

        for (WMSLayer layer: layers) {
            sb.append("   - " + layer + "\n");
        }

        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
