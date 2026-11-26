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
- Die Startseite zeigt das Topf-Secret-Logo, Begrüßung, Markenbereich und Navigationseinstiege. Der Rezepte-Bereich ist mit der lokalen Datenbank verbunden. Die Kategorieansicht listet alle tatsächlich verwendeten Kategorien samt Rezeptanzahl und öffnet die jeweilige gefilterte Rezeptliste. Die Favoritenansicht zeigt alle als Favorit markierten Rezepte; ein Eintrag öffnet die zugehörige Rezeptdetailansicht.
- Navigation nutzt Compose-Zustand und benötigt noch keine zusätzliche Navigation-Bibliothek.
- Rezepteditor erweitert: Vorbereitungszeit und Kochzeit sowie kcal, Kohlenhydrate, Eiweiß und Fett je Portion können optional erfasst und in der Detailansicht angezeigt werden. Diese Felder sind keine Pflichtfelder; Nährwerte werden ausdrücklich als Richtwerte gekennzeichnet.
- Zutatenzeilen erkennen einfache Mengen-/Einheitenangaben am Zeilenanfang (z. B. `500 g Kartoffeln`, `2 EL Öl`) und speichern Menge und Zutatenname getrennt. Beim Bearbeiten werden beide Bestandteile wieder zusammengesetzt.
- Hell-/Dunkelmodus ist über ein Symbol oben rechts in der Kopfzeile umstellbar (Mond/Sonne). Daneben befindet sich ein Info-Button (ⓘ), der einen Dialog mit kleinem App-Logo, „Topf Secret by Andre Schlüter“ und der installierten App-Version öffnet. Die Versionsnummer wird zur Laufzeit aus den Paketinformationen gelesen. Die Modusauswahl wird lokal in den App-Einstellungen gespeichert und bleibt nach einem Neustart erhalten.
- Die Startseiten-Karte „Guten Appetit“ verwendet im Hell- und Dunkelmodus jeweils einen kontrastreichen dunklen Braunton mit heller Schrift; sie übernimmt nicht mehr versehentlich die dynamische Textfarbe als Hintergrund.
- Anzeigeproblem behoben: In der Überschrift der „Guten Appetit“-Karte wurde `\\n` als sichtbarer Text ausgegeben. Die Kotlin-Zeichenkette verwendet jetzt `\n`, sodass „Eure Küche.“ und „Eure Regeln.“ in zwei Zeilen dargestellt werden. Änderung auf `development` committed (`742f5d4`); neuer Release-Build und Gerätetest stehen noch aus.
- Info-Dialog: Die Autorenzeile ist jetzt bewusst zweizeilig (`Topf Secret by` / `Andre Schlüter`), damit der Vorname nicht am Ende der ersten Zeile steht. Änderung auf `development` committed (`d6beab6`); neuer Release-Build und Gerätetest stehen noch aus.
- Für diese Erweiterung war keine Room-Schemaänderung nötig; Datenbankversion 1 bleibt bestehen.
- Der Quellstand wurde auf Version 0.6.0 (`versionCode = 8`) angehoben. Build und Gerätetest für diese Erweiterung stehen noch aus.
- Room `2.8.3` ist eingebunden (Kotlin KAPT als Compiler-Anbindung). Die lokale Datenbank `topf_secret.db` wird bei Bedarf über `TopfSecretDatabase.getInstance(context)` erstellt.
- Datenmodell v1: `RecipeEntity` mit Titel, Beschreibung, Kategorie, Portionen, Zeiten, Bild-/Quellen-URI, Notizen, optionalen Nährwert-Schätzwerten pro Portion, Favoritenstatus und Zeitstempeln. Zutaten (`IngredientEntity`) und Zubereitungsschritte (`PreparationStepEntity`) sind normalisiert und per Fremdschlüssel mit Cascade Delete verknüpft.
- Sicherheitsarchitektur für lokale Profile ist in `docs/SECURITY_ARCHITECTURE.md` dokumentiert (Commit `0cad4fa`). Sie sieht eine verschlüsselte gemeinsame SQLCipher-Datenbank und je eine separat verschlüsselte private Datenbank pro Profil vor. Die gemeinsame Datenbank wird für beide Profile unabhängig freigegeben; private Datenbankschlüssel bleiben profilgebunden. Private Bilddateien müssen ebenfalls separat verschlüsselt werden.
- SQLCipher for Android `net.zetetic:sqlcipher-android:4.20.0` und `androidx.sqlite:sqlite:2.7.0` sind eingebunden. Room 2.8.3 wird über `net.zetetic.database.sqlcipher.SupportOpenHelperFactory` angebunden; die vorhandene Produktivdatenbank wird weiterhin unverändert mit dem bisherigen Room-Builder geöffnet.
- Instrumentierungstest `SqlCipherRoomSmokeTest` nutzt eine separate Datenbank und einen zufälligen Testschlüssel: Room-Datensatz schreiben/lesen, verschlüsselten Dateiheader prüfen und Wiederöffnung mit demselben Schlüssel testen. GitHub Actions führt `connectedDebugAndroidTest` auf einem API-35-Emulator aus. Wegen eines beobachteten Emulator-/ADB-Ausfalls wurde der Runner vorläufig auf `ubuntu-22.04` festgesetzt; der nächste Lauf muss die Stabilität erst bestätigen. CI-Ergebnis steht noch aus. Noch kein Keystore- oder Produktionsschlüsselcode eingebaut.
- `RecoveryCodeGenerator` erzeugt 24 kryptografisch zufällige Crockford-Base32-Zeichen (120 Bit) und formatiert sie in sechs Vierergruppen. Die Eingabe-Normalisierung akzeptiert Bindestriche/Leerzeichen und Groß-/Kleinschreibung, lehnt aber Zeichen außerhalb des Alphabets ab. Der Generator ist noch nicht in die Einrichtung eingebunden.
- Unit-Tests für Format, Alphabet, Normalisierung und ungültige Codes sind angelegt. GitHub Actions führt vor dem Release-Build jetzt `testReleaseUnitTest` aus. Ein Lauf wurde für diese Commits noch nicht ausgeführt/bestätigt.
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
- Zwei lokale persönliche Profile (Nutzer und Ehefrau) mit getrennten PINs und privaten Rezeptbereichen.
- Gemeinsame Rezepte bleiben für beide Profile sichtbar; bestehende Rezepte werden bei der Einführung als gemeinsam übernommen.
- PIN-Ersteinrichtung mit freier PIN-Wahl; PIN nicht im Klartext speichern und Fehlversuche begrenzen.
- Optionale biometrische Entsperrung (z. B. Fingerabdruck) über Android BiometricPrompt; PIN bleibt immer als Alternative verfügbar.
- Jeder Nutzer erhält einen eigenen, automatisch auf dem Gerät generierten Wiederherstellungscode. Vereinbartes Format: 24 Zeichen aus einem Verwechslungen vermeidenden Base32-Zeichensatz, lesbar in sechs Vierergruppen mit Bindestrichen (120 Bit Zufall bei 32 Symbolen).
- Der Wiederherstellungscode wird bei der Ersteinrichtung vollständig angezeigt, mit einer Schaltfläche kopierbar gemacht und kann vom Nutzer als Screenshot oder Notiz gesichert werden. Der Nutzer muss die Sicherung bestätigen, bevor die Einrichtung abgeschlossen wird.
- Der vollständige Wiederherstellungscode wird nur einmal angezeigt und nicht im Klartext gespeichert oder protokolliert. Für die Prüfung/Wiederherstellung werden nur sichere kryptografische Ableitungen bzw. Verifier gespeichert.
- Der Code eines Profils stellt ausschließlich dieses Profil wieder her: Nach erfolgreicher Prüfung darf eine neue PIN festgelegt und die zugehörige Schlüsselhülle erneuert werden.
- Der Admin-Freischaltstatus bleibt unabhängig von den persönlichen PINs.
- Rezeptverwaltung: Rezepte manuell erstellen, bearbeiten und speichern.
- Webseiten-Rezeptimport mit Vorschau und ausdrücklicher Übernahme.
- Gerichtsbilder speichern.
- Nährwertberechnung, insbesondere kcal und Kohlenhydrate pro Portion und Portionsmenge.
- Vollständiger Rezept-Export/Import inklusive Bilder als Backup.
- Möglichst breite Android-Kompatibilität, vorläufig Android 8.0 / API 26.
- Öffentliches GitHub-Repository; private Daten und Zugangsdaten bleiben außerhalb des Repositories.

## Sicherheitsregeln
- Keine echten Rezepte, privaten Fotos, Backups, Zugangsdaten, Keystores oder Cloud-Schlüssel committen.
- PIN nie im Klartext speichern.
- Wiederherstellungscodes nie im Klartext speichern, loggen oder an einen Server übertragen.
- PIN und Wiederherstellungscode müssen mit kryptografisch geeigneten Verfahren geprüft werden; Fehlversuche begrenzen und Vergleiche sicher implementieren.
- Die Schlüsselarchitektur muss vor Implementierung festgelegt und geprüft werden. UI-Filter allein sind keine kryptografische Trennung privater Rezeptdaten.
- Biometrische Daten werden nicht von der App gespeichert; Android stellt nur das Authentifizierungsergebnis bereit.
- Synchronisation nur authentifiziert und auf berechtigte Personen begrenzt.
- Nährwerte als Schätzwerte kennzeichnen; keine medizinischen Therapieempfehlungen.
- Absturzprotokolle können technische Geräteinformationen enthalten; vor dem Teilen keine persönlichen Inhalte ergänzen.

## Nächste Schritte
1. Die vier GitHub Actions Secrets sicher hinterlegen.
2. Manuellen Workflow starten; die signierte APK wird danach im GitHub-Bereich Releases als direkte `.apk`-Datei veröffentlicht.
3. Nach dem Import-Fix einen neuen signierten Release-Build starten, APK-Signatur prüfen und Aktivierungsseite auf dem Gerät testen; eine Deinstallation nur dann durchführen, wenn die Signatur nicht mit der installierten Version übereinstimmt.
4. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
5. Version 0.6.0 in GitHub Actions bauen und auf dem Gerät testen: Hell-/Dunkelmodus über das Symbol oben rechts inklusive Neustart, Kontrast der „Guten Appetit“-Karte, Rezeptzeiten, Nährwerte, Zutatenmengen sowie Anlegen/Bearbeiten/Löschen und Datenpersistenz.
6. Rezept-Suche, Kategorienübersicht, Kategorienfilter, Favoritenübersicht und Info-Dialog (Logo, Autorzeile, dynamische App-Version) sind im Quellstand umgesetzt; GitHub-Actions-Build und Gerätetest stehen noch aus.
7. SQLCipher-/Keystore-/Biometric-Prototyp für Room 2.8.3, compileSdk 36 und API 26+ bauen und im CI validieren.
8. Danach sichere Schlüsselverwaltung, Datenbankmigration v1 mit Erhalt aller bisherigen Rezepte als gemeinsam, zwei private Datenbanken, PIN-/Wiederherstellungsablauf, Biometrie und verschlüsselte Bilddateien schrittweise implementieren und testen.

- Build #22 (Commit `d1ce3f3`) wurde am 26.11.2026 während des SQLCipher-Instrumentierungsschritts vom Nutzer abgebrochen. Im Log war `adb: device 'emulator-5554' not found` sichtbar; dadurch wurde weder der SQLCipher-Test bestätigt noch ein APK-Build gestartet. Der Workflow ist anschließend auf `ubuntu-22.04` festgesetzt worden. Auch Build #23 wurde während desselben Emulator-Schritts abgebrochen; die Runner-Änderung allein genügte nicht. Als nächste gezielte Maßnahme wurden die AVD-Datengröße auf `6G` begrenzt, Emulator-Grafikoptionen explizit gesetzt, das Boot-Timeout auf 900 Sekunden erhöht und eine Datenträgerdiagnose ergänzt. Ein erfolgreicher CI-Lauf steht weiterhin aus.

- Der Emulator-Runner-Bericht zu `adb: device 'emulator-5554' not found` dokumentiert unter anderem einen nicht gestarteten Emulator durch eine zu große AVD-Userdata-Partition bei knappem Runner-Speicher. Daher wird die AVD-Datengröße jetzt explizit auf 6G begrenzt; die konkrete Ursache unseres Laufs ist ohne den vollständigen Emulator-Startlog noch nicht abschließend bewiesen.

- Die Datenträgerdiagnose in Build #24 zeigte 85 GB frei (146 GB gesamt, 42 % belegt); Speichermangel war in diesem Lauf damit nicht die Ursache. Der Emulator meldete außerdem ausdrücklich, dass die Disk-Anforderungen erfüllt waren.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.
