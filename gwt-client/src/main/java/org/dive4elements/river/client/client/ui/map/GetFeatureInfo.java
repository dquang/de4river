/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.event.MapClickListener;

import org.dive4elements.river.client.shared.model.FeatureInfo;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.GFIService;
import org.dive4elements.river.client.client.services.GFIServiceAsync;
import org.dive4elements.river.client.shared.model.FacetRecord;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.AttributedTheme;
import org.dive4elements.river.client.shared.model.FeatureInfoResponse;
import org.dive4elements.river.client.client.ui.ThemePanel;


public class GetFeatureInfo implements MapClickListener {

    protected GFIServiceAsync gfiService = GWT.create(GFIService.class);

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected GetFeatureInfoWindow gfiWindow;

    protected Map        map;
    protected ThemePanel themePanel;
    protected String     infoFormat;


    /**
     * @param map
     * @param themes
     * @param url
     * @param infoFormat
     */
    public GetFeatureInfo(Map map, ThemePanel themePanel, String infoFormat) {
        this.map        = map;
        this.themePanel = themePanel;
        this.infoFormat = infoFormat;
    }


    public void activate(boolean activate) {
        if (activate) {
            map.addMapClickListener(this);
        }
        else {
            map.removeListener(this);
        }
    }


    protected void newGetFeatureInfoWindow(
        List<FeatureInfo> features,
        String title
    ) {
        if (gfiWindow != null) {
            gfiWindow.destroy();
        }

        gfiWindow = new GetFeatureInfoWindow(features, title);
        gfiWindow.show();
    }

    protected void newGetFeatureInfoWindow(String response, String title) {
        if (gfiWindow != null) {
            gfiWindow.destroy();
        }

        gfiWindow = new GetFeatureInfoWindow(response, title);
        gfiWindow.show();
    }

    @Override
    public void onClick(MapClickListener.MapClickEvent e) {
        LonLat lonlat = e.getLonLat();
        Pixel  pixel  = map.getPixelFromLonLat(lonlat);

        if (themePanel.getSelectedRecords().length == 0) {
            SC.say(MSG.requireTheme());
        }

        for (ListGridRecord rec : themePanel.getSelectedRecords()) {
            Theme act_theme = ((FacetRecord)rec).getTheme();
            final AttributedTheme at = (AttributedTheme)act_theme;
            gfiService.query(
                act_theme,
                infoFormat,
                map.getExtent().toString(),
                map.getProjection(),
                (int) map.getSize().getHeight(),
                (int) map.getSize().getWidth(),
                pixel.x(), pixel.y(),
                new AsyncCallback<FeatureInfoResponse>() {
                    @Override
                    public void onFailure(Throwable e) {
                        SC.warn(MSG.getString(e.getMessage()));
                    }

                    @Override
                    public void onSuccess(FeatureInfoResponse response) {
                        List<FeatureInfo> features = response.getFeatures();
                        if (features != null && !features.isEmpty()) {
                            newGetFeatureInfoWindow(
                                features, at.getAttr("description"));
                        } else if (response.getFeatureInfoHTML() != null) {
                            newGetFeatureInfoWindow(
                                response.getFeatureInfoHTML(),
                                at.getAttr("description"));
                        } else {
                            GWT.log("GetFeatureInfo returned neither "
                                + "a list of features nor a string");
                        }
                    }
                }
            );
            break; // More intelligent handling when more then one is selected
        }
    }
}
