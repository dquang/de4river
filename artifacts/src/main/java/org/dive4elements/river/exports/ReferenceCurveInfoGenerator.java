/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;


/**
 * A ChartInfoGenerator that generates meta information for specific reference
 * curves.
 */
public class ReferenceCurveInfoGenerator
extends      ChartInfoGenerator
{
    public ReferenceCurveInfoGenerator() {
        super(new ReferenceCurveGenerator());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
