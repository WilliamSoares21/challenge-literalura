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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LivroService {
  @Autowired
  private LivroRepository livroRepository;
  @Autowired
  private AutorRepository autorRepository;
  @Autowired
  private GutendexClient gutendexClient;

  private static final Logger log = LoggerFactory.getLogger(LivroService.class);

  public Livro buscarESalvarLivroPorTitulo(String titulo) {

    // 1ª Tenta verificar se o livro já existe no banco (cache local)
    Optional<Livro> livroExistente = livroRepository.findByTituloIgnoreCase(titulo);
    if (livroExistente.isPresent()) {
      log.info("LIvro encontrado no banco: '{}'", titulo);
      return livroExistente.get();
    }

    // 2ª Consulta API externa
    log.info("-> Livro não encontrado no banco, consultando API: '{}' ", titulo);
    Optional<LivroDTO> optionalDados = gutendexClient.buscarLivroPorTitulo(titulo);
    if (optionalDados.isEmpty()) {
      log.warn("Error: livro não encontrado em nenhuma fonte: '{}'", titulo);
      throw new LivroNaoEncontradoException("Livro " + titulo + " não encontrado na API do gutendex");
    }

    // 3ª Transforma os dados da API
    LivroDTO dados = optionalDados.get();

    // 4ª Persiste Autor (ou faz get se já existe)
    Autor autor = obterOuCriarAutor(dados.autores().get(0));

    // 5ª Persiste Livro no banco (já na próxima busca será mais rápida)
    Livro livro = construirEPersistirLivro(dados, autor);
    log.info("Livro salvo no banco: '{}' (próximas buscas serão instantâneas)", titulo);

    return livro;
  }

  private Autor obterOuCriarAutor(AutorDTO autorDTO) {
    return autorRepository.findByNome(autorDTO.nome())
        .orElseGet(() -> {
          Autor novoAutor = new Autor();
          novoAutor.setNome(autorDTO.nome());
          novoAutor.setAnoNascimento(autorDTO.anoNascimento());
          novoAutor.setAnoFalecimento(autorDTO.anoFalecimento());
          return autorRepository.save(novoAutor);
        });
  }

  private Livro construirEPersistirLivro(LivroDTO dados, Autor autor) {
    Livro livro = new Livro();
    livro.setTitulo(dados.titulo());
    livro.setIdioma(dados.idiomas().isEmpty() ? "pt" : dados.idiomas().get(0));
    livro.setNumeroDownloads(dados.numeroDownloads() != null ? dados.numeroDownloads() : 0);
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
