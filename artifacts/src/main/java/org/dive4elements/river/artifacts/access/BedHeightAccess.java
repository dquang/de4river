/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.states.SoundingsSelect;

import gnu.trove.TIntArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class BedHeightAccess
extends      RangeAccess
{
    private static final Logger log = LogManager.getLogger(BedHeightAccess.class);

    private int[] singleIDs;
    private int[] epochIDs;

    private String yearEpoch;

    private String type;

    private Integer heightId;

    private Integer time;

    public BedHeightAccess(D4EArtifact artifact) {
        super(artifact);
    }


    public int[] getBedHeightIDs() {
        if (singleIDs == null) {
            String data = getString("soundings");

            if (data == null) {
                log.warn("No 'soundings' parameter specified!");
                return null;
            }
            else {
                log.debug("getBedHeightIDs(): data=" + data);
            }

            String[] parts = data.split(";");

            TIntArrayList ids = new TIntArrayList();

            for (String part: parts) {
                if (part.indexOf(SoundingsSelect.PREFIX_SINGLE) >= 0) {
                    String tmp = part.replace(
                        SoundingsSelect.PREFIX_SINGLE, "");

                    try {
                        int i = Integer.parseInt(tmp);
                        if (!ids.contains(i)) {
                            ids.add(i);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(
                            "Cannot parse int from string: '" + tmp + "'");
                    }
                }
            }

            singleIDs = ids.toNativeArray();
        }

        return singleIDs;
    }


    public String getType() {
        if (type == null) {
            type = getString("type");
        }
        return type;
    }

    public Integer getHeightId() {
        if (heightId == null) {
            heightId = getInteger("height_id");
        }
        return heightId;
    }

    public Integer getTime() {
        if (time == null) {
            time = getInteger("time");
        }
        return time;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
