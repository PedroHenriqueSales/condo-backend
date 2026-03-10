package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {

    Optional<SystemSetting> findById(String key);
}
