package com.keepguard.ms_company.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(
    name = "auth-service",
    url = "${AUTH_SERVICE_URL:http://localhost:8081}",
    configuration = AuthClientConfig.class
)
public interface AuthProvisionClient {

    @PostMapping("/api/v1/companies/{companyId}/roles/provision")
    void provisionCompanyRoles(@PathVariable("companyId") UUID companyId);
}
