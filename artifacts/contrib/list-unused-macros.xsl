<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://www.intevation.org/2011/Datacage"
  version="1.0">

  <xsl:output method="text" encoding="UTF-8"/>

  <xsl:template match="/">
    <xsl:text>Duplicate macros:&#xa;</xsl:text>
    <xsl:for-each select="//dc:macro/@name">
      <xsl:variable name="mname" select="."/>
      <xsl:if test="count(//dc:macro[@name=$mname]) &gt; 1">
        <xsl:text>  </xsl:text>
        <xsl:value-of select="$mname"/>
        <xsl:text>&#xa;</xsl:text>
      </xsl:if>
    </xsl:for-each>

    <xsl:text>Marcos defined but not called:&#xa;</xsl:text>
    <xsl:for-each select="//dc:macro/@name">
      <xsl:variable name="mname" select="."/>
      <xsl:if test="count(//dc:call-macro[@name=$mname]) = 0">
        <xsl:text>  </xsl:text>
        <xsl:value-of select="$mname"/>
        <xsl:text>&#xa;</xsl:text>
      </xsl:if>
    </xsl:for-each>

    <xsl:text>Marcos called but not defined:&#xa;</xsl:text>
    <xsl:for-each select="//dc:call-macro/@name">
      <xsl:variable name="mname" select="."/>
      <xsl:if test="count(//dc:macro[@name=$mname]) = 0">
        <xsl:text>  </xsl:text>
        <xsl:value-of select="$mname"/>
        <xsl:text>&#xa;</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>

