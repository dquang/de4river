/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.minfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.shared.model.Artifact;

public class BedloadCampaignChart extends VLayout {

    private final Artifact artifact;

    protected Img chartImg;

    public BedloadCampaignChart(
        Artifact artifact,
        ResizedHandler resizeHandler
    ) {
        super();

        this.artifact = artifact;
        this.chartImg = new Img();

        addResizedHandler(resizeHandler);
        setAlign(Alignment.CENTER);
    }

    public void update() {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        int hWidth = getWidth() - 12;
        int hHeight = getHeight() - 12;

        if ((int) (hHeight * 4f / 3) < hWidth) {
            hWidth = (int) (hHeight * 4f / 3);
        }
        else {
            hHeight = (int) (hWidth * 3f / 4);
        }

        String river = artifact.getArtifactDescription().getRiver();

        JSONObject jfix = new JSONObject();
        JSONObject jfilter = new JSONObject();
        JSONObject jrName = new JSONObject();
        JSONString jrValue = new JSONString(river);
        JSONObject jextent = new JSONObject();
        JSONNumber jwidth = new JSONNumber(hWidth);
        JSONNumber jheight = new JSONNumber(hHeight);

        jrName.put("name", jrValue);
        jfilter.put("river", jrName);
        jextent.put("width", jwidth);
        jextent.put("height", jheight);
        jfilter.put("extent", jextent);
        jfix.put("bedload", jfilter);
        String filter = jfix.toString();

        String imgUrl = URL.encode(GWT.getModuleBaseURL()
            + "bedload-km-chart"
            + "?locale=" + locale
            + "&filter=" + filter);

        if (chartImg != null && hasMember(chartImg)) {
            chartImg.setWidth(hWidth);
            chartImg.setHeight(hHeight);
            chartImg.setSrc(imgUrl);
        }
        else {
            chartImg = new Img(imgUrl, hWidth, hHeight);
            addMember(chartImg);
        }
    }
}
