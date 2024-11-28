/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.QSectorArtifact;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;


/**
 * Facet to access QSector that where added by user.
 */
public class QSectorFacet
extends      DefaultFacet
{
    /** Logger for this class. */
    private static final Logger log = LogManager.getLogger(QSectorFacet.class);


    /**
     * Trivial Constructor.
     */
    public QSectorFacet() {
    }


    /**
     * Trivial Constructor for a QSectorFacet.
     *
     * @param index       Database-Index to use.
     * @param name        Name (~type) of Facet.
     * @param description Description of Facet.
     */
    public QSectorFacet(int index, String name, String description) {
        super(index, name, description);
    }


    /**
     * Get List of QSector for river from Artifact.
     *
     * @param artifact (QSector-)Artifact to query for list of QSector.
     * @param context  Ignored.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        QSectorArtifact qsectorArtifact = (QSectorArtifact) artifact;
        if (qsectorArtifact == null || context == null ||
            context.getContextValue(CURRENT_KM) == null) {
            log.error("No artifact, context or currentKm in QSectorFacet");
            return null;
        }
        double currentKm =
            ((Double)context.getContextValue(CURRENT_KM)).doubleValue();
        return qsectorArtifact.getQSectors(currentKm, context);
    }


    /** Do a deep copy. */
    @Override
    public Facet deepCopy() {
        QSectorFacet copy = new QSectorFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
