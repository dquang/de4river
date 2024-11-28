/* Copyright (C) 2020 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.dive4elements.river.utils.MapUtils;

public class MapUtilsTest {

    private static final String DRV_PG  = "jdbc:postgresql:";
    private static final String DRV_ORA = "jdbc:oracle:thin:";

    private static final String USER = "d4euser";
    private static final String PSWD = "d4epswd";
    private static final String DB   = "d4edb";
    private static final String HOST = "d4ehost";
    private static final String PORT = "2345";

    private static final String PG_CON_SUFFIX = " sslmode=disable";

    @Test
    public void noJDBCURL() {
        String con = MapUtils.getConnection(USER, PSWD, "xx");
        assertNull(con);
    }

    @Test
    public void invalidHostPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + "//invalid_host_name/");
        assertNull(con);
    }

    @Test
    public void localNamedPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + DB);
        assertEquals(
            "dbname=" + DB
            + " user=" + USER
            + " password='" + PSWD + "'" + PG_CON_SUFFIX,
            con);
    }

    @Test
    public void localUserPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + "/");
        assertEquals(
            "dbname=" + USER
            + " user=" + USER
            + " password='" + PSWD + "'" + PG_CON_SUFFIX,
            con);
    }

    @Test
    public void hostNamedPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + "//" + HOST + "/" + DB);
        assertEquals(
            "dbname=" + DB
            + " user=" + USER
            + " host='" + HOST + "'"
            + " password='" + PSWD + "'" + PG_CON_SUFFIX,
            con);
    }

    @Test
    public void hostUserPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + "//" + HOST + "/");
        assertEquals(
            "dbname=" + USER
            + " user=" + USER
            + " host='" + HOST + "'"
            + " password='" + PSWD + "'" + PG_CON_SUFFIX,
            con);
    }

    @Test
    public void hostPortUserPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + "//" + HOST + ":" + PORT + "/");
        assertEquals(
            "dbname=" + USER
            + " user=" + USER
            + " host='" + HOST + "' port=" + PORT
            + " password='" + PSWD + "'" + PG_CON_SUFFIX,
            con);
    }

    @Test
    public void hostPortNamedPG() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_PG + "//" + HOST + ":" + PORT + "/" + DB);
        assertEquals(
            "dbname=" + DB
            + " user=" + USER
            + " host='" + HOST + "' port=" + PORT
            + " password='" + PSWD + "'" + PG_CON_SUFFIX,
            con);
    }

    @Test
    public void serviceNameORA() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_ORA + "@//" + HOST + ":" + PORT + "/" + DB);
        assertEquals(
            USER + "/" + PSWD + "@" + HOST + ":" + PORT + "/" + DB,
            con);
    }

    @Test
    public void connectDescriptorORA() {
        String con = MapUtils.getConnection(
            USER, PSWD, DRV_ORA
            + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST="
            + HOST + ")(PORT=" + PORT + "))(CONNECT_DATA=(SERVICE_NAME="
            + DB + ")))");
        assertEquals(
            USER + "/" + PSWD
            + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST="
            + HOST + ")(PORT=" + PORT + "))(CONNECT_DATA=(SERVICE_NAME="
            + DB + ")))",
            con);
    }
}
