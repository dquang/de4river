/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.state.DefaultSection;
import org.dive4elements.artifactdatabase.state.DefaultSettings;
import org.dive4elements.artifactdatabase.state.Section;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartSettings extends DefaultSettings {

    private static final Logger log = LogManager.getLogger(ChartSettings.class);

    protected ChartSection  chartSection;
    protected LegendSection legendSection;
    protected ExportSection exportSection;
    protected Section       axesSection;


    public ChartSettings() {
        super();

        axesSection = new DefaultSection("axes");
        addSection(axesSection);
    }


    /**
     * Sets the chart section. Old chart sections are removed.
     *
     * @param chartSection A new Section that stores chart specific attributes.
     */
    public void setChartSection(ChartSection chartSection) {
        ChartSection oldChartSection = getChartSection();

        if (oldChartSection != null) {
            removeSection(oldChartSection);
        }

        this.chartSection = chartSection;
        addSection(chartSection);
    }


    /**
     * Returns the Section that stores chart specific attributes.
     *
     * @return the Section that stores chart specific attributes.
     */
    public ChartSection getChartSection() {
        return chartSection;
    }


    /**
     * Sets the legend section. Old legend sections are removed.
     *
     * @param legendSection A new Section that stores legend specific
     * attributes.
     */
    public void setLegendSection(LegendSection legendSection) {
        LegendSection oldLegendSection = getLegendSection();

        if (oldLegendSection != null) {
            removeSection(oldLegendSection);
        }

        this.legendSection = legendSection;
        addSection(legendSection);
    }


    /**
     * Returns the Section that stores legend specific attributes.
     *
     * @return the Section that stores legend specific attributes.
     */
    public LegendSection getLegendSection() {
        return legendSection;
    }


    /**
     * Sets the export section. Old export sections are removed.
     *
     * @param exportSection A new Section that stores export specific
     * attributes.
     */
    public void setExportSection(ExportSection exportSection) {
        ExportSection oldExportSection = getExportSection();

        if (oldExportSection != null) {
            removeSection(oldExportSection);
        }

        this.exportSection = exportSection;
        addSection(exportSection);
    }


    /**
     * Returns the Section that stores export specific attributes.
     *
     * @return the Section that stores export specific attributes.
     */
    public ExportSection getExportSection() {
        return exportSection;
    }


    /**
     * Adds a Section for a new axis of the chart.
     *
     * @param axisSection The Section specific for a chart axis.
     */
    public void addAxisSection(AxisSection axisSection) {
        if (axisSection != null) {
            axesSection.addSubsection(axisSection);
        }
    }


    /**
     * This method returns an AxisSection specified by <i>axisId</i> or null if
     * no AxisSection is existing with identifier <i>axisId</i>.
     *
     * @param axisId The identifier of the wanted AxisSection.
     *
     * @return the AxisSection specified by <i>axisId</i> or null.
     */
    public AxisSection getAxisSection(String axisId) {
        for (int i = 0, n = axesSection.getSubsectionCount(); i < n; i++) {
            AxisSection as = (AxisSection) axesSection.getSubsection(i);
            String      id = as.getIdentifier();

            if (id != null && id.equals(axisId)) {
                return as;
            }
        }

        return null;
    }


    /**
     * Parses the settings from <i>settings</i>. The result is a new
     * ChartSettings instance.
     *
     * @param settings A <i>settings</i> node.
     *
     * @return a new <i>ChartSettings</i> instance.
     */
    public static ChartSettings parse(Node settings) {
        if (settings == null) {
            log.warn("Tried to parse ChartSettings from empty Node!");
            return null;
        }

        ChartSettings chartSettings = new ChartSettings();

        parseAxes(chartSettings, settings);
        parseChart(chartSettings, settings);
        parseLegend(chartSettings, settings);
        parseExport(chartSettings, settings);

        return chartSettings;
    }


    protected static void parseAxes(ChartSettings target, Node settings) {
        NodeList axesList = (NodeList) XMLUtils.xpath(
            settings, "axes/axis", XPathConstants.NODESET, null);

        int num = axesList != null ? axesList.getLength() : 0;

        if (num <= 0) {
            log.debug("No axis sections found.");
            return;
        }

        for (int i = 0; i < num; i++) {
            parseAxis(target, axesList.item(i));
        }
    }


    protected static void parseAxis(ChartSettings target, Node axis) {
        AxisSection section = new AxisSection();

        String id       = XMLUtils.xpathString(axis, "id", null);
        String label    = XMLUtils.xpathString(axis, "label", null);
        String fSize    = XMLUtils.xpathString(axis, "font-size", null);
        String fixation = XMLUtils.xpathString(axis, "fixation", null);
        String low      = XMLUtils.xpathString(axis, "lower", null);
        String up       = XMLUtils.xpathString(axis, "upper", null);
        String sugLabel = XMLUtils.xpathString(axis, "suggested-label", null);
        String lowTime  = XMLUtils.xpathString(axis, "lower-time", null);
        String upTime   = XMLUtils.xpathString(axis, "upper-time", null);

        if (log.isDebugEnabled()) {
            log.debug("Found axis id:        '" + id + "'");
            log.debug("Found axis label:     '" + label + "'");
            log.debug("Found axis font size: '" + fSize + "'");
            log.debug("Found axis fixation:  '" + fixation + "'");
            log.debug("Found axis lower:     '" + low + "'");
            log.debug("Found axis upper:     '" + up + "'");
            log.debug("Found axis lower-time:'" + lowTime + "'");
            log.debug("Found axis upper-time:'" + upTime + "'");
            log.debug("Found axis sug. label:'" + sugLabel + "'");
        }

        section.setIdentifier(id);
        section.setLabel(label);
        section.setFontSize(
            Integer.parseInt(fSize.length() > 0 ? fSize : "-1"));
        section.setFixed(Boolean.valueOf(fixation));
        if (upTime != null
            && !upTime.isEmpty()
            && lowTime != null
            && !lowTime.isEmpty()
        ) {
            section.setLowerTimeRange(Long.parseLong(lowTime));
            section.setUpperTimeRange(Long.parseLong(upTime));
        } else {
            section.setLowerRange(
                Double.parseDouble(low.length() > 0 ? low : "0"));
            section.setUpperRange(
                Double.parseDouble(up.length() > 0 ? up : "0"));
        }
        section.setSuggestedLabel(sugLabel);

        target.addAxisSection(section);
    }


    /**
     * From document chart create ChartSection and populate it with attributes.
     * Give this object to target as chartsection.
     */
    protected static void parseChart(ChartSettings target, Node chart) {
        ChartSection chartSection = new ChartSection();

        String title = XMLUtils.xpathString(chart, "chart/title", null);
        String sub   = XMLUtils.xpathString(chart, "chart/subtitle", null);
        String grid  = XMLUtils.xpathString(chart, "chart/display-grid", null);
        String logo  = XMLUtils.xpathString(chart, "chart/display-logo", null);
        String placeh = XMLUtils.xpathString(chart, "chart/logo-placeh", null);
        String placev = XMLUtils.xpathString(chart, "chart/logo-placev", null);

        if (log.isDebugEnabled()) {
            log.debug("Found chart title:    '" + title + "'");
            log.debug("Found chart subtitle: '" + sub + "'");
            log.debug("Found chart grid:     '" + grid + "'");
            log.debug("Found chart logo:     '" + logo + "'");
            log.debug("Found chart logo placeh: '" + placeh + "'");
            log.debug("Found chart logo placev: '" + placev + "'");
        }

        chartSection.setTitle(title);
        chartSection.setSubtitle(sub);
        chartSection.setDisplayGrid(Boolean.valueOf(grid));
        chartSection.setDisplayLogo(logo);
        chartSection.setLogoHPlacement(placeh);
        chartSection.setLogoVPlacement(placev);

        target.setChartSection(chartSection);
    }


    protected static void parseLegend(ChartSettings target, Node legend) {
        LegendSection section = new LegendSection();

        String vis   = XMLUtils.xpathString(legend, "legend/visibility", null);
        String fSize = XMLUtils.xpathString(legend, "legend/font-size", null);
        String lthre = XMLUtils.xpathString(
            legend, "legend/aggregation-threshold", null);

        if (log.isDebugEnabled()) {
            log.debug("Found legend visibility: '" + vis + "'");
            log.debug("Found legend font size : '" + fSize + "'");
            log.debug("Found legend aggregation threshold : '" + lthre + "'");
        }

        section.setVisibility(Boolean.valueOf(vis));
        section.setFontSize(
            Integer.valueOf(fSize.length() > 0 ? fSize : "-1"));
        section.setAggregationThreshold(
            Integer.valueOf(lthre.length() >0 ? lthre : "-1"));

        target.setLegendSection(section);
    }


    protected static void parseExport(ChartSettings target, Node export) {
        ExportSection section = new ExportSection();

        String width  = XMLUtils.xpathString(export, "export/width", null);
        String height = XMLUtils.xpathString(export, "export/height", null);

        if (log.isDebugEnabled()) {
            log.debug("Found export width : '" + width + "'");
            log.debug("Found export height: '" + height + "'");
        }

        section.setWidth(Integer.valueOf(width.length() > 0 ? width : "-1"));
        section.setHeight(Integer.valueOf(height.length() > 0 ? height : "-1"));

        target.setExportSection(section);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
