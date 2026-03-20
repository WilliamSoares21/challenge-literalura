package br.com.literalura.literalura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.mapper.LivroMapper;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.service.LivroService;

@RestController
@RequestMapping("/livros")
public class LivroController {

  @Autowired
  private LivroService livroService;

  @Autowired
  private LivroMapper mapper;

  @GetMapping
  public ResponseEntity<LivroDTO> buscarPorTitulo(@RequestParam String titulo) {
    Livro livro = livroService.buscarESalvarLivroPorTitulo(titulo);

    LivroDTO dto = mapper.converteLivroParaDTO(livro);

    return ResponseEntity.ok(dto);
  }

}
