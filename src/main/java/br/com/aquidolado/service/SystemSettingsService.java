package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.SystemSetting;
import br.com.aquidolado.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    public static final String KEY_ADS_ENABLED = "adsEnabled";

    private final SystemSettingRepository repository;

    @Transactional(readOnly = true)
    public boolean isAdsEnabled() {
        return repository.findById(KEY_ADS_ENABLED)
                .map(s -> "true".equalsIgnoreCase(s.getValue()))
                .orElse(true);
    }

    @Transactional
    public void setAdsEnabled(boolean enabled) {
        SystemSetting setting = repository.findById(KEY_ADS_ENABLED)
                .orElse(new SystemSetting(KEY_ADS_ENABLED, "true"));
        setting.setValue(enabled ? "true" : "false");
        repository.save(setting);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminSettings() {
        Map<String, Object> map = new HashMap<>();
        map.put("adsEnabled", isAdsEnabled());
        return map;
    }

    @Transactional
    public Map<String, Object> patchAdminSettings(Map<String, Object> patch) {
        if (patch.containsKey("adsEnabled")) {
            Object v = patch.get("adsEnabled");
            if (v instanceof Boolean) {
                setAdsEnabled((Boolean) v);
            } else if (v != null) {
                setAdsEnabled("true".equalsIgnoreCase(v.toString()));
            }
        }
        return getAdminSettings();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPublicFeatures() {
        Map<String, Object> map = new HashMap<>();
        map.put("adsEnabled", isAdsEnabled());
        return map;
    }
}
