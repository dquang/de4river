/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.RiverInfo;

/**
 * Panel to display information about a river.
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class RiverInfoPanel extends HLayout {

    /** The flys instance */
    protected FLYS flys;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public final static int HEIGHT = 30;
    public final static int BORDER_WIDTH = 3;
    public final static int PADDING = 8;
    public final static int MARGIN = 10;

    public RiverInfoPanel(FLYS flys, RiverInfo riverinfo) {
        this.flys = flys;

        setStyleName("riverinfopanel");
        setHeight(HEIGHT + "px");
        setAlign(VerticalAlignment.CENTER);
        setAlign(Alignment.LEFT);
        setRiverInfo(riverinfo);
    }

    public void setRiverInfo(RiverInfo riverinfo) {
        GWT.log("RiverInfoPanel - setRiverInfo");

        NumberFormat nf = NumberFormat.getDecimalFormat();

        //removeAllLabels();

        addLabel(riverinfo.getName(), false);

        String kmtext = "";
        Double start = riverinfo.getKmStart();
        Double end = riverinfo.getKmEnd();

        if (!riverinfo.isKmUp()) {
            Double tmp = end;
            end = start;
            start = tmp;
        }
        if (end != null) {
            kmtext += nf.format(end);
            kmtext += " - ";
        }
        if (start != null) {
            kmtext += nf.format(start);
        }
        kmtext += " km";

        addLabel(kmtext, false);

        String qtext = "";
        Double qmin = riverinfo.getMinQ();
        Double qmax = riverinfo.getMaxQ();
        if (qmin != null) {
            qtext += nf.format(qmin);
            qtext += " " + MSG.gauge_q_unit();
            qtext += " - ";
        }
        if (qmax != null) {
            qtext += nf.format(qmax);
            qtext += " " + MSG.gauge_q_unit();
        }

        addLabel(qtext, false);

        Long number = riverinfo.getOfficialNumber();
        String url = number != null ?
            MSG.gauge_river_url() + number :
            MSG.gauge_river_url();
        String wikiBaseUrl = Config.getInstance().getWikiUrl();
        DynamicForm infoLink = WikiLinks.linkDynamicForm(
            this.flys, wikiBaseUrl + url,
            MSG.gauge_river_info_link());
        infoLink.setTop(5);
        infoLink.setMargin(5);
        addMember(infoLink);
    }

    public static int getStaticHeight() {
        return RiverInfoPanel.HEIGHT +
            (2 * RiverInfoPanel.BORDER_WIDTH) +
            (2 * RiverInfoPanel.PADDING) +
            (2 * RiverInfoPanel.MARGIN);
    }

    private void addLabel(String text, boolean wordwrap) {
        Label label = new Label(
            "<span style='font-size:1.3em'>" + text + "</span>");
        label.setWrap(wordwrap);
        label.setMargin(5);
        addMember(label);
    }
}
