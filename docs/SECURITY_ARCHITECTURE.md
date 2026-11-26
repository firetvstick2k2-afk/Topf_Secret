# Topf Secret – Datenschutz und Geräteschutz

Stand: 2026-11-26

## Produktentscheidung

Topf Secret wird als gemeinsames, PIN-freies Kochbuch für zwei Personen entwickelt. Rezepte gelten grundsätzlich als gemeinsame Inhalte. Es gibt keine privaten Rezeptbereiche, keine persönliche PIN, keine Biometrie-Entsperrung und keine Wiederherstellungscodes innerhalb der App.

## Geräteschutz

- Der Zugriffsschutz erfolgt über die normale Android-Bildschirmsperre des Geräts.
- Topf Secret speichert keine eigenen PINs oder biometrischen Merkmale.
- Die bestehende Admin-Aktivierung bleibt eine separate Funktion und ist kein persönlicher Rezeptzugang.
- Die App verspricht keinen Schutz vor Zugriff auf ein entsperrtes oder vollständig kompromittiertes Gerät.

## Lokale Daten

- Die bestehende Room-Datenbank `topf_secret.db` bleibt die gemeinsame lokale Rezeptdatenbank.
- Es wird keine Datenbankmigration oder Umstellung auf SQLCipher vorgenommen.
- Rezeptbilder werden als lokale Rezeptressourcen behandelt. Die App soll nur die für die Bildauswahl nötigen Android-Berechtigungen verwenden.
- Vollständige Backups müssen Rezepte, Zutaten, Zubereitungsschritte, Metadaten und zugehörige Bilder enthalten.

## Repository-Sicherheit

- Keine echten Rezepte, privaten Fotos, Backups, Zugangsdaten, Keystores oder Cloud-Schlüssel committen.
- Die öffentliche GitHub-Version enthält ausschließlich Quellcode und nicht-private Projektdokumentation.

## Ausgeschlossene Prototypen

Der zuvor begonnene PIN-/Profil-/Recovery-Code-/SQLCipher-Prototyp wurde verworfen. Die zugehörigen Bibliotheksabhängigkeiten, Instrumentierungstests und Recovery-Code-Dateien wurden aus dem Entwicklungszweig entfernt. Die produktive Room-Datenbank war zu keinem Zeitpunkt auf SQLCipher umgestellt.
