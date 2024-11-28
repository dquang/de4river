/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.WstColumnValue;
import org.dive4elements.river.model.WstColumn;
import org.dive4elements.river.model.River;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ImportWstColumnValue
{
    protected Logger log = LogManager.getLogger(ImportWstColumnValue.class);

    protected BigDecimal      position;
    protected BigDecimal      w;
    protected ImportWstColumn wstColumn;

    protected WstColumnValue  peer;

    public ImportWstColumnValue() {
    }

    public ImportWstColumnValue(
        ImportWstColumn wstColumn,
        BigDecimal      position,
        BigDecimal      w
    ) {
        this.wstColumn = wstColumn;
        this.position  = position;
        this.w         = w;
    }

    public BigDecimal getPosition() {
        return position;
    }

    public void setPosition(BigDecimal position) {
        this.position = position;
    }

    public BigDecimal getW() {
        return w;
    }

    public void setW(BigDecimal w) {
        this.w = w;
    }

    public ImportWstColumn getWstColumn() {
        return wstColumn;
    }

    public void setWstColumn(ImportWstColumn wstColumn) {
        this.wstColumn = wstColumn;
    }

    public WstColumnValue getPeer(River river) {
        if (peer == null) {
            WstColumn c = wstColumn.getPeer(river);
            peer = ImporterSession.getInstance().getWstColumnValue(
                c, position, w);
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
