# Threat Model — VaultSphere

## 1. Overview

VaultSphere is a Spring Boot web application for user authentication and document storage. Users can register, log in, upload, download, and delete their documents. Administrators can manage users and view stored documents.

## 2. Assets to Protect

- Usernames, email addresses, and password hashes
- Uploaded documents and their contents
- User sessions and authentication credentials
- User roles and administrative functions
- Database records and application configuration

## 3. Potential Threats

| Threat | Potential Impact | Mitigation |
|---|---|---|
| Unauthorized access | Private documents or user data exposed | Spring Security authentication and role-based authorization |
| Cross-user document access | A user accesses another user's files | Verify document ownership before download or deletion |
| Weak or exposed passwords | Accounts compromised | BCrypt password hashing and secure password practices |
| Malicious file uploads | Unsafe or unexpected files stored | Validate file extensions and enforce upload size limits |
| Session misuse | Unauthorized use of an authenticated session | Invalidate sessions on logout and protect authenticated routes |
| Privilege escalation | A regular user gains administrative access | Enforce server-side role checks |
| Brute-force login attempts | Accounts targeted by repeated guesses | Consider login rate limiting and account protection |
| Data loss | Documents or database records disappear | Use persistent storage and tested backups |
| Sensitive information exposure | Credentials or private data leaked | Keep secrets out of source control and use HTTPS in deployment |

## 4. Trust Boundaries

- Browser to application: all client input is untrusted.
- Application to database: database access must be controlled.
- Application to file storage: filenames and file contents must be validated.
- User to administrator functions: administrative operations require explicit authorization.

## 5. Existing Security Controls

The application is designed to use Spring Security, BCrypt password hashing, role-based access restrictions, document ownership checks, upload validation, and logout session invalidation.

These controls should be verified through automated tests and deployment checks.

## 6. Recommended Improvements

- Add login rate limiting and monitoring.
- Use CSRF protection for browser-based state-changing requests.
- Store production data in persistent, access-controlled database and file storage.
- Keep secrets in deployment environment variables.
- Add file-content validation and malware scanning where appropriate.
- Review dependencies and keep them updated.
- Test authorization boundaries and document ownership regularly.

## 7. Scope and Limitations

This document identifies likely threats and planned mitigations. It is not a formal security audit, and listing a control does not prove that it is effective in every deployment.
