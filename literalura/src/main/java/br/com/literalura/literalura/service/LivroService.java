package br.com.literalura.literalura.service;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.literalura.literalura.client.GutendexClient;
import br.com.literalura.literalura.dto.AutorDTO;
import br.com.literalura.literalura.dto.EstatisticasDownloadsDTO;
import br.com.literalura.literalura.dto.LivroDTO;
import br.com.literalura.literalura.exception.LivroNaoEncontradoException;
import br.com.literalura.literalura.model.Autor;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.repository.AutorRepository;
import br.com.literalura.literalura.repository.LivroRepository;

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
    Optional<Livro> livroExistente = livroRepository.findByTituloIgnoreCase(dados.titulo());
    if (livroExistente.isPresent()) {
      return livroExistente.get();
    }

    // Obtém ou cria autor
    AutorDTO dadosAutor = dados.autores().get(0);
    Autor autor = autorRepository.findByNome(dadosAutor.nome())
        .orElseGet(() -> {
          Autor novoAutor = new Autor();
          novoAutor.setNome(dadosAutor.nome());
          novoAutor.setAnoNascimento(dadosAutor.anoNascimento());
          novoAutor.setAnoFalecimento(dadosAutor.anoFalecimento());
          return autorRepository.save(novoAutor);
        });

    // Cria e salva o livro
    Livro livro = new Livro();
    livro.setTitulo(dados.titulo());
    livro.setIdioma(dados.idiomas().get(0));
    livro.setNumeroDownloads(dados.numeroDownloads());
    livro.setAutor(autor);

    return livroRepository.save(livro);
  }

  public EstatisticasDownloadsDTO obterEstatisticasDownloadsDTO(String titulo) {
    List<Livro> livros = livroRepository.findAll();
    DoubleSummaryStatistics est = livros.stream()
        .filter(l -> l.getTitulo().equalsIgnoreCase(titulo) && l.getNumeroDownloads() > 0)
        .collect(Collectors.summarizingDouble(Livro::getNumeroDownloads));
    return new EstatisticasDownloadsDTO(est.getAverage(), est.getMax(), est.getMin(), est.getSum());
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
