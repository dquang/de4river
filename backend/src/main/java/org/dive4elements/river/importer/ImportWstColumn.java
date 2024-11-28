/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Wst;
import org.dive4elements.river.model.WstColumn;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.TimeInterval;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Unmapped column of a WST. */
public class ImportWstColumn
{
    private static Logger log = LogManager.getLogger(ImportWstColumn.class);

    protected ImportWst wst;
    protected String    name;
    protected String    description;
    protected Integer   position;
    protected String    source;

    protected ImportTimeInterval timeInterval;

    protected List<ImportWstColumnQRange> columnQRanges;
    protected List<ImportWstColumnValue>  columnValues;

    protected WstColumn peer;

    public ImportWstColumn() {
        columnQRanges = new ArrayList<ImportWstColumnQRange>();
        columnValues  = new ArrayList<ImportWstColumnValue>();
    }

    public ImportWstColumn(
        ImportWst wst,
        String    name,
        String    description,
        Integer   position,
        String    source
    ) {
        this();
        this.wst         = wst;
        this.name        = name;
        this.description = description;
        this.position    = position;
    }

    public ImportWstColumn(
        ImportWst wst,
        String    name,
        String    description,
        Integer   position
    ) {
        this(wst, name, description, position, null);
    }

    public ImportWst getWst() {
        return wst;
    }

    public void setWst(ImportWst wst) {
        this.wst = wst;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void addColumnValue(BigDecimal position, BigDecimal w) {
        columnValues.add(
            new ImportWstColumnValue(this, position, w));
    }

    public void addColumnQRange(ImportWstQRange columnQRange) {
        columnQRanges.add(
            new ImportWstColumnQRange(this, columnQRange));
    }


    /** Get the Column Values stored in this column. */
    public List<ImportWstColumnValue> getColumnValues() {
        return columnValues;
    }


    public void storeDependencies(River river) {
        log.info("store column '" + name + "'");
        getPeer(river);

        for (ImportWstColumnQRange columnQRange: columnQRanges) {
            columnQRange.getPeer(river);
        }

        for (ImportWstColumnValue columnValue: columnValues) {
            columnValue.getPeer(river);
        }
    }

    public ImportTimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(ImportTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public boolean guessWaterLevelIncreasing() {

        int N = columnValues.size();

        if (N < 2) {
            return true;
        }

        Random r = new Random();
        int up = 0;

        int S = N < 50 ? N : (int)(0.1f * N)+1;
        for (int s = 0; s < S; ++s) {
            int i1, i2;
            do {
                i1 = r.nextInt(N-1);
                i2 = r.nextInt(N-1);
            } while (i1 == i2);
            ImportWstColumnValue b = columnValues.get(i1);
            ImportWstColumnValue a = columnValues.get(i2);
            if (b.getPosition().compareTo(a.getPosition()) < 0) {
                ImportWstColumnValue t = a; a = b; b = t;
            }

            if (a.getW().compareTo(b.getW()) < 0) ++up;
        }

        return up > S - up;
    }

    /** Get corresponding mapped wst-column (from database). */
    public WstColumn getPeer(River river) {
        if (peer == null) {
            Wst w = wst.getPeer(river);
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from WstColumn where" +
                " wst=:wst and name=:name" +
                " and source=:source" +
                " and position=:position");
            query.setParameter("wst",      w);
            query.setParameter("name",     name);
            query.setParameter("position", position);
            query.setParameter("source",   source);

            TimeInterval ti = timeInterval != null
                ? timeInterval.getPeer()
                : null;

            List<WstColumn> columns = query.list();
            if (columns.isEmpty()) {
                log.debug("source: " + source);
                peer = new WstColumn(
                    w, name, description, source, position, ti);
                session.save(peer);
            }
            else {
                peer = columns.get(0);
            }
        }
        return peer;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
