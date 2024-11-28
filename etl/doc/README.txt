FLYS-AFT:
"""""""""

Der FLYS-AFT-ETL-Prozessor aktualisiert eine FLYS-Datenbank mithilfe
eines DIPS-XML-Exports und einer AFT-Datenbank in Bezug auf Pegel und
Abflusstafeln.

Vorbedingungen:
---------------

    * Es existiert ein DIPS-XML-Export unter einen erreichbaren Pfad
      im Dateisystem.

    * Es existiert eine AFT-Datenbank mit bekannten Credentials.

    * Es existiert eine FLYS-Datenbank mit bekannten Credentials.

Bau:
----
   * Maven (>= 2) sollte installiert sein und im Pfad liegen.

     $ mvn --version
     Sollte Versionsinformationen ausgeben.

   * Für den Oracle-kompatiblen Bau kann dann folgendes
     aufgerufen werden:

     $ mvn -f pom-oracle.xml clean package assembly:single

     Das Archiv target/river-etl-1.0-SNAPSHOT-bin.tar.gz kann dann an den Ort
     der Installation verschoben und schließlich entpackt werden:

     $ tar xzf river-etl-1.0-SNAPSHOT-bin.tar.gz

   * Folgendes führt das fertige Programm dann aus:

     $ river-etl-1.0-SNAPSHOT/bin/run.sh

Konfiguration:
--------------

Zur Konfiguration wird eine Konfigurations-Datei benötigt. Diese wird
standardmäßig im aktuellen Arbeitsverzeichnis unter dem Namen 'config.xml'
gesucht. Der Pfad zu dieser Datei kann allerdings auch mit der
System-Property config.file gesetzt werden.
Dies geschieht über den Kommandozeilenparameter "-Dconfig.file=/pfad/zur/config.xml"
im Start-Skript bin/run.sh oder als Parameter zu diesem Skript.
Als zweiter Parameter kann eine log4j-Konfigurations-Datei gegeben werden.

Die Konfigurationsdatei hat folgende Struktur:

 1 <?xml version="1.0" encoding="UTF-8"?>
 2 <sync>
 3   <!-- If modified send messages -->
 4   <notifications>
 5     <notifaction url="http://example.com">
 6       <caches>
 7         <cache name="my-cache"/>
 8       </caches>
 9     </notifaction>
10   </notifications>
11   <!-- The path to the DiPs file -->
12   <dips>
13     <file>/the/path/to/the/dips/file</file>
14     <repair>/the/path/to/the/xslt/to/repair/dips</repair>
15   </dips>
16   <!-- The FLYS side -->
17   <side name="flys">
18     <db>
19       <driver>oracle.jdbc.OracleDriver</driver>
20       <user>flys</user>
21       <password>flys</password>
22       <url>jdbc:oracle:thin:@//localhost:1521/XE</url>
23     </db>
24   </side>
25   <!-- The AFT side -->
26   <side name="aft">
27     <db>
28       <driver>oracle.jdbc.OracleDriver</driver>
29       <user>aft</user>
30       <password>aft</password>
31       <url>jdbc:oracle:thin:@//localhost:1521/XE</url>
32       <execute-login>
33         <statement>ALTER SESSION SET CURRENT_SCHEMA=AFT</statement>
34       </execute-login>
35     </db>
36   </side>
37 </sync>

Sie besteht aus vier Bereichen:

  * DIPS:
    Zeile 13: Pfad zur XML-Datei mit dem DIPS-Export
    Zeile 14: Pfad zur Reparatur-XSL-Transformation (s.u.).
               Dieser ist optional.
  * FLYS:
    Zeile 19: JDBC-Treiber für den Zugriff auf die FLYS-Datenbank
    Zeile 20: DB-Nutzername
    Zeile 21: Connection-URL zur FLYS-Datenbank

  * AFT:
    Zeile 28: JDBC-Treiber für den Zugriff auf die AFT-Datenbank
    Zeile 29: DB-Nutzername
    Zeile 30: Connection-URL zur AFT-Datenbank

  * Schemata:
    Zeile 32-34: Schema in welches die Daten geschrieben werden sollen.

  * Benachrichtigungen:
    Zeile    5: URL des Web-Service, der benachrichtigt werden soll.
    Zeile 6-18: Die Nachricht, die an den Web-Service verschickt werden soll.

Funktionsweise:
---------------

    Als erstes wird die DIPS-Datei geladen. Ist angegeben, dass
    eine Reparatur-XSL-Transformation auf diese angewendet werden
    soll, wird diese ebenfalls geladen und auf das DIPS-Dokument
    angewandt.

    !!! Hinweis: Unter doc/repair.xsl findet sich eine Beispiel-Transformation,
    !!! die mithilfe von doc/pegelstationen.xml für die Flüsse
    !!! Saar, Mosel und Elbe die Pegelnummern der FLYS-Pegel
    !!! auf die Pegelnummern von Pegel-Online anpasst.

    Die so vor-behandelten DIPS-Daten werden mit der AFT-Datenbank
    verbunden. Verbindungspunkt ist hierbei die Pegelnummer
    ("NUMMER" in DIPS, "MESSSTELLE.MESSSTELLE_NR" in AFT), die
    in beiden Systemen gleich sein muss.

    Wurde für einzelne Pegel die Verbindung zwischen AFT und DIPS
    erfolgreich hergestellt, wird versucht mit der entsprechenden
    Pegelnummer auch eine Verbindung zu FLYS herzustellen.

    Werden Pegel in AFT und DIPS gefunden, die sich nicht in FLYS befinden,
    werden diese in FLYS angelegt (mit Station "STATIONIERUNG",
    Pegelnullpunkt "PNP" und Einzugsgebietsgröße
    "EINZUGSGEBIET_AEO" aus DIPS) und mit den Abflusstafeln aus AFT
    gefüllt.

    Werden Pegel in AFT, DIPS und FLYS gefunden, so werden die Abflusstafeln
    in FLYS mithilfe von AFT aktualisiert. Die Verbindung der Abflusstafeln
    wird über deren Bezeichner hergestellt:

       AFT:  "ABFLUSSTAFEL.ABFLUSSTAFEL_BEZ"
       FLYS: "discharge_tables.description"

    Für alle vorhandenen Paare von AFT/FLYS-Abflusstafeln werden
    die W/Q-Werte abgeglichen und FLYS entsprechend aktualisiert.
    Abflusstafeln, die in FLYS noch nicht vorhanden sind, werden
    in FLYS übernommen.

    Um Inkonsistenzen in FLYS zu vermeiden, müssen zusätzlich bestimmte
    Bedingungen erfüllt sein, damit der Abgleich korrekt stattfinden kann.
    So muss etwa der Name des Gewässers in DIPS ("GEWAESSER") auch im Namen des
    Gewässers in FLYS ("rivers.name") enthalten sein, sowie der Pegel an einer
    für das Gewässer gültigen Station ("STATIONIERUNG" in DIPS) liegen.
    Die im Folgenden dokumentierten
    Fehlermeldungen geben über derartige Probleme Auskunft.

    Wenn es nach dem Abgleich der AFT- und FLYS-DB eine Veränderung
    in FLYS gegeben hat, können an konfigurierbare Web-Dienste
    Nachrichten verschickt werden, dass sich Daten geändert haben.
    Die FLYS-Applikation selbst besitzt einen Dienst, der aufgerufen
    werden kann, um dessen interne Caches zu invalidieren.
    Dies vermeidet Dateninkonsistenzen.

Fehlermeldungen:
================

Während die Synchronisationsprozesses können verschiedene Fehler
auftreten.

Allgemein:
----------

SYNC: syncing failed.

    Während der Synchronisation ist ein Fehler aufgetreten. Details
    finden sich in der Regel oberhalb dieser Fehlermeldung.

REPAIR: Cannot open DIPS repair XSLT file.

    Die zur Reparatur angegebene XSL-Transformation konnte nicht geladen
    werden.

REPAIR: Fixing DIPS failed.

    Die Anwendung der XSL-Transformation zur Reparatur der DIPS-Daten
    ist fehlgeschlagen. Details hierzu sollten sich oberhalb dieser
    Fehlermeldung zu finden sein.

Benachrichtigung:
-----------------

NOTIFY: Invalid URL '<URL>'. Ignored.

    Die zur Benachrichtigung angegebene URL ist nicht valide und
    wird daher ignoriert.

NOTIFY: '<URL>' is not an HTTP(S) connection.

    Die zur Benachrichtigung angegebene URL öffnet keine
    HTTP- bzw. HTTPS-Verbindung.

NOTIFY: Sending message to '<URL>' failed.

    Der Versand der Benachrichtigung an die URL ist fehlgeschlagen.

DIPS:
-----

DIPS: MESSSTELLE '<NAME>' not found in DIPS. Gauge number used for lookup: <NUMMER>

    Es wurde vergeblich versucht, mithilfe einer AFT-Pegelnummer in DIPS
    ein entsprechendes Gegenstück zu finden.

DIPS: MESSSTELLE '<NAME>' is assigned to river '<FLUSS1>'. Needs to be on '<FLUSS2>'.

    Aus Sicht von AFT wird Messstelle <NAME> an <FLUSS2> erwartet.
    DIPS ordnet sie aber <FLUSS1> zu.

DIPS: Gauge '<PEGEL>' has no datum. Ignored.

    Der DIPS-Pegel <PEGEL> hat keinen PNP und kann deshalb nicht
    importiert werden.

DIPS: Setting AEO of gauge '<NAME>' to zero.

    Der AEO-Wert ist bei dem DIPS-Pegel <NAME> nicht gesetzt und
    wird mit Null angenommen.

DIPS: Setting station of gauge '<NAME>' to zero.

    Der DIPS-Pegel '<NAME>' hat keine zugeordnete Stationierung und
    es wird angenommen, dass dieser an km 0 liegt.

DIPS: Station of gauge '<NAME>' is zero.

    Im Regelfall ist ein Stationierung an km 0 ein Datenfehler.

DIPS: Cannot find '<DATEINAME>'.

    Der Pfad zum XML-Dokument mit den DIPS-Daten konnte nicht gefunden
    werden.

DIPS: Cannot load DIPS document.

    Das XML-Dokument mit den DIPS-Daten konnte nicht geladen werden.

DIPS: '<NAME2>' collides with '<NAME1>' on gauge number <NUMMER>.

    In DIPS gibt es zwei Pegel mit NAME1 und NAME2, die dieselbe Pegelnummer
    haben.

DIPS: Gauge '<NAME>' has invalid gauge number '<NUMBER>'.

    Der DIPS-Pegel Name hat eine Pegelnummer <NUMMER>, die sich nicht
    in einen 64bit-Integer verwandeln lässt.

DIPS: Skipping Gauge: '<NAME>' because it is at Station: <pos> and the
river is limited to: <fromkm> - <tokm>

    Der DIPS Pegel wurde nicht eingelesen da seine Stationierung
    nicht mit den Fluss Kilometern in FLYS übereinstimmt. In einer
    darauf folgenden Meldung wird geloggt das dieser Pegel nicht in
    DIPS vorhanden ist (da er nicht eingelesen wurde).

AFT:
----

AFT: ABFLUSSTAFEL_NR = <NUMMER>: <GUELTIG_VON> > <GUELTIG_BIS>. -> swap

    Eine AFT-Abflusstafel hat vertauschte GUELTIG_VON- und GUELTIG_BIS-Werte.
    Diese werden implizit in die zeitlich richtige Reihenfolge gebracht.

FLYS/AFT: Value duplication w=<W> q=<Q>. -> ignore.

    Beim Laden einer Abflusstafel wurden ein W/Q-Duplikat entdeckt
    und ignoriert.

AFT: Invalid MESSSTELLE_NR for MESSSTELLE '<NAME>':

    Die Messtellen-Nummer für die Messtelle <NAME> ist ungültig.
    Erwartet wird ein String, der sich in einen 64bit-Integer umwandeln lässt.

AFT: Found discharge table '<BESCHREIBUNG>' with same description. -> ignore.

    In AFT wurde eine Abflusstafel gefunden, die die gleiche Bezeichnung
    trägt wie eine andere, die demselben Pegel zugeordnet ist. Somit
    ist keine eindeutige Zuordnung möglich.

FLYS:
-----

FLYS: Found discharge table '<BESCHREIBUNG>' with same description. -> ignore

    In FLYS wurde eine Abflusstafel gefunden, die die gleiche Bezeichnung
    trägt wie eine andere, die demselben Pegel zugeordnet ist. Somit
    ist keine eindeutige Zuordnung möglich.

FLYS: Gauge '<PEGEL>' has no official number. Ignored.

    Der Pegel <PEGEL> in FLYS hat keinen Pegelnummer und wird deshalb
    nicht in Betracht gezogen.

FLYS: Gauge '<PEGEL>' number is not found in AFT/DIPS.

    Der Pegel <PEGEL> hat eine Pegelnummer, die aber nicht in AFT/DIPS
    zu finden ist.

FLYS: discharge table <ID> has no description. Ignored.

    Die Abflusstafel in FLYS hat keine Beschreibung. Diese wird
    allerdings zum Abgleich mit DIPS/AFT benötigt.

FLYS: Found discharge table '<BESCHREIBUNG>' with same description. -> ignore

    In FLYS wurde eine Abflusstafel gefunden, die die gleiche Bezeichnung
    trägt wie eine andere, die demselben Pegel zugeordnet ist. Somit
    ist keine eindeutige Zuordnung möglich.

FLYS: Gauge '<PEGEL>' has no official number. Ignored.

    Der Pegel <PEGEL> in FLYS hat keinen Pegelnummer und wird deshalb
    nicht in Betracht gezogen.

FLYS: Gauge '<PEGEL>' number is not found in AFT/DIPS.

    Der Pegel <PEGEL> hat eine Pegelnummer, die aber nicht in AFT/DIPS
    zu finden ist.

FLYS: discharge table <ID> has no description. Ignored.

    Die Abflusstafel in FLYS hat keine Beschreibung. Diese wird
    allerdings zum Abgleich mit DIPS/AFT benötigt.
