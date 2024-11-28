/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.DischargeZone;
import org.dive4elements.river.model.FlowVelocityModel;
import org.dive4elements.river.model.River;


public class ImportFlowVelocityModel {

    private static final Logger log = LogManager
        .getLogger(ImportFlowVelocityModel.class);

    private String description;

    private ImportDischargeZone dischargeZone;

    private List<ImportFlowVelocityModelValue> values;

    private FlowVelocityModel peer;

    public ImportFlowVelocityModel() {
        values = new ArrayList<ImportFlowVelocityModelValue>();
    }

    public ImportFlowVelocityModel(String description) {
        this();

        this.description = description;
    }

    public ImportFlowVelocityModel(ImportDischargeZone dischargeZone,
        String description) {
        this();

        this.dischargeZone = dischargeZone;
        this.description = description;
    }

    public void setDischargeZone(ImportDischargeZone dischargeZone) {
        this.dischargeZone = dischargeZone;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addValue(ImportFlowVelocityModelValue value) {
        this.values.add(value);
    }

    public void storeDependencies(River river) {
        log.debug("store dependencies");

        if (dischargeZone == null) {
            log.warn("skip flow velocity model: No discharge zone specified.");
            return;
        }

        dischargeZone.storeDependencies(river);

        FlowVelocityModel peer = getPeer(river);

        if (peer != null) {
            int i = 0;

            for (ImportFlowVelocityModelValue value : values) {
                value.storeDependencies(peer);
                i++;
            }

            log.info("stored " + i + " flow velocity model values.");
        }
    }

    public FlowVelocityModel getPeer(River river) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            DischargeZone zone = dischargeZone.getPeer(river);

            Query query = session.createQuery("from FlowVelocityModel where "
                + "   dischargeZone=:dischargeZone");

            query.setParameter("dischargeZone", zone);

            List<FlowVelocityModel> model = query.list();

            if (model.isEmpty()) {
                peer = new FlowVelocityModel(zone, description);
                session.save(peer);
            }
            else {
                peer = model.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
