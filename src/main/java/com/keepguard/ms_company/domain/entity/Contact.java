package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.lib_common.utils.BrazilianValidationUtils;
import com.keepguard.lib_common.utils.ValidationUtils;

import java.util.Objects;
import java.util.UUID;

public final class Contact {

    private final UUID id;
    private final String name;
    private final String email;
    private final String phone;
    private final String website;
    private final String position;
    private final String department;
    private boolean active;

    public Contact(UUID id, String name, String email, String phone, String website, String position,
                   String department, boolean active) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.phone = validatePhone(phone);
        this.website = validateWebsite(website);
        this.position = validatePosition(position);
        this.department = validateDepartment(department);
        this.active = active;
    }

    public static Contact create(String name, String email, String phone, String website, String position,
                                String department) {
        return new Contact(null, name, email, phone, website, position, department, true);
    }

    public static Contact of(UUID id, String name, String email, String phone, String website, String position,
                            String department, boolean active) {
        return new Contact(id, name, email, phone, website, position, department, active);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Nome deve ter no máximo 100 caracteres");
        }
        return name.trim();
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }
        String cleanEmail = email.trim().toLowerCase();
        ValidationUtils.validateEmail(cleanEmail);
        return cleanEmail;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Telefone é obrigatório");
        }
        BrazilianValidationUtils.validatePhone(phone);
        return phone.trim();
    }

    private String validateWebsite(String website) {
        if (website != null && website.length() > 150) {
            throw new IllegalArgumentException("Website deve ter no máximo 150 caracteres");
        }
        return website != null ? website.trim() : null;
    }

    private String validatePosition(String position) {
        if (position != null && position.length() > 100) {
            throw new IllegalArgumentException("Cargo deve ter no máximo 100 caracteres");
        }
        return position != null ? position.trim() : null;
    }

    private String validateDepartment(String department) {
        if (department != null && department.length() > 100) {
            throw new IllegalArgumentException("Departamento deve ter no máximo 100 caracteres");
        }
        return department != null ? department.trim() : null;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getPosition() {
        return position;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(id, contact.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", active=" + active +
                '}';
    }
}