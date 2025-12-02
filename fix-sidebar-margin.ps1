# PowerShell script to add lg:ml-64 margin to all templates (except login, register, verify-2fa)

$templatesPath = "src/main/resources/templates"
$excludeFiles = @("login.html", "register.html", "verify-2fa.html")

# Find all HTML files except fragments and excluded files
$htmlFiles = Get-ChildItem -Path $templatesPath -Recurse -Filter "*.html" | 
    Where-Object { 
        $_.Directory.Name -ne "fragments" -and 
        $excludeFiles -notcontains $_.Name 
    }

foreach ($file in $htmlFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Skip if already has lg:ml-64
    if ($content -match 'lg:ml-64') {
        Write-Host "Skipping $($file.Name) - already has lg:ml-64"
        continue
    }
    
    # Pattern 1: After navbar, find first <div class="container
    $pattern1 = '(<div th:replace="[^"]*navbar[^"]*"></div>\s*\n\s*)(<div class="container[^>]*>)'
    $replacement1 = '$1<div class="lg:ml-64">' + "`n" + '$2'
    
    if ($content -match $pattern1) {
        $content = $content -replace $pattern1, $replacement1
        
        # Add closing div before </body>
        $content = $content -replace '(</div>\s*</body>)', '</div>' + "`n" + '$1'
        
        Set-Content -Path $file.FullName -Value $content
        Write-Host "Updated $($file.Name)"
    } else {
        Write-Host "Pattern not found in $($file.Name)"
    }
}

Write-Host "`nDone!"