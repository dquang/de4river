/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.util.Random;

import gnu.trove.TDoubleArrayList;

public class DataUtil
{
    public static boolean guessDataIncreasing(TDoubleArrayList data) {
        return guessDataIncreasing(data, 0.05f);
    }

    /** Guess if data1 and data2 both grow in the same direction */
    public static boolean guessSameDirectionData(TDoubleArrayList data1,
            TDoubleArrayList data2) {
        boolean d1dir = DataUtil.guessDataIncreasing(data1, 0.05f);
        boolean d2dir = DataUtil.guessDataIncreasing(data2, 0.05f);
        int size = data1.size();
        return ((d1dir && d2dir) || (!d1dir && !d2dir)) && size > 1;
    }

    public static boolean guessDataIncreasing(
        TDoubleArrayList data,
        float factor
    ) {
        int N = data.size();
        if (N < 2) return false;

        int samples = (int)(factor*N) + 1;

        int up = 0;

        Random rand = new Random();

        for (int i = 0; i < samples; ++i) {
            int    pos2 = rand.nextInt(N-1) + 1;
            int    pos1 = rand.nextInt(pos2);
            double w1   = data.getQuick(pos1);
            double w2   = data.getQuick(pos2);
            if (w2 > w1) ++up;
        }

        return up > samples/2;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
