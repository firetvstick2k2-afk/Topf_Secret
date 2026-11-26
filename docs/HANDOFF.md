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
- GitHub-Actions-Workflow `.github/workflows/android-ci.yml` baut bei Push/Pull Request auf `development` ein Debug-APK.
- Android-SDK-Setup ist korrigiert: Pakete werden leerzeichengetrennt übergeben: `platform-tools platforms;android-36 build-tools;36.0.0`.
- CI-Lauf `45738881747` war vollständig erfolgreich: SDK, JDK 17, Gradle und `assembleDebug` liefen grün.
- Workflow erweitert: Das erzeugte Debug-APK wird mit `actions/upload-artifact@v4` als `topf-secret-debug-apk` hochgeladen; Aufbewahrung 14 Tage.
- Der neue Upload-Schritt muss noch durch den nächsten CI-Lauf bestätigt werden.
- Gradle Wrapper ist noch nicht eingecheckt. CI verwendet vorerst Gradle 8.13 über `gradle/actions/setup-gradle@v4`. Wrapper-Dateien erst aus einer vertrauenswürdigen Gradle-8.13-Installation generieren und den offiziellen Wrapper-JAR-Prüfwert verifizieren; kein handgefertigtes oder ungeprüftes JAR committen.

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
1. Neuen GitHub-Actions-Lauf prüfen und APK-Artefakt kontrollieren.
2. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
3. App-Navigation und Designsystem aufbauen.
4. Datenmodell und Room-Datenbank entwerfen.
5. PIN-Sperre implementieren und testen.
6. Rezeptverwaltung entwickeln.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
