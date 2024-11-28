/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import java.io.FileInputStream;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.jdbc.Work;

import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.backend.SessionFactoryProvider;
import org.dive4elements.river.backend.SessionHolder;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.datacage.templating.Builder;
import org.dive4elements.river.artifacts.datacage.templating.BuilderPool;
import org.dive4elements.river.artifacts.model.OfficialLineFinder;


/**
 * Also accessible as Singleton with getInstance().
 */
public class Recommendations
{
    public static final String CONNECTION_USER   = "user";
    public static final String CONNECTION_SYSTEM = "system";
    public static final String CONNECTION_SEDDB  = "seddb";

    public static final String DEFAULT_CONNECTION_NAME = CONNECTION_SYSTEM;

    private static Logger log = LogManager.getLogger(Recommendations.class);

    private static final boolean DEVELOPMENT_MODE =
        Boolean.getBoolean("flys.datacage.recommendations.development");

    public static final String XPATH_TEMPLATE =
        "/artifact-database/metadata/template/text()";

    public static final String DEFAULT_TEMPLATE_PATH =
        "${artifacts.config.dir}/meta-data.xml";

    private static Recommendations INSTANCE;

    public static class BuilderPoolProvider
    {
        protected BuilderPool builderPool;

        public BuilderPoolProvider() {
        }

        public BuilderPoolProvider(BuilderPool builderPool) {
            this.builderPool = builderPool;
        }

        public BuilderPool getBuilderPool() {
            return builderPool;
        }
    } // class BuilderProvider

    public static class FileBuilderPoolProvider
    extends             BuilderPoolProvider
    {
        protected File file;
        protected long lastModified;

        public FileBuilderPoolProvider() {
        }

        public FileBuilderPoolProvider(File file) {
            this.file    = file;
            lastModified = Long.MIN_VALUE;
        }

        @Override
        public synchronized BuilderPool getBuilderPool() {
            long modified = file.lastModified();
            if (modified > lastModified) {
                lastModified = modified;
                try {
                    Document template = loadTemplate(file);
                    builderPool = new BuilderPool(template);
                }
                catch (IOException ioe) {
                    log.error(ioe);
                }
            }
            return builderPool;
        }

        public BuilderPoolProvider toStaticProvider() {
            return new BuilderPoolProvider(builderPool);
        }
    } // class BuilderProvider

    protected BuilderPoolProvider builderPoolProvider;

    public Recommendations() {
    }

    public Recommendations(BuilderPoolProvider builderPoolProvider) {
        this.builderPoolProvider = builderPoolProvider;
    }

    public BuilderPool getBuilderPool() {
        return builderPoolProvider.getBuilderPool();
    }

    protected static void artifactToParameters(
        D4EArtifact         artifact,
        Map<String, Object> parameters
    ) {
        parameters.put("CURRENT-STATE-ID", artifact.getCurrentStateId());
        parameters.put("ARTIFACT-ID",      artifact.identifier());
        parameters.put("ARTIFACT-NAME",    artifact.getName());

        for (StateData sd: artifact.getAllData()) {
            Object value = sd.getValue();
            if (value == null) {
                continue;
            }
            String key = sd.getName().replace('.', '-').toUpperCase();
            parameters.put(key, value);
        }

        // XXX: THIS IS THE HACK TO BRING THE OFFICIAL LINES INTO THE DATACAGE!
        parameters.put(
            "OFFICIAL-LINES", OfficialLineFinder.findOfficialLines(artifact));
    }

    /**
     * Put Key/Values from \param src to \param dst, but uppercase
     * both Keys and Values.
     */
    public static void convertKeysToUpperCase(
        Map<String, Object> src,
        Map<String, Object> dst
    ) {
        for (Map.Entry<String, Object> entry: src.entrySet()) {
            dst.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }


    /**
     * Append recommendations to \param result.
     * @param extraParameters parameters (typical example: 'recommended')
     */
    public void  recommend(
        D4EArtifact         artifact,
        String              userId,
        String []           outs,
        Map<String, Object> extraParameters,
        Node                result
    ) {
        Map<String, Object> parameters = new HashMap<String, Object>();

        if (extraParameters != null) {
            convertKeysToUpperCase(extraParameters, parameters);
        }

        if (userId != null) {
            parameters.put("USER-ID", userId);
        }

        if (artifact != null) {
            artifactToParameters(artifact, parameters);
        }

        parameters.put("ARTIFACT-OUTS", StringUtils.toUpperCase(outs));

        parameters.put("PARAMETERS", parameters);

        recommend(parameters, userId, result);
    }


    /**
     * Append recommendations to \param result.
     */
    public void recommend(
        Map<String, Object> parameters,
        String              userId,
        Node                result
    ) {
        recommend(parameters, userId, result, SessionHolder.HOLDER.get());
    }

    public void recommend(
        final Map<String, Object> parameters,
        final String              userId,
        final Node                result,
        Session                   systemSession
    ) {
        systemSession.doWork(new Work() {
            @Override
            public void execute(final Connection systemConnection)
            throws SQLException
            {
                SessionFactory sedDBFactory =
                    SessionFactoryProvider.getSedDBSessionFactory();

                Session sedDBSession = sedDBFactory.openSession();
                try {
                    sedDBSession.doWork(new Work() {
                        @Override
                        public void execute(Connection sedDBConnection)
                        throws SQLException
                        {
                            recommend(
                                parameters, userId, result,
                                systemConnection,
                                sedDBConnection);
                        }
                    });
                }
                finally {
                    sedDBSession.close();
                }
            }
        });
    }

    public void recommend(
        Map<String, Object> parameters,
        String              userId,
        Node                result,
        Connection          systemConnection,
        Connection          seddbConnection
    ) throws SQLException
    {
        List<Builder.NamedConnection> connections =
            new ArrayList<Builder.NamedConnection>(3);

        Connection userConnection = userId != null
            || parameters.containsKey("ARTIFACT-ID")
            ? DBConfig
                .getInstance()
                .getDBConnection()
                .getDataSource()
                .getConnection()
            : null;

        try {
            connections.add(new Builder.NamedConnection(
                CONNECTION_SYSTEM, systemConnection, true));

            if (seddbConnection != null) {
                connections.add(new Builder.NamedConnection(
                    CONNECTION_SEDDB, seddbConnection, true));
            }

            if (userConnection != null) {
                connections.add(new Builder.NamedConnection(
                    CONNECTION_USER, userConnection, false));
            }

            getBuilderPool().build(connections, result, parameters);
        }
        finally {
            if (userConnection != null) {
                userConnection.close();
            }
        }
    }

    /** Get singleton instance. */
    public static synchronized Recommendations getInstance() {
        if (INSTANCE == null) {
            INSTANCE = createRecommendations();
        }
        return INSTANCE;
    }


    protected static Document loadTemplate(File file) throws IOException {
        InputStream in = new FileInputStream(file);

        try {
            Document template = XMLUtils.parseDocument(in);
            if (template == null) {
                throw new IOException("cannot load template");
            }
            return template;
        }
        finally {
            in.close();
        }
    }

    public static Recommendations createRecommendations(File file) {
        log.debug("Recommendations.createBuilder");

        if (!file.isFile() || !file.canRead()) {
            log.error("Cannot open template file '" + file + "'");
            return null;
        }

        FileBuilderPoolProvider fbp = new FileBuilderPoolProvider(file);

        if (fbp.getBuilderPool() == null) {
            log.error("failed loading builder");
            return null;
        }

        BuilderPoolProvider bp = DEVELOPMENT_MODE
            ? fbp
            : fbp.toStaticProvider();

        return new Recommendations(bp);
    }

    protected static Recommendations createRecommendations() {
        log.debug("Recommendations.createRecommendations");

        String path = Config.getStringXPath(XPATH_TEMPLATE);

        if (path == null) {
            path = DEFAULT_TEMPLATE_PATH;
        }

        path = Config.replaceConfigDir(path);

        log.info("Meta data template: " + path);

        return createRecommendations(new File(path));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
