/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.backend.utils.StringUtil;


public class BedDifferencesAccess
extends RangeAccess
{
    private static Logger log = LogManager.getLogger(BedDifferencesAccess.class);

    private String    yearEpoch;
    private String [] diffs;

    private String [][] differenceArtifactIds;

    public BedDifferencesAccess(D4EArtifact artifact) {
        super(artifact);
    }

    public String getYearEpoch() {
        yearEpoch = getString("ye_select");
        return yearEpoch;
    }

    public String [] getDiffs() {
        if (diffs == null) {
            diffs = getString("diffids").split("#");
            if (log.isDebugEnabled()) {
                log.debug("diffs: " + Arrays.toString(diffs));
            }
        }
        return diffs;
    }

    public String[][] getDifferenceArtifactIds() {

        if (differenceArtifactIds == null) {
            String [] diffs = getDiffs();
            differenceArtifactIds = new String[diffs.length/2][2];
            for (int i = 0; i < diffs.length/2; i++) {
                String diff1 = StringUtil.unbracket(diffs[0 + 2*i]);
                String diff2 = StringUtil.unbracket(diffs[1 + 2*i]);
                String[] diff1parts = diff1.split(";");
                String[] diff2parts = diff2.split(";");
                if (log.isDebugEnabled()) {
                    log.debug("creating 2 artifacts."
                        + diff1parts[0] + "; " + diff2parts[0]);
                }
                differenceArtifactIds[i][0] = diff1parts[0];
                differenceArtifactIds[i][1] = diff2parts[0];
            }
        }

        return differenceArtifactIds;
    }

    public String[] getDifferenceArtifactNamePairs() {

        String [] diffs = getDiffs();
        String [] result = new String[diffs.length/2];
        for (int i = 0; i < diffs.length/2; i++) {
            String diff1 = StringUtil.unbracket(diffs[0 + 2*i]);
            String diff2 = StringUtil.unbracket(diffs[1 + 2*i]);
            String[] diff1parts = diff1.split(";");
            String[] diff2parts = diff2.split(";");
            result[i] = diff1parts[3] + " - " + diff2parts[3];
        }

        return result;
    }

    public int [][] extractHeightIds(CallContext context) {
        String [][] artifactsIds = getDifferenceArtifactIds();
        int [][] ids = new int[artifactsIds.length][2];
        for (int i = 0; i < artifactsIds.length; ++i) {
            D4EArtifact a1 = RiverUtils.getArtifact(
                artifactsIds[i][0], context);
            D4EArtifact a2 = RiverUtils.getArtifact(
                artifactsIds[i][1], context);
            ids[i][0] = getHeightId(a1);
            ids[i][1] = getHeightId(a2);
        }
        return ids;
    }

    public static int getHeightId(D4EArtifact artifact) {
        Access a = new Access(artifact);
        return a.getInteger("height_id");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
