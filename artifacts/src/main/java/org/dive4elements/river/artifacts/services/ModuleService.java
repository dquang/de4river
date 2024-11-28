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

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.river.artifacts.model.Module;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.resources.Resources;

public class ModuleService extends D4EService {

    private static final String MODULE = "module";

    private static Logger log = LogManager.getLogger(ModuleService.class);

    protected Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("ModuleService.process");

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element em = ec.create("modules");
        List<Module> modules = (List<Module>)globalContext.get(
            RiverContext.MODULES);

        for (Module module : modules) {
            Element m = ec.create("module");
            ec.addAttr(m, "name", module.getName(), true);
            String localname = Resources.getMsg(callMeta,
                    MODULE + "." + module.getName(), module.getName());
            ec.addAttr(m, "localname", localname, true);

            for (String river : module.getRivers()) {
                Element r = ec.create("river");
                r.setAttribute("uuid", river);
                m.appendChild(r);
            }
            if (module.isSelected()) {
                ec.addAttr(m, "selected", "true", true);
            }

            em.appendChild(m);
        }

        result.appendChild(em);

        return result;
    }
}

// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 tw=80:
