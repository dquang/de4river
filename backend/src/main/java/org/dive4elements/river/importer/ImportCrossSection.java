/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.CrossSection;
import org.dive4elements.river.model.TimeInterval;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** CrossSection to be imported, holds list of ImportCrossSectionLines. */
public class ImportCrossSection
{
    private static Logger log = LogManager.getLogger(ImportCrossSection.class);

    protected ImportRiver                  river;
    protected String                       description;
    protected ImportTimeInterval           timeInterval;
    protected List<ImportCrossSectionLine> lines;

    protected CrossSection peer;

    public ImportCrossSection() {
    }

    public ImportCrossSection(
        ImportRiver                  river,
        String                       description,
        ImportTimeInterval           timeInterval,
        List<ImportCrossSectionLine> lines
    ) {
        this.river        = river;
        this.description  = description;
        this.timeInterval = timeInterval;
        this.lines        = lines;
        wireWithLines();
    }

    public void wireWithLines() {
        for (ImportCrossSectionLine line: lines) {
            line.setCrossSection(this);
        }
    }

    public ImportRiver getRiver() {
        return river;
    }

    public void setRiver(ImportRiver river) {
        this.river = river;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ImportTimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(ImportTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public void storeDependencies() {

        log.info("store cross section '" + description + "'");

        getPeer();

        int i = 1, N = lines.size();

        for (ImportCrossSectionLine line: lines) {
            line.storeDependencies();
            log.info("  stored " + i + " lines. remaining: " + (N-i));
            ++i;
        }
    }

    public CrossSection getPeer() {

        if (peer == null) {
            River r = river.getPeer();
            TimeInterval t = timeInterval != null
                ? timeInterval.getPeer()
                : null;

            Session session =
                ImporterSession.getInstance().getDatabaseSession();

            Query query = session.createQuery(
                "from CrossSection where " +
                "river=:r and "            +
                "timeInterval=:t and "     +
                "description=:d");

            query.setParameter("r", r);
            query.setParameter("t", t);
            query.setParameter("d", description);

            List<CrossSection> crossSections = query.list();
            if (crossSections.isEmpty()) {
                peer = new CrossSection(r, t, description);
                session.save(peer);
            }
            else {
                peer = crossSections.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
