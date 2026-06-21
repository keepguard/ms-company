package com.keepguard.ms_company.test.builder;

import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeUpdateDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyRepresentativeJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder para criação de dados de teste para Representative
 * Facilita a criação de objetos de teste com dados padrão
 */
public class RepresentativeTestBuilder {
    
    private UUID id = UUID.randomUUID();
    private UUID companyId = UUID.randomUUID();
    private String name = "João Silva";
    private String cpf = "11144477735";
    private String rg = "123456789";
    private LocalDate birthDate = LocalDate.of(1990, 1, 1);
    private String email = "joao.silva@empresa.com";
    private String phone = "11999999999";
    private String role = "Diretor";
    private boolean active = true;
    
    public static RepresentativeTestBuilder builder() {
        return new RepresentativeTestBuilder();
    }
    
    public RepresentativeTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public RepresentativeTestBuilder withCompanyId(UUID companyId) {
        this.companyId = companyId;
        return this;
    }
    
    public RepresentativeTestBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public RepresentativeTestBuilder withCpf(String cpf) {
        this.cpf = cpf;
        return this;
    }
    
    public RepresentativeTestBuilder withRg(String rg) {
        this.rg = rg;
        return this;
    }
    
    public RepresentativeTestBuilder withBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        return this;
    }
    
    public RepresentativeTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public RepresentativeTestBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }
    
    public RepresentativeTestBuilder withRole(String role) {
        this.role = role;
        return this;
    }
    
    public RepresentativeTestBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }
    
    public RepresentativeTestBuilder inactive() {
        this.active = false;
        return this;
    }
    
    public RepresentativeTestBuilder withNullRg() {
        this.rg = null;
        return this;
    }
    
    public RepresentativeTestBuilder withNullRole() {
        this.role = null;
        return this;
    }
    
    public RepresentativeTestBuilder withCpfFormatted() {
        this.cpf = "11144477735";
        return this;
    }
    
    public RepresentativeTestBuilder withPhoneFormatted() {
        this.phone = "(11) 99999-9999";
        return this;
    }
    
    public RepresentativeTestBuilder withMariaSilva() {
        this.name = "Maria Silva";
        this.cpf = "98765432100";
        this.rg = "987654321";
        this.birthDate = LocalDate.of(1985, 5, 15);
        this.email = "maria.silva@empresa.com";
        this.phone = "11888888888";
        this.role = "Gerente";
        return this;
    }
    
    public RepresentativeTestBuilder withPedroSantos() {
        this.name = "Pedro Santos";
        this.cpf = "12345678909";
        this.rg = "111222333";
        this.birthDate = LocalDate.of(1992, 8, 20);
        this.email = "pedro.santos@empresa.com";
        this.phone = "11777777777";
        this.role = "Supervisor";
        return this;
    }
    
    public RepresentativeTestBuilder withAnaCosta() {
        this.name = "Ana Costa";
        this.cpf = "55566677720";
        this.rg = "555666777";
        this.birthDate = LocalDate.of(1988, 12, 10);
        this.email = "ana.costa@empresa.com";
        this.phone = "11666666666";
        this.role = "Coordenadora";
        return this;
    }
    
    public RepresentativeTestBuilder withCarlosOliveira() {
        this.name = "Carlos Oliveira";
        this.cpf = "99988877714";
        this.rg = "999888777";
        this.birthDate = LocalDate.of(1980, 3, 25);
        this.email = "carlos.oliveira@empresa.com";
        this.phone = "11555555555";
        this.role = "Presidente";
        return this;
    }
    
    public Representative buildDomain() {
        Representative representative = Representative.create(name, cpf, rg, birthDate, email, phone, role);
        if (!active) {
            representative.deactivate();
        }
        return representative;
    }
    
    public RepresentativeViewDTO buildView() {
        return new RepresentativeViewDTO(
            id,
            name,
            cpf,
            rg,
            birthDate,
            email,
            phone,
            role,
            active,
            null,
            null
        );
    }
    
    public RepresentativeCreateCommandDTO buildCreateCommand() {
        return new RepresentativeCreateCommandDTO(
            name,
            cpf,
            rg,
            birthDate,
            email,
            phone,
            role,
            companyId
        );
    }
    
    public RepresentativeUpdateCommandDTO buildUpdateCommand() {
        return new RepresentativeUpdateCommandDTO(
            name,
            cpf,
            rg,
            birthDate,
            email,
            phone,
            role
        );
    }
    
    public RepresentativeCreateDTO buildCreateDTO() {
        RepresentativeCreateDTO dto = new RepresentativeCreateDTO();
        dto.setName(name);
        dto.setCpf(cpf);
        dto.setRg(rg);
        dto.setBirthDate(birthDate);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setRole(role);
        return dto;
    }
    
    public RepresentativeUpdateDTO buildUpdateDTO() {
        RepresentativeUpdateDTO dto = new RepresentativeUpdateDTO();
        dto.setName(name);
        dto.setCpf(cpf);
        dto.setRg(rg);
        dto.setBirthDate(birthDate);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setRole(role);
        return dto;
    }
    
    public RepresentativeResponseDTO buildResponseDTO() {
        return RepresentativeResponseDTO.builder()
            .id(id)
            .name(name)
            .cpf(cpf)
            .rg(rg)
            .birthDate(birthDate)
            .email(email)
            .phone(phone)
            .role(role)
            .active(active)
            .build();
    }
    
    public CompanyRepresentativeJpaEntity buildJpaEntity(CompanyJpaEntity company) {
        return CompanyRepresentativeJpaEntity.builder()
            .id(id)
            .company(company)
            .name(name)
            .cpf(cpf)
            .rg(rg)
            .birthDate(birthDate)
            .email(email)
            .phone(phone)
            .role(role)
            .active(active)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    // Métodos estáticos para casos comuns
    public static Representative createDefaultRepresentative() {
        return builder().buildDomain();
    }
    
    public static RepresentativeViewDTO createDefaultRepresentativeViewDTO() {
        return builder().buildView();
    }
    
    public static RepresentativeCreateCommandDTO createDefaultCreateCommand() {
        return builder().buildCreateCommand();
    }
    
    public static RepresentativeUpdateCommandDTO createDefaultUpdateCommand() {
        return builder().buildUpdateCommand();
    }
    
    public static RepresentativeCreateDTO createDefaultCreateDTO() {
        return builder().buildCreateDTO();
    }
    
    public static RepresentativeUpdateDTO createDefaultUpdateDTO() {
        return builder().buildUpdateDTO();
    }
    
    public static RepresentativeResponseDTO createDefaultResponseDTO() {
        return builder().buildResponseDTO();
    }
    
    public static Representative createInactiveRepresentative() {
        return builder().inactive().buildDomain();
    }
    
    public static RepresentativeViewDTO createInactiveRepresentativeViewDTO() {
        return builder().inactive().buildView();
    }
    
    public static Representative createRepresentativeWithNullRg() {
        return builder().withNullRg().buildDomain();
    }
    
    public static RepresentativeViewDTO createRepresentativeViewDTOWithNullRg() {
        return builder().withNullRg().buildView();
    }
    
    public static Representative createRepresentativeWithNullRole() {
        return builder().withNullRole().buildDomain();
    }
    
    public static RepresentativeViewDTO createRepresentativeViewDTOWithNullRole() {
        return builder().withNullRole().buildView();
    }
    
    public static Representative createRepresentativeWithFormattedCpf() {
        return builder().withCpfFormatted().buildDomain();
    }
    
    public static Representative createRepresentativeWithFormattedPhone() {
        return builder().withPhoneFormatted().buildDomain();
    }
    
    public static Representative createMariaSilvaRepresentative() {
        return builder().withMariaSilva().buildDomain();
    }
    
    public static RepresentativeViewDTO createMariaSilvaRepresentativeViewDTO() {
        return builder().withMariaSilva().buildView();
    }
    
    public static Representative createPedroSantosRepresentative() {
        return builder().withPedroSantos().buildDomain();
    }
    
    public static RepresentativeViewDTO createPedroSantosRepresentativeViewDTO() {
        return builder().withPedroSantos().buildView();
    }
    
    public static Representative createAnaCostaRepresentative() {
        return builder().withAnaCosta().buildDomain();
    }
    
    public static RepresentativeViewDTO createAnaCostaRepresentativeViewDTO() {
        return builder().withAnaCosta().buildView();
    }
    
    public static Representative createCarlosOliveiraRepresentative() {
        return builder().withCarlosOliveira().buildDomain();
    }
    
    public static RepresentativeViewDTO createCarlosOliveiraRepresentativeViewDTO() {
        return builder().withCarlosOliveira().buildView();
    }
}
