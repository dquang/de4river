/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.CrossSection;
import org.dive4elements.river.model.CrossSectionPoint;
import org.dive4elements.river.model.CrossSectionLine;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A CrossSectionLine (containing points) ready to be transformed into a mapped
 * object and written to db (used in importer).
 */
public class ImportCrossSectionLine
{
    public static final Comparator<CrossSectionPoint> INDEX_CMP =
        new Comparator<CrossSectionPoint>() {
            public int compare(CrossSectionPoint a, CrossSectionPoint b) {
                return a.getColPos().compareTo(b.getColPos());
            }
        };

    protected Double km;
    protected ImportCrossSection crossSection;
    protected List<XY> points;

    protected CrossSectionLine peer;

    public ImportCrossSectionLine() {
    }

    public ImportCrossSectionLine(Double km, List<XY> points) {
        this.km     = km;
        this.points = points;
    }

    public ImportCrossSection getCrossSection() {
        return crossSection;
    }

    public void setCrossSection(ImportCrossSection crossSection) {
        this.crossSection = crossSection;
    }

    public Double getKm() {
        return km;
    }

    public void setKm(Double km) {
        this.km = km;
    }

    public void storeDependencies() {
        storePoints();
    }


    /** Write a line and its points. */
    protected void storePoints() {
        CrossSectionLine csl = getPeer();

        Map<CrossSectionPoint, CrossSectionPoint> map =
            new TreeMap<CrossSectionPoint, CrossSectionPoint>(INDEX_CMP);

        // Build index for faster (index) collision lookup.
        List<CrossSectionPoint> ps = csl.getPoints();
        if (ps != null) {
            for (CrossSectionPoint point: ps) {
                map.put(point, point);
            }
        }

        Session session =
            ImporterSession.getInstance().getDatabaseSession();

        CrossSectionPoint key = new CrossSectionPoint();

        // Somehow it looks as if even with the map it is still possible that
        // multiple points with same id enter hibernate (and then violate a
        // constraint). -> TODO
        for (XY xy: points) {
            key.setColPos(xy.getIndex());
            CrossSectionPoint csp = map.get(key);
            if (csp == null) { // create new
                csp = new CrossSectionPoint(
                    csl, key.getColPos(),
                    Double.valueOf(xy.getX()),
                    Double.valueOf(xy.getY()));
            }
            else { // update old
                csp.setX(Double.valueOf(xy.getX()));
                csp.setY(Double.valueOf(xy.getY()));
            }
            session.save(csp);
        }
    }

    /** Pull database-mapped object from db, or create (and save) one. */
    public CrossSectionLine getPeer() {
        if (peer == null) {
            CrossSection cs = crossSection.getPeer();

            Session session =
                ImporterSession.getInstance().getDatabaseSession();

            Query query = session.createQuery(
                "from CrossSectionLine where crossSection=:cs and km=:km");
            query.setParameter("cs", cs);
            query.setParameter("km", km);

            List<CrossSectionLine> lines = query.list();
            if (lines.isEmpty()) {
                peer = new CrossSectionLine(cs, km);
                session.save(peer);
            }
            else {
                peer = lines.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
