/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;

public class WDiffProcessor extends WOutProcessor {

    public final static String I18N_WDIFF_YAXIS_LABEL =
        "chart.w_differences.yaxis.label";

    public final static String I18N_WDIFF_YAXIS_LABEL_DEFAULT = "m";

    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }
        return facetType.equals(FacetTypes.W_DIFFERENCES) ||
            facetType.equals(FacetTypes.W_DIFFERENCES_FILTERED);
    }


    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(I18N_WDIFF_YAXIS_LABEL,
                I18N_WDIFF_YAXIS_LABEL_DEFAULT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
