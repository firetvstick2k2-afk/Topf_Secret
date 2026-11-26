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
- GitHub-Actions baut auf `development` erfolgreich ein Debug-APK und lädt es als Artefakt `topf-secret-debug-apk` hoch (14 Tage Aufbewahrung).
- Das vom Nutzer bestätigte Original-Logo liegt als `app/src/main/res/drawable-nodpi/topf_secret_logo.webp` vor.
- Das passende quadratische App-Symbol liegt als `app/src/main/res/drawable-nodpi/ic_launcher.webp` vor.
- Das Manifest verwendet das neue App-Symbol; die Compose-Startseite zeigt das vollständige Logo.
- Build 9 (`b7f0a46`) wurde vom Nutzer installiert, stürzt auf dem Gerät aber direkt beim Öffnen ab. Der GitHub-CI-Build war erfolgreich; ein erfolgreicher CI-Build bestätigt keinen fehlerfreien Lauf auf echter Hardware.
- Zur Diagnose wurde `TopfSecretApplication.kt` ergänzt: Ein globaler Uncaught-Exception-Handler schreibt Absturzdetails (Zeitpunkt, App-/Android-Version, Gerätemodell, Thread und Stacktrace) als `.log`.
- Android 10+ speichert die Datei über MediaStore direkt im öffentlichen Ordner `/storage/emulated/0/Download`, ohne Speicherberechtigung. Android 8/9 nutzt mangels moderner Download-API den app-eigenen Download-Ordner.
- Das Manifest registriert `.TopfSecretApplication`. Der nächste Build muss auf dem betroffenen Gerät getestet werden; nach einem Absturz die neueste `TopfSecret_Crash_*.log` aus Downloads bereitstellen.
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
- Absturzprotokolle können technische Geräteinformationen enthalten; vor dem Teilen keine persönlichen Inhalte ergänzen.

## Nächste Schritte
1. Build mit Absturzprotokollierung auf dem betroffenen Gerät installieren und den Absturz reproduzieren.
2. Die erzeugte `TopfSecret_Crash_*.log` aus Downloads auswerten und die konkrete Ursache beheben.
3. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
4. App-Navigation und Designsystem aufbauen.
5. Datenmodell und Room-Datenbank entwerfen.
6. PIN-Sperre implementieren und testen.
7. Rezeptverwaltung entwickeln.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
