
package com.vaultsphere.controller;

import com.vaultsphere.model.AppUser;
import com.vaultsphere.model.StoredDocument;
import com.vaultsphere.repository.DocumentRepository;
import com.vaultsphere.repository.UserRepository;
import com.vaultsphere.service.DocumentService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public class DashboardController {

    private final UserRepository users;
    private final DocumentRepository documents;
    private final DocumentService service;

    public DashboardController(
            UserRepository users,
            DocumentRepository documents,
            DocumentService service) {
        this.users = users;
        this.documents = documents;
        this.service = service;
    }

    private AppUser current(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication required");
        }

        return users.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new org.springframework.security.access.AccessDeniedException(
                                "User account not found"));
    }

    private boolean isAdmin(AppUser user) {
        return "ADMIN".equals(user.getRole())
                || "ROLE_ADMIN".equals(user.getRole());
    }

    private boolean ownsDocument(AppUser user, StoredDocument doc) {
        return doc.getOwner() != null
                && doc.getOwner().getId() != null
                && doc.getOwner().getId().equals(user.getId());
    }

    private String errorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "An operation failed";
        }
        return URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    // USER DASHBOARD

    @GetMapping("/user/dashboard")
    public String userDashboard(Authentication auth, Model model) {
        AppUser user = current(auth);

        if (isAdmin(user)) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute(
                "documents",
                documents.findByOwnerUsernameOrderByUploadedAtDesc(
                        user.getUsername()));
        model.addAttribute(
                "documentCount",
                documents.countByOwnerUsername(user.getUsername()));

        return "user-dashboard";
    }

    // UPLOAD DOCUMENT

    @PostMapping("/documents/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        AppUser user = current(auth);

        if (isAdmin(user)) {
            return "redirect:/admin/dashboard";
        }

        try {
            service.save(file, user);
            return "redirect:/user/dashboard?uploaded";
        } catch (IllegalArgumentException | IOException ex) {
            return "redirect:/user/dashboard?error="
                    + errorMessage(ex);
        }
    }

    // DOWNLOAD DOCUMENT

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable Long id,
            Authentication auth) throws IOException {

        AppUser user = current(auth);

Optional<StoredDocument> result;

if (isAdmin(user)) {
    result = documents.findById(id);
} else {
    result = documents.findByIdAndOwnerUsername(
            id, user.getUsername());
}

if (result.isEmpty()) {
    return ResponseEntity.notFound().build();
}

StoredDocument doc = result.get();


        if (!isAdmin(user) && !ownsDocument(user, doc)) {
            return ResponseEntity.status(403).build();
        }

        Path path = service.path(doc);

        if (!Files.isRegularFile(path) || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        String safeName = doc.getOriginalName()
                .replace("\"", "")
                .replace("\r", "")
                .replace("\n", "");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + safeName + "\"")
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    // DELETE DOCUMENT

    @PostMapping("/documents/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Authentication auth) {

        AppUser user = current(auth);

        
Optional<StoredDocument> result;

if (isAdmin(user)) {
    result = documents.findById(id);
} else {
    result = documents.findByIdAndOwnerUsername(
            id, user.getUsername());
}

if (result.isEmpty()) {
    return isAdmin(user)
            ? "redirect:/admin/documents?error=notfound"
            : "redirect:/user/dashboard?error=notfound";
}

StoredDocument doc = result.get();


        if (!isAdmin(user) && !ownsDocument(user, doc)) {
            return "redirect:/user/dashboard?forbidden";
        }

        try {
            service.deleteFile(doc);
        } catch (IOException ex) {
            return isAdmin(user)
                    ? "redirect:/admin/documents?error=deletefailed"
                    : "redirect:/user/dashboard?error=deletefailed";
        }

        return isAdmin(user)
                ? "redirect:/admin/documents?deleted"
                : "redirect:/user/dashboard?deleted";
    }

    // ADMIN DASHBOARD

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            Authentication auth,
            Model model) {

        AppUser user = current(auth);

        if (!isAdmin(user)) {
            return "redirect:/user/dashboard?forbidden";
        }

        model.addAttribute("user", user);
        model.addAttribute("users", users.findAll());
        model.addAttribute(
                "documents",
                documents.findAllByOrderByUploadedAtDesc());
        model.addAttribute("documentCount", documents.count());

        return "admin-dashboard";
    }

    // ADMIN USER MANAGEMENT

    @GetMapping("/admin/users")
    public String adminUsers(
            Authentication auth,
            Model model) {

        AppUser user = current(auth);

        if (!isAdmin(user)) {
            return "redirect:/user/dashboard?forbidden";
        }

        model.addAttribute("user", user);
        model.addAttribute("users", users.findAll());

        return "admin-users";
    }

    // ADMIN DOCUMENT MANAGEMENT

    @GetMapping("/admin/documents")
    public String adminDocuments(
            Authentication auth,
            Model model) {

        AppUser user = current(auth);

        if (!isAdmin(user)) {
            return "redirect:/user/dashboard?forbidden";
        }

        model.addAttribute("user", user);
        model.addAttribute(
                "documents",
                documents.findAllByOrderByUploadedAtDesc());

        return "admin-documents";
    }

    // ENABLE OR DISABLE A USER ACCOUNT

    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUser(
            @PathVariable Long id,
            Authentication auth) {

        AppUser admin = current(auth);

        if (!isAdmin(admin)) {
            return "redirect:/user/dashboard?forbidden";
        }

        Optional<AppUser> result = users.findById(id);

        if (result.isEmpty()) {
            return "redirect:/admin/users?error=notfound";
        }

        AppUser target = result.get();

        // Never allow disabling the current administrator
        // or changing another administrator's enabled status.
        if (target.getUsername().equals(admin.getUsername())
                || isAdmin(target)) {
            return "redirect:/admin/users?error=notallowed";
        }

        target.setEnabled(!target.isEnabled());
        users.save(target);

        return "redirect:/admin/users?updated";
    }
}
