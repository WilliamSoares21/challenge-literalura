package br.com.literalura.literalura.dto;

import java.time.LocalDateTime;

public record ErrorDTO(
    String erro,
    String mensagem,
    LocalDateTime timestamp
) {
}
