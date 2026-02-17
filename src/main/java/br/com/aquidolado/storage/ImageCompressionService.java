package br.com.aquidolado.storage;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * Redimensiona e comprime imagens para caber no limite de armazenamento (5MB).
 * Evita OutOfMemoryError: lê dimensões pelo header (sem decodificar pixels) e rejeita
 * imagens com resolução muito alta; processa em um único passo com size() para limitar memória.
 */
@Slf4j
@Service
public class ImageCompressionService {

    private static final long TARGET_MAX_BYTES = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DIMENSION = 1920;
    /** Acima desse lado (px) a decodificação usaria memória excessiva (heap). */
    private static final int MAX_SIDE_BEFORE_DECODE = 2048;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    /**
     * Comprime a imagem se necessário para ficar dentro do limite. Retorna o arquivo original
     * se já estiver dentro do limite; caso contrário, retorna uma versão redimensionada em JPEG.
     * Rejeita imagens com resolução muito alta para evitar OutOfMemoryError.
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

        int width;
        int height;
        try (InputStream is = file.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(is)) {
            int[] dims = readDimensions(iis);
            if (dims == null) {
                throw new IllegalArgumentException("Não foi possível ler as dimensões da imagem. Tente outro arquivo.");
            }
            width = dims[0];
            height = dims[1];
        }

        int maxSide = Math.max(width, height);
        if (maxSide > MAX_SIDE_BEFORE_DECODE) {
            throw new IllegalArgumentException(
                    "Imagem com resolução muito alta (" + width + "x" + height + "). "
                            + "Reduza para no máximo " + MAX_SIDE_BEFORE_DECODE + "px no maior lado ou use uma foto com menos megapixels.");
        }

        log.debug("Comprimindo imagem (tamanho original {} bytes, {}x{})", size, width, height);
        String baseName = file.getOriginalFilename();
        if (baseName == null || baseName.isBlank()) baseName = "image";
        if (baseName.lastIndexOf('.') > 0) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        byte[] fileBytes = file.getBytes();
        byte[] compressed = compressFromBytes(fileBytes);
        return new BytesMultipartFile(
                file.getName(),
                baseName + ".jpg",
                "image/jpeg",
                compressed
        );
    }

    /**
     * Lê largura e altura pelo header (sem decodificar pixels). Retorna [width, height] ou null.
     */
    private int[] readDimensions(ImageInputStream iis) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (!readers.hasNext()) return null;
        ImageReader reader = readers.next();
        try {
            reader.setInput(iis, true);
            int w = reader.getWidth(0);
            int h = reader.getHeight(0);
            return new int[]{w, h};
        } finally {
            reader.dispose();
        }
    }

    /**
     * Redimensiona e comprime a partir dos bytes. Cada tentativa usa um novo ByteArrayInputStream.
     */
    private byte[] compressFromBytes(byte[] fileBytes) throws IOException {
        double quality = 0.85;
        int maxDim = MAX_DIMENSION;
        final int minDimension = 800;

        for (int attempt = 0; attempt < 3; attempt++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(fileBytes))
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
        Thumbnails.of(new ByteArrayInputStream(fileBytes))
                .size(minDimension, minDimension)
                .outputFormat("jpg")
                .outputQuality(0.5)
                .toOutputStream(out);
        return out.toByteArray();
    }
}
