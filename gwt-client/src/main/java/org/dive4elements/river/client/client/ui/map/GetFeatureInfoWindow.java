/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Window;

import com.smartgwt.client.widgets.layout.VLayout;

import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import com.smartgwt.client.widgets.viewer.DetailViewerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.shared.model.FeatureInfo;

import org.gwtopenmaps.openlayers.client.feature.VectorFeature;

import org.gwtopenmaps.openlayers.client.util.Attributes;
import org.gwtopenmaps.openlayers.client.util.JSObject;

public class GetFeatureInfoWindow extends Window {

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected List<FeatureInfo> features;

    protected String title;

    protected String featureInfoHTML;


    public static final int ROW_HEIGHT = 25;


    public GetFeatureInfoWindow(List<FeatureInfo> features, String title) {
        super();
        this.features = features;
        this.title = title;

        initLayout();
    }

    public GetFeatureInfoWindow(String featureInfoHTML, String title) {
        super();
        features = null;
        this.title = title;
        this.featureInfoHTML = featureInfoHTML;

        initLayoutHTML();
    }

    protected void initLayoutHTML() {
        HTMLPane pane = new HTMLPane();
        pane.setContents(featureInfoHTML);
        addItem(pane);
        setWidth(500);
        setHeight(300);

        setTitle(MSG.getFeatureInfoWindowTitle() + " " + title);

        setIsModal(false);
//        setShowModalMask(true);

        centerInPage();
    }

    protected void initLayout() {
        VLayout root = new VLayout();

        for (FeatureInfo feature: features) {
            // Currently this should alway be only one
            root.addMember(createFeatureViewer(feature));
            setTitle(MSG.getFeatureInfoWindowTitle() + " " + title);
        }

        addItem(root);

        setWidth(500);
        setHeight(300);

        setIsModal(false);
//        setShowModalMask(true);

        centerInPage();
    }


    protected DetailViewer createFeatureViewer(FeatureInfo feature) {
        DetailViewer detailViewer = new DetailViewer();
        detailViewer.setWidth(487);

        Map<String, String> attrs = feature.getAttrs();
        Set<Map.Entry<String, String>> entries = attrs.entrySet();
        List <DetailViewerField> fields = new ArrayList<DetailViewerField>();
        DetailViewerRecord dr = new DetailViewerRecord();

        DetailViewerField path_field = null;
        // Make sure path is always the last element

        for (Map.Entry<String, String> entry: entries) {
            String localized;
            try {
                localized = MSG.getString(entry.getKey());
            } catch (MissingResourceException mre) {
                localized = entry.getKey();
//                We filter unwanted information by localization
                continue;
            }
            if (entry.getKey().equals("PATH")) {
                path_field = new DetailViewerField(entry.getKey(), localized);
            } else {
                fields.add(new DetailViewerField(entry.getKey(), localized));
            }
            dr.setAttribute(entry.getKey(), entry.getValue());
        }
        if (path_field != null)
            fields.add(path_field);

        DetailViewerField[] fieldArray = new DetailViewerField[fields.size()];
        detailViewer.setFields(fields.toArray(fieldArray));
        detailViewer.setData(new DetailViewerRecord[]{dr});
        detailViewer.setCanSelectText(true);

        return detailViewer;
    }


    protected String[][] extractProperties(VectorFeature feature) {
        Attributes tmp   = feature.getAttributes();
        JSObject   jsobj = tmp.getJSObject();

        String   tmpNames = jsobj.getPropertyNames();
        String[] allNames = tmpNames.split(",");

        String[][] attr = new String[allNames.length][];

        for (int i = 0, n = attr.length; i < n; i++) {
            attr[i] = new String[] {
                allNames[i],
                jsobj.getPropertyAsString(allNames[i]) };
        }

        return attr;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
