/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.java2d;

import java.util.Map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Image;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.GraphicsConfiguration;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.image.RenderedImage;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;

import java.awt.image.renderable.RenderableImage;

import java.awt.geom.AffineTransform;

import java.text.AttributedCharacterIterator;

import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;

import java.awt.RenderingHints;

public final class NOPGraphics2D
extends            Graphics2D
{
    private Graphics2D parent;

    public NOPGraphics2D(Graphics2D parent) {
        this.parent = parent;
    }

    @Override
    public final void addRenderingHints(Map<?,?> hints) {
        parent.addRenderingHints(hints);
    }

    @Override
    public final void clip(Shape s) {
    }

    @Override
    public final void draw(Shape s) {
    }

    @Override
    public final void drawGlyphVector(GlyphVector g, float x, float y) {
    }

    @Override
    public final void drawImage(
        BufferedImage   img,
        BufferedImageOp op,
        int x,
        int y
    ) {
    }

    @Override
    public final boolean drawImage(
        Image           img,
        AffineTransform xform,
        ImageObserver   obs
    ) {
        return true;
    }

    @Override
    public final void drawRenderableImage(
        RenderableImage img,
        AffineTransform xform
    ) {
    }

    @Override
    public final void drawRenderedImage(
        RenderedImage   img,
        AffineTransform xform
    ) {
    }

    @Override
    public final void drawString(
        AttributedCharacterIterator iterator,
        float x,
        float y
    ) {
    }

    @Override
    public final void drawString(
        AttributedCharacterIterator iterator,
        int x,
        int y
    ) {
    }

    @Override
    public final void drawString(String str, float x, float y) {
    }

    @Override
    public final void drawString(String str, int x, int y) {
    }

    @Override
    public final void fill(Shape s) {
    }

    @Override
    public final Color getBackground() {
        return parent.getBackground();
    }

    @Override
    public final Composite getComposite() {
        return parent.getComposite();
    }

    @Override
    public final GraphicsConfiguration getDeviceConfiguration() {
        return parent.getDeviceConfiguration();
    }

    @Override
    public final FontRenderContext getFontRenderContext() {
        return parent.getFontRenderContext();
    }

    @Override
    public final Paint getPaint() {
        return parent.getPaint();
    }

    @Override
    public final Object getRenderingHint(RenderingHints.Key hintKey) {
        return parent.getRenderingHint(hintKey);
    }

    @Override
    public final RenderingHints getRenderingHints() {
        return parent.getRenderingHints();
    }

    @Override
    public final Stroke getStroke() {
        return parent.getStroke();
    }

    @Override
    public final AffineTransform getTransform() {
        return parent.getTransform();
    }

    @Override
    public final boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return parent.hit(rect, s, onStroke);
    }

    @Override
    public final void rotate(double theta) {
        parent.rotate(theta);
    }

    @Override
    public final void rotate(double theta, double x, double y) {
        parent.rotate(theta);
    }

    @Override
    public final void scale(double sx, double sy) {
        parent.scale(sx, sy);
    }

    @Override
    public final void setBackground(Color color) {
        parent.setBackground(color);
    }

    @Override
    public final void setComposite(Composite comp) {
        parent.setComposite(comp);
    }

    @Override
    public final void setPaint(Paint paint) {
        parent.setPaint(paint);
    }

    @Override
    public final void setRenderingHint(
        RenderingHints.Key hintKey,
        Object             hintValue
    ) {
        parent.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public final void setRenderingHints(Map<?,?> hints) {
        parent.setRenderingHints(hints);
    }

    @Override
    public final void setStroke(Stroke s) {
        parent.setStroke(s);
    }

    @Override
    public final void setTransform(AffineTransform Tx) {
        parent.setTransform(Tx);
    }


    @Override
    public final void shear(double shx, double shy) {
        parent.shear(shx, shy);
    }

    @Override
    public final void transform(AffineTransform Tx) {
        parent.transform(Tx);
    }

    @Override
    public final void translate(double tx, double ty) {
        parent.translate(tx, ty);
    }

    @Override
    public final void translate(int tx, int ty) {
        parent.translate(tx, ty);
    }

    @Override
    public final void dispose() {
        parent.dispose();
    }

    @Override
    public final boolean drawImage(
        Image img,
        int x,
        int y,
        int width,
        int height,
        Color bgcolor,
        ImageObserver observer
    ) {
        return true;
    }

    @Override
    public final boolean drawImage(
        Image img,
        int dx1,
        int dy1,
        int dx2,
        int dy2,
        int sx1,
        int sy1,
        int sx2,
        int sy2,
        Color bgcolor,
        ImageObserver observer
    ) {
        return true;
    }

    @Override
    public final boolean drawImage(
        Image img,
        int dx1,
        int dy1,
        int dx2,
        int dy2,
        int sx1,
        int sy1,
        int sx2,
        int sy2,
        ImageObserver observer
    ) {
        return true;
    }

    @Override
    public final boolean drawImage(
        Image img,
        int x,
        int y,
        Color bgcolor,
        ImageObserver observer
    ) {
        return true;
    }

    @Override
    public final boolean drawImage(
        Image img,
        int x,
        int y,
        int width,
        int height,
        ImageObserver observer
    ) {
        return true;
    }

    @Override
    public final boolean drawImage(
        Image img,
        int x,
        int y,
        ImageObserver observer
    ) {
        return true;
    }

    @Override
    public final void fillPolygon(
        int [] xPoints,
        int [] yPoints,
        int    nPoints
    ) {
    }

    @Override
    public final void drawPolygon(
        int [] xPoints,
        int [] yPoints,
        int    nPoints
    ) {
    }

    @Override
    public final void drawPolyline(
        int [] xPoints,
        int [] yPoints,
        int    nPoints
    ) {
    }

    @Override
    public final void fillArc(
        int x,
        int y,
        int width,
        int height,
        int startAngle,
        int arcAngle
    ) {
    }

    @Override
    public final void drawArc(
        int x,
        int y,
        int width,
        int height,
        int startAngle,
        int arcAngle
    ) {
    }

    @Override
    public final void fillOval(
        int x,
        int y,
        int width,
        int height
    ) {
    }

    @Override
    public final void drawOval(
        int x,
        int y,
        int width,
        int height
    ) {
    }

    @Override
    public final void fillRoundRect(
        int x,
        int y,
        int width,
        int height,
        int arcWidth,
        int arcHeight
    ) {
    }

    @Override
    public final void drawRoundRect(
        int x,
        int y,
        int width,
        int height,
        int arcWidth,
        int arcHeight
    ) {
    }

    @Override
    public final void clearRect(
        int x,
        int y,
        int width,
        int height
    ) {
    }

    @Override
    public final void fillRect(
        int x,
        int y,
        int width,
        int height
    ) {
    }

    @Override
    public final void drawLine(
        int x1,
        int y1,
        int x2,
        int y2
    ) {
    }

    @Override
    public final void copyArea(
        int x,
        int y,
        int width,
        int height,
        int dx,
        int dy
    ) {
    }

    @Override
    public final void setClip(
        int x,
        int y,
        int width,
        int height
    ) {
        parent.setClip(x, y, width, height);
    }

    @Override
    public final void setClip(Shape shape) {
        parent.setClip(shape);
    }

    @Override
    public final Shape getClip() {
        return parent.getClip();
    }

    @Override
    public final void clipRect(
        int x,
        int y,
        int width,
        int height
    ) {
        parent.clipRect(x, y, width, height);
    }

    @Override
    public final Rectangle getClipBounds() {
        return parent.getClipBounds();
    }

    @Override
    public final FontMetrics getFontMetrics(Font f) {
        return parent.getFontMetrics(f);
    }

    @Override
    public final void setFont(Font font) {
        parent.setFont(font);
    }

    @Override
    public final Font getFont() {
        return parent.getFont();
    }

    @Override
    public final void setXORMode(Color c1) {
        parent.setXORMode(c1);
    }

    @Override
    public final void setPaintMode() {
        parent.setPaintMode();
    }

    @Override
    public final void setColor(Color c) {
        parent.setColor(c);
    }

    @Override
    public final Color getColor() {
        return parent.getColor();
    }

    @Override
    public final Graphics create() {
        return new NOPGraphics2D((Graphics2D)parent.create());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
