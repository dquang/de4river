/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.AnnotationArtifact;

import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StickyAxisAnnotation;

import org.dive4elements.river.model.FastAnnotations;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.utils.RiverUtils;

import org.dive4elements.river.artifacts.D4EArtifact;


/**
 * Facet to access Annotations (landmarks, POIs) of a river.
 */
public class AnnotationFacet
extends      DefaultFacet
{
    /**
     * Trivial Constructor.
     */
    public AnnotationFacet() {
    }


    /**
     * Trivial Constructor for a AnnotationFacet.
     *
     * @param index       Database-Index to use.
     * @param name        Name (~type) of Facet.
     * @param description Description of Facet.
     */
    public AnnotationFacet(int index, String name, String description) {
        super(index, name, description);
    }


    /**
     * Get List of Annotations for river from Artifact.
     *
     * @param artifact (Annotation-)Artifact to query for list of Annotations.
     * @param context  Ignored.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {

        // TODO issue880: Make annotations available _per type_
        AnnotationArtifact annotationArtifact = (AnnotationArtifact) artifact;

        String riverName = RiverUtils.getRivername((D4EArtifact)artifact);

        FastAnnotations fas = LocationProvider.getAnnotations(riverName);

        String filterName = annotationArtifact.getFilterName();

        FastAnnotations.Filter filter = (filterName == null)
            ? FastAnnotations.IS_POINT
            : new FastAnnotations.NameFilter(filterName);

        List<StickyAxisAnnotation> xy =
            new ArrayList<StickyAxisAnnotation>(fas.size());

        for (Iterator<FastAnnotations.Annotation> iter =
                fas.filter(filter); iter.hasNext();) {
            FastAnnotations.Annotation fa = iter.next();

            xy.add(new StickyAxisAnnotation(
                fa.getPosition(),
                (float)fa.getA(),
                StickyAxisAnnotation.SimpleAxis.X_AXIS));
        }

        return new RiverAnnotation(description, xy);
    }


    @Override
    public Facet deepCopy() {
        AnnotationFacet copy = new AnnotationFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
