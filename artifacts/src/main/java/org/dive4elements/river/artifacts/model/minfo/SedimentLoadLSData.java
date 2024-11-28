/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dive4elements.river.artifacts.model.NamedObjectImpl;
import org.dive4elements.river.artifacts.model.Range;
import org.dive4elements.river.backend.utils.EpsilonComparator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Gives access to Fractions (at kms). */
public class SedimentLoadLSData
extends NamedObjectImpl
{
    /** Private log. */
    private static final Logger log = LogManager
        .getLogger(SedimentLoadLSData.class);

    protected String description;
    protected Date start;
    protected Date end;
    protected boolean isEpoch;
    protected String unit;

    protected Map<Double, SedimentLoadFraction> kms;

    public SedimentLoadLSData() {
        kms = new TreeMap<Double, SedimentLoadFraction>(EpsilonComparator.CMP);
    }

    public SedimentLoadLSData(
        String description,
        Date start,
        Date end,
        boolean isEpoch,
        String unit
    ) {
        this();
        this.description = description;
        this.start = start;
        this.end = end;
        this.isEpoch = isEpoch;
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isEpoch() {
        return isEpoch;
    }

    public void setEpoch(boolean isEpoch) {
        this.isEpoch = isEpoch;
    }

    public Set<Double> getKms() {
        return kms.keySet();
    }

    public void addKm(double km, SedimentLoadFraction fraction) {
        kms.put(km, fraction);
    }

    public SedimentLoadFraction getFraction(double km) {
        SedimentLoadFraction f = kms.get(km);
        if (f == null) {
            f = new SedimentLoadFraction();
            kms.put(km, f);
        }
        return f;
    }

    public void setCoarse(double km, double coarse, Range range) {
        if (range == null) {
            log.error("coarse/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setCoarse(coarse);
        f.setCoarseRange(range);
    }

    public void setFineMiddle(double km, double fine_middle, Range range) {
        if (range == null) {
            log.error("finemiddle/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setFineMiddle(fine_middle);
        f.setFineMiddleRange(range);
    }


    public void setSand(double km, double sand, Range range) {
        if (range == null) {
            log.error("sand/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setSand(sand);
        f.setSandRange(range);
    }

    public void setSuspSand(double km, double susp_sand, Range range) {
        if (range == null) {
            log.error("suspsand/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setSuspSand(susp_sand);
        f.setSuspSandRange(range);
    }

    public void setSuspSandBed(double km, double susp_sand_bed, Range range) {
        if (range == null) {
            log.error("suspsandbed/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setSuspSandBed(susp_sand_bed);
        f.setSuspSandBedRange(range);
    }

    public void setSuspSediment(double km, double susp_sediment, Range range) {
        if (range == null) {
            log.error("suspsed/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setSuspSediment(susp_sediment);
        f.setSuspSedimentRange(range);
    }

    public void setLoadTotal(double km, double total) {
        setLoadTotal(km, total, null);
    }

    public void setLoadTotal(double km, double total, Range range) {
        if (range == null) {
            log.error("loadtotal/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setLoadTotal(total);
        f.setLoadTotalRange(range);
    }

    public void setTotal(double km, double total, Range range) {
        if (range == null) {
            log.error("total/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setTotal(total);
        f.setTotalRange(range);
    }

    public void setUnknown(double km, double unknown, Range range) {
        if (range == null) {
            log.error("unknown/range is null!");
            return;
        }
        SedimentLoadFraction f = getFraction(km);
        f.setUnknown(unknown);
        f.setUnknownRange(range);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean hasCoarse() {
        for (SedimentLoadFraction slf : kms.values()) {
            if (slf.getCoarse() > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFineMiddle() {
        for (SedimentLoadFraction slf : kms.values()) {
            if (slf.getFineMiddle() > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSand() {
        for (SedimentLoadFraction slf : kms.values()) {
            if (slf.getSand() > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSuspSand() {
        for (SedimentLoadFraction slf : kms.values()) {
            if (slf.getSuspSand() > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSuspSediment() {
        for (SedimentLoadFraction slf : kms.values()) {
            if (slf.getSuspSediment() > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTotalLoad() {
        for (SedimentLoadFraction slf : kms.values()) {
            if (slf.getLoadTotal() > 0d) {
                return true;
            }
        }
        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
