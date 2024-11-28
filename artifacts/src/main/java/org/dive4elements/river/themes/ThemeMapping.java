/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dive4elements.river.artifacts.D4EArtifact;

/**
 * Represents mapping to a theme (including conditions).
 */
public class ThemeMapping implements Serializable {

    /** The log that is used in this class */
    private static Logger log = LogManager.getLogger(ThemeMapping.class);

    /** Name from which to map. */
    protected String from;

    /** Name to which to map. */
    protected String to;

    /** Given pattern (held against facet description). */
    protected String patternStr;

    /** Given masterAttr pattern (held against masterartifacts attributes). */
    protected String masterAttr;

    /** Given output for which mapping is valid. */
    protected String output;

    protected Pattern pattern;


    public ThemeMapping(String from, String to) {
        this(from, to, null, null, null);
    }


    public ThemeMapping(
        String from,
        String to,
        String patternStr,
        String masterAttr,
        String output)
   {
        this.from       = from;
        this.to         = to;
        this.patternStr = patternStr;
        this.masterAttr = masterAttr;
        this.output     = output;

        this.pattern = Pattern.compile(patternStr);
    }


    public String getFrom() {
        return from;
    }


    /**
     * Get name of theme that is mapped to.
     */
    public String getTo() {
        return to;
    }


    /**
     * Get pattern.
     */
    public String getPatternStr() {
        return patternStr;
    }


    /**
     * Match regular expression against text.
     *
     * @param text string to be matched against.
     * @return true if pattern matches text or pattern is empty.
     */
    public boolean applyPattern(String text) {
        if (patternStr == null || patternStr.length() == 0) {
            return true;
        }
        Matcher m = pattern.matcher(text);

       if (m.matches()) {
           log.debug("Pattern matches: " + text);
           return true;
       }
       else {
           log.debug(
               "Pattern '"+ text + "' does not match: " + this.patternStr);
           return false;
       }
    }


    /**
     * Inspects Artifacts data given the masterAttr-condition.
     *
     * The only condition implemented so far is 'key==value', for which
     * the Artifacts data with name "key" has to be of value "value" in order
     * for true to be returned.
     *
     * @param artifact Artifact of which to inspect data.
     * @return true if no condition is specified or condition is met.
     */
    public boolean masterAttrMatches(D4EArtifact artifact) {
        if (masterAttr == null || masterAttr.length() == 0) {
           return true;
        }

        // Operator split.
        String[] parts = masterAttr.split("==");
        if (parts.length != 2) {
            log.error("ThemeMapping could not parse masterAttr.-condition:_"
                + masterAttr + "_");
            return false;
        }

        // Test.
        String artData = artifact.getDataAsString(parts[0]);
        if (artData != null && artData.equals(parts[1])) {
            log.debug("Matches master Attribute.");
            return true;
        }
        else {
            log.debug("Does not match master Attribute.");
            return false;
        }
    }


    /**
     * Returns true if no output condition exists, or the condition is met
     * in parameter output.
     */
    public boolean outputMatches(String output) {
        if (this.output == null || this.output.length() == 0) {
            return true;
        }

        if (this.output.equals(output)) {
            log.debug("Output matches this mapping: " + output);
            return true;
        }
        else {
            log.debug("Output '"+ output +"' does not match: "+ this.output);
            return false;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
