/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import java.util.List;
import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.ExportMode;
import org.dive4elements.river.client.shared.model.Facet;
import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;


/**
 * A panel that displays an download icon for all available export modes of a
 * Collection.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ExportPanel extends VLayout {

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected Collection       c;
    protected List<ExportMode> exports;

    /** This layout will store a list of available export types.*/
    protected HLayout container;


    public ExportPanel(Collection c, List<ExportMode> exports) {
        super();

        this.c         = c;
        this.exports   = exports;
        this.container = new HLayout();

        Label title = new Label(MSG.dataexport());
        title.setHeight(15);
        title.setStyleName("fontNormalSmallUnderlined");

        addMember(title);
        addMember(createExportItems());

        setHeight(45);
        setMembersMargin(5);
    }


    /**
     * This method is used to create an item (created by createExportButton) for
     * each facet for each export mode.
     *
     * @return a horizontal list of buttons.
     */
    protected HLayout createExportItems() {
        HLayout layout = new HLayout();

        for (ExportMode mode: exports) {
            String      name   = mode.getName();
            List<Facet> facets = mode.getFacets();

            for (Facet facet: facets) {
                if (name.equals("fix_wq_curve_at_export")) {
                    continue;
                }
                String filename = name;
                if (name.equals("computed_dischargecurve_at_export")) {
                    filename = "dischargecurve";
                }
                layout.addMember(createExportButton(
                    name,
                    facet.getName(),
                    filename));
            }
        }

        return layout;
    }


    /**
     * This method is used to create a button (with click handler) for a
     * concrete export mode / type.
     *
     * @param name The name of the export.
     * @param facet The name of the export type (e.g. CSV, WST).
     *
     * @return an image with click handler.
     */
    protected Canvas createExportButton(
        String name,
        String facet,
        String filename
    ) {
        String url  = getExportUrl(name, facet, filename);
        String imgUrl = GWT.getHostPageBaseURL();
        if (facet.equals("pdf")) {
            imgUrl += MSG.downloadPDF();
        }
        else if (facet.equals("at")) {
            imgUrl += MSG.downloadAT();
        }
        else if (facet.equals("wst")) {
            imgUrl += MSG.downloadWST();
        }
        else  if (facet.equals("csv")) {
            url += "&encoding=windows-1252";
            imgUrl += MSG.downloadCSV();
        }
        else {
            imgUrl += MSG.imageSave();
        }
        ImgLink link = new ImgLink(imgUrl, url, 30, 30);
        link.setTooltip(getTooltipText(name, facet));

        return link;
    }


    /**
     * Creates the URL used to trigger an export.
     *
     * @param name The name of the export.
     * @param facet The name of the export type (e.g. CSV, WST).
     *
     * @return the export URL.
     */
    protected String getExportUrl(String name, String facet, String filename) {
        Config config = Config.getInstance();

        String url = GWT.getModuleBaseURL();
        url += "export";
        url += "?uuid=" + c.identifier();
        url += "&name=" + filename;
        url += "&mode=" + name;
        url += "&type=" + facet;
        url += "&server=" + config.getServerUrl();
        url += "&locale=" + config.getLocale();

        return url;
    }


    /**
     * Creates a text used as tooltip for a specific export and type.
     *
     * @param name The name of the export.
     * @param facet The name of the export type (e.g. CSV, WST).
     *
     * @return a tooltip text.
     */
    protected String getTooltipText(String name, String facet) {
        try {
            return MSG.getString(name) + " | " + MSG.getString(facet);
        }
        catch (MissingResourceException mre) {
            return name + " | " + facet;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
