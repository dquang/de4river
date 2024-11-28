/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


/**
 * A 'Theme' is something displayed in a Chart. It can be activated or
 * deactivated to show/hide in the resultant visual representation in the
 * chart.
 *
 * A Theme maps more or less directly to a Facet of an Artifact in a
 * Collection (certain attributes are added at Collection-Level).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Theme extends Serializable {

    int getPosition();

    void setPosition(int pos);

    int getIndex();

    int getActive();

    void setActive(int active);

    int getVisible();

    void setVisible(int visible);

    String getArtifact();

    String getFacet();

    String getDescription();

    void setDescription(String description);

    boolean equals(Object o);

    /** Get the CollectionItem representing the facets artifact. */
    CollectionItem getCollectionItem();

    /** Set the CollectionItem representing the facets artifact. */
    void setCollectionItem(CollectionItem ci);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
