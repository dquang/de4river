/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallMeta;

/**
 * This class provides methods for i18n.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Resources {

    /** The log that is used in this class.*/
    private static Logger log = LogManager.getLogger(Resources.class);

    /** The singleton instance.*/
    private static Resources INSTANCE;

    /** The locales supported by this server.*/
    protected Locale[] locales;

    /**
     * No instance of this class is necessary.
     */
    private Resources() {
    }


    /**
     * Returns the locales supported by this server.
     *
     * @return the supported locales.
     */
    public synchronized Locale [] getLocales() {
        if (locales == null) {
            readLocales();
        }

        return locales;
    }


    /**
     * Read the locales configured for this server.
     */
    protected void readLocales() {
        // TODO IMPLEMENT ME

        locales = new Locale[2];
        locales[0] = Locale.GERMANY;
        locales[1] = Locale.ENGLISH;
    }


    private static synchronized void ensureInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Resources();
        }
    }


    public static Locale getLocale(CallMeta meta) {
        ensureInstance();

        Locale[] locales = INSTANCE.getLocales();
        return meta.getPreferredLocale(locales);
    }

    public static String getMsg(CallMeta meta, String key) {
        return getMsg(meta, key, key);
    }

    /**
     * This method returns the translated value for <i>key</i> or <i>def</i> if
     * <i>key</i> is not existing in the resource bundle.
     *
     * @param meta The CallMeta object of the request that contains the
     * preferred locale.
     * @param key The key that should be translated.
     * @param def A default value that is returned, if <i>key</i> was not found.
     *
     * @return the translated message.
     */
    public static String getMsg(CallMeta meta, String key, String def) {
        ensureInstance();

        Locale[] locales = INSTANCE.getLocales();
        Locale   locale  = meta.getPreferredLocale(locales);

        return getMsg(locale, key, def);
    }

    public static String getMsg(
            CallMeta meta,
            String   key,
            Object[] args
            ) {
        return getMsg(meta, key, key, args);
    }

    /**
     * Returns a translated message based on a template specified by <i>key</i>
     * that has necessary values to be filled in.
     *
     * @param meta The CallMeta object.
     * @param key The key of the template in the resource bundle.
     * @param def the default value if no template was found with <i>key</i>.
     * @param args The arguments that are necessary for the template.
     *
     * @return a translated string.
     */
    public static String getMsg(
            CallMeta meta,
            String   key,
            String   def,
            Object[] args)
    {
        String template = getMsg(meta, key, (String)null);

        if (template == null) {
            return def;
        }

        return format(meta, template, args);
    }

    public static String format(
            CallMeta   meta,
            String     key,
            String     def,
            Object ... args
            ) {
        String template = getMsg(meta, key, (String)null);

        if (template == null) {
            template = def;
        }

        return format(meta, template, args);
    }

    /**
     * Formats the given template using the arguments with respect of the
     * appropriate locale given by the CallMeta instance.
     */
    public static String format(CallMeta meta, String templ, Object ... args) {
        Locale locale = getLocale(meta);
        MessageFormat mf = new MessageFormat(templ, locale);

        return mf.format(args, new StringBuffer(), null).toString();
    }

    /**
     * This method returns the translated value for <i>key</i> or <i>def</i> if
     * <i>key</i> is not existing in the resource bundle.
     *
     * @param locale The locale.
     * @param key The key that should be translated.
     * @param def A default value that is returned, if <i>key</i> was not found.
     *
     * @return the translated message.
     */
    public static String getMsg(Locale locale, String key, String def) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);

        try {
            return bundle.getString(key);
        }
        catch (MissingResourceException mre) {
            log.warn("No message found for key: " + key);

            return def;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
