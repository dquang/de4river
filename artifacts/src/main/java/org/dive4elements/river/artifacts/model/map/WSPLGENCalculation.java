/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.model.Calculation;


public class WSPLGENCalculation extends Calculation {

    private static final Logger log = LogManager.getLogger(
        WSPLGENCalculation.class);

    protected Map<Integer, String> errors;
    protected Map<Integer, String> warnings;


    public WSPLGENCalculation() {
        errors   = new HashMap<Integer, String>();
        warnings = new HashMap<Integer, String>();
    }


    public void addError(Integer key, String msg) {
        log.debug("New error: (" + key + ") " + msg);
        errors.put(key, msg);
    }


    public void addWarning(Integer key, String msg) {
        log.debug("New warning: (" + key + ") " + msg);
        warnings.put(key, msg);
    }


    public int numErrors() {
        return errors.size();
    }


    public int numWarnings() {
        return warnings.size();
    }


    @Override
    public void toXML(Document document, CallMeta meta) {
        Element root = document.createElement("problems");

        if (numErrors() > 0) {
            for (Map.Entry<Integer, String> entry: errors.entrySet()) {
                Element problem = document.createElement("problem");
                problem.setAttribute("error", String.valueOf(entry.getKey()));
                problem.setTextContent(entry.getValue());

                root.appendChild(problem);
            }
        }

        if (numWarnings() > 0) {
            for (Map.Entry<Integer, String> entry: warnings.entrySet()) {
                Element problem = document.createElement("problem");
                problem.setAttribute("error", String.valueOf(entry.getKey()));
                problem.setTextContent(entry.getValue());

                root.appendChild(problem);
            }
        }

        document.appendChild(root);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
