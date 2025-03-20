package com.restaurent.manager.config;

import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.enums.RoleSystem;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.PackageRepository;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.repository.TableTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ApplicationConfig {
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    TableTypeRepository tableTypeRepository;
    @Bean
    ApplicationRunner applicationRunner(AccountRepository repository){
        return args -> {
//            if(tableTypeRepository.findByName("Bàn tròn").isEmpty()){
//
//            }
//            if(tableTypeRepository.findByName("Bàn vuông").isEmpty()){
//
//            }if(tableTypeRepository.findByName("Bàn chữ nhật").isEmpty()){
//
//            }
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
                        .password(passwordEncoder.encode("admin"))
                        .build();
                repository.save(user);
            }
        };
    }
}
