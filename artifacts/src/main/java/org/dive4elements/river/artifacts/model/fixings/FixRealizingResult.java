/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.model.WQKms;

import org.dive4elements.river.artifacts.model.Parameters;
import org.dive4elements.river.artifacts.model.WQKmsResult;

import org.dive4elements.river.utils.KMIndex;

/** Result of a FixRealizing Calculation. */
public class FixRealizingResult
extends      FixResult
implements   WQKmsResult
{
    public WQKms [] wqkms;

    public FixRealizingResult() {
    }

    public FixRealizingResult(
        Parameters      parameters,
        KMIndex<QWD []> referenced,
        KMIndex<QWI []> outliers,
        WQKms []        wqkms
    ) {
        super(parameters, referenced, outliers);
        this.wqkms = wqkms;
    }

    @Override
    public WQKms [] getWQKms() {
        return wqkms;
    }

    public void setWQKms(WQKms [] wqkms) {
        this.wqkms = wqkms;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
