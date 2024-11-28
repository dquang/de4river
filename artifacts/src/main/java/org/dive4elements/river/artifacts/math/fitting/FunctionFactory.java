/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FunctionFactory
{
    private static FunctionFactory instance;

    private Map<String, Function> functions;

    private FunctionFactory() {
        functions = new LinkedHashMap<String, Function>();

        registerFunction(Log.INSTANCE);
        registerFunction(Linear.INSTANCE);
        registerFunction(LogLinear.INSTANCE);
        registerFunction(Exp.INSTANCE);
        registerFunction(Quad.INSTANCE);
        registerFunction(Pow.INSTANCE);
        registerFunction(SQPow.INSTANCE);
    }

    public static synchronized FunctionFactory getInstance() {
        if (instance == null) {
            instance = new FunctionFactory();
        }
        return instance;
    }

    public Function getFunction(String name) {
        return functions.get(name);
    }

    public void registerFunction(Function function) {
        functions.put(function.getName(), function);
    }

    public Collection<Function> getFunctions() {
        return functions.values();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
