/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend;

import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.SessionFactory;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import org.hibernate.impl.SessionFactoryImpl;

public final class SessionFactoryProvider
{
    private static Logger log = LogManager.getLogger(SessionFactoryProvider.class);

    //public static final boolean ENABLE_JMX =
    //    Boolean.getBoolean("flys.backend.enablejmx");

    private static SessionFactory flysSessionFactory;
    private static SessionFactory sedDBSessionFactory;

    private SessionFactoryProvider() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (flysSessionFactory == null) {
            flysSessionFactory =
                createSessionFactory(FLYSCredentials.getInstance());
        }
        return flysSessionFactory;
    }

    public static SessionFactory createSessionFactory() {
        return createSessionFactory(FLYSCredentials.getDefault());
    }

    public static synchronized SessionFactory getSedDBSessionFactory() {
        if (sedDBSessionFactory == null) {
            sedDBSessionFactory =
                createSessionFactory(SedDBCredentials.getInstance());
        }
        return sedDBSessionFactory;
    }

    public static SessionFactory createSedDBSessionFactory() {
        return createSessionFactory(SedDBCredentials.getDefault());
    }

    public static SessionFactory createSessionFactory(
        Credentials credentials
    ) {
        Configuration cfg = createConfiguration(credentials);

        SessionFactory factory = cfg.buildSessionFactory();

        /*
        if (ENABLE_JMX) {
            registerAsMBean(factory);
        }
        else {
            log.info("No JMX support for hibernate.");
        }
        */

        return factory;
    }

    /** XXX: Commented out till it is configured correctly.
    public static void registerAsMBean(SessionFactory factory) {

        //

        StatisticsService statsMBean = new StatisticsService();
        statsMBean.setSessionFactory(factory);
        statsMBean.setStatisticsEnabled(true);

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(
                statsMBean,
                new ObjectName("Hibernate:application=Statistics"));

            log.info("Enabled JMX support for hibernate.");
        }
        catch (MalformedObjectNameException mone) {
            log.warn(mone, mone);
        }
        catch (InstanceAlreadyExistsException iaee) {
            log.warn(iaee, iaee);
        }
        catch (MBeanRegistrationException mbre) {
            log.warn(mbre, mbre);
        }
        catch (NotCompliantMBeanException ncmbe) {
            log.warn(ncmbe, ncmbe);
        }
    }
    */

    public static Configuration createConfiguration() {
        return createConfiguration(FLYSCredentials.getInstance());
    }

    public static Configuration createConfiguration(
        Credentials credentials
    ) {
        Configuration cfg = new Configuration();

        for (Class<?> clazz: credentials.getClasses()) {
            cfg.addAnnotatedClass(clazz);
        }

        if (log.isDebugEnabled()) {
            log.debug("user: "    + credentials.getUser());
            log.debug("dialect: " + credentials.getDialect());
            log.debug("driver: "  + credentials.getDriver());
            log.debug("url: "     + credentials.getUrl());
        }

        Properties props = new Properties();

        // We rely on our own connection pool
        props.setProperty(
            "hibernate.connection.provider_class",
            "org.dive4elements.river.backend.utils.DBCPConnectionProvider");

        props.setProperty(Environment.DIALECT, credentials.getDialect());
        props.setProperty(Environment.USER,    credentials.getUser());
        props.setProperty(Environment.PASS,    credentials.getPassword());
        props.setProperty(Environment.DRIVER,  credentials.getDriver());
        props.setProperty(Environment.URL,     credentials.getUrl());

        String connectionInitSqls = credentials.getConnectionInitSqls();
        if (connectionInitSqls != null) {
            props.setProperty("connectionInitSqls", connectionInitSqls);
        }
        String validationQuery = credentials.getValidationQuery();
        if (validationQuery != null) {
            props.setProperty("validationQuery", validationQuery);
        }
        String maxWait = credentials.getMaxWait();
        if (maxWait != null) {
            props.setProperty("maxWait", maxWait);
        }

        cfg.mergeProperties(props);

        return cfg;
    }


    public static String getProperty(SessionFactoryImpl factory, String key) {
        Properties props = factory.getProperties();
        return props.getProperty(key);
    }

    public static String getUser(SessionFactoryImpl factory) {
        return getProperty(factory, Environment.USER);
    }


    public static String getPass(SessionFactoryImpl factory) {
        return getProperty(factory, Environment.PASS);
    }


    public static String getURL(SessionFactoryImpl factory) {
        return getProperty(factory, Environment.URL);
    }


    public static String getDriver(SessionFactoryImpl factory) {
        return getProperty(factory, Environment.DRIVER);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
