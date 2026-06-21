package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;

import java.util.List;
import java.util.UUID;

public interface RepresentativePort {

    // === OPERAÇÕES DE COMANDO ===

    RepresentativeViewDTO create(UUID companyId, RepresentativeCreateCommandDTO command);

    RepresentativeViewDTO update(UUID id, RepresentativeUpdateCommandDTO command);

    RepresentativeViewDTO activate(UUID id);

    RepresentativeViewDTO deactivate(UUID id);

    void delete(UUID id);

    // === OPERAÇÕES DE CONSULTA ===

    RepresentativeViewDTO findById(UUID id);

    List<RepresentativeViewDTO> findAll();

    List<RepresentativeViewDTO> findByCompanyId(UUID companyId);

    RepresentativeViewDTO findActiveByCompanyId(UUID companyId);

    List<RepresentativeViewDTO> findAllActive();

    RepresentativeViewDTO findByCpf(String cpf);

    RepresentativeViewDTO findByEmail(String email);
}
