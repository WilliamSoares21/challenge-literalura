package br.com.literalura.literalura.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.literalura.literalura.model.Autor;
import br.com.literalura.literalura.repository.AutorRepository;

@Service
public class AutorService {
  @Autowired
  private AutorRepository autorRepository;

  public List<Autor> listarTodos() {
    return autorRepository.findAllComLivros();
  }

  public List<Autor> listarAutoresVivosNoAno(Integer ano) {
    return autorRepository.findAutoresVivosNoAno(ano);
  }

  public List<Autor> buscarAutorPorNome(String nome) {
    return autorRepository.findByNomeContainingIgnoreCase(nome);
  }
}
