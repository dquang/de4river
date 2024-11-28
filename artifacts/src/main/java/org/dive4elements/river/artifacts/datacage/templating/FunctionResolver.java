/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.transition.TransitionEngine;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.context.RiverContextFactory;


/** Resolves functions (e.g. dc:contains) in Datacage/Meta-Data system. */
public class FunctionResolver
implements   XPathFunctionResolver
{
    /** Home log. */
    private static Logger log = LogManager.getLogger(FunctionResolver.class);

    public static final String FUNCTION_NAMESPACE_URI = "dc";

    public static final double FAR_AWAY = 99999d;

    protected static final class Entry {

        Entry         next;
        XPathFunction function;
        int           arity;

        public Entry(Entry next, XPathFunction function, int arity) {
            this.next     = next;
            this.function = function;
            this.arity    = arity;
        }

        XPathFunction find(int arity) {
            Entry current = this;
            while (current != null) {
                if (current.arity == arity) {
                    return current.function;
                }
                current = current.next;
            }
            return null;
        }
    } // class Entry

    /** List of functions. */
    protected Map<String, Entry> functions;

    protected Builder.BuildHelper buildHelper;


    public FunctionResolver() {
        this(null);
    }

    public FunctionResolver(Builder.BuildHelper buildHelper) {
        this.buildHelper = buildHelper;

        functions = new HashMap<String, Entry>();

        addFunction("coalesce", 2, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return coalesce(args);
            }
        });

        addFunction("lowercase", 1, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return args.get(0).toString().toLowerCase();
            }
        });

        addFunction("uppercase", 1, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return args.get(0).toString().toUpperCase();
            }
        });

        addFunction("contains", 2, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return contains(args);
            }
        });

        addFunction("fromValue", 3, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return fromValue(args);
            }
        });

        addFunction("toValue", 3, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return toValue(args);
            }
        });

        addFunction("replace", 3, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return replace(args);
            }
        });

        addFunction("replace-all", 3, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return replaceAll(args);
            }
        });

        addFunction("has-result", 0, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return FunctionResolver.this.buildHelper.hasResult();
            }
        });

        addFunction("group-key", 0, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return FunctionResolver.this.buildHelper.getGroupKey();
            }
        });

        addFunction("date-format", 2, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return dateFormat(args);
            }
        });

        addFunction("dump-variables", 0, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return FunctionResolver.this.buildHelper.frames.dump();
            }
        });

        addFunction("get", 1, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                Object o = args.get(0);
                if (o instanceof String) {
                    return FunctionResolver.this.buildHelper.frames.getNull(
                        (String)o, StackFrames.NULL);
                }
                return StackFrames.NULL;
            }
        });

        addFunction("all-state-successors", 2, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                Object artifactName  = args.get(0);
                Object stateId       = args.get(1);

                return artifactName instanceof String
                    && stateId      instanceof String
                    ? allStateSuccessors((String)artifactName, (String)stateId)
                    : Collections.<String>emptySet();
            }
        });

        addFunction("find-all", 2, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                Object needle    = args.get(0);
                Object haystack  = args.get(1);
                return haystack instanceof String
                    && needle   instanceof String
                    ? findAll((String)needle, (String)haystack)
                    : Collections.<String>emptyList();
            }
        });

        addFunction("max-number", 1, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return maxNumber(args.get(0));
            }
        });

        addFunction("min-number", 1, new XPathFunction() {
            @Override
            public Object evaluate(List args) throws XPathFunctionException {
                return minNumber(args.get(0));
            }
        });

    }

    /**
     * Create a new function.
     * @param name  Name of the function.
     * @param arity Number of arguments for function.
     * @param function the function itself.
     */
    public void addFunction(String name, int arity, XPathFunction function) {
        Entry entry = functions.get(name);
        if (entry == null) {
            entry = new Entry(null, function, arity);
            functions.put(name, entry);
        }
        else {
            Entry newEntry = new Entry(entry.next, function, arity);
            entry.next = newEntry;
        }
    }

    @Override
    public XPathFunction resolveFunction(QName functionName, int arity) {

        if (!functionName.getNamespaceURI().equals(FUNCTION_NAMESPACE_URI)) {
            return null;
        }

        Entry entry = functions.get(functionName.getLocalPart());
        return entry != null
            ? entry.find(arity)
            : null;
    }

    /** Implementation of case-ignoring dc:contains. */
    public static Object contains(List args) throws XPathFunctionException {
        Object haystack = args.get(0);
        Object needle   = args.get(1);

        if (needle instanceof String && !(haystack instanceof String)) {
            needle = ((String)needle).toUpperCase();
        }

        try {
            if (haystack instanceof Collection) {
                return Boolean.valueOf(
                    ((Collection)haystack).contains(needle));
            }

            if (haystack instanceof Map) {
                return Boolean.valueOf(
                    ((Map)haystack).containsKey(needle));
            }

            if (haystack instanceof Object []) {
                for (Object straw: (Object [])haystack) {
                    if (straw.equals(needle)) {
                        return Boolean.TRUE;
                    }
                }
            }

            if (haystack instanceof String && needle instanceof String) {
                String h = (String)haystack;
                String n = (String)needle;
                return h.contains(n);
            }

            return Boolean.FALSE;
        }
        catch (Exception e) {
            log.error(e);
            throw new XPathFunctionException(e);
        }
    }

    /** Implementation for getting the minimum value of location or distance
     *  dc:fromValue.
     */
    public static Object fromValue(List args) throws XPathFunctionException {
        Object mode      = args.get(0);
        Object locations = args.get(1);
        Object from      = args.get(2);

        if ((mode instanceof String && mode.equals("location")) ||
            (locations instanceof String && !((String)locations).isEmpty())) {
            if (!(locations instanceof String)) {
                return -FAR_AWAY;
            }
            String loc = ((String)locations).replace(" ", "");
            String[] split = loc.split(",");
            if (split.length < 1) {
                return -FAR_AWAY;
            }
            try {
                double min = Double.parseDouble(split[0]);
                for (int i = 1; i < split.length; ++i) {
                    double v = Double.parseDouble(split[i]);
                    if (v < min) {
                        min = v;
                    }
                }
                return min;
            }
            catch (NumberFormatException nfe) {
                return -FAR_AWAY;
            }
        }
        else {
            if (!(from instanceof String)) {
                return -FAR_AWAY;
            }
            String f = (String)from;
            try {
                return Double.parseDouble(f);
            }
            catch(NumberFormatException nfe) {
                return -FAR_AWAY;
            }
        }
    }

    /** Implementation for getting the maximum value of location or distance
     *  dc:toValue.
     */
    public static Object toValue(List args) throws XPathFunctionException {
        Object mode      = args.get(0);
        Object locations = args.get(1);
        Object to        = args.get(2);

        if ((mode instanceof String && mode.equals("location")) ||
            (locations instanceof String && !((String)locations).isEmpty())) {
            if (!(locations instanceof String)) {
                return FAR_AWAY;
            }
            try {
                String loc = ((String)locations).replace(" ", "");
                String[] split = loc.split(",");
                if (split.length < 1) {
                    return FAR_AWAY;
                }
                double max = Double.parseDouble(split[0]);
                for (int i = 1; i < split.length; ++i) {
                    double v = Double.parseDouble(split[i]);
                    if (v > max) {
                        max = v;
                    }
                }
                return max;
            }
            catch (NumberFormatException nfe) {
                return FAR_AWAY;
            }
        }
        else {
            if (!(to instanceof String)) {
                return FAR_AWAY;
            }
            else {
                String t = (String)to;
                try {
                    return Double.parseDouble(t);
                }
                catch (NumberFormatException nfe) {
                    return FAR_AWAY;
                }
            }
        }
    }

    /** Implementation for doing a string replace
     *  dc:replace .
     */
    public static Object replace(List args) throws XPathFunctionException {
        Object haystack    = args.get(0);
        Object needle      = args.get(1);
        Object replacement = args.get(2);

        if (needle      instanceof String
        &&  haystack    instanceof String
        &&  replacement instanceof String) {
            return ((String)haystack).replace(
                    (String)needle, (String)replacement);
        }
        return haystack;
    }

    /** Implementation for doing a string replace
     *  dc:replace-all
     */
    public static Object replaceAll(List args) throws XPathFunctionException {
        Object haystack    = args.get(0);
        Object needle      = args.get(1);
        Object replacement = args.get(2);

        if (needle      instanceof String
        &&  haystack    instanceof String
        &&  replacement instanceof String) {
            return ((String)haystack).replaceAll(
                    (String)needle, (String)replacement);
        }
        return haystack;
    }

    public static Object dateFormat(List args) throws XPathFunctionException {
        Object pattern = args.get(0);
        Object date    = args.get(1);

        try {
            // TODO: Take locale into account.
            SimpleDateFormat format = new SimpleDateFormat((String)pattern);

            if (date instanceof Number) {
                return format.format(new Date(((Number)date).longValue()));
            }

            try {
                /* Oracle does not return a date object but
                   an oracle.sql.TIMESTAMP */
                Method meth = date.getClass()
                    .getMethod("dateValue", new Class[] {});
                date = meth.invoke(date, new Object[] {});
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }

            if (date instanceof Date) {
                return format.format((Date)date);
            }
        }
        catch (IllegalArgumentException iae) {
            log.error(iae.getMessage());
        }

        return "";
    }

    public static Set<String> allStateSuccessors(
        String artifactName,
        String stateId
    ) {
        GlobalContext gc = RiverContextFactory.getGlobalContext();
        if (gc == null) {
            return Collections.<String>emptySet();
        }
        Object o = gc.get(RiverContext.TRANSITION_ENGINE_KEY);
        if (o instanceof TransitionEngine) {
            TransitionEngine te = (TransitionEngine)o;
            return te.allRecursiveSuccessorStateIds(artifactName, stateId);
        }
        return Collections.<String>emptySet();
    }

    public static Collection<String> findAll(String needle, String haystack) {

        ArrayList<String> result = new ArrayList<String>();

        Pattern pattern = Pattern.compile(needle);
        Matcher matcher = pattern.matcher(haystack);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    public static Number maxNumber(Object list) {
        if (list instanceof Collection) {
            Collection collection = (Collection)list;
            double max = -Double.MAX_VALUE;
            for (Object x: collection) {
                Number n;
                if (x instanceof Number) {
                    n = (Number)x;
                }
                else if (x instanceof String) {
                    try {
                        n = Double.valueOf((String)x);
                    }
                    catch (NumberFormatException nfe) {
                        log.warn("'" + x + "' is not a number.");
                        continue;
                    }
                }
                else {
                    log.warn("'" + x + "' is not a number.");
                    continue;
                }

                double v = n.doubleValue();

                if (v > max) {
                    max = v;
                }
            }

            return Double.valueOf(max == -Double.MAX_VALUE
                ? Double.MAX_VALUE
                : max);
        }

        return list instanceof Number
            ? (Number)list
            : Double.valueOf(Double.MAX_VALUE);
    }

    public static Number minNumber(Object list) {
        if (list instanceof Collection) {
            Collection collection = (Collection)list;
            double min = Double.MAX_VALUE;
            for (Object x: collection) {
                Number n;
                if (x instanceof Number) {
                    n = (Number)x;
                }
                else if (x instanceof String) {
                    try {
                        n = Double.valueOf((String)x);
                    }
                    catch (NumberFormatException nfe) {
                        log.warn("'" + x + "' is not a number.");
                        continue;
                    }
                }
                else {
                    log.warn("'" + x + "' is not a number.");
                    continue;
                }

                double v = n.doubleValue();

                if (v < min) {
                    min = v;
                }
            }

            return Double.valueOf(min == Double.MAX_VALUE
                ? -Double.MAX_VALUE
                : min);
        }

        return list instanceof Number
            ? (Number)list
            : Double.valueOf(-Double.MAX_VALUE);
    }

    public static Object coalesce(List list) {
        for (Object x: list) {
            if (x instanceof String && ((String)x).length() != 0) {
                return x;
            }
            if (x instanceof Number && ((Number)x).doubleValue() != 0.0) {
                return x;
            }
        }
        return StackFrames.NULL;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
