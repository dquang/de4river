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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class River
extends      IdPair
{
    private static Logger log = LogManager.getLogger(River.class);

    protected String name;

    protected double from;
    protected double to;

    public River() {
    }

    public River(int id1, String name, double from, double to) {
        super(id1);
        this.name = name;
        this.from = from;
        this.to   = to;
    }

    public River(int id1, int id2, String name) {
        super(id1, id2);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public boolean inside(double x) {
        return x >= from && x <= to;
    }

    public boolean sync(SyncContext context) throws SQLException {
        log.info("sync river: " + this);

        // Only take relevant gauges into account.
        Map<Long, DIPSGauge> dipsGauges = context.getDIPSGauges(name, from, to);

        ConnectedStatements flysStatements = context.getFlysStatements();
        ConnectedStatements aftStatements  = context.getAftStatements();

        String riverName = getName();
        String lowerRiverName = riverName.toLowerCase();

        Map<Long, DIPSGauge> aftDIPSGauges = new HashMap<Long, DIPSGauge>();

        ResultSet messstellenRs = aftStatements
            .getStatement("select.messstelle")
            .clearParameters()
            .setInt("GEWAESSER_NR", id2)
            .executeQuery();

        try {
            while (messstellenRs.next()) {
                String name = messstellenRs.getString("NAME");
                String num  = messstellenRs.getString("MESSSTELLE_NR");
                double station = messstellenRs.getDouble("STATIONIERUNG");

                if (!messstellenRs.wasNull() && !inside(station)) {
                    log.warn("Station found in AFT but in not range: " + station);
                    continue;
                }

                Long number = SyncContext.numberToLong(num);
                if (number == null) {
                    log.warn("AFT: Invalid MESSSTELLE_NR for MESSSTELLE '"+name+"'");
                    continue;
                }
                DIPSGauge dipsGauge = dipsGauges.get(number);
                if (dipsGauge == null) {
                    log.warn(
                        "DIPS: MESSSTELLE '" + name + "' not found in DIPS. " +
                        "Gauge number used for lookup: " + number);
                    continue;
                }
                String gaugeRiver = dipsGauge.getRiverName();
                if (!lowerRiverName.contains(gaugeRiver.toLowerCase())) {
                    log.warn(
                        "DIPS: MESSSTELLE '" + name +
                        "' is assigned to river '" + gaugeRiver +
                        "'. Needs to be on '" + riverName + "'.");
                    continue;
                }
                dipsGauge.setAftName(name);
                dipsGauge.setOfficialNumber(number);
                aftDIPSGauges.put(number, dipsGauge);
            }
        }
        finally {
            messstellenRs.close();
        }

        List<DIPSGauge> updateGauges = new ArrayList<DIPSGauge>();

        ResultSet gaugesRs = flysStatements
            .getStatement("select.gauges")
            .clearParameters()
            .setInt("river_id", id1).executeQuery();

        TreeMap<Double, String> station2gaugeName = new TreeMap<Double, String>(
            new Comparator<Double>() {
                @Override
                public int compare(Double a, Double b) {
                    double diff = a - b;
                    if (diff < -0.0001) return -1;
                    if (diff >  0.0001) return +1;
                    return 0;
                }
            });

        try {
            while (gaugesRs.next()) {
                int gaugeId = gaugesRs.getInt("id");
                String name = gaugesRs.getString("name");
                long   number = gaugesRs.getLong("official_number");
                double station = gaugesRs.getDouble("station");
                station2gaugeName.put(station, name);

                if (gaugesRs.wasNull()) {
                    log.warn("FLYS: Gauge '" + name +
                        "' has no official number. Ignored.");
                    continue;
                }
                Long key = Long.valueOf(number);
                DIPSGauge aftDIPSGauge = aftDIPSGauges.remove(key);
                if (aftDIPSGauge == null) {
                    log.warn("FLYS: Gauge '" + name + "' number " + number +
                        " is not found in AFT/DIPS.");
                    continue;
                }
                aftDIPSGauge.setFlysId(gaugeId);
                log.info("Gauge '" + name +
                    "' found in FLYS, AFT and DIPS. -> Update");
                updateGauges.add(aftDIPSGauge);
            }
        }
        finally {
            gaugesRs.close();
        }

        boolean modified = createGauges(
            context, aftDIPSGauges, station2gaugeName);

        modified |= updateGauges(context, updateGauges);

        return modified;
    }

    protected boolean updateGauges(
        SyncContext     context,
        List<DIPSGauge> gauges
    )
    throws SQLException
    {
        boolean modified = false;

        for (DIPSGauge gauge: gauges) {
            // XXX: Do dont modify the master AT.
            // modified |= updateBfGIdOnMasterDischargeTable(context, gauge);
            modified |= updateGauge(context, gauge);
        }

        return modified;
    }

    protected boolean updateBfGIdOnMasterDischargeTable(
        SyncContext context,
        DIPSGauge   gauge
    ) throws SQLException {
        log.info(
            "FLYS: Updating master discharge table bfg_id for '" +
            gauge.getAftName() + "'");
        ConnectedStatements flysStatements = context.getFlysStatements();

        ResultSet rs = flysStatements
            .getStatement("select.gauge.master.discharge.table")
            .clearParameters()
            .setInt("gauge_id", gauge.getFlysId())
            .executeQuery();

        int flysId;

        try {
            if (!rs.next()) {
                log.error(
                    "FLYS: No master discharge table found for gauge '" +
                    gauge.getAftName() + "'");
                return false;
            }
            String bfgId = rs.getString("bfg_id");
            if (!rs.wasNull()) { // already has BFG_ID
                return false;
            }
            flysId = rs.getInt("id");
        } finally {
            rs.close();
        }

        // We need to find out the BFG_ID of the current discharge table
        // for this gauge in AFT.

        ConnectedStatements aftStatements = context.getAftStatements();

        rs = aftStatements
            .getStatement("select.bfg.id.current")
            .clearParameters()
            .setString("number", "%" + gauge.getOfficialNumber())
            .executeQuery();

        String bfgId = null;

        try {
            if (rs.next()) {
                bfgId = rs.getString("BFG_ID");
            }
        } finally {
            rs.close();
        }

        if (bfgId == null) {
            log.warn(
                "No BFG_ID found for current discharge table of gauge '" +
                gauge + "'");
            return false;
        }

        // Set the BFG_ID in FLYS.
        flysStatements.beginTransaction();
        try {
            flysStatements
                .getStatement("update.bfg.id.discharge.table")
                .clearParameters()
                .setInt("id", flysId)
                .setString("bfg_id", bfgId)
                .executeUpdate();
            flysStatements.commitTransaction();
        } catch (SQLException sqle) {
            flysStatements.rollbackTransaction();
            log.error(sqle, sqle);
            return false;
        }

        return true;
    }

    protected boolean updateGauge(
        SyncContext context,
        DIPSGauge   gauge
    )
    throws SQLException
    {
        log.info("FLYS: Updating gauge '" + gauge.getAftName() + "'.");
        // We need to load all discharge tables from both databases
        // of the gauge and do some pairing based on their bfg_id.

        boolean modified = false;

        ConnectedStatements flysStatements = context.getFlysStatements();

        flysStatements.beginTransaction();
        try {
            List<DischargeTable> flysDTs =
                DischargeTable.loadFlysDischargeTables(
                    context, gauge.getFlysId());

            List<DischargeTable> aftDTs =
                DischargeTable.loadAftDischargeTables(
                    context, gauge.getOfficialNumber());

            Map<String, DischargeTable> bfgId2FlysDT =
                new HashMap<String, DischargeTable>();

            for (DischargeTable dt: flysDTs) {
                String bfgId = dt.getBfgId();
                if (bfgId == null) {
                    log.warn("FLYS: discharge table " + dt.getId()
                        + " has no bfg_id. Ignored.");
                    continue;
                }
                bfgId2FlysDT.put(bfgId, dt);
            }

            List<DischargeTable> createDTs = new ArrayList<DischargeTable>();

            for (DischargeTable aftDT: aftDTs) {
                String bfgId = aftDT.getBfgId();
                DischargeTable flysDT = bfgId2FlysDT.remove(bfgId);
                if (flysDT != null) {
                    // Found in AFT and FLYS.
                    log.info("FLYS: Discharge table '" + bfgId
                        + "' found in AFT and FLYS. -> update");
                    // Create the W/Q diff.
                    modified |= writeWQChanges(context, flysDT, aftDT);
                }
                else {
                    log.info("FLYS: Discharge table '" + bfgId
                        + "' not found in FLYS. -> create");
                    createDTs.add(aftDT);
                }
            }

            modified |= deleteDischargeTables(context, bfgId2FlysDT);

            log.info("FLYS: Copy " + createDTs.size() +
                " discharge tables over from AFT.");

            // Create the new discharge tables.
            for (DischargeTable aftDT: createDTs) {
                createDischargeTable(context, aftDT, gauge.getFlysId());
                modified = true;
            }

            flysStatements.commitTransaction();
        }
        catch (SQLException sqle) {
            flysStatements.rollbackTransaction();
            log.error(sqle, sqle);
            modified = false;
        }

        return modified;
    }

    protected boolean writeWQChanges(
        SyncContext    context,
        DischargeTable flysDT,
        DischargeTable aftDT
    )
    throws SQLException
    {
        flysDT.loadFlysValues(context);
        aftDT.loadAftValues(context);
        WQDiff diff = new WQDiff(flysDT.getValues(), aftDT.getValues());
        if (diff.hasChanges()) {
            diff.writeChanges(context, flysDT.getId());
            return true;
        }
        return false;
    }

    protected boolean createGauges(
        SyncContext          context,
        Map<Long, DIPSGauge> gauges,
        Map<Double, String>  station2gaugeName
    )
    throws SQLException
    {
        ConnectedStatements flysStatements = context.getFlysStatements();

        SymbolicStatement.Instance nextId =
            flysStatements.getStatement("next.gauge.id");

        SymbolicStatement.Instance insertStmnt =
            flysStatements.getStatement("insert.gauge");

        boolean modified = false;

        for (Map.Entry<Long, DIPSGauge> entry: gauges.entrySet()) {
            Long      officialNumber = entry.getKey();
            DIPSGauge gauge          = entry.getValue();

            log.info("Gauge '" + gauge.getAftName() +
                "' not in FLYS but in AFT/DIPS. -> Create");

            String flysGaugeName = station2gaugeName.get(gauge.getStation());
            if (flysGaugeName != null) {
                log.warn("FLYS: AFT gauge " + gauge.getName() +
                    " has same station as FLYS gauge " + flysGaugeName +
                    " -> ignored.");
                continue;
            }

            if (!gauge.hasDatums()) {
                log.warn("DIPS: Gauge '" +
                    gauge.getAftName() + "' has no datum. Ignored.");
                continue;
            }

            ResultSet rs = null;
            flysStatements.beginTransaction();
            try {
                (rs = nextId.executeQuery()).next();
                int gaugeId = rs.getInt("gauge_id");
                rs.close(); rs = null;

                insertStmnt
                    .clearParameters()
                    .setInt("id", gaugeId)
                    .setString("name", gauge.getAftName())
                    .setInt("river_id", id1)
                    .setDouble("station", gauge.getStation())
                    .setDouble("aeo", gauge.getAeo())
                    .setLong("official_number", officialNumber)
                    .setDouble("datum", gauge.getLatestDatum().getValue());

                insertStmnt.execute();

                log.info("FLYS: Created gauge '" + gauge.getAftName() +
                    "' with id " + gaugeId + ".");

                gauge.setFlysId(gaugeId);
                createDischargeTables(context, gauge);
                flysStatements.commitTransaction();
                modified = true;
            }
            catch (SQLException sqle) {
                flysStatements.rollbackTransaction();
                log.error(sqle, sqle);
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }

        return modified;
    }

    protected void createDischargeTable(
        SyncContext    context,
        DischargeTable aftDT,
        int            flysGaugeId
    )
    throws SQLException
    {
        aftDT.persistFlysTimeInterval(context);
        int flysId = aftDT.persistFlysDischargeTable(context, flysGaugeId);

        aftDT.loadAftValues(context);
        aftDT.storeFlysValues(context, flysId);
    }

    protected boolean deleteDischargeTables(
        SyncContext                 context,
        Map<String, DischargeTable> tables
    )
    throws SQLException
    {
        ConnectedStatements flysStatements = context.getFlysStatements();

        SymbolicStatement.Instance deleteDischargeTableValues =
            flysStatements.getStatement("delete.discharge.table.values");

        SymbolicStatement.Instance deleteDischargeTable =
            flysStatements.getStatement("delete.discharge.table");

        boolean modified = false;

        for (Map.Entry<String, DischargeTable> entry: tables.entrySet()) {
            log.info("FLYS: Discharge table '" + entry.getKey()
                + "' found in FLYS but not in AFT. -> delete");
            int id = entry.getValue().getId();

            deleteDischargeTableValues
                .clearParameters()
                .setInt("id", id);
            deleteDischargeTableValues.execute();

            deleteDischargeTable
                .clearParameters()
                .setInt("id", id);
            deleteDischargeTable.execute();

            modified = true;
        }
        return modified;
    }

    protected void createDischargeTables(
        SyncContext context,
        DIPSGauge   gauge
    )
    throws SQLException
    {
        log.info("FLYS: Create discharge tables for '" +
            gauge.getAftName() + "'.");

        // Load the discharge tables from AFT.
        List<DischargeTable> dts = loadAftDischargeTables(
            context, gauge);

        // Persist the time intervals.
        persistFlysTimeIntervals(context, dts);

        // Persist the discharge tables
        int [] flysDTIds = persistFlysDischargeTables(
            context, dts, gauge.getFlysId());

        // Copy over the W/Q values
        copyWQsFromAftToFlys(context, dts, flysDTIds);
    }

    protected List<DischargeTable> loadAftDischargeTables(
        SyncContext context,
        DIPSGauge   gauge
    )
    throws SQLException
    {
        return DischargeTable.loadAftDischargeTables(
            context, gauge.getOfficialNumber(), gauge.getFlysId());
    }

    protected void persistFlysTimeIntervals(
        SyncContext          context,
        List<DischargeTable> dts
    )
    throws SQLException
    {
        for (DischargeTable dt: dts) {
            dt.persistFlysTimeInterval(context);
        }
    }

    protected int [] persistFlysDischargeTables(
        SyncContext          context,
        List<DischargeTable> dts,
        int                  flysGaugeId
    )
    throws SQLException
    {
        int [] flysDTIds = new int[dts.size()];

        for (int i = 0; i < flysDTIds.length; ++i) {
            flysDTIds[i] = dts.get(i)
                .persistFlysDischargeTable(context, flysGaugeId);
        }

        return flysDTIds;
    }

    protected void copyWQsFromAftToFlys(
        SyncContext          context,
        List<DischargeTable> dts,
        int []               flysDTIds
    )
    throws SQLException
    {
        for (int i = 0; i < flysDTIds.length; ++i) {
            DischargeTable dt = dts.get(i);
            dt.loadAftValues(context);
            dt.storeFlysValues(context, flysDTIds[i]);
            dt.clearValues(); // To save memory.
        }
    }

    public String toString() {
        return "[River: name=" + name + ", " + super.toString() + "]";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
