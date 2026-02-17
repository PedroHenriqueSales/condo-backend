package br.com.aquidolado.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    private final Path uploadsDir;
    private final String uploadsUrlPrefix;
    private final ImageCompressionService imageCompressionService;

    public LocalStorageService(
            @Value("${app.storage.local.path:uploads}") String uploadsPath,
            @Value("${app.storage.local.url-prefix:/uploads}") String urlPrefix,
            ImageCompressionService imageCompressionService) {
        this.uploadsDir = Paths.get(uploadsPath).toAbsolutePath();
        this.uploadsUrlPrefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        this.imageCompressionService = imageCompressionService;
    }

    @Override
    public String save(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }
        try {
            file = imageCompressionService.compressIfNeeded(file);
        } catch (IOException e) {
            log.error("Falha ao comprimir imagem: {}", e.getMessage());
            throw new RuntimeException("Falha ao processar imagem", e);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Imagem muito grande mesmo após compressão. Tente outra imagem.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Use JPEG, PNG ou WebP.");
        }

        String ext = getExtension(contentType);
        String filename = UUID.randomUUID() + ext;
        Path targetDir = uploadsDir.resolve(prefix);
        Path targetFile = targetDir.resolve(filename);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile.toFile());
            String relativePath = prefix + "/" + filename;
            return uploadsUrlPrefix + relativePath;
        } catch (IOException e) {
            log.error("Falha ao salvar arquivo: {}", e.getMessage());
            throw new RuntimeException("Falha ao salvar imagem", e);
        }
    }

    @Override
    public void delete(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.isBlank()) return;
        try {
            String relative = urlOrPath;
            if (relative.startsWith(uploadsUrlPrefix)) {
                relative = relative.substring(uploadsUrlPrefix.length());
            }
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            Path file = uploadsDir.resolve(relative);
            if (Files.exists(file) && Files.isRegularFile(file)) {
                Files.delete(file);
            }
        } catch (IOException e) {
            log.warn("Falha ao remover arquivo {}: {}", urlOrPath, e.getMessage());
        }
    }

    @Override
    public void deleteByPrefix(String prefix) {
        Path dir = uploadsDir.resolve(prefix);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return;
        try {
            Files.walk(dir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            if (Files.isRegularFile(p)) {
                                Files.delete(p);
                            }
                        } catch (IOException e) {
                            log.warn("Falha ao remover {}: {}", p, e.getMessage());
                        }
                    });
            Files.deleteIfExists(dir);
        } catch (IOException e) {
            log.warn("Falha ao remover diretório {}: {}", prefix, e.getMessage());
        }
    }

    private String getExtension(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
