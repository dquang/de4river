/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.HeaderDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.HeaderDoubleClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.ThemePanel;
import org.dive4elements.river.client.shared.model.AttributedTheme;
import org.dive4elements.river.client.shared.model.FacetRecord;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Theme;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapThemePanel extends ThemePanel {

    public static final int CELL_HEIGHT = 25;


    public interface ActivateCallback {
        void activate(Theme theme, boolean activate);
    }


    public interface ThemeMovedCallback {
        void onThemeMoved(Theme theme, int oldIdx, int newIdx);
    }

    public interface LayerZoomCallback {
        void onLayerZoom(Theme theme, String extent);
    }



    private FLYSConstants MSG = GWT.create(FLYSConstants.class);


    protected ActivateCallback   activateCallback;
    protected ThemeMovedCallback themeMovedCallback;
    protected LayerZoomCallback  layerZoomCallback;

    protected ListGridRecord[] oldRecords;


    public static final String GRID_FIELD_ACTIVE = "active";
    public static final String GRID_FIELD_NAME   = "name";


    protected MapOutputTab mapOut;


    public MapThemePanel(
        CollectionView     view,
        OutputMode         mode,
        MapOutputTab       mapOut,
        ActivateCallback   activateCallback,
        ThemeMovedCallback themeMovedCallback,
        LayerZoomCallback  layerZoomCallback
    ) {
        super(mode, view);

        this.mapOut             = mapOut;
        this.activateCallback   = activateCallback;
        this.themeMovedCallback = themeMovedCallback;
        this.layerZoomCallback  = layerZoomCallback;

        initGrid();
        initLayout();

        updateGrid();
    }


    protected void initLayout() {
        setWidth100();
        setHeight100();

        VLayout layout = new VLayout();
        layout.setWidth100();
        layout.setHeight100();

        layout.addMember(list);
        layout.addMember(navigation);

        addChild(layout);
    }


    protected void initGrid() {
        list.setCanEdit(true);
        list.setCanSort(false);
        list.setShowRecordComponents(false);
        list.setShowRecordComponentsByCell(true);
        list.setShowHeader(true);
        list.setShowHeaderContextMenu(false);
        list.setCanReorderFields(false);
        list.setWidth100();
        list.setHeight100();

        list.addHeaderDoubleClickHandler(new HeaderDoubleClickHandler() {
            @Override
            public void onHeaderDoubleClick(HeaderDoubleClickEvent event) {
                // cancel the event.
                return;
            }
        });

        list.setCellHeight(CELL_HEIGHT);
        list.setShowRecordComponents(true);
        list.setShowRecordComponentsByCell(true);
        list.setShowAllRecords(true);

        list.addEditCompleteHandler(this);

        ListGridField active = new ListGridField(GRID_FIELD_ACTIVE, " ", 20);
        active.setType(ListGridFieldType.BOOLEAN);
        active.setCanDragResize(false);

        ListGridField name = new ListGridField(
            GRID_FIELD_NAME, MSG.chart_themepanel_header_themes());
        name.setType(ListGridFieldType.TEXT);

        list.setFields(active, name);
    }


    @Override
    protected void clearGrid() {
        oldRecords = list.getRecords();
        super.clearGrid();
    }


    @Override
    protected void addFacetRecord(FacetRecord rec) {
        Theme newTheme = rec.getTheme();
        boolean  isNew = true;

        for (ListGridRecord old: getOldRecords()) {
            FacetRecord fr = (FacetRecord) old;

            if (newTheme.equals(fr.getTheme())) {
                isNew = false;
                break;
            }
        }

        if (isNew && mapOut != null) {
            mapOut.addLayer(mapOut.createWMSLayer(newTheme));
        }

        super.addFacetRecord(rec);
    }


    @Override
    protected Menu getSingleContextMenu(final ListGridRecord[] records) {
        Menu menu = super.getSingleContextMenu(records);

        MenuItem layerZoom = createLayerZoomItem(records);
        if (layerZoom != null) {
            menu.addItem(layerZoom);
        }
        menu.addItem(createMapURLItem(records));

        return menu;
    }


    protected MenuItem createMapURLItem(final ListGridRecord[] records) {
        final FacetRecord     fr = (FacetRecord) records[0];
        final AttributedTheme at = (AttributedTheme) fr.getTheme();

        MenuItem item = new MenuItem(MSG.wmsURLMenuItem());
        item.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                String url = getMapOutputTab().wmsUrls().get(
                    at.getAttr("layers"));
                SC.say(MSG.wmsURLBoxTitle(), url);
            }
        });

        return item;
    }


    @Override
    protected MenuItem createRemoveItem(final ListGridRecord[] records) {
        MenuItem item = super.createRemoveItem(records);
        item.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                SC.ask(MSG.askThemeRemove(), new BooleanCallback() {
                    @Override
                    public void execute(Boolean value) {
                        if (value) {
                            for (ListGridRecord record: records) {
                                FacetRecord facet = (FacetRecord) record;

                                Theme theme = facet.getTheme();
                                theme.setVisible(0);
                                theme.setActive(0);

                                AttributedTheme at = (AttributedTheme) theme;
                                getMapOutputTab().removeLayer(
                                    at.getAttr("layers"));
                            }

                            updateCollection();
                        }
                    }
                });
            }
        });

        return item;
    }


    protected MenuItem createLayerZoomItem(final ListGridRecord[] recs) {
        final FacetRecord     fr = (FacetRecord) recs[0];
        final AttributedTheme at = (AttributedTheme) fr.getTheme();

        final String extent = at.getAttr("extent");

        if (extent == null || extent.length() == 0) {
            return null;
        }

        MenuItem zoom = new MenuItem(MSG.zoomToLayer());
        zoom.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent evt) {
                if (layerZoomCallback != null) {
                    layerZoomCallback.onLayerZoom(at, extent);
                }
            }
        });

        return zoom;
    }

    @Override
    public String getWidthAsString() {
        if(!isVisible()) {
            return "0";
        }
        else {
            return super.getWidthAsString();
        }
    }

    @Override
    public void activateTheme(Theme theme, boolean active) {
        if (activateCallback != null) {
            activateCallback.activate(theme, active);
        }

        theme.setActive(active ? 1 : 0);
    }


    @Override
    protected void fireThemeMoved(Theme theme, int oldIdx, int newIdx) {
        if (themeMovedCallback != null) {
            themeMovedCallback.onThemeMoved(theme, oldIdx, newIdx);
        }
    }


    protected ListGridRecord[] getOldRecords() {
        return oldRecords != null ? oldRecords : new ListGridRecord[0];
    }


    protected MapOutputTab getMapOutputTab() {
        return mapOut;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
