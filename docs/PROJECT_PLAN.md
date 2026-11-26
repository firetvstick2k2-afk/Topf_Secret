# Topf Secret – Produktplan

## Produktziel

Ein einfaches, zuverlässiges Android-Kochbuch für zwei Personen. Rezepte sollen lokal verfügbar bleiben, kontrolliert importiert, sicher synchronisiert und vollständig gesichert werden können.

## Funktionale Anforderungen

### A. Schutz und Privatsphäre
- Technische Grundlage begonnen: Recovery-Code-Generator mit 120 Bit Zufall und strenger Crockford-Base32-Validierung ist vorhanden; Onboarding, Schlüsselhüllen und Wiederherstellung folgen.
- Zwei lokale persönliche Profile (Nutzer und Ehefrau) mit jeweils eigener PIN und privatem Rezeptbereich.
- Gemeinsame Rezepte sind für beide Profile sichtbar; bestehende Rezepte werden bei Einführung als gemeinsam übernommen.
- PIN-Ersteinrichtung mit freier PIN-Wahl; PIN niemals im Klartext speichern und Fehlversuche begrenzen.
- Optionale biometrische Entsperrung über Android BiometricPrompt (z. B. Fingerabdruck); PIN bleibt immer verfügbar.
- Jeder Nutzer erhält einen eigenen, automatisch auf dem Gerät generierten Wiederherstellungscode.
- Wiederherstellungscode: 24 Zeichen aus einem verwechselungsarmen Base32-Zeichensatz, in sechs Vierergruppen formatiert (120 Bit Zufall bei 32 Symbolen).
- Bei der Einrichtung einmal vollständig anzeigen; Kopierfunktion anbieten und Speichern als Screenshot/Notiz ermöglichen. Fortfahren erst nach Bestätigung, dass der Code gesichert wurde.
- Vollständigen Code nicht im Klartext speichern, loggen oder übertragen. Nur kryptografisch sichere Prüfdaten/Ableitungen speichern.
- Der Wiederherstellungscode gilt nur für das jeweilige Profil und ermöglicht nach erfolgreicher Prüfung das Setzen einer neuen PIN.
- Admin-Aktivierung bleibt getrennt von persönlichen Profilen und PINs.
- Rezeptdaten und Bilder standardmäßig privat.
- Lokale Datenaufteilung: eine verschlüsselte gemeinsame SQLCipher-Datenbank plus je eine separat verschlüsselte private Datenbank pro Profil. Beide Profile erhalten jeweils eine eigene Schlüsselhülle für die gemeinsame Datenbank; private Datenbankschlüssel werden nicht zwischen Profilen geteilt.
- Private Rezeptbilder und Anhänge werden zusätzlich dateibasiert verschlüsselt; Datenbankverschlüsselung allein schützt keine extern referenzierten Dateien.
- Öffentlicher GitHub-Code darf keine Zugangsdaten, echten Rezeptdaten oder privaten Backups enthalten.

### B. Rezepte
- Titel, Beschreibung, Zutaten, Mengen und Einheiten.
- Zubereitungsschritte, Vorbereitungs-/Kochzeit, Portionenzahl.
- Kategorien, Tags, Favoriten und persönliche Notizen.
- Eigene Bilder und Quellenangabe.

### C. Synchronisation
- Zwei getrennte App-Installationen mit autorisierter Verbindung.
- Synchronisation in beide Richtungen.
- Konflikterkennung bei gleichzeitigen Änderungen.
- Offline-Änderungen lokal puffern und später übertragen.
- Zugriff ausschließlich für eingeladene/verknüpfte Konten.

### D. Web-Rezeptimport
- Teilen-Menü (Android Sharesheet) und manuelle URL-Eingabe.
- Strukturierte Rezeptdaten bevorzugt aus Schema.org Recipe lesen.
- Importvorschau mit editierbaren Feldern.
- Erst nach ausdrücklicher Bestätigung speichern.
- Quelle und Bildrechte/Quell-URL dokumentieren; keine Umgehung von Bezahlschranken oder Zugriffsschutz.

### E. Bilder
- Bilder aus Kamera/Galerie.
- Lokale Speicherung und optional synchronisierte Speicherung.
- Bilder im vollständigen Backup einschließen.

### F. Nährwerte
- Energie, Kohlenhydrate, Zucker, Ballaststoffe, Eiweiß und Fett.
- Berechnung aus Zutatenmengen und nachvollziehbaren Nährwertdaten.
- Anzeige pro Gesamt-Rezept, Portion und frei gewählter Portionsanzahl.
- Datenquelle und Schätzstatus sichtbar machen.
- Nutzer kann Zutaten und Nährwerte korrigieren.
- Werte sind Schätzungen und keine medizinische Therapie- oder Insulinempfehlung.

### G. Backup und Wiederherstellung
- Versioniertes ZIP-basiertes `.tsb`-Format.
- Vollständiger Export aller Rezepte, Metadaten und Bilder.
- Optional einzelne Rezepte exportieren/teilen.
- Import mit Vorschau, Validierung und Duplikaterkennung.
- Verschlüsseltes Backup als Option; Wiederherstellung ohne Cloud möglich.
- Formatversion und Prüfsummen vorsehen, damit spätere App-Versionen alte Backups migrieren können.

## Technische Leitlinien

- Kotlin und Jetpack Compose.
- Room 2.8.3 als lokale Datenzugriffsschicht.
- SQLCipher for Android 4.20.0 ist eingebunden; Room 2.8.3 nutzt die SupportOpenHelperFactory und AndroidX SQLite 2.7.0. Ein isolierter Room-/SQLCipher-Instrumentierungstest ist für CI auf einem API-35-Emulator eingerichtet. Der Workflow ist wegen eines ADB-/Emulator-Ausfalls vorläufig auf `ubuntu-22.04` festgesetzt; ein erfolgreicher CI-Lauf und API-26-Gerätetest stehen noch aus.
- Schlüsselmaterial mit Android Keystore schützen; optionale biometrische Freigabe über AndroidX BiometricPrompt.
- Repository-/Use-Case-Schicht, damit Datenbank und Synchronisationsanbieter austauschbar bleiben.
- Synchronisation erst nach Sicherheits- und Datenschutzkonzept integrieren.
- Android 8.0 / API 26 als vorläufiges Mindestziel; reale Kompatibilität testen.
- Keine geheimen Schlüssel oder privaten Nutzerdaten in Git.

## Roadmap

1. Projektgrundlage und CI-Build
2. UI-Grundgerüst und Navigation
3. Lokales Datenmodell und Room
4. Sicherheitsarchitektur und SQLCipher-/Room-2-Prototyp (eingebunden; Emulator-/API-26-Validierung offen)
5. Datenbankmigration und lokale Profile mit PIN, Wiederherstellungscode und optionaler Biometrie
6. Rezeptverwaltung und verschlüsselte Bilder
7. Portionen- und Nährwertberechnung
8. Backup/Restore
9. Web-Rezeptimport
10. Sichere Synchronisation für zwei Personen
11. Geräte- und Regressionstests, Release

## Noch zu entscheiden

- Konkrete KDF-Parameter und konkrete SQLCipher-Version müssen vor Implementierung per Prototyp und API-26-/CI-Build bestätigt werden
- PIN-Länge und Sperrverhalten
- Supabase oder eigener Synchronisationsdienst
- Backup-Verschlüsselung standardmäßig optional oder verpflichtend
- Nährwertdatenquelle und Lizenz
- App-Sprache zunächst Deutsch oder mehrsprachig
