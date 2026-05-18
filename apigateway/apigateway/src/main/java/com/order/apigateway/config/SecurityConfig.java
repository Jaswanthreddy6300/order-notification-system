package com.order.apigateway.config;

import org.apache.http.protocol.HTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
                serverHttpSecurity
                                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers(HttpMethod.OPTIONS).permitAll() // ← allow preflight
                                                .pathMatchers(HttpMethod.GET).authenticated()
                                                .pathMatchers(HttpMethod.POST).authenticated()
                                                .pathMatchers(HttpMethod.PUT).authenticated()
                                                .pathMatchers(HttpMethod.DELETE).authenticated()
                                                .anyExchange().authenticated())
                                .oauth2ResourceServer(
                                                oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                                                                .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(
                                                                                grantedAuthoritiesExtractor())))
                                .csrf(csrfSpec -> csrfSpec.disable());

                return serverHttpSecurity.build();
        }

        @Bean
        public org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource() {
                org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                config.addAllowedOrigin("http://localhost:4200");
                config.addAllowedOrigin("https://microvault-sfb.vercel.app"); // ← add this
                config.addAllowedMethod("*");
                config.addAllowedHeader("*");
                config.setAllowCredentials(true);

                org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
                return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
        }
}