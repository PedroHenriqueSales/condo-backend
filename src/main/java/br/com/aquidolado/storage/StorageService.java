package br.com.aquidolado.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Armazena arquivos e retorna a URL/path para acesso.
 * Implementações: LocalStorageService (dev), S3StorageService (prod).
 */
public interface StorageService {

    /**
     * Salva um arquivo e retorna a URL ou path para acesso.
     *
     * @param file     arquivo enviado
     * @param prefix   prefixo do path (ex: "ads/123")
     * @return URL ou path relativo (ex: "/uploads/ads/123/uuid.jpg")
     */
    String save(MultipartFile file, String prefix);

    /**
     * Remove um arquivo pelo path/URL retornado por save().
     */
    void delete(String urlOrPath);

    /**
     * Remove todos os arquivos com o prefixo dado.
     */
    void deleteByPrefix(String prefix);
}
