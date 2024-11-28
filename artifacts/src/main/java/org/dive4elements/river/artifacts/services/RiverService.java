/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.model.RiverFactory;


/**
 * This service provides information about the supported rivers by this
 * application.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RiverService extends D4EService {

    /** The log used in this service.*/
    private static Logger log = LogManager.getLogger(RiverService.class);

    @Override
    protected Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("RiverService.process");

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        List<River> allRivers = RiverFactory.getRivers();

        Element rivers = ec.create("rivers");

        for (River river: allRivers) {
            Element r = ec.create("river");
            ec.addAttr(r, "name", river.getName(), true);
            ec.addAttr(r, "modeluuid", river.getModelUuid(), true);

            rivers.appendChild(r);
        }

        result.appendChild(rivers);

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
