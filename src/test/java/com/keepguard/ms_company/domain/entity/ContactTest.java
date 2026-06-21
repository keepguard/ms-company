package com.keepguard.ms_company.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Contact
 * Testa apenas lógica de domínio, sem dependências de frameworks
 */
@DisplayName("Contact Domain Tests")
class ContactTest {
    
    private Contact contact;
    private UUID contactId;
    
    @BeforeEach
    void setUp() {
        contactId = UUID.randomUUID();
        contact = Contact.create(
            "João Silva",
            "joao.silva@empresa.com",
            "(11) 99999-9999",
            "https://www.empresa.com",
            "Gerente",
            "Vendas"
        );
    }
    
    @Test
    @DisplayName("Deve criar contato com dados válidos")
    void shouldCreateContactWithValidData() {
        assertNotNull(contact);
        assertEquals("João Silva", contact.getName());
        assertEquals("joao.silva@empresa.com", contact.getEmail());
        assertEquals("(11) 99999-9999", contact.getPhone());
        assertEquals("https://www.empresa.com", contact.getWebsite());
        assertEquals("Gerente", contact.getPosition());
        assertEquals("Vendas", contact.getDepartment());
        assertTrue(contact.isActive());
    }
    
    @Test
    @DisplayName("Deve criar contato com ID específico")
    void shouldCreateContactWithSpecificId() {
        Contact contactWithId = Contact.of(
            contactId, "João Silva", "joao.silva@empresa.com", 
            "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas", true
        );
        
        assertEquals(contactId, contactWithId.getId());
        assertEquals("João Silva", contactWithId.getName());
        assertTrue(contactWithId.isActive());
    }
    
    @Test
    @DisplayName("Deve ativar contato")
    void shouldActivateContact() {
        contact.deactivate();
        assertFalse(contact.isActive());
        
        contact.activate();
        assertTrue(contact.isActive());
    }
    
    @Test
    @DisplayName("Deve desativar contato")
    void shouldDeactivateContact() {
        assertTrue(contact.isActive());
        
        contact.deactivate();
        assertFalse(contact.isActive());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com nome nulo")
    void shouldThrowExceptionWhenCreatingContactWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create(null, "joao.silva@empresa.com", "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com nome vazio")
    void shouldThrowExceptionWhenCreatingContactWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create("   ", "joao.silva@empresa.com", "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com nome muito longo")
    void shouldThrowExceptionWhenCreatingContactWithLongName() {
        String longName = "a".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create(longName, "joao.silva@empresa.com", "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com email nulo")
    void shouldThrowExceptionWhenCreatingContactWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create("João Silva", null, "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com email inválido")
    void shouldThrowExceptionWhenCreatingContactWithInvalidEmail() {
        assertThrows(com.keepguard.lib_common.exception.InvalidEmailException.class, () -> {
            Contact.create("João Silva", "email-invalido", "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException quando email for nulo")
    void shouldThrowValidationExceptionWhenEmailIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Contact.create(
                "João Silva",
                null, // Email nulo
                "(11) 99999-9999",
                "https://www.empresa.com",
                "Gerente",
                "Vendas"
            )
        );
        
        assertEquals("Email é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando email for vazio")
    void shouldThrowValidationExceptionWhenEmailIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Contact.create(
                "João Silva",
                "", // Email vazio
                "(11) 99999-9999",
                "https://www.empresa.com",
                "Gerente",
                "Vendas"
            )
        );
        
        assertEquals("Email é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando telefone for nulo")
    void shouldThrowValidationExceptionWhenPhoneIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Contact.create(
                "João Silva",
                "joao.silva@empresa.com",
                null, // Telefone nulo
                "https://www.empresa.com",
                "Gerente",
                "Vendas"
            )
        );
        
        assertEquals("Telefone é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando telefone for vazio")
    void shouldThrowValidationExceptionWhenPhoneIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Contact.create(
                "João Silva",
                "joao.silva@empresa.com",
                "", // Telefone vazio
                "https://www.empresa.com",
                "Gerente",
                "Vendas"
            )
        );
        
        assertEquals("Telefone é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar contato com telefone nulo")
    void shouldThrowExceptionWhenCreatingContactWithNullPhone() {
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create("João Silva", "joao.silva@empresa.com", null, "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com telefone muito curto")
    void shouldThrowExceptionWhenCreatingContactWithShortPhone() {
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create("João Silva", "joao.silva@empresa.com", "123", "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar contato com telefone muito longo")
    void shouldThrowExceptionWhenCreatingContactWithLongPhone() {
        String longPhone = "1".repeat(16);
        assertThrows(IllegalArgumentException.class, () -> {
            Contact.create("João Silva", "joao.silva@empresa.com", longPhone, "https://www.empresa.com", "Gerente", "Vendas");
        });
    }
    
    @Test
    @DisplayName("Deve normalizar email para minúsculas")
    void shouldNormalizeEmailToLowerCase() {
        Contact contactWithUpperCaseEmail = Contact.create(
            "João Silva", "JOAO.SILVA@EMPRESA.COM", "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas"
        );
        
        assertEquals("joao.silva@empresa.com", contactWithUpperCaseEmail.getEmail());
    }
    
    @Test
    @DisplayName("Deve aceitar cargo nulo")
    void shouldAcceptNullPosition() {
        Contact contactWithoutPosition = Contact.create(
            "João Silva", "joao.silva@empresa.com", "(11) 99999-9999", "https://www.empresa.com", null, "Vendas"
        );
        
        assertNull(contactWithoutPosition.getPosition());
    }
    
    @Test
    @DisplayName("Deve aceitar departamento nulo")
    void shouldAcceptNullDepartment() {
        Contact contactWithoutDepartment = Contact.create(
            "João Silva", "joao.silva@empresa.com", "(11) 99999-9999", "https://www.empresa.com", "Gerente", null
        );
        
        assertNull(contactWithoutDepartment.getDepartment());
    }
    
    @Test
    @DisplayName("Deve considerar contatos iguais pelo ID")
    void shouldConsiderContactsEqualById() {
        Contact contact1 = Contact.of(contactId, "João Silva", "joao.silva@empresa.com", 
            "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas", true);
        Contact contact2 = Contact.of(contactId, "Maria Santos", "maria.santos@empresa.com", 
            "(11) 88888-8888", "https://www.empresa.com", "Analista", "TI", false);
        
        assertEquals(contact1, contact2);
        assertEquals(contact1.hashCode(), contact2.hashCode());
    }
    
    @Test
    @DisplayName("Deve ter toString informativo")
    void shouldHaveInformativeToString() {
        String toString = contact.toString();
        
        assertTrue(toString.contains("Contact"));
        assertTrue(toString.contains(contact.getId().toString()));
        assertTrue(toString.contains(contact.getName()));
        assertTrue(toString.contains(contact.getEmail()));
    }
}
