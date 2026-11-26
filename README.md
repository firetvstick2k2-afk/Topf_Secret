# 🍲 Topf Secret

**Deine Rezepte. Eure Küche. Euer Geheimnis.**

Topf Secret ist ein Android-Kochbuch für private Rezeptsammlungen – mit optionaler Synchronisation zwischen zwei Geräten, Rezeptimport aus Webseiten, Bildern, Nährwertberechnung und vollständigen Backups.

> Das Repository enthält ausschließlich Quellcode und öffentliche Projektdokumentation. Private Rezepte, Bilder, Backups, Zugangsdaten und personenbezogene Daten gehören nicht hierher.

## Geplante Funktionen

- PIN-geschützter App-Zugang
- Rezepte erstellen, bearbeiten, suchen und kategorisieren
- Gerichtsfotos speichern
- Rezepte zwischen zwei persönlichen Geräten synchronisieren
- Rezeptdaten aus Webseiten übernehmen – immer erst nach Vorschau und Bestätigung
- Nährwerte pro Portion und für frei wählbare Portionsgrößen berechnen
- Vollständiger Export und Import inklusive Rezeptbildern
- Offline-Nutzung mit lokaler Datenhaltung

## Android-Kompatibilität

Das aktuelle Entwicklungsziel ist **Android 8.0 (API 26) und neuer**. Die tatsächliche Gerätekompatibilität wird während der Entwicklung anhand der verwendeten AndroidX-Bibliotheken und Tests überprüft.

## Projektstatus

Frühe Grundstruktur – noch kein funktionsfähiger Rezeptmanager. Details und nächste Schritte stehen in [docs/HANDOFF.md](docs/HANDOFF.md) und [docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md).

## Entwicklung

- IDE: Android Studio
- Sprache: Kotlin
- UI: Jetpack Compose
- Mindest-SDK-Ziel: API 26
- Aktiver Entwicklungszweig: `development`

Build-Anleitung und reproduzierbarer Gradle-Wrapper werden ergänzt, sobald die lokale Android-Build-Umgebung festgelegt ist.
