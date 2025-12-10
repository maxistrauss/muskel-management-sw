# Plan: Refactor Trainer Course View and Actions

This document outlines the plan to introduce a new "Info" page for courses in the trainer's section and to simplify the actions available on the course list page.

## Part 1: Backend (`TrainerController`)

1.  **Create New Course Info Endpoint:**
    -   Create a new `GET` mapping: `@GetMapping("/courses/{id}/info")`.
    -   The corresponding method, `showCourseInfo`, will handle rendering the new course information page.
    -   **Authorization:** This method must include the standard authorization check to ensure the user is either the assigned trainer for the course or an admin.
    -   **Model Population:** The method will add the following to the model:
        -   The `Course` object, fetched by its ID.
        -   A list of all `Review` objects associated with that course.

2.  **Remove Redundant Endpoint:**
    -   The existing `GET` mapping for `/courses/{id}/reviews` (`viewReviews` method) will become obsolete, as its functionality is being merged into the new info page. This method will be deleted from `TrainerController`.

## Part 2: Frontend (Templates)

1.  **Create New `course-info.html` for Trainer:**
    -   A new template file will be created at `src/main/resources/templates/trainer/course-info.html`.
    -   The content for this new file will be copied from the general course details page (`src/main/resources/templates/course-details.html`).
    -   **Modifications:** Inside the new `trainer/course-info.html`, the following elements will be located and removed:
        -   The "Enroll in Course" button/form.
        -   The "Write a Review" button/link.
    -   The rest of the view, including the section that displays existing reviews, will remain.

2.  **Update Actions on `trainer/courses.html`:**
    -   The "Actions" column in the table on the `/trainer/courses` page will be modified.
    -   The existing links will be replaced with three clear actions:
        -   **Info:** A link pointing to the new `/trainer/courses/{id}/info` page.
        -   **Manage:** A link pointing to the existing `/trainer/courses/edit/{id}` page.
        -   **Delete:** The existing link to `/trainer/courses/delete/{id}`.
    -   The standalone "Reviews" link will be removed from this action list.

3.  **Delete Redundant Template:**
    -   The template file `src/main/resources/templates/trainer/course-reviews.html` will be deleted as it is no longer used.

By completing these steps, the trainer's interface will be streamlined, providing a clear "Info" page with all relevant details (including reviews) and a simplified set of actions on the main course list.
