package br.com.literalura.literalura.service;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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
import br.com.literalura.literalura.mapper.LivroMapper;

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
    // Sanitização: remove espaços extras no início e fim
    String tituloSanitizado = titulo.trim();
    
    if (tituloSanitizado.isBlank()) {
      log.warn("⚠️ ERRO: Título vazio após sanitização");
      throw new IllegalArgumentException("Título não pode ser vazio");
    }

    // 1ª Tenta verificar se o livro já existe no banco (cache local)
    // Busca com o título sanitizado
    Optional<Livro> livroExistente = livroRepository.findByTituloIgnoreCase(tituloSanitizado);
    if (livroExistente.isPresent()) {
      log.info("✅ Livro encontrado no banco: '{}'", tituloSanitizado);
      return livroExistente.get();
    }

    log.info("🔍 Livro não encontrado no banco ('{}'). Consultando API Gutendex...", tituloSanitizado);

    // 2ª Consulta API externa com retry automático
    Optional<LivroDTO> optionalDados = gutendexClient.buscarLivroPorTitulo(tituloSanitizado);
    
    if (optionalDados.isEmpty()) {
      log.error("❌ Livro não encontrado em nenhuma fonte: '{}' (nem no banco, nem na API)", 
          tituloSanitizado);
      throw new LivroNaoEncontradoException(
          "Livro '" + tituloSanitizado + "' não encontrado no banco de dados nem na API Gutendex");
    }

    // 3ª Transforma os dados da API
    LivroDTO dados = optionalDados.get();

    // 4ª Persiste Autor (ou faz get se já existe)
    if (dados.autores() == null || dados.autores().isEmpty()) {
      log.warn("⚠️ Livro da API sem autores: '{}'", tituloSanitizado);
      throw new IllegalArgumentException("Livro retornado da API sem informações de autor");
    }
    
    Autor autor = obterOuCriarAutor(dados.autores().get(0));

    // 5ª Persiste Livro no banco (já na próxima busca será mais rápida)
    Livro livro = construirEPersistirLivro(dados, autor);
    log.info("✅ Livro salvo no banco com sucesso: '{}' | ID: {} (próximas buscas serão instantâneas)", 
        tituloSanitizado, livro.getId());

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
    livro.setDataUltimaAtualizacao(LocalDateTime.now());

    // Extrai imagem com segurança
    if (dados.formats() != null && dados.formats().imagemJpeg() != null) {
      livro.setImagem(dados.formats().imagemJpeg());
      log.info("✅ IMAGEM_CAPTURADA de '{}' | URL: {}", dados.titulo(), dados.formats().imagemJpeg());
    } else {
      log.warn("⚠️ IMAGEM_NÃO_CAPTURADA de '{}' | formats: {} | imagemJpeg: {}", 
        dados.titulo(), 
        dados.formats() != null ? "OK" : "NULL",
        dados.formats() != null ? dados.formats().imagemJpeg() : "N/A");
    }

    // Extrai resumo com segurança (pega o primeiro da lista de summaries)
    if (dados.summaries() != null && !dados.summaries().isEmpty()) {
      String primeiroResumo = dados.summaries().get(0);
      if (primeiroResumo != null && !primeiroResumo.isBlank()) {
        livro.setResumo(primeiroResumo);
      }
    }

    // Extrai gênero com segurança (concatena os subjects ou usa o primeiro)
    if (dados.subjects() != null && !dados.subjects().isEmpty()) {
      // Concatena todos os subjects separados por vírgula, limitando a 255 caracteres
      String generos = String.join(", ", dados.subjects());
      if (generos.length() > 255) {
        generos = generos.substring(0, 252) + "...";
      }
      livro.setGenero(generos);
    }

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

  /**
   * Obtém um livro completo e atualizado por título.
   * Estratégia: Se os dados críticos faltarem (imagem == null), re-busca na API e atualiza.
   * 
   * Fluxo:
   * 1. Sanitiza o título (remove espaços extras)
   * 2. Valida se o título é válido
   * 3. Busca o livro no banco de dados
   * 4. Valida se os dados foram completamente preenchidos
   * 5. Se faltarem dados, consulta a API Gutendex novamente
   * 6. Preenche os dados e registra o timestamp de atualização
   * 7. Retorna o livro completo em DTO
   * 
   * @param titulo Título do livro a buscar (case-insensitive)
   * @return LivroDTO com dados completos
   * @throws LivroNaoEncontradoException se livro não existir no banco nem na API
   * @throws IllegalArgumentException se título for nulo ou vazio
   */
  public LivroDTO obterLivroCompleto(String titulo) {
    // Sanitização: remove espaços extras no início e fim
    String tituloSanitizado = (titulo != null) ? titulo.trim() : "";
    
    // Validação de entrada
    if (tituloSanitizado.isBlank()) {
      log.warn("⚠️ INVALID_INPUT: Título nulo ou vazio fornecido para obterLivroCompleto");
      throw new IllegalArgumentException("Título não pode ser nulo ou vazio");
    }

    // 1ª Busca o livro no banco de dados local com título sanitizado
    Livro livro = livroRepository.findByTituloIgnoreCase(tituloSanitizado)
        .orElse(null);

    // Se não encontrou no banco, tenta buscar na API
    if (livro == null) {
      log.info("🔍 Livro '{}' não encontrado no banco. Consultando API Gutendex...", tituloSanitizado);
      
      Optional<LivroDTO> dadosAPI = gutendexClient.buscarLivroPorTitulo(tituloSanitizado);
      
      if (dadosAPI.isEmpty()) {
        log.error("❌ Livro não encontrado na API: '{}'", tituloSanitizado);
        throw new LivroNaoEncontradoException(
            "Livro '" + tituloSanitizado + "' não encontrado no banco de dados nem na API Gutendex");
      }

      // Persiste o livro da API no banco para futuras buscas
      try {
        LivroDTO dados = dadosAPI.get();
        
        if (dados.autores() == null || dados.autores().isEmpty()) {
          log.error("❌ Livro da API sem informações de autor: '{}'", tituloSanitizado);
          throw new IllegalArgumentException("Livro retornado da API sem informações de autor");
        }
        
        Autor autor = obterOuCriarAutor(dados.autores().get(0));
        livro = construirEPersistirLivro(dados, autor);
        log.info("✅ Livro persistido a partir da API: '{}' | ID: {}", tituloSanitizado, livro.getId());
      } catch (Exception e) {
        log.error("❌ Erro ao persistir livro da API: '{}' | Erro: {}", tituloSanitizado, e.getMessage());
        throw e;
      }
    }

    // 2ª Verifica se os dados estão "velhos" (imagem == null)
    // Isso indica que o livro foi criado antes das colunas serem populadas
    if (livro.getImagem() == null) {
      log.info("🔄 Atualizando dados antigos/incompletos do livro: '{}' | ID: {} | Última atualização: {}",
          livro.getTitulo(), livro.getId(), livro.getDataUltimaAtualizacao());

      try {
        // Re-busca na API Gutendex
        Optional<LivroDTO> dadosNovos = gutendexClient.buscarLivroPorTitulo(livro.getTitulo());

        if (dadosNovos.isPresent()) {
          // Preenche apenas os campos que faltavam (estratégia não-destrutiva)
          preencherDadosFaltantes(livro, dadosNovos.get());

          // Atualiza o timestamp de última atualização
          livro.setDataUltimaAtualizacao(LocalDateTime.now());

          // Persiste as mudanças
          livro = livroRepository.save(livro);
          log.info("✅ Livro atualizado com sucesso: '{}' | Timestamp: {} | Campos: imagem={} resumo={} genero={}",
              livro.getTitulo(), 
              livro.getDataUltimaAtualizacao(),
              livro.getImagem() != null ? "✅" : "❌",
              livro.getResumo() != null ? "✅" : "❌",
              livro.getGenero() != null ? "✅" : "❌");
        } else {
          log.warn("⚠️ Não foi possível atualizar o livro na API: '{}'. Retornando dados parciais.", 
              tituloSanitizado);
        }
      } catch (Exception e) {
        log.error("❌ Erro ao atualizar dados do livro: '{}' | Erro: {} | Tipo: {}", 
            tituloSanitizado, e.getMessage(), e.getClass().getSimpleName(), e);
        // Não falha, apenas retorna o livro com dados parciais (graceful degradation)
      }
    } else {
      log.debug("✅ Livro '{}' já possui dados completos, nenhuma atualização necessária", livro.getTitulo());
    }

    // 3ª Retorna o livro em formato DTO
    return new LivroMapper().converteLivroParaDTO(livro);
  }

  /**
   * Preenche os campos faltantes (imagem, resumo, gênero) de um livro existente.
   * Estratégia não-destrutiva: não sobrescreve dados já preenchidos.
   * 
   * @param livro Livro existente a ser atualizado
   * @param dadosNovos Dados recebidos da API Gutendex
   */
  private void preencherDadosFaltantes(Livro livro, LivroDTO dadosNovos) {
    if (dadosNovos == null) {
      return;
    }

    // Preenche imagem apenas se estiver nula
    if (livro.getImagem() == null && dadosNovos.formats() != null && 
        dadosNovos.formats().imagemJpeg() != null) {
      livro.setImagem(dadosNovos.formats().imagemJpeg());
      log.info("✅ IMAGEM_ATUALIZADA de '{}' | URL: {}", livro.getTitulo(), dadosNovos.formats().imagemJpeg());
    } else if (livro.getImagem() == null) {
      log.warn("⚠️ IMAGEM_NÃO_DISPONÍVEL para '{}' | formats: {} | imagemJpeg: {}", 
        livro.getTitulo(),
        dadosNovos.formats() != null ? "OK" : "NULL",
        dadosNovos.formats() != null ? dadosNovos.formats().imagemJpeg() : "N/A");
    }

    // Preenche resumo apenas se estiver nulo
    if (livro.getResumo() == null && dadosNovos.summaries() != null && 
        !dadosNovos.summaries().isEmpty()) {
      String primeiroResumo = dadosNovos.summaries().get(0);
      if (primeiroResumo != null && !primeiroResumo.isBlank()) {
        livro.setResumo(primeiroResumo);
        log.debug("Resumo preenchido para livro: '{}' | Tamanho: {} caracteres", 
            livro.getTitulo(), primeiroResumo.length());
      }
    }

    // Preenche gênero apenas se estiver nulo
    if (livro.getGenero() == null && dadosNovos.subjects() != null && 
        !dadosNovos.subjects().isEmpty()) {
      String generos = String.join(", ", dadosNovos.subjects());
      // Limita a 255 caracteres (tamanho da coluna)
      if (generos.length() > 255) {
        generos = generos.substring(0, 252) + "...";
        log.debug("Gênero truncado para tamanho máximo de 255 caracteres");
      }
      livro.setGenero(generos);
      log.debug("Gênero preenchido para livro: '{}'", livro.getTitulo());
    }

    // Atualiza número de downloads se a API tem informação mais recente
    if (dadosNovos.numeroDownloads() != null && dadosNovos.numeroDownloads() > 0) {
      livro.setNumeroDownloads(dadosNovos.numeroDownloads());
      log.debug("Downloads atualizado para livro: '{}' | Novo valor: {}", 
          livro.getTitulo(), dadosNovos.numeroDownloads());
    }
  }
}
