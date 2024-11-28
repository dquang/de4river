/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.model.River;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.themes.ThemeDocument;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.awt.Shape;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StyledXYSeries
extends XYSeries
implements StyledSeries, HasLabel, XYMetaDataset {

    private static final Logger log = LogManager.getLogger(StyledXYSeries.class);

    protected Style style;

    /** If this Series is to be labelled, use this String as label. */
    protected String label;

    /** The meta data for this series. */
    protected Map<String, String> metaData;

    public StyledXYSeries(String key, ThemeDocument theme) {
        this(key, true, theme, (Shape)null);
    }


    public StyledXYSeries(
        String key,
        ThemeDocument theme,
        XYSeries unstyledSeries
    ) {
        this(key, theme);
        add(unstyledSeries);
    }

    public StyledXYSeries(String key, boolean sorted, ThemeDocument theme) {
        this(key, sorted, theme, (Shape)null);
    }


    public StyledXYSeries(String key, ThemeDocument theme, Shape shape) {
        this(key, true, theme, shape);
    }

    /**
     * @param sorted whether or not to sort the points. Sorting will move NANs
     *               to one extrema which can cause problems in certain
     *               algorithms.
     */
    public StyledXYSeries(
        String key,
        boolean sorted,
        ThemeDocument theme,
        Shape shape
    ) {
        super(key, sorted);
        setStyle(new XYStyle(theme, shape));
        this.label = key.toString();
    }

    public StyledXYSeries(
        String        key,
        boolean       sorted,
        boolean       allowDuplicateXValues,
        ThemeDocument theme
    ) {
        this(key, sorted, allowDuplicateXValues, theme, (Shape)null);
    }

    public StyledXYSeries(
        String        key,
        boolean       sorted,
        boolean       allowDuplicateXValues,
        ThemeDocument theme,
        Shape         shape
    ) {
        super(key, sorted, allowDuplicateXValues);
        setStyle(new XYStyle(theme, shape));
        this.label = key.toString();
    }


    @Override
    public void setStyle(Style style) {
        this.style = style;
    }


    @Override
    public Style getStyle() {
        return style;
    }


    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    protected void add(XYSeries series) {
        List<XYDataItem> items = series.getItems();
        add(items);
    }

    protected void add(List<XYDataItem> items) {
        for(XYDataItem item : items) {
            add(item.getXValue(), item.getYValue());
        }
    }


    @Override
    public Map<String, String> getMetaData() {
        return metaData;
    }


    @Override
    public void putMetaData(Map<String, String> metaData,
        Artifact artifact,
        CallContext context) {
        this.metaData = metaData;
        River river = new RiverAccess((D4EArtifact)artifact).getRiver();
        String rivername = "";
        String unit = "";
        if (river != null) {
            rivername = river.getName();
            unit      = river.getWstUnit().getName();
        }
        if (metaData.containsKey("X")) {
            this.metaData.put("X",
                Resources.getMsg(
                    context.getMeta(),
                    metaData.get("X"),
                    new Object[] { rivername }));
        }
        if (metaData.containsKey("Y")) {
            this.metaData.put("Y",
                Resources.getMsg(
                    context.getMeta(),
                    metaData.get("Y"), new Object[] { unit }));
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
