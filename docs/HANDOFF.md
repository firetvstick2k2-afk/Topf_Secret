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
- Erste App-Struktur umgesetzt: Startseite im warmen Creme-/Kupfer-/Dunkelbraun-Design und Bottom-Navigation für Start, Rezepte, Kategorien und Favoriten.
- Die Startseite zeigt das Topf-Secret-Logo, Begrüßung, Markenbereich und Navigationseinstiege. Rezepte, Kategorien und Favoriten sind derzeit bewusst als Platzhalter gekennzeichnet; Datenbank und Rezeptfunktionen folgen.
- Navigation nutzt Compose-Zustand und benötigt noch keine zusätzliche Navigation-Bibliothek.
- Room `2.8.3` ist eingebunden (Kotlin KAPT als Compiler-Anbindung). Die lokale Datenbank `topf_secret.db` wird bei Bedarf über `TopfSecretDatabase.getInstance(context)` erstellt.
- Datenmodell v1: `RecipeEntity` mit Titel, Beschreibung, Kategorie, Portionen, Zeiten, Bild-/Quellen-URI, Notizen, optionalen Nährwert-Schätzwerten pro Portion, Favoritenstatus und Zeitstempeln. Zutaten (`IngredientEntity`) und Zubereitungsschritte (`PreparationStepEntity`) sind normalisiert und per Fremdschlüssel mit Cascade Delete verknüpft.
- `RecipeDao` bietet Flow-Beobachtung aller Rezepte, einzelner Rezepte und Kategorien sowie transaktionales Speichern mit Ersetzen der Zutaten und Schritte. Die Datenbank ist noch nicht mit der Oberfläche verbunden; es gibt noch keine Rezept-Eingabemasken oder Migrationen über Version 1 hinaus.
- Pushes auf `development` lösen keinen Build mehr aus. Builds werden manuell über GitHub Actions → Android CI → Run workflow gestartet. Pull Requests gegen `development` lösen weiterhin automatisch einen Build aus.
- CI erstellt nun ein signiertes Release-APK als Artefakt `topf-secret-release-apk` (14 Tage Aufbewahrung).
- Aktuelle App-Version im Quellstand: `versionCode = 5`, `versionName = 0.3.0`.
- Neues App-Symbol liegt unter `app/src/main/res/drawable-nodpi/topf_secret_app_icon.png`; `AndroidManifest.xml` verweist für `icon` und `roundIcon` auf `@drawable/topf_secret_app_icon`.
- Das vollständige Startlogo wurde als PNG unter `app/src/main/res/drawable-nodpi/topf_secret_logo.png` hochgeladen; der Kotlin-Ressourcenname bleibt `R.drawable.topf_secret_logo`.
- Die vorherige JPEG-Datei `topf_secret_logo.jpg` war unvollständig (APK enthielt 7.500 Bytes und der JPEG-Decoder meldete fehlende Bytes am Dateiende). Sie wurde aus `development` entfernt. Die frühere Annahme, die Dateiendung sei alleinige Ursache, war nicht korrekt.
- Build 9 (`b7f0a46`) und ein späterer Build stürzten beim Laden des Logos mit `NullPointerException: null cannot be cast to BitmapDrawable` in `painterResource` ab. Die PNG-Datei ist jetzt die aktive Ressource; erneuter Build und Gerätetest stehen noch aus.
- Zur Diagnose bleibt `TopfSecretApplication.kt` enthalten: Ein globaler Uncaught-Exception-Handler schreibt Absturzdetails (Zeitpunkt, App-/Android-Version, Gerätemodell, Thread und Stacktrace) als `.log`.
- Android 10+ speichert die Datei über MediaStore direkt im öffentlichen Ordner `/storage/emulated/0/Download`, ohne Speicherberechtigung. Android 8/9 nutzt den app-eigenen Download-Ordner.
- Für Updates wird ein fester Upload-Signaturschlüssel verwendet. Der Keystore gehört **nicht** ins öffentliche Repository.
- GitHub Actions benötigt vor dem ersten signierten Release diese Repository-Secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` (Wert: `topfsecret`) und `ANDROID_KEY_PASSWORD`. Der Nutzer muss sie unter Settings → Secrets and variables → Actions → New repository secret hinterlegen.
- Der gleiche Keystore muss für alle künftigen Versionen sicher aufbewahrt und wiederverwendet werden. Verlust des Schlüssels verhindert normale Updates dieser App-Installation.
- Nach Einrichtung der Secrets muss der Nutzer die bisherige, mit dem wechselnden Debug-Schlüssel installierte App einmal deinstallieren und die erste signierte Release-Version installieren. Ab dann sollten Updates mit derselben Signatur direkt über die vorhandene App möglich sein; App-Daten bleiben erhalten, sofern die App nicht deinstalliert wird.
- Gradle Wrapper ist noch nicht eingecheckt. CI verwendet vorerst Gradle 8.13 über `gradle/actions/setup-gradle@v4`.

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
- Keine echten Rezepte, privaten Fotos, Backups, Zugangsdaten, Keystores oder Cloud-Schlüssel committen.
- PIN nie im Klartext speichern.
- Synchronisation nur authentifiziert und auf berechtigte Personen begrenzt.
- Nährwerte als Schätzwerte kennzeichnen; keine medizinischen Therapieempfehlungen.
- Absturzprotokolle können technische Geräteinformationen enthalten; vor dem Teilen keine persönlichen Inhalte ergänzen.

## Nächste Schritte
1. Die vier GitHub Actions Secrets sicher hinterlegen.
2. Manuellen Workflow starten und signiertes Release-APK bauen.
3. Version 0.1.2 bauen, APK-Signatur prüfen und auf dem Samsung-Gerät testen; eine Deinstallation nur dann durchführen, wenn die Signatur nicht mit der installierten Version übereinstimmt.
4. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
5. Room-Datenbank und erstes Rezeptdatenmodell sind angelegt; als Nächstes Build/Schema prüfen und die Datenbank über ein Repository an die Oberfläche anbinden.
6. PIN-Sperre implementieren und testen.
7. Rezeptverwaltung mit Anlegen, Bearbeiten, Löschen und Favoriten entwickeln.
8. Suche und Kategorien mit den gespeicherten Rezepten verbinden.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
