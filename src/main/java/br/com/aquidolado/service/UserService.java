package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.dto.UpdateProfileRequest;
import br.com.aquidolado.dto.UserProfileResponse;
import br.com.aquidolado.repository.UserRepository;
import br.com.aquidolado.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .whatsapp(user.getWhatsapp())
                .address(user.getAddress())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.setName(request.getName().trim());
        user.setWhatsapp(PhoneUtil.normalize(request.getWhatsapp()));
        user.setAddress(request.getAddress() != null && !request.getAddress().isBlank()
                ? request.getAddress().trim()
                : null);

        user = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .whatsapp(user.getWhatsapp())
                .address(user.getAddress())
                .build();
    }
}
