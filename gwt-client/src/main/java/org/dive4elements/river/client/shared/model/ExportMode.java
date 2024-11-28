/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;


/**
 * An derived OutputMode that marks an OutputMode as an export. An export mode
 * should at least support one (or more) facet which specify the type of export
 * (e.g. CSV, WST).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ExportMode extends DefaultOutputMode {

    public ExportMode() {
    }


    public ExportMode(String name, String desc, String mimeType) {
        super(name, desc, mimeType);
    }


    public ExportMode(
        String name,
        String descrition,
        String mimeType,
        List<Facet> facets)
    {
        super(name, descrition, mimeType, facets);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
