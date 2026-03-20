package br.com.literalura.literalura.dto;

import jakarta.validation.constraints.NotBlank;

public record AutorDTO(
    String nome, 
    Integer anoNascimento,
    Integer anoFalecimento) {
}
