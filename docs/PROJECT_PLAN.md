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
