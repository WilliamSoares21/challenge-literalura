package br.com.literalura.literalura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.literalura.literalura.service.LivroService;

@RestController
@RequestMapping("/livros")
public class LivroController {

  @Autowired
  private LivroService livroService;

}