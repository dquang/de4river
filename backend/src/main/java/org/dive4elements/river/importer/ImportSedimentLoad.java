/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;
import java.util.ArrayList;

import org.dive4elements.river.model.GrainFraction;
import org.dive4elements.river.model.SedimentLoad;
import org.dive4elements.river.model.TimeInterval;
import org.hibernate.Query;
import org.hibernate.Session;

public class ImportSedimentLoad
{
    private SedimentLoad peer;

    private ImportGrainFraction grainFraction;
    private ImportTimeInterval  timeInterval;
    private ImportTimeInterval  sqTimeInterval;
    private String              description;
    private Integer             kind;

    private List<ImportSedimentLoadValue> values;

    public ImportSedimentLoad() {
        this.values = new ArrayList<ImportSedimentLoadValue>();
    }

    public ImportSedimentLoad(
        ImportGrainFraction grainFraction,
        ImportTimeInterval  timeInterval,
        ImportTimeInterval  sqTimeInterval,
        String              description,
        Integer             kind
    ) {
        this.grainFraction  = grainFraction;
        this.timeInterval   = timeInterval;
        this.sqTimeInterval = sqTimeInterval;
        this.description    = description;
        this.kind           = kind;

        this.values = new ArrayList<ImportSedimentLoadValue>();
    }

    public void addValue(ImportSedimentLoadValue value) {
        values.add(value);
    }

    public void storeDependencies() {
        grainFraction.getPeer();
        timeInterval.getPeer();

        if (sqTimeInterval != null) {
            sqTimeInterval.getPeer();
        }

        getPeer();

        for (ImportSedimentLoadValue value : values) {
            value.storeDependencies(peer);
        }

    }

    public SedimentLoad getPeer() {

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            String sqtquery = sqTimeInterval == null
                ? "sq_time_interval_id is null"
                : "sqTimeInterval = :sqTimeInterval";
            Query query = session.createQuery(
                "from SedimentLoad where " +
                "   grainFraction = :grainFraction and " +
                "   timeInterval = :timeInterval and " +
                "   description = :description and " +
                "   kind = :kind and " +
                    sqtquery);

            GrainFraction gf = grainFraction.getPeer();
            TimeInterval  ti = timeInterval.getPeer();

            TimeInterval sqti = sqTimeInterval != null
                ? sqTimeInterval.getPeer()
                : null;

            query.setParameter("grainFraction", gf);
            query.setParameter("timeInterval", ti);

            if (sqti != null) {
                query.setParameter("sqTimeInterval", sqti);
            }
            query.setParameter("description", description);
            query.setParameter("kind", kind);

            List<SedimentLoad> loads = query.list();
            if (loads.isEmpty()) {
                peer = new SedimentLoad(gf, ti, sqti, description, kind);
                session.save(peer);
            }
            else {
                peer = loads.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
