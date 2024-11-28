/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.data.xy.XYSeries;

import gnu.trove.TDoubleArrayList;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData.Station;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataFactory;

// Base class for SedimantLoad$UNITProcessors
public class SedimentLoadProcessor extends DefaultProcessor
{
    private final static Logger log =
            LogManager.getLogger(SedimentLoadProcessor.class);

    private static final double EPS = 1e-4;

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {

        CallContext context = generator.getCallContext();
        XYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                false, // Handle NaN
                theme);
        Object data = bundle.getData(context);
        String facetName = bundle.getFacetName();

        log.debug("Do out for: " + facetName);
        if (facetName.startsWith("sedimentload.")) {
            /* Remove stations (with NaN-values) at stations of
               different type than appropriate for current fraction.*/
            String [] facetNameParts = facetName.split("\\.");
            int gfSType = SedimentLoadData.measurementStationType(
                SedimentLoadData.grainFractionIndex(
                    facetNameParts[facetNameParts.length-1]));

            RiverAccess access =
                new RiverAccess((D4EArtifact)bundle.getArtifact());
            String river = access.getRiverName();
            SedimentLoadData sld =
                SedimentLoadDataFactory.INSTANCE.getSedimentLoadData(river);

            double [][] allData = (double[][]) data;
            TDoubleArrayList cleanedKms =
                new TDoubleArrayList(allData[0].length);
            TDoubleArrayList cleanedValues =
                new TDoubleArrayList(allData[0].length);

            for (int i = 0; i < allData[0].length; ++i) {
                double km = allData[0][i];
                Station station = sld.findStations(km-EPS, km+EPS).get(0);
                if (station.isType(gfSType)) {
                    cleanedKms.add(km);
                    cleanedValues.add(allData[1][i]);
                }
            }

            double [][] points = new double[2][cleanedKms.size()];
            points[0] = cleanedKms.toNativeArray();
            points[1] = cleanedValues.toNativeArray();

            StyledSeriesBuilder.addPoints(series, points, false); // Keep NaN

            generator.addAxisSeries(series, axisName, visible);

        } else {
            log.error("Unknown facet name: " + facetName);
            return;
        }
    }
}

