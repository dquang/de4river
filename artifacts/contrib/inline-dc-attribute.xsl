<?xml version="1.0" encoding="UTF-8"?>
<!--
    inline-dc-attribute.xsl
    =======================
    Transforms datacage templates from:

        <foo>
          <dc:element name="bar" value="${baz}"/>
          <dc:element name="bla" value="${blub}-${urgs}"/>
        </foo>

    to:

        <foo bar="{$bar} bla="{$blub}-{$urgs}/>
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://www.intevation.org/2011/Datacage">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template name="string-replace-all">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="by"/>
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$by"/>
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="by" select="$by"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template
    match="node()[count(dc:attribute) &gt; 0 and namespace-uri() != 'http://www.intevation.org/2011/Datacage']">
    <xsl:copy>
      <xsl:for-each select="./dc:attribute">
        <xsl:attribute name="{@name}">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="@value"/>
            <xsl:with-param name="replace">${</xsl:with-param>
            <xsl:with-param name="by">{$</xsl:with-param>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="@*|node()" mode="ignore-text"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dc:attribute|text()" mode="ignore-text"/>
  <xsl:template match="@*|node()" mode="ignore-text">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
