package br.com.aquidolado.exception;

/**
 * Lançada quando o usuário tenta denunciar um anúncio que já denunciou.
 */
public class AlreadyReportedException extends RuntimeException {

    public AlreadyReportedException() {
        super("Você já denunciou este anúncio.");
    }
}
