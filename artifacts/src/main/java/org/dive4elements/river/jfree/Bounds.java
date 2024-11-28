/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;


/**
 * Somewhat better Ranges.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Bounds extends Serializable {

    Number getLower();

    Number getUpper();

    void applyBounds(ValueAxis axis);

    void applyBounds(ValueAxis axis, int percent);

    Bounds combine(Bounds bounds);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
