package br.com.literalura.literalura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.literalura.literalura.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {
  List<Livro> findByIdioma(String idioma);

  /**
   * Busca livro por título (case-insensitive).
   * Se houver duplicatas, retorna o resultado mais antigo (id menor).
   * 
   * Por que @Query customizada?
   * - findByTituloIgnoreCase() falha se houver 2+ resultados
   * - findFirstByTituloIgnoreCase() pega qualquer um (não determinístico)
   * - Esta query garante resultado previsível: sempre a primeira instância salva
   */
  @Query("SELECT l FROM Livro l WHERE LOWER(l.titulo) = LOWER(:titulo) ORDER BY l.id ASC LIMIT 1")
  Optional<Livro> findByTituloIgnoreCase(@Param("titulo") String titulo);

  /**
   * Lista títulos duplicados (case-insensitive).
   */
  @Query("SELECT LOWER(l.titulo) FROM Livro l GROUP BY LOWER(l.titulo) HAVING COUNT(l) > 1")
  List<String> findDuplicateTitles();

  List<Livro> findTop10ByOrderByNumeroDownloadsDesc();
}
