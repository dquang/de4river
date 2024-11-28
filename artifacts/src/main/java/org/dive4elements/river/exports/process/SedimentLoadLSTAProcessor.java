/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.dive4elements.river.exports.DiagramGenerator;

public class SedimentLoadLSTAProcessor extends SedimentLoadLSProcessor{

    public static final String I18N_YAXIS_LABEL =
        "chart.sedimentload.ls.yaxis.label.tpera";
    public static final String I18N_YAXIS_LABEL_DEFAULT = "[t/a]";

    @Override
    public boolean canHandle(String facettype) {
        return facettype.startsWith("sedimentload.ls.ta");
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(I18N_YAXIS_LABEL, I18N_YAXIS_LABEL_DEFAULT);
    }
}
