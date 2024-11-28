/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.CollectionAttributeService;
import org.dive4elements.river.client.client.services.CollectionAttributeServiceAsync;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.OutputSettings;
import org.dive4elements.river.client.shared.model.Property;
import org.dive4elements.river.client.shared.model.PropertySetting;
import org.dive4elements.river.client.shared.model.Settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.MissingResourceException;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;

public class MapPrintPanel extends Canvas {

    private static final String MAPFISH_MAPTITLE = "mapfish_mapTitle";
    private static final String MAPFISH_RANGE = "mapfish_data_range";
    private static final String MAPFISH_SUBTITLE = "mapfish_data_subtitle";
    private static final String MAPFISH_STRETCH = "mapfish_data_strech";
    private static final String MAPFISH_INSTITUTION =
        "mapfish_data_institution";
    private static final String MAPFISH_SOURCE = "mapfish_data_source";
    private static final String MAPFISH_CREATOR = "mapfish_data_creator";
    private static final String MAPFISH_DATEPLACE = "mapfish_data_dateplace";
    private static final String MAPFISH_RIVER = "mapfish_data_river";
    private static final String MAPFISH_LOGO = "mapfish_logo";

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** CollectionAttribute Update Service. */
    protected CollectionAttributeServiceAsync updater =
        GWT.create(CollectionAttributeService.class);


    protected Collection collection;
    protected Settings settings;
    protected TextItem pageTitle = new TextItem();
    protected TextItem pageRange = new TextItem();
    protected TextItem pageSubtitle = new TextItem();
    protected TextItem pageStretch = new TextItem();
    protected TextItem pageInstitution = new TextItem();
    protected TextItem pageSource = new TextItem();
    protected TextItem pageCreator = new TextItem();
    protected TextItem pageDatePlace = new TextItem();
    protected SelectItem pageLogo = createPageLogoSelectItem();
//    protected SelectItem pageFormat = createPageFormatSelectItem();
    protected MapToolbar mapToolbar;
    protected MapPrintWindow parent;

    public MapPrintPanel(
        Collection collection,
        MapToolbar mapToolbar,
        MapPrintWindow parent
    ) {
        this.collection = collection;
        this.mapToolbar = mapToolbar;
        this.parent     = parent;
        initLayout();

        this.settings = collection.getSettings("print-settings");
        if (settings == null) {
            settings = new OutputSettings();
            GWT.log("settings are empty");
        }
        else {
            List<Property> properties = settings.getSettings("default");
            for (Property prop : properties) {
                PropertySetting props = (PropertySetting)prop;
                GWT.log(props.getName() + "=" + props.getValue());
                if (props.getName().equals(MAPFISH_MAPTITLE)) {
                    this.pageTitle.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_RANGE)) {
                    this.pageRange.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_SUBTITLE)) {
                    this.pageSubtitle.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_STRETCH)) {
                    this.pageStretch.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_INSTITUTION)) {
                    this.pageInstitution.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_SOURCE)) {
                    this.pageSource.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_CREATOR)) {
                    this.pageCreator.setValue(props.getValue());
                }
                else if (props.getName().equals(MAPFISH_DATEPLACE)) {
                    this.pageDatePlace.setValue(props.getValue());
                } else {
                    GWT.log("Unknown Print property: " + prop.getName());
                }
            }
        }
    }

    protected void initLayout() {
        // TODO: i18n
        this.pageTitle.setTitle(MSG.mapTitle());
        this.pageSubtitle.setTitle(MSG.mapSubtitle());
        this.pageRange.setTitle(MSG.mapRange());
        this.pageStretch.setTitle(MSG.mapStretch());
        this.pageInstitution.setTitle(MSG.mapInstitution());
        this.pageSource.setTitle(MSG.mapSource());
        this.pageCreator.setTitle(MSG.mapCreator());
        this.pageDatePlace.setTitle(MSG.mapDate());

        pageTitle.setLength(20);
        pageSubtitle.setLength(30);
        pageRange.setLength(30);
        pageStretch.setLength(30);
        pageInstitution.setLength(30);
        pageSource.setLength(30);
        pageCreator.setLength(30);
        pageDatePlace.setLength(30);
        ButtonItem printButton = createPrintButtonItem();

        printButton.setAlign(Alignment.RIGHT);

        DynamicForm df = new DynamicForm();
        df.setFields(
//               this.pageFormat,
               this.pageTitle,
               this.pageSubtitle,
               this.pageRange,
               this.pageStretch,
               this.pageInstitution,
               this.pageSource,
               this.pageCreator,
               this.pageDatePlace,
               this.pageLogo,
               printButton);
        addChild(df);
    }

    protected SelectItem createPageLogoSelectItem() {
        LinkedHashMap values = new LinkedHashMap();
        // TODO: this should be configurable
        values.put(MSG.bfgLogo(), "BfG Logo");

        SelectItem selItem = new SelectItem();
        selItem.setTitle(MSG.mapLogo());
        selItem.setValueMap(values);
        selItem.setDefaultToFirstOption(true);

        return selItem;
    }

/*
 * Commented out because we only provide a layout for A4 Landscape atm

    protected SelectItem createPageFormatSelectItem() {
        LinkedHashMap values = new LinkedHashMap();
        // TODO: i18n
        values.put("A4 landscape", "DIN A4 (Querformat)");
        //values.put("A4 portrait", "DIN A4 (Hochformat)");
        //values.put("A0 portrait", "DIN A0 (Hochformat)");

        SelectItem selItem = new SelectItem();
        selItem.setTitle("Seitengröße:"); // TODO: i18n
        selItem.setValueMap(values);
        selItem.setDefaultToFirstOption(true);

        return selItem;
    }
*/
    protected ButtonItem createPrintButtonItem() {
        ButtonItem btn = new ButtonItem();
        btn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateCollection();
                Window.open(createPrintUrl(), "_blank", "");
                parent.destroy();
            }
        });
        btn.setTitle(MSG.print());
        return btn;
    }

    private String createPrintUrl() {
        MapOutputTab ot = (MapOutputTab)mapToolbar.getOutputTab();
        Collection collection = ot.getCollection();
        String uuid = collection.identifier();

        String mapType = collection.getOutputModes().containsKey("floodmap")
            ? "floodmap"
            : "map";

        StringBuilder url = new StringBuilder();
        url.append(GWT.getModuleBaseURL());
        url.append("map-print?");

        Map map = mapToolbar.getMap();
        Bounds bounds = map.getExtent();

        if (bounds != null) {
            try {
                url.append("minx=");
                url.append(bounds.getLowerLeftX());
                url.append("&");

                url.append("maxx=");
                url.append(bounds.getUpperRightX());
                url.append("&");

                url.append("miny=");
                url.append(bounds.getLowerLeftY());
                url.append("&");

                url.append("maxy=");
                url.append(bounds.getUpperRightY());
                url.append("&");
            }
            catch (Exception e) {
                // XXX: Ignore it. bounds.getXXX() throw
                // exceptions when bound is invalid. :-/
            }
        }

        url.append("uuid=");
        url.append(uuid);
        url.append("&maptype=");
        url.append(mapType);

        appendPrintToUrl(collection, url);

        return URL.encode(url.toString());
    }

    private void appendPrintToUrl(Collection collection, StringBuilder url) {
        Settings settings = collection.getSettings("print-settings");
        if (settings != null) {
            List<Property> properties = settings.getSettings("default");
            for (Property prop : properties) {
                PropertySetting props = (PropertySetting)prop;
                url.append("&");
                String localized;
                try {
                    localized = MSG.getString(props.getName());
                }
                catch (MissingResourceException mre) {
                    localized = props.getName();
                }
                url.append(localized);
                url.append("=");
                url.append((String)props.getValue());
            }
        }
        // O.o
        String river = findRiver(((MapOutputTab)mapToolbar.getOutputTab()
                    ).getCollectionView().getArtifact());
        url.append("&" + MSG.getString(MAPFISH_RIVER) + "=" + river);
    }

    // Copy of DatacageWindow's findRiver with added state for map.river
    protected String findRiver(Artifact artifact) {
        ArtifactDescription adescr = artifact.getArtifactDescription();
        DataList [] data = adescr.getOldData();

        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                DataList dl = data[i];
                if (dl.getState().equals("state.winfo.river") ||
                        dl.getState().equals("state.map.river")) {
                    for (int j = dl.size()-1; j >= 0; --j) {
                        Data d = dl.get(j);
                        DataItem [] di = d.getItems();
                        if (di != null && di.length == 1) {
                           return d.getItems()[0].getStringValue();
                        }
                    }
                }
            }
        }

        return "";
    }

    protected void updateCollection() {
        final Config config = Config.getInstance();
        final String loc    = config.getLocale();

        GWT.log("MapPrintPanel.updateCollection via RPC now");

        List<Property> properties = new ArrayList<Property>();
        properties.add(new PropertySetting(
                MAPFISH_MAPTITLE, pageTitle.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_SUBTITLE, pageSubtitle.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_RANGE, pageRange.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_STRETCH, pageStretch.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_INSTITUTION, pageInstitution.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_SOURCE, pageSource.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_CREATOR, pageCreator.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_DATEPLACE, pageDatePlace.getValueAsString()));
        properties.add(new PropertySetting(
                MAPFISH_LOGO, pageLogo.getValueAsString()));
        settings.setSettings("default", properties);

        collection.addSettings("print-settings", settings);
        updater.update(collection, loc, new AsyncCallback<Collection>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not update collection attributes.");
                SC.warn(MSG.getString(caught.getMessage()));
            }
            @Override
            public void onSuccess(Collection collection) {
                GWT.log("MapPrint: collection attributes updated");
            }
        });
    }
}
