/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import java.awt.geom.Point2D;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Entity
@Table(name = "cross_section_lines")
public class CrossSectionLine
implements   Serializable
{
    private static Logger log = LogManager.getLogger(CrossSectionLine.class);

    public static final double EPSILON   = 1e-4;

    private Integer                 id;
    private Double                  km;
    private CrossSection            crossSection;

    private List<CrossSectionPoint> points;

    public static final Comparator<CrossSectionPoint> COL_POS_CMP =
        new Comparator<CrossSectionPoint>() {
            @Override
            public int compare(CrossSectionPoint a, CrossSectionPoint b) {
                double xa = a.getX().doubleValue();
                double xb = b.getX().doubleValue();
                double d = xa - xb;
                if (d < -EPSILON) return -1;
                if (d > +EPSILON) return +1;
                int diff = a.getColPos() - b.getColPos();
                return diff < 0 ? -1 : diff > 0 ? +1 : 0;
            }
        };


    public CrossSectionLine() {
    }

    public CrossSectionLine(CrossSection crossSection, Double km) {
        this.crossSection = crossSection;
        this.km           = km;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_CROSS_SECTION_LINES_ID_SEQ",
        sequenceName   = "CROSS_SECTION_LINES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_CROSS_SECTION_LINES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "km")
    public Double getKm() {
        return km;
    }

    public void setKm(Double km) {
        this.km = km;
    }

    @OneToOne
    @JoinColumn(name = "cross_section_id")
    public CrossSection getCrossSection() {
        return crossSection;
    }

    public void setCrossSection(CrossSection crossSection) {
        this.crossSection = crossSection;
    }

    @OneToMany
    @JoinColumn(name="cross_section_line_id")
    public List<CrossSectionPoint> getPoints() {
        return points;
    }

    public void setPoints(List<CrossSectionPoint> points) {
        this.points = points;
    }


    public List<Point2D> fetchCrossSectionLinesPoints() {

        List<CrossSectionPoint> linePoints =
            new ArrayList<CrossSectionPoint>(getPoints());

        Collections.sort(linePoints, COL_POS_CMP);

        List<Point2D> points = new ArrayList<Point2D>(linePoints.size());
        for (CrossSectionPoint p: linePoints) {
            double x = p.getX().doubleValue();
            double y = p.getY().doubleValue();
            points.add(new Point2D.Double(x, y));
        }

        return points;
    }

    public double [][] fetchCrossSectionProfile() {
        return fetchCrossSectionProfile(fetchCrossSectionLinesPoints());
    }

    /** double[][] from List<Point2D> */
    public static double [][] fetchCrossSectionProfile(List<Point2D> points) {

        int P = points.size();

        double [] xs = new double[P];
        double [] ys = new double[P];

        if (P > 0) {
            xs[0] = points.get(0).getX();
            ys[0] = points.get(0).getY();

            for (int i = 1; i < P; i++) {
                Point2D p = points.get(i);
                double x = p.getX();
                double y = p.getY();

                if (x <= xs[i-1]) {
                    x = xs[i-1] + EPSILON;
                }

                xs[i] = x;
                ys[i] = y;
            }
        }

        return new double [][] { xs, ys };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
