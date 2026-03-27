package br.com.literalura.literalura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
  
  /**
   * Configura um RestTemplate padrão.
   * 
   * A deserialização robusta é garantida pelas anotações
   * @JsonIgnoreProperties nas DTOs, que ignoram campos extras
   * retornados pela API Gutendex.
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
