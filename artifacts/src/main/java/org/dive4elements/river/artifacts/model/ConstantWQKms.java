/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

public class ConstantWQKms
extends      WQKms
{
    public ConstantWQKms() {
        this("");
    }

    public ConstantWQKms(String name) {
        super(name);
    }

    public ConstantWQKms(int capacity) {
        this(capacity, "");
    }

    public ConstantWQKms(int capacity, String name) {
        super(capacity, name);
    }

    public ConstantWQKms(double [] kms, double [] qs, double [] ws) {
        this(kms, qs, ws, "");
    }

    public ConstantWQKms(
        double [] kms,
        double [] qs,
        double [] ws,
        String name
    ) {
        super(kms, qs, ws, name);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
