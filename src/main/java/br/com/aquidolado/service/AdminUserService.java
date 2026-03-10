package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.dto.admin.AdminUserDetailResponse;
import br.com.aquidolado.dto.admin.AdminUserListItem;
import br.com.aquidolado.dto.admin.AdminUserPatchRequest;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminAuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<AdminUserListItem> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return AdminUserDetailResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .whatsapp(u.getWhatsapp())
                .active(Boolean.TRUE.equals(u.getActive()))
                .emailVerified(Boolean.TRUE.equals(u.getEmailVerified()))
                .systemAdmin(Boolean.TRUE.equals(u.getSystemAdmin()))
                .termsAcceptedAt(u.getTermsAcceptedAt())
                .communityIds(u.getCommunities().stream().map(c -> c.getId()).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public AdminUserDetailResponse patchUser(Long id, AdminUserPatchRequest request, Long adminUserId) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (request.getActive() != null) {
            u.setActive(request.getActive());
        }
        if (request.getEmailVerified() != null) {
            u.setEmailVerified(request.getEmailVerified());
        }
        u = userRepository.save(u);
        auditLogService.log(adminUserId, "PATCH_USER", "User", String.valueOf(id),
                "active=" + request.getActive() + ",emailVerified=" + request.getEmailVerified());
        return getUser(u.getId());
    }

    private AdminUserListItem toListItem(User u) {
        return AdminUserListItem.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .active(Boolean.TRUE.equals(u.getActive()))
                .emailVerified(Boolean.TRUE.equals(u.getEmailVerified()))
                .termsAcceptedAt(u.getTermsAcceptedAt())
                .build();
    }
}
