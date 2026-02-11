package br.com.aquidolado.util;

/**
 * Normaliza número de telefone: apenas dígitos.
 * Se tiver menos de 12 dígitos, considera como número brasileiro e adiciona o código do país (55).
 */
public final class PhoneUtil {

    private static final int MIN_DIGITS_WITH_COUNTRY = 12; // 55 + 10 dígitos (DDD + número)

    private PhoneUtil() {
    }

    /**
     * @param raw telefone como digitado (pode ter espaços, traços, parênteses, +)
     * @return apenas dígitos; se tiver menos de 12 dígitos, prefixa com 55 (Brasil)
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < MIN_DIGITS_WITH_COUNTRY) {
            digits = "55" + digits;
        }
        return digits;
    }
}
