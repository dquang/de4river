<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml"/>

    <xsl:key name="gauge-name" match="/STATIONEN/STATION" use="@NAME"/>

    <xsl:template name="lookup-gauge-number">
        <xsl:param name="name"/>
        <xsl:param name="number"/>
        <xsl:variable name="fixed-number">
            <xsl:for-each select="document('pegelstationen.xml')">
                <xsl:value-of select="key('gauge-name', $name)/@NUMMER"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$fixed-number != ''">
                <xsl:value-of select="$fixed-number"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$number"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/DIPSFLYS/STATIONEN/PEGELSTATION">
        <PEGELSTATION>
        <xsl:attribute name="NUMMER">
            <xsl:call-template name="lookup-gauge-number">
                <xsl:with-param name="name" select="@NAME"/>
                <xsl:with-param name="number" select="@NUMMER"/>
            </xsl:call-template>
        </xsl:attribute>
        <xsl:apply-templates select="@*[local-name() != 'NUMMER']"/>
        <xsl:apply-templates select="node()"/>
        </PEGELSTATION>
    </xsl:template>

    <xsl:template match="@*|node()">
       <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
