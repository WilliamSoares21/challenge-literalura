package br.com.literalura.literalura.dto;

public record EstatisticasDownloadsDTO(
    Double media,
    Double maximo,
    Double minimo,
    Double total
) {
}
