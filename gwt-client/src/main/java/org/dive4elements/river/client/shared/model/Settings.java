/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.List;

/**
 * This interface describes an output settings of an artifact.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public interface Settings extends Serializable {

    /** The output name */
    String getName();

    /** */
    List<String> getCategories();

    /** */
    void setSettings(String category, List<Property> settings);

    /** */
    List<Property> getSettings(String category);

}
