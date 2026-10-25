
package com.vaultsphere.service;

import com.vaultsphere.model.AppUser;
import com.vaultsphere.model.StoredDocument;
import com.vaultsphere.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documents;
    private final Path uploadPath;

    private static final long MAX_SIZE = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED = Set.of(
            "pdf", "png", "jpg", "jpeg", "txt",
            "doc", "docx", "xls", "xlsx", "csv"
    );

    public DocumentService(
            DocumentRepository documents,
            @Value("${vaultsphere.upload-dir}") String dir
    ) throws IOException {

        this.documents = documents;

        this.uploadPath = Paths.get(dir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadPath);
    }

    public StoredDocument save(
            MultipartFile file,
            AppUser owner
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please choose a file to upload."
            );
        }

        if (owner == null) {
            throw new IllegalArgumentException(
                    "A valid document owner is required."
            );
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Maximum file size is 10 MB."
            );
        }

        String suppliedName = file.getOriginalFilename();

        if (suppliedName == null || suppliedName.isBlank()) {
            throw new IllegalArgumentException(
                    "The file must have a valid name."
            );
        }

        // Extract the filename without directory components.
        String original = Paths.get(suppliedName)
                .getFileName()
                .toString();

        if (original.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid filename."
            );
        }

        int dot = original.lastIndexOf('.');

        if (dot <= 0 || dot == original.length() - 1) {
            throw new IllegalArgumentException(
                    "The file must have an allowed extension."
            );
        }

        String ext = original.substring(dot + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED.contains(ext)) {
            throw new IllegalArgumentException(
                    "Allowed types: PDF, PNG, JPG, JPEG, TXT, "
                    + "DOC, DOCX, XLS, XLSX and CSV."
            );
        }

        // Never use the supplied filename as the storage name.
        String stored = UUID.randomUUID() + "." + ext;
        Path target = resolveStoredPath(stored);

        try (InputStream input = file.getInputStream()) {
            // Do not overwrite an existing file.
            Files.copy(input, target);
        }

        try {
            StoredDocument doc = new StoredDocument();

            doc.setOriginalName(original);
            doc.setStoredName(stored);

            // Metadata only; this value does not validate file contents.
            doc.setContentType(
                    file.getContentType() == null
                            ? "application/octet-stream"
                            : file.getContentType()
            );

            doc.setSizeBytes(file.getSize());
            doc.setOwner(owner);

            return documents.save(doc);

        } catch (RuntimeException ex) {
            // Avoid leaving an orphaned file if the database save fails.
            try {
                Files.deleteIfExists(target);
            } catch (IOException cleanupError) {
                ex.addSuppressed(cleanupError);
            }

            throw ex;
        }
    }

    public Path path(StoredDocument doc) {
        if (doc == null || doc.getStoredName() == null) {
            throw new IllegalArgumentException(
                    "Invalid document."
            );
        }

        return resolveStoredPath(doc.getStoredName());
    }

    private Path resolveStoredPath(String storedName) {
        // Reject path components and directory traversal.
        Path name = Paths.get(storedName);

        if (name.isAbsolute()
                || name.getNameCount() != 1
                || !name.getFileName().toString().equals(storedName)) {
            throw new IllegalArgumentException(
                    "Invalid stored filename."
            );
        }

        Path resolved = uploadPath.resolve(name).normalize();

        if (!resolved.startsWith(uploadPath)
                || resolved.equals(uploadPath)) {
            throw new IllegalArgumentException(
                    "Invalid file path."
            );
        }

        return resolved;
    }

    public void deleteFile(StoredDocument doc) throws IOException {
        Path filePath = path(doc);

        // Remove the database record only after the file is deleted.
        Files.deleteIfExists(filePath);
        documents.delete(doc);
    }
}
