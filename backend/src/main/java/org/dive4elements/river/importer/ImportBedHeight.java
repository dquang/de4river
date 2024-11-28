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

import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.BedHeightType;
import org.dive4elements.river.model.ElevationModel;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;


public class ImportBedHeight
{
    private static Logger log = LogManager.getLogger(ImportBedHeight.class);

    protected Integer year;

    protected String evaluationBy;
    protected String description;

    protected ImportRange          range;
    protected ImportBedHeightType  type;
    protected ImportLocationSystem locationSystem;
    protected ImportElevationModel curElevationModel;
    protected ImportElevationModel oldElevationModel;

    protected List<ImportBedHeightValue> values;

    protected BedHeight peer;


    public ImportBedHeight(String description) {
        this.description = description;
        this.values      = new ArrayList<ImportBedHeightValue>();
    }


    public String getDescription() {
        return description;
    }

    public int getValueCount() {
        return values.size();
    }


    public void setYear(int year) {
        this.year = year;
    }

    public void setTimeInterval(ImportTimeInterval timeInterval) {
        // do nothing
    }

    public void setEvaluationBy(String evaluationBy) {
        this.evaluationBy = evaluationBy;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRange(ImportRange range) {
        this.range = range;
    }

    public void setType(ImportBedHeightType type) {
        this.type = type;
    }

    public void setLocationSystem(ImportLocationSystem locationSystem) {
        this.locationSystem = locationSystem;
    }

    public void setCurElevationModel(ImportElevationModel curElevationModel) {
        this.curElevationModel = curElevationModel;
    }

    public void setOldElevationModel(ImportElevationModel oldElevationModel) {
        this.oldElevationModel = oldElevationModel;
    }

    public void addValue(ImportBedHeightValue value) {
        values.add((ImportBedHeightValue) value);
    }

    public void storeDependencies(River river) {
        log.info("Store dependencies for single: '" + getDescription() + "'");

        if (type != null) {
            type.storeDependencies();
        }

        if (locationSystem != null) {
            locationSystem.storeDependencies();
        }

        if (curElevationModel != null) {
            curElevationModel.storeDependencies();
        }

        if (oldElevationModel != null) {
            oldElevationModel.storeDependencies();
        }

        BedHeight peer = getPeer(river);

        if (peer != null) {
            for (ImportBedHeightValue value: values) {
                value.storeDependencies(peer);
            }
        }

        Session session = ImporterSession.getInstance().getDatabaseSession();
        session.flush();
    }

    public BedHeight getPeer(River river) {
        if (peer == null) {
            BedHeightType  theType     = type != null ? type.getPeer() : null;
            ElevationModel theCurModel = curElevationModel.getPeer();
            Range          theRange    = range != null
                ? range.getPeer(river)
                : null;

            if (theType == null) {
                log.warn("BHS: No bed height type given. Skip file '" +
                    description + "'");
                return null;
            }

            if (theCurModel == null) {
                log.warn("BHS: No elevation model given. Skip file '" +
                    description + "'");
                return null;
            }

            if (theRange == null) {
                log.warn("BHS: No km-range given: '" +
                    description + "'");
            }

            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from BedHeight where " +
                "river=:river and year=:year " +
                "and type=:type and locationSystem=:locationSystem and " +
                "curElevationModel=:curElevationModel and range=:range");

            query.setParameter("river", river);
            query.setParameter("year", year);
            query.setParameter("type", theType);
            query.setParameter("locationSystem", locationSystem.getPeer());
            query.setParameter("curElevationModel", theCurModel);
            query.setParameter("range", range.getPeer(river));

            List<BedHeight> bedHeights = query.list();
            if (bedHeights.isEmpty()) {
                log.info("Create new BedHeight DB instance.");

                peer = new BedHeight(
                    river,
                    year,
                    theType,
                    locationSystem.getPeer(),
                    theCurModel,
                    oldElevationModel != null
                        ? oldElevationModel.getPeer()
                        : null,
                    range.getPeer(river),
                    evaluationBy,
                    description
                );

                session.save(peer);
            }
            else {
                peer = bedHeights.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
