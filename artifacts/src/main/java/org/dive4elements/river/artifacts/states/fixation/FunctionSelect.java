/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import java.util.Collection;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;
import org.dive4elements.river.artifacts.states.DefaultState;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FunctionSelect extends DefaultState {

    /**
     * The default constructor that initializes an empty State object.
     */
    public FunctionSelect() {
    }

    @Override
    public String getUIProvider() {
        return "fix.functionselect";
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator ec,
        Artifact                artifact,
        String                  name,
        CallContext             context)
    {
        FunctionFactory ff = FunctionFactory.getInstance();
        Collection<Function> fc = ff.getFunctions();
        Element[] functions = new Element[fc.size()];

        int j = 0;
        for (Function f: fc) {
            String n = f.getName();
            String d = f.getDescription();
            functions[j] = createItem(ec, new String[] {d, n});
            j++;
        }

        return functions;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
