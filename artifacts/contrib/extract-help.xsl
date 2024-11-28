<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>

  <xsl:param name="base-url">https://flys-intern.intevation.de/Flys-3.0/OnlineHilfe</xsl:param>
  <xsl:param name="module">Fixierungsanalyse</xsl:param>

  <xsl:template match="//state">
      <xsl:value-of select="@helpText"/>=<xsl:value-of
              select="concat($base-url, '/', $module, '#', @helpText)"/>
      <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template match="text()"/>

</xsl:stylesheet>
