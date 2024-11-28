/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.Shape;

import org.jfree.chart.entity.XYAnnotationEntity;

/**
 * Chart Entity for XYTextAnnotations that should not collide.
 */
public class CollisionFreeXYTextAnnotationEntity
extends XYAnnotationEntity {
    public CollisionFreeXYTextAnnotationEntity(
        Shape hotspot,
        int rendererIndex,
        String toolTip,
        String url
    ) {
        super(hotspot, rendererIndex, toolTip, url);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
