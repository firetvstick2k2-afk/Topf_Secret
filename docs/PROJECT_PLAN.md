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
- Bilder aus Galerie auswählen; Kameraaufnahme als nächster Ausbau.
- Bildauswahl dauerhaft dem jeweiligen Rezept zuordnen.
- Bildvorschau in Rezeptliste und Rezeptdetail.
- Lokale Speicherung bzw. dauerhaft gesicherter Zugriff ohne unnötige Berechtigungen.
- Bilder im vollständigen Backup einschließen.

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
2. Rezeptbilder aus Galerie auswählen und in Rezeptliste/-detail anzeigen – erste Umsetzung auf `development`; Kameraaufnahme und echte Dateiübernahme noch offen.
3. Kameraaufnahme und robuste lokale Bildablage.
4. Portionen- und Nährwertberechnung erweitern.
5. Vollständiger Backup-/Restore-Workflow inklusive Bilder.
6. Web-Rezeptimport mit Vorschau.
7. Sichere Synchronisation für zwei Personen.
8. Geräte- und Regressionstests, Release.

## Noch zu entscheiden

- Ob Backups standardmäßig verschlüsselt oder optional verschlüsselt sein sollen.
- Nährwertdatenquelle und Lizenz.
- Synchronisationsdienst.
- App-Sprache zunächst Deutsch oder mehrsprachig.
