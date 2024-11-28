/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.GrainFraction;


public class ImportGrainFraction {

    private GrainFraction peer;

    public ImportGrainFraction(GrainFraction gf) {
        this.peer = gf;
    }


    public void storeDependencies() {
        // Nothing to store because its prefilled in schema.
    }


    public GrainFraction getPeer() {
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
