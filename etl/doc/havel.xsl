<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml"/>

    <xsl:template match="/DIPSFLYS/STATIONEN/PEGELSTATION[@GEWAESSER='Untere Havel-Wasserstraße (UHW)']">
        <PEGELSTATION>
            <xsl:attribute name="GEWAESSER">Havel</xsl:attribute>
            <xsl:apply-templates select="@*[local-name() != 'GEWAESSER']"/>
            <xsl:apply-templates select="node()"/>
        </PEGELSTATION>
    </xsl:template>

    <xsl:template match="@*|node()">
       <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
