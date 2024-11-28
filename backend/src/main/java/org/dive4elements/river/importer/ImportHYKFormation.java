/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.HYKFormation;
import org.dive4elements.river.model.HYKEntry;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Query;

import java.math.BigDecimal;

public class ImportHYKFormation
{
    protected int            formationNum;
    protected ImportHYKEntry entry;
    protected BigDecimal     top;
    protected BigDecimal     bottom;
    protected BigDecimal     distanceVL;
    protected BigDecimal     distanceHF;
    protected BigDecimal     distanceVR;

    protected List<ImportHYKFlowZone> zones;

    protected HYKFormation peer;

    public ImportHYKFormation() {
        zones = new ArrayList<ImportHYKFlowZone>();
    }

    public ImportHYKFormation(
        int            formationNum,
        ImportHYKEntry entry,
        BigDecimal     top,
        BigDecimal     bottom,
        BigDecimal     distanceVL,
        BigDecimal     distanceHF,
        BigDecimal     distanceVR
    ) {
        this();
        this.formationNum = formationNum;
        this.entry        = entry;
        this.top          = top;
        this.bottom       = bottom;
        this.distanceVL   = distanceVL;
        this.distanceHF   = distanceHF;
        this.distanceVR   = distanceVR;
    }

    public void addFlowZone(ImportHYKFlowZone zone) {
        zones.add(zone);
        zone.setFormation(this);
    }

    public int getFormationNum() {
        return formationNum;
    }

    public void setFormationNum(int formationNum) {
        this.formationNum = formationNum;
    }

    public ImportHYKEntry getEntry() {
        return entry;
    }

    public void setEntry(ImportHYKEntry entry) {
        this.entry = entry;
    }

    public BigDecimal getTop() {
        return top;
    }

    public void setTop(BigDecimal top) {
        this.top = top;
    }

    public BigDecimal getBottom() {
        return bottom;
    }

    public void setBottom(BigDecimal bottom) {
        this.bottom = bottom;
    }

    public BigDecimal getDistanceVL() {
        return distanceVL;
    }

    public void setDistanceVL(BigDecimal distanceVL) {
        this.distanceVL = distanceVL;
    }

    public BigDecimal getDistanceHF() {
        return distanceHF;
    }

    public void setDistanceHF(BigDecimal distanceHF) {
        this.distanceHF = distanceHF;
    }

    public BigDecimal getDistanceVR() {
        return distanceVR;
    }

    public void setDistanceVR(BigDecimal distanceVR) {
        this.distanceVR = distanceVR;
    }

    public void storeDependencies() {
        getPeer();
        for (ImportHYKFlowZone zone: zones) {
            zone.storeDependencies();
        }
    }

    public HYKFormation getPeer() {
        if (peer == null) {
            HYKEntry e = entry.getPeer();
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from HYKFormation where formationNum=:formationNum " +
                "and entry=:entry and top=:top and bottom=:bottom " +
                "and distanceVL=:distanceVL and distanceHF=:distanceHF " +
                "and distanceVR=:distanceVR");
            query.setParameter("formationNum", formationNum);
            query.setParameter("entry", e);
            query.setParameter("top", top);
            query.setParameter("bottom", bottom);
            query.setParameter("distanceVL", distanceVL);
            query.setParameter("distanceHF", distanceHF);
            query.setParameter("distanceVR", distanceVR);
            List<HYKFormation> formations = query.list();
            if (formations.isEmpty()) {
                peer = new HYKFormation(
                    formationNum, e, top, bottom,
                    distanceVL, distanceHF, distanceVR);
                session.save(peer);
            }
            else {
                peer = formations.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
