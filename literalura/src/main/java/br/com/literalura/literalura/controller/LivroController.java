package br.com.literalura.literalura.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.literalura.literalura.dto.CuriosidadeDTO;
import br.com.literalura.literalura.dto.EstatisticasDownloadsDTO;
import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.exception.LivroNaoEncontradoException;
import br.com.literalura.literalura.mapper.LivroMapper;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.security.InputValidator;
import br.com.literalura.literalura.security.RateLimiter;
import br.com.literalura.literalura.service.ConsultaGemini;
import br.com.literalura.literalura.service.LivroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/livros")
@CrossOrigin(origins = "http://localhost:3000")
public class LivroController {

  private static final Logger log = LoggerFactory.getLogger(LivroController.class);

  @Autowired
  private LivroService livroService;

  @Autowired
  private LivroMapper mapper;

  @Autowired
  private ConsultaGemini consultaGemini;

  @Autowired
  private InputValidator inputValidator;

  @Autowired
  private RateLimiter rateLimiter;

  @GetMapping
  public ResponseEntity<List<LivroDTO>> listarTodos() {
    List<Livro> livros = livroService.listarTodos();
    List<LivroDTO> dto = livros.stream()
        .map(mapper::converteLivroParaDTO)
        .toList();
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/titulo")
  public ResponseEntity<LivroDTO> buscarPorTitulo(
      @RequestParam @NotBlank(message = "Título não pode ser vazio") String titulo,
      HttpServletRequest request) {

    String clientIp = getClientIp(request);
    if (!rateLimiter.allowRequest(clientIp)) {
      log.warn("🚫 RATE_LIMIT_EXCEEDED para IP: {}", clientIp);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    if (!inputValidator.isValidTitle(titulo)) {
      log.warn("⚠️ INVALID_INPUT detectado: titulo='{}' (pode conter injeção)", titulo);
      return ResponseEntity.badRequest().build();
    }

    try {
      Livro livro = livroService.buscarESalvarLivroPorTitulo(titulo);
      LivroDTO dto = mapper.converteLivroParaDTO(livro);
      return ResponseEntity.ok(dto);
    } catch (LivroNaoEncontradoException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Erro ao buscar livro: {}", e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/titulo/{titulo}/estatisticas-downloads")
  public ResponseEntity<EstatisticasDownloadsDTO> getEstatisticasDownloads(@PathVariable String titulo) {
    var estatisticasDownloadsDTO = livroService.obterEstatisticasDownloadsDTO(titulo);
    return ResponseEntity.ok(estatisticasDownloadsDTO);
  }

  @GetMapping("/curiosidades")
  public ResponseEntity<CuriosidadeDTO> buscarCuriosidade(
      @RequestParam String titulo,
      HttpServletRequest request) {

    String clientIp = getClientIp(request);
    if (!rateLimiter.allowRequest(clientIp)) {
      log.warn("🚫 RATE_LIMIT_EXCEEDED para IP: {} no endpoint /curiosidades", clientIp);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    if (titulo == null || titulo.isBlank() || !inputValidator.isValidTitle(titulo)) {
      log.warn("⚠️ INVALID_INPUT no /curiosidades: titulo='{}' (pode conter injeção)", titulo);
      return ResponseEntity.badRequest().build();
    }

    log.info("CURIOSIDADE_REQ titulo='{}' from IP={}", titulo, clientIp);

    try {
      String resultado = consultaGemini.obterInformacao(titulo);
      log.info("CURIOSIDADE_RES titulo='{}' curiosidadeNull={} tamanho={}",
          titulo, resultado == null, resultado == null ? 0 : resultado.length());
      return ResponseEntity.ok(new CuriosidadeDTO(titulo, resultado));
    } catch (Exception e) {
      log.error("❌ Erro ao buscar curiosidade para: {} | Erro: {}", titulo, e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/idioma/{idioma}")
  public ResponseEntity<List<LivroDTO>> listarPorIdioma(@PathVariable String idioma) {
    List<Livro> livros = livroService.listarPorIdioma(idioma);
    List<LivroDTO> dto = livros.stream()
        .map(mapper::converteLivroParaDTO)
        .toList();
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/top10")
  public ResponseEntity<List<LivroDTO>> obterTop10() {
    List<Livro> livros = livroService.obterTop10();
    List<LivroDTO> dto = livros.stream()
        .map(mapper::converteLivroParaDTO)
        .toList();
    return ResponseEntity.ok(dto);
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
  
  @GetMapping("/completo")
  public ResponseEntity<LivroDTO> obterLivroCompleto(
    @RequestParam @NotBlank(message = "Título não pode ser vazio") String titulo,
    HttpServletRequest request) {

    // Validação de rate limiting
    String clientIp = getClientIp(request);
    if (!rateLimiter.allowRequest(clientIp)) {
      log.warn("🚫 RATE_LIMIT_EXCEEDED para IP: {} no endpoint /completo", clientIp);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    // Validação de injeção
    if (!inputValidator.isValidTitle(titulo)) {
      log.warn("⚠️ INVALID_INPUT detectado: titulo='{}' (pode conter injeção)", titulo);
      return ResponseEntity.badRequest().build();
    }

    try {
      log.info("LIVRO_COMPLETO_REQ titulo='{}' from IP={}", titulo, clientIp);
      
      // Busca ou atualiza o livro
      LivroDTO livroCompleto = livroService.obterLivroCompleto(titulo);
      
      log.info("LIVRO_COMPLETO_RES titulo='{}' imagem={} resumo={} genero={}",
          titulo, 
          livroCompleto.formats() != null ? "✅" : "❌",
          livroCompleto.summaries() != null ? "✅" : "❌",
          !livroCompleto.subjects().isEmpty() ? "✅" : "❌");
      
      return ResponseEntity.ok(livroCompleto);
      
    } catch (LivroNaoEncontradoException e) {
      log.warn("Livro não encontrado: {}", titulo);
      return ResponseEntity.notFound().build();
      
    } catch (IllegalArgumentException e) {
      log.warn("Argumento inválido: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
      
    } catch (Exception e) {
      log.error("❌ Erro ao buscar livro completo: {} | Erro: {}", titulo, e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

}
