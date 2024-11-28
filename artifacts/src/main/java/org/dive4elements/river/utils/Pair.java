/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.io.Serializable;

/**
 * @param <A>
 * @param <B>
 * @author <a href="mailto:teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public final class Pair<A, B>
implements         Serializable
{
    private A a;
    private B b;

    public Pair() {
    }

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
