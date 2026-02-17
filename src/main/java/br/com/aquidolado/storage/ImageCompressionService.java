package br.com.aquidolado.storage;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Redimensiona e comprime imagens para caber no limite de armazenamento (5MB).
 * Imagens maiores que o alvo são convertidas para JPEG com qualidade reduzida e tamanho máximo 1920px.
 */
@Slf4j
@Service
public class ImageCompressionService {

    private static final long TARGET_MAX_BYTES = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DIMENSION = 1920;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    /**
     * Comprime a imagem se necessário para ficar dentro do limite. Retorna o arquivo original
     * se já estiver dentro do limite e com dimensões aceitáveis; caso contrário, retorna
     * uma versão redimensionada em JPEG.
     */
    public MultipartFile compressIfNeeded(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Use JPEG, PNG ou WebP.");
        }

        long size = file.getSize();
        if (size <= TARGET_MAX_BYTES) {
            return file;
        }

        log.debug("Comprimindo imagem (tamanho original {} bytes)", size);
        BufferedImage source = Thumbnails.of(file.getInputStream()).scale(1).asBufferedImage();
        String baseName = file.getOriginalFilename();
        if (baseName == null || baseName.isBlank()) baseName = "image";
        if (baseName.lastIndexOf('.') > 0) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        byte[] compressed = compressToTarget(source);
        return new BytesMultipartFile(
                file.getName(),
                baseName + ".jpg",
                "image/jpeg",
                compressed
        );
    }

    private byte[] compressToTarget(BufferedImage source) throws IOException {
        double quality = 0.85;
        int maxDim = MAX_DIMENSION;
        final int minDimension = 800;

        for (int attempt = 0; attempt < 3; attempt++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(source)
                    .size(maxDim, maxDim)
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toOutputStream(out);
            byte[] bytes = out.toByteArray();
            if (bytes.length <= TARGET_MAX_BYTES) {
                log.debug("Imagem comprimida para {} bytes (quality={}, maxDim={})", bytes.length, quality, maxDim);
                return bytes;
            }
            quality = Math.max(0.5, quality - 0.15);
            maxDim = (int) (maxDim * 0.85);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(source)
                .size(minDimension, minDimension)
                .outputFormat("jpg")
                .outputQuality(0.5)
                .toOutputStream(out);
        return out.toByteArray();
    }
}
