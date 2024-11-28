/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

/** A pretty naive pointwise algorithm to find out the columns
 *  of a WSTValueTable which imfold ("umhuellen") a set of WQKMs
 *  in terms of Q.
 *  A better implemention would exploit the fact that the
 *  Qs normally are constant for a while along km. This would
 *  reduce the runtime complexity to only a few Q spans instead
 *  of the pointwise evaluation.
 */
public class InfoldingColumns
{
    private QRangeTree.QuickQFinder [] qFinders;

    private boolean [] infoldingColumns;

    public InfoldingColumns() {
    }

    public InfoldingColumns(WstValueTable.Column [] columns) {

        qFinders = new QRangeTree.QuickQFinder[columns.length];
        for (int i = 0; i < qFinders.length; ++i) {
            qFinders[i] = columns[i].getQRangeTree().new QuickQFinder();
        }

        infoldingColumns = new boolean[columns.length];
    }

    public boolean [] getInfoldingColumns() {
        return infoldingColumns;
    }

    public void markInfoldingColumns(QKms [] qkms) {
        for (QKms qk: qkms) {
            markInfoldingColumns(qk);
        }
    }

    public void markInfoldingColumns(QKms qkms) {
        int N = qkms.size();
        int C = qFinders.length-1;
        for (int i = 0; i < N; ++i) {
            double km       = qkms.getKm(i);
            double q        = qkms.getQ(i);
            double above    =  Double.MAX_VALUE;
            double below    = -Double.MAX_VALUE;
            int    aboveIdx = -1;
            int    belowIdx = -1;

            for (int j = C; j >= 0; --j) {
                double qc = qFinders[j].findQ(km);
                if (Double.isNaN(qc)) {
                    continue;
                }
                if (qc <= q) {
                    if (qc > below) {
                        below    = qc;
                        belowIdx = j;
                    }
                }
                else if (qc < above) { // qc > q
                    above    = qc;
                    aboveIdx = j;
                }
            }

            if (aboveIdx != -1) {
                infoldingColumns[aboveIdx] = true;
            }

            if (belowIdx != -1) {
                infoldingColumns[belowIdx] = true;
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
