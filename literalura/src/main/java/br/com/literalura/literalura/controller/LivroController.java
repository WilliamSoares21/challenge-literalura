package br.com.literalura.literalura.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.literalura.literalura.dto.EstatisticasDownloadsDTO;
import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.mapper.LivroMapper;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.service.LivroService;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/livros")
public class LivroController {

  @Autowired
  private LivroService livroService;

  @Autowired
  private LivroMapper mapper;

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

  @GetMapping
  public ResponseEntity<List<LivroDTO>> listarTodos() {
    List<Livro> livros = livroService.listarTodos();

    List<LivroDTO> dto = livros.stream()
        .map(mapper::converteLivroParaDTO)
        .toList();

    return ResponseEntity.ok(dto);
  }
}
