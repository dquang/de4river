/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.wsplgen.FacetCreator;


public class WSPLGENJob {
    private static Logger log = LogManager.getLogger(WSPLGENJob.class);

    public static final String GEL_SPERRE   = "SPERRE";
    public static final String GEL_NOSPERRE = "NOSPERRE";


    protected D4EArtifact artifact;

    protected CallContext callContext;

    protected WSPLGENCalculation calculation;

    protected FacetCreator facetCreator;

    protected File workingDir;

    protected String dgm;
    protected String pro;
    protected String wsp;
    protected String wspTag;
    protected String axis;
    protected String area;
    protected String gel;
    protected String outFile;

    protected List<String> lin;

    protected int out;

    protected double start;
    protected double end;
    protected double from;
    protected double to;
    protected double diff;
    protected double dist;



    public WSPLGENJob(
        D4EArtifact       flys,
        File               workingDir,
        FacetCreator       facetCreator,
        CallContext        context,
        WSPLGENCalculation calculation)
    {
        this.artifact     = flys;
        this.workingDir   = workingDir;
        this.facetCreator = facetCreator;
        this.callContext  = context;
        this.calculation  = calculation;

        out   = -1;
        start = Double.NaN;
        end   = Double.NaN;
        from  = Double.NaN;
        to    = Double.NaN;
        diff  = Double.NaN;
        dist  = Double.NaN;
        lin   = new ArrayList<String>(3);
    }


    public File getWorkingDir() {
        return workingDir;
    }


    public D4EArtifact getArtifact() {
        return artifact;
    }


    public FacetCreator getFacetCreator() {
        return facetCreator;
    }


    public WSPLGENCalculation getCalculation() {
        return calculation;
    }


    public CallContext getCallContext() {
        return callContext;
    }


    public void setWsp(String wsp) {
        this.wsp = wsp;
    }


    public String getWsp() {
        return wsp;
    }


    public void setWspTag(String wspTag) {
        this.wspTag = wspTag;
    }


    public String getWspTag() {
        return wspTag;
    }


    public void addLin(String lin) {
        this.lin.add(lin);
    }


    public List<String> getLin() {
        return lin;
    }


    public void setAxis(String axis) {
        this.axis = axis;
    }


    public String getAxis() {
        return axis;
    }


    public void setArea(String area) {
        this.area = area;
    }


    public String getArea() {
        return area;
    }


    public void setOut(int out) {
        this.out = out;
    }


    public int getOut() {
        return out;
    }


    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }


    public String getOutFile() {
        return outFile;
    }


    public void setStart(double start) {
        this.start = start;
    }


    public double getStart() {
        return start;
    }


    public void setEnd(double end) {
        this.end = end;
    }


    public double getEnd() {
        return end;
    }


    public void setPro(String pro) {
        this.pro = pro;
    }


    public String getPro() {
        return pro;
    }


    public void setDgm(String dgm) {
        this.dgm = dgm;
    }


    public String getDgm() {
        return dgm;
    }


    public void setFrom(double from) {
        this.from = from;
    }


    public double getFrom() {
        return from;
    }


    public void setTo(double to) {
        this.to = to;
    }


    public double getTo() {
        return to;
    }


    public void setDiff(double diff) {
        this.diff = diff;
    }


    public double getDiff() {
        return diff;
    }


    public void setDist(double dist) {
        this.dist = dist;
    }


    public double getDist() {
        return dist;
    }


    public void setGel(String gel) {
        if (gel == null || gel.length() == 0) {
            return;
        }

        if (gel.equals(GEL_SPERRE) || gel.equals(GEL_NOSPERRE)) {
            this.gel = gel;
        }
    }


    public String getGel() {
        return gel;
    }


    public void toFile(File file)
    throws IOException, IllegalArgumentException
    {
        PrintWriter writer = null;

        try {
            writer =
                new PrintWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(file)));

            write(writer);
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }


    /** Prepare the job by writing supplementary files. */
    protected void write(PrintWriter writer)
    throws IOException, IllegalArgumentException
    {
        writeWsp(writer);    // required
        writeWspTag(writer); // required
        writeLin(writer);
        writeAxis(writer);   // required
        writeArea(writer);
        writeOut(writer);
        writeOutFile(writer);
        writeRange(writer);
        writeDelta(writer);
        writeGel(writer);
        writeDist(writer);
        writePro(writer);    // required
        writeDgm(writer);    // required
    }


    protected void writeWsp(PrintWriter writer)
    throws IllegalArgumentException
    {
        String wsp = getWsp();

        if (wsp != null && wsp.length() > 0) {
            writer.println("-WSP=\"" + wsp + "\"");
            return;
        }

        log.error("Required WSP missing!");
        throw new IllegalArgumentException("Required WSP missing!");
    }

    protected void writeWspTag(PrintWriter writer)
    throws IllegalArgumentException
    {
        String wspTag = getWspTag();

        if (wspTag != null && wspTag.length() > 0) {
            writer.println("-WSPTAG=\"" + wspTag + "\"");
            return;
        }

        log.error("Required WSPTAG missing!");
        throw new IllegalArgumentException("Required WSPTAG missing!");
    }

    protected void writeLin(PrintWriter writer)
    throws IllegalArgumentException
    {
        List<String> lins = getLin();

        if (lins != null && !lins.isEmpty()) {
            for (String lin: lins) {
                writer.println("-LIN=\"" + lin + "\"");
            }
        }
    }

    protected void writeAxis(PrintWriter writer)
    throws IllegalArgumentException
    {
        String axis = getAxis();

        if (axis != null && axis.length() > 0) {
            writer.println("-ACHSE=\"" + axis + "\"");
            return;
        }

        log.error("Required axis missing!");
        throw new IllegalArgumentException("Required axis missing!");
    }

    protected void writeGel(PrintWriter writer)
    throws IllegalArgumentException
    {
        String gel = getGel();

        if (gel != null && gel.length() > 0) {
            writer.println("-GEL=" + gel);
        }
    }

    protected void writeArea(PrintWriter writer)
    throws IllegalArgumentException
    {
        String area = getArea();

        if (area != null && area.length() > 0) {
            writer.println("-GEBIET=\"" + area + "\"");
        }
    }


    protected void writeOut(PrintWriter writer)
    throws IllegalArgumentException
    {
        int out = getOut();

        if (out >= 0) {
            writer.println("-OUTPUT=" + String.valueOf(out));
        }
    }

    protected void writeOutFile(PrintWriter writer)
    throws IllegalArgumentException
    {
        String outFile = getOutFile();

        if (outFile != null && outFile.length() > 0) {
            writer.println("-AUSGABE=\""+ outFile + "\"");
        }
    }

    protected void writeRange(PrintWriter writer)
    throws IllegalArgumentException
    {
        StringBuilder sb = new StringBuilder("-STRECKE=");

        double start = getStart();
        double end   = getEnd();

        if (Double.isNaN(start) && Double.isNaN(end)) {
            return;
        }

        if (! Double.isNaN(getStart())) {
            sb.append(getStart());
        }

        sb.append(",");

        if (! Double.isNaN(getEnd())) {
            sb.append(getEnd());
        }

        writer.println(sb.toString());
    }

    protected void writeDelta(PrintWriter writer)
    throws IllegalArgumentException
    {
        StringBuilder sb = new StringBuilder("-DELTA=");
        if (! Double.isNaN(from)) {
            sb.append(from);
        }

        sb.append(",");

        if (! Double.isNaN(to)) {
            sb.append(to);
        }

        sb.append(",");

        if (! Double.isNaN(diff)) {
            sb.append(diff);
        }

        writer.println(sb.toString());
    }

    protected void writeDist(PrintWriter writer)
    throws IllegalArgumentException
    {
        if (! Double.isNaN(getDist())) {
            writer.println("-DIST=" + String.valueOf(getDist()));
        }
    }

    protected void writePro(PrintWriter writer)
    throws IllegalArgumentException
    {
        if (pro != null && pro.length() > 0) {
            writer.println("-PRO=\"" + getPro() + "\"");
            return;
        }

        log.error("Required cross section tracks missing!");
        throw new IllegalArgumentException(
            "Required cross section tracks missing!");
    }

    protected void writeDgm(PrintWriter writer)
    throws IllegalArgumentException
    {
        if (dgm != null && dgm.length() > 0) {
            writer.println("-DGM=\"" + getDgm() + "\"");
            return;
        }

        log.error("Required DEM missing!");
        throw new IllegalArgumentException("Required DEM missing!");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
