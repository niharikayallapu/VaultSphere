# VaultSphere — Secure Document Workspace

A Spring Boot 3 / Java 17 project with separate Admin and User experiences, server-side role authorization, BCrypt password hashing, H2 file database, and local document storage.

## Requirements
- JDK 17 or newer
- Maven 3.9+ (or open the project in IntelliJ IDEA / VS Code with Java extensions)
- Internet for the first Maven dependency download. The UI uses Google Fonts; it falls back to system fonts if offline.

## Run on Windows
1. Extract `VaultSphere.zip` to a folder such as `C:\Projects\VaultSphere`.
2. Open that folder in VS Code.
3. Check Java: open Terminal → run `java -version` and `mvn -version`.
4. If Maven is installed, run `mvn spring-boot:run` from the folder containing `pom.xml`.
5. Open `http://localhost:8080`.

If Maven is not installed, use VS Code's Java Extension Pack and install Maven, or open the folder in IntelliJ IDEA and run `VaultSphereApplication.java`.

## Demo administrator
- Username: `admin`
- Password: `ChangeMe@123`
- Change this demo password before any public deployment. The account is created automatically on first start if it does not already exist.

## User journey
- Click **Create an account** to register a standard User.
- Sign in; the server redirects Admin to `/admin/dashboard` and User to `/user/dashboard`.
- Users can upload, download and delete their own documents (maximum 10 MB per file).
- Admin can see account and document metadata, disable/enable standard user accounts, and download/delete documents.

## Storage
- H2 database files are created under `./data/`.
- Uploaded file bytes are stored under `./uploads/`.
- Do not delete these folders if you want to preserve local data.

## Security notes / before production
- Admin demo credentials are for local demonstration only. Change the seeded password and use environment-based secrets before deployment.
- H2 and local disk are intended for a deadline-friendly local MVP. For a public deployment use a managed database and persistent/private file storage.
- Configure HTTPS, backup, rate limiting, and audit logging before real-world use. Do not upload confidential or sensitive files to an unreviewed deployment.
- The current registration endpoint always assigns `USER`; never add a public role selector.
- Spring Security protects routes server-side. Users cannot access other users' documents by changing an ID; ownership is checked before download/delete.
