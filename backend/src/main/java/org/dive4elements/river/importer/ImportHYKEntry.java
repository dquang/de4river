/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.HYKEntry;
import org.dive4elements.river.model.HYK;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import java.math.BigDecimal;

import org.hibernate.Session;
import org.hibernate.Query;

public class ImportHYKEntry
{
    protected ImportHYK  hyk;
    protected BigDecimal km;
    protected Date       measure;

    protected List<ImportHYKFormation> formations;

    protected HYKEntry peer;

    public ImportHYKEntry() {
        formations = new ArrayList<ImportHYKFormation>();
    }

    public ImportHYKEntry(
        ImportHYK  hyk,
        BigDecimal km,
        Date       measure
    ) {
        this();
        this.hyk     = hyk;
        this.km      = km;
        this.measure = measure;
    }

    public ImportHYK getHYK() {
        return hyk;
    }

    public void setHYK(ImportHYK hyk) {
        this.hyk = hyk;
    }

    public BigDecimal getKm() {
        return km;
    }

    public void setKm(BigDecimal km) {
        this.km = km;
    }

    public void addFormation(ImportHYKFormation formation) {
        int numFormation = formations.size();
        formations.add(formation);
        formation.setFormationNum(numFormation);
        formation.setEntry(this);
    }

    public void storeDependencies() {
        getPeer();
        for (ImportHYKFormation formation: formations) {
            formation.storeDependencies();
        }
    }

    public HYKEntry getPeer() {
        if (peer == null) {
            HYK h = hyk.getPeer();
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from HYKEntry where HYK=:hyk " +
                "and km=:km and measure=:measure");
            query.setParameter("hyk", h);
            query.setParameter("km", km);
            query.setParameter("measure", measure);
            List<HYKEntry> entries = query.list();
            if (entries.isEmpty()) {
                peer = new HYKEntry(h, km, measure);
                session.save(peer);
            }
            else {
                peer = entries.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
