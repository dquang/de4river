/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.HYK;
import org.dive4elements.river.model.River;

import java.util.List;
import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ImportHYK
{
    private static Logger log = LogManager.getLogger(ImportHYK.class);

    protected ImportRiver river;
    protected String      description;

    protected List<ImportHYKEntry> entries;

    protected HYK peer;

    public ImportHYK() {
        entries = new ArrayList<ImportHYKEntry>();
    }

    public ImportHYK(ImportRiver river, String description) {
        this();
        this.river       = river;
        this.description = description;
    }

    public ImportRiver getRiver() {
        return river;
    }

    public void setRiver(ImportRiver river) {
        this.river = river;
    }

    public void addEntry(ImportHYKEntry entry) {
        entries.add(entry);
        entry.setHYK(this);
    }

    public void storeDependencies() {
        log.info("store HYK '" + description + "'");
        getPeer();
        for (int i = 0, N = entries.size(); i < N; ++i) {
            ImportHYKEntry entry = entries.get(i);
            log.info("  store km " + entry.getKm() +
                " (" + (i+1) + " of " + N + ")");
            entry.storeDependencies();
        }
    }

    public HYK getPeer() {
        if (peer == null) {
            River r = river.getPeer();
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from HYK where river=:river and description=:description");
            query.setParameter("river", r);
            query.setParameter("description", description);
            List<HYK> hyks = query.list();
            if (hyks.isEmpty()) {
                peer = new HYK(r, description);
                session.save(peer);
            }
            else {
                peer = hyks.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
