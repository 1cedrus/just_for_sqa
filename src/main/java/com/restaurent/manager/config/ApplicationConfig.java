package com.restaurent.manager.config;

import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.enums.RoleSystem;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${application.account-default.password}")
    private String defaultPassword;

    @Bean
    ApplicationRunner applicationRunner(AccountRepository repository){
        return args -> {
            if(roleRepository.findByName(RoleSystem.ADMIN.name()).isEmpty()){
                roleRepository.save(Role.builder()
                        .name(RoleSystem.ADMIN.name())
                        .description("Admin of system")
                        .build());
            }
            if(repository.findByEmail("admin@gmail.com").isEmpty()){
                Account user = Account.builder()
                        .username("admin")
                        .phoneNumber("0357753844")
                        .email("admin@gmail.com")
                        .status(true)
                        .role(roleRepository.findByName(RoleSystem.ADMIN.name()).orElseThrow(
                                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED)
                        ))
                        .password(passwordEncoder.encode(defaultPassword))
                        .build();
                repository.save(user);
            }
        };
    }
}
