/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.FixingsFilterBuilder;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing.Filter;

import org.dive4elements.river.artifacts.model.Range;

import org.dive4elements.river.artifacts.model.FixingsOverview;
import org.dive4elements.river.artifacts.model.FixingsOverviewFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FixingsOverviewService
extends      D4EService
{
    private static Logger log =
        LogManager.getLogger(FixingsOverviewService.class);

    public FixingsOverviewService() {
    }

    @Override
    public Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("FixingsOverviewService.doProcess");

        Document document = XMLUtils.newDocument();

        NodeList nodes = data.getElementsByTagName("river");

        String river = nodes.getLength() > 0
            ? ((Element)nodes.item(0)).getAttribute("name")
            : "";

        FixingsOverview overview = FixingsOverviewFactory.getOverview(river);

        if (overview != null) {
            FixingsFilterBuilder ffb = new FixingsFilterBuilder(data);
            Range  range  = ffb.getRange();
            Filter filter = ffb.getFilter();
            overview.generateOverview(document, range, filter);
        }
        else {
            log.warn("No overview for river '" + river + "' available.");
        }

        return document;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
