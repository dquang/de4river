/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Comparator;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.InputStreamReader;

import org.apache.commons.math.optimization.fitting.CurveFitter;

import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

import org.apache.commons.math.MathException;

public class App
{
    public static final double EPS = 1e-5;

    public static final String FUNCTION_NAME =
        System.getProperty("function", "linear");

    public static final Comparator<Double> EPS_CMP =
        new Comparator<Double>()  {
            @Override
            public int compare(Double a, Double b) {
                double diff = a - b;
                if (diff < -EPS) return -1;
                if (diff >  EPS) return +1;
                return 0;
            }
        };

    public static final List<Double []>readPoints(Reader reader)
    throws IOException
    {
        Map<Double, Double> map = new TreeMap<Double, Double>(EPS_CMP);

        BufferedReader input = new BufferedReader(reader);

        String line;
        while ((line = input.readLine()) != null) {
            if ((line = line.trim()).length() == 0 || line.startsWith("#")) {
                continue;
            }

            String [] parts = line.split("\\s+");

            if (parts.length < 2) {
                continue;
            }

            try {
                Double x = Double.valueOf(parts[0]);
                Double y = Double.valueOf(parts[1]);

                Double old = map.put(x, y);

                if (old != null) {
                    System.err.println("duplicate x: " + x);
                }
            }
            catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        List<Double []> list = new ArrayList<Double []>(map.size());

        for (Map.Entry<Double, Double> entry: map.entrySet()) {
            list.add(new Double [] { entry.getKey(), entry.getValue() });
        }

        return list;
    }

    public static void main(String [] args) {

        Function function = FunctionFactory
            .getInstance()
            .getFunction(FUNCTION_NAME);

        if (function == null) {
            System.err.println("Cannot find function '" + FUNCTION_NAME + "'.");
            System.exit(1);
        }

        List<Double []> points = null;

        try {
            points = readPoints(new InputStreamReader(System.in));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();

        CurveFitter cf = new CurveFitter(lmo);

        for (Double [] point: points) {
            cf.addObservedPoint(point[0], point[1]);
        }

        double [] parameters = null;

        try {
            parameters = cf.fit(function, function.getInitialGuess());
        }
        catch (MathException me) {
            me.printStackTrace();
            System.exit(1);
        }

        String [] parameterNames = function.getParameterNames();

        for (int i = 0; i < parameterNames.length; ++i) {
            System.err.println(parameterNames[i] + ": " + parameters[i]);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
