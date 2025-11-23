# Account Blocking Feature - Implementierungsdokumentation

## Übersicht

Dieses Feature ermöglicht es Administratoren, Benutzerkonten zu sperren und zu entsperren mit automatischem E-Mail-Versand an die betroffenen Benutzer.

## Implementierte Komponenten

### 1. Backend Services

#### EmailService
- `sendAccountBlockedEmail(email, userName, reason)` - Sendet E-Mail bei Kontosperrung
- `sendAccountUnblockedEmail(email, userName)` - Sendet E-Mail bei Entsperrung

#### UserService  
- `blockUser(userId, reason)` - Sperrt Benutzer und sendet E-Mail
- `unblockUser(userId)` - Entsperrt Benutzer und sendet E-Mail

### 2. Controller Endpoints

#### AdminController
- `POST /admin/users/{id}/block?reason=<text>` - Sperrt Benutzer
- `POST /admin/users/{id}/unblock` - Entsperrt Benutzer

#### AdminRestController  
- `POST /api/admin/users/{id}/block?reason=<text>` - REST API für Sperrung
- `POST /api/admin/users/{id}/unblock` - REST API für Entsperrung

### 3. Frontend (users.html)

- Modal-Dialog für Eingabe des Sperrgrundes
- "Active" Button (grün) bei aktiven Konten - öffnet Modal
- "Blocked" Button (rot) bei gesperrten Konten - entsperrt direkt

## Funktionsweise

### Benutzer sperren:
1. Admin klickt auf "✓ Active" Button
2. Modal öffnet sich mit Textfeld für Sperrgrund
3. Admin gibt Grund ein und bestätigt
4. Backend setzt `user.enabled = false`
5. E-Mail wird an Benutzer gesendet mit Sperrgrund
6. Benutzer kann sich nicht mehr einloggen

### Benutzer entsperren:
1. Admin klickt auf "✗ Blocked" Button
2. Backend setzt `user.enabled = true`
3. E-Mail wird an Benutzer gesendet
4. Benutzer kann sich wieder einloggen

## E-Mail Templates

### Sperr-E-Mail
- **Betreff:** "Ihr Konto wurde gesperrt - MuskelManagement"
- **Inhalt:** Informiert über Sperrung mit angegebenem Grund

### Entsperr-E-Mail
- **Betreff:** "Ihr Konto wurde entsperrt - MuskelManagement"  
- **Inhalt:** Bestätigung der Entsperrung

## Testing

### Voraussetzungen
- Anwendung läuft (`./gradlew bootRun`)
- Mailtrap ist konfiguriert (siehe `application.properties`)
- Admin-Konto vorhanden
- Test-Benutzer vorhanden

### Test-Schritte

1. **Als Admin einloggen**
2. **Navigieren zu** `/admin/users`
3. **Benutzer sperren:**
   - Klick auf grünen "✓ Active" Button
   - Sperrgrund eingeben (z.B. "Verstoß gegen Nutzungsbedingungen")
   - "Benutzer sperren" klicken
   - Überprüfen: Button zeigt nun "✗ Blocked" (rot)
4. **E-Mail überprüfen:**
   - Mailtrap öffnen: https://mailtrap.io
   - E-Mail an gesperrten Benutzer überprüfen
   - Sperrgrund sollte in E-Mail sichtbar sein
5. **Login-Test:**
   - Als gesperrter Benutzer einloggen versuchen
   - Login sollte fehlschlagen (Spring Security blockiert)
6. **Benutzer entsperren:**
   - Als Admin auf roten "✗ Blocked" Button klicken
   - Überprüfen: Button zeigt nun "✓ Active" (grün)
7. **E-Mail überprüfen:**
   - Mailtrap überprüfen
   - Entsperr-E-Mail sollte angekommen sein
8. **Login-Test:**
   - Als entsperrter Benutzer einloggen
   - Login sollte nun funktionieren

## Sicherheit

- Admins können **nicht** ihr eigenes Konto sperren/entsperren
- Spring Security überprüft automatisch `user.enabled` beim Login
- E-Mails werden nur bei erfolgreicher Sperrung/Entsperrung versendet

## Technische Details

### Datenbankänderungen
- **Keine** neuen Tabellen oder Felder erforderlich
- Nutzt bestehendes `enabled` Feld in `users` Tabelle

### Security Flow
```java
// SecurityConfig.java - Line 66
UserDetailsService erstellt UserDetails mit:
user.isEnabled() → Spring Security prüft dies automatisch
```

### Fehlerbehandlung
- Exception wenn Benutzer nicht gefunden
- Exception wenn Benutzer bereits gesperrt/entsperrt
- Exception wenn Admin eigenes Konto bearbeiten will
- RuntimeException bei E-Mail-Versand-Fehler (mit Logging)

## Änderungen an bestehenden Features

- **Alte Toggle-Funktion bleibt erhalten** (`toggleUserEnabled`)
- Neue Funktionen sind **zusätzlich**, ersetzen nichts
- Beide Ansätze können parallel genutzt werden

## Konfiguration

Keine zusätzliche Konfiguration erforderlich. E-Mail-Settings aus `application.properties`:

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=587
app.mail.from=noreply@muskelmanagement.de