/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents minmimum and maximum values for W and Q
 */
public class MinMaxWQ implements Serializable {

    private BigDecimal minw;
    private BigDecimal maxw;
    private BigDecimal minq;
    private BigDecimal maxq;

    /**
     * Default constuctor to indecate that no min and max w and q values
     * are available
     */
    public MinMaxWQ() {
    }

    /**
     * Constructor for a new MinMaxWQ value
     *
     * @param minw Mimimim W
     * @param maxw Maximum W
     * @param minq Mimimim Q
     * @param maxq Maximum Q
     */
    public MinMaxWQ(
            BigDecimal minw,
            BigDecimal maxw,
            BigDecimal minq,
            BigDecimal maxq)
    {
        this.minw = minw;
        this.maxw = maxw;
        this.minq = minq;
        this.maxq = maxq;
    }

    public BigDecimal getMinW() {
        return this.minw;
    }

    public BigDecimal getMaxW() {
        return this.maxw;
    }

    public BigDecimal getMinQ() {
        return this.minq;
    }

    public BigDecimal getMaxQ() {
        return this.maxq;
    }
}
