package br.com.literalura.literalura.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.repository.LivroRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaGemini {
  private static final Logger log = LoggerFactory.getLogger(ConsultaGemini.class);

  private final String apiKey;

  private final String model;
  private final LivroRepository livroRepository;

  public ConsultaGemini(@Value("${gemini.api-key}") String apiKey,
      @Value("${gemini.model}") String model,
      LivroRepository livroRepository) {
    this.apiKey = apiKey;
    this.model = model;
    this.livroRepository = livroRepository;
  }

  @Transactional // <--- MUITO IMPORTANTE: Garante consistência do banco durante a atualização
  public String obterInformacao(String texto) {
    String nomeNormalizado = texto.toLowerCase().trim();

    // LOG 1: Entrada do método com o título original e normalizado
    // Ajuda a rastrear qual título está sendo processado
    log.info("CACHE_MISS titulo='{}' (Necessário consultar Gemini)", nomeNormalizado);

    // DIAGNÓSTICO: Detecta duplicatas no banco (problema comum)
    // Se houver 2+ livros com o mesmo título, a query retorna apenas o primeiro
    // (por ID)
    // Este log avisa quando há duplicatas para que eu possa limpar manualmente
    var duplicatas = livroRepository.findDuplicateTitles();
    if (!duplicatas.isEmpty()) {
      log.warn("⚠️ DUPLICATAS_DETECTADAS: {} títulos com múltiplos registros: {}",
          duplicatas.size(), duplicatas);
    }

    Optional<Livro> cache = livroRepository.findByTituloIgnoreCase(nomeNormalizado);
    if (cache.isPresent() && cache.get().getCuriosidade() != null && !cache.get().getCuriosidade().isBlank()) {
      // LOG 2: Cache HIT - livro encontrado no banco com curiosidade válida
      // Campos importantes: ID do livro (para auditoria), título armazenado
      // curiosidadeNull: verifica se é null, curiosidadeBlank: verifica se é
      // vazio/whitespace
      Livro livroCache = cache.get();
      log.info("CACHE_HIT id={} titulo='{}' curiosidadeNull={} curiosidadeBlank={}",
          livroCache.getId(),
          livroCache.getTitulo(),
          livroCache.getCuriosidade() == null,
          livroCache.getCuriosidade() != null && livroCache.getCuriosidade().isBlank());
      return livroCache.getCuriosidade();
    }

    // LOG 3: Cache MISS - livro não encontrado ou curiosidade vazia
    // Significa que vai chamar a API Gemini
    log.info("CACHE_MISS titulo='{}'", nomeNormalizado);

    Client client = Client.builder()
        .apiKey(apiKey)
        .build();

    String prompt = String.format(
        "Cite uma curiosidade única e pouco conhecida sobre o livro %s. " +
            "Máximo 50 palavras. Responda apenas o fato, sem introduções, saudações ou conclusões.",
        texto);

    GenerateContentResponse response = client.models.generateContent(
        model,
        prompt,
        null);

    // LOG 4: Resposta da API Gemini - momento crítico para diagnosticar null
    // textNull: true se response.text() retornou null (problema na API)
    // textBlank: true se retornou string vazia ou só whitespace
    // finishReason: explica POR QUE a resposta acabou (STOP, SAFETY, etc)
    // partsCount: número de partes na resposta estruturada
    // candidatesCount: número de candidatos de resposta fornecidos
    String curiosidade = response.text();
    log.info("GEMINI_OUT textNull={} textBlank={} finishReason={} partsCount={} candidatesCount={}",
        curiosidade == null,
        curiosidade != null && curiosidade.isBlank(),
        response.finishReason(),
        response.parts() == null ? 0 : response.parts().size(),
        response.candidates().map(java.util.List::size).orElse(0));

    // LOG 4.1: Se a resposta veio vazia/null, registra mais detalhes
    // responseId: ID da requisição na API (útil para Support)
    // promptFeedbackPresent: se API forneceu feedback sobre o prompt
    // 1. Buscamos novamente o livro que causou o CACHE_MISS (para ter certeza que
    // ele está "monitorado" pelo JPA)
    Optional<Livro> livroOriginal = livroRepository.findByTituloIgnoreCase(nomeNormalizado);

    Livro livroParaSalvar;

    if (livroOriginal.isPresent()) {
      // CÉNARIO A: O livro existe (como o ID 2 "Dracula"). Vamos ENRIQUECER.
      livroParaSalvar = livroOriginal.get();
      log.info("🆙 Atualizando livro existente (ID: {}) com nova curiosidade.", livroParaSalvar.getId());
      livroParaSalvar.setCuriosidade(curiosidade);
      // Ao alterar o campo em um objeto monitorado dentro de uma transação,
      // o JPA detecta a mudança e faz um UPDATE automaticamente ao fim do método.
      // Mas chamar o saveexplicitamente ajuda na clareza.
    } else {
      // CÉNARIO B: O livro não existe no banco. Vamos CRIAR (INSERT).
      // Para manter a consistência visual no banco, vamos capitalizar a primeira
      // letra
      String tituloFormatado = formatarTitulo(texto);
      log.info("➕ Criando novo registro para livro inexistente: '{}'", tituloFormatado);
      livroParaSalvar = new Livro(tituloFormatado, curiosidade);
    }

    // Faz o save (se for update, ele sabe; se for novo, ele insere)
    Livro salvo = livroRepository.save(livroParaSalvar);

    log.info("CACHE_SAVE id={} titulo='{}' curiosidadeNull={}",
        salvo.getId(), salvo.getTitulo(), salvo.getCuriosidade() == null);

    return curiosidade;
  }

  // Helper simples para formatar título na criação
  private String formatarTitulo(String t) {
    if (t == null || t.trim().isEmpty())
      return t;
    String trimmed = t.trim();
    return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
  }
}
