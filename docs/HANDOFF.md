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
- Die Startseite zeigt das Topf-Secret-Logo, Begrüßung, Markenbereich und Navigationseinstiege. Der Rezepte-Bereich ist mit der lokalen Datenbank verbunden. Die Kategorieansicht listet jetzt alle tatsächlich verwendeten Kategorien samt Rezeptanzahl und öffnet die jeweilige gefilterte Rezeptliste.
- Navigation nutzt Compose-Zustand und benötigt noch keine zusätzliche Navigation-Bibliothek.
- Rezepteditor erweitert: Vorbereitungszeit und Kochzeit sowie kcal, Kohlenhydrate, Eiweiß und Fett je Portion können optional erfasst und in der Detailansicht angezeigt werden. Nährwerte werden ausdrücklich als Richtwerte gekennzeichnet.
- Zutatenzeilen erkennen einfache Mengen-/Einheitenangaben am Zeilenanfang (z. B. `500 g Kartoffeln`, `2 EL Öl`) und speichern Menge und Zutatenname getrennt. Beim Bearbeiten werden beide Bestandteile wieder zusammengesetzt.
- Hell-/Dunkelmodus ist über ein Symbol oben rechts in der Kopfzeile umstellbar (Mond/Sonne). Die Auswahl wird lokal in den App-Einstellungen gespeichert und bleibt nach einem Neustart erhalten.
- Die Startseiten-Karte „Guten Appetit“ verwendet im Hell- und Dunkelmodus jeweils einen kontrastreichen dunklen Braunton mit heller Schrift; sie übernimmt nicht mehr versehentlich die dynamische Textfarbe als Hintergrund.
- Für diese Erweiterung war keine Room-Schemaänderung nötig; Datenbankversion 1 bleibt bestehen.
- Der Quellstand wurde auf Version 0.6.0 (`versionCode = 8`) angehoben. Build und Gerätetest für diese Erweiterung stehen noch aus.
- Room `2.8.3` ist eingebunden (Kotlin KAPT als Compiler-Anbindung). Die lokale Datenbank `topf_secret.db` wird bei Bedarf über `TopfSecretDatabase.getInstance(context)` erstellt.
- Datenmodell v1: `RecipeEntity` mit Titel, Beschreibung, Kategorie, Portionen, Zeiten, Bild-/Quellen-URI, Notizen, optionalen Nährwert-Schätzwerten pro Portion, Favoritenstatus und Zeitstempeln. Zutaten (`IngredientEntity`) und Zubereitungsschritte (`PreparationStepEntity`) sind normalisiert und per Fremdschlüssel mit Cascade Delete verknüpft.
- `RecipeDao` bietet Flow-Beobachtung aller Rezepte, einzelner Rezepte und Kategorien sowie transaktionales Speichern mit Ersetzen der Zutaten und Schritte. Der Rezepte-Bereich ist mit Room verbunden; Rezeptliste, Detailansicht und Eingabemaske unterstützen den ersten lokalen CRUD-Ablauf. Die Rezeptliste bietet eine Live-Suche über Rezeptname, Kategorie, Beschreibung und Zutatenname sowie einen Kategorienfilter. Kategorien werden aus den gespeicherten Rezepten abgeleitet; dafür war keine Room-Schemaänderung nötig. Migrationen über Datenbankversion 1 hinaus sind noch nicht umgesetzt.
- Pushes auf `development` lösen keinen Build mehr aus. Builds werden manuell über GitHub Actions → Android CI → Run workflow gestartet. Pull Requests gegen `development` lösen weiterhin automatisch einen Build aus.
- CI erstellt ein signiertes Release-APK und benennt es vor der Veröffentlichung nach der GitHub-Buildnummer, z. B. `topf_secret_#18.apk`.
- Manuelle Builds (`workflow_dispatch`) veröffentlichen die APK als direkt herunterladbares `.apk`-Asset in einem GitHub-Release mit Tag `build-<Run-Nummer>` (als Pre-Release markiert). Dadurch entfällt das ZIP-Verpacken durch GitHub Actions für den regulären Download. Pull-Request-Builds veröffentlichen keine Releases.
- Aktuelle App-Version im Quellstand: `versionCode = 8`, `versionName = 0.6.0`.
- Build 15 war erfolgreich. Build 16 scheiterte zunächst wegen eines falschen `KeyboardOptions`-Imports; der Import wurde auf `androidx.compose.foundation.text.KeyboardOptions` korrigiert.
- Der Nutzer bestätigte anschließend den erfolgreichen Build 17 und dass die Aktivierung den eingegebenen Key nach App-Neustart speichert.
- Workflow-Änderung: Release-APK wird mit der laufenden GitHub-Buildnummer benannt (`topf_secret_#<Run-Nummer>.apk`) und bei manuellem Start als direkter APK-Download in GitHub Releases veröffentlicht.
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


## Admin-Freischaltung (Version 0.5.0)
- Beim Start erscheint vor dem Kochbuch eine lokale Freischaltseite.
- Ein gemeinsamer, dauerhaft gültiger Admin-Key ist für den Nutzer und seine Ehefrau vorgesehen: `KOCH-1312-2709-TOPF`.
- Die App vergleicht einen SHA-256-Prüfwert; der Klartext-Key ist nicht im Repository hinterlegt.
- Nach erfolgreicher Eingabe wird nur der Freischaltstatus in den privaten App-Einstellungen gespeichert. Der Key muss auf diesem Gerät danach nicht erneut eingegeben werden.
- Die Rezeptdatenbank wird durch Fehlversuche nicht verändert oder gelöscht.
- Die Freischaltung ist offline und gerätebezogen. Bei Neuinstallation muss der Key erneut eingegeben werden.
- Wichtige Einschränkung: Das Repository ist öffentlich. Eine lokale Schlüsselprüfung in einer öffentlich einsehbaren Android-App ist kein starker Kopierschutz und kann von technisch versierten Personen umgangen werden. Für eine spätere Verteilung an weitere Nutzer sind signierte, individuelle Lizenzen (und der separate Key Manager) vorgesehen.
- Der Admin-Key enthält die vom Nutzer gewünschten Zahlenfolgen 1312 und 2709. Er ist bewusst als gemeinsamer Familien-Key ausgelegt, nicht als sichere individuelle Lizenz.

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
2. Manuellen Workflow starten; die signierte APK wird danach im GitHub-Bereich Releases als direkte `.apk`-Datei veröffentlicht.
3. Nach dem Import-Fix einen neuen signierten Release-Build starten, APK-Signatur prüfen und Aktivierungsseite auf dem Gerät testen; eine Deinstallation nur dann durchführen, wenn die Signatur nicht mit der installierten Version übereinstimmt.
4. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
5. Version 0.6.0 in GitHub Actions bauen und auf dem Gerät testen: Hell-/Dunkelmodus über das Symbol oben rechts inklusive Neustart, Kontrast der „Guten Appetit“-Karte, Rezeptzeiten, Nährwerte, Zutatenmengen sowie Anlegen/Bearbeiten/Löschen und Datenpersistenz.
6. Rezept-Suche, Kategorienübersicht und Kategorienfilter sind im Quellstand umgesetzt; GitHub-Actions-Build und Gerätetest stehen noch aus.
7. PIN-Sperre implementieren und testen.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
