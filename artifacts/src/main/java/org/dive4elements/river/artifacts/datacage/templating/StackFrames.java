/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathVariableResolver;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Maintains stack of 'frames' which are maps from string to object.
 * Used for variables in datacage/meta-data system.
 */
public class StackFrames
implements   XPathVariableResolver
{
    private static Logger log = LogManager.getLogger(StackFrames.class);

    public static final Object NULL = new Object() {
        @Override
        public String toString() {
            return "";
        }
    };

    /** The frames (used like a stack). */
    protected List<Map<String, Object>> frames;

    public StackFrames() {
        frames = new ArrayList<Map<String, Object>>();
    }

    public StackFrames(Map<String, Object> initialFrame) {
        this();
        if (initialFrame != null) {
            frames.add(new HashMap<String, Object>(initialFrame));
        }
    }

    /** Push a new String->Object map. */
    public void enter() {
        frames.add(new HashMap<String, Object>());
    }

    /** Pop/Remove last String->Object map. */
    public void leave() {
        frames.remove(frames.size()-1);
    }

    /** Put Key/Value in last String->Object map. */
    public void put(String key, Object value) {
        int N = frames.size();
        if (N > 0) {
            frames.get(N-1).put(key, value);
        }
    }

    /** Put multiple Key/Values in last String->Object map. */
    public void put(String [] keys, Object [] values) {
        Map<String, Object> top = frames.get(frames.size()-1);
        for (int i = 0; i < keys.length; ++i) {
            top.put(keys[i], values[i]);
        }
    }

    /** Check last frame (string->object map) for key. */
    public boolean containsKey(String key) {
        key = key.toUpperCase();
        for (int i = frames.size()-1; i >= 0; --i) {
            if (frames.get(i).containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get element (variable) key.
     * Returns null if not found.
     * @param key name to resolve
     * @return resolution, null if not found.
     */
    public Object get(String key) {
        return get(key, null);
    }

    /** result[0] is modified with value when true returned.
     * @return false if key not found in any frame. */
    public boolean getStore(String key, Object [] result) {

        key = key.toUpperCase();

        for (int i = frames.size()-1; i >= 0; --i) {
            Map<String, Object> frame = frames.get(i);
            if (frame.containsKey(key)) {
                result[0] = frame.get(key);
                return true;
            }
        }

        return false;
    }

    public Object get(String key, Object def) {

        key = key.toUpperCase();

        for (int i = frames.size()-1; i >= 0; --i) {
            Map<String, Object> frame = frames.get(i);
            if (frame.containsKey(key)) {
                return frame.get(key);
            }
        }

        return def;
    }

    public Object getNull(String key) {
        return getNull(key, null);
    }

    public Object getNull(String key, Object def) {

        key = key.toUpperCase();

        for (int i = frames.size()-1; i >= 0; --i) {
            Map<String, Object> frame = frames.get(i);
            if (frame.containsKey(key)) {
                Object value = frame.get(key);
                return value != null ? value : NULL;
            }
        }

        return def;
    }

    @Override
    public Object resolveVariable(QName variableName) {
        /*
        if (log.isDebugEnabled()) {
            log.debug("resolve var: " + variableName);
        }
        */

        return getNull(variableName.getLocalPart());
    }

    public String dump() {
        StringBuilder sb = new StringBuilder("[");
        Set<String> already = new HashSet<String>();

        boolean first = true;

        for (int i = frames.size()-1; i >= 0; --i) {
            Map<String, Object> frame = frames.get(i);
            for (Map.Entry<String, Object> entry: frame.entrySet()) {
                if (already.add(entry.getKey())) {
                    if (first) { first = false;   }
                    else       { sb.append(", "); }
                    if (sb.length() - sb.lastIndexOf("\n") > 80) {
                        sb.append("\n");
                    }
                    sb.append('\'').append(entry.getKey())
                      .append("'='").append(entry.getValue()).append('\'');
                }
            }
        }
        return sb.append(']').toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
