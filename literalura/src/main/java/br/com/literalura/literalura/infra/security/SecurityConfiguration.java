package br.com.literalura.literalura.infra.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable()) // Desabilita CSRF (necessário para APIs REST)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/livros/**").permitAll() // Permite acesso público
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll() // Swagger
            .anyRequest().authenticated());

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // React + Vite 
    config.setAllowedMethods(Arrays.asList("GET", "OPTIONS")); // OPTIONS necessário para preflight
    config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization")); // Headers explícitos
    config.setExposedHeaders(Arrays.asList("X-Total-Count")); // Headers que o frontend pode ler
    config.setMaxAge(3600L); // Cache do CORS por 1 hora
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
