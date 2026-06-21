package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.domain.entity.Contact;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.domain.entity.Cnae;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyAddressJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyContactJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyRepresentativeJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyBankAccountJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyCnaeJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CompanyJpaMapper {

    private final CnaeJpaMapper cnaeJpaMapper;

    public CompanyJpaMapper(CnaeJpaMapper cnaeJpaMapper) {
        this.cnaeJpaMapper = cnaeJpaMapper;
    }

    public CompanyJpaEntity toEntity(Company company) {
        if (company == null) {
            return null;
        }

        CompanyJpaEntity entity = CompanyJpaEntity.builder()
                .id(company.getId())
                .codeCompany(company.getCodeCompany())
                .xApplication(company.getXApplication())
                .name(company.getName())
                .legalName(company.getLegalName())
                .cnpj(company.getCnpj())
                .stateRegistration(company.getStateRegistration())
                .municipalRegistration(company.getMunicipalRegistration())
                .addresses(new ArrayList<>())
                .contacts(new ArrayList<>())
                .representatives(new ArrayList<>())
                .bankAccounts(new ArrayList<>())
                .cnaes(new ArrayList<>())
                .taxRegime(company.getTaxRegime())
                .ein(company.getEin())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();

        // Mapear endereços
        company.getAddresses().forEach(address -> {
            CompanyAddressJpaEntity addressEntity = mapAddressToJpa(address);
            addressEntity.setCompany(entity);
            entity.getAddresses().add(addressEntity);
        });

        // Mapear contatos
        company.getContacts().forEach(contact -> {
            CompanyContactJpaEntity contactEntity = mapContactToJpa(contact);
            contactEntity.setCompany(entity);
            entity.getContacts().add(contactEntity);
        });

        // Mapear representantes
        company.getRepresentatives().forEach(representative -> {
            CompanyRepresentativeJpaEntity representativeEntity = mapRepresentativeToJpa(representative);
            representativeEntity.setCompany(entity);
            entity.getRepresentatives().add(representativeEntity);
        });

        // Mapear contas bancárias
        company.getBankAccounts().forEach(bankAccount -> {
            CompanyBankAccountJpaEntity bankAccountEntity = mapBankAccountToJpa(bankAccount);
            bankAccountEntity.setCompany(entity);
            entity.getBankAccounts().add(bankAccountEntity);
        });

        // Mapear CNAEs
        company.getCnaes().forEach(cnae -> {
            CompanyCnaeJpaEntity cnaeEntity = cnaeJpaMapper.toEntity(cnae, company.getId());
            cnaeEntity.setCompany(entity);
            entity.getCnaes().add(cnaeEntity);
        });

        return entity;
    }

    public Company toDomain(CompanyJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Company company = Company.of(
                entity.getId(),
                entity.getCodeCompany(),
                entity.getXApplication(),
                entity.getName(),
                entity.getLegalName(),
                entity.getCnpj(),
                entity.getStateRegistration(),
                entity.getMunicipalRegistration(),
                entity.getTaxRegime(),
                entity.getEin(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

        // Mapear endereços
        if (entity.getAddresses() != null) {
            entity.getAddresses().forEach(addressEntity -> {
                Address address = mapAddressFromJpa(addressEntity);
                company.addAddress(address);
            });
        }

        // Mapear contatos
        if (entity.getContacts() != null) {
            entity.getContacts().forEach(contactEntity -> {
                Contact contact = mapContactFromJpa(contactEntity);
                company.addContact(contact);
            });
        }

        // Mapear representantes
        if (entity.getRepresentatives() != null) {
            entity.getRepresentatives().forEach(representativeEntity -> {
                Representative representative = mapRepresentativeFromJpa(representativeEntity);
                company.addRepresentative(representative);
            });
        }

        // Mapear contas bancárias
        if (entity.getBankAccounts() != null) {
            entity.getBankAccounts().forEach(bankAccountEntity -> {
                BankAccount bankAccount = mapBankAccountFromJpa(bankAccountEntity);
                company.addBankAccount(bankAccount);
            });
        }

        // Mapear CNAEs
        if (entity.getCnaes() != null) {
            entity.getCnaes().forEach(cnaeEntity -> {
                Cnae cnae = cnaeJpaMapper.toDomain(cnaeEntity);
                company.addCnae(cnae);
            });
        }

        return company;
    }

    private CompanyAddressJpaEntity mapAddressToJpa(Address address) {
        if (address == null) {
            return null;
        }

        return CompanyAddressJpaEntity.builder()
                .id(address.getId())
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .district(address.getDistrict())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .active(address.isActive())
                .build();
    }

    private Address mapAddressFromJpa(CompanyAddressJpaEntity addressJpa) {
        if (addressJpa == null) {
            return null;
        }

        return Address.of(
                addressJpa.getId(),
                addressJpa.getStreet(),
                addressJpa.getNumber(),
                addressJpa.getComplement(),
                addressJpa.getDistrict(),
                addressJpa.getCity(),
                addressJpa.getState(),
                addressJpa.getCountry(),
                addressJpa.getZipCode(),
                addressJpa.getActive()
        );
    }

    private CompanyContactJpaEntity mapContactToJpa(Contact contact) {
        if (contact == null) {
            return null;
        }

        return CompanyContactJpaEntity.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .website(contact.getWebsite())
                .position(contact.getPosition())
                .department(contact.getDepartment())
                .active(contact.isActive())
                .build();
    }

    private Contact mapContactFromJpa(CompanyContactJpaEntity contactJpa) {
        if (contactJpa == null) {
            return null;
        }

        return Contact.of(
                contactJpa.getId(),
                contactJpa.getName(),
                contactJpa.getEmail(),
                contactJpa.getPhone(),
                contactJpa.getWebsite(),
                contactJpa.getPosition(),
                contactJpa.getDepartment(),
                contactJpa.getActive()
        );
    }

    private CompanyRepresentativeJpaEntity mapRepresentativeToJpa(Representative representative) {
        if (representative == null) {
            return null;
        }

        return CompanyRepresentativeJpaEntity.builder()
                .id(representative.getId())
                .name(representative.getName())
                .cpf(representative.getCpf())
                .rg(representative.getRg())
                .birthDate(representative.getBirthDate())
                .email(representative.getEmail())
                .phone(representative.getPhone())
                .role(representative.getRole())
                .active(representative.isActive())
                .build();
    }

    private Representative mapRepresentativeFromJpa(CompanyRepresentativeJpaEntity representativeJpa) {
        if (representativeJpa == null) {
            return null;
        }

        return Representative.of(
                representativeJpa.getId(),
                representativeJpa.getName(),
                representativeJpa.getCpf(),
                representativeJpa.getRg(),
                representativeJpa.getBirthDate(),
                representativeJpa.getEmail(),
                representativeJpa.getPhone(),
                representativeJpa.getRole(),
                representativeJpa.getActive()
        );
    }

    private CompanyBankAccountJpaEntity mapBankAccountToJpa(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        return CompanyBankAccountJpaEntity.builder()
                .id(bankAccount.getId())
                .code(bankAccount.getCode())
                .agency(bankAccount.getAgency())
                .agencyDigit(bankAccount.getAgencyDigit())
                .accountNumber(bankAccount.getAccountNumber())
                .accountDigit(bankAccount.getAccountDigit())
                .accountType(bankAccount.getAccountType())
                .active(bankAccount.isActive())
                .build();
    }

    private BankAccount mapBankAccountFromJpa(CompanyBankAccountJpaEntity bankAccountJpa) {
        if (bankAccountJpa == null) {
            return null;
        }

        return BankAccount.of(
                bankAccountJpa.getId(),
                bankAccountJpa.getCode(),
                bankAccountJpa.getAgency(),
                bankAccountJpa.getAgencyDigit(),
                bankAccountJpa.getAccountNumber(),
                bankAccountJpa.getAccountDigit(),
                bankAccountJpa.getAccountType(),
                bankAccountJpa.getActive()
        );
    }

    public CompanyJpaEntity toJpaEntity(Company company) {
        return toEntity(company);
    }
}