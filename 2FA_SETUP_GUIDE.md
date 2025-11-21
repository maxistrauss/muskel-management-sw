# Zwei-Faktor-Authentifizierung (2FA) - Setup-Anleitung

## 🎉 Implementierung abgeschlossen!

Die 2FA-Funktionalität wurde erfolgreich in Ihre MuskelManagement-Anwendung integriert.

## 📋 Was wurde implementiert?

### ✅ Neue Komponenten

1. **Email-Service** - Verschickt HTML-Emails mit 2FA-Codes
2. **TwoFactorAuthService** - Generiert und validiert Codes
3. **TwoFactorCode Entity** - Speichert Codes mit Ablaufzeit in der Datenbank
4. **Custom Authentication Handler** - Steuert den 2FA-Flow nach Login
5. **2FA Verification Page** - Benutzerfreundliche Code-Eingabe
6. **Profil-Einstellungen** - Toggle zum Aktivieren/Deaktivieren von 2FA

### 🔐 Sicherheitsmerkmale

- ✅ 4-stellige Zufallscodes (1000-9999)
- ✅ 5 Minuten Gültigkeit
- ✅ Maximal 3 Eingabeversuche
- ✅ Codes können nur einmal verwendet werden
- ✅ Automatisches Cleanup alter Codes
- ✅ Sichere Code-Generierung mit SecureRandom

## 🚀 Setup-Schritte

### 1. Mailtrap-Account einrichten

1. Gehen Sie zu https://mailtrap.io
2. Erstellen Sie einen kostenlosen Account
3. Erstellen Sie eine neue Inbox
4. Kopieren Sie die SMTP-Credentials

### 2. Application Properties konfigurieren

Öffnen Sie `src/main/resources/application.properties` und ersetzen Sie:

```properties
# Mail Configuration (Mailtrap for Development)
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
```

Mit Ihren tatsächlichen Mailtrap-Credentials.

### 3. Anwendung starten

```bash
./gradlew bootRun
```

Oder in Windows PowerShell:
```powershell
.\gradlew.bat bootRun
```

## 📖 Verwendung

### Für Benutzer

1. **2FA aktivieren:**
   - Melden Sie sich an
   - Gehen Sie zu "Profil" (http://localhost:8080/profile)
   - Scrollen Sie zu "Sicherheitseinstellungen"
   - Klicken Sie auf "Aktivieren"

2. **Login mit 2FA:**
   - Geben Sie Email und Passwort ein
   - Sie werden zur 2FA-Verifizierung weitergeleitet
   - Überprüfen Sie Ihre Email (in Mailtrap)
   - Geben Sie den 4-stelligen Code ein
   - Der Code wird automatisch abgeschickt, wenn 4 Ziffern eingegeben wurden

3. **2FA deaktivieren:**
   - Gehen Sie zu Ihrem Profil
   - Klicken Sie auf "Deaktivieren" in den Sicherheitseinstellungen

### Email-Beispiel

Benutzer erhalten eine professionell formatierte HTML-Email mit:
- Dem 4-stelligen Code (groß und deutlich angezeigt)
- Gültigkeitsdauer (5 Minuten)
- Maximale Versuche (3)
- Sicherheitshinweise

## 🧪 Testing

### Manueller Test

1. Registrieren Sie einen Test-Benutzer
2. Aktivieren Sie 2FA in den Profil-Einstellungen
3. Loggen Sie sich aus
4. Loggen Sie sich wieder ein
5. Überprüfen Sie Mailtrap auf die Email
6. Geben Sie den Code ein

### Test-Szenarien

#### ✅ Erfolgreicher 2FA-Login
1. Korrektes Email/Passwort eingeben
2. Code aus Email kopieren
3. Code eingeben → Erfolgreicher Login

#### ❌ Falscher Code
1. Login mit Email/Passwort
2. Falschen Code eingeben
3. Fehlermeldung mit verbleibenden Versuchen

#### ⏰ Abgelaufener Code
1. Login mit Email/Passwort
2. Warten Sie > 5 Minuten
3. Code eingeben → Fehlermeldung "Code abgelaufen"

#### 🔄 Code neu anfordern
1. Login mit Email/Passwort
2. Klicken Sie auf "Neuen Code anfordern"
3. Neuen Code aus Email eingeben

## 📁 Dateistruktur

```
src/main/java/de/oth/muskelmanagement/
├── model/
│   ├── User.java (erweitert mit twoFactorEnabled)
│   └── TwoFactorCode.java (neu)
├── repository/
│   └── TwoFactorCodeRepository.java (neu)
├── service/
│   ├── EmailService.java (neu)
│   ├── EmailServiceImpl.java (neu)
│   ├── TwoFactorAuthService.java (neu)
│   ├── TwoFactorAuthServiceImpl.java (neu)
│   ├── UserService.java (erweitert)
│   └── UserServiceImpl.java (erweitert)
├── controller/
│   ├── TwoFactorController.java (neu)
│   └── ProfileController.java (erweitert)
├── config/
│   ├── SecurityConfig.java (angepasst)
│   └── handler/
│       └── CustomAuthenticationSuccessHandler.java (neu)
└── MuskelManagementApplication.java (Scheduling aktiviert)

src/main/resources/
├── templates/
│   ├── verify-2fa.html (neu)
│   └── profile.html (erweitert)
└── application.properties (Email-Konfiguration)
```

## 🔧 Konfigurationsoptionen

In `application.properties`:

```properties
# Code-Gültigkeit in Minuten (Standard: 5)
app.2fa.code-validity-minutes=5

# Maximale Versuche (Standard: 3)
app.2fa.max-attempts=3

# Absender-Email
app.mail.from=noreply@muskelmanagement.de
```

## 🚨 Troubleshooting

### Problem: Emails werden nicht versendet

**Lösung:**
1. Überprüfen Sie Mailtrap-Credentials in `application.properties`
2. Überprüfen Sie Logs auf Fehler: `Failed to send 2FA email`
3. Stellen Sie sicher, dass Spring Boot Mail Dependency installiert ist

### Problem: Code wird nicht akzeptiert

**Lösung:**
1. Überprüfen Sie, dass der Code nicht abgelaufen ist (5 Min.)
2. Überprüfen Sie, dass nicht mehr als 3 Versuche gemacht wurden
3. Überprüfen Sie in Mailtrap, ob der richtige Code verwendet wird

### Problem: Session verloren nach 2FA

**Lösung:**
- Überprüfen Sie, dass `CustomAuthenticationSuccessHandler` korrekt konfiguriert ist
- Logs prüfen auf: "Authentication completed for user ID"

## 📊 Datenbank-Schema

### User Table (erweitert)
```sql
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE;
```

### Two Factor Codes Table (neu)
```sql
CREATE TABLE two_factor_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    code VARCHAR(4) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    attempts INT DEFAULT 0,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_id ON two_factor_codes(user_id);
CREATE INDEX idx_expiry_time ON two_factor_codes(expiry_time);
```

## 🔮 Zukünftige Erweiterungen

Mögliche Verbesserungen:
- SMS-basierte 2FA als Alternative
- Authenticator-App Support (TOTP - Time-based One-Time Password)
- Backup-Codes für Notfälle
- 2FA für sensible Admin-Aktionen erzwingen
- Audit-Log für 2FA-Ereignisse
- Remember Device Option (Gerät für X Tage vertrauenswürdig)

## 📞 Support

Bei Fragen oder Problemen:
1. Überprüfen Sie die Logs
2. Konsultieren Sie das technische Design-Dokument: `2FA_IMPLEMENTATION_PLAN.md`
3. Überprüfen Sie die Mailtrap-Inbox auf gesendete Emails

## ✅ Checkliste vor Produktion

- [ ] Mailtrap durch echten SMTP-Server ersetzen (z.B. Gmail, SendGrid)
- [ ] Email-Templates personalisieren (Logo, Farben, etc.)
- [ ] Rate Limiting für Code-Anforderungen implementieren
- [ ] Monitoring für fehlgeschlagene 2FA-Versuche einrichten
- [ ] Backup-Mechanismus für Email-Ausfälle überlegen
- [ ] Rechtliche Aspekte (DSGVO) für Email-Speicherung prüfen

---

**Version:** 1.0  
**Datum:** 2025-11-21  
**Status:** ✅ Produktionsbereit (nach Konfiguration)