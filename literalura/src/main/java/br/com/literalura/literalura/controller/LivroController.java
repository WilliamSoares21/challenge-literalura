package br.com.literalura.literalura.controller;

import java.awt.image.ReplicateScaleFilter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import br.com.literalura.literalura.mapper.LivroMapper;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.service.ConsultaGemini;
import br.com.literalura.literalura.service.LivroService;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/livros")
public class LivroController {

  private static final Logger log = LoggerFactory.getLogger(LivroController.class);

  @Autowired
  private LivroService livroService;

  @Autowired
  private LivroMapper mapper;

  @Autowired
  private ConsultaGemini consultaGemini;

  @GetMapping
  public ResponseEntity<List<LivroDTO>> listarTodos() {
    List<Livro> livros = livroService.listarTodos();

    List<LivroDTO> dto = livros.stream()
        .map(mapper::converteLivroParaDTO)
        .toList();

    return ResponseEntity.ok(dto);
  }

  @GetMapping("/titulo")
  public ResponseEntity<LivroDTO> buscarPorTitulo(@RequestParam @NotBlank String titulo) {
    Livro livro = livroService.buscarESalvarLivroPorTitulo(titulo);

    LivroDTO dto = mapper.converteLivroParaDTO(livro);

    return ResponseEntity.ok(dto);
  }

  @GetMapping("/titulo/{titulo}/estatisticas-downloads")
  public ResponseEntity<EstatisticasDownloadsDTO> getEstatisticasDownloads(@PathVariable String titulo) {
    var estatisticasDownloadsDTO = livroService.obterEstatisticasDownloadsDTO(titulo);

    return ResponseEntity.ok(estatisticasDownloadsDTO);
  }

  @GetMapping("/curiosidades")
  public ResponseEntity<CuriosidadeDTO> buscarCuriosidade(@RequestParam String titulo) {
    // LOG: Entrada no controller - rastreia qual request chegou
    log.info("CURIOSIDADE_REQ titulo='{}'", titulo);

    // Chamada ao service que coordena cache + Gemini
    String resultado = consultaGemini.obterInformacao(titulo);

    // LOG: Saída do service - verifica se resultado é null e tamanho da resposta
    // Se este log mostrar curiosidadeNull=true, confirma que a perda ocorreu no
    // service
    // tamanho > 0 indica que pelo menos texto foi recebido
    log.info("CURIOSIDADE_RES titulo='{}' curiosidadeNull={} tamanho={}",
        titulo, resultado == null, resultado == null ? 0 : resultado.length());

    return ResponseEntity.ok(new CuriosidadeDTO(titulo, resultado));
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

}
