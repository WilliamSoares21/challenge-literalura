package br.com.literalura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AutorDTO(
    @JsonProperty("name")
    String nome,
    
    @JsonProperty("birth_year")
    Integer anoNascimento,
    
    @JsonProperty("death_year")
    Integer anoFalecimento) {
}
