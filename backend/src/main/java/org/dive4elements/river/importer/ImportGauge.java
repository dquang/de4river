/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Gauge;

import org.hibernate.Session;
import org.hibernate.Query;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.parsers.AtFileParser;
import org.dive4elements.river.importer.parsers.StaFileParser;

/** Gauge not in DB. */
public class ImportGauge
{
    private static Logger log = LogManager.getLogger(ImportGauge.class);

    public static final String HISTORICAL_DISCHARGE_TABLES =
        "Histor.Abflusstafeln";

    protected ImportRange range;

    protected File        staFile;
    protected File        atFile;

    protected String      name;
    protected BigDecimal  aeo;
    protected BigDecimal  datum;
    protected BigDecimal  station;
    protected Long        officialNumber;

    protected Gauge  peer;

    protected ImportDischargeTable dischargeTable;

    protected List<ImportMainValueType>  mainValueTypes;
    protected List<ImportNamedMainValue> namedMainValues;
    protected List<ImportMainValue>      mainValues;
    protected List<ImportDischargeTable> historicalDischargeTables;

    public ImportGauge() {
        historicalDischargeTables = new ArrayList<ImportDischargeTable>();
    }

    public ImportGauge(ImportRange range, File staFile, File atFile) {
        this();
        this.range   = range;
        this.staFile = staFile;
        this.atFile  = atFile;
    }

    public void setRange(ImportRange range) {
        this.range = range;
    }

    public void setStaFile(File staFile) {
        this.staFile = staFile;
    }

    public File getStaFile() {
        return staFile;
    }

    public void setAtFile(File atFile) {
        this.atFile = atFile;
    }

    public File getAtFile() {
        return atFile;
    }

    public BigDecimal getAeo() {
        return aeo;
    }

    public void setAeo(BigDecimal aeo) {
        this.aeo = aeo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDatum() {
        return datum;
    }

    public void setDatum(BigDecimal datum) {
        this.datum = datum;
    }

    public BigDecimal getStation() {
        return station;
    }

    public void setStation(BigDecimal station) {
        this.station = station;
    }

    public Long getOfficialNumber() {
        return officialNumber;
    }

    public void setOfficialNumber(Long officialNumber) {
        this.officialNumber = officialNumber;
    }

    public ImportDischargeTable getDischargeTable() {
        return dischargeTable;
    }

    public void setDischargeTable(ImportDischargeTable dischargeTable) {
        this.dischargeTable = dischargeTable;
    }

    public List<ImportMainValueType> getMainValueTypes() {
        return mainValueTypes;
    }

    public void setMainValueTypes(List<ImportMainValueType> mainValueTypes) {
        this.mainValueTypes = mainValueTypes;
    }

    public List<ImportNamedMainValue> getNamedMainValues() {
        return namedMainValues;
    }

    public void setNamedMainValues(
        List<ImportNamedMainValue> namedMainValues
    ) {
        this.namedMainValues = namedMainValues;
    }

    public List<ImportMainValue> getMainValues() {
        return mainValues;
    }

    public void setMainValues(List<ImportMainValue> mainValues) {
        this.mainValues = mainValues;
    }

    public void parseDependencies() throws IOException {
        StaFileParser sfp = new StaFileParser();
        if (!sfp.parse(this)) {
            log.error("Parsing STA file failed.");
        }

        AtFileParser afp = new AtFileParser();
        setDischargeTable(afp.parse(getAtFile()));
        parseHistoricalDischargeTables();
    }

    public void parseHistoricalDischargeTables() throws IOException {
        if (Config.INSTANCE.skipHistoricalDischargeTables()) {
            log.info("skip historical discharge tables");
            return;
        }

        log.info("parse historical discharge tables");

        File riverDir = atFile.getParentFile().getParentFile();

        File histDischargeDir = FileTools.repair(
            new File(riverDir, HISTORICAL_DISCHARGE_TABLES));

        if (!histDischargeDir.isDirectory() || !histDischargeDir.canRead()) {
            log.info("cannot find '" + histDischargeDir + "'");
            return;
        }

        histDischargeDir = FileTools.repair(
            new File(histDischargeDir, getName()));

        if (!histDischargeDir.isDirectory() || !histDischargeDir.canRead()) {
            log.info("cannot find '" + histDischargeDir + "'");
            return;
        }

        File [] files = histDischargeDir.listFiles();

        if (files == null) {
            log.info("cannot read directory '" + histDischargeDir + "'");
            return;
        }

        for (File file: files) {
            if (!file.isFile() || !file.canRead()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".at")) {
                continue;
            }
            log.info("found at file '" + file.getName() + "'");

            AtFileParser afp = new AtFileParser();
            historicalDischargeTables.add(
                afp.parse(file, HISTORICAL_DISCHARGE_TABLES + "/", 1));
        }
    }

    public void storeDependencies(River river) {

        Gauge gauge = getPeer(river);

        if (mainValueTypes != null) {
            log.info("store main value types");
            for (ImportMainValueType mainValueType: mainValueTypes) {
                mainValueType.getPeer();
            }
        }

        if (namedMainValues != null) {
            log.info("store named main values");
            for (ImportNamedMainValue namedMainValue: namedMainValues) {
                namedMainValue.getPeer();
            }
        }

        if (mainValues != null) {
            log.info("store main values");
            for (ImportMainValue mainValue: mainValues) {
                mainValue.getPeer(river);
            }
        }

        storeDischargeTable(gauge);
        storeHistoricalDischargeTable(gauge);
    }

    public void storeDischargeTable(Gauge gauge) {
        log.info("store discharge table");
        dischargeTable.getPeer(gauge);
        dischargeTable.storeDependencies(gauge);
    }

    public void storeHistoricalDischargeTable(Gauge gauge) {
        log.info("store historical discharge tables");
        for (ImportDischargeTable hdt: historicalDischargeTables) {
            hdt.storeDependencies(gauge);
        }
    }

    public Gauge getPeer(River river) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from Gauge where officialNumber=:officialNumber " +
                "and river.id=:river");
            query.setParameter("officialNumber", officialNumber);
            query.setParameter("river", river.getId());
            List<Gauge> gauges = query.list();
            if (gauges.isEmpty()) {
                peer = new Gauge(
                    name, river,
                    station, aeo, datum,
                    officialNumber,
                    range.getPeer(river));
                session.save(peer);
            }
            else {
                peer = gauges.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
