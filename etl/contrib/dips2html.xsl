<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" version="1.0" exclude-result-prefixes="html">
  <xsl:output method="html"/>
  <xsl:template name="out">
    <xsl:param name="value"/>
    <xsl:choose>
      <xsl:when test="$value = ''">-/-</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="PEGELSTATION">
    <tr>
      <td>
        <xsl:value-of select="@NAME"/>
      </td>
      <td>
        <xsl:value-of select="@NUMMER"/>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@HOCHWERT"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@TK_BLATT"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@BETREIBER"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@GEOBREITE"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@GEOLAENGE"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@GEWAESSER"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@GUELTIGAB"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@LAGESTATUS"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@RECHTSWERT"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@ABLESUNGBIS"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@ABLESUNGSEIT"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@STATIONIERUNG"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@EINGERICHTETAM"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@EINZUGSGEBIET_AEO"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@GEBIETSKENNZIFFER"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@KILOMETRIERUNG_AB"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@LAGE_AM_GEWAESSER"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@ENTFERNUNGMUENDUNG"/>
        </xsl:call-template>
      </td>
      <td>
        <xsl:call-template name="out">
          <xsl:with-param name="value" select="@KILOMETRIERUNGSRICHTUNG"/>
        </xsl:call-template>
      </td>
    </tr>
    <xsl:if test="string-length(@BESCHREIBUNG) &gt; 0">
      <tr>
        <td/>
        <td colspan="21">
          <xsl:value-of select="@BESCHREIBUNG"/>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  <xsl:template match="/">
    <html>
      <head>
        <title>DiPs</title>
      </head>
      <body>
        <table border="1" cellspacing="0" width="50%">
          <tr>
            <th>Name</th>
            <th>Nummer</th>
            <th>Hochwert</th>
            <th>TK-Blatt</th>
            <th>Betreiber</th>
            <th>Geo-Breite</th>
            <th>Geo-L&#xE4;nge</th>
            <th>Gew&#xE4;sser</th>
            <th>G&#xFC;ltig ab</th>
            <th>Lagestatus</th>
            <th>Rechtswert</th>
            <th>Ablesung seit</th>
            <th>Ablesung bis</th>
            <th>Stationierung</th>
            <th>Eingerichtet am</th>
            <th>Einzugsgebiet AEO</th>
            <th>Gebietskennziffer</th>
            <th>Kilometrierung ab</th>
            <th>Lage am Gew&#xE4;sser</th>
            <th>Entfernung zu M&#xFC;ndung</th>
            <th>Kilometrierungsrichtung</th>
          </tr>
          <xsl:apply-templates select="/DIPSFLYS/STATIONEN"/>
        </table>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="text()"/>
</xsl:stylesheet>
