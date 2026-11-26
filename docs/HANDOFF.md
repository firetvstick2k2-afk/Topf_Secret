# Topf Secret – Handoff

Stand: 2026-11-26

## Repository und Branch-Regeln
- Repository: https://github.com/firetvstick2k2-afk/Topf_Secret
- Sichtbarkeit: Public
- Hauptbranch: `main` (nicht direkt bearbeiten)
- Entwicklungsbranch: `development`
- Alle Entwicklungsänderungen ausschließlich auf `development`.
- Vor jeder Dateiänderung aktuellen Inhalt und Blob-SHA aus GitHub abrufen.
- Handoff und Projektdokumentation bei relevanten Änderungen aktuell halten.

## Aktueller Stand
- Repository initialisiert.
- `development` wurde von `main` abgezweigt.
- Android-Projektgrundgerüst mit Kotlin, Jetpack Compose und API-26-Mindestziel angelegt.
- Erste Platzhalter-Startansicht vorhanden.
- Produktanforderungen und Roadmap dokumentiert.
- GitHub-Actions-Workflow `.github/workflows/android-ci.yml` ergänzt: baut bei Push/Pull Request auf `development` ein Debug-APK.
- Die ersten CI-Läufe scheiterten vor dem Gradle-Build bei der Android-SDK-Einrichtung.
- Ursache Lauf 1: Setup-Action versuchte das veraltete SDK-Paket `tools` zu installieren.
- Ursache Lauf 2: Die Paketliste wurde mit Kommas übergeben; dadurch wurde beispielsweise `platform-tools,` als Paketname interpretiert.
- Workflow erneut korrigiert: SDK-Pakete werden jetzt als leerzeichengetrennte Argumente übergeben: `platform-tools platforms;android-36 build-tools;36.0.0`.
- Dieser neue Workflow-Stand muss noch durch einen CI-Lauf verifiziert werden.
- Gradle Wrapper ist noch nicht eingecheckt; der CI-Workflow verwendet vorerst Gradle 8.13 aus der Action.

## Vereinbarte Produktanforderungen
- Integrierter PIN-Schutz.
- Rezepte manuell erstellen, bearbeiten und speichern.
- Zwei-Wege-Synchronisation zwischen Nutzer und Ehefrau.
- Webseiten-Rezeptimport mit Vorschau und ausdrücklicher Übernahme.
- Gerichtsbilder speichern.
- Nährwertberechnung, insbesondere kcal und Kohlenhydrate pro Portion und Portionsmenge.
- Vollständiger Rezept-Export/Import inklusive Bilder als Backup.
- Möglichst breite Android-Kompatibilität, vorläufig Android 8.0 / API 26.
- Öffentliches GitHub-Repository; private Daten und Zugangsdaten bleiben außerhalb des Repositories.

## Sicherheitsregeln
- Keine echten Rezepte, privaten Fotos, Backups, Zugangsdaten oder Cloud-Schlüssel committen.
- PIN nie im Klartext speichern.
- Synchronisation nur authentifiziert und auf berechtigte Personen begrenzt.
- Nährwerte als Schätzwerte kennzeichnen; keine medizinischen Therapieempfehlungen.

## Nächste Schritte
1. Neuen GitHub-Actions-Build prüfen und eventuelle Fehler beheben.
2. Gradle Wrapper ergänzen und lokal/CI verifizieren.
3. App-Navigation und Designsystem aufbauen.
4. Datenmodell und Room-Datenbank entwerfen.
5. PIN-Sperre implementieren und testen.
6. Rezeptverwaltung entwickeln.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
