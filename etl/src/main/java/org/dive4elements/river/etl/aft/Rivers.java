/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import org.dive4elements.river.etl.db.ConnectedStatements;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Rivers
{
    private static Logger log = LogManager.getLogger(Rivers.class);

    public Rivers() {
    }

    private static List<River> findFLYSRivers(
        List<River> flysRivers,
        String      needle
    ) {
        List<River> rivers = new ArrayList<River>();

        needle = needle.toLowerCase();

        for (River river: flysRivers) {
            String name = river.getName().toLowerCase();
            if (name.contains(needle)) {
                rivers.add(river);
            }
        }

        return rivers;
    }

    public boolean sync(SyncContext context) throws SQLException {

        log.info("sync: rivers");

        ConnectedStatements flysStatements = context.getFlysStatements();
        ConnectedStatements aftStatements  = context.getAftStatements();

        List<River> flysRivers = new ArrayList<River>();

        ResultSet flysRs = flysStatements
            .getStatement("select.rivers").executeQuery();

        try {
            while (flysRs.next()) {
                int    id   = flysRs.getInt("id");
                String name = flysRs.getString("name");
                double from = flysRs.getDouble("min_km");
                double to   = flysRs.getDouble("max_km");
                flysRivers.add(new River(id, name, from, to));
            }
        }
        finally {
            flysRs.close();
        }

        List<River> commonRivers = new ArrayList<River>();

        ResultSet aftRs = aftStatements
            .getStatement("select.gewaesser").executeQuery();

        try {
            while (aftRs.next()) {
                String name = aftRs.getString("NAME");
                int    id2  = aftRs.getInt("GEWAESSER_NR");
                for (River river: findFLYSRivers(flysRivers, name)) {
                    river.setId2(id2);
                    commonRivers.add(river);
                }
            }
        }
        finally {
            aftRs.close();
        }

        boolean modified = false;

        if (log.isDebugEnabled()) {
            log.debug("Rivers found in FLYS and AFT:");
            for (River river: commonRivers) {
                log.debug("  " + river.getName());
            }
            log.debug("---");
        }

        for (River river: commonRivers) {
            modified |= river.sync(context);
        }

        return modified;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
