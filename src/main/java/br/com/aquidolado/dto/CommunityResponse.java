package br.com.aquidolado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {

    private Long id;
    private String name;
    private String accessCode;
    private Boolean isPrivate;
    private String postalCode;
    private Instant createdAt;
    private Long createdById;
    /** Preenchido apenas no detalhe (GET /communities/{id}) */
    private String createdByName;
    /** Preenchido apenas no detalhe (GET /communities/{id}), nomes ordenados */
    private List<String> memberNames;
    /** Para o usuário autenticado: é administrador desta comunidade? */
    private Boolean isAdmin;
    /** No detalhe, lista de membros com id para "Tornar admin". Acessível a qualquer membro. */
    private List<MemberSummary> members;
    /** Ao entrar em comunidade privada: true indica que a solicitação foi enviada e está pendente de aprovação. */
    private Boolean joinPending;
    /** Quando o usuário é admin: ids dos administradores (para não mostrar "Tornar admin" para quem já é). */
    private List<Long> adminIds;
}
