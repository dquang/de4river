/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

/**
 * An OutGenerator that generates reference curves.
 */
public class NormalizedReferenceCurveGenerator
extends      ReferenceCurveGenerator
{
    public static final String I18N_NORMALIZED_CHART_TITLE =
        "chart.normalized.reference.curve.title";

    public static final String I18N_NORMALIZED_CHART_TITLE_DEFAULT  =
        "Reduzierte Bezugslinie";

    public NormalizedReferenceCurveGenerator() {
    }

    /** Get default chart title. */
    @Override
    protected String getDefaultChartTitle() {
        return msg(
            I18N_NORMALIZED_CHART_TITLE,
            I18N_NORMALIZED_CHART_TITLE_DEFAULT);
    }

    @Override
    protected String facetName() {
        return REFERENCE_CURVE_NORMALIZED;
    }

    @Override
    protected boolean doNormalize() {
        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
