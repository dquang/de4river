/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;


import java.io.Serializable;

import org.dive4elements.river.artifacts.model.fixings.QWD;

public class QWDDateRange
implements   Serializable
{

    public QWD qwd;
    public DateRange dateRange;

    public QWDDateRange(QWD qwd, DateRange dr) {
        this.qwd = qwd;
        this.dateRange = dr;
    }

    public QWD getQWD() {
        return qwd;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
