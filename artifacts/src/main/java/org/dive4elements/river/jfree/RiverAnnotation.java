/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import org.dive4elements.river.artifacts.model.HYKFactory;
import org.dive4elements.river.themes.ThemeDocument;

import java.util.Collections;
import java.util.List;

import org.jfree.chart.annotations.XYTextAnnotation;

/**
 * List of Text- Annotations (Sticky to one axis or in space)
 * and 'HYK'-Annotations (rectangles/areas) with name and theme.
 */
public class RiverAnnotation {

    /** 'Other' Text Annotations. */
    protected List<XYTextAnnotation> textAnnotations;

    /** Annotations at axis. */
    protected List<StickyAxisAnnotation> axisTextAnnotations;

    /** Areas at axis. */
    protected List<HYKFactory.Zone> boxes;

    /** Styling information. */
    protected ThemeDocument theme;

    /** Chart-legend information. */
    protected String label;


    public RiverAnnotation(
        String label,
        List<StickyAxisAnnotation> annotations
    ) {
        this(label, annotations, null, null);
    }


    /** Create annotations, parameter might be null. */
    public RiverAnnotation(
        String label,
        List<StickyAxisAnnotation> annotations,
        List<HYKFactory.Zone> bAnnotations
    ) {
        this(label, annotations, bAnnotations, null);
    }


    /** Create annotations, parameter might be null. */
    public RiverAnnotation(
        String label,
        List<StickyAxisAnnotation> annotations,
        List<HYKFactory.Zone> bAnnotations,
        ThemeDocument theme
    ) {
        this.label = label;
        this.axisTextAnnotations = (annotations != null)
            ? annotations
            : Collections.<StickyAxisAnnotation>emptyList();
        this.boxes = (bAnnotations != null)
            ? bAnnotations
            : Collections.<HYKFactory.Zone>emptyList();
        this.textAnnotations = Collections.<XYTextAnnotation>emptyList();
        this.setTheme(theme);
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public List<StickyAxisAnnotation> getAxisTextAnnotations() {
        return axisTextAnnotations;
    }

    public void setTextAnnotations(List<XYTextAnnotation> annotations) {
        this.textAnnotations = annotations;
    }

    /** Set the "other" Text Annotations. */
    public List<XYTextAnnotation> getTextAnnotations() {
        return textAnnotations;
    }

    public List<HYKFactory.Zone> getBoxes() {
        return boxes;
    }

    public void setTheme(ThemeDocument theme) {
        this.theme = theme;
    }

    public ThemeDocument getTheme() {
        return theme;
    }

    /**
     * Set sticky axis of all axisTextAnnotations
     * to the X axis if it is currently Y, and vice versa.
     * @return this
     */
    public RiverAnnotation flipStickyAxis() {
        for (StickyAxisAnnotation saa: axisTextAnnotations) {
            saa.flipStickyAxis();
        }
        return this;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
