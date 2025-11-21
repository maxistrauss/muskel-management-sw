# 2-Faktor-Authentifizierung (2FA) - Technisches Design

## Übersicht

Implementierung einer einfachen Email-basierten 2FA für die MuskelManagement-Anwendung.

**Konfiguration:**
- Email-Provider: Mailtrap.io (Development/Testing)
- Code-Gültigkeit: 5 Minuten
- Maximale Fehlversuche: 3
- Code-Format: 4-stellige Zufallszahl (0000-9999)

## Architektur-Diagramm

```
┌─────────────────────────────────────────────────────────────────┐
│                         Login-Flow                               │
└─────────────────────────────────────────────────────────────────┘

User Input (Email/Password)
        ↓
SecurityConfig (Authentication)
        ↓
CustomAuthenticationSuccessHandler
        ↓
    ┌─────────────────┐
    │ 2FA Enabled?    │
    └─────────────────┘
         ↓           ↓
        NO          YES
         ↓           ↓
    Default      TwoFactorAuthService
    Success      ↓
    URL          Generate Code (4-digit)
                 ↓
                 Save to DB (TwoFactorCode)
                 ↓
                 EmailService.sendCode()
                 ↓
                 Redirect to /verify-2fa
                 ↓
    TwoFactorController (GET /verify-2fa)
                 ↓
    verify-2fa.html Template
                 ↓
    User enters code
                 ↓
    TwoFactorController (POST /verify-2fa)
                 ↓
    TwoFactorAuthService.validateCode()
                 ↓
         ┌──────────────────┐
         │ Code Valid?      │
         └──────────────────┘
         ↓             ↓
        YES            NO
         ↓             ↓
    Complete       Increment
    Login          Attempts
         ↓             ↓
    Redirect    ┌─────────────┐
    to /        │ Attempts>3? │
                └─────────────┘
                 ↓         ↓
                YES        NO
                 ↓         ↓
            Invalidate  Show Error
            Code        Message
                 ↓
            Request
            New Code
```

## Datenbank-Schema

### User Entity (Erweiterung)

```java
@Entity
@Table(name = "users")
public class User {
    // ... existing fields ...
    
    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;  // Default: deaktiviert
}
```

### TwoFactorCode Entity (NEU)

```java
@Entity
@Table(name = "two_factor_codes")
public class TwoFactorCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "code", nullable = false, length = 4)
    private String code;  // 4-stelliger Code
    
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;  // Jetzt + 5 Minuten
    
    @Column(name = "attempts")
    private int attempts = 0;  // Anzahl der Versuche
    
    @Column(name = "used")
    private boolean used = false;  // Wurde der Code schon verwendet?
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Indexe für Performance

```sql
CREATE INDEX idx_two_factor_codes_user_id ON two_factor_codes(user_id);
CREATE INDEX idx_two_factor_codes_expiry_time ON two_factor_codes(expiry_time);
```

## Services

### 1. EmailService

**Interface:**
```java
public interface EmailService {
    void sendTwoFactorCode(String toEmail, String code, String userName);
}
```

**Implementation:**
```java
@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public void sendTwoFactorCode(String toEmail, String code, String userName) {
        // HTML-Email mit Code
        // Template: Professionell formatiert
        // Enthält: Code, Ablaufzeit, Sicherheitshinweis
    }
}
```

### 2. TwoFactorAuthService

**Interface:**
```java
public interface TwoFactorAuthService {
    String generateAndSendCode(User user);
    boolean validateCode(Long userId, String code);
    void invalidateAllCodesForUser(Long userId);
    void cleanupExpiredCodes();
}
```

**Implementation:**
```java
@Service
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {
    private static final int CODE_VALIDITY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    @Autowired
    private TwoFactorCodeRepository codeRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    public String generateAndSendCode(User user) {
        // 1. Alte Codes für User invalidieren
        invalidateAllCodesForUser(user.getId());
        
        // 2. Neuen 4-stelligen Code generieren
        String code = generateRandomCode();
        
        // 3. In DB speichern
        TwoFactorCode twoFactorCode = new TwoFactorCode();
        twoFactorCode.setUserId(user.getId());
        twoFactorCode.setCode(code);
        twoFactorCode.setExpiryTime(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
        twoFactorCode.setCreatedAt(LocalDateTime.now());
        codeRepository.save(twoFactorCode);
        
        // 4. Email senden
        emailService.sendTwoFactorCode(user.getEmail(), code, user.getFirstName());
        
        return code; // Nur für Logging/Testing
    }
    
    @Override
    public boolean validateCode(Long userId, String code) {
        // 1. Aktuellen Code für User holen
        List<TwoFactorCode> codes = codeRepository
            .findByUserIdAndUsedFalseOrderByCreatedAtDesc(userId);
        
        if (codes.isEmpty()) {
            return false; // Kein Code vorhanden
        }
        
        TwoFactorCode latestCode = codes.get(0);
        
        // 2. Prüfen ob abgelaufen
        if (LocalDateTime.now().isAfter(latestCode.getExpiryTime())) {
            latestCode.setUsed(true);
            codeRepository.save(latestCode);
            return false;
        }
        
        // 3. Prüfen ob max. Versuche erreicht
        if (latestCode.getAttempts() >= MAX_ATTEMPTS) {
            latestCode.setUsed(true);
            codeRepository.save(latestCode);
            return false;
        }
        
        // 4. Versuch inkrementieren
        latestCode.setAttempts(latestCode.getAttempts() + 1);
        
        // 5. Code prüfen
        if (latestCode.getCode().equals(code)) {
            latestCode.setUsed(true);
            codeRepository.save(latestCode);
            return true;
        }
        
        // Code falsch - speichern für Attempt-Tracking
        codeRepository.save(latestCode);
        return false;
    }
    
    private String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000; // 1000-9999
        return String.valueOf(code);
    }
    
    @Override
    public void invalidateAllCodesForUser(Long userId) {
        List<TwoFactorCode> codes = codeRepository
            .findByUserIdAndUsedFalse(userId);
        codes.forEach(code -> code.setUsed(true));
        codeRepository.saveAll(codes);
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Jede Stunde
    public void cleanupExpiredCodes() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        codeRepository.deleteByCreatedAtBefore(cutoff);
    }
}
```

### 3. UserService (Erweiterung)

**Neue Methoden:**
```java
public interface UserService {
    // ... existing methods ...
    
    void enableTwoFactor(Long userId);
    void disableTwoFactor(Long userId);
    boolean isTwoFactorEnabled(Long userId);
}
```

## Controllers

### 1. CustomAuthenticationSuccessHandler

```java
@Component
public class CustomAuthenticationSuccessHandler 
    implements AuthenticationSuccessHandler {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        
        if (user != null && user.isTwoFactorEnabled()) {
            // 2FA aktiviert - Code generieren und senden
            twoFactorAuthService.generateAndSendCode(user);
            
            // User-ID in Session speichern
            HttpSession session = request.getSession();
            session.setAttribute("2FA_USER_ID", user.getId());
            session.setAttribute("2FA_USER_EMAIL", user.getEmail());
            
            // Zur 2FA-Verifizierung weiterleiten
            response.sendRedirect("/verify-2fa");
        } else {
            // Normaler Login-Flow
            response.sendRedirect("/");
        }
    }
}
```

### 2. TwoFactorController

```java
@Controller
public class TwoFactorController {
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/verify-2fa")
    public String showVerificationPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("2FA_USER_ID");
        String email = (String) session.getAttribute("2FA_USER_EMAIL");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("email", maskEmail(email));
        return "verify-2fa";
    }
    
    @PostMapping("/verify-2fa")
    public String verifyCode(
            @RequestParam("code") String code,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("2FA_USER_ID");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        boolean isValid = twoFactorAuthService.validateCode(userId, code);
        
        if (isValid) {
            // Code korrekt - Login abschließen
            User user = userService.findById(userId);
            
            // Spring Security Authentication manuell setzen
            // (wird im Code-Modus implementiert)
            
            // Session aufräumen
            session.removeAttribute("2FA_USER_ID");
            session.removeAttribute("2FA_USER_EMAIL");
            
            return "redirect:/";
        } else {
            // Code falsch
            redirectAttributes.addFlashAttribute("error", 
                "Ungültiger oder abgelaufener Code. Bitte versuchen Sie es erneut.");
            return "redirect:/verify-2fa";
        }
    }
    
    @PostMapping("/verify-2fa/resend")
    public String resendCode(
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("2FA_USER_ID");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userService.findById(userId);
        twoFactorAuthService.generateAndSendCode(user);
        
        redirectAttributes.addFlashAttribute("success", 
            "Ein neuer Code wurde an Ihre Email-Adresse gesendet.");
        return "redirect:/verify-2fa";
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username + "@" + domain;
        }
        
        return username.charAt(0) + 
               "*".repeat(username.length() - 2) + 
               username.charAt(username.length() - 1) + 
               "@" + domain;
    }
}
```

## Templates

### verify-2fa.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>2FA Verifizierung</title>
    <link rel="stylesheet" th:href="@{/css/output.css}"/>
</head>
<body class="bg-gray-100">
<div class="flex items-center justify-center min-h-screen">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h1 class="text-2xl font-bold mb-6 text-center">
            Zwei-Faktor-Authentifizierung
        </h1>
        
        <!-- Success Message -->
        <div th:if="${success}" 
             class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
            <span th:text="${success}"></span>
        </div>
        
        <!-- Error Message -->
        <div th:if="${error}" 
             class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            <span th:text="${error}"></span>
        </div>
        
        <p class="text-gray-600 mb-6 text-center">
            Wir haben einen 4-stelligen Verifizierungscode an 
            <strong th:text="${email}"></strong> gesendet.
            <br/>
            <span class="text-sm">Der Code ist 5 Minuten gültig.</span>
        </p>
        
        <form method="post" th:action="@{/verify-2fa}">
            <div class="mb-6">
                <label class="block text-gray-700 text-sm font-bold mb-2">
                    Verifizierungscode:
                </label>
                <input 
                    type="text" 
                    name="code" 
                    maxlength="4" 
                    pattern="[0-9]{4}"
                    required
                    autofocus
                    class="shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 text-center text-2xl tracking-widest font-bold focus:outline-none focus:shadow-outline"
                    placeholder="0000"/>
            </div>
            
            <button type="submit" 
                    class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded w-full mb-4">
                Verifizieren
            </button>
        </form>
        
        <form method="post" th:action="@{/verify-2fa/resend}">
            <button type="submit" 
                    class="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded w-full">
                Neuen Code anfordern
            </button>
        </form>
        
        <div class="text-center mt-4">
            <a th:href="@{/login}" class="text-blue-500 hover:text-blue-700">
                Zurück zum Login
            </a>
        </div>
    </div>
</div>
</body>
</html>
```

### profile.html (Erweiterung)

Füge eine Sektion für 2FA-Einstellungen hinzu:

```html
<!-- 2FA Settings Section -->
<div class="bg-white p-6 rounded-lg shadow-md mb-6">
    <h2 class="text-xl font-bold mb-4">Zwei-Faktor-Authentifizierung</h2>
    
    <div class="flex items-center justify-between">
        <div>
            <p class="font-semibold">2FA Status:</p>
            <p class="text-gray-600">
                <span th:if="${user.twoFactorEnabled}" class="text-green-600">
                    ✓ Aktiviert
                </span>
                <span th:unless="${user.twoFactorEnabled}" class="text-gray-500">
                    Deaktiviert
                </span>
            </p>
        </div>
        
        <form method="post" th:action="@{/profile/toggle-2fa}">
            <button type="submit" 
                    th:classappend="${user.twoFactorEnabled} ? 'bg-red-500 hover:bg-red-700' : 'bg-green-500 hover:bg-green-700'"
                    class="text-white font-bold py-2 px-4 rounded">
                <span th:if="${user.twoFactorEnabled}">Deaktivieren</span>
                <span th:unless="${user.twoFactorEnabled}">Aktivieren</span>
            </button>
        </form>
    </div>
    
    <p class="text-sm text-gray-600 mt-4">
        Die Zwei-Faktor-Authentifizierung erhöht die Sicherheit Ihres Accounts.
        Bei aktivierter 2FA erhalten Sie beim Login einen Code per Email.
    </p>
</div>
```

## Konfiguration

### application.properties

```properties
# Existing config...

# Mail Configuration (Mailtrap)
spring.mail.host=smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application Settings
app.2fa.code-validity-minutes=5
app.2fa.max-attempts=3
```

### build.gradle

```gradle
dependencies {
    // ... existing dependencies ...
    
    // Email Support
    implementation 'org.springframework.boot:spring-boot-starter-mail'
}
```

## Security Configuration

### SecurityConfig.java (Anpassungen)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(
                    "/login", 
                    "/register", 
                    "/verify-2fa",           // NEU
                    "/verify-2fa/resend",    // NEU
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated())
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .successHandler(successHandler)  // Custom Handler statt defaultSuccessUrl
                .failureHandler(authenticationFailureHandler)
                .usernameParameter("email")
                .permitAll())
            .logout(LogoutConfigurer::permitAll)
            .exceptionHandling(exceptionHandling -> 
                exceptionHandling.accessDeniedHandler(accessDeniedHandler))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }
}
```

## Email Template

### 2FA Code Email (HTML)

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .header {
            background-color: #3b82f6;
            color: white;
            padding: 20px;
            text-align: center;
            border-radius: 5px 5px 0 0;
        }
        .content {
            background-color: #f9fafb;
            padding: 30px;
            border-radius: 0 0 5px 5px;
        }
        .code-box {
            background-color: white;
            border: 2px dashed #3b82f6;
            border-radius: 5px;
            padding: 20px;
            text-align: center;
            margin: 20px 0;
        }
        .code {
            font-size: 36px;
            font-weight: bold;
            letter-spacing: 10px;
            color: #3b82f6;
        }
        .warning {
            background-color: #fef3c7;
            border-left: 4px solid #f59e0b;
            padding: 10px;
            margin: 20px 0;
        }
        .footer {
            text-align: center;
            margin-top: 20px;
            color: #6b7280;
            font-size: 12px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>MuskelManagement</h1>
            <p>Zwei-Faktor-Authentifizierung</p>
        </div>
        <div class="content">
            <p>Hallo {{userName}},</p>
            
            <p>Sie haben eine Anmeldung bei MuskelManagement angefordert. 
               Hier ist Ihr Verifizierungscode:</p>
            
            <div class="code-box">
                <div class="code">{{code}}</div>
            </div>
            
            <p><strong>Wichtige Hinweise:</strong></p>
            <ul>
                <li>Dieser Code ist nur <strong>5 Minuten</strong> gültig</li>
                <li>Sie haben maximal <strong>3 Versuche</strong> zur Eingabe</li>
                <li>Teilen Sie diesen Code niemals mit anderen</li>
            </ul>
            
            <div class="warning">
                <strong>⚠️ Achtung:</strong> Falls Sie diese Anmeldung nicht 
                angefordert haben, ignorieren Sie diese Email und ändern Sie 
                umgehend Ihr Passwort.
            </div>
            
            <p>Mit freundlichen Grüßen,<br/>
               Ihr MuskelManagement Team</p>
        </div>
        <div class="footer">
            <p>Diese Email wurde automatisch generiert. Bitte antworten Sie nicht darauf.</p>
        </div>
    </div>
</body>
</html>
```

## Testing-Strategie

### Unit Tests

1. **TwoFactorAuthServiceTest**
   - Code-Generierung (4 Stellen)
   - Code-Validierung (korrekt/falsch)
   - Ablauf-Handling
   - Max-Attempts Handling
   - Code-Invalidierung

2. **EmailServiceTest**
   - Email-Versand (Mock)
   - Template-Rendering

### Integration Tests

1. **2FA Login Flow**
   - Login mit aktivierter 2FA
   - Code-Eingabe (korrekt)
   - Code-Eingabe (falsch)
   - Abgelaufener Code
   - Zu viele Versuche
   - Neuen Code anfordern

2. **2FA Settings Toggle**
   - 2FA aktivieren
   - 2FA deaktivieren

## Mailtrap Setup

1. Account erstellen auf https://mailtrap.io
2. Inbox erstellen
3. SMTP Credentials kopieren:
   - Host: `smtp.mailtrap.io`
   - Port: `587` oder `2525`
   - Username: (aus Mailtrap)
   - Password: (aus Mailtrap)
4. In `application.properties` eintragen

## Sicherheits-Überlegungen

1. **Code-Generierung**: Cryptographically secure random (SecureRandom)
2. **Rate Limiting**: Verhindert Brute-Force (max 3 Versuche)
3. **Zeitlimit**: Codes verfallen nach 5 Minuten
4. **Session-Security**: 2FA-Status nur in Session, nicht in Cookie
5. **Code-Einmaligkeit**: Jeder Code kann nur einmal verwendet werden
6. **Cleanup**: Alte Codes werden regelmäßig gelöscht

## Deployment-Checkliste

- [ ] Mailtrap Account erstellen
- [ ] SMTP Credentials in application.properties eintragen
- [ ] Datenbank-Migration für neue Felder durchführen
- [ ] Tests ausführen
- [ ] Email-Templates testen
- [ ] 2FA Flow manuell testen
- [ ] Für Produktion: Auf echten SMTP-Server umstellen

## Nächste Schritte (Optional/Zukunft)

- SMS-basierte 2FA als Alternative
- Authenticator-App Support (TOTP)
- Backup-Codes für Notfälle
- 2FA für Admin-Aktionen erzwingen
- Audit-Log für 2FA-Ereignisse