package br.com.comuminha.config;

import br.com.comuminha.domain.entity.Ad;
import br.com.comuminha.domain.entity.Community;
import br.com.comuminha.domain.entity.User;
import br.com.comuminha.domain.enums.AdType;
import br.com.comuminha.repository.AdRepository;
import br.com.comuminha.repository.CommunityRepository;
import br.com.comuminha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Seeder de dados APENAS para desenvolvimento local.
 * Cria um usuário, uma comunidade e alguns anúncios para facilitar o teste do frontend.
 *
 * Profile: dev
 */
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String SEED_EMAIL = "seed@comuminha.local";
    private static final String SEED_PASSWORD = "123456";
    private static final String SEED_ACCESS_CODE = "SEED2026";

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final AdRepository adRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedIfNeeded();
    }

    @Transactional
    public void seedIfNeeded() {
        // Se já existe o usuário seed, não recria
        User user = userRepository.findByEmail(SEED_EMAIL).orElse(null);
        if (user == null) {
            user = User.builder()
                    .name("Usuário Seed")
                    .email(SEED_EMAIL)
                    .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                    .whatsapp("+55 11 99999-9999")
                    .address("Apto 101")
                    .active(true)
                    .invitesRemaining(5)
                    .build();
            user = userRepository.save(user);
        }

        // Comunidade seed (reutiliza se já existe por access code)
        Community community = communityRepository.findByAccessCode(SEED_ACCESS_CODE).orElse(null);
        if (community == null) {
            community = Community.builder()
                    .name("Condomínio Seed (Dev)")
                    .accessCode(SEED_ACCESS_CODE)
                    .createdAt(Instant.now())
                    .createdBy(user)
                    .build();
            community = communityRepository.save(community);
        }

        // Garante membership (evita depender de equals/hashcode)
        if (!userRepository.existsByIdAndCommunitiesId(user.getId(), community.getId())) {
            user.getCommunities().add(community);
            userRepository.save(user);
        }

        // Se já existem anúncios do seed nessa comunidade, não duplica
        if (adRepository.count() > 0) {
            return;
        }

        Instant now = Instant.now();

        adRepository.save(Ad.builder()
                .title("Bicicleta aro 29 (pouco usada)")
                .description("Bicicleta em ótimo estado. Retirada no bloco A.")
                .type(AdType.SALE_TRADE)
                .price(new BigDecimal("850.00"))
                .user(user)
                .community(community)
                .createdAt(now.minusSeconds(3600))
                .build());

        adRepository.save(Ad.builder()
                .title("Aluguel de furadeira (por dia)")
                .description("Furadeira com brocas básicas. R$ 15/dia.")
                .type(AdType.RENT)
                .price(new BigDecimal("15.00"))
                .user(user)
                .community(community)
                .createdAt(now.minusSeconds(1800))
                .build());

        adRepository.save(Ad.builder()
                .title("Serviço: montagem de móveis")
                .description("Monto guarda-roupa, cama, estante. Valor a combinar.")
                .type(AdType.SERVICE)
                .price(null)
                .user(user)
                .community(community)
                .createdAt(now.minusSeconds(900))
                .build());
    }
}

