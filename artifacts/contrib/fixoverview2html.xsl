<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="UTF-8"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" indent="yes"/>

  <xsl:param name="project-uuid">de3f3307-3429-4ff9-8f43-3fb2fcf21b27</xsl:param>
  <xsl:param name="render-checkboxes" select="true()"/>
  <xsl:param name="callback"/>

  <xsl:param name="locale">de</xsl:param>

  <xsl:decimal-format name="de" decimal-separator=',' grouping-separator='.'/>
  <xsl:decimal-format name="en" decimal-separator='.' grouping-separator=','/>

  <!-- XXX: This kind of i18n is cheesy.
            It should be better done in an external resource. -->

  <xsl:variable name="km-pattern">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">0,##</xsl:when>
      <xsl:otherwise>0.##</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-event">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">Ereignis</xsl:when>
      <xsl:otherwise>Event</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-legend">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">Legende</xsl:when>
      <xsl:otherwise>Caption</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-color">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">Farbe</xsl:when>
      <xsl:otherwise>Color</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-meaning">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">Bedeutung</xsl:when>
      <xsl:otherwise>Meaning</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-around-mnq">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">um MNQ</xsl:when>
      <xsl:otherwise>around MNQ</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-around-mq">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">um MQ</xsl:when>
      <xsl:otherwise>around MQ</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-around-mhq">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">um MHQ</xsl:when>
      <xsl:otherwise>around MHQ</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="i18n-above-hq5">
    <xsl:choose>
      <xsl:when test="$locale = 'de'">über HQ5</xsl:when>
      <xsl:otherwise>above HQ5</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- TODO: Format dates according locale. -->

  <xsl:template match="@*" mode="min">
    <xsl:if test="position() = 1">
      <xsl:value-of select="number(.)"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@*" mode="max">
    <xsl:if test="position() = last()">
      <xsl:value-of select="number(.)"/>
    </xsl:if>
  </xsl:template>

  <xsl:variable name="global-min">
    <xsl:choose>
      <xsl:when test="count(/fixings/events/event/sector) &gt; 0">
        <xsl:apply-templates mode="min" select="/fixings/events/event/sector/@from">
          <xsl:sort data-type="number" select="."/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(/fixings/river/@from)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="global-max">
    <xsl:choose>
      <xsl:when test="count(/fixings/events/event/sector) &gt; 0">
        <xsl:apply-templates mode="max" select="/fixings/events/event/sector/@to">
          <xsl:sort data-type="number" select="."/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(/fixings/river/@to)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template name="percent">
    <xsl:param name="sector"/>

    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="number($sector/@from) &lt; $global-min">
          <xsl:value-of select="$global-min"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="number($sector/@from)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="end">
      <xsl:choose>
        <xsl:when test="number($sector/@to) &gt; $global-max">
          <xsl:value-of select="$global-max"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="number($sector/@to)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$end &lt; $start">
        <xsl:value-of select="number(0)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="100.0 * (($end - $start) div ($global-max - $global-min))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="emit-gap-gauge">
    <xsl:param name="gauge"/>
    <xsl:call-template name="internal-emit-gap">
      <xsl:with-param name="sector" select="$gauge"/>
      <xsl:with-param name="preds" select="$gauge/preceding-sibling::gauge"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="emit-gap">
    <xsl:param name="sector"/>
    <xsl:call-template name="internal-emit-gap">
      <xsl:with-param name="sector" select="$sector"/>
      <xsl:with-param name="preds" select="$sector/preceding-sibling::sector"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="internal-emit-gap">
    <xsl:param name="sector"/>
    <xsl:param name="preds"/>

    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="number($sector/@from) &lt; $global-min">
          <xsl:value-of select="$global-min"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$sector/@from"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="$start &lt; $global-max">
      <xsl:variable name="num-preds" select="count($preds)"/>
      <xsl:variable name="prev-end">
        <xsl:choose>
          <xsl:when test="count($preds) &lt; 1">
            <xsl:value-of select="$global-min"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="number($preds[last()]/@to)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:if test="$prev-end &lt; $global-max">
        <xsl:variable name="gap-len" select="$start - $prev-end"/>
        <xsl:if test="$gap-len &gt; 0.005">
          <div>
            <xsl:attribute name="style">
              <xsl:text>width:</xsl:text>
                <xsl:value-of select="100.0 * ($gap-len div ($global-max - $global-min))"/>
              <xsl:text>%;float:left</xsl:text>
            </xsl:attribute>
            <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
          </div>
        </xsl:if>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="sector" mode="sectors">
    <xsl:call-template name="emit-gap">
      <xsl:with-param name="sector" select="."/>
    </xsl:call-template>
    <div>
      <xsl:attribute name="style">
        <xsl:text>background:</xsl:text>
        <xsl:choose>
          <xsl:when test="@class = '0'">green</xsl:when>
          <xsl:when test="@class = '1'">blue</xsl:when>
          <xsl:when test="@class = '2'">magenta</xsl:when>
          <xsl:when test="@class = '3'">red</xsl:when>
          <xsl:otherwise>black</xsl:otherwise>
        </xsl:choose>
        <xsl:text>;width:</xsl:text>
        <xsl:call-template name="percent">
          <xsl:with-param name="sector" select="."/>
        </xsl:call-template>
        <xsl:text>%</xsl:text>
        <xsl:text>;float:left</xsl:text>
      </xsl:attribute>
      <xsl:attribute name="title">
        <xsl:text>km </xsl:text>
        <xsl:value-of select="format-number(@from, $km-pattern, $locale)"/>
        <xsl:text> - </xsl:text>
        <xsl:value-of select="format-number(@to, $km-pattern, $locale)"/>
        <xsl:choose>
          <xsl:when test="@class = '0'"> / Q <xsl:value-of select="$i18n-around-mnq"/></xsl:when>
          <xsl:when test="@class = '1'"> / Q <xsl:value-of select="$i18n-around-mq"/></xsl:when>
          <xsl:when test="@class = '2'"> / Q <xsl:value-of select="$i18n-around-mhq"/></xsl:when>
          <xsl:when test="@class = '3'"> / Q <xsl:value-of select="$i18n-above-hq5"/></xsl:when>
        </xsl:choose>
      </xsl:attribute>
      <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
    </div>
  </xsl:template>

  <xsl:template match="event">
    <tr id="{@cid}">
      <xsl:if test="$render-checkboxes">
        <td>
          <input type="checkbox" name="{$project-uuid}:{@cid}" checked="true" onclick="{$callback}"/>
        </td>
      </xsl:if>
      <td>
        <xsl:apply-templates mode="sectors"/>
      </td>
      <td align="center">
        <xsl:attribute name="title"><xsl:value-of select="@description"/></xsl:attribute>
        <xsl:value-of select="@date"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="gauge" mode="gauges">
    <xsl:call-template name="emit-gap-gauge">
      <xsl:with-param name="gauge" select="."/>
    </xsl:call-template>
    <div>
      <xsl:variable name="start">
        <xsl:choose>
          <xsl:when test="number(@from) &lt; $global-min">
            <xsl:value-of select="$global-min"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="number(@from)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="end">
        <xsl:choose>
          <xsl:when test="number(@to) &gt; $global-max">
            <xsl:value-of select="$global-max"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="number(@to)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:attribute name="style">
        <xsl:text>overflow:hidden;background:</xsl:text>
        <xsl:choose>
          <xsl:when test="(count(preceding::*) mod 2) = 0">#ada96e</xsl:when>
          <xsl:otherwise>silver</xsl:otherwise>
        </xsl:choose>
        <xsl:text>;width:</xsl:text>
        <xsl:call-template name="percent">
          <xsl:with-param name="sector" select="."/>
        </xsl:call-template>
        <xsl:text>%</xsl:text>
        <xsl:text>;float:left</xsl:text>
      </xsl:attribute>
      <xsl:attribute name="title">
        <xsl:value-of select="@name"/>
        <xsl:text>: km </xsl:text>
        <xsl:value-of select="format-number($start, $km-pattern, $locale)"/>
        <xsl:text> - </xsl:text>
        <xsl:value-of select="format-number($end, $km-pattern, $locale)"/>
      </xsl:attribute>
      <nobr><xsl:value-of select="@name"/></nobr>
    </div>
  </xsl:template>

  <xsl:template match="events">
    <table width="97%" border="1" cellspacing="0" cellpadding="0"
           style="font-size: 10pt;font-family:Arial;Verdana,sans-serif">
      <colgroup>
        <xsl:if test="$render-checkboxes">
            <col width="20px"/>
        </xsl:if>
        <col width="*"/>
        <col width="75px"/>
      </colgroup>
      <tr>
        <xsl:if test="$render-checkboxes">
          <th>&#160;</th>
        </xsl:if>
        <th><xsl:apply-templates mode="gauges" select="/fixings/gauges"/></th>
        <th><xsl:value-of select="$i18n-event"/></th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="/">
    <html>
      <head>
        <title>Fixierungen:</title>
      </head>
      <body>
        <xsl:apply-templates/>
        <hr/>
        <table border="1" cellspacing="0" cellpadding="0">
          <caption><xsl:value-of select="$i18n-legend"/></caption>
          <tr>
            <th><xsl:value-of select="$i18n-color"/></th>
            <th colspan="2"><xsl:value-of select="$i18n-meaning"/></th>
          </tr>
          <xsl:if test="count(/fixings/events/event/sector[@class = '0']) &gt; 0">
          <tr>
            <td style="background: green">&#160;</td>
            <td><xsl:value-of select="$i18n-around-mnq"/></td>
            <td>Q &#8712; [0, (MNQ+MQ)/2)</td>
          </tr>
          </xsl:if>
          <xsl:if test="count(/fixings/events/event/sector[@class = '1']) &gt; 0">
          <tr>
            <td style="background: blue">&#160;</td>
            <td><xsl:value-of select="$i18n-around-mq"/></td>
            <td>Q &#8712; [(MNQ+MQ)/2, (MQ+MHQ)/2)</td>
          </tr>
          </xsl:if>
          <xsl:if test="count(/fixings/events/event/sector[@class = '2']) &gt; 0">
          <tr>
            <td style="background: magenta">&#160;</td>
            <td><xsl:value-of select="$i18n-around-mhq"/></td>
            <td>Q &#8712; [(MQ+MHQ)/2, HQ5)</td>
          </tr>
          </xsl:if>
          <xsl:if test="count(/fixings/events/event/sector[@class = '3']) &gt; 0">
          <tr>
            <td style="background: red">&#160;</td>
            <td><xsl:value-of select="$i18n-above-hq5"/></td>
            <td>Q &#8712; [HQ5, &#8734;)</td>
          </tr>
          </xsl:if>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="text()"/>
  <xsl:template match="text()" mode="sectors"/>
  <xsl:template match="text()" mode="gauges"/>

</xsl:stylesheet>
