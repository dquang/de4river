/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

/**
 * Text, position on axis, and maybe a hit-point in a class.
 *
 * Idea is to draw a given text and a line to it from either axis.
 * This class just keeps the info.
 */
public class StickyAxisAnnotation {

    /** Simplified view on axes. */
    public static enum SimpleAxis {
        X_AXIS, /** Usually "horizontal". */
        Y_AXIS, /** Usually "vertical". */
        Y_AXIS2
    }

    /** The "symbolic" integer representing which axis to stick to. */
    protected int axisSymbol;

    /** Which axis to stick to. */
    protected SimpleAxis stickyAxis = SimpleAxis.X_AXIS;

    /** The 1-dimensional position of this annotation. */
    protected float pos;

    /**
     * Optional field used when from axis a line should be drawn that
     * hits a curve or something similar (current scenario: duration curves).
     * This value is in the "other" dimension than the pos - field.
     */
    protected float hitPoint;

    /** The text to display at axis. */
    String text;


    /**
     * Constructor with implicit sticky x-axis.
     * @param text the text to display.
     * @param pos  the position at which to draw the text and mark.
     */
    public StickyAxisAnnotation(String text, float pos) {
        this(text, pos, SimpleAxis.X_AXIS);
    }


    /**
     * Constructor with given explicit axis.
     * @param text       the text to display.
     * @param pos        the position at which to draw the text and mark.
     * @param stickAxis the axis at which to stick (and to which 'pos' is
     *                   relative).
     */
    public StickyAxisAnnotation(String text, float pos, SimpleAxis stickAxis
    ) {
        this(text, pos, stickAxis, 0);
    }


    /**
     * Constructor with given explicit axis and axisSymbol
     * @param text       the text to display.
     * @param pos        the position at which to draw the text and mark.
     * @param stickAxis  the axis at which to stick (and to which 'pos' is
     *                   relative).
     */
    public StickyAxisAnnotation(String text, float pos, SimpleAxis stickAxis,
            int axisSymbol
    ) {
        setStickyAxis(stickAxis);
        this.text   = text;
        this.pos    = pos;
        this.axisSymbol = axisSymbol;
        this.hitPoint = Float.NaN;
    }


    /**
     * Sets the "sticky axis" (whether to draw annotations at the
     * X- or the Y-Axis.
     *
     * @param stickyAxis axis to stick to.
     */
    public void setStickyAxis(SimpleAxis stickyAxis) {
        this.stickyAxis = stickyAxis;
    }


    /** The position (relative to axis). */
    public float getPos() {
        return this.pos;
    }

    /** The position (relative to axis). */
    public void setPos(double pos) {
        this.pos = (float) pos;
    }

    public SimpleAxis getStickyAxis() {
        return this.stickyAxis;
    }

    /** True if at x axis. */
    public boolean atX() {
        return this.getStickyAxis() == SimpleAxis.X_AXIS;
    }

    /** Get text to be displayed at axis. */
    public String getText() {
        return this.text;
    }


    public int getAxisSymbol() {
        return this.axisSymbol;
    }

    public void setAxisSymbol(int axis) {
        this.axisSymbol = axis;
    }

    /** Set where to hit a curve (if any). */
    public void setHitPoint(float pos) {
        this.hitPoint = pos;
    }

    /** Get where to hit a curve (if any). */
    public float getHitPoint() {
        return this.hitPoint;
    }

    /** Set sticky axis to the X axis if it is currently Y, and vice versa. */
    public void flipStickyAxis() {
        if (this.getStickyAxis() == SimpleAxis.X_AXIS) {
            this.setStickyAxis(SimpleAxis.Y_AXIS);
        }
        else {
            this.setStickyAxis(SimpleAxis.X_AXIS);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
