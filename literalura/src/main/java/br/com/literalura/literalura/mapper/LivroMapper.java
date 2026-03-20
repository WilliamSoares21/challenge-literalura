package br.com.literalura.literalura.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.literalura.literalura.dto.AutorDTO;
import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.model.Livro;

@Component
public class LivroMapper {
  public LivroDTO converteLivroParaDTO(Livro livro) {
    // Mapeia o autor da entidade para o DTO
    AutorDTO autorDTO = null;
    if (livro.getAutor() != null) {
      autorDTO = new AutorDTO(
          livro.getAutor().getNome(),
          livro.getAutor().getAnoNascimento(),
          livro.getAutor().getAnoFalecimento());
    }

    return new LivroDTO(
        livro.getTitulo(),
        autorDTO != null ? List.of(autorDTO) : List.of(),
        List.of(livro.getIdioma()),
        livro.getNumeroDownloads(),
        livro.getCuriosidade());
  }
}
