package com.keepguard.ms_company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a classe principal MsCompanyApplication
 * Teste simples para cobertura sem usar H2 ou carregar contexto completo
 */
@DisplayName("Ms Company Application Tests")
class MsCompanyApplicationTest {
    
    @Test
    @DisplayName("Deve verificar se a classe principal existe")
    void shouldVerifyMainApplicationClassExists() {
        // Given
        Class<?> applicationClass = MsCompanyApplication.class;
        
        // When & Then
        assertNotNull(applicationClass, "Classe MsCompanyApplication deve existir");
        assertEquals("MsCompanyApplication", applicationClass.getSimpleName(), "Nome da classe deve ser correto");
        assertEquals("com.keepguard.ms_company", applicationClass.getPackageName(), "Package deve ser correto");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal tem método main")
    void shouldVerifyMainMethodExists() {
        // Given
        Class<?> applicationClass = MsCompanyApplication.class;
        
        try {
            // When
            applicationClass.getDeclaredMethod("main", String[].class);
            
            // Then
            assertTrue(true, "Método main encontrado na classe principal");
        } catch (NoSuchMethodException e) {
            fail("Método main não encontrado na classe principal");
        }
    }
    
    @Test
    @DisplayName("Deve verificar anotações da classe principal")
    void shouldVerifyMainApplicationAnnotations() {
        // Given
        Class<?> applicationClass = MsCompanyApplication.class;
        
        // When & Then
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class), 
            "Classe deve ter anotação @SpringBootApplication");
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.data.jpa.repository.config.EnableJpaRepositories.class), 
            "Classe deve ter anotação @EnableJpaRepositories");
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.context.annotation.Import.class), 
            "Classe deve ter anotação @Import");
    }
    
    @Test
    @DisplayName("Deve verificar configurações do SpringBootApplication")
    void shouldVerifySpringBootApplicationConfiguration() {
        // Given
        org.springframework.boot.autoconfigure.SpringBootApplication annotation = 
            MsCompanyApplication.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        
        // When & Then
        assertNotNull(annotation, "Anotação @SpringBootApplication deve existir");
        
        String[] scanBasePackages = annotation.scanBasePackages();
        assertNotNull(scanBasePackages, "scanBasePackages deve estar configurado");
        assertEquals(2, scanBasePackages.length, "Deve ter 2 pacotes de scan");
        assertEquals("com.keepguard.ms_company", scanBasePackages[0], "Primeiro pacote deve ser ms_company");
        assertEquals("com.keepguard.lib_common", scanBasePackages[1], "Segundo pacote deve ser lib_common");
    }
    
    @Test
    @DisplayName("Deve verificar configurações do EnableJpaRepositories")
    void shouldVerifyEnableJpaRepositoriesConfiguration() {
        // Given
        org.springframework.data.jpa.repository.config.EnableJpaRepositories annotation = 
            MsCompanyApplication.class.getAnnotation(org.springframework.data.jpa.repository.config.EnableJpaRepositories.class);
        
        // When & Then
        assertNotNull(annotation, "Anotação @EnableJpaRepositories deve existir");
        
        String[] basePackages = annotation.basePackages();
        assertNotNull(basePackages, "basePackages deve estar configurado");
        assertEquals(1, basePackages.length, "Deve ter 1 pacote base");
        assertEquals("com.keepguard.ms_company.infrastructure.persistence.spring", basePackages[0], 
            "Pacote base deve ser o correto");
    }
    
    @Test
    @DisplayName("Deve verificar configurações do Import")
    void shouldVerifyImportConfiguration() {
        // Given
        org.springframework.context.annotation.Import annotation = 
            MsCompanyApplication.class.getAnnotation(org.springframework.context.annotation.Import.class);
        
        // When & Then
        assertNotNull(annotation, "Anotação @Import deve existir");
        
        Class<?>[] value = annotation.value();
        assertNotNull(value, "value deve estar configurado");
        assertEquals(1, value.length, "Deve ter 1 classe importada");
        assertEquals("MetricsConfig", value[0].getSimpleName(), "Classe importada deve ser MetricsConfig");
        assertEquals("com.keepguard.lib_common.config.MetricsConfig", value[0].getName(), 
            "Package da classe importada deve ser correto");
    }
    
    @Test
    @DisplayName("Deve verificar se o método main é estático e público")
    void shouldVerifyMainMethodIsStaticAndPublic() {
        // Given
        Class<?> applicationClass = MsCompanyApplication.class;
        
        try {
            // When
            java.lang.reflect.Method mainMethod = applicationClass.getDeclaredMethod("main", String[].class);
            
            // Then
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()), 
                "Método main deve ser estático");
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()), 
                "Método main deve ser público");
            assertEquals(String[].class, mainMethod.getParameterTypes()[0], 
                "Método main deve receber array de String");
            assertEquals(void.class, mainMethod.getReturnType(), 
                "Método main deve retornar void");
        } catch (NoSuchMethodException e) {
            fail("Método main não encontrado");
        }
    }
    
    @Test
    @DisplayName("Deve verificar se a classe pode ser carregada pelo ClassLoader")
    void shouldVerifyClassCanBeLoadedByClassLoader() {
        // Given
        String className = "com.keepguard.ms_company.MsCompanyApplication";
        
        // When
        Class<?> loadedClass = null;
        try {
            loadedClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            fail("Classe não pode ser carregada: " + e.getMessage());
        }
        
        // Then
        assertNotNull(loadedClass, "Classe deve ser carregada com sucesso");
        assertEquals(MsCompanyApplication.class, loadedClass, "Classe carregada deve ser a mesma");
    }
}
