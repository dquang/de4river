/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import org.dive4elements.river.etl.db.ConnectedStatements;
import org.dive4elements.river.etl.db.SymbolicStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** A Discharge Table. */
public class DischargeTable
{
    private static Logger log = LogManager.getLogger(DischargeTable.class);

    protected int          id;
    protected int          gaugeId;
    protected TimeInterval timeInterval;
    protected String       description;
    protected String       bfgId;
    protected Set<WQ>      values;

    public DischargeTable() {
    }

    public DischargeTable(
        int          gaugeId,
        TimeInterval timeInterval,
        String       description,
        String       bfgId
    ) {
        this.gaugeId      = gaugeId;
        this.timeInterval = timeInterval;
        this.description  = description;
        this.bfgId        = bfgId;
        values = new TreeSet<WQ>(WQ.EPS_CMP);
    }

    public DischargeTable(
        int          id,
        int          gaugeId,
        TimeInterval timeInterval,
        String       description,
        String       bfgId
    ) {
        this(gaugeId, timeInterval, description, bfgId);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGaugeId() {
        return gaugeId;
    }

    public void setGaugeId(int gaugeId) {
        this.gaugeId = gaugeId;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBfgId() {
        return bfgId;
    }

    public void setBfgId(String bfgId) {
        this.bfgId = bfgId;
    }


    public void clearValues() {
        values.clear();
    }

    public Set<WQ> getValues() {
        return values;
    }

    public void setValues(Set<WQ> values) {
        this.values = values;
    }


    protected void loadValues(SymbolicStatement.Instance query)
    throws SQLException
    {
        ResultSet rs = query.executeQuery();
        while (rs.next()) {
            int    id = rs.getInt("id");
            double w  = rs.getDouble("w");
            double q  = rs.getDouble("q");
            if (!values.add(new WQ(id, w, q))) {
                log.warn("FLYS/AFT: Value duplication w="+w+" q="+q+". -> ignore.");
            }
        }
        rs.close();
    }

    public void loadAftValues(SyncContext context) throws SQLException {
        loadValues(context.getAftStatements()
            .getStatement("select.tafelwert")
            .clearParameters()
            .setInt("number", getId()));
    }

    public void loadFlysValues(SyncContext context) throws SQLException {
        loadValues(context.getFlysStatements()
            .getStatement("select.discharge.table.values")
            .clearParameters()
            .setInt("table_id", getId()));
    }

    public void storeFlysValues(
        SyncContext context,
        int         dischargeTableId
    )
    throws SQLException
    {
        ConnectedStatements flysStatements = context.getFlysStatements();

        // Create the ids.
        SymbolicStatement.Instance nextId = flysStatements
            .getStatement("next.discharge.table.values.id");

        // Insert the values.
        SymbolicStatement.Instance insertDTV = flysStatements
            .getStatement("insert.discharge.table.value");

        for (WQ wq: values) {
            int wqId;
            ResultSet rs = nextId.executeQuery();
            try {
                rs.next();
                wqId = rs.getInt("discharge_table_values_id");
            }
            finally {
                rs.close();
            }

            insertDTV
                .clearParameters()
                .setInt("id", wqId)
                .setInt("table_id", dischargeTableId)
                .setDouble("w", wq.getW())
                .setDouble("q", wq.getQ())
                .execute();
        }
    }

    public static List<DischargeTable> loadFlysDischargeTables(
        SyncContext context,
        int         gaugeId
    )
    throws SQLException
    {
        List<DischargeTable> dts = new ArrayList<DischargeTable>();

        ResultSet rs = context
            .getFlysStatements()
            .getStatement("select.gauge.discharge.tables")
            .clearParameters()
            .setInt("gauge_id", gaugeId)
            .executeQuery();
        try {
            OUTER: while (rs.next()) {
                int    id          = rs.getInt("id");
                String description = rs.getString("description");
                String bfgId       = rs.getString("bfg_id");
                if (description == null) {
                    description = "";
                }
                if (bfgId == null) {
                    bfgId = "";
                }
                for (DischargeTable dt: dts) {
                    if (dt.getBfgId().equals(bfgId)) {
                        log.warn("FLYS: Found discharge table '" +
                            bfgId + "' with same bfg_id. -> ignore");
                        continue OUTER;
                    }
                }
                Date startTime = rs.getDate("start_time");
                Date stopTime  = rs.getDate("stop_time");
                TimeInterval ti = startTime == null
                    ? null
                    : new TimeInterval(startTime, stopTime);

                DischargeTable dt = new DischargeTable(
                    id, gaugeId, ti, description, bfgId);
                dts.add(dt);
            }
        }
        finally {
            rs.close();
        }

        return dts;
    }

    public static List<DischargeTable> loadAftDischargeTables(
        SyncContext context,
        Long        officialNumber
    )
    throws SQLException
    {
        return loadAftDischargeTables(context, officialNumber, 0);
    }

    public static List<DischargeTable> loadAftDischargeTables(
        SyncContext context,
        Long        officialNumber,
        int         flysGaugeId
    )
    throws SQLException
    {
        List<DischargeTable> dts = new ArrayList<DischargeTable>();

        ResultSet rs = context
            .getAftStatements()
            .getStatement("select.abflusstafel")
            .clearParameters()
            .setString("number", "%" + officialNumber)
            .executeQuery();
        try {
            OUTER: while (rs.next()) {
                int  dtId = rs.getInt("ABFLUSSTAFEL_NR");
                Date from = rs.getDate("GUELTIG_VON");
                Date to   = rs.getDate("GUELTIG_BIS");

                if (from == null) {
                    log.warn("AFT: ABFLUSSTAFEL_NR = "
                        + dtId + ": GUELTIG_VON = NULL -> ignored.");
                    continue;
                }

                if (to != null && from.after(to)) {
                        log.warn("AFT: ABFLUSSTAFEL_NR = "
                        + dtId + ": " + from + " > " + to + ". -> swap");
                    Date temp = from;
                    from = to;
                    to = temp;
                }

                String description = rs.getString("ABFLUSSTAFEL_BEZ");
                if (description == null) {
                    description = String.valueOf(officialNumber);
                }

                String bfgId = rs.getString("BFG_ID");
                if (bfgId == null) {
                    bfgId = "";
                }

                for (DischargeTable dt: dts) {
                    if (dt.getBfgId().equals(bfgId)) {
                        log.warn("AFT: Found discharge table '" +
                            bfgId + "' with same bfg_id. -> ignore.");
                        continue OUTER;
                    }
                }

                TimeInterval timeInterval = new TimeInterval(from, to);

                DischargeTable dt = new DischargeTable(
                    dtId,
                    flysGaugeId,
                    timeInterval,
                    description,
                    bfgId);
                dts.add(dt);
            }
        }
        finally {
            rs.close();
        }

        return dts;
    }

    public void persistFlysTimeInterval(
        SyncContext context
    )
    throws SQLException
    {
        if (timeInterval != null) {
            timeInterval = context.fetchOrCreateFLYSTimeInterval(
                timeInterval);
        }
    }

    public int persistFlysDischargeTable(
        SyncContext context,
        int         gaugeId
    )
    throws SQLException
    {
        ConnectedStatements flysStatements =
            context.getFlysStatements();

        int flysId;

        ResultSet rs = flysStatements
            .getStatement("next.discharge.id")
            .executeQuery();
        try {
            rs.next();
            flysId = rs.getInt("discharge_table_id");
        }
        finally {
            rs.close();
        }

        SymbolicStatement.Instance insertDT = flysStatements
            .getStatement("insert.dischargetable")
            .clearParameters()
            .setInt("id", flysId)
            .setInt("gauge_id", gaugeId)
            .setString("description", description)
            .setString("bfg_id", bfgId);

        if (timeInterval != null) {
            insertDT.setInt("time_interval_id", timeInterval.getId());
        }
        else {
            insertDT.setNull("time_interval_id", Types.INTEGER);
        }

        insertDT.execute();

        if (log.isDebugEnabled()) {
            log.debug("FLYS: Created discharge table id: " + id);
        }

        return flysId;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
