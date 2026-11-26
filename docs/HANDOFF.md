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
- `RecipeDao` bietet Flow-Beobachtung aller Rezepte, einzelner Rezepte und Kategorien sowie transaktionales Speichern mit Ersetzen der Zutaten und Schritte. Der Rezepte-Bereich ist mit Room verbunden; Rezeptliste, Detailansicht und Eingabemaske unterstützen den ersten lokalen CRUD-Ablauf. Die Rezeptliste bietet eine Live-Suche über Rezeptname, Kategorie, Beschreibung und Zutatenname sowie einen Kategorienfilter. Kategorien werden aus den gespeicherten Rezepten abgeleitet; dafür war keine Room-Schemaänderung nötig. Migrationen über Datenbankversion 1 hinaus sind noch nicht umgesetzt.
- Pushes auf `development` lösen keinen Build mehr aus. Builds werden manuell über GitHub Actions → Android CI → Run workflow gestartet. Pull Requests gegen `development` lösen weiterhin automatisch einen Build aus.
- CI erstellt ein signiertes Release-APK und benennt es vor der Veröffentlichung nach der GitHub-Buildnummer, z. B. `topf_secret_#18.apk`.
- Manuelle Builds (`workflow_dispatch`) veröffentlichen die APK als direkt herunterladbares `.apk`-Asset in einem GitHub-Release mit Tag `build-<Run-Nummer>` (als Pre-Release markiert). Der Release-Titel enthält App-Version und Buildnummer (`Topf Secret v<Version> – Build #<Nummer>`); der Beschreibungstext nennt zusätzlich Version, Buildnummer und Commit-SHA. Dadurch lässt sich der installierte Stand eindeutig zuordnen. Pull-Request-Builds veröffentlichen keine Releases.
- Release-Bestand am 2026-11-26 geprüft: Es existieren zwei Releases (`build-21` und `build-29`), beide tragen aktuell denselben generischen Namen „Topf Secret Build“. Es handelt sich nicht um ein doppeltes Release, sondern um zwei unterschiedliche Builds. Die vorhandenen Release-Titel konnten über die hier verfügbare GitHub-Aktion nicht nachträglich geändert werden; neue manuelle Releases erhalten den eindeutigen Titel.
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
- Gemeinsames, PIN-freies Kochbuch für Nutzer und Ehefrau; keine privaten Rezeptbereiche.
- Die normale Android-Bildschirmsperre schützt den Gerätezugriff.
- Admin-Freischaltung bleibt davon unabhängig bestehen.
- Rezepte manuell erstellen, bearbeiten und speichern.
- Rezeptbilder aus Galerie, Kamera und Online-Bildersuche auswählen und anzeigen.
- Webseiten-Rezeptimport mit Vorschau und ausdrücklicher Übernahme.
- Nährwertberechnung, insbesondere kcal und Kohlenhydrate pro Portion und Portionsmenge.
- Vollständiger Rezept-Export/Import inklusive Bilder als Backup.
- Möglichst breite Android-Kompatibilität, vorläufig Android 8.0 / API 26.
- Öffentliches GitHub-Repository; private Daten und Zugangsdaten bleiben außerhalb des Repositories.

## Datenschutz- und Sicherheitsregeln
- Keine persönlichen PINs, Wiederherstellungscodes, privaten Profile oder biometrischen Zugangsdaten in Topf Secret.
- Keine echten Rezepte, privaten Fotos, Backups, Zugangsdaten, Keystores oder Cloud-Schlüssel committen.
- Die bestehende Admin-Aktivierung bleibt getrennt von der Rezeptverwaltung.
- Nährwerte als Schätzwerte kennzeichnen; keine medizinischen Therapieempfehlungen.
- Absturzprotokolle können technische Geräteinformationen enthalten; vor dem Teilen keine persönlichen Inhalte ergänzen.

## Nächste Schritte
1. Neue Online-Suchlogik auf dem Gerät testen: ausschließlich Wikimedia-Bitmap-Bilder (MIME image/*), Audio-/Video-Dateien ausschließen, mehrere deutsche/englische Suchvarianten nutzen und nur Titel mit Gerichte-/Speisenhinweisen anzeigen; besonders „Grünkohl“ und „Grünkohl Eintopf“ testen.
2. Bildgrößenoptimierung und Fehlerbehandlung für Galerie-, Kamera- und Onlinebilder ergänzen.
3. Gradle Wrapper aus vertrauenswürdiger Gradle-8.13-Installation ergänzen und CI darauf umstellen.
4. Version 0.6.0 in GitHub Actions bauen und auf dem Gerät testen: Hell-/Dunkelmodus, Rezeptzeiten, Nährwerte, Zutatenmengen sowie Anlegen/Bearbeiten/Löschen und Datenpersistenz.
5. Backup/Restore einschließlich aller Rezeptbilder und ihrer Quellenbelege umsetzen.
6. Danach Web-Rezeptimport und Zwei-Wege-Synchronisation planen.

## Aktueller Funktionsschritt
- PIN-/Biometrie-/Recovery-Code-/SQLCipher-Prototyp vollständig verworfen und aus dem App-Projekt entfernt. Die produktive Room-Datenbank blieb unverändert.
- CI führt keine Android-Emulator-Instrumentierung mehr aus; Release-Build und Unit-Tests laufen weiterhin.
- Rezeptbilder: Galerieauswahl über Android Document Picker; ausgewählte Bilddateien werden in `filesDir/recipe_images` kopiert, damit sie nicht von externen URI-Berechtigungen abhängen. Kameraaufnahme erfolgt über Android `TakePicture` und einen nicht exportierten `FileProvider`; auch Kamerabilder landen direkt im internen App-Speicher.
- Bildvorschau in Rezeptliste, Detailansicht und Editor; Bild ändern/entfernen. Alte intern verwaltete Bilddateien werden nach erfolgreichem Speichern ersetzt/entfernt; neu importierte Dateien werden beim Abbrechen des Editors bereinigt.
- Speicherung verwendet das bereits vorhandene Feld `RecipeEntity.imageUri`; Room-Schema bleibt Version 1. `AndroidManifest.xml` registriert den FileProvider und `res/xml/file_paths.xml` begrenzt den freigegebenen Pfad auf `recipe_images/`.
- Online-Gerichtebildersuche ergänzt: Suche nach Gerichtsnamen über die Wikimedia-Commons-Action-API, Treffer mit Vorschaubildern und Lizenz-/Urheberangaben; Auswahl lädt ein Vorschaubild in `filesDir/recipe_images` herunter. Ein begleitender `.source.txt`-Beleg speichert Commons-Dateiseite, Thumbnail-URL, Lizenz, Urheber und Attribution. Die Bildquelle wird beim Löschen/Ersetzen der verwalteten Bilddatei mit entfernt. Dafür ist die Android-Internetberechtigung erforderlich. Es werden keine Google-Bilderseiten gescrapt und kein API-Schlüssel benötigt.
- Online-Suche ist auf Gerichtefotos begrenzt; keine Rezept- oder Zutaten-Websuche. Ergebnisse ohne auslesbare Lizenzangabe werden ausgelassen. Vor der Übernahme werden Lizenz und Urheber im Treffer angezeigt.
- Suchlogik nach Grünkohl-Test verbessert (Commit `12c6c1e`, danach `0ce220f`): mehrere Suchvarianten pro Anfrage (u. a. „Gericht“, „gekocht“, „food“, „dish“; bei Grünkohl zusätzlich „Grünkohl mit Pinkel“, „Grünkohleintopf“, „Kale stew/soup“); Wikimedia-Treffer werden anhand `mediatype=BITMAP` und `mime=image/*` auf echte Bitmap-Bilder begrenzt. Audio-/Videoformate sowie offensichtliche Pflanzen-/Botanik-/Sound-Titel werden ausgeschlossen. Titel müssen außerdem einen Speisen-/Gerichtehinweis enthalten; passende Titel werden höher priorisiert. Build und Gerätetest dieser neuen Suchlogik stehen noch aus.
- Gerätetest des Nutzers bestätigt: Online-Suche, Galerie und Kamera funktionieren grundsätzlich. Beim Praxistest mit „Grünkohl“ und „Grünkohl Eintopf“ lieferte die Online-Suche jedoch überwiegend Gemüse-/Pflanzenmotive und Audio-Dateien statt zubereiteter Gerichte. Ursache im aktuellen Code: Wikimedia Commons `generator=search` sucht nur nach Dateititeln und die Implementierung filtert Dateityp/Lizenz, aber nicht semantisch nach fertigen Speisen. Nächster Schritt: Suchstrategie und Ergebnisfilter gezielt auf Food-/Dish-Fotos verbessern, Audio-Dateien und Pflanzenbilder zuverlässig ausschließen und Suchbegriffe bei Bedarf erweitern.
- Build 27 (`45770453918`) scheiterte an einer ungültigen Activity-Label-Referenz (`Unresolved label`). Der darauf folgende Build 28 (`45770905010`) zeigte, dass auch `this@MainActivity@onCreate` syntaktisch ungültig ist (`Unresolved label`, `Expecting ','`, `Unresolved reference onCreate`). Korrektur auf `development` committed: `MainActivity` erfasst `applicationContext` vor `setContent`, übergibt ihn explizit an `TopfSecretApp`, und die Bildbereinigung verwendet diesen Context-Parameter statt Kotlin-Labels. Commit `15bf57bc23029b4463a4029b8fb5b35cbf18665d`; Build 29 zur Verifikation steht noch aus.
- Nutzerbestätigung: Galerie, Kamera und Online-Suche funktionieren im Gerätetest grundsätzlich. Die Ergebnisqualität der Online-Suche ist noch unzureichend (Grünkohl-Test: Gemüse-/Pflanzenmotive und Audio-Dateien statt Gerichte); Suchlogik und Filter müssen nachgebessert werden.
- Noch offen: gezielte Ergebnisfilter/Suchverbesserung, Netzwerkfehler/Commons-Rate-Limits, Galerieformate und Kamera-Apps sowie Bildgrößen-/Speicheroptimierung.

## Letzter Hinweis
Die Versionsstände der Gradle-/Android-Plugins und Bibliotheken müssen beim ersten echten Build gegen die verfügbare Android-Studio-/SDK-Umgebung geprüft und gegebenenfalls aktualisiert werden.


## Ergänzung: Nährwert- und Einheitenberechnung (in Arbeit)
- Vereinbart: Lebensmittel per Barcode erkennen, Produktname und verfügbare Nährwerte übernehmen und alle berechenbaren Zutaten eines Gerichts addieren. Anzeige später für Gesamtgericht und je Portion, ausdrücklich als Schätzwert.
- Einheiten: mg/g/kg, ml/cl/dl/l sowie EL/TL/Tasse. Stück, Prise, Bund usw. nur mit bekanntem Stück-/Referenzgewicht.
- Open Food Facts API v3 ist als Barcode-Datenquelle vorgesehen. Produktabfragen erfolgen über `/api/v3/product/{code}` mit eigenem User-Agent; Lesezugriffe benötigen keinen API-Schlüssel. Die Daten können unvollständig/fehlerhaft sein. ODbL-/Datenlizenz-Attribution und Share-Alike-Bedingungen sind einzuhalten.
- Implementiert auf `development`: Room-Schema v2 mit optionalem Barcode, Nährwerten pro 100 g bzw. 100 ml und Quellenfeld je Zutat; Migration 1→2 erhält vorhandene Rezeptdaten. Zutatenparser erkennt zusätzlich mg, dl, Esslöffel, Teelöffel, Tassen und weitere Küchenmaße.
- Neue Rechenbasis `app/src/main/java/de/topfsecret/app/nutrition/IngredientNutrition.kt`: Gewichts-/Volumennormalisierung, definierte EL/TL/Tassen-Volumina und begrenzte lebensmittelspezifische Dichtewerte (z. B. Wasser, Milch, Öl, Honig). Volumen→Masse wird ohne bekannte Dichte bewusst nicht geraten. EL/TL/Tasse und Dichtewerte werden als Schätzungen markiert.
- Noch NICHT fertig/getestet: Barcode-Scanner-UI, API-Client, Übergabe der Barcode-Nährwerte in Zutaten, automatische Berechnung/Anzeige im Rezepteditor und in der Detailansicht, Unit-/Migrationstests und Android-Build.


### Fortschritt Barcode/Nährwerte (2026-11-26)
- Google Code Scanner integriert (play-services-code-scanner:16.1.0) und Barcode-UI im Rezepteditor ergänzt. Verwendet Google Play-Dienste; die App benötigt dafür keine eigene Kamera-Berechtigung. Unterstützte Formate im aktuellen Button: EAN-13, EAN-8, UPC-A und UPC-E. Scanner-Modul wird über Manifest-Metadaten zum Installationszeitpunkt angefordert.
- OpenFoodFactsClient fragt für einen erkannten Barcode die Open Food Facts API v3 ab. Angefordert werden Produktname (de/en), Marke und Nährwerte. Verwendet werden kcal (oder Umrechnung von kJ), Kohlenhydrate, Eiweiß und Fett je 100 g. Netzwerk-Timeouts, unbekannte Barcodes und fehlende Produktnamen werden abgefangen.
- Nach Scan erscheint eine Produktvorschau mit verfügbaren Nährwerten und bewusster Bestätigung. Bei Übernahme wird eine 100-g-Zeile in die Zutatenliste eingefügt; Menge kann anschließend editiert werden. Barcode, Nährwerte und Quellen-URL werden pro Zutat in Room gespeichert und beim erneuten Öffnen des Editors wieder geladen.
- Rezepteditor summiert aktuell erkannte Zutaten mit Nährwertdaten und zeigt Gesamtgericht sowie je Portion. Es wird angezeigt, wie viele Zutaten berechnet werden konnten; EL/TL/Tasse und Dichteumrechnungen werden als Schätzung kenntlich gemacht. Manuelle Nährwertfelder bleiben als Rückfall erhalten, wenn keine Barcode-Zutaten berechnet werden.
- Unit-Tests für mg/g/kg, ml/l/dl, EL/TL, Dichteumrechnung und deutsche Dezimalmengen hinzugefügt.
- Achtung: noch kein bestätigter Android-Build oder Gerätetest dieses neuen Schritts. Vor dem nächsten Release Build/CI ausführen und auf dem Telefon testen (Scanner-Modul/Google Play-Dienste, Barcode unbekannt, Produkt ohne Nährwerte, Mengenänderung, bestehende Datenbankmigration v1→v2).
- Open Food Facts-Daten sind community-gepflegt und können unvollständig oder fehlerhaft sein. ODbL-/Datenlizenzbedingungen und Quellenhinweis sind zu beachten; API-Limits beachten (Produktabfragen 15/min/IP). API-Doku: https://openfoodfacts.github.io/documentation/docs/Product-Opener/api/.


### Ergänzung Produktsuche nach Barcode (2026-11-26)
- Nutzerwunsch: Wenn ein Barcode nicht erkannt/abgerufen werden kann, der Produktname fehlt oder die Produktdaten nicht brauchbar sind, soll automatisch ein Such-Popup erscheinen. Die Produktsuche soll außerdem jederzeit manuell aus der Zutatenverwaltung geöffnet werden können.
- Umsetzung auf `development`: Open Food Facts Produktsuche über die Such-API ergänzt. Suche nach Lebensmittelbegriff oder Marke, Treffer zeigen Produktname/Marke und ob Nährwerte vorliegen. Auswahl öffnet die vorhandene Produktvorschau; Übernahme speichert die gefundenen Nährwerte wie beim Barcode-Scan.
- Wenn kein Treffer verfügbar ist, kann der Suchbegriff als normale Zutat ohne Produktdaten übernommen werden. Der Barcode ist damit optional und darf die Rezeptbearbeitung nicht blockieren.
- Fehler beim Barcode-Lookup, fehlender erkannter Barcode und Scannerfehler öffnen automatisch das Such-Popup. Zusätzlich gibt es neben „Barcode scannen“ die manuelle Aktion „Produkt suchen“.
- Datenquelle bleibt Open Food Facts; Produktdaten sind community-gepflegt und können unvollständig oder ungenau sein. Nährwerte bleiben Richtwerte.
- Noch offen: Android-Build/Unit-Tests und Gerätetest der Suche (z. B. „Semmelknödel“, Markenfilter, kein Treffer, API nicht erreichbar, Barcode 22018632).

### Erweiterung Produktquellen und lokale Barcode-Korrektur (2026-11-26)
- Open Food Facts Produktsuche wurde vom Legacy-Endpunkt `/cgi/search.pl` auf Search-a-licious `POST https://search.openfoodfacts.org/search` umgestellt. Die offizielle API dokumentiert JSON-POST mit `q`, `langs`, `page_size`, `page`, `boost_phrase` und `fields`; Antwortparser berücksichtigt Elasticsearch-`hits.hits` sowie `_source`. API-Referenz: https://openfoodfacts.github.io/search-a-licious/users/ref-openapi/.
- Barcode-Lookup prüft jetzt den zurückgegebenen Produktcode gegen den tatsächlich gescannten Barcode und weist abweichende Treffer zurück, statt sie als richtig auszugeben.
- Room-Datenbank auf Version 3 angehoben; Migration 2→3 ergänzt `local_food_products`. Die lokale Produkttabelle speichert Barcode, Name, Marke, Makronährwerte, Quelle, Qualitätsstatus und Änderungszeit.
- Im Rezepteditor werden lokale Produktzuordnungen vor Online-Lookups verwendet. Manuelle Suche zeigt lokale Produkte zuerst und kann diese auch dann anzeigen, wenn Open Food Facts nicht erreichbar ist.
- Produktvorschau bietet „Angaben korrigieren“: Barcode, Produktname, Marke sowie kcal/Kohlenhydrate/Eiweiß/Fett je 100 g lassen sich bearbeiten. Korrekturen werden lokal mit Status `VERIFIED_LOCAL` gespeichert; bestätigte Online-Treffer bleiben als `UNVERIFIED` gekennzeichnet. Ein geänderter Barcode ersetzt die alte lokale Zuordnung.
- Wichtige Einschränkung: Die lokale Produkttabelle ist geräteintern und noch nicht Bestandteil des Backup-/Restore-Formats. Export/Import dieser Daten muss beim Backup-Projekt ergänzt werden.
- Noch NICHT bestätigt: Android-Build, Room-Migration auf einem Gerät mit bestehenden v2-Daten, reale Search-a-licious-Antworten/„Semmelknödel“-Suche sowie Scanner-Regressionstest.
- Quellenrecherche für weitere kostenlose Suchanbieter: BLS 4.0 bietet mehr als 7.000 deutsche Lebensmittel/Gerichte und ist unter CC BY 4.0 kostenfrei nutzbar; MRI-Nennung erforderlich. Daten-Download: https://blsdb.de/download; MRI-Infos: https://www.mri.bund.de/de/institute/ernaehrungsverhalten/forschungsbereiche/bundeslebensmittelschluessel-bls/.
- Livsmedelsverket (Schwedische Lebensmittelbehörde) stellt eine JSON-API für ca. 2.500 Lebensmittel mit CC BY 4.0 bereit; Quellenangabe erforderlich: https://www.livsmedelsverket.se/en/about-us/open-data/food-composition-data.
- USDA FoodData Central ist CC0 und kostenlos, benötigt für die API jedoch einen data.gov-API-Key. Kein Schlüssel darf in die öffentliche App oder das Repository eingebettet werden; Integration erst mit sicherem Proxy oder vom Nutzer verwaltetem Schlüssel erwägen: https://fdc.nal.usda.gov/api-guide/.
- UK CoFID ist als herunterladbarer Datensatz auf GOV.UK verfügbar; vor einer Einbindung sind aktuelle Lizenz-/Attributionsbedingungen und Datenformat zu prüfen: https://www.gov.uk/government/publications/composition-of-foods-integrated-dataset-cofid.


## Quellen & Danksagungen / Transparenz
- Auf development wurde ein Drei-Striche-Menü auf der Startseite ergänzt, das zur Seite „Quellen & Danksagungen“ führt.
- Die Seite nennt den BLS 4.0 mit offizieller Zitierangabe des Max Rubner-Instituts und CC BY 4.0, Open Food Facts mit ODbL 1.0 sowie Wikimedia Commons mit dateispezifischen Lizenz-/Urheberangaben.
- Der BLS-Hinweis enthält eine ausdrückliche Nicht-Unterstützungs-/Nicht-Affiliationsklausel. Weitere Quellen und Mitwirkende können ergänzt werden; persönliche Namensnennungen nur mit Zustimmung.
- Rechtliche Umsetzung ist noch nicht abschließend geprüft: Vor Einbindung des BLS-Datensatzes müssen die Originaldateien, Metadaten, Lizenzhinweise und etwaige Änderungen/Aufbereitungen konkret dokumentiert werden. Für Open Food Facts ist die ODbL einschließlich etwaiger Share-Alike-Pflichten gesondert zu beachten.
- Die neue UI ist noch nicht gebaut oder auf einem Gerät getestet.


## BLS 4.0 – Import und Suche (Entwicklung)
- Room-Schema v4 ergänzt Tabelle `bls_foods` samt DAO und Migration 3→4. Gespeichert werden BLS-Code, deutsche/englische Bezeichnung, Energie (ENERCC), Protein (PROT625), verfügbare Kohlenhydrate (CHO), Fett (FAT) je 100 g sowie je Makronährstoff die originale Datenherkunft und Referenz aus den jeweils folgenden XLSX-Spalten.
- `BlsXlsxImporter` liest die offizielle BLS-4.0-Hauptdatei direkt aus einer vom Nutzer ausgewählten XLSX-Datei. Der Import verarbeitet das erste Tabellenblatt und erkennt die Spalten über die BLS-Codes; Werte, die fehlen oder nicht numerisch sind, bleiben null und werden nicht zu null Gramm umgedeutet.
- Im Zutaten-/Produktsuchdialog gibt es Schaltflächen zum Import der BLS-XLSX-Datei und zur Suche in den lokal importierten BLS-Lebensmitteln. Ein Treffer kann als Zutat übernommen werden; BLS-Code und Quelle werden in der Rezeptzutat kenntlich gemacht. BLS-Datensätze werden nicht in den Barcode-Produktkatalog geschrieben.
- Der Import speichert derzeit die vier für die bestehende Rezeptberechnung benötigten Makronährwerte; die übrigen 134 Nährstoffkomponenten werden noch nicht in Room übernommen. Datenherkunft/Referenzen der vier gespeicherten Werte bleiben erhalten.
- BLS-Import ist noch nicht mit der Originaldatei auf einem Gerät verifiziert; Build und Test stehen aus. Vor Freigabe muss der Import mit der offiziellen BLS_4_0_Daten_2025_DE.xlsx getestet werden.
- Android CI wurde so angepasst, dass Pushes auf `development` automatisch einen Build starten; Release-Veröffentlichung bleibt weiterhin manuell.


### Absturzdiagnose BLS-Datenbankmigration (2026-11-26)
- Nutzer-Gerätetest nach Installation von App 0.6.0 auf Samsung SM-S908B / Android 16 stürzte beim Öffnen der Room-Datenbank ab.
- Absturzprotokoll: `Migration didn't properly handle: bls_foods`. Die Migration 3→4 legt den Index `index_bls_foods_germanName` an, aber `BlsFoodEntity` hatte diesen Index nicht in der `@Entity`-Definition deklariert. Room erwartete deshalb ein Schema ohne Index und fand einen zusätzlichen Index.
- Korrektur auf `development`: `BlsFoodEntity` deklariert jetzt `Index(value = ["germanName"])`; dies stimmt mit dem bereits vorhandenen SQL in Migration 3→4 überein. Datenbankversion und Tabellen-/Spaltenstruktur bleiben unverändert; kein Datenbank-Reset und keine destructive migration.
- Noch offen: CI-Build des Korrektur-Commits und erneuter Installationstest auf dem betroffenen Gerät. Bis zur Bestätigung ist der Startfehler als behoben im Code, aber noch nicht auf dem Gerät verifiziert zu behandeln.


## BLS 4.0 – Offline-Vollintegration (2026-11-26)

- Der Nutzer hat den komprimierten Datensatz `app/src/main/assets/bls_foods_v4.jsonl.gz` auf `development` hochgeladen. GitHub-Blob-SHA: `827fc98c0a296d0eb64062f80cd31738f8a20f22`; lokale SHA-256 vor Upload: `96e3f2aaf98fa538f84dd6d826a36b1a7565e87f2520bb80b2d9b4aa51ec0bbf`.
- Umfang: 7.140 Lebensmittel mit jeweils 138 Nährstoffkomponenten. Alle Nährstoff-Tripel (Wert, Datenherkunft, Referenz) werden in `nutrientsJson` gespeichert; zusätzliche Datensatzhinweise in `note`. Textmarker wie `&lt;LOQ`, `&lt;LOD` und `-` bleiben im JSON erhalten und werden nicht als numerische Null interpretiert.
- Room-Schema v5 ergänzt `nutrientsJson` (Standardwert `{}`) und `note` über die nicht-destruktive Migration 4→5. Bestehende Rezepte, Zutaten und lokale Produktdaten bleiben erhalten.
- `BlsDatasetSeeder` liest das gzip-JSONL offline aus den App-Assets. Die Vollständigkeit wird anhand der erwarteten 7.140 Datensätze geprüft; ein unvollständiger Seed wird erneut aufgebaut. Die Suche sichert die Initialisierung zusätzlich ab.
- Der manuelle XLSX-Importer und dessen Dateiauswahl wurden entfernt. Die Rezeptbild-Dateiauswahl bleibt bestehen.
- ENERCC, PROT625, CHO und FAT werden für die bestehende Rezeptberechnung in die Makrospalten gespiegelt; alle 138 Nährstoff-Tripel bleiben parallel erhalten.
- **Status:** Code committed auf `development`. Android-CI-Build, Room-Migrationstest und Gerätetest sind noch offen. Die Integration ist noch nicht als auf dem Gerät verifiziert zu betrachten.
- Lizenz: Max Rubner-Institut (2025): Bundeslebensmittelschlüssel (BLS), Version 4.0, Karlsruhe; CC BY 4.0. Änderungen/Aufbereitung kenntlich machen; keine Unterstützung oder Prüfung durch das MRI suggerieren.

## CI-Trigger-Policy (maßgeblich, 2026-11-26)

- Android-CI läuft bei Pull Requests gegen `development` und bei manuellem Start (`workflow_dispatch`).
- Kein automatischer `push`-Trigger. Ältere Dokumentationsstellen, die einen Push-Build erwähnen, sind überholt.
- Ausschließlich `development` bearbeiten; `main` bleibt unangetastet.


## BLS-Suche: Korrektur nach Gerätetest-Rückmeldung (2026-11-26)

- Der Nutzer meldete, dass der Hinweis „BLS 4.0 wird offline vorbereitet …“ dauerhaft sichtbar blieb und Semmelknödel nicht gefunden wurden. Ursache im UI: Der Hinweis war nur ein initialer Platzhalter; die BLS-Abfrage war von der separaten Schaltfläche „BLS suchen“ getrennt. Die normale Schaltfläche „Suchen“ fragte ausschließlich lokalen Produktkatalog und Open Food Facts ab.
- Korrektur auf `development`: BLS wird nun bei der normalen Produktsuche zuerst initialisiert/validiert und parallel in die Trefferliste aufgenommen; anschließend folgen lokale Produktkatalog- und Online-Treffer. Die separate BLS-Schaltfläche und der irreführende Dauerhinweis wurden entfernt.
- Der Datensatz enthält nach Prüfung u. a. „Semmelknödel“, „Semmelknödel roh“, „Semmelknödel gekocht“ und „Semmelknödel getrocknet“.
- **Status:** UI-Code committed (Commit `5b451330263a65c1a3b2fb4ccbb8155c799354dc`). Build und erneuter Gerätetest stehen noch aus.


### BLS-Suche – APK-Bereitstellung und Update (2026-11-26)
- GitHub Actions Build #41 (`0a06d5168d2facf87d4bd0bfd8258d3afcce9197`) lief erfolgreich; die signierte APK wurde als Release `build-41` veröffentlicht. Die APK-Größe stieg gegenüber Build #39 von ca. 11,5 MB auf ca. 15,7 MB, konsistent mit dem gebündelten BLS-Asset (3,72 MB komprimiert). Der Android-Standardpfad `app/src/main/assets` wird ohne zusätzliche Gradle-Konfiguration in die APK übernommen.
- Für ein Update über eine vorhandene Installation muss `versionCode` steigen. Deshalb wurde auf `development` auf `versionCode = 9`, `versionName = 0.6.1` angehoben und die Release-Metadaten der CI angepasst. Die Room-Datenbank wird nicht zurückgesetzt; bestehende Rezepte bleiben erhalten.
- Nächster Schritt: manuellen Android-CI-Workflow auf `development` starten, APK-Asset/Build prüfen und auf dem Gerät installieren. Gerätetest der BLS-Suche (z. B. „Semmelknödel“) steht weiterhin aus.


### APK-Prüfung BLS-Asset (2026-11-26)
- Nutzer bestätigt: Version 0.6.1 aus Release `build-42` installiert; Fehler beim Öffnen von `bls_foods_v4.jsonl.gz` bleibt bestehen.
- Git-Tree des Build-42-Commits `28409a0935b575a535efaf33fa47efcfae93927b` enthält `app/src/main/assets/bls_foods_v4.jsonl.gz` mit 3.720.228 Bytes und Blob-SHA `827fc98c0a296d0eb64062f80cd31738f8a20f22`.
- Build #42 war erfolgreich; das beweist allein nicht, dass das Asset in der APK enthalten ist.
- CI-Workflow wurde auf `development` um einen harten APK-Integritätstest ergänzt: `unzip -Z1` muss `assets/bls_foods_v4.jsonl.gz` finden und der aus der APK extrahierte SHA-256 muss `96e3f2aaf98fa538f84dd6d826a36b1a7565e87f2520bb80b2d9b4aa51ec0bbf` entsprechen. Der nächste manuelle Build soll die Frage eindeutig entscheiden.
- Bis zum erfolgreichen APK-Asset-Test und Gerätetest bleibt BLS-Lauffähigkeit unbestätigt. Keine Deinstallation oder Datenlöschung.


### BLS-Assetfehler durch Android-GZIP-Asset-Paketierung behoben (2026-11-26)
- Nutzer hat bestätigt, Release Build #42 / Version 0.6.1 installiert zu haben; der Fehler beim Laden der Datei blieb bestehen.
- Diagnose anhand des CI-Laufs #43 (45871865372): Build und APK-Erstellung waren erfolgreich, aber die neue Integritätsprüfung brach ab. Die APK enthält `assets/bls_foods_v4.jsonl` (nicht `.jsonl.gz`). Android/AAPT hat das als `.gz` eingecheckte Asset beim APK-Packaging expandiert und die `.gz`-Endung entfernt.
- Seeder korrigiert: öffnet nun `bls_foods_v4.jsonl` und liest direkt UTF-8-JSONL; `GZIPInputStream` entfernt. Erwartete Datensatzanzahl und die vollständigen 138 Nährstoff-Tripel bleiben unverändert.
- CI-APK-Prüfung korrigiert: fordert `assets/bls_foods_v4.jsonl` und prüft den SHA-256 des entpackten Inhalts `b58c764620ab5b3b28f81aa3d7caca23b4b2665b151642fde809fca25f4890a6`.
- Updateversion auf 0.6.2 / versionCode 10 erhöht, damit das nächste Release über 0.6.1 aktualisierbar ist, ohne Deinstallation oder Verlust der lokalen Rezepte.
- Noch ausstehend: neuer manueller CI-Lauf muss Build und APK-Asset-Prüfung bestehen; danach Geräteprüfung der BLS-Erstinitialisierung und Suche nach Semmelknödel.

- Zusatzhärtung nach Diagnose: Der Seeder erkennt jetzt anhand der GZIP-Magic-Bytes, ob das unter `assets/bls_foods_v4.jsonl` ausgelieferte Asset noch gzip-komprimiert oder bereits entpackt ist. Beide Varianten werden korrekt verarbeitet.
- CI extrahiert das Asset und hasht immer den entpackten JSONL-Inhalt; damit ist der Test unabhängig davon, ob AAPT den Quellinhalt beim Packaging entpackt oder nur den Namen ändert.

## Zutatenzeilen und erweiterte Nährwertberechnung (2026-11-26)

- Nutzergerätetest mit drei Screenshots zeigte, dass die Produktauswahl in der Zutatenliste den literalen Text `\\n` statt eines echten Zeilenumbruchs einfügte. Die Ursache lag in `joinToString("\\\\n")` bei der Produktübernahme. Beide Übernahmepfade verwenden nun echte Zeilenumbrüche; die Zutatenliste normalisiert zusätzlich eingefügte Escape-Sequenzen.
- Rezepteditor analysiert nun jede nichtleere Zutatenzeile getrennt. Bei nicht berechenbaren Zutaten werden die Originalzeile und ein konkreter Grund angezeigt (z. B. fehlendes Stückgewicht, nicht unterstützte Einheit, fehlende Produktdaten oder nicht lesbare Mengenangabe).
- Stückmengen wie „4 Semmelknödel“ werden ohne bekanntes Stückgewicht nicht geschätzt. Eine eindeutige Namensähnlichkeit zu einem ausgewählten Produkt/BLS-Treffer kann dessen Nährwertdaten weiterhin zuordnen, die Stückmenge bleibt aber unberechenbar.
- Nährwertdarstellung im Editor erfolgt zeilenweise für Energie, Kohlenhydrate, davon Zucker, Ballaststoffe, Eiweiß, Fett, davon gesättigte Fettsäuren und Salz. Werte werden auf maximal zwei Nachkommastellen formatiert. Nicht vollständig verfügbare Detailwerte werden nicht als 0 ausgegeben.
- BLS 4.0-Komponenten werden anhand der offiziellen Codes gelesen: `SUGAR` (Gesamtzucker), `FIBT` (Gesamtballaststoffe), `NACL` (Salzäquivalent) und `FASAT` (gesättigte Fettsäuren). Nichtnumerische Marker wie `<LOQ`, `<LOD` und `-` bleiben fehlend und werden nicht zu Nullwerten.
- Open Food Facts liefert zusätzlich `sugars_100g`, `fiber_100g`, `salt_100g` und `saturated-fat_100g`, sofern vorhanden. Die Werte werden im lokalen Produktkatalog und an Rezeptzutaten gespeichert.
- Room-Schema von Version 5 auf 6 erweitert; Migration ergänzt nullable Spalten für Zutaten, lokale Produkte und Rezept-Portionswerte. Keine Tabellen werden gelöscht und keine destructive migration wird verwendet.
- Rezeptdetail zeigt die zusätzlich berechneten Werte pro Portion. Unvollständige Rezeptberechnungen werden als Teilmenge gekennzeichnet und nicht als vollständige Rezeptwerte gespeichert.
- App-Version auf 0.6.3 / versionCode 11 angehoben, damit das Update über eine vorhandene Installation möglich ist.
- Unit-Tests für zusätzliche Nährstoffskalierung, fehlende Nährwerte und Stückmengen ergänzt.
- **Noch nicht verifiziert:** Android-CI-Build/Unit-Tests, Room-Migration auf einem Gerät mit bestehenden Rezepten, echte OFF-Antworten für zusätzliche Nährstoffe und Gerätetest der Zeilenübernahme. Vor Release manuellen Workflow auf `development` starten. Keine Deinstallation oder Datenbanklöschung.
- Letzter dokumentierter CI-Trigger bleibt `workflow_dispatch` bzw. Pull Request gegen `development`; Pushes starten keinen Build.
