package br.com.literalura.literalura.security;

import org.springframework.stereotype.Component;

/**
 * Validador de inputs contra padrões de ataque comuns.
 * 
 * Protege contra:
 * - SQL Injection patterns
 * - Command Injection
 * - Path Traversal
 * - XSS patterns
 */
@Component
public class InputValidator {

  private static final int MAX_TITLE_LENGTH = 500;
  private static final int MIN_TITLE_LENGTH = 1;

  /**
   * Valida título de livro.
   * 
   * @param titulo Título a validar
   * @return true se válido, false caso contrário
   */
  public boolean isValidTitle(String titulo) {
    if (titulo == null || titulo.isBlank()) {
      return false;
    }

    // Verifica tamanho
    if (titulo.length() < MIN_TITLE_LENGTH || titulo.length() > MAX_TITLE_LENGTH) {
      return false;
    }

    // Rejeita padrões SQL injection comuns
    if (containsSqlInjectionPatterns(titulo)) {
      return false;
    }

    // Rejeita padrões de command injection
    if (containsCommandInjectionPatterns(titulo)) {
      return false;
    }

    // Rejeita padrões de path traversal
    if (containsPathTraversalPatterns(titulo)) {
      return false;
    }

    return true;
  }

  /**
   * Valida string genérica (para outros campos).
   * 
   * @param input String a validar
   * @param maxLength Comprimento máximo permitido
   * @return true se válido
   */
  public boolean isValidInput(String input, int maxLength) {
    if (input == null || input.isBlank()) {
      return false;
    }

    if (input.length() > maxLength) {
      return false;
    }

    return !containsSqlInjectionPatterns(input) &&
        !containsCommandInjectionPatterns(input) &&
        !containsPathTraversalPatterns(input);
  }

  private boolean containsSqlInjectionPatterns(String input) {
    String lower = input.toLowerCase();
    String[] sqlPatterns = {
        "' or '1'='1",
        "'; drop",
        "'; delete",
        "'; update",
        "' union",
        "exec(",
        "execute(",
        "script>",
        "onclick",
        "onerror"
    };

    for (String pattern : sqlPatterns) {
      if (lower.contains(pattern)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsCommandInjectionPatterns(String input) {
    String[] cmdPatterns = {
        ";", "|", "||", "&", "&&",
        "`", "$(",
        "\n", "\r"
    };

    for (String pattern : cmdPatterns) {
      if (input.contains(pattern)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsPathTraversalPatterns(String input) {
    return input.contains("../") ||
        input.contains("..\\") ||
        input.contains("//") ||
        input.contains("\\\\");
  }
}
