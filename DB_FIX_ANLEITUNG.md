# Datenbank-Problem beheben

## Problem
Die alte Datenbank enthält Benutzer ohne das neue `twoFactorEnabled` Feld, was zu Fehlern führt.

## Lösung: Datenbank zurücksetzen

### Schritt 1: Alle laufenden Prozesse stoppen
1. Drücken Sie `Ctrl+C` in allen Terminals wo die App läuft
2. Schließen Sie alle Gradle-Prozesse

### Schritt 2: Datenbankdatei manuell löschen

**Option A: Windows Explorer**
1. Öffnen Sie den Windows Explorer
2. Navigieren Sie zu: `C:\Studium\7. Semester\Softwareentwicklung\MuscleManagement\muskel-management`
3. Löschen Sie diese Dateien:
   - `muskelmanagement.mv.db`
   - `muskelmanagement.trace.db` (falls vorhanden)

**Option B: PowerShell**
```powershell
# Stoppen Sie ALLE Gradle-Prozesse erst:
taskkill /F /IM java.exe

# Warten Sie 5 Sekunden, dann:
Remove-Item -Force muskelmanagement.mv.db
Remove-Item -Force muskelmanagement.trace.db -ErrorAction SilentlyContinue
```

### Schritt 3: Anwendung neu starten
```bash
./gradlew bootRun
```

## Nach dem Neustart

Die Datenbank wird mit dem korrekten Schema neu erstellt. Sie müssen:
1. Neue Benutzer registrieren
2. Im Profil 2FA aktivieren
3. Ausloggen und neu einloggen
4. Code aus Mailtrap-Email eingeben

## Wichtig: Mailtrap konfigurieren

Vergessen Sie nicht, in `application.properties` die Mailtrap-Credentials einzutragen:

```properties
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
```

Ohne diese Konfiguration können keine Emails versendet werden!