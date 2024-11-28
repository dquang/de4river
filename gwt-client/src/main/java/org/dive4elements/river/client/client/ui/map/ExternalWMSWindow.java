/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Capabilities;
import org.dive4elements.river.client.shared.model.WMSLayer;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.GCService;
import org.dive4elements.river.client.client.services.GCServiceAsync;
import org.dive4elements.river.client.client.services.MapUrlService;
import org.dive4elements.river.client.client.services.MapUrlServiceAsync;


public class ExternalWMSWindow extends Window {

    public interface LayerLoader {
        void load(List<WMSLayer> toLoad);
    } // end of interface WMSLayerLoader


    protected GCServiceAsync gcService = GWT.create(GCService.class);
    protected MapUrlServiceAsync muService = GWT.create(MapUrlService.class);
    protected FLYSConstants  MSG       = GWT.create(FLYSConstants.class);

    protected Layout inputPanel;
    protected Layout infoPanel;
    protected Layout layersPanel;

    protected Capabilities capabilites;

    protected String srs;

    protected LinkedHashMap<String, String> urls;
    protected String url;

    protected LayerLoader loader;


    public ExternalWMSWindow(LayerLoader loader) {
        super();
        this.urls = new LinkedHashMap<String, String>();
        this.loader = loader;
    }


    public ExternalWMSWindow(LayerLoader loader, String srs) {
        this(loader);
        this.srs = srs;
    }


    protected void setUrl(String url) {
        this.url = url;
    }


    protected String getUrl() {
        return url;
    }


    protected String getCapabilitiesUrl() {
        String cUrl = url;

        if (url.indexOf("?") >= 0) {
            cUrl += "&SERVICE=WMS&REQUEST=GetCapabilities";
        }
        else {
            cUrl += "?SERVICE=WMS&REQUEST=GetCapabilities";
        }

        return cUrl;
    }


    protected void setCapabilites(Capabilities capabilites) {
        this.capabilites = capabilites;
    }


    public void start() {
        show();
        centerInPage();

        goToInputPanel();
    }


    protected void goToInputPanel() {
        clearItems();

        inputPanel = createInputPanel();

        addItem(inputPanel);

        setWidth(380);
        setHeight(140);
    }


    protected void goToInfoPanel() {
        clearItems();

        infoPanel = createInfoPanel();

        addItem(infoPanel);

        setWidth(500);
        setHeight(500);

        centerInPage();
    }


    protected void goToLayersPanel() {
        clearItems();

        layersPanel = createLayersPanel();

        addItem(layersPanel);

        setWidth(500);
        setHeight(500);
    }


    protected void clearItems() {
        Canvas[] items = getItems();

        if (items != null) {
            for (Canvas item: items) {
                removeItem(item);
            }
        }
    }


    protected void setUrls(Map<String, String> urls) {
        this.urls.putAll(urls);
    }

    protected void readUrls() {
    }


    protected Layout createInputPanel() {
        setTitle(MSG.addwmsInputTitle());

        readUrls();

        DynamicForm form = new DynamicForm();
        final ComboBoxItem url = new ComboBoxItem("Url:");
        url.setRedrawOnChange(true);
        muService.getUrls(new AsyncCallback<Map<String, String> >() {
            public void onFailure(Throwable caught) {
                GWT.log("Error reading WMS-Services" + caught.getMessage());
            }
            public void onSuccess(Map<String, String> wms) {
                urls.putAll(wms);
                url.setValueMap(urls);

            }
        });

        String oldUrl = getUrl();
        if (oldUrl != null && oldUrl.length() > 0) {
            url.setValue(oldUrl);
        }

        ClickHandler goHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                String newUrl = url.getValue().toString();

                if (!isUrlValid(newUrl)) {
                    SC.warn(MSG.addwmsInvalidURL());
                    return;
                }

                setUrl(newUrl);

                doCapabilitesRequest();
            }
        };

        ClickHandler cancelHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                quit();
            }
        };

        VLayout root = new VLayout();
        root.setHeight(75);
        root.setMargin(10);
        root.setLayoutMargin(10);

        form.setFields(url);
        root.addMember(form);
        root.addMember(createButtonPanel(null, goHandler, cancelHandler));

        return root;
    }


    protected Layout createInfoPanel() {
        setTitle(MSG.addwmsInfoTitle());

        ClickHandler backHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                goToInputPanel();
            }
        };

        ClickHandler goHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                goToLayersPanel();
            }
        };

        ClickHandler cancelHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                quit();
            }
        };

        VLayout root  = new VLayout();
        VLayout panel = new CapabilitiesPanel(capabilites);

        root.setLayoutMargin(10);
        panel.setHeight(420);

        root.addMember(panel);
        root.addMember(
            createButtonPanel(backHandler, goHandler, cancelHandler));

        return root;
    }


    protected Layout createLayersPanel() {
        setTitle(MSG.addwmsLayerTitle());

        final WMSLayersTree tree = new WMSLayersTree(capabilites, srs);

        ClickHandler backHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                goToInfoPanel();
            }
        };

        ClickHandler goHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                ListGridRecord[] selection = tree.getSelectedRecords();

                if (selection == null || selection.length == 0) {
                    return;
                }

                List<WMSLayer> toLoad = new ArrayList<WMSLayer>();

                for (ListGridRecord record: selection) {
                    toLoad.add(
                        ((WMSLayersTree.WMSLayerNode) record).getWMSLayer());

                }

                finish(toLoad);
            }
        };

        ClickHandler cancelHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                quit();
            }
        };

        VLayout root = new VLayout();

        root.setLayoutMargin(10);
        tree.setHeight(420);

        root.addMember(tree);
        root.addMember(
            createButtonPanel(backHandler, goHandler, cancelHandler));

        return root;
    }


    /**
     * @param back
     * @param ok
     * @param cancel
     *
     * @return
     */
    protected Layout createButtonPanel(
        ClickHandler backHandler,
        ClickHandler goHandler,
        ClickHandler cancelHandler
    ) {
        Button back   = new Button(MSG.addwmsBack());
        Button go     = new Button(MSG.addwmsContinue());
        Button cancel = new Button(MSG.addwmsCancel());

        if (backHandler != null) {
            back.addClickHandler(backHandler);
        }
        else {
            back.setDisabled(true);
        }

        if (goHandler != null) {
            go.addClickHandler(goHandler);
        }
        else {
            go.setDisabled(true);
        }

        if (cancelHandler != null) {
            cancel.addClickHandler(cancelHandler);
        }
        else {
            cancel.setDisabled(true);
        }

        HLayout buttonPanel = new HLayout();
        buttonPanel.setHeight(25);
        buttonPanel.setMembersMargin(15);
        buttonPanel.setLayoutTopMargin(10);
        buttonPanel.addMember(back);
        buttonPanel.addMember(go);
        buttonPanel.addMember(cancel);

        return buttonPanel;
    }


    protected boolean isUrlValid(String url) {
        // TODO Improve URL validation
        return !(url == null || url.length() == 0);
    }


    protected void finish(List<WMSLayer> toLoad) {
        loader.load(toLoad);

        quit();
    }


    protected void quit() {
        destroy();
    }


    protected void doCapabilitesRequest() {
        gcService.query(getCapabilitiesUrl(),new AsyncCallback<Capabilities>() {
            public void onFailure(Throwable e) {
                SC.warn(MSG.getString(e.getMessage()));
            }

            public void onSuccess(Capabilities capabilites) {
                setCapabilites(capabilites);
                goToInfoPanel();
            }
        });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
