/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class QualityMeasurements {
    private static Logger log = LogManager.getLogger(QualityMeasurements.class);
    private List<QualityMeasurement> measurements;

    public QualityMeasurements() {
    }

    public QualityMeasurements(List<QualityMeasurement> list) {
        measurements = list;
    }

    public List<QualityMeasurement> getMeasurements() {
        return measurements;
    }

    public List<QualityMeasurement> getMeasurements(double km) {
        List<QualityMeasurement> res = new LinkedList<QualityMeasurement>();
        for (QualityMeasurement qm: measurements) {
            if (qm.getKm() == km) {
                res.add(qm);
            }
        }
        return res;
    }

    public List<Double> getKms() {
        List<Double> result = new LinkedList<Double>();
        for (QualityMeasurement qm : measurements) {
            if (result.indexOf(qm.getKm()) < 0) {
                result.add(qm.getKm());
            }
        }
        return result;
    }

    public void setMeasurements(List<QualityMeasurement> list) {
        this.measurements = list;
    }

    public void addMeasurement(QualityMeasurement qm) {
        this.measurements.add(qm);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
