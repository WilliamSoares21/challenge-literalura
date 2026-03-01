package br.com.literalura.literalura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.literalura.literalura.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {
  List<Livro> findByIdioma(String idioma);

  Optional<Livro> findByTituloIgnoreCase(String titulo);

  List<Livro> findTop10ByOrderByNumeroDownloadsDesc();
}
