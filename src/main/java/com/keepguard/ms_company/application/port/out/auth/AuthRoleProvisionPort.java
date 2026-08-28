package com.keepguard.ms_company.application.port.out.auth;

import java.util.UUID;

public interface AuthRoleProvisionPort {
    void provisionCompanyRoles(UUID companyId);
}
