/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage;

import java.util.Collection;
import java.util.List;
import java.util.Date;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.sql.Timestamp;

import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.User;

import org.dive4elements.artifactdatabase.db.SQL;
import org.dive4elements.artifactdatabase.db.SQLExecutor;

import org.dive4elements.artifactdatabase.LifetimeListener;
import org.dive4elements.artifactdatabase.Backend;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.artifacts.common.utils.LRUCache;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

public class Datacage
implements   LifetimeListener
{
    private static Logger log = LogManager.getLogger(Datacage.class);

    public static final String DATACAGE_KEY =
        "global.datacage.instance";

    public static final String ARTEFACT_DATABASE_KEY =
        "global.artifact.database";

    private String SQL_DELETE_ALL_USERS      = "delete.all.users";
    private String SQL_DELETE_ALL_ARTIFACTS  = "delete.all.artifacts";
    private String SQL_USER_ID_NEXTVAL       = "user.id.nextval";
    private String SQL_USER_BY_GID           = "user.by.gid";
    private String SQL_INSERT_USER           = "insert.user";
    private String SQL_COLLECTION_BY_GID     = "collection.by.gid";
    private String SQL_COLLECTION_ID_NEXTVAL = "collection.id.nextval";
    private String SQL_INSERT_COLLECTION     = "insert.collection";
    private String SQL_ARTIFACT_BY_GID       = "artifact.by.gid";
    private String SQL_COLLECTION_ITEM_ID_NEXTVAL =
        "collection.item.id.nextval";
    private String SQL_INSERT_COLLECTION_ITEM = "insert.collection.item";
    private String SQL_ARTIFACT_ID_NEXTVAL    = "artifact.id.nextval";
    private String SQL_INSERT_ARTIFACT        = "insert.artifact";
    private String SQL_ARTIFACT_DATA_ID_NEXTVAL = "artifact.data.id.nextval";
    private String SQL_UPDATE_ARTIFACT_STATE  = "update.artifact.state";
    private String SQL_INSERT_ARTIFACT_DATA   = "insert.artifact.data";
    private String SQL_OUT_ID_NEXTVALUE       = "out.id.nextval";
    private String SQL_INSERT_OUT             = "insert.out";
    private String SQL_FACET_ID_NEXTVAL       = "facet.id.nextval";
    private String SQL_INSERT_FACET           = "insert.facet";
    private String SQL_UPDATE_COLLECTION_NAME = "update.collection.name";
    private String SQL_DELETE_ARTIFACT_FROM_COLLECTION =
        "delete.artifact.from.collection";
    private String SQL_DELETE_COLLECTION_BY_GID =
        "delete.collection.by.gid";
    private String SQL_DELETE_USER_BY_GID = "delete.user.by.gid";
    private String SQL_DELETE_ARTIFACT_DATA_BY_ARTIFACT_ID =
        "delete.artifact.data.by.artifact.id";
    private String SQL_DELETE_OUTS_BY_ARTIFACT_ID =
        "delete.outs.by.artifact.id";
    private String SQL_DELETE_FACETS_BY_ARTIFACT_ID =
        "delete.facets.by.artifact.id";
    private String SQL_DELETE_ARTIFACT_BY_GID =
        "delete.artifact.by.gid";

    protected SQLExecutor sqlExecutor;

    public class InitialScan
    implements   ArtifactDatabase.ArtifactLoadedCallback
    {
        protected LRUCache<String, Integer> users;
        protected LRUCache<String, Integer> collections;
        protected LRUCache<String, Integer> artifacts;

        protected GlobalContext context;

        public InitialScan() {
            users       = new LRUCache<String, Integer>();
            collections = new LRUCache<String, Integer>();
            artifacts   = new LRUCache<String, Integer>();
        }

        public InitialScan(GlobalContext context) {
            this();
            this.context = context;
        }

        @Override
        public void artifactLoaded(
            String   userId,
            String   collectionId,
            String   collectionName,
            Date     collectionCreated,
            String   artifactId,
            Date     artifactCreated,
            Artifact artifact
        ) {
            if (!(artifact instanceof D4EArtifact)) {
                log.warn("ignoring none FLYS artifacts");
                return;
            }

            D4EArtifact flysArtifact = (D4EArtifact)artifact;

            Integer uId = getUserId(userId);
            Integer cId = getCollectionId(
                collectionId, uId, collectionName, collectionCreated);

            storeArtifact(artifactId, cId, flysArtifact, artifactCreated);
        }

        protected Integer getId(
            LRUCache<String, Integer> cache,
            final String              idString,
            final String              selectById
        ) {
            Integer id = cache.get(idString);
            if (id != null) {
                return id;
            }

            final Integer [] res = new Integer[1];

            SQLExecutor.Instance exec = sqlExecutor.new Instance() {
                @Override
                public boolean doIt() throws SQLException {
                    prepareStatement(selectById);
                    stmnt.setString(1, idString);
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        return false;
                    }
                    res[0] = result.getInt(1);
                    return true;
                }
            };

            if (exec.runRead()) {
                cache.put(idString, res[0]);
                return res[0];
            }

            return null;
        }

        protected void storeArtifact(
            final String       artifactId,
            Integer            collectionId,
            final D4EArtifact artifact,
            final Date         artifactCreated
        ) {
            Integer aId = getId(artifacts, artifactId, SQL_ARTIFACT_BY_GID);

            if (aId != null) {
                // We've already stored it. Just create the collection item.
                storeCollectionItem(collectionId, aId);
                return;
            }
            // We need to write it to database

            final Integer [] res = new Integer[1];

            SQLExecutor.Instance exec = sqlExecutor.new Instance() {
                @Override
                public boolean doIt() throws SQLException {
                    prepareStatement(SQL_ARTIFACT_ID_NEXTVAL);
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        return false;
                    }
                    res[0] = result.getInt(1);
                    reset();
                    prepareStatement(SQL_INSERT_ARTIFACT);
                    stmnt.setInt   (1, res[0]);
                    stmnt.setString(2, artifactId);
                    stmnt.setString(3, artifact.getCurrentStateId());
                    Timestamp timestamp = new Timestamp(artifactCreated != null
                        ? artifactCreated.getTime()
                        : System.currentTimeMillis());
                    stmnt.setTimestamp(4, timestamp);
                    stmnt.execute();
                    conn.commit();
                    return true;
                }
            };

            if (!exec.runWrite()) {
                log.error("storing of artifact failed.");
                return;
            }

            artifacts.put(artifactId, aId = res[0]);

            storeCollectionItem(collectionId, aId);

            storeData(aId, artifact);

            storeOuts(aId, artifact, context);
        }


        protected void storeCollectionItem(
            final Integer collectionId,
            final Integer artifactId
        ) {
            SQLExecutor.Instance exec = sqlExecutor.new Instance() {
                @Override
                public boolean doIt() throws SQLException {
                    prepareStatement(SQL_COLLECTION_ITEM_ID_NEXTVAL);
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        return false;
                    }
                    int ciId = result.getInt(1);
                    reset();
                    prepareStatement(SQL_INSERT_COLLECTION_ITEM);
                    stmnt.setInt(1, ciId);
                    stmnt.setInt(2, collectionId);
                    stmnt.setInt(3, artifactId);
                    stmnt.execute();
                    conn.commit();
                    return true;
                }
            };

            if (!exec.runWrite()) {
                log.error("storing of collection item failed.");
            }
        }

        protected Integer getCollectionId(
            final String  collectionId,
            final Integer ownerId,
            final String  collectionName,
            final Date    collectionCreated
        ) {
            Integer c = getId(
                collections, collectionId, SQL_COLLECTION_BY_GID);

            if (c != null) {
                return c;
            }

            final Integer [] res = new Integer[1];

            SQLExecutor.Instance exec = sqlExecutor.new Instance() {
                @Override
                public boolean doIt() throws SQLException {
                    prepareStatement(SQL_COLLECTION_ID_NEXTVAL);
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        return false;
                    }
                    res[0] = result.getInt(1);
                    reset();
                    prepareStatement(SQL_INSERT_COLLECTION);
                    stmnt.setInt   (1, res[0]);
                    stmnt.setString(2, collectionId);
                    stmnt.setInt   (3, ownerId);
                    setString(stmnt, 4, collectionName);
                    Timestamp timestamp =
                        new Timestamp(collectionCreated != null
                            ? collectionCreated.getTime()
                            : System.currentTimeMillis());
                    stmnt.setTimestamp(5, timestamp);
                    stmnt.execute();
                    conn.commit();
                    return true;
                }
            };

            if (exec.runWrite()) {
                collections.put(collectionId, res[0]);
                return res[0];
            }

            return null;
        }

        protected Integer getUserId(final String userId) {

            Integer u = getId(users, userId, SQL_USER_BY_GID);

            if (u != null) {
                return u;
            }

            final Integer [] res = new Integer[1];

            SQLExecutor.Instance exec = sqlExecutor.new Instance() {
                @Override
                public boolean doIt() throws SQLException {
                    prepareStatement(SQL_USER_ID_NEXTVAL);
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        return false;
                    }
                    res[0] = result.getInt(1);
                    reset();
                    prepareStatement(SQL_INSERT_USER);
                    stmnt.setInt   (1, res[0]);
                    stmnt.setString(2, userId);
                    stmnt.execute();
                    conn.commit();
                    return true;
                }
            };

            if (exec.runWrite()) {
                users.put(userId, res[0]);
                return res[0];
            }

            return null;
        }

        public boolean scan(ArtifactDatabase adb) {
            log.debug("scan");
            try {
                adb.loadAllArtifacts(this);
            }
            catch (ArtifactDatabaseException ade) {
                log.error(ade);
                return false;
            }
            return true;
        }
    } // class InitialScan


    public Datacage() {
    }

    @Override
    public void setup(Document document) {
        log.debug("setup");
        DBConfig config = DBConfig.getInstance();
        setupSQL(config.getSQL());
        sqlExecutor = new SQLExecutor(config.getDBConnection());
    }

    protected void setupSQL(SQL sql) {
        SQL_DELETE_ALL_USERS      = sql.get(SQL_DELETE_ALL_USERS);
        SQL_DELETE_ALL_ARTIFACTS  = sql.get(SQL_DELETE_ALL_ARTIFACTS);
        SQL_USER_ID_NEXTVAL       = sql.get(SQL_USER_ID_NEXTVAL);
        SQL_USER_BY_GID           = sql.get(SQL_USER_BY_GID);
        SQL_INSERT_USER           = sql.get(SQL_INSERT_USER);
        SQL_COLLECTION_BY_GID     = sql.get(SQL_COLLECTION_BY_GID);
        SQL_COLLECTION_ID_NEXTVAL = sql.get(SQL_COLLECTION_ID_NEXTVAL);
        SQL_INSERT_COLLECTION     = sql.get(SQL_INSERT_COLLECTION);
        SQL_ARTIFACT_BY_GID       = sql.get(SQL_ARTIFACT_BY_GID);
        SQL_COLLECTION_ITEM_ID_NEXTVAL =
            sql.get(SQL_COLLECTION_ITEM_ID_NEXTVAL);
        SQL_INSERT_COLLECTION_ITEM =
            sql.get(SQL_INSERT_COLLECTION_ITEM);
        SQL_ARTIFACT_ID_NEXTVAL = sql.get(SQL_ARTIFACT_ID_NEXTVAL);
        SQL_INSERT_ARTIFACT     = sql.get(SQL_INSERT_ARTIFACT);
        SQL_ARTIFACT_DATA_ID_NEXTVAL = sql.get(SQL_ARTIFACT_DATA_ID_NEXTVAL);
        SQL_INSERT_ARTIFACT_DATA = sql.get(SQL_INSERT_ARTIFACT_DATA);
        SQL_UPDATE_ARTIFACT_STATE = sql.get(SQL_UPDATE_ARTIFACT_STATE);
        SQL_OUT_ID_NEXTVALUE     = sql.get(SQL_OUT_ID_NEXTVALUE);
        SQL_INSERT_OUT           = sql.get(SQL_INSERT_OUT);
        SQL_FACET_ID_NEXTVAL     = sql.get(SQL_FACET_ID_NEXTVAL);
        SQL_INSERT_FACET         = sql.get(SQL_INSERT_FACET);
        SQL_UPDATE_COLLECTION_NAME = sql.get(SQL_UPDATE_COLLECTION_NAME);
        SQL_DELETE_ARTIFACT_FROM_COLLECTION =
            sql.get(SQL_DELETE_ARTIFACT_FROM_COLLECTION);
        SQL_DELETE_COLLECTION_BY_GID = sql.get(SQL_DELETE_COLLECTION_BY_GID);
        SQL_DELETE_USER_BY_GID       = sql.get(SQL_DELETE_USER_BY_GID);
        SQL_DELETE_ARTIFACT_DATA_BY_ARTIFACT_ID =
            sql.get(SQL_DELETE_ARTIFACT_DATA_BY_ARTIFACT_ID);
        SQL_DELETE_OUTS_BY_ARTIFACT_ID =
            sql.get(SQL_DELETE_OUTS_BY_ARTIFACT_ID);
        SQL_DELETE_FACETS_BY_ARTIFACT_ID =
            sql.get(SQL_DELETE_FACETS_BY_ARTIFACT_ID);
        SQL_DELETE_ARTIFACT_BY_GID =
            sql.get(SQL_DELETE_ARTIFACT_BY_GID);
    }

    /** Sum over facets in outs. */
    protected static final int numFacets(List<Output> outs) {
        int sum = 0;
        for (Output out: outs) {
            sum += out.getFacets().size();
        }
        return sum;
    }

    protected static final void setString(
        PreparedStatement stmnt,
        int               index,
        Object            value
    )
    throws SQLException
    {
        if (value == null) {
            stmnt.setNull(index, Types.VARCHAR);
        }
        else {
            stmnt.setString(index, value.toString());
        }
    }

    @Override
    public void systemUp(GlobalContext context) {
        log.debug("systemUp entered");
        initialScan(context);
        context.put(DATACAGE_KEY, this);
        log.debug("systemUp leaved");
    }

    protected void initialScan(GlobalContext context) {
        log.debug("initialScan");

        Object adbObject = context.get(ARTEFACT_DATABASE_KEY);

        if (!(adbObject instanceof ArtifactDatabase)) {
            log.error("missing artefact database. Cannot scan");
            return;
        }

        ArtifactDatabase adb = (ArtifactDatabase)adbObject;

        if (!cleanDatabase()) {
            log.error("cleaning database failed");
            return;
        }

        InitialScan is = new InitialScan(context);

        if (!is.scan(adb)) {
            log.error("initial scan failed");
            return;
        }

    }

    protected boolean cleanDatabase() {
        log.debug("cleanDatabase");

        boolean success = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_DELETE_ALL_USERS);
                stmnt.execute();
                prepareStatement(SQL_DELETE_ALL_ARTIFACTS);
                stmnt.execute();
                conn.commit();
                return true;
            }
        }.runWrite();

        log.debug("after runWrite(): " + success);

        return success;
    }


    @Override
    public void systemDown(GlobalContext context) {
        log.debug("systemDown");
    }

    public void setup(GlobalContext globalContext) {
        log.debug("setup");
    }

    public void createdArtifact(
        Artifact      artifact,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("createdArtifact");

        if (artifact == null) {
            log.warn("artifact to create is null");
            return;
        }

        if (!(artifact instanceof D4EArtifact)) {
            log.warn("need D4EArtifact here (have "
                + artifact.getClass() + ")");
            return;
        }

        final D4EArtifact flys = (D4EArtifact)artifact;

        final int [] res = new int[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_ARTIFACT_ID_NEXTVAL);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    log.error("id generation for artifact failed");
                    return false;
                }
                res[0] = result.getInt(1);
                reset();
                prepareStatement(SQL_INSERT_ARTIFACT);
                stmnt.setInt      (1, res[0]);
                stmnt.setString   (2, flys.identifier());
                stmnt.setString   (3, flys.getCurrentStateId());
                stmnt.setTimestamp(4,
                    new Timestamp(System.currentTimeMillis()));
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("storing of artifact failed.");
            return;
        }

        storeData(res[0], flys);
        storeOuts(res[0], flys, context);
    }

    public void storedArtifact(
        Artifact      artifact,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("storedArtifact");
        if (!(artifact instanceof D4EArtifact)) {
            log.warn("need D4EArtifact here but have a "
                + artifact.getClass());
            return;
        }

        final D4EArtifact flys = (D4EArtifact)artifact;

        final Integer [] res = new Integer[1];

        // check first if artifact already exists
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_ARTIFACT_BY_GID);
                stmnt.setString(1, flys.identifier());
                result = stmnt.executeQuery();
                if (!result.next()) {
                    // new artifact
                    return true;
                }
                res[0] = result.getInt(1);
                return true;
            }
        };

        if (!exec.runRead()) {
            log.error("querying artifact failed");
            return;
        }

        if (res[0] == null) { // new artifact
            createdArtifact(artifact, backend, context);
            return;
        }

        // artifact already exists -> delete old data
        exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_DELETE_ARTIFACT_DATA_BY_ARTIFACT_ID);
                stmnt.setInt(1, res[0]);
                stmnt.execute();
                prepareStatement(SQL_DELETE_FACETS_BY_ARTIFACT_ID);
                stmnt.setInt(1, res[0]);
                stmnt.execute();
                prepareStatement(SQL_DELETE_OUTS_BY_ARTIFACT_ID);
                stmnt.setInt(1, res[0]);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("deleting old artifact data failed");
            return;
        }

        // write new data
        storeData(res[0], flys);
        storeOuts(res[0], flys, context);
        storeState(res[0], flys);
    }

    public void createdUser(
        final User    user,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("createdUser");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_USER_ID_NEXTVAL);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    log.error("id generation for user failed");
                    return false;
                }
                int uId = result.getInt(1);
                reset();
                prepareStatement(SQL_INSERT_USER);
                stmnt.setInt(1, uId);
                stmnt.setString(2, user.identifier());
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("create user failed");
        }
    }

    public void deletedUser(
        final String  identifier,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("deletedUser");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_DELETE_USER_BY_GID);
                stmnt.setString(1, identifier);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("delete user failed");
        }
    }

    public void createdCollection(
        final ArtifactCollection collection,
        Backend                  backend,
        GlobalContext            context
    ) {
        log.debug("createdCollection");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                String userId = collection.getUser().identifier();
                prepareStatement(SQL_USER_BY_GID);
                stmnt.setString(1, userId);
                result = stmnt.executeQuery();
                int uId;
                if (result.next()) {
                    uId = result.getInt(1);
                    reset();
                }
                else {
                    // need to create user first
                    reset();
                    prepareStatement(SQL_USER_ID_NEXTVAL);
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        log.error("id generation for user failed");
                        return false;
                    }
                    uId = result.getInt(1);
                    reset();
                    prepareStatement(SQL_INSERT_USER);
                    stmnt.setInt(1, uId);
                    stmnt.setString(2, userId);
                    stmnt.execute();
                    conn.commit();
                    reset();
                }

                prepareStatement(SQL_COLLECTION_ID_NEXTVAL);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    log.error("id generation for collection failed");
                    return false;
                }
                int cId = result.getInt(1);
                reset();

                String identifier = collection.identifier();
                String name       = collection.getName();

                prepareStatement(SQL_INSERT_COLLECTION);
                stmnt.setInt(1, cId);
                stmnt.setString(2, identifier);
                stmnt.setInt(3, uId);
                setString(stmnt, 4, name);
                stmnt.setTimestamp(5,
                    new Timestamp(System.currentTimeMillis()));
                stmnt.execute();

                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("create collection failed");
        }
    }

    public void deletedCollection(
        final String  identifier,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("deletedCollection");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_DELETE_COLLECTION_BY_GID);
                stmnt.setString(1, identifier);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("delete collection failed");
        }
    }

    public void changedCollectionAttribute(
        String        identifier,
        Document      document,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("changedCollectionAttribute");
    }

    public void changedCollectionItemAttribute(
        String        collectionId,
        String        artifactId,
        Document      document,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("changedCollectionItemAttribute");
    }

    public void addedArtifactToCollection(
        final String  artifactId,
        final String  collectionId,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("addedArtifactToCollection");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_ARTIFACT_BY_GID);
                stmnt.setString(1, artifactId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    return false;
                }
                int aId = result.getInt(1);
                reset();

                prepareStatement(SQL_COLLECTION_BY_GID);
                stmnt.setString(1, collectionId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    return false;
                }
                int cId = result.getInt(1);
                reset();

                prepareStatement(SQL_COLLECTION_ITEM_ID_NEXTVAL);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    return false;
                }
                int ciId = result.getInt(1);
                reset();

                prepareStatement(SQL_INSERT_COLLECTION_ITEM);
                stmnt.setInt(1, ciId);
                stmnt.setInt(2, cId);
                stmnt.setInt(3, aId);
                stmnt.execute();

                conn.commit();
                return true;
            }
        };
        if (!exec.runWrite()) {
            log.error("added artifact to collection failed");
        }
    }

    public void removedArtifactFromCollection(
        final String  artifactId,
        final String  collectionId,
        Backend       backend,
        GlobalContext context
    ) {
        log.debug("removedArtifactFromCollection");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_ARTIFACT_BY_GID);
                stmnt.setString(1, artifactId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    return false;
                }
                int aId = result.getInt(1);
                reset();
                prepareStatement(SQL_COLLECTION_BY_GID);
                stmnt.setString(1, collectionId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    return false;
                }
                int cId = result.getInt(1);
                reset();
                prepareStatement(SQL_DELETE_ARTIFACT_FROM_COLLECTION);
                stmnt.setInt(1, cId);
                stmnt.setInt(2, aId);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };
        if (!exec.runWrite()) {
            log.error("removing artifact from collection failed");
        }
    }

    public void setCollectionName(
        final String  collectionId,
        final String  name,
        GlobalContext context
    ) {
        log.debug("setCollectionName");
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_UPDATE_COLLECTION_NAME);
                stmnt.setString(1, name);
                stmnt.setString(2, collectionId);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };
        if (!exec.runWrite()) {
            log.error("changing name failed");
        }
    }

    /** Update state of artifact. */
    protected void storeState(
        final int         artifactId,
        final D4EArtifact artifact) {
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_UPDATE_ARTIFACT_STATE);
                stmnt.setString(1, artifact.getCurrentStateId());
                stmnt.setInt(2, artifactId);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("storing state of artifact failed ("
                + artifactId + "," + artifact.getCurrentStateId() + ")");
        }
    }

    protected void storeData(
        final int   artifactId,
        D4EArtifact artifact
    ) {
        final Collection<StateData> data = artifact.getAllData();

        if (data.isEmpty()) {
            return;
        }

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                int [] ids = new int[data.size()];
                prepareStatement(SQL_ARTIFACT_DATA_ID_NEXTVAL);

                for (int i = 0; i < ids.length; ++i) {
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        log.error("generating id for artifact data failed");
                        return false;
                    }
                    ids[i] = result.getInt(1);
                    result.close(); result = null;
                }
                reset();
                prepareStatement(SQL_INSERT_ARTIFACT_DATA);

                int i = 0;
                for (StateData sd: data) {
                    int id = ids[i++];
                    stmnt.setInt(1, id);
                    stmnt.setInt(2, artifactId);
                    // XXX: Where come the nulls from?
                    String type = sd.getType();
                    if (type == null) type = "String";
                    stmnt.setString(3, type);
                    stmnt.setString(4, sd.getName());
                    setString(stmnt, 5, sd.getValue());
                    stmnt.execute();
                }

                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("storing artifact data failed");
        }
    }

    protected void storeOuts(
        final int          artifactId,
        final D4EArtifact artifact,
        GlobalContext      context
    ) {
        final List<Output> outs = artifact.getOutputs(context);

        if (outs.isEmpty()) {
            return;
        }

        final int [] outIds = new int[outs.size()];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_OUT_ID_NEXTVALUE);
                for (int i = 0; i < outIds.length; ++i) {
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        log.error("generation of out ids failed");
                        return false;
                    }
                    outIds[i] = result.getInt(1);
                    result.close(); result = null;
                }
                reset();
                prepareStatement(SQL_INSERT_OUT);
                for (int i = 0; i < outIds.length; ++i) {
                    Output out = outs.get(i);
                    stmnt.setInt(1, outIds[i]);
                    stmnt.setInt(2, artifactId);
                    stmnt.setString(3, out.getName());
                    setString(stmnt, 4, out.getDescription());
                    setString(stmnt, 5, out.getType());
                    stmnt.execute();
                }
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("storing artifact outs failed");
            return;
        }

        final int FACETS = numFacets(outs);

        if (FACETS == 0) {
            return;
        }

        exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                int [] facetIds = new int[FACETS];
                prepareStatement(SQL_FACET_ID_NEXTVAL);
                for (int i = 0; i < facetIds.length; ++i) {
                    result = stmnt.executeQuery();
                    if (!result.next()) {
                        log.error("generation of facet ids failed");
                        return false;
                    }
                    facetIds[i] = result.getInt(1);
                    result.close(); result = null;
                }
                reset();
                prepareStatement(SQL_INSERT_FACET);
                int index = 0;
                for (int i = 0, N = outs.size(); i < N; ++i) {
                    Output out = outs.get(i);
                    int outId = outIds[i];
                    for (Facet facet: out.getFacets()) {
                        stmnt.setInt(1, facetIds[index]);
                        stmnt.setInt(2, outId);
                        stmnt.setString(3, facet.getName());
                        stmnt.setInt(4, facet.getIndex());
                        stmnt.setString(5, "XXX"); // TODO: handle states
                        setString(stmnt, 6, facet.getDescription());
                        stmnt.execute();
                        ++index;
                    }
                }
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("storing facets failed");
        }
    }

    public void killedCollections(
        final List<String> identifiers,
        GlobalContext      context
    ) {
        log.debug("killedCollections");

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_DELETE_COLLECTION_BY_GID);
                for (String identifier: identifiers) {
                    stmnt.setString(1, identifier);
                    stmnt.execute();
                }
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("killing collections failed");
        }
    }

    public void killedArtifacts(
        final List<String> identifiers,
        GlobalContext      context
    ) {
        log.debug("killedArtifacts");

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_DELETE_ARTIFACT_BY_GID);
                for (String identifier: identifiers) {
                    stmnt.setString(1, identifier);
                    stmnt.execute();
                }
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            log.error("killing artifacts failed");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
