/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import java.util.Date;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.MeasurementStation;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class MeasurementStationRecord
extends ListGridRecord
implements MeasurementStation {

    /** The message class that provides i18n strings.*/
    private final FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public MeasurementStationRecord(MeasurementStation station) {
        this.setCanExpand(true);

        String wikiBaseUrl = Config.getInstance().getWikiUrl();

        Integer number = station.getID();
        String stationName = station.getName();
        String stationIdent = stationName.replaceAll("\\W", "");
        String stationType = station.getMeasurementType();
        String link = wikiBaseUrl + MSG.measurement_station_url() +
            stationIdent + stationType;
        this.setLink(link);
        this.setLinkText(MSG.measurement_station_info_link());
        this.setCurveLink(MSG.static_sqrelation());
        this.setID(number);
        this.setName(station.getName());
        this.setKmEnd(station.getKmEnd());
        this.setKmStart(station.getKmStart());
        this.setRiverName(station.getRiverName());
        this.setGaugeName(station.getGaugeName());
        this.setMeasurementType(station.getMeasurementType());
        this.setOperator(station.getOperator());
        this.setRiverSide(station.getRiverSide());
        this.setStartTime(station.getStartTime());
        this.setStopTime(station.getStopTime());
        this.setComment(station.getComment());
    }

    @Override
    public Integer getID() {
        return this.getAttributeAsInt("stationid");
    }

    private void setID(Integer value) {
        this.setAttribute("stationid", value);
    }

    @Override
    public String getName() {
        return this.getAttributeAsString("name");
    }

    private void setName(String value) {
        this.setAttribute("name", value);
    }

    @Override
    public Double getKmStart() {
        return this.getAttributeAsDouble("kmstart");
    }

    private void setKmStart(Double value) {
        this.setAttribute("kmstart", value);
    }

    @Override
    public Double getKmEnd() {
        return this.getAttributeAsDouble("kmend");
    }

    private void setKmEnd(Double value) {
        this.setAttribute("kmend", value);
    }

    @Override
    public String getRiverName() {
        return this.getAttributeAsString("rivername");
    }

    private void setRiverName(String rivername) {
        this.setAttribute("rivername", rivername);
    }

    @Override
    public String getRiverSide() {
        return this.getAttributeAsString("riverside");
    }

    private void setRiverSide(String riverside) {
        this.setAttribute("riverside", riverside);
    }

    @Override
    public String getMeasurementType() {
        return this.getAttributeAsString("measurementtype");
    }

    private void setMeasurementType(String value) {
        this.setAttribute("measurementtype", value);
    }

    @Override
    public String getOperator() {
        return this.getAttributeAsString("operator");
    }

    private void setOperator(String value) {
        this.setAttribute("operator", value);
    }

    @Override
    public Date getStartTime() {
        return this.getAttributeAsDate("starttime");
    }

    private void setStartTime(Date value) {
        this.setAttribute("starttime", value);
    }

    @Override
    public Date getStopTime() {
        return this.getAttributeAsDate("stoptime");
    }

    private void setStopTime(Date value) {
        this.setAttribute("stoptime", value);
    }

    @Override
    public String getGaugeName() {
        return this.getAttributeAsString("gaugename");
    }

    private void setGaugeName(String value) {
        this.setAttribute("gaugename", value);
    }

    @Override
    public String getComment() {
        return this.getAttributeAsString("comment");
    }

    private void setComment(String value) {
        this.setAttribute("comment", value);
    }

    public String getLink() {
        return this.getAttributeAsString("link");
    }

    public void setLink(String link) {
        this.setAttribute("link", link);
    }

    public void setCurveLink(String link) {
        this.setAttribute("curvelink", link);
    }

    public String getCurveLink() {
        return this.getAttribute("curvelink");
    }

}
