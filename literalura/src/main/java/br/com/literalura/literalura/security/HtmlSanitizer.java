package br.com.literalura.literalura.security;

import org.springframework.stereotype.Component;

/**
 * Sanitizador de HTML para prevenir XSS (Cross-Site Scripting).
 * 
 * Protege contra:
 * - Script injections (<script>, <iframe>)
 * - Event handlers (onclick, onerror, etc)
 * - Dangerous protocols (javascript:, data:)
 * - HTML tags potencialmente perigosas
 * 
 * Nota: Esta é uma proteção básica. Para produção, futuramente irei estudar
 * bibliotecas como OWASP Java Encoder ou OWASP HTML Sanitizer.
 */
@Component
public class HtmlSanitizer {

  /**
   * Sanitiza string removendo tags e atributos perigosos.
   * Mantém apenas texto escapado.
   * 
   * @param input String potencialmente maliciosa
   * @return String sanitizada segura para armazenar e retornar
   */
  public String sanitize(String input) {
    if (input == null || input.isBlank()) {
      return input;
    }

    // Escape básico de caracteres HTML perigosos
    String sanitized = input
        .replaceAll("<", "&lt;")           // < → &lt;
        .replaceAll(">", "&gt;")           // > → &gt;
        .replaceAll("\"", "&quot;")        // " → &quot;
        .replaceAll("'", "&#x27;")         // ' → &#x27;
        .replaceAll("/", "&#x2F;");        // / → &#x2F;

    return sanitized;
  }

  /**
   * Valida se a string contém padrões maliciosos conhecidos.
   * Retorna true se suspicious, false se limpo.
   * 
   * @param input String a validar
   * @return true se contém padrões maliciosos
   */
  public boolean isSuspicious(String input) {
    if (input == null) return false;

    // Padrões de XSS conhecidos (case-insensitive)
    String[] xssPatterns = {
        "<script", "</script",
        "onerror=", "onclick=", "onload=", "onmouseover=",
        "javascript:", "data:text/html",
        "<iframe", "</iframe",
        "<object", "<embed",
        "eval(", "expression(",
        "<img", "src="
    };

    String lowerInput = input.toLowerCase();
    for (String pattern : xssPatterns) {
      if (lowerInput.contains(pattern)) {
        return true;
      }
    }

    return false;
  }
}
