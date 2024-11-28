/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.model.FacetTypes;


public class OutputState extends DefaultState implements FacetTypes {

    @Override
    public Element describeStatic(
        Artifact    artifact,
        Document    document,
        Node        root,
        CallContext context,
        String      uuid)
    {
        return null;
    }


    @Override
    public Element describe(
        Artifact    artifact,
        Document    document,
        Node        root,
        CallContext context,
        String      uuid)
    {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
