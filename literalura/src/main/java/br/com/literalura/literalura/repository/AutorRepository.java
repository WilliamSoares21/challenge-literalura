package br.com.literalura.literalura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.literalura.literalura.model.Autor;

public interface AutorRepository extends JpaRepository<Autor, Long> {
  // Busca todos os autores e já traz os livros de cada um em uma única consulta
  @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.livros")
  List<Autor> findAllComLivros();

  // Busca autores vivos e já traz seus livros para evitar novas consultas no loop de exibição
  @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.livros WHERE a.anoNascimento <= :ano AND (a.anoFalecimento IS NULL OR a.anoFalecimento >= :ano)")
  List<Autor> findAutoresVivosNoAno(@Param("ano") Integer ano);

  List<Autor> findByNomeContainingIgnoreCase(String nome);

  List<Autor> findByAnoNascimentoBetween(Integer inicio, Integer fim);

  Optional<Autor> findByNome(String nome);
}
