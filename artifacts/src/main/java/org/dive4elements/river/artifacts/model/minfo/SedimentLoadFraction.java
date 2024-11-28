/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.dive4elements.river.artifacts.model.NamedObjectImpl;
import org.dive4elements.river.artifacts.model.Range;

/** One part of sedimentload. */
public class SedimentLoadFraction
extends NamedObjectImpl
{
    double sand;
    double fineMiddle;
    double coarse;
    double suspSand;
    double suspSandBed;
    double suspSediment;
    double loadTotal;
    double total;
    double unknown;
    /** Values are valid within this km range. */
    Range sandRange;
    Range fineMiddleRange;
    Range coarseRange;
    Range suspSandRange;
    Range suspSandBedRange;
    Range suspSedimentRange;
    Range loadTotalRange;
    Range totalRange;
    Range unknownRange;

    public SedimentLoadFraction() {
        sand         = Double.NaN;
        fineMiddle   = Double.NaN;
        coarse       = Double.NaN;
        suspSand     = Double.NaN;
        suspSandBed  = Double.NaN;
        suspSediment = Double.NaN;
        loadTotal    = Double.NaN;
        unknown      = Double.NaN;
    }

    public double getSand() {
        return sand;
    }

    public void setSand(double sand) {
        this.sand = sand;
    }

    public void setSandRange(Range range) {
        this.sandRange = range;
    }

    public Range getSandRange() {
        return this.sandRange;
    }

    public double getFineMiddle() {
        return fineMiddle;
    }

    public void setFineMiddle(double fineMiddle) {
        this.fineMiddle = fineMiddle;
    }

    public void setFineMiddleRange(Range range) {
        this.fineMiddleRange = range;
    }

    public Range getFineMiddleRange() {
        return this.fineMiddleRange;
    }

    public double getCoarse() {
        return coarse;
    }

    public void setCoarse(double coarse) {
        this.coarse = coarse;
    }

    public Range getCoarseRange() {
        return this.coarseRange;
    }

    public void setCoarseRange(Range range) {
        this.coarseRange = range;
    }

    public double getSuspSand() {
        return suspSand;
    }

    public void setSuspSand(double suspSand) {
        this.suspSand = suspSand;
    }

    public void setSuspSandRange(Range range) {
        this.suspSandRange = range;
    }

    public Range getSuspSandRange() {
        return this.suspSandRange;
    }

    public double getSuspSandBed() {
        return suspSandBed;
    }

    public void setSuspSandBed(double suspSandBed) {
        this.suspSandBed = suspSandBed;
    }

    public void setSuspSandBedRange(Range range) {
        this.suspSandRange = range;
    }

    public Range getSuspSandBedRange() {
        return this.suspSandRange;
    }

    public double getSuspSediment() {
        return suspSediment;
    }

    public void setSuspSediment(double suspSediment) {
        this.suspSediment = suspSediment;
    }

    public void setSuspSedimentRange(Range range) {
        this.suspSedimentRange = range;
    }

    public Range getSuspSedimentRange() {
        return this.suspSedimentRange;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setTotalRange(Range range) {
        this.totalRange = range;
    }

    public Range getTotalRange() {
        return this.totalRange;
    }

    public double getLoadTotal() {
        return loadTotal;
    }

    public void setLoadTotal(double total) {
        this.loadTotal = total;
    }

    public Range getLoadTotalRange() {
        return this.loadTotalRange;
    }

    public void setLoadTotalRange(Range range) {
        this.loadTotalRange = range;
    }

    public double getUnknown() {
        return unknown;
    }

    public void setUnknown(double unknown) {
        this.unknown = unknown;
    }

    public Range getUnknownRange() {
        return unknownRange;
    }

    public void setUnknownRange(Range unknownRange) {
        this.unknownRange = unknownRange;
    }

    /** Returns true if all fraction values except SuspSediment are unset. */
    public boolean hasOnlySuspValues() {
        return
            !Double.isNaN(getSuspSediment()) &&
            Double.isNaN(getCoarse()) &&
            Double.isNaN(getFineMiddle()) &&
            Double.isNaN(getSand()) &&
            Double.isNaN(getSuspSand());
    }

    /** Returns true if all fraction values except SuspSediment are set. */
    public boolean hasButSuspValues() {
        return
            Double.isNaN(getSuspSediment()) &&
            !Double.isNaN(getCoarse()) &&
            !Double.isNaN(getFineMiddle()) &&
            !Double.isNaN(getSand()) &&
            !Double.isNaN(getSuspSand());
    }

    /** Returns true if all fraction needed for total calculation are set. */
    public boolean isComplete() {
        return
            !Double.isNaN(getCoarse()) &&
            !Double.isNaN(getFineMiddle()) &&
            !Double.isNaN(getSand()) &&
            !Double.isNaN(getSuspSand()) &&
            !Double.isNaN(getSuspSediment());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
