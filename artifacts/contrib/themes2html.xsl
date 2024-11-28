<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="UTF-8"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" indent="yes"/>


  <xsl:template match="/themes/themegroup" mode="header">
    <li><a href="#tg-{@name}"><xsl:value-of select="@name"/></a> (<xsl:value-of select="count(theme)"/> themes)</li>
  </xsl:template>

  <xsl:template match="field" mode="theme-inherits">
    <tr>
        <td><i><xsl:value-of select="@name"/></i> (<a
            href="#theme-{../../../@name}-{../../@name}"><xsl:value-of select="../../@name"/></a>)</td>
        <td><xsl:value-of select="@display"/></td>
        <td><xsl:value-of select="@type"/></td>
        <td><xsl:value-of select="@default"/></td>
        <td><xsl:value-of select="@hints"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="field" mode="theme-fields">
    <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="@display"/></td>
        <td><xsl:value-of select="@type"/></td>
        <td><xsl:value-of select="@default"/></td>
        <td><xsl:value-of select="@hints"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="inherit" mode="theme-inherits">
    <xsl:variable name="target" select="@from"/>
    <xsl:apply-templates
        select="../../../theme[@name = $target]/inherits/inherit | /themes/themegroup/theme[@type='virtual' and @name=$target]/inherits/inherit"
        mode="theme-inherits"/>
    <xsl:apply-templates
        select="../../../theme[@name = $target]/fields/field | /themes/themegroup/theme[@type='virtual' and @name=$target]/fields/field"
        mode="theme-inherits"/>
  </xsl:template>

  <xsl:template match="theme" mode="sub-themes">
    <xsl:variable name="tname" select="../@name"/>
    <xsl:variable name="name" select="@name"/>
    <li><a href="#theme-{$tname}-{$name}"><xsl:value-of select="$tname"/>/<xsl:value-of select="$name"/></a></li>
  </xsl:template>

  <xsl:template match="theme" mode="theme-groups">
    <div>
      <h4>[<a href="#theme-groups">Groups</a>] [<a href="#tg-{../@name}"><xsl:value-of select="../@name"/></a>] <a 
          name="theme-{../@name}-{@name}">Theme '<xsl:value-of select="@name"/>'<xsl:if
        test="@type = 'virtual'"> (virtual)</xsl:if></a></h4>

      <xsl:variable name="tname" select="../@name"/>
      <xsl:variable name="is-virtual" select="$tname = 'virtual'"/>
      <xsl:variable name="name" select="@name"/>

      <xsl:if
        test="count(/themes/themegroup[$is-virtual or @name=$tname or @name='virtual']/theme[inherits/inherit/@from=$name]) &gt; 0">
      <strong>Sub themes</strong>
      <ul>
        <xsl:apply-templates
            select="/themes/themegroup[$is-virtual or @name=$tname or @name='virtual']/theme[inherits/inherit/@from=$name]"
            mode="sub-themes"/>
      </ul>
      </xsl:if>

      <table border="1" cellspacing="0" width="80%" summary="Definition of theme {@name}">
        <tr>
          <th>Name</th>
          <th>Display</th>
          <th>Type</th>
          <th>Default</th>
          <th>Hints</th>
        </tr>
        <xsl:apply-templates mode="theme-inherits" select="inherits/inherit"/>
        <xsl:apply-templates mode="theme-fields" select="fields/field"/>
      </table>
    </div>
    <hr/>
  </xsl:template>

  <xsl:template match="theme" mode="theme-list">
    <li><a href="#theme-{../@name}-{@name}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>

  <xsl:template match="/themes/themegroup" mode="theme-groups">
    <div>
        <h2><a name="tg-{@name}">Theme group '<xsl:value-of select="@name"/>'</a></h2>
        <div>
          <h3>Theme List</h3>
          <ul>
            <xsl:apply-templates mode="theme-list" select="theme"/>
          </ul>
        </div>
        <div>
          <h3>Theme Definitions</h3>
          <xsl:apply-templates mode="theme-groups" select="theme"/>
        </div>
    </div>
  </xsl:template>

  <xsl:template match="mapping" mode="mappings">
    <xsl:variable name="target" select="@to"/>
    <tr>
      <td><xsl:value-of select="@from"/></td>
      <td><a 
        href="#theme-{/themes/themegroup/theme[@name = $target][1]/../@name}-{$target}"
      ><xsl:value-of select="@to"/></a></td>
      <td><xsl:if test="@pattern"><pre><xsl:value-of select="@pattern"/></pre></xsl:if></td>
      <td><xsl:if test="@masterAttr"><pre><xsl:value-of select="@masterAttr"/></pre></xsl:if></td>
    </tr>
  </xsl:template>

  <xsl:template match="mappings" mode="mappings">
    <div>
      <h3><a name="mappings">Mappings</a></h3>
      <table width="80%" border="1" cellspacing="0" summary="Mappings from facets to themes">
      <tr>
        <th>From</th>
        <th>To</th>
        <th>Pattern</th>
        <th>Condition</th>
      </tr>
      <xsl:apply-templates select="mapping" mode="mappings"/>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="/">
    <html>
    <head>
        <title>FLYS3 - Themes</title>
    </head>
    <body>
      <h1>FLYS3 - Themes</h1>
      <div>
      <h2><a name="theme-groups">Theme groups</a> (<xsl:value-of select="count(/themes/themegroup)"/> groups)</h2>
      <ul>
        <xsl:apply-templates mode="header" select="/themes/themegroup[@name != 'virtual']"/>
        <xsl:apply-templates mode="header" select="/themes/themegroup[@name = 'virtual']"/>
      </ul>
      </div>
      <div>
        <h2><a href="#mappings">Mappings</a> (<xsl:value-of select="count(/themes/mappings/mapping)"/> mappings)</h2>
      </div>
      <hr/>
      <div>
        <xsl:apply-templates mode="theme-groups" select="/themes/themegroup[@name != 'virtual']"/>
        <xsl:apply-templates mode="theme-groups" select="/themes/themegroup[@name = 'virtual']"/>
      </div>
      <xsl:apply-templates mode="mappings" select="/themes/mappings"/>
    </body>
    </html>
  </xsl:template>

  <xsl:template match="text()"/>
  <xsl:template match="text()" mode="header"/>
  <xsl:template match="text()" mode="theme-groups"/>
  <xsl:template match="text()" mode="theme-list"/>
  <xsl:template match="text()" mode="sub-themes"/>
  <xsl:template match="text()" mode="theme-fields"/>
  <xsl:template match="text()" mode="theme-inherits"/>
  <xsl:template match="text()" mode="mappings"/>

</xsl:stylesheet>
