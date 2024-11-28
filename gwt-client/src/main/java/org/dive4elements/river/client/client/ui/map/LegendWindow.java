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

import com.smartgwt.client.types.ImageStyle;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.shared.MapUtils;
import org.dive4elements.river.client.shared.model.AttributedTheme;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.ThemeList;


public class LegendWindow extends Window {

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    private ThemeList themeList;

    private VLayout legendContainer;


    public LegendWindow(ThemeList themeList) {
        this.themeList = themeList;
        this.legendContainer = new VLayout();

        init();
    }

    public void update(ThemeList themeList) {
        this.themeList = themeList;

        Canvas[] legends = legendContainer.getMembers();
        legendContainer.removeMembers(legends);

        addLegends();
    }

    private void addLegends() {
        List<Theme> themes = themeList.getActiveThemes();

        for (Theme theme : themes) {
            if (theme.getActive() == 0) {
                continue;
            }

            if (theme instanceof AttributedTheme) {
                legendContainer.addMember(
                    createLegendGraphicsRow((AttributedTheme) theme));
            }
        }
    }

    private Canvas createLegendGraphicsRow(AttributedTheme at) {
        Label label = new Label(at.getDescription());
        Img img = createLegendGraphics(at);

        HLayout row = new HLayout();
        row.addMember(label);
        row.addMember(img);

        row.setHeight(150);
        row.setWidth(400);

        return row;
    }

    private Img createLegendGraphics(AttributedTheme at) {
        String imgUrl = MapUtils.getLegendGraphicUrl(at.getAttr("url"),
            at.getAttr("layers"));

        Img img = new Img(imgUrl);
        img.setImageType(ImageStyle.CENTER);
        img.setAutoFit(true);

        return img;
    }

    private void init() {
        legendContainer.setAutoHeight();
        legendContainer.setLayoutAlign(VerticalAlignment.TOP);
        legendContainer.setAlign(VerticalAlignment.CENTER);

        setTitle(MSG.wms_legend());
        setAutoSize(true);
        setCanDragResize(true);
        setIsModal(false);
        setShowModalMask(false);
        setLayoutAlign(VerticalAlignment.TOP);
        setAlign(VerticalAlignment.TOP);

        addItem(legendContainer);
        addLegends();

        centerInPage();
    }
}
