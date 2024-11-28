/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.model.River;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class RiverInfoService extends D4EService {

    private static final Logger log = LogManager.getLogger(
            RiverInfoService.class);

    protected static final String RIVER_XPATH = "/art:river/text()";

    protected XMLUtils.ElementCreator ec;
    protected River river;
    protected Element riverele;

    @Override
    protected Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        String rivername = XMLUtils.xpathString(
            data, RIVER_XPATH, ArtifactNamespaceContext.INSTANCE);

        river = RiverFactory.getRiver(rivername);

        Document result = XMLUtils.newDocument();

        if (river == null) {
            log.warn("No river with name " + rivername + " found.");
            return null;
        }

        ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        riverele = ec.create("river-info");

        double[] minmax  = river.determineMinMaxDistance();
        double[] minmaxq = river.determineMinMaxQ();
        Long offnumber = river.getOfficialNumber();

        Element r = ec.create("river");
        ec.addAttr(r, "name", river.getName(), true);
        ec.addAttr(r, "start",
            minmax != null ? Double.toString(minmax[0]) : "", true);
        ec.addAttr(r, "end",
            minmax != null ? Double.toString(minmax[1]) : "", true);
        ec.addAttr(r, "wstunit", river.getWstUnit().getName(), true);
        ec.addAttr(r, "kmup", Boolean.toString(river.getKmUp()), true);
        ec.addAttr(r, "minq", Double.toString(minmaxq[0]), true);
        ec.addAttr(r, "maxq", Double.toString(minmaxq[1]), true);
        ec.addAttr(r, "official",
            offnumber != null ? Long.toString(offnumber) : "", true);
        ec.addAttr(r, "model-uuid", river.getModelUuid(), true);

        riverele.appendChild(r);
        result.appendChild(riverele);

        return result;
    }

    /**
     * Returns a Double as String from a BigDecimal value.
     *
     * If value is null an empty String is returned.
     */
    protected static String getStringValue(BigDecimal value) {
        return value != null
            ? Double.toString(value.doubleValue()) : "";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
