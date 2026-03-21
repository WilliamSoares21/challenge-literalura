package br.com.literalura.literalura.dto;

import java.time.LocalDateTime;

public record ErrorDTO(String codigo, String mensagem, LocalDateTime timestamp) {
}
