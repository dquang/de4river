/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import gnu.trove.TDoubleArrayList;

public interface WKms
extends          NamedObject
{
    int size();

    double getKm(int index);

    double getW(int index);

    TDoubleArrayList allKms();

    /** A new list of values between the km's from and to. */
    WKms filteredKms(double from, double to);

    TDoubleArrayList allWs();

    boolean guessWaterIncreasing();

    /** Guess if the Water flows from right to left.
     *
     * @return True if km's and ws's both grow in the same direction */
    boolean guessRTLData();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
