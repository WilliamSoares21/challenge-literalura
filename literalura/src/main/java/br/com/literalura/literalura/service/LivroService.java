package br.com.literalura.literalura.service;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.literalura.literalura.client.GutendexClient;
import br.com.literalura.literalura.dto.AutorDTO;
import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.model.Autor;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.repository.AutorRepository;
import br.com.literalura.literalura.repository.LivroRepository;
import br.com.literalura.literalura.exception.LivroNaoEncontradoException;
@Service
public class LivroService {
  @Autowired
  private LivroRepository livroRepository;
  @Autowired
  private AutorRepository autorRepository;
  @Autowired
  private GutendexClient gutendexClient;

  public Livro buscarESalvarLivroPorTitulo(String titulo) {
    // Consulta API
    Optional<LivroDTO> optionalDados = gutendexClient.buscarLivroPorTitulo(titulo);
    if (optionalDados.isEmpty()) {
      throw new LivroNaoEncontradoException("Livro não encontrado na API");
    }
    LivroDTO dados = optionalDados.get();

    // Verifica se livro já existe no banco (pelo título)
    Optional<Livro> livroExistente = livroRepository.findByTituloIgnoreCase(dados.title());
    if (livroExistente.isPresent()) {
      return livroExistente.get();
    }

    // Obtém ou cria autor
    AutorDTO dadosAutor = dados.authors().get(0); // assume que tem pelo menos um autor
    Autor autor = autorRepository.findByNome(dadosAutor.name())
        .orElseGet(() -> {
          Autor novoAutor = new Autor();
          novoAutor.setNome(dadosAutor.name());
          novoAutor.setAnoNascimento(dadosAutor.birth_year());
          novoAutor.setAnoFalecimento(dadosAutor.death_year());
          return autorRepository.save(novoAutor);
        });

    // Cria e salva o livro
    Livro livro = new Livro();
    livro.setTitulo(dados.title());
    livro.setIdioma(dados.languages().get(0)); // considera primeiro idioma
    livro.setNumeroDownloads(dados.download_count());
    livro.setAutor(autor);

    return livroRepository.save(livro);
  }

  public void exibirEstatisticas() {
        List<Livro> livros = livroRepository.findAll();
        
        DoubleSummaryStatistics est = livros.stream()
                .filter(l -> l.getNumeroDownloads() > 0)
                .collect(Collectors.summarizingDouble(Livro::getNumeroDownloads));

        System.out.println("\n--- Estatísticas de Downloads ---");
        System.out.println("Média: " + String.format("%.2f", est.getAverage()));
        System.out.println("Máximo: " + est.getMax());
        System.out.println("Mínimo: " + est.getMin());
        System.out.println("Total de registros: " + est.getCount());
  }

  public List<Livro> obterTop10() {
    return livroRepository.findTop10ByOrderByNumeroDownloadsDesc();
  }

  public List<Livro> listarTodos() {
    return livroRepository.findAll();
  }

  public List<Livro> listarPorIdioma(String idioma) {
    return livroRepository.findByIdioma(idioma);
  }
}
