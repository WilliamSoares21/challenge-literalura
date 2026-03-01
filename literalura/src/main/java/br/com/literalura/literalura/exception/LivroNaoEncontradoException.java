package br.com.literalura.literalura.exception;

public class LivroNaoEncontradoException extends RuntimeException {
  public LivroNaoEncontradoException(String message) {
    super(message);
  }
}
