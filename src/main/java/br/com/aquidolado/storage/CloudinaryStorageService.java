package br.com.aquidolado.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Profile("prod")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryStorageService implements StorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    private final Cloudinary cloudinary;
    private final String folder;
    private final ImageCompressionService imageCompressionService;

    public CloudinaryStorageService(
            @Value("${app.storage.cloudinary.cloud-name}") String cloudName,
            @Value("${app.storage.cloudinary.api-key}") String apiKey,
            @Value("${app.storage.cloudinary.api-secret}") String apiSecret,
            @Value("${app.storage.cloudinary.folder:aquidolado}") String folder,
            ImageCompressionService imageCompressionService) {
        this.folder = folder;
        this.imageCompressionService = imageCompressionService;

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);
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

        try {
            String publicId = folder + "/" + prefix + "/" + UUID.randomUUID();
            
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("resource_type", "image");
            uploadParams.put("overwrite", false);

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadParams
            );

            String url = (String) uploadResult.get("secure_url");
            log.info("Imagem salva no Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Falha ao fazer upload para Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Falha ao salvar imagem no Cloudinary", e);
        }
    }

    @Override
    public void delete(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.isBlank()) {
            return;
        }

        try {
            // Extrai o public_id da URL do Cloudinary
            String publicId = extractPublicId(urlOrPath);
            if (publicId == null) {
                log.warn("Não foi possível extrair public_id da URL: {}", urlOrPath);
                return;
            }

            Map<String, Object> deleteParams = ObjectUtils.emptyMap();
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, deleteParams);
            
            String result = (String) deleteResult.get("result");
            if ("ok".equals(result)) {
                log.info("Imagem removida do Cloudinary: {}", publicId);
            } else {
                log.warn("Falha ao remover imagem do Cloudinary: {}", deleteResult);
            }

        } catch (Exception e) {
            log.warn("Erro ao remover imagem do Cloudinary {}: {}", urlOrPath, e.getMessage());
        }
    }

    @Override
    public void deleteByPrefix(String prefix) {
        try {
            String folderPath = folder + "/" + prefix;
            
            // Lista todos os recursos com o prefixo
            Map<String, Object> listParams = new HashMap<>();
            listParams.put("type", "upload");
            listParams.put("prefix", folderPath);
            listParams.put("max_results", 500);

            ApiResponse listResult = cloudinary.api().resources(listParams);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resources = (List<Map<String, Object>>) listResult.get("resources");
            
            if (resources == null || resources.isEmpty()) {
                log.info("Nenhum recurso encontrado com prefixo: {}", folderPath);
                return;
            }

            // Extrai os public_ids
            List<String> publicIds = new ArrayList<>();
            for (Map<String, Object> resource : resources) {
                String publicId = (String) resource.get("public_id");
                if (publicId != null) {
                    publicIds.add(publicId);
                }
            }

            if (publicIds.isEmpty()) {
                return;
            }

            // Remove todos os recursos de uma vez
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("public_ids", publicIds);
            
            Map<String, Object> deleteResult = cloudinary.api().deleteResources(publicIds, deleteParams);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> deleted = (Map<String, Object>) deleteResult.get("deleted");
            if (deleted != null) {
                log.info("Removidos {} recursos do Cloudinary com prefixo: {}", deleted.size(), folderPath);
            }

        } catch (Exception e) {
            log.error("Erro ao remover recursos por prefixo do Cloudinary {}: {}", prefix, e.getMessage());
        }
    }

    /**
     * Extrai o public_id de uma URL do Cloudinary.
     * Exemplo: https://res.cloudinary.com/cloud_name/image/upload/v1234567/folder/prefix/uuid.jpg
     * Retorna: folder/prefix/uuid
     */
    private String extractPublicId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            // Se já for um public_id (sem http), retorna como está
            if (!url.startsWith("http")) {
                return url;
            }

            // Extrai o public_id da URL do Cloudinary
            // Formato: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{ext}
            String[] parts = url.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];
            // Remove a versão se existir (v1234567/)
            if (afterUpload.matches("^v\\d+/.*")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }

            // Remove a extensão
            int lastDot = afterUpload.lastIndexOf('.');
            if (lastDot > 0) {
                afterUpload = afterUpload.substring(0, lastDot);
            }

            return afterUpload;

        } catch (Exception e) {
            log.warn("Erro ao extrair public_id da URL: {}", url);
            return null;
        }
    }
}
