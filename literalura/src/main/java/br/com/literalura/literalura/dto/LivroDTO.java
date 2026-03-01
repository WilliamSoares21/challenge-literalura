package br.com.literalura.literalura.dto;

import java.util.List;

public record LivroDTO(
    String title,
    List<AutorDTO> authors,
    List<String> languages,
    int download_count) {
}
