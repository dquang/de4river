/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dive4elements.artifacts.ContextInjector;

import org.dive4elements.river.artifacts.D4EArtifact.FacetFilter;

public class GeneratorLookup
implements   FacetFilter
{
    public static final class Item {
        private Class<OutGenerator>   generator;
        private Object                ctx;
        private List<ContextInjector> cis;

        public Item(
            Class<OutGenerator> generator,
            Object ctx,
            List<ContextInjector> cis
        ) {
            this.generator = generator;
            this.ctx       = ctx;
            this.cis       = cis;
        }

        public Class<OutGenerator> getGenerator() {
            return generator;
        }

        public Object getContext() {
            return ctx;
        }

        public List<ContextInjector> getContextInjectors() {
            return cis;
        }
    } // class Item

    private Map<String, Item> generators;

    public GeneratorLookup() {
        generators = new HashMap<String, Item>();
    }

    public void putGenerator(
        String                outName,
        Class<OutGenerator>   generatorClass,
        Object                ctx,
        List<ContextInjector> cis
    ) {
        Item item = new Item(generatorClass, ctx, cis);
        generators.put(outName, item);
    }

    public Item getGenerator(String outName) {
        return generators.get(outName);
    }

    @Override
    public boolean accept(String outName, String facetName) {

        Item item = generators.get(outName);
        if (item == null) {
            return true;
        }

        Object ctx = item.getContext();
        return ctx instanceof FacetFilter
            ? ((FacetFilter)ctx).accept(outName, facetName)
            : true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
