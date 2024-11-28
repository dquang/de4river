/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.river.model.Annotation;
import org.dive4elements.river.model.AnnotationType;
import org.dive4elements.river.model.Attribute;
import org.dive4elements.river.model.AxisKind;
import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.BedHeightValue;
import org.dive4elements.river.model.BedHeightType;
import org.dive4elements.river.model.Building;
import org.dive4elements.river.model.BoundaryKind;
import org.dive4elements.river.model.CrossSection;
import org.dive4elements.river.model.CrossSectionLine;
import org.dive4elements.river.model.CrossSectionPoint;
import org.dive4elements.river.model.CrossSectionTrack;
import org.dive4elements.river.model.CrossSectionTrackKind;
import org.dive4elements.river.model.DGM;
import org.dive4elements.river.model.Depth;
import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.DischargeTableValue;
import org.dive4elements.river.model.DischargeZone;
import org.dive4elements.river.model.Edge;
import org.dive4elements.river.model.ElevationModel;
import org.dive4elements.river.model.FedState;
import org.dive4elements.river.model.Fixpoint;
import org.dive4elements.river.model.Floodmaps;
import org.dive4elements.river.model.Floodmark;
import org.dive4elements.river.model.Floodplain;
import org.dive4elements.river.model.FloodplainKind;
import org.dive4elements.river.model.FlowVelocityMeasurement;
import org.dive4elements.river.model.FlowVelocityMeasurementValue;
import org.dive4elements.river.model.FlowVelocityModel;
import org.dive4elements.river.model.FlowVelocityModelValue;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.GrainFraction;
import org.dive4elements.river.model.HWSKind;
import org.dive4elements.river.model.HWSLine;
import org.dive4elements.river.model.HWSPoint;
import org.dive4elements.river.model.HYK;
import org.dive4elements.river.model.HYKEntry;
import org.dive4elements.river.model.HYKFlowZone;
import org.dive4elements.river.model.HYKFlowZoneType;
import org.dive4elements.river.model.HYKFormation;
import org.dive4elements.river.model.HydrBoundary;
import org.dive4elements.river.model.HydrBoundaryPoly;
import org.dive4elements.river.model.Jetty;
import org.dive4elements.river.model.LocationSystem;
import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.MainValueType;
import org.dive4elements.river.model.MeasurementStation;
import org.dive4elements.river.model.MorphologicalWidth;
import org.dive4elements.river.model.MorphologicalWidthValue;
import org.dive4elements.river.model.NamedMainValue;
import org.dive4elements.river.model.Porosity;
import org.dive4elements.river.model.PorosityValue;
import org.dive4elements.river.model.Position;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.RiverAxis;
import org.dive4elements.river.model.RiverAxisKm;
import org.dive4elements.river.model.SQRelation;
import org.dive4elements.river.model.SQRelationValue;
import org.dive4elements.river.model.SectieKind;
import org.dive4elements.river.model.SobekKind;
import org.dive4elements.river.model.SeddbName;
import org.dive4elements.river.model.SedimentDensity;
import org.dive4elements.river.model.SedimentDensityValue;
import org.dive4elements.river.model.SedimentLoad;
import org.dive4elements.river.model.SedimentLoadValue;
import org.dive4elements.river.model.SedimentLoadLS;
import org.dive4elements.river.model.SedimentLoadLSValue;
import org.dive4elements.river.model.TimeInterval;
import org.dive4elements.river.model.Unit;
import org.dive4elements.river.model.Wst;
import org.dive4elements.river.model.WstColumn;
import org.dive4elements.river.model.WstColumnQRange;
import org.dive4elements.river.model.WstColumnValue;
import org.dive4elements.river.model.WstQRange;
import org.dive4elements.river.model.OfficialLine;

public class FLYSCredentials
extends      Credentials
{
    public static final String XPATH_USER =
        "/artifact-database/backend-database/user/text()";

    public static final String XPATH_PASSWORD =
        "/artifact-database/backend-database/password/text()";

    public static final String XPATH_DIALECT =
        "/artifact-database/backend-database/dialect/text()";

    public static final String XPATH_DRIVER =
        "/artifact-database/backend-database/driver/text()";

    public static final String XPATH_URL =
        "/artifact-database/backend-database/url/text()";

    public static final String XPATH_CONNECTION_INIT_SQLS =
        "/artifact-database/backend-database/connection-init-sqls/text()";

    public static final String XPATH_VALIDATION_QUERY =
        "/artifact-database/backend-database/validation-query/text()";

    public static final String XPATH_MAX_WAIT =
        "/artifact-database/backend-database/max-wait/text()";

    public static final String DEFAULT_USER =
        System.getProperty("flys.backend.user", "flys");

    public static final String DEFAULT_PASSWORD =
        System.getProperty("flys.backend.password", "flys");

    public static final String DEFAULT_DIALECT =
        System.getProperty(
            "flys.backend.dialect",
            "org.hibernate.dialect.PostgreSQLDialect");

    public static final String DEFAULT_DRIVER =
        System.getProperty(
            "flys.backend.driver",
            "org.postgresql.Driver");

    public static final String DEFAULT_URL =
        System.getProperty(
            "flys.backend.url",
            "jdbc:postgresql://localhost:5432/flys");

    public static final String DEFAULT_CONNECTION_INIT_SQLS =
        System.getProperty(
            "flys.backend.connection.init.sqls");

    public static final String DEFAULT_VALIDATION_QUERY =
        System.getProperty(
            "flys.backend.connection.validation.query");

    public static final String DEFAULT_MAX_WAIT =
        System.getProperty("flys.backend.connection.max.wait");

    public static final Class [] CLASSES = {
        Annotation.class,
        AnnotationType.class,
        Attribute.class,
        AxisKind.class,
        BedHeight.class,
        BedHeightValue.class,
        BedHeightType.class,
        Building.class,
        BoundaryKind.class,
        CrossSection.class,
        CrossSectionLine.class,
        CrossSectionPoint.class,
        CrossSectionTrack.class,
        CrossSectionTrackKind.class,
        Depth.class,
        DGM.class,
        DischargeTable.class,
        DischargeTableValue.class,
        DischargeZone.class,
        Edge.class,
        ElevationModel.class,
        FedState.class,
        Fixpoint.class,
        Floodmark.class,
        Floodplain.class,
        FloodplainKind.class,
        Floodmaps.class,
        FlowVelocityMeasurement.class,
        FlowVelocityMeasurementValue.class,
        FlowVelocityModel.class,
        FlowVelocityModelValue.class,
        Gauge.class,
        GrainFraction.class,
        HWSKind.class,
        HWSLine.class,
        HWSPoint.class,
        HydrBoundary.class,
        HydrBoundaryPoly.class,
        HYK.class,
        HYKEntry.class,
        HYKFormation.class,
        HYKFlowZoneType.class,
        HYKFlowZone.class,
        Jetty.class,
        LocationSystem.class,
        MainValueType.class,
        MeasurementStation.class,
        MorphologicalWidth.class,
        MorphologicalWidthValue.class,
        NamedMainValue.class,
        MainValue.class,
        Position.class,
        Range.class,
        River.class,
        RiverAxis.class,
        RiverAxisKm.class,
        Porosity.class,
        PorosityValue.class,
        SectieKind.class,
        SobekKind.class,
        SeddbName.class,
        SedimentDensity.class,
        SedimentDensityValue.class,
        SedimentLoad.class,
        SedimentLoadValue.class,
        SedimentLoadLS.class,
        SedimentLoadLSValue.class,
        SQRelation.class,
        SQRelationValue.class,
        TimeInterval.class,
        Unit.class,
        WstColumn.class,
        WstColumnQRange.class,
        WstColumnValue.class,
        Wst.class,
        WstQRange.class,
        OfficialLine.class
    };

    public FLYSCredentials() {
    }

    public FLYSCredentials(
        String user,
        String password,
        String dialect,
        String driver,
        String url,
        String connectionInitSqls,
        String validationQuery,
        String maxWait
    ) {
        super(
            user, password, dialect, driver, url,
            connectionInitSqls, validationQuery, maxWait, CLASSES);
    }

    private static Credentials instance;

    public static synchronized Credentials getInstance() {
        if (instance == null) {
            String user =
                Config.getStringXPath(XPATH_USER, DEFAULT_USER);
            String password =
                Config.getStringXPath(XPATH_PASSWORD, DEFAULT_PASSWORD);
            String dialect =
                Config.getStringXPath(XPATH_DIALECT, DEFAULT_DIALECT);
            String driver =
                Config.getStringXPath(XPATH_DRIVER, DEFAULT_DRIVER);
            String url =
                Config.getStringXPath(XPATH_URL, DEFAULT_URL);
            String connectionInitSqls =
                Config.getStringXPath(
                    XPATH_CONNECTION_INIT_SQLS,
                    DEFAULT_CONNECTION_INIT_SQLS);
            String validationQuery =
                Config.getStringXPath(
                    XPATH_VALIDATION_QUERY,
                    DEFAULT_VALIDATION_QUERY);
            String maxWait =
                Config.getStringXPath(XPATH_MAX_WAIT, DEFAULT_MAX_WAIT);

            instance = new FLYSCredentials(
                user, password, dialect, driver, url, connectionInitSqls,
                validationQuery, maxWait);
        }
        return instance;
    }

    public static Credentials getDefault() {
        return new FLYSCredentials(
            DEFAULT_USER,
            DEFAULT_PASSWORD,
            DEFAULT_DIALECT,
            DEFAULT_DRIVER,
            DEFAULT_URL,
            DEFAULT_CONNECTION_INIT_SQLS,
            DEFAULT_VALIDATION_QUERY,
            DEFAULT_MAX_WAIT
        );
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
