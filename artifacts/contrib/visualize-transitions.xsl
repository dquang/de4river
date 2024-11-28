<?xml version="1.0" encoding="UTF-8"?>

<!--
 Copyright (c) 2010 by Intevation GmbH

 This program is free software under the LGPL (>=v2.1)
 Read the file LGPL.txt coming with the software for details
 or visit http://www.gnu.org/licenses/ if it does not exist.

 Author: Sascha L. Teichmann (sascha.teichmann@intevation.de)
-->

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    version="1.0">

    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:param name="base-dir">.</xsl:param>

    <xsl:template match="/">
        <xsl:text>digraph transition_model {&#xa;</xsl:text>
        <xsl:apply-templates />
        <xsl:text>}&#xa;</xsl:text>
    </xsl:template>

    <xsl:template match="artifact">
        <xsl:choose>
            <xsl:when test="@xlink:href != ''">
                <!-- handle external artifacts -->
                <xsl:variable name="path">
                    <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="text" select="@xlink:href" />
                    <xsl:with-param name="replace">${artifacts.config.dir}</xsl:with-param>
                    <xsl:with-param name="by" select="$base-dir" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:for-each select="document($path)">
                    <xsl:apply-templates select="/artifact"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <!-- handle internal artifacts -->
                <xsl:text>subgraph </xsl:text><xsl:value-of select="@name"/>
                <xsl:text> {&#xa;</xsl:text>
                <xsl:text>    label = "Artefakt: </xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>";&#xa;</xsl:text>
                <xsl:apply-templates mode="inside-artifact" select="./states/state"/>
                <xsl:apply-templates mode="inside-artifact" select="./states/transition"/>
                <xsl:text>}&#xa;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="state" mode="inside-artifact">
        <xsl:text>    "</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text disable-output-escaping="yes"
        >" [ shape = "record" label=&lt;&lt;table border="0" cellborder="0" cellpadding="3"&gt;
        &lt;tr&gt;&lt;td align="center" colspan="2" bgcolor="black"&gt;&lt;font color="white"&gt;</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text disable-output-escaping="yes"
        >&lt;/font&gt;&lt;/td&gt;&lt;/tr&gt;</xsl:text>
            <xsl:apply-templates mode="inside-artifact" />
        <xsl:text disable-output-escaping="yes"
        >&lt;/table&gt;&gt;]</xsl:text>
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>

    <xsl:template match="data" mode="inside-artifact">
        <xsl:text disable-output-escaping="yes"
        >&lt;tr&gt;&lt;td align="right"&gt;</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text disable-output-escaping="yes"
        >&lt;/td&gt;&lt;td align="left"&gt;</xsl:text>
        <xsl:value-of select="@type"/>
        <xsl:text disable-output-escaping="yes"
        >&lt;/td&gt;&lt;/tr&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="transition" mode="inside-artifact">
        <xsl:text>    "</xsl:text>
        <xsl:value-of select="from/@state"/>
        <xsl:text disable-output-escaping="yes">" -&gt; "</xsl:text>
        <xsl:value-of select="to/@state"/>
        <xsl:text>"</xsl:text>
        <xsl:apply-templates mode="inside-artifact"/>
        <xsl:text>;&#xa;</xsl:text>
    </xsl:template>

    <xsl:template match="condition" mode="inside-artifact">
        <xsl:text> [ label="</xsl:text>
        <xsl:value-of select="@data"/>
        <xsl:text> </xsl:text>
        <xsl:call-template name="readable-operator">
            <xsl:with-param name="operator" select="@operator"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@value"/>
        <xsl:text>" ]</xsl:text>
    </xsl:template>

    <xsl:template match="text()" mode="inside-artifact"/>
    <xsl:template match="text()"/>

    <xsl:template name="readable-operator">
        <xsl:param name="operator" />
        <xsl:choose>
            <xsl:when test='$operator = "equal"'>=</xsl:when>
            <xsl:when test='$operator = "notequal"'>!=</xsl:when>
            <xsl:otherwise><xsl:value-of select="$operator"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

     <xsl:template name="string-replace-all">
        <xsl:param name="text" />
        <xsl:param name="replace" />
        <xsl:param name="by" />
        <xsl:choose>
          <xsl:when test="contains($text, $replace)">
            <xsl:value-of select="substring-before($text,$replace)" />
            <xsl:value-of select="$by" />
            <xsl:call-template name="string-replace-all">
              <xsl:with-param name="text"
                  select="substring-after($text,$replace)" />
              <xsl:with-param name="replace" select="$replace" />
              <xsl:with-param name="by" select="$by" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$text" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:template>
    
</xsl:stylesheet>

