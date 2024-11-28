try:
    from osgeo import ogr
except ImportError:
    import ogr

import utils, optparse
import sys
import os
import logging

from uesg  import UESG
from axis  import Axis
from km    import KM
from fixpoints import Fixpoint
from buildings import Building
from crosssectiontracks import CrosssectionTrack
from floodplains import Floodplain
from boundaries import HydrBoundary, HydrBoundaryPoly
from hws import HWSLines, HWSPoints
from jetties import Jetties
from dgm import insertRiverDgm
from floodmarks import Floodmark

logger = logging.getLogger("shpimporter")

os.environ["NLS_LANG"] = ".AL32UTF8"

def initialize_logging(level):
    """Initializes the logging system"""
    root = logging.getLogger()
    root.setLevel(level)
    hdlr = logging.StreamHandler()
    fmt = logging.Formatter("%(levelname)s %(name)s: %(message)s")
    hdlr.setFormatter(fmt)
    root.addHandler(hdlr)

def getImporters(river_id, dbconn, dry_run):
    return [
        Axis(river_id, dbconn, dry_run),
        KM(river_id, dbconn, dry_run),
        CrosssectionTrack(river_id, dbconn, dry_run),
        Fixpoint(river_id, dbconn, dry_run),
        Building(river_id, dbconn, dry_run),
        Floodplain(river_id, dbconn, dry_run),
        HydrBoundary(river_id, dbconn, dry_run),
        HydrBoundaryPoly(river_id, dbconn, dry_run),
        HWSLines(river_id, dbconn, dry_run),
        HWSPoints(river_id, dbconn, dry_run),
        Jetties(river_id, dbconn, dry_run),
        Floodmark(river_id, dbconn, dry_run),
        UESG(river_id, dbconn, dry_run)
        ]


def getConfig():
    parser = optparse.OptionParser()
    parser.add_option("--directory", type="string")
    parser.add_option("--target_srs", type="int")
    parser.add_option("--host", type="string")
    parser.add_option("--user", type="string")
    parser.add_option("--password", type="string")
    parser.add_option("--river_name", type="string")
    parser.add_option("--verbose", type="int", default=1)
    parser.add_option("--dry_run", type="int", default=0)
    parser.add_option("--ogr_connection", type="string")
    parser.add_option("--skip_axis", type="int")
    parser.add_option("--skip_hydr_boundaries", type="int")
    parser.add_option("--skip_buildings", type="int")
    parser.add_option("--skip_crosssections", type="int")
    parser.add_option("--skip_fixpoints", type="int")
    parser.add_option("--skip_floodplains", type="int")
    parser.add_option("--skip_hws_lines", type="int")
    parser.add_option("--skip_hws_points", type="int")
    parser.add_option("--skip_kms", type="int")
    parser.add_option("--skip_uesgs", type="int")
    parser.add_option("--skip_dgm", type="int")
    parser.add_option("--skip_jetties", type="int")
    parser.add_option("--skip_floodmarks", type="int")
    (config, args) = parser.parse_args()

    if config.verbose > 1:
        initialize_logging(logging.DEBUG)
    elif config.verbose == 1:
        initialize_logging(logging.INFO)
    else:
        initialize_logging(logging.WARN)

    if config.directory == None:
        logger.error("No river directory specified!")
        raise Exception("Invalid config")
    if not config.ogr_connection:
        if not config.host:
            logger.error("No database host specified!")
            raise Exception("Invalid config")
        if not config.user:
            logger.error("No databaser user specified!")
            raise Exception("Invalid config")
        if not config.password:
            logger.error("No password specified!")
            raise Exception("Invalid config")

    return config


def skip_importer(config, importer):
    if config.skip_axis == 1 and isinstance(importer, Axis):
        return True
    elif config.skip_hydr_boundaries == 1 and isinstance(importer, HydrBoundary):
        return True
    elif config.skip_hydr_boundaries == 1 and isinstance(importer, HydrBoundaryPoly):
        return True
    elif config.skip_buildings == 1 and isinstance(importer, Building):
        return True
    elif config.skip_crosssections == 1 and isinstance(importer, CrosssectionTrack):
        return True
    elif config.skip_fixpoints == 1 and isinstance(importer, Fixpoint):
        return True
    elif config.skip_floodplains == 1 and isinstance(importer, Floodplain):
        return True
    elif config.skip_hws_lines == 1 and isinstance(importer, HWSLines):
        return True
    elif config.skip_hws_points == 1 and isinstance(importer, HWSPoints) and \
            not isinstance(importer, HWSLines):
        return True
    elif config.skip_jetties == 1 and isinstance(importer, Jetties):
        return True
    elif config.skip_kms == 1 and isinstance(importer, KM):
        return True
    elif config.skip_uesgs == 1 and isinstance(importer, UESG):
        return True
    elif config.skip_floodmarks == 1 and isinstance(importer, Floodmark):
        return True

    return False

def main():
    config=None
    try:
        config = getConfig()
    except:
        return -1

    if config == None:
        logger.error("Unable to read config from command line!")
        return

    if config.dry_run > 0:
        logger.info("You enable 'dry_run'. No database transaction will take place!")

    if config.ogr_connection:
        connstr = config.ogr_connection
    else:
        connstr = 'OCI:%s/%s@%s' % (config.user, config.password, config.host)

    oracle = False # Marker if oracle is used.
    if 'OCI:' in connstr:
        oracle = True
        try:
            import cx_Oracle as dbapi
            raw_connstr=connstr.replace("OCI:", "")
        except ImportError:
            logger.error("Module cx_Oracle not found in: %s\n"
                  "Neccessary to connect to a Oracle Database.\n"
                  "Please refer to the installation "
                  "documentation." % sys.path)
            return -1

    else: # Currently only support for oracle and postgres
        try:
            import psycopg2 as dbapi
            raw_connstr=connstr.replace("PG:", "")
        except ImportError:
            logger.error("Module psycopg2 not found in: %s\n"
                  "Neccessary to connect to a Posgresql Database.\n"
                  "Please refer to the installation "
                  "documentation." % sys.path)
            return -1

    dbconn_raw = dbapi.connect(raw_connstr)
    dbconn = ogr.Open(connstr, 1)

    if dbconn == None:
        logger.error("Could not connect to database %s" % connstr)
        return -1

    types = {}

    directories = []
    if not config.river_name:
        for file in [os.path.join(config.directory, d) for d in \
                os.listdir(config.directory)]:
            if os.path.isdir(file):
                directories.append(file)
    else:
        directories.append(config.directory)

    for directory in directories:
        if not config.river_name:
            river_name = utils.getUTF8Path(
                    os.path.basename(os.path.normpath(directory)))
        else:
            river_name = config.river_name.strip()
        river_id = utils.getRiverId(dbconn_raw, river_name, oracle)

        if not river_id:
            logger.info(u"Could not find river in database. Skipping: %s"
                  % unicode(utils.getUTF8(river_name), "UTF-8"))
            continue
        else:
            logger.info(u"Importing River: %s" % unicode(
                       utils.getUTF8(river_name), "UTF-8"))

        for importer in getImporters(river_id, dbconn, config.dry_run):
            if skip_importer(config, importer):
                logger.info("Skip import of '%s'" % importer.getName())
                continue

            logger.info("Start import of '%s'" % importer.getName())

            shapes = utils.findShapefiles(importer.getPath(config.directory))
            logger.debug("Found %i Shapefiles" % len(shapes))

            for shpTuple in shapes:
                geomType = importer.walkOverShapes(shpTuple)
                try:
                    if geomType is not None:
                        num = types[geomType]
                        types[geomType] = num+1
                except:
                    types[geomType] = 1

        for key in types:
            logger.debug("%i x geometry type %s" % (types[key], key))

        if not config.skip_dgm:
            dgmfilename = os.path.join(
                    config.directory, "..", "DGMs.csv")
            if not os.access(dgmfilename, os.R_OK) or not \
                    os.path.isfile(dgmfilename):
                logger.info("Could not find or access DGM file: %s \n"
                     "Skipping DGM import." % dgmfilename)
            else:
                logger.info("Inserting DGM meta information in 'dem' table.")
                insertRiverDgm(dbconn_raw, dgmfilename, river_name,
                        config.dry_run, oracle)
        else:
            logger.info("Skip import of DGM.")

if __name__ == '__main__':
    main()
