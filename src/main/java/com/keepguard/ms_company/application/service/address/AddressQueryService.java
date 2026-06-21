package com.keepguard.ms_company.application.service.address;

import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
import com.keepguard.ms_company.application.mapper.AddressApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.AddressRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.QueryOperationException;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.application.port.out.cache.AddressCachePort;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AddressQueryService {

    private final AddressRepositoryPort addressRepository;
    private final AddressApplicationMapper addressMapper;
    private final AddressCachePort addressCachePort;
    private final MetricsPort metricsPort;

    public AddressViewDTO getById(UUID id) {
        try {
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("address_not_found_total",
                        Map.of("entity_id", id.toString(), "operation", "get_by_id"));
                    return new NotFoundException("Endereço não encontrado: " + id, "ADDRESS_NOT_FOUND", Map.of("addressId", id));
                });

            metricsPort.incrementCounter("address_queries_total",
                Map.of("query_type", "GET_BY_ID", "status", "SUCCESS"));

            // Busca o companyId do endereço
            UUID companyId = findCompanyIdByAddressId(id);

            return addressMapper.toViewDTO(address, companyId);

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar endereço por ID: {} - Erro: {}", id, e.getMessage(), e);
            metricsPort.incrementCounter("address_system_errors_total",
                Map.of("error_type", "GET_ADDRESS_BY_ID_ERROR", "operation", "get_by_id"));
            throw new QueryOperationException("Falha ao buscar endereço", "getById", "ADDRESS_QUERY_ERROR", Map.of("addressId", id), e);
        }
    }

    public List<AddressViewDTO> listByCompanyId(UUID companyId) {

        try {
            // Tentar buscar no cache primeiro
            List<AddressViewDTO> cachedAddresses = addressCachePort.getAddressesByCompanyIdFromCache(companyId.toString());
            if (cachedAddresses != null) {
                metricsPort.incrementCounter("address_queries_total",
                    Map.of("query_type", "LIST_BY_COMPANY", "status", "CACHE_HIT", "count", String.valueOf(cachedAddresses.size())));
                return cachedAddresses;
            }

            // Se não encontrou no cache, buscar no banco
            List<Address> addresses = addressRepository.findByCompanyId(companyId);
            List<AddressViewDTO> views = addresses.stream()
                .map(address -> addressMapper.toViewDTO(address, companyId))
                .toList();

            // Cachear o resultado
            addressCachePort.cacheAddressesByCompanyId(companyId.toString(), views);

            metricsPort.incrementCounter("address_queries_total",
                Map.of("query_type", "LIST_BY_COMPANY", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar endereços da empresa: {} - Erro: {}", companyId, e.getMessage(), e);
            metricsPort.incrementCounter("address_system_errors_total",
                Map.of("error_type", "LIST_ADDRESSES_BY_COMPANY_ERROR", "operation", "list_by_company"));
            throw new QueryOperationException("Falha ao listar endereços da empresa", "listByCompanyId", "ADDRESS_QUERY_ERROR", Map.of("companyId", companyId), e);
        }
    }

    public AddressViewDTO getActiveByCompanyId(UUID companyId) {

        try {
            // Tentar buscar no cache primeiro
            AddressViewDTO cachedAddress = addressCachePort.getActiveAddressByCompanyIdFromCache(companyId.toString());
            if (cachedAddress != null) {
                metricsPort.incrementCounter("address_queries_total",
                    Map.of("query_type", "GET_ACTIVE_BY_COMPANY", "status", "CACHE_HIT"));
                return cachedAddress;
            }

            // Se não encontrou no cache, buscar no banco
            Address address = addressRepository.findActiveByCompanyId(companyId)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("address_not_found_total",
                        Map.of("company_id", companyId.toString(), "operation", "get_active_by_company"));
                    return new NotFoundException("Endereço ativo não encontrado para a empresa: " + companyId, "ADDRESS_NOT_FOUND", Map.of("companyId", companyId));
                });

            AddressViewDTO addressView = addressMapper.toViewDTO(address, companyId);

            // Cachear o resultado
            addressCachePort.cacheActiveAddressByCompanyId(companyId.toString(), addressView);

            metricsPort.incrementCounter("address_queries_total",
                Map.of("query_type", "GET_ACTIVE_BY_COMPANY", "status", "SUCCESS"));

            return addressView;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar endereço ativo da empresa: {} - Erro: {}", companyId, e.getMessage(), e);
            metricsPort.incrementCounter("address_system_errors_total",
                Map.of("error_type", "GET_ACTIVE_ADDRESS_BY_COMPANY_ERROR", "operation", "get_active_by_company"));
            throw new QueryOperationException("Falha ao buscar endereço ativo da empresa", "getActiveByCompanyId", "ADDRESS_QUERY_ERROR", Map.of("companyId", companyId), e);
        }
    }

    public List<AddressViewDTO> listAll() {

        try {
            List<Address> addresses = addressRepository.findAll();
            List<AddressViewDTO> views = addresses.stream()
                .map(address -> {
                    UUID companyId = findCompanyIdByAddressId(address.getId());
                    return addressMapper.toViewDTO(address, companyId);
                })
                .toList();

            metricsPort.incrementCounter("address_queries_total",
                Map.of("query_type", "LIST_ALL", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar todos os endereços - Erro: {}", e.getMessage(), e);
            metricsPort.incrementCounter("address_system_errors_total",
                Map.of("error_type", "LIST_ALL_ADDRESSES_ERROR", "operation", "list_all"));
            throw new QueryOperationException("Falha ao listar todos os endereços", "listAll", "ADDRESS_QUERY_ERROR", Map.of(), e);
        }
    }

    public PageResultDTO<AddressViewDTO> search(AddressSearchCriteriaDTO criteria) {

        try {
            PageResultDTO<Address> addresses = addressRepository.search(criteria);
            PageResultDTO<AddressViewDTO> views = new PageResultDTO<>(
                addresses.items().stream()
                    .map(address -> {
                        UUID companyId = findCompanyIdByAddressId(address.getId());
                        return addressMapper.toViewDTO(address, companyId);
                    })
                    .toList(),
                addresses.total(),
                addresses.page(),
                addresses.size()
            );

            metricsPort.incrementCounter("address_queries_total",
                Map.of("query_type", "SEARCH", "status", "SUCCESS", "count", String.valueOf(views.total())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao buscar endereços com critérios - Erro: {}", e.getMessage(), e);
            metricsPort.incrementCounter("address_system_errors_total",
                Map.of("error_type", "SEARCH_ADDRESSES_ERROR", "operation", "search"));
            throw new QueryOperationException("Falha ao buscar endereços com critérios", "search", "ADDRESS_QUERY_ERROR", Map.of("criteria", criteria), e);
        }
    }

    private UUID findCompanyIdByAddressId(UUID addressId) {
        // Busca o companyId através do repository
        return addressRepository.findCompanyIdByAddressId(addressId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para o endereço: " + addressId, "COMPANY_NOT_FOUND", Map.of("addressId", addressId)));
    }
}
