package com.keepguard.ms_company.adapters.out.feign;

import com.keepguard.ms_company.application.port.out.auth.AuthRoleProvisionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRoleProvisionAdapter implements AuthRoleProvisionPort {

    private final AuthProvisionClient authProvisionClient;

    @Override
    public void provisionCompanyRoles(UUID companyId) {
        log.info("Provisionando roles no ms-auth para company {}", companyId);
        authProvisionClient.provisionCompanyRoles(companyId);
    }
}
