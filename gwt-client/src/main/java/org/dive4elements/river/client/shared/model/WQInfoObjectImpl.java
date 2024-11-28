/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.Date;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class WQInfoObjectImpl implements WQInfoObject {

    protected String name;

    protected String type;

    protected Double value;

    Date startTime;

    Date stopTime;

    protected boolean isOfficial;


    public WQInfoObjectImpl() {
    }


    public WQInfoObjectImpl(
        String name,
        String type,
        Double value,
        boolean isOfficial,
        Date startTime,
        Date stopTime
    ) {
        this.name  = name;
        this.type  = type;
        this.value = value;
        this.isOfficial = isOfficial;
        this.startTime = startTime;
        this.stopTime  = stopTime;
    }


    @Override
    public Date getStopTime() {
        return stopTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getType() {
        return type;
    }


    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public boolean isOfficial() {
        return isOfficial;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
