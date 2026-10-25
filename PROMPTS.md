# Development Prompts — VaultSphere

This document records representative prompts for the development, review, and testing of VaultSphere.

## 1. Application Development

> Build a Spring Boot document management application named VaultSphere using Java, Spring Security, Thymeleaf, Spring Data JPA, and H2. Support user registration, login, document upload, download, and deletion.

## 2. Authentication and Authorization

> Review the Spring Security configuration. Ensure passwords are hashed with BCrypt, users receive appropriate roles, disabled accounts cannot log in, and administrative endpoints are restricted to administrators.

## 3. Document Ownership

> Review document download and deletion logic. Ensure a regular user can access only their own documents, while administrator access follows the application's intended authorization policy.

## 4. File Upload Validation

> Review document upload handling. Enforce the configured file-size limit, validate permitted file extensions, generate safe server-side storage names, and avoid trusting user-supplied filenames.

## 5. Security Testing

> Create tests for registration validation, login and logout behavior, disabled accounts, role-based access, cross-user document access, and invalid or oversized uploads. Verify the expected behavior rather than assuming the implementation is secure.

## 6. Deployment

> Prepare the Spring Boot application for deployment using Docker. Configure the application to use the hosting platform's assigned port and identify database and uploaded-file persistence requirements.

## 7. Documentation and Review

> Review the project README and security documentation for accuracy. Clearly distinguish implemented security controls from recommended future improvements.

## Notes

These are representative prompts for documenting the development workflow. Update this file to reflect the prompts actually used during the project, the tools involved, and any resulting changes. Do not present example prompts as a complete historical record unless they match the actual development process.
