package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.lib_common.utils.BrazilianValidationUtils;
import com.keepguard.lib_common.utils.ValidationUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Representative {

    private final UUID id;
    private final String name;
    private final String cpf;
    private final String rg;
    private final LocalDate birthDate;
    private final String email;
    private final String phone;
    private final String role;
    private boolean active;

    public Representative(UUID id, String name, String cpf, String rg, LocalDate birthDate,
                         String email, String phone, String role, boolean active) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = validateName(name);
        this.cpf = validateCpf(cpf);
        this.rg = validateRg(rg);
        this.birthDate = validateBirthDate(birthDate);
        this.email = validateEmail(email);
        this.phone = validatePhone(phone);
        this.role = validateRole(role);
        this.active = active;
    }

    public static Representative create(String name, String cpf, String rg, LocalDate birthDate,
                                      String email, String phone, String role) {
        return new Representative(null, name, cpf, rg, birthDate, email, phone, role, true);
    }

    public static Representative of(UUID id, String name, String cpf, String rg, LocalDate birthDate,
                                  String email, String phone, String role, boolean active) {
        return new Representative(id, name, cpf, rg, birthDate, email, phone, role, active);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do representante é obrigatório");
        }
        if (name.length() > 150) {
            throw new IllegalArgumentException("Nome deve ter no máximo 150 caracteres");
        }
        return name.trim();
    }

    private String validateCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new ValidationException("CPF é obrigatório");
        }
        String cleanCpf = cpf.replaceAll("\\D", "");
        BrazilianValidationUtils.validateCpf(cleanCpf);
        return cleanCpf;
    }

    private String validateRg(String rg) {
        if (rg == null || rg.trim().isEmpty()) {
            return null;
        }
        if (rg.length() > 15) {
            throw new IllegalArgumentException("RG deve ter no máximo 15 caracteres");
        }
        return rg.trim();
    }

    private LocalDate validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser futura");
        }
        if (birthDate.isBefore(LocalDate.now().minusYears(120))) {
            throw new IllegalArgumentException("Data de nascimento inválida");
        }
        return birthDate;
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email do representante é obrigatório");
        }
        String cleanEmail = email.trim().toLowerCase();
        ValidationUtils.validateEmail(cleanEmail);
        return cleanEmail;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Telefone do representante é obrigatório");
        }
        BrazilianValidationUtils.validatePhone(phone);
        return phone.trim();
    }

    private String validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        if (role.length() > 100) {
            throw new IllegalArgumentException("Cargo deve ter no máximo 100 caracteres");
        }
        return role.trim();
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCpf() { return cpf; }
    public String getRg() { return rg; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Representative that = (Representative) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Representative{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", rg='" + rg + '\'' +
                ", birthDate=" + birthDate +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
