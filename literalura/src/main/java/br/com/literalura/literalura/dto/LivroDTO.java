package br.com.literalura.literalura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LivroDTO(
    @NotBlank String title,
    @NotNull List<AutorDTO> authors,
    @NotNull List<String> languages,
    int download_count) {
}
