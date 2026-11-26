# Topf Secret – Produktplan

## Produktziel

Ein einfaches, zuverlässiges Android-Kochbuch für zwei Personen. Rezepte sollen lokal verfügbar bleiben, kontrolliert importiert, sicher synchronisiert und vollständig gesichert werden können.

## Funktionale Anforderungen

### A. Schutz und Privatsphäre
- Integrierte PIN-Sperre beim Öffnen bzw. nach konfigurierbarer Inaktivität.
- PIN niemals im Klartext speichern; sichere Ableitung/Prüfung und Schutz vor Rateversuchen.
- Biometrische Entsperrung optional, niemals Voraussetzung.
- Rezeptdaten und Bilder standardmäßig privat.
- Öffentlicher GitHub-Code darf keine Zugangsdaten, echten Rezeptdaten oder privaten Backups enthalten.

### B. Rezepte
- Titel, Beschreibung, Zutaten, Mengen und Einheiten.
- Zubereitungsschritte, Vorbereitungs-/Kochzeit, Portionenzahl.
- Kategorien, Tags, Favoriten und persönliche Notizen.
- Eigene Bilder und Quellenangabe.

### C. Synchronisation
- Zwei getrennte App-Installationen mit autorisierter Verbindung.
- Synchronisation in beide Richtungen.
- Konflikterkennung bei gleichzeitigen Änderungen.
- Offline-Änderungen lokal puffern und später übertragen.
- Zugriff ausschließlich für eingeladene/verknüpfte Konten.

### D. Web-Rezeptimport
- Teilen-Menü (Android Sharesheet) und manuelle URL-Eingabe.
- Strukturierte Rezeptdaten bevorzugt aus Schema.org Recipe lesen.
- Importvorschau mit editierbaren Feldern.
- Erst nach ausdrücklicher Bestätigung speichern.
- Quelle und Bildrechte/Quell-URL dokumentieren; keine Umgehung von Bezahlschranken oder Zugriffsschutz.

### E. Bilder
- Bilder aus Kamera/Galerie.
- Lokale Speicherung und optional synchronisierte Speicherung.
- Bilder im vollständigen Backup einschließen.

### F. Nährwerte
- Energie, Kohlenhydrate, Zucker, Ballaststoffe, Eiweiß und Fett.
- Berechnung aus Zutatenmengen und nachvollziehbaren Nährwertdaten.
- Anzeige pro Gesamt-Rezept, Portion und frei gewählter Portionsanzahl.
- Datenquelle und Schätzstatus sichtbar machen.
- Nutzer kann Zutaten und Nährwerte korrigieren.
- Werte sind Schätzungen und keine medizinische Therapie- oder Insulinempfehlung.

### G. Backup und Wiederherstellung
- Versioniertes ZIP-basiertes `.tsb`-Format.
- Vollständiger Export aller Rezepte, Metadaten und Bilder.
- Optional einzelne Rezepte exportieren/teilen.
- Import mit Vorschau, Validierung und Duplikaterkennung.
- Verschlüsseltes Backup als Option; Wiederherstellung ohne Cloud möglich.
- Formatversion und Prüfsummen vorsehen, damit spätere App-Versionen alte Backups migrieren können.

## Technische Leitlinien

- Kotlin und Jetpack Compose.
- Room als lokale Datenbank.
- Repository-/Use-Case-Schicht, damit Datenbank und Synchronisationsanbieter austauschbar bleiben.
- Synchronisation erst nach Sicherheits- und Datenschutzkonzept integrieren.
- Android 8.0 / API 26 als vorläufiges Mindestziel; reale Kompatibilität testen.
- Keine geheimen Schlüssel oder privaten Nutzerdaten in Git.

## Roadmap

1. Projektgrundlage und CI-Build
2. UI-Grundgerüst und Navigation
3. Lokales Datenmodell und Room
4. PIN-Sperre
5. Rezeptverwaltung und Bilder
6. Portionen- und Nährwertberechnung
7. Backup/Restore
8. Web-Rezeptimport
9. Sichere Synchronisation für zwei Personen
10. Geräte- und Regressionstests, Release

## Noch zu entscheiden

- Supabase oder eigener Synchronisationsdienst
- PIN-Länge und Sperrverhalten
- Backup-Verschlüsselung standardmäßig optional oder verpflichtend
- Nährwertdatenquelle und Lizenz
- App-Sprache zunächst Deutsch oder mehrsprachig
