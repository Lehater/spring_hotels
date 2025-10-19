package com.example.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityWebFilterChain springSecurity(
      ServerHttpSecurity http,
      Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>>
          reactiveJwtAuthConverter,
      ReactiveJwtDecoder gatewayJwtDecoder) {

    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            ex -> ex.pathMatchers("/actuator/**").permitAll().anyExchange().authenticated())
        .oauth2ResourceServer(
            oauth ->
                oauth.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(reactiveJwtAuthConverter)
                            .jwtDecoder(gatewayJwtDecoder)))
        .build();
  }

  // ВАЖНО: ReactiveJwtDecoder (а не JwtDecoder)
  @Bean
  ReactiveJwtDecoder gatewayJwtDecoder(@Value("${auth.jwt.secret}") String secret) {
    SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> reactiveJwtAuthConverter() {
    var servletConv = new JwtAuthenticationConverter();
    servletConv.setJwtGrantedAuthoritiesConverter(
        jwt ->
            List.of(
                new SimpleGrantedAuthority(
                    (String) jwt.getClaims().getOrDefault("role", "ROLE_USER"))));
    return new ReactiveJwtAuthenticationConverterAdapter(servletConv);
  }
}
