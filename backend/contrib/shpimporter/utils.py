import os
import sys
import logging

try:
    from osgeo import ogr
except ImportError:
    import ogr

logger = logging.getLogger("utils")

SHP='.shp'
SQL_SELECT_RIVER_ID="SELECT id FROM rivers WHERE name = %s"
SQL_SELECT_RIVER_ID_ORA="SELECT id FROM rivers WHERE name = :s"

def findShapefiles(path):
    shapes = []

    for root, dirs, files in os.walk(path):
        if len(files) == 0:
            continue

        logger.debug("Processing directory '%s' with %i files " % (root, len(files)))

        for f in files:
            idx = f.find(SHP)
            if (idx+len(SHP)) == len(f):
                shapes.append((f.replace(SHP, ''), root + "/" + f))

    return shapes

def getRiverId(dbconn, name, oracle):
    """
    Returns the id of the river "name"
    Dbconn must be a python database connection api compliant object
    """
    cur = dbconn.cursor()
    if oracle:
        # This is stupid and shoudl not be neccessary. But I don't
        # know how to make it work both ways. aheinecke - 02/2013
        stmt = SQL_SELECT_RIVER_ID_ORA
    else:
        stmt = SQL_SELECT_RIVER_ID
    cur.execute(stmt, (getUTF8(name),))
    row = cur.fetchone()
    if row:
        return row[0]
    else:
        return 0

def getUTF8(string):
    """
    Tries to convert the string to a UTF-8 encoding by first checking if it
    is UTF-8 and then trying cp1252
    """
    try:
        return unicode.encode(unicode(string, "UTF-8"), "UTF-8")
    except UnicodeDecodeError:
        # Probably European Windows names so lets try again
        return unicode.encode(unicode(string, "cp1252"), "UTF-8")

def getUTF8Path(path):
    """
    Tries to convert path to utf-8 by first checking the filesystemencoding
    and trying the default windows encoding afterwards.
    Returns a valid UTF-8 encoded unicode object or throws a UnicodeDecodeError
    """
    try:
        return unicode.encode(unicode(path, sys.getfilesystemencoding()), "UTF-8")
    except UnicodeDecodeError:
        # Probably European Windows names so lets try again
        return unicode.encode(unicode(path, "cp1252"), "UTF-8")

WKB_MAP = {
    ogr.wkb25Bit :                'wkb25Bit',
    ogr.wkbGeometryCollection :   'wkbGeometryCollection',
    ogr.wkbGeometryCollection25D :'wkbGeometryCollection25D',
    ogr.wkbLineString :           'wkbLineString',
    ogr.wkbLineString25D :        'wkbLineString25D',
    ogr.wkbLinearRing :           'wkbLinearRing',
    ogr.wkbMultiLineString :      'wkbMultiLineString',
    ogr.wkbMultiLineString25D :   'wkbMultiLineString25D',
    ogr.wkbMultiPoint :           'wkbMultiPoint',
    ogr.wkbMultiPoint25D :        'wkbMultiPoint25D',
    ogr.wkbMultiPolygon :         'wkbMultiPolygon',
    ogr.wkbMultiPolygon25D :      'wkbMultiPolygon25D',
    ogr.wkbNDR :                  'wkbNDR',
    ogr.wkbNone :                 'wkbNone',
    ogr.wkbPoint :                'wkbPoint',
    ogr.wkbPoint25D :             'wkbPoint25D',
    ogr.wkbPolygon :              'wkbPolygon',
    ogr.wkbPolygon25D :           'wkbPolygon25D',
    ogr.wkbUnknown :              'wkbUnknown',
    ogr.wkbXDR :                  'wkbXDR'
}

def getWkbString(type):
    return WKB_MAP.get(type) or "Unknown"

def convertToMultiLine(feature):
    """
    Converts a feature to a multiline feature.
    """
    geometry = feature.GetGeometryRef()
    # SRS information is lost while forcing to multiline
    srs = geometry.GetSpatialReference()
    geometry = ogr.ForceToMultiLineString(geometry)
    geometry.AssignSpatialReference(srs)
    feature.SetGeometry(geometry)
    return feature

def convertToMultiPolygon(feature):
    """
    Converts a feature to a multiline feature.
    """
    geometry = feature.GetGeometryRef()
    # SRS information is lost while forcing to multiline
    srs = geometry.GetSpatialReference()
    geometry = ogr.ForceToMultiPolygon(geometry)
    geometry.AssignSpatialReference(srs)
    feature.SetGeometry(geometry)
    return feature
