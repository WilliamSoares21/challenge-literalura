package br.com.literalura.literalura.dto;

import java.util.List;

public record LivroDTO(
    String titulo,
    List<AutorDTO> autores,
    List<String> idiomas,
    Integer numeroDownloads) {
}
