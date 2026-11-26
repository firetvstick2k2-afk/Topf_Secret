# Topf Secret – Sicherheitsarchitektur für lokale Profile

Stand: 2026-11-26  
Status: Architektur dokumentiert; der reine Recovery-Code-Generator ist implementiert, Profil-/Schlüssel-/Datenbankfunktionen sind noch nicht implementiert.

## Ziele

- Zwei lokale Profile auf demselben Gerät: Andre und Ehefrau.
- Jede Person hat eine eigene PIN, einen eigenen Wiederherstellungscode und einen privaten Rezeptbereich.
- Gemeinsame Rezepte sind nach Anmeldung für beide Profile verfügbar.
- Bestehende Rezepte werden bei der Umstellung als gemeinsam behandelt.
- Optionaler Fingerabdruck bzw. eine andere vom Android-System unterstützte starke Biometrie.
- Die App speichert keine biometrischen Merkmale.
- Ein vergessener PIN darf nicht automatisch zum Verlust der Rezepte führen.

## Gewählte Datenaufteilung

Es werden drei logisch und kryptografisch getrennte SQLCipher-Datenbanken verwendet:

1. **Gemeinsam:** enthält Rezepte, die beide Profile sehen dürfen.
2. **Privat – Profil 1:** enthält ausschließlich private Rezepte von Andre.
3. **Privat – Profil 2:** enthält ausschließlich private Rezepte der Ehefrau.

Jede Datenbank erhält einen eigenen zufälligen Datenbankschlüssel (DEK). Die gemeinsame Datenbank besitzt eine Schlüsselhülle für jedes Profil, damit beide Profile sie unabhängig öffnen können. Der private Datenbankschlüssel eines Profils wird ausschließlich durch die Zugangsmittel dieses Profils freigegeben. Das jeweils andere Profil erhält keinen Schlüssel für diese private Datenbank.

Die App führt die sichtbare Rezeptliste aus der gemeinsamen Datenbank und der privaten Datenbank des aktuell angemeldeten Profils zusammen. Das ist eine echte Trennung auf Datenbank-Schlüsselebene und nicht nur ein Filter in der Benutzeroberfläche.

## PIN, Wiederherstellungscode und Schlüsselhüllen

- PIN und Wiederherstellungscode werden niemals im Klartext gespeichert.
- Für jeden Profilzugang werden zufällige Salze und authentifizierte Verschlüsselung (AES-GCM) verwendet.
- Die PIN wird nicht direkt als SQLCipher-Passwort benutzt. Aus PIN, individuellem Salt und einem nicht exportierbaren, app-installationsgebundenen Android-Keystore-Geheimnis wird mit einem speicherharten bzw. ausreichend aufwendigen Schlüsselableitungsverfahren ein Schlüssel zum Öffnen der Schlüsselhülle abgeleitet. Die konkrete KDF und Parameter werden vor Implementierung auf API 26+ getestet und dokumentiert.
- Das Keystore-Geheimnis dient als Pepper gegen einfache Offline-PIN-Wörterbuchangriffe auf kopierte App-Dateien. Es wird nicht in SharedPreferences oder in Git gespeichert.
- Die 24 Zeichen des Wiederherstellungscodes werden mit einem kryptografisch sicheren Zufallszahlengenerator erzeugt. Vorgesehen ist Crockford-Base32 ohne leicht verwechselbare Zeichen; 24 Symbole liefern 120 Bit Zufall. Die Anzeige erfolgt in sechs Vierergruppen.
- Der Wiederherstellungscode öffnet ausschließlich die Schlüsselhüllen des zugehörigen Profils. Nach erfolgreicher Wiederherstellung kann die Person eine neue PIN festlegen; die Datenbankschlüssel werden anschließend mit dem neuen PIN-Schlüssel erneut verpackt.
- Der vollständige Wiederherstellungscode wird nur während der Einrichtung angezeigt. Kopieren und manuelles Speichern (z. B. Screenshot/Notiz) werden angeboten; vor dem Fortfahren ist eine ausdrückliche Bestätigung erforderlich. Der Code wird nicht in Logs geschrieben oder an einen Server gesendet.
- Ein Profil kann optional einen eigenen biometrischen Schlüssel im Android Keystore registrieren. Dieser schützt die Schlüsselhülle des Profils und wird über BiometricPrompt freigegeben. PIN und Wiederherstellungscode bleiben als Rückfallwege verfügbar. Wird eine Biometrie ungültig (z. B. nach Änderung der registrierten Fingerabdrücke), muss die Person per PIN oder Wiederherstellungscode erneut freischalten und Biometrie neu einrichten.

## Bereits implementierte Grundlage

- SQLCipher for Android `4.20.0` und AndroidX SQLite `2.7.0` sind als Abhängigkeiten eingebunden; die bestehende Room-2.8.3-Datenbank wird dadurch noch nicht umgestellt.
- `SqlCipherRoomSmokeTest` prüft die Room-2-Integration mit einer isolierten Testdatenbank. GitHub Actions startet dafür einen API-35-Emulator und führt `connectedDebugAndroidTest` aus. Ein erfolgreicher CI-Lauf ist noch nicht bestätigt.

- `app/src/main/java/de/topfsecret/app/security/RecoveryCodeGenerator.kt` erzeugt 24 zufällige Crockford-Base32-Symbole mit `SecureRandom`, formatiert sie als sechs Vierergruppen und normalisiert/validiert spätere Eingaben.
- Unit-Tests liegen unter `app/src/test/java/de/topfsecret/app/security/RecoveryCodeGeneratorTest.kt`.
- GitHub Actions führt die Release-Unit-Tests vor dem APK-Build aus. Ein erfolgreicher CI-Lauf für diese Änderung ist noch nicht bestätigt.
- Der Generator zeigt oder speichert selbst keinen Code; die spätere Onboarding-Oberfläche muss den Klartext nur einmal anzeigen und kopierbar machen.

## Fehlversuche und Sperre

- PIN- und Wiederherstellungsversuche werden begrenzt; die Wartezeit steigt nach wiederholten Fehlversuchen.
- Der Zähler wird persistent gespeichert und bei erfolgreicher Anmeldung kontrolliert zurückgesetzt.
- Keine automatische Löschung von Rezepten oder Schlüsseln nach Fehlversuchen.
- Die Sicherheitsarchitektur darf nicht so tun, als wäre ein lokaler Sperrzähler gegen einen Angreifer mit Root-Zugriff manipulationssicher. Der Keystore-Pepper und die Datenbankverschlüsselung sind zusätzliche Schutzschichten.

## Bestehende Daten und Migration

Die bisherige Room-Datenbank v1 (`topf_secret.db`) ist unverschlüsselt. Sie darf nicht einfach mit einem neuen SQLCipher-Builder geöffnet werden.

Geplanter Migrationsablauf:

1. Alte Datenbank unverändert und lesbar als Quelle öffnen.
2. Drei neue, verschlüsselte Datenbanken mit neuen zufälligen Schlüsseln erstellen.
3. Alle vorhandenen Rezepte, Zutaten und Zubereitungsschritte in die gemeinsame Datenbank kopieren.
4. Datensatzanzahlen, Beziehungen, Fremdschlüssel und Datenbankintegrität prüfen.
5. Erst nach erfolgreicher Prüfung den neuen Datenbestand aktivieren.
6. Die alte unverschlüsselte Datenbank erst nach erfolgreicher Aktivierung und zusätzlicher Prüfung entfernen; bei jedem Fehler bleibt die Originaldatenbank erhalten und die App meldet den Migrationsfehler, statt Daten zu verwerfen.

Es wird keine destruktive Room-Migration und kein `fallbackToDestructiveMigration` verwendet.

## Bilder und Dateien

SQLCipher verschlüsselt nur die Datenbankdateien. Private Rezeptbilder und andere private Anhänge müssen separat verschlüsselt im internen App-Speicher abgelegt werden. Gemeinsame Bilder werden mit einem gemeinsamen Dateischlüssel geschützt; private Bilder mit dem jeweiligen Profil-Dateischlüssel. Externe Bild-URIs dürfen nicht fälschlich als durch die Datenbankverschlüsselung geschützt dargestellt werden.

## Bibliotheken und Plattform

- Bestehende Datenzugriffsschicht: Room 2.8.3.
- SQLCipher for Android `net.zetetic:sqlcipher-android:4.20.0` statt der inzwischen abgekündigten alten `android-database-sqlcipher`-Bibliothek.
- Room-2-Integration erfolgt über `net.zetetic.database.sqlcipher.SupportOpenHelperFactory`; dafür ist `androidx.sqlite:sqlite:2.7.0` eingebunden. Die offizielle SQLCipher-Dokumentation beschreibt die Room-2-Integration über diese Factory. Die Bibliothek unterstützt Android API 23+ und ist damit grundsätzlich mit dem App-Mindestziel API 26 kompatibel.
- Ein Android-Instrumentierungstest öffnet eine separate Testdatenbank mit einem zufälligen Testschlüssel, schreibt/liest einen Rezeptdatensatz, prüft, dass der SQLite-Klartextheader fehlt, und öffnet die Datenbank erneut mit demselben Schlüssel. Die CI soll diesen Test auf einem API-35-Emulator ausführen. Das ist zunächst nur ein Prototyp; die produktive Datenbank und ihr Schlüssel bleiben unverändert.
- Biometrie über AndroidX Biometric / BiometricPrompt. Verfügbarkeit wird zur Laufzeit geprüft; bei fehlender Hardware oder nicht eingerichteter Biometrie bleibt PIN-Eingabe möglich.
- Android Keystore schützt nicht exportierbare Schlüsselmaterialien; Biometrie wird vom Betriebssystem ausgewertet, nicht von Topf Secret selbst.

## Grenzen und klare Aussagen

- Der Wiederherstellungscode ist ein **Zugangswiederherstellungscode für den PIN**, kein Ersatz für ein Rezept-Backup und keine Cloud-Wiederherstellung.
- Die Schlüssel sind an diese App-Installation und deren Android-Keystore gebunden. Nach Deinstallation oder Löschen der App-Daten kann der lokale Keystore-Schlüssel fehlen; dafür ist zusätzlich das separate Rezept-Backup/Restore erforderlich.
- Schutz vor einem entsperrten, gerooteten oder vollständig kompromittierten Gerät kann eine lokale App nicht vollständig garantieren.
- Biometrie ist optional und kann je nach Gerät, Android-Version und eingerichteter Sensorik unterschiedlich verfügbar sein.

## Implementierungsreihenfolge

1. SQLCipher-/Room-2-Prototyp auf API-35-Emulator und anschließend auf einem API-26-Gerät validieren; CI-Ergebnis abwarten.
2. Schlüsselverwaltung und Profil-Metadaten implementieren und testen.
3. Datenbankmigration v1 → verschlüsselte gemeinsame Datenbank mit Integritätsprüfung implementieren.
4. Zwei private Datenbanken und Profilzugriff ergänzen.
5. Onboarding: Namen/Profil, freie PIN, PIN-Bestätigung, Backup-Code anzeigen/kopieren/Bestätigung.
6. Profilwechsel, PIN-Eingabe, Fehlversuchsbegrenzung und Wiederherstellung implementieren.
7. Biometrische Schlüsselhüllen optional ergänzen.
8. Private Bilddateien verschlüsseln und sämtliche Datenpfade testen.
9. Tests: falsche PIN, falscher Code, Profilgrenzen, Biometrie nicht verfügbar/ungültig, App-Neustart, Migration mit vorhandenen Rezepten und Migrationsabbruch.

## Referenzen

- SQLCipher for Android: https://github.com/sqlcipher/sqlcipher-android
- Android BiometricPrompt: https://developer.android.com/reference/androidx/biometric/BiometricPrompt
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- Room-Datenbankmigrationen: https://developer.android.com/training/data-storage/room/migrating-db-versions
