package br.com.literalura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LivroDTO(
    @NotBlank 
    @JsonProperty("title") 
    String titulo,
    
    @NotNull 
    @JsonProperty("authors") 
    List<AutorDTO> autores,
    
    @NotNull 
    @JsonProperty("languages") 
    List<String> idiomas,
    
    @JsonProperty("download_count") 
    Integer numeroDownloads,
    
    String curiosidade
) {
}
