/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.FlowVelocityModel;
import org.dive4elements.river.model.FlowVelocityModelValue;


public class ImportFlowVelocityModelValue {

    private BigDecimal station;
    private BigDecimal q;
    private BigDecimal totalChannel;
    private BigDecimal mainChannel;
    private BigDecimal shearStress;

    private FlowVelocityModelValue peer;


    public ImportFlowVelocityModelValue(
        BigDecimal station,
        BigDecimal q,
        BigDecimal totalChannel,
        BigDecimal mainChannel,
        BigDecimal shearStress
    ) {
        this.station      = station;
        this.q            = q;
        this.totalChannel = totalChannel;
        this.mainChannel  = mainChannel;
        this.shearStress  = shearStress;
    }


    public void storeDependencies(FlowVelocityModel model) {
        getPeer(model);
    }


    public FlowVelocityModelValue getPeer(FlowVelocityModel model) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from FlowVelocityModelValue where " +
                "   flowVelocity=:model and " +
                "   station between :station - 0.00001 and :station + 0.00001"
            );

            query.setParameter("model", model);
            query.setParameter("station", station.doubleValue());

            List<FlowVelocityModelValue> values = query.list();

            if (values.isEmpty()) {
                peer = new FlowVelocityModelValue(
                    model, station, q, totalChannel, mainChannel, shearStress);

                session.save(peer);
            }
            else {
                peer = values.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
