/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@Table(name = "ranges")
public class Range
implements   Serializable
{
    public static final double EPSILON = 1e-5;
    private Integer    id;
    private BigDecimal a;
    private BigDecimal b;

    private River      river;

    public Range() {
    }

    public Range(double a, double b, River river) {
        this(new BigDecimal(a), new BigDecimal(b), river);
    }

    public Range(BigDecimal a, BigDecimal b, River river) {
        this.a     = a;
        this.b     = b;
        this.river = river;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_RANGES_ID_SEQ",
        sequenceName   = "RANGES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_RANGES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "a") // FIXME: type mapping needed?
    public BigDecimal getA() {
        return a;
    }

    public void setA(BigDecimal a) {
        this.a = a;
    }

    @Column(name = "b") // FIXME: type mapping needed?
    public BigDecimal getB() {
        return b;
    }

    public void setB(BigDecimal b) {
        this.b = b;
    }

    public boolean containsTolerant(double x) {
            return containsTolerant(x, EPSILON);
    }

    public boolean containsTolerant(double x, double tolerance) {
        BigDecimal b = this.b != null ? this.b : a;
        double av = a.doubleValue();
        double bv = b.doubleValue();
        if (av > bv) {
            double t = av;
            av = bv;
            bv = t;
        }
        return x+tolerance >= av && x-tolerance <= bv;
    }

    public boolean contains(double x) {
        BigDecimal b = this.b != null ? this.b : a;
        double av = a.doubleValue();
        double bv = b.doubleValue();
        if (av > bv) {
            double t = av;
            av = bv;
            bv = t;
        }
        return x >= av && x <= bv;
    }

    @OneToOne
    @JoinColumn(name = "river_id")
    public River getRiver() {
        return river;
    }

    public void setRiver(River river) {
        this.river = river;
    }

    public int code() {
        int code = 0;
        if (a != null) code  = 1;
        if (b != null) code |= 2;
        return code;
    }

    public boolean intersects(BigDecimal c) {
        return !(a.compareTo(c) > 0 || b.compareTo(c) < 0);
    }

    public boolean intersects(Range other) {

        int code  = code();
        int ocode = other.code();

        if (code == 0 || ocode == 0) {
            return false;
        }

        switch (code) {
            case 1: // has a
                switch (ocode) {
                    case 1: // has a
                        return a.compareTo(other.a) == 0;
                    case 2: // has b
                        return a.compareTo(other.b) == 0;
                    case 3: // has range
                        return other.intersects(a);
                }
                break;
            case 2: // has b
                switch (ocode) {
                    case 1: // has a
                        return b.compareTo(other.a) == 0;
                    case 2: // has b
                        return b.compareTo(other.b) == 0;
                    case 3: // has range
                        return other.intersects(b);
                }
                break;
            case 3: // has range
                switch (ocode) {
                    case 1: // has a
                        return intersects(other.a);
                    case 2: // has b
                        return intersects(other.b);
                    case 3: // has range
                        return !(other.b.compareTo(a) < 0
                               ||other.a.compareTo(b) > 0);
                }
                break;

        }

        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
