# Topf Secret – Produktplan

## Produktziel

Ein einfaches, zuverlässiges Android-Kochbuch für zwei Personen. Rezepte und Bilder bleiben lokal auf dem Gerät verfügbar. Vollständige Backups sollen später Rezepte, Metadaten und Bilder gemeinsam sichern und wiederherstellen können.

## Grundsatz: gemeinsames Kochbuch ohne PIN

- Es gibt einen gemeinsamen Rezeptbestand für beide Personen auf dem Gerät.
- Keine separaten privaten Rezeptbereiche und keine persönliche PIN-/Biometrie-Sperre innerhalb der App.
- Keine Wiederherstellungscodes und keine SQLCipher-Schlüsselverwaltung.
- Der Schutz des Geräts erfolgt über die normale Android-Bildschirmsperre.
- Die vorhandene Admin-Aktivierung bleibt davon getrennt bestehen.

## Funktionale Anforderungen

### A. Rezepte
- Titel, Beschreibung, Zutaten, Mengen und Einheiten.
- Zubereitungsschritte, Vorbereitungs-/Kochzeit, Portionenzahl.
- Kategorien, Tags, Favoriten und persönliche Notizen.
- Eigene Bilder und Quellenangabe.

### B. Bilder
- Bilder aus Galerie und Kamera auswählen; zusätzlich passende Gerichtefotos online suchen.
- Bildauswahl dauerhaft dem jeweiligen Rezept zuordnen.
- Bildvorschau in Rezeptliste und Rezeptdetail.
- Lokale Speicherung bzw. dauerhaft gesicherter Zugriff ohne unnötige Berechtigungen.
- Bilder im vollständigen Backup einschließen.
- Online-Suche ausschließlich für Gerichtefotos über Wikimedia Commons; Treffer mit Vorschau sowie Lizenz-/Urheberangaben.
- Ausgewählte Vorschaubilder samt lokalem Quellen-/Lizenzbeleg im privaten App-Speicher ablegen; keine Google-Bildersuche scrapen.

### C. Web-Rezeptimport
- Teilen-Menü (Android Sharesheet) und manuelle URL-Eingabe.
- Strukturierte Rezeptdaten bevorzugt aus Schema.org Recipe lesen.
- Importvorschau mit editierbaren Feldern.
- Erst nach ausdrücklicher Bestätigung speichern.
- Quelle und Bildrechte/Quell-URL dokumentieren; keine Umgehung von Bezahlschranken oder Zugriffsschutz.

### D. Nährwerte
- Energie, Kohlenhydrate, Zucker, Ballaststoffe, Eiweiß und Fett.
- Berechnung aus Zutatenmengen und nachvollziehbaren Nährwertdaten.
- Anzeige pro Gesamt-Rezept, Portion und frei gewählter Portionsanzahl.
- Datenquelle und Schätzstatus sichtbar machen.
- Nutzer kann Zutaten und Nährwerte korrigieren.
- Werte sind Schätzungen und keine medizinische Therapie- oder Insulinempfehlung.

### E. Backup und Wiederherstellung
- Versioniertes ZIP-basiertes `.tsb`-Format.
- Vollständiger Export aller Rezepte, Metadaten und Bilder.
- Optional einzelne Rezepte exportieren/teilen.
- Import mit Vorschau, Validierung und Duplikaterkennung.
- Optional verschlüsseltes Backup; Wiederherstellung ohne Cloud möglich.
- Formatversion und Prüfsummen vorsehen, damit spätere App-Versionen alte Backups migrieren können.

### F. Synchronisation
- Zwei getrennte App-Installationen mit autorisierter Verbindung.
- Synchronisation in beide Richtungen.
- Konflikterkennung bei gleichzeitigen Änderungen.
- Offline-Änderungen lokal puffern und später übertragen.
- Zugriff ausschließlich für eingeladene/verknüpfte Konten.

## Technische Leitlinien

- Kotlin und Jetpack Compose.
- Room 2.8.3 als lokale Datenzugriffsschicht.
- Android 8.0 / API 26 als Mindestziel.
- Repository-/Use-Case-Schicht, damit Datenbank und Synchronisationsanbieter austauschbar bleiben.
- Keine echten Rezeptdaten, privaten Bilder, Backups, Zugangsdaten oder Schlüssel in GitHub committen.
- Die vorhandene Room-Datenbank v1 bleibt erhalten; neue Bildfunktionen verwenden das bereits vorhandene Feld `imageUri`, daher ist derzeit keine Datenbankschema-Migration erforderlich.

## Roadmap

1. Projektgrundlage, UI-Grundgerüst, Navigation und lokale Rezeptverwaltung – umgesetzt.
2. Rezeptbilder aus Galerie und Kamera aufnehmen; Dateien in `filesDir/recipe_images` übernehmen und in Rezeptliste/-detail anzeigen – umgesetzt auf `development`; Build-/Gerätetest und Optimierung noch offen.
3. Online-Gerichtebildersuche über Wikimedia Commons mit Treffer-Vorschau, Lizenz-/Urheberanzeige, lokalem Download und Quellenbeleg – implementiert auf `development`; Galerie, Kamera und Suche wurden vom Nutzer auf dem Gerät erfolgreich ausprobiert. Die Ergebnisqualität ist noch nicht ausreichend: Suche nach „Grünkohl“/„Grünkohl Eintopf“ liefert Gemüse-/Pflanzenmotive und Audio-Dateien statt zubereiteter Gerichte. Suchstrategie und Filter wurden in Commits `12c6c1e` und `0ce220f` verbessert (mehrere Suchvarianten, Bitmap-/MIME-Filter, Ausschluss offensichtlicher Pflanzen-/Audio-/Videotreffer, Titelpriorisierung mit Speisenhinweisen); Build- und Gerätetest stehen aus. Build 27 und 28 scheiterten an ungültigen Kotlin-Labels für den Activity-Context. Korrektur committed: `applicationContext` wird vor `setContent` erfasst und explizit an `TopfSecretApp` übergeben (`15bf57b`).
4. Neue Online-Suchlogik auf dem Gerät testen: Bitmap-/MIME-Filter, Ausschluss von Audio/Video und Pflanzenmotiven, Suchvarianten und Gerichte-Titelpriorisierung; anschließend Bildgrößenoptimierung und Regressionstests für Galerie-, Kamera- und Onlinebilder.
5. Portionen- und Nährwertberechnung erweitern.
6. Vollständiger Backup-/Restore-Workflow inklusive Bilder und Quellenbelege.
7. Web-Rezeptimport mit Vorschau.
8. Sichere Synchronisation für zwei Personen.
9. Geräte- und Regressionstests, Release.

## Noch zu entscheiden

- Ob Backups standardmäßig verschlüsselt oder optional verschlüsselt sein sollen.
- Nährwertdatenquelle und Lizenz.
- Synchronisationsdienst.
- App-Sprache zunächst Deutsch oder mehrsprachig.


## Release-Kennzeichnung

- Manuelle GitHub-Releases tragen künftig den Titel `Topf Secret v<versionName> – Build #<GitHub-Run-Nummer>`.
- Die Release-Beschreibung nennt App-Version, versionCode, Buildnummer und Commit-SHA. So sind installierter Stand und Änderungen leichter zuzuordnen.
- Der am 2026-11-26 geprüfte Bestand enthält `build-21` und `build-29`; beide bisherigen Releases haben noch den generischen Titel „Topf Secret Build“. Neue Releases verwenden das eindeutige Schema.


### G. Barcode-Import und Einheitenberechnung (vereinbart, Umsetzung begonnen)
- Zutatenprodukte per Barcode scannen und Produktname sowie vorhandene Nährwerte aus Open Food Facts übernehmen.
- Nährwerte je Zutat speichern (Wertebasis 100 g oder 100 ml, Barcode und Datenquelle) und bei Mengen-/Portionsänderungen neu berechnen.
- Gesamtwerte des Gerichts durch Addition aller berechenbaren Zutaten bilden; Werte pro Portion anhand der Portionszahl ableiten.
- Mengen normalisieren: mg/g/kg und ml/cl/dl/l; EL/TL/Tasse als definierte, ausdrücklich geschätzte Volumenmaße.
- Volumen-/Gewichtsumrechnung nur mit bekannter lebensmittelspezifischer Dichte; Stück, Prise, Bund und ähnliche Angaben nur mit hinterlegtem Stück-/Referenzgewicht.
- Nicht zuordenbare oder unvollständige Zutaten sichtbar als nicht berechnet kennzeichnen; manuelle Eingabe/Korrektur ermöglichen.
- Open Food Facts API v3, eigener User-Agent, Quellenhinweis und Einhaltung der ODbL-/Datenlizenzbedingungen.


### G.1 Aktueller Implementierungsstand
- Barcode-Scan per Google Code Scanner (ohne eigene Kamera-Berechtigung), Produktabfrage Open Food Facts API v3 und Vorschau/Bestätigung.
- Produktname und verfügbare Nährwerte je 100 g werden mit Barcode und Quellen-URL an der Zutat gespeichert.
- Automatische Addition bekannter Zutaten und Ausgabe Gesamtgericht/pro Portion im Rezepteditor; fehlende Zutaten werden gezählt und angezeigt.
- Konvertierungs- und Parser-Unit-Tests ergänzt. Build und Gerätetest sind noch offen.


### G.2 Produktsuche und Barcode-Fallback (vereinbart und implementiert auf development)
- Manuelle Produktsuche jederzeit in der Zutatenverwaltung; Suchbegriffe dürfen Lebensmittelname oder Marke sein.
- Bei unbekanntem Barcode, fehlendem Produktnamen, Lookup-/Netzwerkfehler oder Scannerfehler automatisch Such-Popup öffnen.
- Treffer aus Open Food Facts mit Produktname, Marke und verfügbaren Nährwerten anzeigen; ausgewähltes Produkt über die bestehende Vorschau übernehmen.
- Bei keinem Treffer den Suchbegriff als normale Zutat ohne Produktdaten übernehmen können. Keine erfundenen Nährwerte.
- Build und Gerätetest stehen noch aus.

### G.3 Lokaler Produktkatalog und Korrektur von Barcode-Zuordnungen (in Umsetzung)
- Lokale Room-Tabelle `local_food_products` (Schema v3) speichert bestätigte Produktdaten je Barcode. Lokale Zuordnungen haben beim Scan Vorrang vor Onlinequellen.
- Manuelle Produktsuche führt lokale Einträge zuerst auf und bleibt mit lokalen Treffern auch bei Offline-/API-Ausfall nutzbar.
- Produktdaten können in der Vorschau korrigiert werden: Barcode, Name, Marke und Makronährwerte. Manuell korrigierte Einträge erhalten den Status `VERIFIED_LOCAL`; Online-Treffer bleiben `UNVERIFIED`, bis sie bewusst lokal geprüft/korrigiert wurden.
- Falsche Barcode-Antworten von Open Food Facts werden abgewiesen, wenn der API-Produktcode vom gescannten Code abweicht.
- Datenquellen-Roadmap für manuelle Suche: Open Food Facts Search-a-licious; BLS 4.0 (kostenlos, CC BY 4.0, besonders relevant für deutsche Lebensmittel); Livsmedelsverket JSON API (CC BY 4.0); USDA FoodData Central (CC0, API-Key erforderlich). UK CoFID als möglicher Offline-Datensatz nach Lizenz-/Formatprüfung. Keine privaten/gebührenpflichtigen Lifesum-Daten oder APIs verwenden.
- Offen: weitere Datenquellen technisch integrieren, deutsche Bezeichnungen/Übersetzungen, Qualitäts-/Plausibilitätsregeln und Katalog-Backup/Restore.


## H. Quellen, Lizenzen und Danksagungen
- Ein Drei-Striche-Menü auf der Startseite bietet den Einstieg zu „Quellen & Danksagungen“.
- Die Seite führt verwendete Datenquellen und Projekte mit Herausgeber, Lizenz und Link auf. Der BLS 4.0 wird mit offizieller Zitierangabe des Max Rubner-Instituts und CC BY 4.0 aufgeführt; Open Food Facts mit ODbL 1.0; Wikimedia Commons mit dateispezifischer Lizenz-/Urheberzuordnung.
- Weitere Datenquellen, Bibliotheken und freiwillige Mitwirkende sollen später ergänzbar sein. Personen werden nur mit ihrer Zustimmung namentlich genannt.
- BLS-Integration: Daten lokal/offline durchsuchbar machen; deutsche Lebensmittelbezeichnungen und Nährwerte übernehmen; Datenherkunft und fehlende Werte erhalten; Quellenstatus in der Zutaten-/Nährwertansicht anzeigen.
- Vor Veröffentlichung der BLS-Datenintegration Lizenzhinweise exakt umsetzen, Änderungen/Aufbereitungen kennzeichnen und klarstellen, dass das Max Rubner-Institut Topf Secret nicht unterstützt oder geprüft hat. Open-Food-Facts-ODbL-Anforderungen separat behandeln.
- Status: Menü und Quellen-/Danksagungsseite auf development angelegt; Build und Gerätetest ausstehend. Der eigentliche BLS-Datenimport ist noch nicht implementiert.


## H. Quellen, Lizenzen und BLS-Import
- Startseiten-Drei-Striche-Menü führt zur Seite „Quellen & Danksagungen“; Quellenkarten nennen BLS 4.0/Max Rubner-Institut, Open Food Facts/ODbL und Wikimedia Commons mit dateispezifischen Bildlizenzen. Weitere Quellen und Personen können ergänzt werden; Personen nur mit Zustimmung.
- BLS 4.0: lokale Room-Tabelle `bls_foods`, Schema v4, Migration 3→4. XLSX-Import aus der offiziellen, vom Nutzer ausgewählten Datei. Importer liest `xl/sharedStrings.xml` und `xl/worksheets/sheet1.xml`, erkennt BLS-Code, deutsche/englische Bezeichnung sowie ENERCC, PROT625, CHO und FAT. Zu den vier gespeicherten Nährwerten bleiben die jeweiligen Datenherkunfts- und Referenzfelder erhalten. Fehlende/nichtnumerische Werte bleiben null.
- Die Rezept-Zutatensuche bietet BLS-Dateiimport und lokale BLS-Suche. Auswahl übernimmt BLS-Grundnahrungsmittel mit Nährwerten je 100 g; BLS-Codes werden als BLS-Code statt als Barcode gekennzeichnet. BLS-Treffer werden nicht als OFF-Barcodeprodukt gespeichert.
- Begrenzung des ersten Schritts: 4 von 138 Nährstoffen werden in die aktuelle Room-Suche übernommen (kcal, Protein, verfügbare Kohlenhydrate, Fett). Die vollständige 138-Komponenten-Übernahme ist noch nicht umgesetzt.
- Offizielle BLS-Dokumentation: 7.140 Lebensmittel, 138 Nährstoffe, je Nährstoff Wert plus Datenherkunft plus Referenz; CC BY 4.0 und MRI-Zitierangabe. Die Lizenzseite nennt Max Rubner-Institut als Herausgeber. Vor Release Import anhand der offiziellen XLSX-Datei prüfen und Quellen-/Änderungshinweis beibehalten.
- Android CI startet jetzt automatisch bei Push auf `development`; GitHub Release bleibt manuell.
- Build und Gerätetest der neuen Datenbankmigration, XLSX-Import- und Suchfunktion stehen noch aus.


### BLS-Migrationsfehler nach Installation (2026-11-26)
- Gerätetest auf Samsung SM-S908B / Android 16 zeigte beim Start: `Migration didn't properly handle: bls_foods`.
- Ursache: Migration 3→4 erstellte `index_bls_foods_germanName`, während `BlsFoodEntity` den Index nicht in `@Entity(indices = ...)` deklarierte. Room erkannte dadurch eine Abweichung zwischen erwartetem und tatsächlichem Schema.
- Korrektur: Entity deklariert jetzt `Index(value = ["germanName"])`, passend zum bestehenden Migrations-SQL. Keine Änderung der Datenbankversion und keine Löschung vorhandener Daten.
- Build und erneuter Gerätetest stehen aus.


## BLS 4.0 – Offline-Vollintegration (Aktualisierung 2026-11-26)

- Der offizielle Datensatz liegt als gzip-komprimiertes JSONL unter `app/src/main/assets/bls_foods_v4.jsonl.gz` und wird ohne Download oder Dateiauswahl direkt aus den App-Assets eingelesen.
- Umfang: 7.140 Lebensmittel und 138 Nährstoffkomponenten je Lebensmittel. Jedes Nährstoff-Tripel (Wert, Datenherkunft, Referenz) bleibt in `nutrientsJson` erhalten; Datensatzhinweise werden in `note` gespeichert. Textmarker wie `&lt;LOQ`, `&lt;LOD` und `-` bleiben im Original-JSON und werden nicht zu Nullwerten.
- Room-Schema v5 ergänzt diese Felder über Migration 4→5. Bestehende App-Daten bleiben erhalten.
- `BlsDatasetSeeder` initialisiert den Datensatz beim App-Start offline und prüft die Vollständigkeit anhand der erwarteten 7.140 Einträge. Die Suche kann die Initialisierung bei Bedarf ebenfalls sicherstellen.
- ENERCC, PROT625, CHO und FAT werden zusätzlich in die bestehenden Makrospalten gespiegelt; alle 138 Komponenten bleiben vollständig verfügbar.
- Der manuelle XLSX-Import wurde entfernt; die BLS-Suche verwendet den gebündelten Offline-Datensatz.
- **Status:** Code auf `development` committed; CI-Build, Migrationstest und Gerätetest sind noch offen. Erst nach erfolgreichen Prüfungen als fertig/verifiziert markieren.

## Verbindliche CI-Trigger-Policy (2026-11-26)

- Android-CI nur bei Pull Requests gegen `development` und manuellem Start über `workflow_dispatch`.
- Kein automatischer Push-Build; ältere anderslautende Angaben in diesem Plan sind überholt.


### BLS-Suche – Build und Updatepfad (2026-11-26)
- Die vereinheitlichte Produktsuche auf `development` führt BLS 4.0 vor lokalen und Online-Treffern aus. Der alte Vorbereitungs-Dauerhinweis wurde entfernt.
- Android CI Build #41 lief erfolgreich und veröffentlichte eine signierte APK. Die BLS-Assetdatei liegt im Android-Standardpfad `app/src/main/assets`; die APK-Größe ist gegenüber dem Build ohne den Datensatz deutlich gestiegen.
- Für ein Update ohne Deinstallation wurde `versionCode` auf 9 und `versionName` auf 0.6.1 erhöht; CI-Release-Metadaten wurden angepasst.
- Noch offen: neuen manuellen CI-Build für Version 0.6.1 starten, APK installieren und BLS-Suche auf dem Gerät mit „Semmelknödel“/„Knödel“ prüfen. Keine Datenbank- oder Rezeptdatenlöschung.


### APK-Prüfung BLS-Asset (2026-11-26)
- Nutzer bestätigt: Version 0.6.1 aus Release `build-42` installiert; Fehler beim Öffnen von `bls_foods_v4.jsonl.gz` bleibt bestehen.
- Git-Tree des Build-42-Commits `28409a0935b575a535efaf33fa47efcfae93927b` enthält `app/src/main/assets/bls_foods_v4.jsonl.gz` mit 3.720.228 Bytes und Blob-SHA `827fc98c0a296d0eb64062f80cd31738f8a20f22`.
- Build #42 war erfolgreich; das beweist allein nicht, dass das Asset in der APK enthalten ist.
- CI-Workflow wurde auf `development` um einen harten APK-Integritätstest ergänzt: `unzip -Z1` muss `assets/bls_foods_v4.jsonl.gz` finden und der aus der APK extrahierte SHA-256 muss `96e3f2aaf98fa538f84dd6d826a36b1a7565e87f2520bb80b2d9b4aa51ec0bbf` entsprechen. Der nächste manuelle Build soll die Frage eindeutig entscheiden.
- Bis zum erfolgreichen APK-Asset-Test und Gerätetest bleibt BLS-Lauffähigkeit unbestätigt. Keine Deinstallation oder Datenlöschung.


### BLS-GZIP-Paketierungsfehler korrigiert (2026-11-26)
- CI-Lauf #43 (45871865372) zeigte: APK enthält den Datensatz unter `assets/bls_foods_v4.jsonl`, nicht unter dem ursprünglichen Namen mit `.gz`. Der Android-Packaging-Schritt hatte die GZIP-Quelldatei expandiert und die Endung entfernt.
- Runtime-Seeder liest jetzt das resultierende Plain-JSONL-Asset direkt; keine zweite GZIP-Dekomprimierung mehr.
- CI prüft Pfad und SHA-256 des entpackten JSONL-Inhalts (`b58c764620ab5b3b28f81aa3d7caca23b4b2665b151642fde809fca25f4890a6`), damit fehlende oder veränderte BLS-Dateien keine Release-APK durchlassen.
- App-Version 0.6.2 / versionCode 10 für sicheres In-Place-Update; keine Rezeptdatenbank- oder Rezeptdatenlöschung.
- Noch offen: nächster manueller CI-Build und anschließender Gerätetest.

- Runtime-Einlesung zusätzlich robust gemacht: Asset-Name `bls_foods_v4.jsonl`; GZIP-Magic-Bytes werden geprüft und je nach tatsächlichem APK-Inhalt wird entpackt oder direkt gelesen. CI prüft den entpackten JSONL-Inhalt unabhängig vom Packaging-Verhalten.

### G.4 Zutatenzeilen und erweiterte Nährwerte (2026-11-26)
- Fehlerursache für sichtbares `\\n` beim Übernehmen eines Suchtreffers gefunden und korrigiert: Produktübernahme fügt nun echte Zeilenumbrüche ein; Eingabe normalisiert literal escapte Zeilenumbrüche.
- Jede Zutatenzeile wird separat analysiert. Nicht berechenbare Zeilen werden mit dem konkreten Grund ausgegeben; Stückmengen werden ohne bekanntes Gewicht nicht geschätzt.
- Nährwertanzeige untereinander: Energie, Kohlenhydrate, davon Zucker, Ballaststoffe, Eiweiß, Fett, davon gesättigte Fettsäuren, Salz. Anzeige auf maximal zwei Nachkommastellen.
- BLS 4.0 nutzt Codes SUGAR, FIBT, NACL und FASAT; OFF nutzt sugars_100g, fiber_100g, salt_100g und saturated-fat_100g. Fehlende Daten bleiben fehlend und werden nicht als Nullwerte dargestellt.
- Room-Schema v6 ergänzt die Nährstofffelder für Zutaten, lokale Produkte und gespeicherte Rezeptwerte per nicht-destruktiver Migration 5→6.
- Rezeptdetail zeigt zusätzliche Nährwerte pro Portion. Teilberechnungen werden als Teilmenge markiert und nicht als vollständige Rezeptwerte gespeichert.
- App-Version 0.6.3 / versionCode 11.
- Tests ergänzt; CI-/Geräteprüfung steht noch aus. Vor Release manuell auf development bauen und Migration mit vorhandenen Rezepten prüfen.

- Nachprüfung ergänzt: Stück-, Packungs- und Haushaltsmaße ohne Referenzgewicht werden mit konkretem Grund als nicht berechenbar ausgewiesen. Aktueller Code-Stand vor dieser Doku-Aktualisierung: `ad56eeba9ae9cacec734b354c8c09518bd000421`; Build #44 gehört zum vorherigen Stand. Neuer manueller CI-Lauf erforderlich.


### G.5 Korrekturen nach Gerätetest: Cursor und gespeicherte Nährwerte (2026-11-26)
- Zutatenfeld nutzt `TextFieldValue`; Produktübernahme aus Barcode-Scanner/Produktsuche setzt den Cursor direkt hinter den neu eingefügten Eintrag in die nächste leere Zeile. Der nächste Such-/Scanvorgang ist damit direkt möglich.
- Rezeptdetail berechnet aus persistierten Zutaten und Nährwerten mit derselben Mengen- und Nährwertlogik wie der Editor erneut Gesamtgericht und Portionen. Nicht berechenbare Zutaten werden als Teilmenge transparent gemacht.
- Falsche manuelle Rückfallwerte werden nicht mehr als automatische Rezeptberechnung ausgegeben, sobald eine automatische Teilberechnung vorliegt. Manuelle Altwerte bleiben nur dann sichtbar, wenn keine Zutat automatisch berechenbar ist.
- Keine Datenbankschemaänderung, keine Datenlöschung. Build, Tests und Gerätetest stehen für diesen Commit noch aus.


### G.6 Nährwertangaben als optionaler Klappbereich (2026-11-26)
- Rezeptdetail: Nährwertblock aus dem oberen Rezeptbereich entfernt und unterhalb von Beschreibung, Zeiten, Zutaten und Zubereitung verschoben. Anzeige standardmäßig eingeklappt; Button blendet den Bereich ein/aus.
- Rezepteditor: automatische Berechnung und optionale manuelle Nährwertfelder liegen ebenfalls in einem standardmäßig eingeklappten Bereich unterhalb der Zubereitung.
- Einheitliche Überschrift „Nährwertangaben“; Werte darin getrennt nach „Gesamtgericht“ und „Pro Portion“.
- Keine Schemaänderung; Build/Tests und Gerätetest stehen noch aus.


### G.7 Eigene Zutaten dauerhaft speichern und priorisieren (2026-11-26)
- Neue Aktion „Zutat hinzufügen“ neben Barcode-Scan und Produktsuche. Das Zutatenfeld steht darunter.
- Manuelle Nährwerterfassung mit Bezugsbasis 100 g oder 100 ml und allen angezeigten Nährwertfeldern; leere Werte bleiben unbekannt.
- Speicherung im lokalen Room-Produktkatalog mit stabiler lokaler Kennung; Einträge bleiben über App-Neustarts hinweg und offline verfügbar.
- Produktsuche priorisiert eigene Einträge vor BLS 4.0, weiteren lokalen Produkten und Online-Treffern. Dieselbe Priorität gilt bei fehlender Internetverbindung.
- Room-Schema auf Version 7 erweitert; Migration 6→7 ergänzt `nutritionBasis` ohne bestehende Rezeptdaten zu löschen.
- Build, Migrationstest und Gerätetest noch offen.


### G.8 Verifikation: Datenbank und Nährwertangaben (2026-11-26)
- Nutzerbestätigung: aktueller Build und Gerätetest erfolgreich. Room-Datenbank inkl. Migration 6→7, BLS 4.0, lokale Produkte und Nährwertberechnung sind als integriert und getestet markiert.
- Berechnung umfasst Gesamtgericht und Portionen sowie Kohlenhydrate, Zucker, Ballaststoffe, Eiweiß, Fett, gesättigte Fettsäuren und Salz. Eigene Zutaten bleiben lokal gespeichert und werden in der Suche priorisiert.

### G.9 Geplante Erweiterung: öffentliche Online-Rezepte
- Gewünscht ist eine Online-Rezeptsuche mit Detailansicht und Übernahme in die lokale Rezeptsammlung.
- Chefkoch nicht scrapen: die recherchierten Chefkoch-AGB untersagen automatisches Auslesen und den Aufbau eigener Suchsysteme. Keine öffentliche Entwickler-API wurde verifiziert. Chefkoch beschreibt Link-/Rezeptimport-Funktionen in seinem eigenen Kochbuch.
- API-Kandidaten: TheMealDB, Spoonacular, Edamam; vor Auswahl sind aktuelle Lizenz-, Attribution-, Preis- und Caching-Regeln zu prüfen.
- Alternativ Share-to-App/URL-Import über erlaubte strukturierte Recipe-Daten (Schema.org), mit Quelllink, manueller Kontrolle und ohne unzulässige Volltext-/Bildkopie.
- Noch nicht implementiert; zunächst Quelle und erlaubtes Importmodell auswählen.


### G.10 Online-Rezeptimport – MVP implementiert, Test offen (2026-11-26)
- Rezeptübersicht enthält den Einstieg „Online-Rezept über Link importieren“.
- Eingabe einer Rezeptseiten-URL; der Importer liest strukturierte Schema.org/Recipe-JSON-LD-Daten. Vorschau mit Titel, Beschreibung, Portionen, Zeiten, Zutaten und Zubereitung; Nutzer bestätigt vor dem Speichern.
- Quelle wird in RecipeEntity.sourceUrl festgehalten. Keine Kopie des Originalbildes oder des vollständigen Seitentextes.
- Zutatenabgleich mit lokalem Produktkatalog und BLS 4.0 bei exakter Namensübereinstimmung; nicht zugeordnete Zutaten bleiben zur späteren Bearbeitung erhalten.
- Kein Chefkoch-spezifisches Scraping und noch keine externe Rezept-API. Nicht jede Webseite bietet maschinenlesbare Recipe-Daten; UI weist auf diese Einschränkung hin.
- Technische Schutzmaßnahmen: nur HTTP(S), Timeout, max. 1.5 MB, HTTP-Statusprüfung; keine zusätzliche Library.
- Version 0.7.0 / versionCode 12.
- **Status: implementiert, aber nicht gebaut/getestet.** Vor Nutzerfreigabe Android-CI und Gerätetest durchführen und mehrere echte Rezeptseiten prüfen.


### G.11 Online-Rezeptimport – Kategorieauswahl und Quell-Nährwerte (2026-11-26)
- Vor dem Speichern kann die Rezeptkategorie frei eingegeben/geändert werden; vorhandene Kategorie wird als Vorschlag verwendet.
- Strukturierte Nährwerte aus Schema.org NutritionInformation werden übernommen, wenn die Quelle sie bereitstellt: kcal, Kohlenhydrate, Zucker, Ballaststoffe, Eiweiß, Fett, gesättigte Fettsäuren und Salz. Sodium wird bei fehlendem SaltContent mit Faktor 2,5 in Salzäquivalent umgerechnet.
- Werte der Quelle werden pro Portion separat von der Zutatenberechnung gespeichert und angezeigt. Keine Werte werden geschätzt oder erfunden, wenn die Quelle sie nicht bereitstellt.
- Version 0.7.1 / versionCode 13. Build und Gerätetest ausstehend.


### G.12 Online-Rezeptimport – mehrere Bilder auswählen (2026-11-26)
- Bildkandidaten aus Schema.org `image`, `og:image`, `twitter:image` und HTML-`img`-Attributen (`src`/`data-src`) sammeln, relative Bild-URLs auflösen, maximal 24 Kandidaten anzeigen.
- In der Vorschau werden Bilder einzeln dargestellt; Nutzer wählt das gewünschte Rezeptfoto vor dem Import. Bild bleibt optional.
- Nur ausgewähltes Bild wird lokal gespeichert (offline verfügbar); Bildquelle wird zusammen mit der Rezeptquelle dokumentiert.
- Download-Schutz: HTTP(S), Timeouts, Content-Type `image/*`, maximal 8 MB.
- Version 0.7.2 / versionCode 14. Build und Gerätetest ausstehend.


### G.13 Integrierter Rezept-Browser (2026-11-26)
- Neuer Tab „Browser“ mit eingebettetem Android WebView, Chefkoch als Startseite, Adress-/Suchfeld sowie Zurück/Vorwärts/Aktualisieren.
- Der Button „Dieses Rezept übernehmen“ übergibt die aktuelle Rezeptseiten-URL an den vorhandenen Importer. Kategorieauswahl, Quell-Nährwerte und Bildgalerie bleiben im bestehenden Import-Workflow.
- WebView: JavaScript/DOM Storage an, File-/Content-Zugriff aus, Popups aus, nur HTTP(S)-Navigation. Keine Umgehung von Zugangsbeschränkungen.
- Unterstützung hängt weiterhin davon ab, ob die Webseite maschinenlesbare Rezeptdaten anbietet.
- Version 0.8.0 / versionCode 15. Build und Gerätetest ausstehend.


### G.14 Rezept-Entdecken – eigener Bild-Feed (2026-11-26)
- Bottom-Navigation „Browser“ durch „Entdecken“ ersetzt. Eigene Netflix-artige Oberfläche mit Hero-Karte, horizontalen Bildkarten, Kategorien und Suche.
- TheMealDB als erste öffentliche Rezeptdatenquelle über `RecipeDiscoveryClient.kt`; Bild- und Rezeptmetadaten für Feed-Karten.
- Karte übergibt die Rezeptdetail-Webadresse an den vorhandenen Importer. API-direkter Import vollständiger Rezeptdetails ist als nächste Ausbaustufe vorgesehen; aktuell hängt Import von strukturierten Daten auf der Detailseite ab.
- Chefkoch-API noch nicht eingebunden; keine nicht autorisierte Seitenauslesung.
- Version 0.9.0 / versionCode 16. Build und Gerätetest ausstehend.


### G.15 TheMealDB: vollständiger Rezeptabruf (2026-11-26)
- Entdecken-Karten übergeben TheMealDB-Rezept-IDs intern an den Rezeptimport; kein Umweg mehr über die öffentliche HTML-Detailseite und Schema.org-Parser.
- Detaildaten werden direkt über TheMealDB `lookup.php?i=` geladen: Titel, Kategorie, Region, Bild, Zutaten mit Mengen und Zubereitungsschritte.
- Vorhandene Vorschau, Kategorieauswahl, Zutatenverarbeitung und lokale Speicherfunktion werden wiederverwendet. Fehlende Nährwerte und Zeiten bleiben leer; Portionszahl vorerst Standardwert 2.
- Chefkoch bleibt beim bestehenden Linkimport. Eine offizielle Chefkoch-Such-API ist weiterhin nicht bestätigt; keine automatisierte Chefkoch-Suche oder Seitenauslesung vortäuschen.
- Version 0.9.1 / versionCode 17. Build und Gerätetest stehen aus.


### G.16 TheMealDB-Bild in der Rezeptvorschau (2026-11-26)
- Ursache: Die Detailabfrage lieferte `imageUrl`, die gemeinsame Vorschau rendert Bildkandidaten aber ausschließlich aus `imageUrls`. Deshalb erschien „Bildauswahl“ ohne Bild.
- TheMealDB-Detailantwort übergibt das vorhandene Rezeptfoto jetzt zusätzlich als einzigen Bildkandidaten. Es wird in der Vorschau angezeigt und ist standardmäßig ausgewählt; beim Speichern wird es wie beim bisherigen Import lokal abgelegt.
- Version 0.9.2 / versionCode 18. Build und Gerätetest stehen aus.


### G.16 Chefkoch als externe Suchquelle (2026-11-26)
- Entdecken erhält eine Quellenumschaltung zwischen TheMealDB und Chefkoch.
- Chefkoch-Suche öffnet die offizielle Chefkoch-Suche mit dem eingegebenen Suchbegriff im Browser. Der Nutzer kopiert anschließend den konkreten Rezeptlink und nutzt den bestehenden Online-Linkimport in Topf Secret.
- Keine automatisierte Chefkoch-Suche, kein Scraping und keine Umgehung von Zugriffsbeschränkungen; die Chefkoch-Bedingungen untersagen automatisches Auslesen und den Aufbau eigener Suchsysteme.
- Version 0.9.3 / versionCode 19. Build und Gerätetest stehen aus. Deutsche Übersetzung für TheMealDB ist als nächster Umsetzungsschritt offen.

## Produkt- und Lizenzarchitektur für eine spätere Veröffentlichung (Entscheidung 2026-11-26)

### Drei getrennte Android-Apps
- **Topf_Secret_Admin**: private Familien-/Entwicklungsvariante für Nutzer und Ehefrau. Die umfangreichere Entdecken-Funktion wird hier weiterentwickelt und getestet (TheMealDB, geplante deutsche Übersetzung und zusätzliche Testquellen); vorhandener Familien-Key bleibt für diese Variante vorgesehen.
- **Topf Secret Release 1.0**: stabile Kundenvariante. Entdecken enthält nur rechtlich geprüfte externe Rezeptseiten als Linkbuttons. Die Buttons öffnen die Originalseiten im Browser. Nutzer kopieren/teilen die konkrete Rezeptadresse und importieren sie anschließend über den vorhandenen URL-Import. Keine nicht autorisierte automatisierte Suche, Extraktion oder Bildkopie.
- **Topf Secret Lizenzmanager**: separate Android-App für den Betreiber zur Erstellung und Verwaltung von Kundenlizenzen.
- Getrennte Application IDs/Installationen und Build-Konfigurationen vorsehen. Gemeinsame Rezeptbasis/Funktionen möglichst wiederverwenden; Admin-Testfunktionen und nicht freigegebene Quellen dürfen nicht in Release gelangen.

### Einmalige Keys ohne laufenden Lizenzserver (erste Ausbaustufe)
- Geschäftsmodell: einmaliger Kauf, kein Abo und keine automatische Verlängerung.
- Lizenzmanager erfasst Kundenname und E-Mail-Adresse; Betreiber wählt eine Laufzeit und tippt auf „Generate Key“.
- Laufzeiten: mindestens Beta, 1 Monat, 3 Monate, 6 Monate, 1 Jahr, 2 Jahre und Lifetime; Auswahl soll erweiterbar bleiben. Zeitlizenzen haben ein Ablaufdatum, Lifetime nicht.
- Lokale Lizenzübersicht mit Suche nach Name/E-Mail, Lizenztyp, Key/Key-Referenz, Ausstellungsdatum, Ablaufdatum und Status. Verlorene Keys sollen anhand des Kundeneintrags wiedergefunden bzw. erneut ausgegeben werden können.
- Lizenzformat offline prüfbar und digital signiert: privater Signaturschlüssel ausschließlich im Lizenzmanager und sicher gesichert; öffentlicher Prüfschlüssel in Admin- und Release-App. Niemals private Schlüssel in APKs oder öffentliches GitHub einchecken.
- Lizenzdaten sollen eine eindeutige Lizenz-ID, Lizenztyp, Ausstellungszeitpunkt, Ablaufdatum (außer Lifetime), Kundenreferenz/E-Mail und ggf. Gerätebindung enthalten.
- Lizenzmanager-Datenbank benötigt verschlüsselten Export/Backup und einen getesteten Wiederherstellungsweg, da keine zentrale Serverkopie vorgesehen ist.
- E-Mail-Zuordnung ist zunächst manuell durch den Betreiber zu verifizieren. Automatischer E-Mail-Einmalcode würde einen Maildienst/externen Dienst erfordern und ist nicht Teil der kostenlosen Offline-Erstausbaustufe.
- Beta ist ein eigener Lizenztyp für ausgewählte Tester, mit eigener Laufzeit und Kennzeichnung.

### Grenzen und Sicherheitsentscheidungen
- Offline-Signaturprüfung schützt gegen selbst erzeugte/manipulierte Keys, bietet aber ohne Server keinen sofortigen zentralen Widerruf. Ein altes Gerät kann nach einem Gerätewechsel nicht zuverlässig remote deaktiviert werden. Gerätewechsel erfolgt zunächst durch Ausstellung/Übertragung einer neuen signierten Lizenz; die Grenze muss in der späteren UX berücksichtigt werden.
- Ablaufprüfung bei zeitlich begrenzten Keys benötigt eine robuste Zeitstrategie; Manipulation der Geräteuhr ist bei rein offline arbeitender App nicht vollständig auszuschließen. Vor Implementierung geeignete lokale Speicherung/Prüfung und verständliche Kulanz-/Offline-Regeln festlegen.
- Keine laufenden Serverkosten für die erste Ausbaustufe geplant. Externe Maildienste, Cloud-Synchronisierung oder zentrale Widerrufslisten wären spätere optionale Erweiterungen mit möglichen Kosten.
- Vor Release kommerzielle Nutzungsrechte, Attribution, Caching- und Bildrechte jeder Rezeptquelle prüfen. TheMealDB bleibt zunächst Entwicklungsquelle; kommerzielle Freigabe/Bedingungen sind vor Veröffentlichung zu klären. Chefkoch bleibt bei externem Browser-Link und manuellem URL-Import ohne Scraping.

**Status:** Vereinbart und dokumentiert, nicht implementiert. Zuerst die bestehende Admin-App/Entdecken-Funktion weiterentwickeln; Lizenzmanager und Release-Aufteilung sind spätere eigene Arbeitspakete.



### H. Startanimation / Markenauftritt (2026-11-26)
- Kurze, witzige Startanimation mit eigenem Compose-gezeichnetem Kochtopf: Einflug/Pop-in, verschmitztes Gesicht, angehobener Deckel, Dampf und anschließender Topf-Secret-Schriftzug.
- Läuft vor der Aktivierungsprüfung, ist offlinefähig und benötigt keine zusätzliche Animationsbibliothek.
- Version 0.9.4 / versionCode 20. Implementiert auf `development`; CI-Build und Gerätetest stehen noch aus.


### H.1 Entdecken UX und deutsche Rezeptübersetzung (2026-11-26)
- Nicht funktionierende globale Suchfelder in Entdecken entfernt. TheMealDB-Navigation erfolgt über Kategorien; Chefkoch wird über einen einzelnen Button extern geöffnet, ohne einen Suchbegriff in einer nicht funktionierenden Eingabe vorzutäuschen.
- „Für dich“ wählt bei jedem Öffnen der Entdecken-Ansicht zufällig eine TheMealDB-Kategorie, damit nicht immer dieselbe Chicken-Auswahl erscheint.
- TheMealDB-Detailrezepte werden mit Google ML Kit Translation Englisch→Deutsch übersetzt (Titel, Kategorie, Region, Zutaten und Anweisungen). Das Sprachmodell wird dynamisch beim ersten Gebrauch geladen und anschließend lokal wiederverwendet. Der erste Download benötigt Internet; danach ist die Übersetzung mit vorhandenem Modell offline möglich.
- Version 0.9.5 / versionCode 21; implementiert auf `development`, CI-Build und Gerätetest offen.
