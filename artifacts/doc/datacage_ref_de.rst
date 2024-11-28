=========
Datenkorb
=========


.. contents:: Inhalt

Übersicht
---------

Der Datenkorb ist im Kern ein Dive4Elements/River eingebetteter Webdienst,
der dazu dient, ein XML-Dokument zu generieren, das seinerseits benutzt
werden kann, um Daten so identifizierbar zu machen, dass sie zu
Projekten innerhalb von Dive4Elements/River hinzugeladen werden können.

Das vom Datenkorb erzeugte XML-Dokument wird dann vom D4E/River-Client in
eine Baumansicht innerhalb der graphischen Benutzeroberfläche
transformiert.

Die Grundlage für das XML-Doument, das an die Oberfäche gesandt wird,
ist ein XML-Dokument namens ``meta-data.xml``, das im ``conf``-Verzeichnis
des Servers als Template für die Ausgabe-XML benutzt wird.

Dieses Referenzhandbuch beschreibt die Strukturen innerhalb von
``metadata.xml``.

Grundstruktur
-------------

Das Grundgerüst einer validen ``meta-data.xml`` sieht sieh so aus:

.. code:: xml

  <xml version="1.0" encoding="UTF-8">
  <dc:template xmlns:dc="http://www.intevation.org/2011/Datacage">
    <datacage>
       ...
    </datacage>
  </dc:template>

Dies erzeugt folgendes Dokument:

.. code:: xml

  <xml version="1.0" encoding="UTF-8">
  <datacage>
       ...
  </datacage>

Elemente, die nicht aus dem Namespace ``dc`` stammen, werden durchkopiert.
``dc``-Elemente dienen der Steuerung des Templating. Generell kann sagen,
dass das ``meta-data.xml``-Template mittels einer
rekursiven Tiefensuche (siehe http://de.wikipedia.org/wiki/Tiefensuche)
über seine Elemente abgearbeitet wird.

Daten
-----
Im Datenkorb stehen zwei Arten von Daten zur Auswertung zur Verfügung.

``Datensätze``
~~~~~~~~~~~~~~
Datensätze sind Tabellen mit benannten Spalten. Sie werden von Datenquellen
erzeugt und in einem Stapel verwaltet. Die konkreten Werte stehen erst durch
Auffaltung als Variablen zur Verfügung.

``Variablen``
~~~~~~~~~~~~~
Variablen sind die Werte, die aktuell zur Auswertung zur Verfügung stehen. Sie haben
einen Namen und einen Typ (String, Zahl, durch Datenquelle bestimmt).

Textersatz
----------
Um die Wertebelegungen in das Ausgabedokument schreiben zu kann an entsprechenden
Stellen eine String-Interpolation durchgeführt werden.
Hier finden zwei Arten von Textersatz ihren Einsatz.

``${}-Ersetzungen``
~~~~~~~~~~~~~~~~~~~
Die ältere Variante. Innerhalb von Strings werden hier nur Variablen aufgelöst.
Beispiel: "**Hallo, ${planet}!**" wird bei planet = 'Welt' zu "**Hallo, Welt!**".

``{$}-Ersetzungen``
~~~~~~~~~~~~~~~~~~~
Die neuere Variante, allerdings noch nicht komplett an allen Stellen einsatzfähig.
Innerhalb der geschweiften Klammern können beliebige XPath-Ausdrück stehen, die
zu Strings evaluiert werden. Dies orientiert sich an den String-Auswertungen in XSLT.
"**{ concat($greet, ', ', $planet, '!') }**" wird bei gesetzten Variablen greet = Hallo
und planet = "Welt" zu **Hallo, Welt!** ausgewertet.

Datenkorb-Elemente
------------------

Datensätze erzeugen
~~~~~~~~~~~~~~~~~~~

``dc:context`` Aufspannen eines Gültigkeitsbereiches eines Datenbankdatensatz
.............................................................................

.. code:: xml

    <dc:context connection="Verbindung">
      ...
    </dc:element>

Spannt einen Gültigkeitsbereich für einen Datensatz auf, der aus der Datenbank
kommt. Die adressierte Datenbank wird über das optionale Attribut 'connection'
bestimmt. Zur Zeit sind drei Datenbanken angebunden:

- **user**: Die Index-Datenbank des Artefakt-Servers. Hierüber stehen Meta-Informationen
  zu alten Berechnungen zur Verfügung.

- **system**: Die FLYS-Datenbank mit den hydrologischen, morphologische und geodätischen
  Daten.

- **seddb**: Die Sediment-Datenbank.

In einem `<dc:context>`-Element wird zwingend ein `<dc:statement>`-Element erwartet,
um einen Datensatz aus der Datenbank zu holen. Dieser Datensatz steht dann innerhalb des
`<dc:context>`-Elementes ein oder mehrfach zur Verfügung. Innerhalb eines
`<dc:context>`-Elementes können weitere `<dc:context>`-Elemente eingeschachtelt werden.
Ist kein 'connection'-Attribut vorhanden, wird die Datenbank-Verbindung des umgebenen
Kontextes weiterbenutzt. Initial wird je nach Aufrufart des Datenkorbs entweder
der **user** oder **system**-Kontext angenommen. Dies richtet sich danach, ob in
der Aufrufumgebung ein Artefakt gesetzt wird oder nicht. Im ersten Fall wird
angenommen, dass es sich um einen Ladevorgang für zusätzliche Daten handelt, die
auch alte Berechnungen mit einschließt.


``dc:statement`` Holen eines Datensatzes aus einer Datenbank
............................................................

.. code:: xml

    <dc:statement>
      SQL-Select-Statement.
    </dc:statement>

Mittels eines SQL-Select-Statements werden in einem `<dc:context>` Elemente aus
der dort definierten Datenbank geholt. Die Spaltennamen der Projektion des SQL-Statements
werden übernommen und später beim Auffalten des Datensatzes benutzt.

``dc:container-context`` Tabellarische Daten aus der Programm-Umgebung
......................................................................

.. code:: xml

    <dc:container-context container="Container">
       ...
    </dc:container-context>

Dies spannt einen Kontext auf einem Container namens Container auf,
einer listen-ähnlichen Struktur, die als externe Datenquellen in
Form eines Java-Objekts an den Datenkorb übergeben werden kann.
Dies müssen Klassen sein, die das Interface ``java.util.Collection``
implementieren. Mit diesem Konstrukt ist es möglich, tabellarische
Ergebnisse von außen in die Auswertung des Datenkorbs zu integrieren.
Der Container muss hierfür über die Variable ``Container`` zur Verfügung
gestellt werden. Die Spalten des Tabelle werden über ``dc:properties``
definiert. Der Datensatz selbst kann wie gewohnt mittels ``dc:for-each``
realisiert werden.

``dc:properties`` Spaltendefinitionen für externe Daten aus der Programm-Umgebung
.................................................................................

.. code:: xml

    <dc:container-context container="Container">
       <dc:properties>
          <dc:property name="name" alias="alias"/>
       </dc:properties>
    </dc:container-context>

Muss innerhalb eines ``dc:container-context`` angegeben werden und enthält eine
Liste von Attributen der Java-Objekte, die in den Datenkorb eingeblendet werden sollen.
Die Auflösung der Namen auf der Java-Seite funktioniert nach Konvention von Java-Beans,
sprich aus ``name`` wird ``getName``. ``alias`` ist optional und weisst explizit einen
Spaltennamen aus. Dies entspricht im Wesentlichen einen 'AS' bei einem SQL-Statement.

Ein vollständigeres Beispiel könnte in etwa so aussehen.
Die Java-Seite:

.. code:: java

   public class Car {

      private String marke;
      private String name;

      public Car(String marke, String name) {
          this.marke = marke;
          this.name = name;
      }

      public String getMarke() {
          return marke;
      }

      public String getName() {
          return name;
      }
   }

   // Liste befuellen.

   java.util.Container<Car> container = new java.util.ArrayList<Car>();

   container.add(new Car("Volvo", "V80"));
   container.add(new Car("Ferrari", "Testarossa"));

   // Dem Datenkorb uebergeben.

   parameters.put("CARS", cars);

Das Datenkorb-Schnipsel

.. code:: xml

    <dc:container-context container="cars">
       <dc:properties>
          <dc:property name="marke" alias="brand" />
          <dc:property name="name" alias="type" />
       </dc:properties>

       <cars>
         <dc:for-each>
           <car type="$type" brand="$brand" />
         </dc:for-each>
       </cars>
    </dc:container-context>

liefert dann folgende Ausgabe:

.. code:: xml

   <cars>
     <car type="V80" brand="Volvo" />
     <car type="Testarossa" brand="Ferrari" />
   </cars>



``dc:virtual-column`` Virtuelle Spalten definieren
..................................................

.. code:: xml

    <dc:virtual-column name="Name" type="Type" expr="XPath-Ausdruck">
      ...
    </dc:virtual-column>

Definiert eine neue, virtuelle Spalte namens Name für den aktuellen Datensatz.
Der Typ wird optional durch Typ festgelegt; ohne Angabe wird der Typ String angenommen.
Die Wertebelegung ergibt sich aus der zeilenweisen Auswertung des XPath-Ausdrucks.

Dies sei an folgendem Beispiel illustriert:

 +----+
 +Zahl+
 +====+
 +   1+
 +----+
 +   2+
 +----+
 +   3+
 +----+

.. code:: xml

    <dc:virtual-column name="Quadrat" type="number" expr="$Zahl * $Zahl">
      ...
    </dc:virtual-column>

Erzeugt einen neuen Datensatz folgender Struktur:

 +----+-------+
 +Zahl+Quadrat+
 +====+=======+
 +   1+      1+
 +----+-------+
 +   2+      4+
 +----+-------+
 +   3+      9+
 +----+-------+

Innerhalb eines `virtual-column`-Elementes kann der Datensatz mittel `dc:for-each` wie
gewohnt realisiert werden. Wird das `dc:virtual-column`-Element verlassen, gilt wieder
der vormalige Datensatz ohne die zusätzlich Spalte.

Datensätze einschränken
~~~~~~~~~~~~~~~~~~~~~~~

``dc:filter`` Selektieren einzelner Zeilen aus dem aktuellen Datensatz
......................................................................

.. code:: xml

    <dc:filter expr="XPath-Ausdruck">
      ...
    </dc:filter>

Evaluiert pro Zeile des aktuellen Datensatzes den XPath-Ausdruck 'expr' und erzeugt
einen neuen Datensatz, der nur die Zeilen des Originaldatensatzes enthält für den
die Auswertung logisch wahr ergab. Im XPath-Ausdruck stehen die aufgefalteten Werte
der jeweiligen Zeile zur Verfügung.

Folgends Beispiel soll dies verdeutlichen:

 +----+
 +Zahl+
 +====+
 +   1+
 +----+
 +   2+
 +----+
 +   3+
 +----+
 +   4+
 +----+

Zahlen erhalten, die einen Wert kleiner drei haben.

.. code:: xml

    <dc:filter expr="$Zahl &lt; 3">
      ...
    <dc:filter>

Dies erzeugt folgenden neuen Datensatz:

 +----+
 +Zahl+
 +====+
 +   1+
 +----+
 +   2+
 +----+

Innerhalb des `dc:filter`-Elementes kann der neue Datensatz wie gewohnt mit `dc:for-each`
realisiert werden. Nach Verlassen des `dc:filter`-Elementes ist wieder der vormalige
Datensatz aktuell.

.. _dc:group:

``dc:group`` Datensätze in Gruppen ordnen
..........................................

.. code:: xml

    <dc:group expr="XPath-Ausdruck">
      ...
    </dc:group>

Pro Zeile des Originaldatensatzes wird der XPath-Ausdruck 'expr' ausgewertet.
Wie bei `dc:filter` stehen hier die aufgefalteten Werte der einzelnen Spalten zur
Verfügung. Das Resultat der XPath-Auswertung wird als Schlüssel für zu erzeugende
Gruppen benutzt, denen dann die Zeilen zugeordnet werden.
Nachdem alle Zeilen ihren entsprechenden Gruppen zugeordnet wurden, wir der
Inhalt des `dc:group`-Elements für jede Gruppe durchlaufen. Innerhalb des
`dc:group`-Elementes steht der jeweilige Schlüssel der aktuellen Gruppe über
die Funktion `dc:group-key()` zur Auswertung zu Verfügung. Innerhalb der
jeweiligen Gruppen kann mittels `dc:for-each` der jeweilige Unterdatensatz
realisiert werden.

Zur Verdeutlichung folgendes Beispiel:

 +-------+-----------+
 +Marke  +Bezeichnung+
 +=======+===========+
 +Ferrari+Testarossa +
 +-------+-----------+
 +Volvo  +V40        +
 +-------+-----------+
 +Volvo  +780        +
 +-------+-----------+
 +Ferrari+F40        +
 +-------+-----------+
 +VW     +Käfer      +
 +-------+-----------+

.. code:: xml

    <marken>
      <dc:group expr="$Marke">
        <marke name="{dc:group-key()}">
           <dc:for-each>
              <bezeichnung name="$Bezeichnung"/>
           </dc:for-each>
        </marke>
      </dc:group>
    </marken>

Dies führt zu folgender Ausgabe:

.. code:: xml

    <marken>
      <marke name="Ferrari">
        <bezeichnung name="F40"/>
        <bezeichnung name="Testarossa"/>
      </marke>
      <marke name="Volvo">
        <bezeichnung name="V40"/>
        <bezeichnung name="780"/>
      </marke>
      <marke name="VW">
        <bezeichnung name="Käfer"/>
      </marke>
    </marken>

Der Expr-Ausdruck kann mittels der Zeichenfolge '#!#' innerhalb des Ausdrucks
in mehrere Ausdrücke zerlegt werden. Damit ist es möglich, einen Wert
in mehrere Kategorien einzusortieren. Wird z.B. ein Film über den Zeitraum
von zwei Jahren gedreht und man möchte eine Liste von Filmen nach ihren
Produktionsjahren gruppieren, sollte der Film im Resulat in beiden Jahren auftauchen.

.. code:: xml

    <dc:group expr="$start_jahr #!# $end_jahr">

Datensätze auffalten
~~~~~~~~~~~~~~~~~~~~

``dc:for-each`` Realisieren eines Datensatzes
.............................................

.. code:: xml

    <dc:for-each>
      ...
    <dc:for-each>

Erzeugt nacheinander alle zeilenweisen Realisationen des aktuellen Datensatzes. Die
einzelnen Spaltenwerte sind dann über Variablen erreichbar, die nach den Spaltenbezeichnern
des Datenstzes benannt sind.

Folgendes Beispiel soll den Sachverhalt illustieren:

 +----+
 +Zahl+
 +====+
 +   1+
 +----+
 +   2+
 +----+
 +   3+
 +----+

.. code:: xml

    <zahlen>
      <dc:for-each>
        <zahl wert="$Zahl"/>
      <dc:for-each>
    </zahlen>

Dies erzeugt folgende Ausgabe:

.. code:: xml

    <zahlen>
        <zahl wert="1"/>
        <zahl wert="2"/>
        <zahl wert="3"/>
    </zahlen>

``dc:iterate`` **TODO**

Bedingte Ausführung
~~~~~~~~~~~~~~~~~~~

``<dc:if>`` Einfaches Wenn-Dann ohne Sonst-Fall
...............................................

.. code:: xml

    <dc:if test="XPath-Ausdruck">
      ...
    </dc:if>

Der innere Teil wird nur dann betreten, wenn der XPath-Ausdruck zu
logisch wahr evaluiert wird. Dieses Konstrukt kennt keinen alternativen
Pfad, der betreten wird, falls der Ausdruck zu logisch falsch ausgewertet
wird. Wird dies benötigt, muss man ``<dc:choose>`` benutzen.
``<dc:if>`` ist in Symmetrie zu ``<xsl:if>`` von XSLT entworfen worden.

``<dc:choose>`` Ketten von Wenn-Dann-Ausdrücken
...............................................

.. code:: xml
    
    <dc:choose>
       <dc:when test="XPath-Ausdruck 1"> ... </dc:when>
       <dc:when test="XPath-Ausdruck 2"> ... </dc:when>
       ...
       <dc:otherwise> ...  </dc:otherwise>
    </dc:choose>

Es werden der Reihe nach von oben nach unter die ``test``-XPath-Ausdrücke der ``dc:when``-Elemente ausgewertet.  Evaluiert ein Ausdruck zu logisch wahr, wird der innere Teil des entsprechenden ``<dc:when>``-Elements betreten. Die verbliebenen
``<dc:when>``- und ``<dc:otherwise>``-Elemente werden dann ignoriert. Evaluiert
keiner der ``test``-Ausdrücke zu wahr, wird der innere Teil des
``<dc:otherwise>``-Elements betreten.
``<dc:choose>`` ist in Symmetrie zu ``<xsl:choose>`` von XSLT entworfen worden.



Makros
~~~~~~
Um innerhalb des Definitionsdokumentes Wiederholungen zu vermeiden, können sogenannte
Makros definiert werden, die dann von anderer Stellen aus eingefügt werden können.

``dc:macro`` Wiederverwendbare Teile definieren
...............................................

.. code:: xml

    <dc:macro name="Name">
      ...
    </dc:macro>

Definiert ein Makro namens Name. Nach der Definition ist dieses dann unter diesem
Namen global innerhalb des Definitionsdokumentes bekannt. Makros können geschachtelt
werden. Auch eingeschachtelte Makros sind global sichtbar. Die Makrodefinition und
ihre eingeschalteten Elemente werden nicht in das Ausgabedokument übernommen.

``dc:call-macro`` Makros aufrufen

.. code:: xml

    <dc:call-macro name="Name">

Ruft ein Makro names Name auf. Dieses muss mit `dc:macro` definiert sein. Die Reihenfolge
von Definition und Aufruf ist egal.

``dc:macro-body`` Elemente an ein Makro übergeben
.................................................

.. code:: xml

    <dc:macro name="Name">
      ...
      <dc:macro-body/>
      ...
    </dc:macro>

Um an Makros weitere Bausteine als Argument übergeben zu können, ist es optional
möglich innerhalb einer Makrodefinition ein Element `dc:macro-body` einzufügen.
Dieses Element expandiert sich zum Inhalt des `dc:call-macro`-Aufrufs.

.. code:: xml

    <dc:call-macro name="Name">Inhalt von dc:macro-body</dc:call-macro>

Zur Verdeutlichung ein konkretes Beispiel

.. code:: xml

    <dc:macro name="Greetings">
      <Hallo>
          <dc:macro-body/>
      </Hallo>
    </dc:macro>

    <dc:call-macro name="Greetings">Welt</dc:call-macro>
    <dc:call-macro name="Greetings">Mond</dc:call-macro>

Dies produziert folgende Ausgabe

.. code:: xml

    <Hallo>Welt</Hallo>
    <Hallo>Mond</Hallo>

Das Haupteinsatzgebiet dieses Konstruktes ist die transparente Bereitstellung
von Kontexten, die dann verschiedentlich ausgewertet werden sollen.

Sonstige Elemente
~~~~~~~~~~~~~~~~~

``dc:element`` Hinzufügen neuer Elemente in der Ausgabe
.......................................................

.. code:: xml

    <dc:element name="Name">
      ...
    </dc:element>

Erzeugt ein Element namens Name. Für den Namen gelten die `${}-Ersetzungen`_.


``dc:attribute`` Hinzufügen neuer Attribute zum umgebenden Ausgabeelement
.........................................................................

.. code:: xml

    <dc:attribute name="Name" value="Wert"/>

Fügt dem umgebenden Ausgabeelement ein weiteres Attribut namens Name mit dem
Wert von Wert hinzu. Für Namen und Wert gelten die `${}-Ersetzungen`_.
Der Einsatz dieses Konstrukts findet häufig im Zusammenhang mit dc:element
seinen Einsatz, wenn es ein Ausgabeelement vollständig aus Variablenbelegungen
erstellt werden soll.

``dc:comment`` Kommentare im Beschreibungsdokument
..................................................

.. code:: xml

    <dc:comment>
      ...
    </dc:comment>

Ein Kommentar auf Ebene des Beschreibungsdokumentes, das keinerlei Ausgabe
im Ausgabedokument erzeugt. Eingeschachtelte Elemente werden ebenfalls nicht ausgewertet.
Im Gegensatz dazu werden die XML-typischen **<!-- Kommetare -->** in das Ausgabedokument übernommen!


``dc:message`` Ausgabe auf die Diagnoseausgabe (Log)
....................................................

.. code:: xml

    <dc:message>
      Text für die Diagnose.
    </dc:message>

Gibt den Text innerhalb des Elementes aus Ausgabe im Log aus. Dies dient in erster Linie
dem Nachvollziehen von Aufrufen innerhalb des Datenkorbdokumentes. Für den Text gelten
die `{$}-Ersetzungen`_.

``dc:variable`` Erzeugung einer kontext-lokalen Variablen
.........................................................

.. code:: xml

    <dc:variable name="Name" type="Typ" expr="XPath-Ausdruck"/>

Legt im aktuellen Kontext eine lokale Variable namens Name an. Diese hat den
Typ Typ und entsteht durch Auswertung des XPath-Ausdruck expr. Der Typ
ist optional. Wird dieser nicht gesetzt, wird das Ergebnis als String interpretiert.
Alternativ können hier die Werte 'number' für Zahlen, 'bool' für Boolean-Werte
benutzt werden. Für den Namen und den Typ gelten die `${}-Ersetzungen`_.
Wird der aktuellen `dc:context` verlassen, ist diese Variable nicht mehr definiert.


``dc:convert`` kontext-lokale Konvertierung von Variablen
.........................................................

.. code:: xml

    <dc:convert name="Name" type="Typ"/>

Konvertiert die Variable namens Name für die Gültigkeit des aktuellen Kontextes in
einen anderen Typ. Für Name und Typ gelten die `${}-Ersetzungen`_. Für die
Typen gilt das gleiche wie für die Typen von `dc:variable`.


Datenkorb-Funktionen
--------------------

``dc:has-result`` Prüfen, ob der aktuelle Datensatz nicht leer ist
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:has-result()`` liefert logisch wahr zurück, falls der aktuelle
Datensatz nicht leer ist, sprich Datenzeilen enthält. Ansonsten wird
logisch falsch zurück gegeben. Mittels dieser Funktion können leere
Knoten im Resultatdokument verhindert werden. Die typische Nutzung
ist daher innerhalb des ``test``-Attributs eines ``<dc:if>``-Elements.

``dc:contains`` Prüfen, ob Suchbegriff in einer Liste vorhanden ist
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:contains(Heuhaufen, Nadel)`` prüft ob das Suchobjekt Nadel in
der Liste Heufhaufen zu finden ist. Liefert logisch wahr falls die
Nadel gefunden wurde, ansonsten logisch falsch. Typischer Anwendungsfall:
``dc:contains($outs, 'longitudinal-section')`` prüft, ob in der Liste
der aktuellen Outs der Eintrag 'longitudinal-section' zu finden ist.

``dc:replace`` Textersatz in Strings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:replace(Heuhaufen, Nadel, Ersatz)`` ersetzt in im String Heuhaufen alle
Vorkommen des Strings Nadel durch den String Ersatz. ``dc:replace('Banane', 'a', 'e')``
resultiert folglich in dem String ``Benene``.

``dc:replace-all`` Textersatz in Strings mit regulären Ausdrücken
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:replace-all(Heuhaufen, Nadel, Ersatz)`` ersetzt in im String Heuhaufen alle
Vorkommen des regulären Ausdrucks durch den String Ersatz. Auf im regulären Ausdruck
gebildete Gruppen ``()`` kann mit ``$1``, ``$2``, ``$3`` usw. im Ersatztext zurückgegriffen werden.
``dc:replace-all('KlausGabiPeter', '(Klaus|Peter)', '>$1<')`` resultiert demnach
in ``>Klaus<Gabi>Peter<``.

``dc:find-all`` Extraktion von Strings aus einem String anhand eines regulären Ausdrucks
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:find-all(Nadel, Heuhaufen)`` extrahiert aus einem String Heuhaufen all jene Substrings,
die auf den regulären Ausdruck Nadel passen. Das Resultat dieser Funktion ist eine Liste
von Strings. Sie ist leer, wenn das Muster nicht gefunden wurde.

``dc:find-all('\d{4}', '1900 1930 1941 1960')`` liefert dem entsprechend die Liste
``'1900'``, ``'1930'``, ``'1941'``, ``'1960'``.

``dc:date-format`` Formatierte Ausgabe eines Datums
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:date-format(Format, Datum)`` liefert für ein Datum einen String, der
nach den Formatierungsregeln aus Format formatiert wird. Für die Formatierungen
gelten die in SimpleFormat_ beschriebenen Regeln. ``Datum`` kann dabei ein
Datums-Objekt oder eine Zahl sein. Letztere wird als Millisekunden nach dem
1.1.1970 00:00:00 GMT interpretiert.

.. _SimpleFormat: http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html

``dc:date-format('dd.MM.yyyy', $datum)`` liefert für ein ``$datum``
mit dem Wert 1. Dezember 1941 den String ``01.12.1941``.


``dc:group-key`` Wert des aktuellen Gruppierungsschlüssels
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Siehe hierzu `dc:group`_.

``dc:dump-variables`` Aktuelle Variablenbelegung als String
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:dump-variables()`` liefert einen String mit der aktuellen Belegung aller Variablen.
Dies ist zusammen mit ``dc:message`` nützlich, um eine Ablaufverfolgung zu implementieren.

``dc:get`` Dynamische Variablenauswertung
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:get(Variablenname)`` ermöglicht es, eine Variable über ihren Namen als String
auszuwerten. Gegenüber der normalen XPath-Variablenauswertung kann die Variable auch
null liefern. Bis auf diese Ausnahme ist der Aufruf ``dc:get('a')`` identisch zu ``$a``
in XPath-Kontexten.

``dc:max-number`` Bestimmung des Maximums einer Liste von Zahlen
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:max-number($liste)`` Ermittelt aus einer Liste von Zahlen das Maximum. Strings werden dabei in Zahlen umgewandelt.

``dc:max-number(dc:find-all('\d{4}', '1900 1930 1941 1960'))`` liefert also ``1960``.


``dc:min-number`` Bestimmung des Minimums einer Liste von Zahlen
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``dc:min-number($liste)`` Ermittelt aus einer Liste von Zahlen das Minimum. Strings werden dabei in Zahlen umgewandelt.

``dc:min-number(dc:find-all('\d{4}', '1900 1930 1941 1960'))`` liefert also ``1960``.




``dc:fromValue`` **TODO**

``dc:toValue`` **TODO**

``dc:coalesce`` **TODO**

