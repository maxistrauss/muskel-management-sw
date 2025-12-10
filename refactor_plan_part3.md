# Plan: Refactor "My Courses" Page and Secure Reviews

This document outlines the plan to refactor the member's "My Courses" page to a table view with specific actions, and to add security to ensure members can only review courses they are enrolled in.

## Part 1: Refactor "My Courses" Page (`/member/course-info/my`)

1.  **Identify Controller and Template:**
    -   Locate the controller method responsible for handling requests to `/member/course-info/my`.
    -   Locate the corresponding Thymeleaf template, which is expected to be `src/main/resources/templates/member/my-courses.html`.

2.  **Update `my-courses.html` Template:**
    -   **Convert to Table View:** Change the entire layout to use a `<table>` to display the list of enrolled courses.
    -   **Table Columns:** The table should include columns for relevant course information, such as:
        -   Course Name
        -   Trainer
        -   Start & End Dates
        -   Actions
    -   **Implement Action Buttons:** In the "Actions" column for each course, add three distinct, button-styled links:
        -   **Info:** An `<a>` tag styled as a button pointing to the main course details page (e.g., `@{|/course-details/${course.id}|}`).
        -   **Unenroll:** A small `<form>` containing a "Unenroll" button that submits a `POST` request to the unenrollment endpoint (e.g., `@{|/member/course-info/${course.id}/unenroll|}`).
        -   **Write Review:** An `<a>` tag styled as a button pointing to the review submission page (e.g., `@{|/member/course-info/${id}/reviews/new|}`).

3.  **Verify Controller Method:**
    -   Check the controller method for `/member/course-info/my` to ensure it passes the necessary list of enrolled courses to the template. No changes are anticipated if it already provides this data.

## Part 2: Secure Review Submission

1.  **Identify Review Form Controller:**
    -   Locate the controller method that handles `GET` requests for the review form page (e.g., `/member/course-info/{id}/reviews/new`).

2.  **Add Backend Authorization Check:**
    -   In this controller method, add a security check at the very beginning.
    -   The check must verify if the currently authenticated user is enrolled in the course specified by the `{id}` path variable.
    -   If the user is **not** enrolled, the method should throw an `AccessDeniedException` to prevent unauthorized access. This ensures that even if a user tries to access the URL directly, they cannot write a review for a course they haven't taken.

3.  **Verify General Course Overview Page:**
    -   Briefly inspect the template for the general course overview (`/member/course-info`) to confirm that its "Write Review" button is already conditionally displayed only for enrolled courses (`th:if="${enrolled}"`). This ensures consistent behavior across the application.

By following this plan, the "My Courses" page will be improved for better usability, and a critical security check will be added to the review submission process.
