package br.com.literalura.literalura.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.dto.ResultadoDTO;

@Component
public class GutendexClient {
    @Autowired
    private RestTemplate restTemplate;

    private static final String BASE_URL = "https://gutendex.com/books?search=";

    public Optional<LivroDTO> buscarLivroPorTitulo(String titulo) {
        try {
            String tituloCodificado = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            String url = BASE_URL + tituloCodificado;

            ResultadoDTO resultado = restTemplate.getForObject(url, ResultadoDTO.class);
            
            if (resultado != null && resultado.results() != null && !resultado.results().isEmpty()) {
                return Optional.of(resultado.results().get(0));
            }
        } catch (Exception e) {
            System.err.println("Erro técnico na comunicação com a API Gutendex: " + e.getMessage());
        }
        return Optional.empty();
    }
}