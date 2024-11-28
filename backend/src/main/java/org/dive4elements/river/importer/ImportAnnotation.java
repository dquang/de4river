/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Annotation;
import org.dive4elements.river.model.AnnotationType;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.Position;
import org.dive4elements.river.model.Attribute;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Edge;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

public class ImportAnnotation
implements   Comparable<ImportAnnotation>
{
    protected ImportAttribute      attribute;
    protected ImportPosition       position;
    protected ImportRange          range;
    protected ImportEdge           edge;
    protected ImportAnnotationType type;

    protected Annotation      peer;

    public ImportAnnotation() {
    }

    public ImportAnnotation(
        ImportAttribute      attribute,
        ImportPosition       position,
        ImportRange          range,
        ImportEdge           edge,
        ImportAnnotationType type
    ) {
        this.attribute = attribute;
        this.position  = position;
        this.range     = range;
        this.edge      = edge;
        this.type      = type;
    }

    public int compareTo(ImportAnnotation other) {
        int d = attribute.compareTo(other.attribute);
        if (d != 0) {
            return d;
        }

        if ((d = position.compareTo(other.position)) != 0) {
            return d;
        }

        if ((d = range.compareTo(other.range)) != 0) {
            return d;
        }

        if (edge == null && other.edge != null) return -1;
        if (edge != null && other.edge == null) return +1;
        if (edge == null && other.edge == null) return 0;

        if ((d = edge.compareTo(other.edge)) != 0) {
            return d;
        }

        if (type == null && other.type != null) return -1;
        if (type != null && other.type == null) return +1;
        if (type == null && other.type == null) return 0;

        return type.compareTo(other.type);
    }

    public ImportAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(ImportAttribute attribute) {
        this.attribute = attribute;
    }

    public ImportPosition getPosition() {
        return position;
    }

    public void setPosition(ImportPosition position) {
        this.position = position;
    }

    public ImportRange getRange() {
        return range;
    }

    public void setRange(ImportRange range) {
        this.range = range;
    }

    public ImportEdge getEdge() {
        return edge;
    }

    public void setEdge(ImportEdge edge) {
        this.edge = edge;
    }

    public ImportAnnotationType getType() {
        return type;
    }

    public void setType(ImportAnnotationType type) {
        this.type = type;
    }

    public Annotation getPeer(River river) {
        if (peer == null) {
            Range          r = range.getPeer(river);
            Attribute      a = attribute.getPeer();
            Position       p = position.getPeer();
            Edge           e = edge != null ? edge.getPeer() : null;
            AnnotationType t = type != null ? type.getPeer() : null;

            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from Annotation where "    +
                "range=:range and "         +
                "attribute=:attribute and " +
                "position=:position and "   +
                "edge=:edge and "           +
                "type=:type");
            query.setParameter("range",     r);
            query.setParameter("attribute", a);
            query.setParameter("position",  p);
            query.setParameter("edge",      e);
            query.setParameter("type",      t);
            List<Annotation> annotations = query.list();
            if (annotations.isEmpty()) {
                peer = new Annotation(r, a, p, e, t);
                session.save(peer);
            }
            else {
                peer = annotations.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
