/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;

import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.DischargeTableValue;


public class ImportDischargeTableValue
{
    private BigDecimal q;
    private BigDecimal w;

    private DischargeTableValue peer;

    public ImportDischargeTableValue() {
    }


    public ImportDischargeTableValue(BigDecimal q, BigDecimal w) {
        this.q = q;
        this.w = w;
    }


    public DischargeTableValue getPeer(DischargeTable dischargeTable) {
        if (peer == null) {
            peer = ImporterSession.getInstance()
                .getDischargeTableValue(dischargeTable, q, w);
        }

        return peer;
    }
}
