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
