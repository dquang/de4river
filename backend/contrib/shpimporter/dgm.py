# -*- coding: latin-1 -*-

import codecs
import utils
import datetime

def latin(string):
    return unicode(string, "latin1")

import logging
logger = logging.getLogger("DGM")


# <dbfield> : (<csvfield>, conversion function)
DGM_MAP = {
    "projection"      : "Projektion",
    "elevation_state" : latin("Höhenstatus"),
    "format"          : "Format",
    "border_break"    : ("Bruchkanten",
        lambda x: True if x.lower() == "Ja" else False),
    "resolution"      : (latin("Auflösung"), lambda x: x),
#   "description"     : 
    "srid"            : "SRID",
    "path"            : ("Pfad_Bestand", lambda x: x),
    }

SQL_INSERT_DGT = "INSERT INTO dem (river_id, name," \
        " time_interval_id, range_id, " + ", ".join(DGM_MAP.keys()) + \
        ") VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
SQL_INSERT_DGT_ORA = "INSERT INTO dem (river_id, name," \
        " time_interval_id, range_id, " + ", ".join(DGM_MAP.keys()) + \
        ") VALUES (:s, :s, :s, :s, :s, :s, :s, :s, :s, :s, :s)"
SQL_SELECT_TIME_ID = """
SELECT id FROM time_intervals WHERE start_time = %s AND stop_time = %s
"""
SQL_INSERT_TIME_ID = """
INSERT INTO time_intervals (id, start_time, stop_time) VALUES (%s, %s, %s)
"""
SQL_SELECT_TIME_ID_ORA = """
SELECT id FROM time_intervals WHERE start_time = :s AND stop_time = :s
"""
SQL_INSERT_TIME_ID_ORA = """
INSERT INTO time_intervals (id, start_time, stop_time) VALUES (:s, :s, :s)
"""
SQL_SELECT_RANGE_ID = """
SELECT id FROM ranges WHERE river_id = %s AND a = %s AND b = %s
"""
SQL_INSERT_RANGE_ID = """
INSERT INTO ranges (id, river_id, a, b) VALUES (%s, %s, %s, %s)
"""
SQL_SELECT_RANGE_ID_ORA = """
SELECT id FROM ranges WHERE river_id = :s AND a = :s AND b = :s
"""
SQL_INSERT_RANGE_ID_ORA = """
INSERT INTO ranges (id, river_id, a, b) VALUES (:s, :s, :s, :s)
"""
SQL_NEXT_ID     = "select nextval('%s_ID_SEQ')"
SQL_NEXT_ID_ORA = "select %s_ID_SEQ.nextval FROM dual"

def next_id(cur, relation, oracle):
    if oracle:
        cur.execute(SQL_NEXT_ID_ORA % relation.upper())
    else:
        cur.execute(SQL_NEXT_ID % relation.upper())
    idx = cur.fetchone()[0]
    return idx

def get_range_id(cur, river_id, a, b, oracle):
    if oracle:
        cur.execute(SQL_SELECT_RANGE_ID_ORA, (river_id, a, b))
    else:
        cur.execute(SQL_SELECT_RANGE_ID, (river_id, a, b))
    row = cur.fetchone()
    if row: return row[0]
    idx = next_id(cur, "ranges", oracle)
    if oracle:
        cur.execute(SQL_INSERT_RANGE_ID_ORA, (idx, river_id, a, b))
    else:
        cur.execute(SQL_INSERT_RANGE_ID, (idx, river_id, a, b))
    cur.connection.commit()
    return idx

def get_time_interval_id(cur, a, b, oracle):
    if not a or not b:
        return None
    if oracle:
        cur.execute(SQL_SELECT_TIME_ID_ORA, (a, b))
    else:
        cur.execute(SQL_SELECT_TIME_ID, (a, b))
    row = cur.fetchone()
    if row: return row[0]
    idx = next_id(cur, "time_intervals", oracle)
    if oracle:
        cur.execute(SQL_INSERT_TIME_ID_ORA, (idx, a, b))
    else:
        cur.execute(SQL_INSERT_TIME_ID, (idx, a, b))
    cur.connection.commit()
    return idx

def insertRiverDgm(dbconn, dgmfile, river_name, dry_run, oracle):
    with codecs.open(dgmfile, "r", "latin1") as csvfile:
        firstline = csvfile.readline()
        names = firstline.split(";")
        namedict = {}
        field_nr = 0
        for name in names:
            namedict[name] = field_nr
            field_nr += 1

        river_id = utils.getRiverId(dbconn, river_name, oracle)
        for line in csvfile:
            fields = line.split(";")
            if not fields: continue
            if fields[namedict[latin("Gewässer")]] != \
                    unicode(utils.getUTF8(river_name),'UTF-8'):
                continue
            else:
                values=[]
                for key, val in DGM_MAP.items():
                    if isinstance(val, tuple):
                        values.append(val[1](fields[namedict[val[0]]]))
                    else:
                        values.append(unicode.encode(
                            fields[namedict[val]], "UTF-8"))
                km_von = min(float(fields[namedict["km_von"]]), 
                    float(fields[namedict["km_bis"]]))
                km_bis = max(float(fields[namedict["km_von"]]), 
                    float(fields[namedict["km_bis"]]))
                year_from = None
                year_to = None
                try:
                    year_from = datetime.datetime(
                        int(fields[namedict["Jahr_von"]]), 1, 1)
                    year_to = datetime.datetime(
                        int(fields[namedict["Jahr_bis"]]),1 ,1)
                except ValueError:
                    logger.warn("Invalid numbers (or none) found in year_from and year_to")

                name = "%s KM %s - %s" % (unicode(river_name, "latin1"), km_von, km_bis)
                cur = dbconn.cursor()
                range_id = get_range_id(cur, river_id, float(km_von),
                    float(km_bis), oracle)
                time_interval_id = get_time_interval_id(cur, year_from,
                    year_to, oracle)

                if oracle:
                    stmt = SQL_INSERT_DGT_ORA
                else:
                    stmt = SQL_INSERT_DGT

                cur.execute(stmt, [river_id, name, time_interval_id,
                    range_id] + values)

        if not dry_run:
            dbconn.commit()

