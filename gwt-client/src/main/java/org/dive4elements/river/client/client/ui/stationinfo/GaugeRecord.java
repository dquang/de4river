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

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.GaugeInfo;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class GaugeRecord extends ListGridRecord implements GaugeInfo {

    /** The message class that provides i18n strings.*/
    private final FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public GaugeRecord(GaugeInfo gauge) {
        String wikiBaseUrl = Config.getInstance().getWikiUrl();

        setCanExpand(true);
        Long number = gauge.getOfficialNumber();
        String url = number != null ?
                MSG.gauge_url() + number :
                MSG.gauge_url();
        setLink(wikiBaseUrl + url);
        setLinkText(MSG.gauge_info_link());
        setName(gauge.getName());
        setKmStart(gauge.getKmStart());
        setKmEnd(gauge.getKmEnd());
        setMinQ(gauge.getMinQ());
        setMaxQ(gauge.getMaxQ());
        setMinW(gauge.getMinW());
        setMaxW(gauge.getMaxW());
        setAeo(gauge.getAeo());
        setDatum(gauge.getDatum());
        setKmUp(gauge.isKmUp());
        setOfficialNumber(gauge.getOfficialNumber());
        setRiverName(gauge.getRiverName());
        setStation(gauge.getStation());
        setWstUnit(gauge.getWstUnit());
        setCurveLink(MSG.gauge_curve_link());
    }

    private void setCurveLink(String value) {
        this.setAttribute("curvelink", value);
    }

    private void setLink(String url) {
        this.setAttribute("link", url);
    }

    public String getLink() {
        return this.getAttributeAsString("link");
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
    public Double getMinQ() {
        return this.getAttributeAsDouble("minq");
    }

    private void setMinQ(Double value) {
        this.setAttribute("minq", value);
    }

    @Override
    public Double getMaxQ() {
        return this.getAttributeAsDouble("maxq");
    }

    private void setMaxQ(Double value) {
        this.setAttribute("maxq", value);
    }

    @Override
    public Double getMinW() {
        return this.getAttributeAsDouble("minw");
    }

    private void setMinW(Double value) {
        this.setAttribute("minw", value);
    }

    @Override
    public Double getMaxW() {
        return this.getAttributeAsDouble("maxw");
    }

    private void setMaxW(Double value) {
        this.setAttribute("maxw", value);
    }

    @Override
    public Double getDatum() {
        return this.getAttributeAsDouble("datum");
    }

    private void setDatum(Double value) {
        this.setAttribute("datum", value);
    }

    @Override
    public Double getAeo() {
        return this.getAttributeAsDouble("aeo");
    }

    private void setAeo(Double value) {
        this.setAttribute("aeo", value);
    }

    @Override
    public boolean isKmUp() {
        return this.getAttributeAsBoolean("kmup");
    }

    private void setKmUp(boolean value) {
        this.setAttribute("kmup", value);
    }

    @Override
    public Double getStation() {
        return this.getAttributeAsDouble("station");
    }

    private void setStation(Double value) {
        this.setAttribute("station", value);
    }

    @Override
    public String getWstUnit() {
        return this.getAttributeAsString("wstunit");
    }

    private void setWstUnit(String value) {
        this.setAttribute("wstunit", value);
    }

    @Override
    public Long getOfficialNumber() {
        return this.getAttributeAsLong("officialnumber");
    }

    private void setOfficialNumber(Long number) {
        this.setAttribute("officialnumber", number);
    }

    @Override
    public String getRiverName() {
        return this.getAttributeAsString("rivername");
    }

    private void setRiverName(String rivername) {
        this.setAttribute("rivername", rivername);
    }
}
