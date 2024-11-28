/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** A little round robin pool of builders to mitigate
  * the fact the XML DOM documents are not thread safe.
  */
public class BuilderPool
{
    private static Logger log = LogManager.getLogger(BuilderPool.class);

    private static final int DEFAULT_POOL_SIZE = 4;

    private static final int POOL_SIZE = Math.max(
        Integer.getInteger("flys.datacage.pool.size", DEFAULT_POOL_SIZE), 1);

    private Deque<Builder> pool;

    public BuilderPool(Document document) {
        this(document, POOL_SIZE);
    }

    public BuilderPool(Document document, int poolSize) {

        if (log.isDebugEnabled()) {
            log.debug("Create build pool with " + poolSize + " elements.");
        }

        pool = new ArrayDeque<Builder>(poolSize);
        for (int i = 0; i < poolSize; ++i) {
            pool.add(new Builder(cloneDocument(document)));
        }
    }

    private final static Document cloneDocument(Document document) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Node origRoot = document.getDocumentElement();

            Document copy = db.newDocument();
            Node copyRoot = copy.importNode(origRoot, true);
            copy.appendChild(copyRoot);

            return copy;
        }
        catch (ParserConfigurationException pce) {
            log.error(pce);
        }

        log.error("Returning original document. "
            + "This will lead to threading issues.");

        return document;
    }

    public void build(
        List<Builder.NamedConnection> connections,
        Node                          output,
        Map<String, Object>           parameters
    )
    throws SQLException
    {
        Builder builder;
        synchronized (pool) {
            try {
                while ((builder = pool.poll()) == null) {
                    pool.wait();
                }
            }
            catch (InterruptedException ie) {
                log.debug("Waiting for builder interrupted. Build canceled.");
                return;
            }
        }
        try {
            builder.build(connections, output, parameters);
        }
        finally {
            synchronized (pool) {
                pool.add(builder);
                pool.notify();
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
