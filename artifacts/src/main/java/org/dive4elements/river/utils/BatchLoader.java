/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/** To reduce the number of SQL queries send to the backend
 *  (mainly by the fixings overviews) we execute them in batches of ids
 *  and store the results in a small cache.
 *  TODO: It currently relies on dynamic SQL.
 *  Is there a way to use Hibernate with java.sql.Array
 *  in cross database compatible manner?
 */
public abstract class BatchLoader<T> {

    private static Logger log = LogManager.getLogger(BatchLoader.class);

    public static final int BATCH_SIZE = 100;

    private Map<Integer, T> loaded;
    private List<Integer>   rest;
    private Session         session;
    private String          sqlTemplate;

    public BatchLoader(
        List<Integer> columns,
        Session       session,
        String        sqlTemplate
    ) {
        rest             = new ArrayList<Integer>(columns.size());
        loaded           = new HashMap<Integer, T>();
        this.session     = session;
        this.sqlTemplate = sqlTemplate;

        // Insert in reverse order to minize searching.
        for (int i = columns.size()-1; i >= 0; --i) {
            rest.add(columns.get(i));
        }
    }

    /** Searches for id and fill a batch to load containing the found id. */
    private List<Integer> prepareBatch(int id) {
        List<Integer> batch = new ArrayList<Integer>(BATCH_SIZE);

        boolean found = false;

        for (int i = rest.size()-1; batch.size() < BATCH_SIZE && i >= 0; --i) {
            Integer cid = rest.get(i);
            if (cid == id) {
                found = true;
                batch.add(cid);
                rest.remove(i);
            }
            else if ((found && batch.size() < BATCH_SIZE)
                 || (!found && batch.size() < BATCH_SIZE-1)) {
                batch.add(cid);
                rest.remove(i);
            }
        }

        return batch;
    }

    /** Converts id to a list of comma separated ints. */
    private static String idsAsString(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /** Get data for id. */
    public T get(int id) {
        T already = loaded.get(id);
        if (already != null) {
            return already;
        }

        List<Integer> batch = prepareBatch(id);
        if (batch.isEmpty()) {
            return null;
        }
        String sql = sqlTemplate.replace("$IDS", idsAsString(batch));
        if (log.isDebugEnabled()) {
            log.debug(sql + " " + sql.length());
        }
        fill(session.createSQLQuery(sql));
        return get(id);
    }

    /** Call this from fill() to store data in the cache. */
    protected void cache(int key, T data) {
        loaded.put(key, data);
    }

    /** Override this to fill the cache */
    protected abstract void fill(SQLQuery query);
}
