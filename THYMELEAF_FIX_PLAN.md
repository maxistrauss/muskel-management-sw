# Thymeleaf Error Fix - Implementation Plan

## Problem Summary

**Error**: `TemplateInputException` when accessing any page with the navbar
**Root Cause**: `#request.requestURI` is no longer available in Thymeleaf 3.1+
**Location**: [`navbar.html`](src/main/resources/templates/fragments/navbar.html) lines 20, 23, 26

## Solution Overview

Create a `@ControllerAdvice` class to automatically inject the current request URI into all models, then update the navbar template to use this model attribute.

---

## Code Changes

### 1. Create New File: `GlobalControllerAdvice.java`

**File Path**: `src/main/java/de/oth/muskelmanagement/config/GlobalControllerAdvice.java`

**Complete Code**:
```java
package de.oth.muskelmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds common model attributes to all controllers.
 * This is used to make the current request URI available to Thymeleaf templates
 * since #request is no longer available by default in Thymeleaf 3.1+.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Adds the current request URI to the model for all controller methods.
     * This allows templates to access the current URI via ${currentUri}.
     *
     * @param request the current HTTP request
     * @return the request URI
     */
    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
```

**What it does**:
- Intercepts all controller requests
- Automatically adds `currentUri` to the model
- Makes the URI available to all Thymeleaf templates via `${currentUri}`

---

### 2. Update Existing File: `navbar.html`

**File Path**: `src/main/resources/templates/fragments/navbar.html`

#### Changes on Line 20:
**BEFORE**:
```html
th:classappend="${#request.requestURI.contains('/admin/users') ? 'text-gray-700 hover:text-gray-900' : ''}"
```

**AFTER**:
```html
th:classappend="${currentUri.contains('/admin/users') ? 'text-gray-700 hover:text-gray-900' : ''}"
```

#### Changes on Line 23:
**BEFORE**:
```html
th:classappend="${#request.requestURI.contains('/admin/equipment') ? 'text-gray-700 hover:text-gray-900' : ''}"
```

**AFTER**:
```html
th:classappend="${currentUri.contains('/admin/equipment') ? 'text-gray-700 hover:text-gray-900' : ''}"
```

#### Changes on Line 26:
**BEFORE**:
```html
th:classappend="${#request.requestURI.contains('/admin/tarifs') ? 'text-gray-700 hover:text-gray-900' : ''}"
```

**AFTER**:
```html
th:classappend="${currentUri.contains('/admin/tarifs') ? 'text-gray-700 hover:text-gray-900' : ''}"
```

#### Complete Updated navbar.html:
```html
<!DOCTYPE html>
<html xmlns:sec="http://www.thymeleaf.org/extras/spring-security" xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Navbar Fragment</title>
</head>
<body>
<!-- Navigation Bar Fragment -->
<nav class="bg-white shadow-md" th:fragment="navbar">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
            <div class="flex">
                <div class="flex-shrink-0 flex items-center">
                    <a class="text-2xl font-bold text-gray-800" href="/">MuskelManagement</a>
                </div>
            </div>
            <div class="flex items-center">
                <div sec:authorize="hasRole('ADMIN')">
                    <a class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
                       href="/admin/users"
                       th:classappend="${currentUri.contains('/admin/users') ? 'text-gray-700 hover:text-gray-900' : ''}">User Management</a>
                    <a class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
                       href="/admin/equipment"
                       th:classappend="${currentUri.contains('/admin/equipment') ? 'text-gray-700 hover:text-gray-900' : ''}">Equipment Management</a>
                    <a class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
                       href="/admin/tarifs"
                       th:classappend="${currentUri.contains('/admin/tarifs') ? 'text-gray-700 hover:text-gray-900' : ''}">Tarif Management</a>
                </div>
                <form class="ml-4" method="post" th:action="@{/logout}">
                    <input class="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                           type="submit" value="Logout"/>
                </form>
            </div>
        </div>
    </div>
</nav>
</body>
</html>
```

---

## Summary of Changes

### Files to Create:
1. ✨ **NEW**: `src/main/java/de/oth/muskelmanagement/config/GlobalControllerAdvice.java`

### Files to Modify:
1. ✏️ **UPDATE**: `src/main/resources/templates/fragments/navbar.html` (3 lines changed)

### Total Changes:
- 1 new file (24 lines)
- 1 modified file (3 replacements)

---

## Expected Behavior After Fix

✅ All pages will load without errors
✅ Navigation bar will display correctly
✅ Active navigation links will be highlighted with the darker text color
✅ No functional changes to the application behavior

---

## Testing Recommendations

After implementing these changes:

1. **Start the application**
2. **Navigate to**: `/` (Welcome page)
3. **Verify**: Page loads without error
4. **Navigate to**: `/admin/users`
5. **Verify**: "User Management" link is highlighted
6. **Navigate to**: `/admin/equipment`
7. **Verify**: "Equipment Management" link is highlighted
8. **Navigate to**: `/admin/tarifs`
9. **Verify**: "Tarif Management" link is highlighted

---

## Why This Solution?

✅ **Best Practice**: Uses Spring's `@ControllerAdvice` pattern
✅ **Centralized**: Single place to manage global model attributes
✅ **Secure**: Follows Thymeleaf 3.1+ security recommendations
✅ **Maintainable**: No changes needed to existing controllers
✅ **Scalable**: Easy to add more global attributes in the future