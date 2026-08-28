package com.keepguard.ms_company;

import com.keepguard.lib_common.config.MetricsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.keepguard.ms_company", "com.keepguard.lib_common"})
@EnableJpaRepositories(basePackages = "com.keepguard.ms_company.infrastructure.persistence.spring")
@EnableFeignClients(basePackages = "com.keepguard.ms_company.adapters.out.feign")
@Import(MetricsConfig.class)
public class MsCompanyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCompanyApplication.class, args);
	}

}
