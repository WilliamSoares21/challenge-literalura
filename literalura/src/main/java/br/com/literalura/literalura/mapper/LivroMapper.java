package br.com.literalura.literalura.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.literalura.literalura.dto.AutorDTO;
import br.com.literalura.literalura.dto.FormatsDTO;
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

    // Reconstrói o FormatsDTO com a imagem armazenada
    FormatsDTO formatsDTO = livro.getImagem() != null 
        ? new FormatsDTO(livro.getImagem()) 
        : null;

    return new LivroDTO(
        livro.getTitulo(),
        autorDTO != null ? List.of(autorDTO) : List.of(),
        List.of(livro.getIdioma()),
        livro.getNumeroDownloads(),
        formatsDTO,
        livro.getImagem(),
        livro.getResumo() != null ? List.of(livro.getResumo()) : null,
        livro.getGenero() != null ? List.of(livro.getGenero()) : List.of(),
        livro.getCuriosidade());
  }
}
