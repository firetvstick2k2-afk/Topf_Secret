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
- Android-Projektgrundgerüst mit Kotlin, Jetpack Compose und API-26-Mindestziel.
- GitHub-Actions baut auf `development` erfolgreich ein Debug-APK und lädt es als Artefakt `topf-secret-debug-apk` hoch (14 Tage Aufbewahrung); der Upload-Schritt wurde bereits erfolgreich ausgeführt.
- Das vom Nutzer bestätigte Original-Logo liegt als `app/src/main/res/drawable-nodpi/topf_secret_logo.webp` vor.
- Das passende quadratische App-Symbol liegt als `app/src/main/res/drawable-nodpi/ic_launcher.webp` vor.
- Das Manifest verwendet das neue App-Symbol; die Compose-Startseite zeigt das vollständige Logo.
- Grafiken wurden für Android-WebP-Darstellung optimiert; das Motiv und der Entwurf wurden nicht verändert.
- Die neue Logo-/Icon-Version muss noch durch den nächsten CI-Build und eine Installation auf einem echten Gerät bestätigt werden.
- Gradle Wrapper ist noch nicht eingecheckt. CI verwendet vorerst Gradle 8.13 über `gradle/actions/setup-gradle@v4`. Wrapper-Dateien erst aus einer vertrauenswürdigen Gradle-8.13-Installation generieren und den offiziellen Wrapper-JAR-Prüfwert verifizieren.

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
1. CI-Build mit Logo und App-Symbol prüfen und neue APK als Artefakt bereitstellen.
2. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
3. App-Navigation und Designsystem aufbauen.
4. Datenmodell und Room-Datenbank entwerfen.
5. PIN-Sperre implementieren und testen.
6. Rezeptverwaltung entwickeln.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
