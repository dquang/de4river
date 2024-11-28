/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.FileTools;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.utils.RiverUtils;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ScenarioSelect extends DefaultState {

    /** The log that is used in this class.*/
    private static Logger log = LogManager.getLogger(ScenarioSelect.class);


    public static final String FIELD_MODE     = "scenario";

    public static final String SCENARIO_CURRENT   = "scenario.current";
    public static final String SCENARIO_POTENTIEL = "scenario.potentiel";
    public static final String SCENARIO_SCENRAIO  = "scenario.scenario";

    public static final String[] SCENARIOS = {
        SCENARIO_CURRENT,
        SCENARIO_POTENTIEL,
        SCENARIO_SCENRAIO };


    @Override
    protected String getUIProvider() {
        return "";
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        CallMeta meta = context.getMeta();

        if (name.equals(FIELD_MODE)) {
            Element[] scenarios = new Element[SCENARIOS.length];

            int i = 0;

            for (String scenario: SCENARIOS) {
                scenarios[i++] = createItem(
                    cr, new String[] {
                        Resources.getMsg(meta, scenario, scenario),
                        scenario
                    });
            }

            return scenarios;
        }
        else {
            D4EArtifact flys = (D4EArtifact) artifact;
            String       data = flys.getDataAsString(name);

            return new Element[] { createItem(
                cr,
                new String[] {
                    Resources.getMsg(meta, name, name),
                    data
                }
            )};
        }
    }


    @Override
    protected Element createItem(XMLUtils.ElementCreator cr, Object obj) {
        Element item  = ProtocolUtils.createArtNode(cr, "item", null, null);
        Element label = ProtocolUtils.createArtNode(cr, "label", null, null);
        Element value = ProtocolUtils.createArtNode(cr, "value", null, null);

        String[] arr = (String[]) obj;

        label.setTextContent(arr[0]);
        value.setTextContent(arr[1]);

        item.appendChild(label);
        item.appendChild(value);

        return item;
    }



    @Override
    public void endOfLife(Artifact artifact, Object callContext) {
        super.endOfLife(artifact, callContext);
        log.info("ScenarioSelect.endOfLife: " + artifact.identifier());

        D4EArtifact flys = (D4EArtifact) artifact;
        removeDirectory(flys);
    }


    /**
     * Removes the directory and all its content where the required data and the
     * results of WSPLGEN are stored. Should be called in endOfLife().
     */
    // FIXME: I've seen this code somewhere else...
    protected void removeDirectory(D4EArtifact artifact) {
        String shapePath = RiverUtils.getXPathString(
            RiverUtils.XPATH_MAPFILES_PATH);

        File artifactDir = new File(shapePath, artifact.identifier());

        if (artifactDir.exists()) {
            log.debug("Delete directory: " + artifactDir.getAbsolutePath());
            boolean success = FileTools.deleteRecursive(artifactDir);
            if (!success) {
                log.warn("could not remove dir '" + artifactDir + "'");
            }
        }
        else {
            log.debug("There is no directory to remove.");
        }
    }


}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
