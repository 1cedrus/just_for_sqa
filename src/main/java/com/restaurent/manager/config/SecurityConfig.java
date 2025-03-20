package com.restaurent.manager.config;


import com.restaurent.manager.enums.RoleSystem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebSecurity
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig{
    private static final String[] PUBLIC_ENDPOINT = {"/api/identify/refresh-token/{token}","/api/package/view","/api/account/verify/otp","/websocket/**","/api/identify/employee/login","/api/account","/api/account/**","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html","/swagger-ui/index.html","/api/identify/*","/oauth2/**,/login/oauth2/code/google"};
    private static final String[] PRIVATE_ENDPOINT_ADMIN = {"/api/role/create","/api/permission/**", "/api/restaurant/admin/{restaurantId}"};
    private static final String[] RESTAURANT_MANAGER_ENDPOINT = {"/api/restaurant/init","/api/restaurant/manager/{accountId}","/api/restaurant/manager/payment/{accountId}", "/api/restaurant/{restaurantId}/pack/require-money", "/api/restaurant/{restaurant}/vat/{status}",
    "/api/restaurant/{restaurantId}/point"};
    private static final String[] EMPLOYEE_MANAGER_ENDPOINT = {"/api/employee/**"};
    @NonFinal
    @Value("${allow-origin}")
    private String allowOrigin;
    CustomJwtDecoder customJwtDecoder;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(PUBLIC_ENDPOINT).permitAll()
                        .requestMatchers(PRIVATE_ENDPOINT_ADMIN).hasRole(RoleSystem.ADMIN.name())
                        .requestMatchers(RESTAURANT_MANAGER_ENDPOINT).hasRole(RoleSystem.MANAGER.name())
                        .requestMatchers(EMPLOYEE_MANAGER_ENDPOINT).hasRole(RoleSystem.MANAGER.name())
                        .anyRequest().authenticated());
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())

        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }
    @Bean
    public WebMvcConfigurer corsConfig(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry){
                registry.addMapping("/**")
                        .allowedOrigins(allowOrigin)
                        .allowedMethods(HttpMethod.POST.name(),
                                HttpMethod.GET.name(),
                                HttpMethod.PUT.name(),
                                HttpMethod.DELETE.name())
                        .allowedHeaders(HttpHeaders.CONTENT_TYPE,
                                HttpHeaders.AUTHORIZATION);
            }
        };
    }
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}
