/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

public class ReportMode
extends      DefaultOutputMode
{
    public ReportMode() {
    }


    public ReportMode(String name, String desc, String mimeType) {
        super(name, desc, mimeType);
    }


    public ReportMode(
        String      name,
        String      description,
        String      mimeType,
        List<Facet> facets
    ) {
        super(name, description, mimeType, facets);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Facet f: facets) {
            if (first) first = false;
            else       sb.append(", ");
            sb.append("(name = '").append(f.getName())
              .append("', index = ").append(f.getIndex())
              .append(", desc = '").append(f.getDescription()).append("')");
        }
        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
