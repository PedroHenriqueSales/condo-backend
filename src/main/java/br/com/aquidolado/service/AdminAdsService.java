package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.dto.admin.AdminAdListItem;
import br.com.aquidolado.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAdsService {

    private final AdRepository adRepository;

    @Transactional(readOnly = true)
    public Page<AdminAdListItem> listAds(Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable p = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return adRepository.findAll(p).map(this::toListItem);
    }

    private AdminAdListItem toListItem(Ad a) {
        return AdminAdListItem.builder()
                .id(a.getId())
                .title(a.getTitle())
                .type(a.getType())
                .status(a.getStatus())
                .userId(a.getUser() != null ? a.getUser().getId() : null)
                .userName(a.getUser() != null ? a.getUser().getName() : null)
                .communityId(a.getCommunity() != null ? a.getCommunity().getId() : null)
                .communityName(a.getCommunity() != null ? a.getCommunity().getName() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
