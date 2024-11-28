/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DistanceInfoObjectImpl implements DistanceInfoObject {

    protected String description;

    protected Double from;

    protected Double to;

    protected String riverside;

    protected Double bottom;

    protected Double top;


    public DistanceInfoObjectImpl() {
    }


    public DistanceInfoObjectImpl(
        String description,
        Double from,
        Double to,
        String riverside,
        Double bottom,
        Double top
    ) {
        this.description = description;
        this.from        = from;
        this.to          = to;
        this.riverside   = riverside;
        this.bottom      = bottom;
        this.top         = top;
    }

    public String getDescription() {
        return description;
    }


    public Double getFrom() {
        return from;
    }


    public Double getTo() {
        return to;
    }


    public String getRiverside() {
        return riverside;
    }

    public Double getBottom() {
        return bottom;
    }

    public Double getTop() {
        return top;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
