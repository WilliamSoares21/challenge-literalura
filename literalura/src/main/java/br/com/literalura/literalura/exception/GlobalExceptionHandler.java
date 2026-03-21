package br.com.literalura.literalura.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.literalura.literalura.dto.ErrorDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(LivroNaoEncontradoException.class)
  public ResponseEntity<ErrorDTO> handleLivroNaoEncontrado(LivroNaoEncontradoException e) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorDTO(
            "LIVRO_NAO_ENCONTRADO",
            e.getMessage(),
            LocalDateTime.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDTO> handleValidation(
      MethodArgumentNotValidException ex) {

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
}
