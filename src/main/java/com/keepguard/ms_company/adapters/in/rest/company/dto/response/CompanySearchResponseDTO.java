package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import java.util.List;

public record CompanySearchResponseDTO(
    List<CompanyResponseDTO> companies,
    int totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
}
