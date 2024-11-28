/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** A range that is about to be imported. */
public class ImportRange
implements   Comparable<ImportRange>
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(ImportRange.class);

    protected BigDecimal a;
    protected BigDecimal b;

    protected Range peer;

    public ImportRange() {
    }

    public ImportRange(BigDecimal a) {
        this.a = a;
        this.b = null;
    }

    public ImportRange(BigDecimal a, BigDecimal b) {

        // enforce a<b and set only a for zero-length ranges
        if (a != null && b == null) {
            this.a = a;
            this.b = null;
        }
        else if (a == null && b != null) {
            this.a = b;
            this.b = null;
        }
        else if (a == null && b == null) {
            throw new IllegalArgumentException("Both a and b are null.");
        }
        else if (a == b) {
            this.a = a;
            this.b = null;
        }
        else {
            if (a.compareTo(b) > 0) {
                BigDecimal t = a; a = b; b = t;
            }
            this.a = a;
            this.b = b;
        }
    }

    private static final int compare(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null && b != null) {
            return -1;
        }
        if (a != null && b == null) {
            return +1;
        }
        return a.compareTo(b);
    }

    public int compareTo(ImportRange other) {
        int cmp = compare(a, other.a);
        if (cmp != 0) return cmp;
        return compare(b, other.b);
    }

    public BigDecimal getA() {
        return a;
    }

    public void setA(BigDecimal a) {
        if (this.b != null && a.compareTo(b) >= 0) {
            throw new IllegalArgumentException(
                "a (" + a + ") must be smaller than b (" + b + ").");
        }
        this.a = a;
    }

    public BigDecimal getB() {
        return b;
    }

    public void setB(BigDecimal b) {
        if (b != null && b.compareTo(a) <= 0) {
            throw new IllegalArgumentException(
                "b (" + b + ") must be greater than a (" + a + ") or null.");
        }
        this.b = b;
    }

    public Range getPeer(River river) {
        if (peer == null) {
            peer = ImporterSession.getInstance().getRange(river, a, b);
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
