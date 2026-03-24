package br.com.literalura.literalura.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // Aplica a todos os endpoints
        .allowedOrigins("http://localhost:5173") // Origem permitida
        .allowedMethods("GET") // Métodos permitidos
        .allowedHeaders("*") // Permite todos os cabeçalhos
        .allowCredentials(true); // Permite envio de cookies/autenticação
  }
}
