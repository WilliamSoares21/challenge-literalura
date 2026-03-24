package br.com.literalura.literalura.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.literalura.literalura.dto.AutorDTO;
import br.com.literalura.literalura.model.Autor;
import br.com.literalura.literalura.service.AutorService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/autores")
@CrossOrigin(origins = "http://localhost:3000")
public class AutorController {

  @Autowired
  private AutorService service;

  @GetMapping
  public ResponseEntity<List<AutorDTO>> listarAutores() {
    List<Autor> autores = service.listarTodos();

    List<AutorDTO> dto = autores.stream()
        .map(a -> new AutorDTO(
            a.getNome(),
            a.getAnoNascimento(),
            a.getAnoFalecimento()))
        .toList();
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/vivos/{ano}")
  public ResponseEntity<List<AutorDTO>> listarAutoresVivosEmAno(@PathVariable Integer ano) {
    List<Autor> autores = service.listarAutoresVivosNoAno(ano);

    List<AutorDTO> dto = autores.stream()
        .map(a -> new AutorDTO(
            a.getNome(),
            a.getAnoNascimento(),
            a.getAnoFalecimento()))
        .toList();
    return ResponseEntity.ok(dto);
  }
}
