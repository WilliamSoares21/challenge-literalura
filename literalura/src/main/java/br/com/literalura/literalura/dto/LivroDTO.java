package br.com.literalura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import br.com.literalura.literalura.infra.json.SummariesDeserializer;

/**
 * DTO de retorno para o frontend com todos os dados do livro.
 * 
 * Campo "imagem" é extraído automaticamente de formats.image/jpeg pelo mapper
 * para facilitar o consumo no frontend React/TypeScript.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LivroDTO(
    @JsonProperty("title")
    String titulo,
    
    @JsonProperty("authors")
    List<AutorDTO> autores,
    
    @JsonProperty("languages")
    List<String> idiomas,
    
    @JsonProperty("download_count")
    Integer numeroDownloads,
    
    @JsonProperty("formats")
    FormatsDTO formats,
    
    @JsonProperty("imagem")
    String imagem,
    
    @JsonProperty("summaries")
    @JsonDeserialize(using = SummariesDeserializer.class)
    List<String> summaries,
    
    @JsonProperty("subjects")
    List<String> subjects,
    
    String curiosidade
) {
}
