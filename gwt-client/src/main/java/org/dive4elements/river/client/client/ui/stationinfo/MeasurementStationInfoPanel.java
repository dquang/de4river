/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.MeasurementStation;

public class MeasurementStationInfoPanel extends VLayout {

    /** The message class that provides i18n strings.*/
    private FLYSConstants MSG = GWT.create(FLYSConstants.class);

    private static final int KEY_WIDTH = 150;

    public MeasurementStationInfoPanel(MeasurementStation station) {
        setStyleName("infopanel");
        setWidth100();

        VLayout grid = new VLayout();

        HLayout line1 = new HLayout();
        String type = station.getMeasurementType();
        if (type != null) {
            Label key = new Label(MSG.measurement_station_type());
            Label value = new Label(type);
            key.setWidth(KEY_WIDTH);
            line1.addMember(key);
            line1.addMember(value);
            grid.addMember(line1);
        }

        HLayout line2 = new HLayout();
        String riverside = station.getRiverSide();
        if (riverside != null) {
            Label key = new Label(MSG.riverside());
            Label value = new Label(riverside);
            key.setWidth(KEY_WIDTH);
            line2.addMember(key);
            line2.addMember(value);
            grid.addMember(line2);
        }

        HLayout line3 = new HLayout();
        String gaugename = station.getGaugeName();
        if (gaugename != null) {
            Label key = new Label(MSG.measurement_station_gauge_name());
            Label value = new Label(gaugename);
            key.setWidth(KEY_WIDTH);
            line3.addMember(key);
            line3.addMember(value);
            grid.addMember(line3);
        }

        HLayout line4 = new HLayout();
        DateTimeFormat df = DateTimeFormat.getFormat(
            PredefinedFormat.DATE_MEDIUM);

        Date starttime = station.getStartTime();
        if (starttime != null) {
            Label key = new Label(MSG.measurement_station_start_time());
            Label value = new Label(df.format(starttime));
            key.setWidth(KEY_WIDTH);
            line4.addMember(key);
            line4.addMember(value);
            grid.addMember(line4);
        }

        HLayout line5 = new HLayout();
        String moperator = station.getOperator();
        if (moperator != null) {
            Label key = new Label(MSG.measurement_station_operator());
            Label value = new Label(moperator);
            key.setWidth(KEY_WIDTH);
            line5.addMember(key);
            line5.addMember(value);
            grid.addMember(line5);
        }

        HLayout line6 = new HLayout();
        String mcomment = station.getComment();
        if (mcomment != null) {
            Label key = new Label(MSG.measurement_station_comment());
            Label value = new Label(mcomment);
            key.setWidth(KEY_WIDTH);
            value.setWidth(300);
            line6.addMember(key);
            line6.addMember(value);
            grid.addMember(line6);
        }

        addMember(grid);
    }
}
