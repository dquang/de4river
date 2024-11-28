/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import com.vividsolutions.jts.geom.Geometry;

import java.io.Serializable;
import java.util.List;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Type;
import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.backend.SessionHolder;

@Entity
@Table(name = "hws_points")
public class HWSPoint implements Serializable {

    private Integer    id;

    private Integer    ogrFid;
    private HWSKind    kind;
    private FedState   fedState;
    private River      river;
    private Integer    official;
    private Integer    shoreSide;
    private String     name;
    private String     path;
    private String     agency;
    private String     range;
    private String     source;
    private String     statusDate;
    private String     description;
    private BigDecimal freeboard;
    private BigDecimal dikeKm;
    private BigDecimal z;
    private BigDecimal zTarget;
    private BigDecimal ratedLevel;
    private Geometry   geom;

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_HWS_POINTS_ID_SEQ",
        sequenceName   = "HWS_POINTS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_HWS_POINTS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Geometry getGeom() {
        return geom;
    }


    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    /**
     * Get ogrFid.
     *
     * @return ogrFid as Integer.
     */
    @Column(name = "ogr_fid")
    public Integer getOgrFid() {
        return ogrFid;
    }

    /**
     * Set ogrFid.
     *
     * @param ogrFid the value to set.
     */
    public void setOgrFid(Integer ogrFid) {
        this.ogrFid = ogrFid;
    }


    /**
     * Get official.
     *
     * @return official as Integer.
     */
    @Column(name = "official")
    public Integer getofficial() {
        return official;
    }

    /**
     * Set official.
     *
     * @param official the value to set.
     */
    public void setofficial(Integer official) {
        this.official = official;
    }

    /**
     * Get shoreSide.
     *
     * @return shoreSide as Integer.
     */
    @Column(name = "shore_side")
    public Integer getShoreSide() {
        return shoreSide;
    }

    /**
     * Set shoreSide.
     *
     * @param shoreSide the value to set.
     */
    public void setShoreSide(Integer shoreSide) {
        this.shoreSide = shoreSide;
    }

    /**
     * Get name.
     *
     * @return name as String.
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get path.
     *
     * @return path as String.
     */
    @Column(name = "path")
    public String getPath() {
        return path;
    }

    /**
     * Set path.
     *
     * @param path the value to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get agency.
     *
     * @return agency as String.
     */
    @Column(name = "agency")
    public String getAgency() {
        return agency;
    }

    /**
     * Set agency.
     *
     * @param agency the value to set.
     */
    public void setAgency(String agency) {
        this.agency = agency;
    }

    /**
     * Get range.
     *
     * @return range as String.
     */
    @Column(name = "range")
    public String getRange() {
        return range;
    }

    /**
     * Set range.
     *
     * @param range the value to set.
     */
    public void setRange(String range) {
        this.range = range;
    }

    /**
     * Get source.
     *
     * @return source as String.
     */
    @Column(name = "source")
    public String getSource() {
        return source;
    }

    /**
     * Set source.
     *
     * @param source the value to set.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get statusDate.
     *
     * @return statusDate as String.
     */
    @Column(name = "status_date")
    public String getStatusDate() {
        return statusDate;
    }

    /**
     * Set statusDate.
     *
     * @param statusDate the value to set.
     */
    public void setStatusDate(String statusDate)
    {
        this.statusDate = statusDate;
    }

    /**
     * Get description.
     *
     * @return description as String.
     */
    @Column(name = "description")
    public String getDescription()
    {
        return description;
    }

    /**
     * Set description.
     *
     * @param description the value to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Get freeboard.
     *
     * @return freeboard as BigDecimal.
     */
    @Column(name = "freeboard")
    public BigDecimal getFreeboard()
    {
        return freeboard;
    }

    /**
     * Set freeboard.
     *
     * @param freeboard the value to set.
     */
    public void setFreeboard(BigDecimal freeboard)
    {
        this.freeboard = freeboard;
    }

    /**
     * Get dikeKm.
     *
     * @return dikeKm as BigDecimal.
     */
    @Column(name = "dike_km")
    public BigDecimal getDikeKm()
    {
        return dikeKm;
    }

    /**
     * Set dikeKm.
     *
     * @param dikeKm the value to set.
     */
    public void setDikeKm(BigDecimal dikeKm)
    {
        this.dikeKm = dikeKm;
    }

    /**
     * Get z.
     *
     * @return z as BigDecimal.
     */
    @Column(name = "z")
    public BigDecimal getZ()
    {
        return z;
    }

    /**
     * Set z.
     *
     * @param z the value to set.
     */
    public void setZ(BigDecimal z)
    {
        this.z = z;
    }

    /**
     * Get zTarget.
     *
     * @return zTarget as BigDecimal.
     */
    @Column(name = "z_target")
    public BigDecimal getZTarget()
    {
        return zTarget;
    }

    /**
     * Set zTarget.
     *
     * @param zTarget the value to set.
     */
    public void setZTarget(BigDecimal zTarget)
    {
        this.zTarget = zTarget;
    }

    /**
     * Get ratedLevel.
     *
     * @return ratedLevel as BigDecimal.
     */
    @Column(name = "rated_level")
    public BigDecimal getRatedLevel()
    {
        return ratedLevel;
    }

    /**
     * Set ratedLevel.
     *
     * @param ratedLevel the value to set.
     */
    public void setRatedLevel(BigDecimal ratedLevel)
    {
        this.ratedLevel = ratedLevel;
    }

    /**
     * Get kind.
     *
     * @return kind as HWSKind.
     */
    @OneToOne
    @JoinColumn(name = "kind_id")
    public HWSKind getKind()
    {
        return kind;
    }

    /**
     * Set kind.
     *
     * @param kind the value to set.
     */
    public void setKind(HWSKind kind)
    {
        this.kind = kind;
    }

    /**
     * Get fedState.
     *
     * @return fedState as FedState.
     */
    @OneToOne
    @JoinColumn(name = "fed_state_id")
    public FedState getFedState()
    {
        return fedState;
    }

    /**
     * Set fedState.
     *
     * @param fedState the value to set.
     */
    public void setFedState(FedState fedState)
    {
        this.fedState = fedState;
    }

    /**
     * Get river.
     *
     * @return river as River.
     */
    @OneToOne
    @JoinColumn(name = "river_id")
    public River getRiver()
    {
        return river;
    }

    /**
     * Set river.
     *
     * @param river the value to set.
     */
    public void setRiver(River river)
    {
        this.river = river;
    }

    public static List<HWSPoint> getPoints(int riverId, String name) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
                    "from HWSPoint where river.id =:river_id and name=:name");
        query.setParameter("river_id", riverId);
        query.setParameter("name", name);

        return query.list();
    }
}
