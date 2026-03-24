package br.com.literalura.literalura.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.literalura.literalura.dto.ErrorDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(LivroNaoEncontradoException.class)
  public ResponseEntity<ErrorDTO> handleLivroNaoEncontrado(LivroNaoEncontradoException e) {
    // Log completo para debug (apenas no servidor)
    log.error("❌ Livro não encontrado: {}", e.getMessage());
    
    // Resposta genérica ao cliente (sem expor detalhes internos)
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorDTO(
            "LIVRO_NAO_ENCONTRADO",
            "O livro solicitado não foi encontrado",
            LocalDateTime.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDTO> handleValidation(
      MethodArgumentNotValidException ex) {

    // Log detalhado no servidor
    String detalhesCompletos = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("⚠️ Validação falhou: {}", detalhesCompletos);

    // Resposta sanitizada ao cliente
    String detalhes = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .collect(Collectors.joining(", "));

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorDTO(
            "VALIDATION_ERROR",
            detalhes,
            LocalDateTime.now()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDTO> handleGenericError(Exception e) {
    // Log detalhado no servidor para debug
    log.error("❌ Erro inesperado: {}", e.getMessage(), e);
    
    // Resposta genérica ao cliente (não expõe stack trace!)
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorDTO(
            "INTERNAL_ERROR",
            "Ocorreu um erro no servidor. Tente novamente mais tarde.",
            LocalDateTime.now()));
  }
}
