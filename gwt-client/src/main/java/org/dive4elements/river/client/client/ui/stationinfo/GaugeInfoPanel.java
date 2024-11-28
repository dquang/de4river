/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.Label;

import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.GaugeInfo;

public class GaugeInfoPanel extends VLayout {

    /** The message class that provides i18n strings.*/
    private FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** Application instance. */
    private FLYS flys;

    public GaugeInfoPanel(GaugeInfo gauge, FLYS flys) {
        this.flys = flys;
        setStyleName("gaugeinfopanel");

        NumberFormat nf = NumberFormat.getDecimalFormat();

        VLayout grid = new VLayout();
        HLayout line1 = new HLayout();

        Double minw = gauge.getMinW();
        Double maxw = gauge.getMaxW();
        if (minw != null && maxw != null) {
            Label key = new Label(MSG.wq_value_q());
            Label value = new Label(nf.format(minw) +
                                    " - " + nf.format(maxw));
            key.setWidth(150);
            line1.addMember(key);
            line1.addMember(value);
        }

        HLayout line2 = new HLayout();
        Double minq = gauge.getMinQ();
        Double maxq = gauge.getMaxQ();
        if (minq != null && maxq != null) {
            Label key = new Label(MSG.wq_value_w());
            Label value = new Label( nf.format(minq) +
                    " - " + nf.format(maxq));
            key.setWidth(150);
            line2.addMember(key);
            line2.addMember(value);
        }

        HLayout line3 = new HLayout();
        Double aeo = gauge.getAeo();
        if (aeo != null) {
            Label key = new Label("AEO [km²]");
            Label value = new Label(nf.format(aeo));
            key.setWidth(150);
            line3.addMember(key);
            line3.addMember(value);
        }

        HLayout line4 = new HLayout();
        Double datum = gauge.getDatum();
        if (datum != null) {
            Label key = new Label(MSG.gauge_zero() + " [" +
                    gauge.getWstUnit() + "]");
            Label value = new Label(nf.format(datum));
            key.setWidth(150);
            line4.addMember(key);
            line4.addMember(value);
        }

        HLayout line5 = new HLayout();
        DynamicForm line5Form = new DynamicForm();
        line5Form.setItems(new GaugeMainValueAnchor(flys, gauge));
        line5.addMember(line5Form);

        if (minw != null && maxw != null) {
            grid.addMember(line1);
        }
        if (minq != null && maxq != null) {
            grid.addMember(line2);
        }
        grid.addMember(line3);
        grid.addMember(line4);
        // Do not show link if no values anyway.
        if (minw != null && maxw != null && minq != null && maxq != null) {
            grid.addMember(line5);
        }
        addMember(grid);
    }


    /**
     * Clickable anchor that asks application to show window with
     * main values for gauge.
     */
    class GaugeMainValueAnchor extends LinkItem implements ClickHandler {

        private FLYS flys;
        private GaugeInfo gauge;

        public GaugeMainValueAnchor(FLYS flys, GaugeInfo gauge) {
            super();
            this.setLinkTitle(MSG.show_mainvalues());
            this.setShowTitle(false);
            this.flys = flys;
            this.gauge = gauge;

            addClickHandler(this);
        }

        @Override
        public void onClick(ClickEvent ev) {
            GWT.log("GaugeMainValueAnchor - onClick " + gauge.getRiverName() +
                    " " + gauge.getOfficialNumber());
            flys.newGaugeMainValueTable(gauge);
        }
    }
}
