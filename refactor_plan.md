# Plan: Refactor Course Member Management

This document outlines the steps to refactor the member management functionality for courses. The goal is to move all member management (add/remove) directly onto the course edit pages for both `Admin` and `Trainer` roles, and to eliminate the separate pages for participant lists and adding members.

## Part 1: Backend Refactoring (`TrainerController`)

1.  **Update `showEditCourseForm` Method:**
    -   Modify the existing `GET` mapping for `/trainer/courses/edit/{id}`.
    -   In addition to the `Course` object, the model must be populated with:
        -   `enrolledMembers`: A list of `User` or `UserDto` objects currently enrolled in the course.
        -   `availableMembers`: A list of all users with `ROLE_MEMBER` who are *not* currently enrolled in this specific course. This will populate the "add member" dropdown.
    -   This ensures the edit page has all the necessary data to display both current participants and potential new ones.

2.  **Create New Endpoints for Member Management:**
    -   **Add Member:** Create a new `POST` mapping: `/trainer/courses/edit/{id}/add-member`.
        -   This method will accept a `userId` from the form payload.
        -   It will call the appropriate service method (e.g., `courseService.addMember(courseId, userId)`).
        -   It must include the same authorization check as the edit page (isOwner or isAdmin).
        -   It will redirect back to `/trainer/courses/edit/{id}`.
    -   **Remove Member:** Create a new `POST` mapping: `/trainer/courses/edit/{id}/remove-member/{memberId}`.
        -   This method will accept `memberId` as a path variable.
        -   It will call the service method to remove the member from the course.
        -   It must include the same authorization check.
        -   It will redirect back to `/trainer/courses/edit/{id}`.

3.  **Remove Obsolete Methods and Mappings:**
    -   Delete the `GET` method `viewParticipants` associated with `/trainer/courses/{id}/participants`.
    -   Delete the `GET` method `showAddMemberForm` associated with `/trainer/courses/{id}/add-member`.
    -   Delete the `POST` method `addMember` associated with `/trainer/courses/{id}/add-member`.
    -   Delete the `POST` method `removeMember` associated with `/trainer/courses/{id}/remove-member/{userId}`.

## Part 2: Backend Refactoring (`AdminController`)

1.  **Update `showEditCourseForm` Method:**
    -   Modify the existing `GET` mapping for `/admin/courses/edit/{id}`.
    -   Just like the `TrainerController` changes, populate the model with `enrolledMembers` and `availableMembers`.

2.  **Create New Endpoints for Member Management:**
    -   **Add Member:** Create a new `POST` mapping: `/admin/courses/edit/{id}/add-member`.
        -   This will be functionally identical to the trainer's version but will reside in the `AdminController`.
        -   It will redirect back to `/admin/courses/edit/{id}`.
    -   **Remove Member:** Create a new `POST` mapping: `/admin/courses/edit/{id}/remove-member/{memberId}`.
        -   Functionally identical to the trainer's version.
        -   It will redirect back to `/admin/courses/edit/{id}`.

3.  **Remove Obsolete Methods and Mappings (if any):**
    -   Check `AdminController` for any existing participant management endpoints (like `/admin/courses/{id}/participants`) and remove them if they exist and are being replaced.

## Part 3: Frontend Template Refactoring

1.  **Modify `trainer/course-form.html`:**
    -   Below the main course details form, add a new "Participant Management" section, visible only in edit mode (`th:if="${course.id != null}"`).
    -   **Display Enrolled Members:**
        -   Create a table or list to display `enrolledMembers`.
        -   Each row will show member details (e.g., name, email).
        -   Each row will contain a small form with a "Remove" button that submits a `POST` request to `/trainer/courses/edit/{id}/remove-member/{memberId}`.
    -   **Add New Member:**
        -   Create a form for adding a new member.
        -   This form will contain a `<select>` dropdown populated by the `availableMembers` list.
        -   A "Add Member" button will submit this form via `POST` to `/trainer/courses/edit/{id}/add-member`.

2.  **Modify `admin/course-form.html`:**
    -   Implement the exact same "Participant Management" section as in the trainer's form.
    -   Ensure all form `th:action` attributes point to the correct `/admin/...` endpoints.

3.  **Delete Obsolete Template Files:**
    -   Delete `src/main/resources/templates/trainer/course-participants.html`.
    -   Delete `src/main/resources/templates/trainer/add-member.html`.

## Part 4: Service and Repository Layer Verification

1.  **`CourseService`:**
    -   Verify the existence and signature of methods for adding, removing, and listing members/enrollments (e.g., `addMember`, `removeMember`, `listEnrollments`). The existing controllers suggest these are already implemented.
2.  **`UserService`/`UserRepository`:**
    -   Ensure there's an efficient way to fetch all users with `ROLE_MEMBER`. A method like `findAllByRole(String role)` would be ideal. If not, the current approach of filtering all users will be used.

By following this plan, the member management functionality will be streamlined and integrated directly into the course editing workflow for both trainers and admins, as requested.
