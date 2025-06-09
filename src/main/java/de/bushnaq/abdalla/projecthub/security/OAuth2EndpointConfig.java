/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Security configuration specifically for OAuth2 endpoints.
 * This configuration ensures that the OAuth2 endpoints are accessible without authentication.
 * It runs with a higher precedence (lower order number) than other security configurations.
 */
@EnableWebSecurity
@Configuration
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.keycloak.client-id")
public class OAuth2EndpointConfig {

    /**
     * Configure OAuth2 endpoints to be publicly accessible.
     * This is necessary for the OAuth2 login flow to work properly.
     */
//    @Bean
//    @Order(1) // Higher precedence than other security configurations
//    public SecurityFilterChain oauth2EndpointSecurity(HttpSecurity http) throws Exception {
//        return http
//                .securityMatcher("/oauth2/**", "/login/oauth2/**")
//                .authorizeHttpRequests(authorize -> authorize
//                        .anyRequest().permitAll()
//                )
//                .csrf(csrf -> csrf.disable())
//                .build();
//    }
}
