/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.widgets;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;


public class DischargeTablesChart extends Canvas implements ResizedHandler {

    protected Artifact artifact;

    protected Img img;

    public DischargeTablesChart() {
        super();
    }

    public DischargeTablesChart(Artifact artifact) {
        super();
        this.artifact = artifact;
        init();
    }

    private void init() {
        addChild(createImage());
        addResizedHandler(this);
        setSize("100%", "100%");
    }

    protected Img createImage() {
        img = new Img(getUrl());
        img.setSize("100%", "100%");

        return img;
    }

    protected String getUrl() {
        String url = GWT.getModuleBaseURL();
        url += "dischargetablesoverview";
        url += "?gauge=" + getGauge();
        url += "&format=png";

        String[] timerange = getTimerange();
        url += "&lower=" + timerange[0];
        url += "&upper=" + timerange[1];

        int width = 600;
        int height = 400;
        if (img != null) {
            width = img.getWidth();
            height = img.getHeight();
        }

        url += "&width=" + String.valueOf(width);
        url += "&height=" + String.valueOf(height);

        // add time millis to 'deactivate' caching
        url += "&timemillis=" + System.currentTimeMillis();

        GWT.log("DischargeTablesService URL = '" + url + "'");
        return url;
    }

    protected String getGauge() {
        ArtifactDescription desc = artifact.getArtifactDescription();
        return desc.getReferenceGauge();
    }

    protected String[] getTimerange() {
        ArtifactDescription desc = artifact.getArtifactDescription();
        String yearStr = desc.getDataValueAsString("year_range");

        if (yearStr != null && yearStr.length() > 0) {
            return yearStr.split(";");
        }

        return new String[2];
    }

    @Override
    public void onResized(ResizedEvent event) {
        GWT.log("resized discharge tables overview chart");
        img.setSrc(getUrl());
    }
}
