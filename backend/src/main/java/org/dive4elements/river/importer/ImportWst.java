/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.Wst;
import org.dive4elements.river.model.River;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.ArrayList;
import java.util.List;

/** Not (yet) db-mapped WST object. */
public class ImportWst
{
    private static Logger log = LogManager.getLogger(ImportWst.class);

    public interface ImportWstColumnFactory {
        ImportWstColumn create(ImportWst iw, int position);
    }

    public static final ImportWstColumnFactory COLUMN_FACTORY =
        new ImportWstColumnFactory() {
            @Override
            public ImportWstColumn create(ImportWst importWst, int position) {
                return new ImportWstColumn(importWst, null, null, position);
            }
        };

    protected String description;

    protected Integer kind;

    protected List<ImportWstColumn> columns;

    protected ImportUnit unit;

    protected ImportWstColumnFactory columnFactory;

    protected boolean kmUp;

    /** Wst as in db. */
    protected Wst peer;

    public ImportWst() {
        this(COLUMN_FACTORY);
    }

    public ImportWst(ImportWstColumnFactory columnFactory) {
        this.columnFactory = columnFactory;
        kind = 0;
        columns = new ArrayList<ImportWstColumn>();
    }

    public ImportWst(String description) {
        this(description, COLUMN_FACTORY);
    }

    public ImportWst(
        String description,
        ImportWstColumnFactory columnFactory
    ) {
        this(columnFactory);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public boolean getKmUp() {
        return kmUp;
    }

    public void setKmUp(boolean kmUp) {
        this.kmUp = kmUp;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Create columns that can be accessed with getColumn. */
    public void setNumberColumns(int numColumns) {
        for (int i = 0; i < numColumns; ++i) {
            columns.add(columnFactory.create(this, i));
        }
    }

    public int getNumberColumns() {
        return columns.size();
    }

    public ImportWstColumn getColumn(int index) {
        return columns.get(index);
    }

    public List<ImportWstColumn> getColumns() {
        return columns;
    }

    /** Adds a column. Assumes that columns wst is this instance. */
    public void addColumn(ImportWstColumn column) {
        columns.add(column);
    }

    public ImportUnit getUnit() {
        return unit;
    }

    public void setUnit(ImportUnit unit) {
        this.unit = unit;
    }

    public void storeDependencies(River river) {

        log.info("store '" + description + "'");
        getPeer(river);

        for (ImportWstColumn column: columns) {
            column.storeDependencies(river);
        }

        Session session = ImporterSession.getInstance().getDatabaseSession();
        session.flush();
    }

    public boolean guessWaterLevelIncreasing() {
        int up = 0;
        for (ImportWstColumn column: columns) {
            if (column.guessWaterLevelIncreasing()) ++up;
        }
        return up > columns.size() - up;
    }

    /** Get corresponding mapped wst (from database). */
    public Wst getPeer(River river) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from Wst where " +
                "river=:river and description=:description and kind=:kind");
            query.setParameter("river",       river);
            query.setParameter("description", description);
            query.setParameter("kind",        kind);
            List<Wst> wsts = query.list();
            if (wsts.isEmpty()) {
                peer = new Wst(river, description, kind);
                session.save(peer);
            }
            else {
                peer = wsts.get(0);
            }

        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
