# Guia de Leitura dos Logs de Diagnóstico

## O que cada log faz e por que foi adicionado

### 1. **GEMINI_IN** (Entrada no service)
```
log.info("GEMINI_IN tituloOriginal='{}' tituloNormalizado='{}'", texto, nomeNormalizado);
```
**O que faz:** Registra o título exatamente como veio (ex: "DRACULA") e como foi normalizado (ex: "dracula")

**Por que é importante:** 
- Se há problema de case-sensitivity, você vê aqui
- Permite rastrear qual requisição gerou qual log
- Marca o início da operação

**Exemplo de saída:**
```
GEMINI_IN tituloOriginal='DRACULA' tituloNormalizado='dracula'
```

---

### 2. **CACHE_HIT** (Encontrou no banco)
```
log.info("CACHE_HIT id={} titulo='{}' curiosidadeNull={} curiosidadeBlank={}",
    livroCache.getId(), livroCache.getTitulo(), 
    livroCache.getCuriosidade() == null,
    livroCache.getCuriosidade() != null && livroCache.getCuriosidade().isBlank());
```

**O que faz:** Confirma que o livro já estava no banco de dados com curiosidade armazenada

**Por que é importante:** 
- Diferencia entre "não encontrou" vs "encontrou mas curiosidade é null/vazia"
- `curiosidadeNull=true` = PROBLEMA! O cache estava vazio
- `curiosidadeBlank=true` = PROBLEMA! O cache tinha só espaços em branco
- Ambos os casos agora usam a nova lógica para chamar Gemini novamente

**Exemplo de saída PROBLEMA:**
```
CACHE_HIT id=1 titulo='dracula' curiosidadeNull=true curiosidadeBlank=false
↑ Isso significa que um livro antigo foi salvo SEM curiosidade!
```

**Exemplo de saída OK:**
```
CACHE_HIT id=1 titulo='dracula' curiosidadeNull=false curiosidadeBlank=false
↑ Livro encontrado com curiosidade válida, retorna direto (economia de API)
```

---

### 3. **CACHE_MISS** (Não encontrou ou curiosidade vazia)
```
log.info("CACHE_MISS titulo='{}'", nomeNormalizado);
```

**O que faz:** Indica que vai chamar a API Gemini porque o livro não estava em cache ou estava com curiosidade vazia

**Por que é importante:**
- Você vê quando a API está sendo chamada vs quando está usando cache
- Ajuda a entender o padrão de acesso

**Exemplo:**
```
CACHE_MISS titulo='1984'
↑ Vai chamar Gemini agora
```

---

### 4. **GEMINI_OUT** (Resposta da API)
```
log.info("GEMINI_OUT textNull={} textBlank={} finishReason={} partsCount={} candidatesCount={}",
    curiosidade == null,
    curiosidade != null && curiosidade.isBlank(),
    response.finishReason(),
    response.parts() == null ? 0 : response.parts().size(),
    response.candidates().map(java.util.List::size).orElse(0));
```

**O que cada campo significa:**

| Campo | Significado |
|-------|-------------|
| `textNull=true` | response.text() retornou null - PROBLEMA! |
| `textBlank=true` | response.text() retornou string vazia "" ou só espaços - PROBLEMA! |
| `finishReason=STOP` | Resposta acabou normalmente ✓ |
| `finishReason=MAX_TOKENS` | Atingiu limite de tokens |
| `finishReason=SAFETY` | API bloqueou por segurança! |
| `partsCount=1` | Quantas partes estruturadas vieram (esperado: 1 para texto) |
| `candidatesCount=1` | Quantas respostas alternativas (esperado: 1) |

**Por que é importante:**
- Se `textNull=true` e `finishReason=SAFETY`, a API bloqueou o conteúdo
- Se `textNull=true` mas `finishReason=STOP`, há bug na lib ou formatação incorreta

**Exemplo de PROBLEMA:**
```
GEMINI_OUT textNull=true textBlank=false finishReason=SAFETY partsCount=0 candidatesCount=0
↑ API bloqueou a resposta por motivos de segurança!
```

**Exemplo de OK:**
```
GEMINI_OUT textNull=false textBlank=false finishReason=STOP partsCount=1 candidatesCount=1
↑ Resposta recebida normalmente
```

---

### 5. **GEMINI_EMPTY_RESPONSE** (Alerta se resposta vazia)
```
if (curiosidade == null || curiosidade.isBlank()) {
    log.warn("GEMINI_EMPTY_RESPONSE responseId={} promptFeedbackPresent={}",
        response.responseId().orElse("sem-id"),
        response.promptFeedback().isPresent());
}
```

**O que faz:** Se a resposta foi vazia/null, registra com nível WARNING (mais destaque que INFO)

**Por que é importante:**
- `responseId` = ID da requisição para você enviar ao Google Support se precisar
- `promptFeedbackPresent=true` = API forneceu critique ao prompt (talvez prompt ruim?)

**Exemplo:**
```
GEMINI_EMPTY_RESPONSE responseId=6a7b8c9d10 promptFeedbackPresent=true
↑ Pode ser que o prompt gerou um feedback/crítica da API
```

---

### 6. **CACHE_SAVE** (Confirmação de persistência)
```
log.info("CACHE_SAVE id={} titulo='{}' curiosidadeNull={}",
    salvo.getId(), salvo.getTitulo(), salvo.getCuriosidade() == null);
```

**O que faz:** Confirma que o livro foi salvo no banco (com o ID gerado)

**Por que é importante:**
- Se este log mostrar `curiosidadeNull=true` mas GEMINI_OUT mostrou `textNull=false`, há problema de JPA/mapeamento
- `id` é único para auditar depois no banco

**Exemplo OK:**
```
CACHE_SAVE id=42 titulo='dracula' curiosidadeNull=false
↑ Livro foi salvo com curiosidade válida
```

**Exemplo de PROBLEMA RARO:**
```
CACHE_SAVE id=43 titulo='dune' curiosidadeNull=true
↑ Se GEMINI_OUT no mesmo trace não mostrou textNull=true, há bug de mapeamento
```

---

### 7. **CURIOSIDADE_REQ** (Controller - entrada)
```
log.info("CURIOSIDADE_REQ titulo='{}'", titulo);
```

**O que faz:** Marca que a requisição HTTP chegou no controller

**Por que é importante:**
- Você vê exatamente o que veio da query string
- Diferencia problemas de rede vs código

---

### 8. **CURIOSIDADE_RES** (Controller - saída)
```
log.info("CURIOSIDADE_RES titulo='{}' curiosidadeNull={} tamanho={}",
    titulo, resultado == null, resultado == null ? 0 : resultado.length());
```

**O que faz:** Confirma o que vai ser serializado para JSON

**Por que é importante:**
- Se `curiosidadeNull=true`, confirma que o problema está no Service (não no Jackson)
- `tamanho` mostra quantos caracteres tem a resposta (debug extra)

**Exemplo:**
```
CURIOSIDADE_RES titulo='dracula' curiosidadeNull=true tamanho=0
↑ JSON vai sair com curiosidade=null (problema está no Service ou Gemini)
```

---

## Fluxo Completo de Diagnóstico

Se o json final estiver com `"curiosidade": null`, siga este passo a passo:

### Passo 1: Procure por `CURIOSIDADE_RES` no log
```
✓ curiosidadeNull=false  → Problema está em Jackson/serialização (improvável)
✗ curiosidadeNull=true   → Problema está no Service, continue...
```

### Passo 2: Se for true, procure por `CACHE_HIT` no MESMO trace
```
✓ CACHE_HIT aparece     → O livro existe no banco, mas curiosidade está vazia!
                          SOLUÇÃO: Execute uma consulta Gemini forçada ou delete o livro antigo do DB
                          
✗ CACHE_HIT não aparece → Vai usar Gemini, continue...
```

### Passo 3: Procure por `GEMINI_OUT` no mesmo trace
```
✓ textNull=false        → Resposta chegou ok, mas algo perdeu no caminho (raro, verificar CACHE_SAVE)

✗ textNull=true         → Gemini retornou null, procure por:
                          - finishReason=SAFETY   → API bloqueou, mude o prompt
                          - finishReason=MAX_TOKENS → Prompt muito longo
                          - sem finishReason      → Erro de conexão/API, tente novamente
```

### Passo 4: Se CACHE_SAVE aparecer com `curiosidadeNull=true`
```
Mas GEMINI_OUT mostrou textNull=false
→ BUG DE MAPEAMENTO JPA: Verifidar coluna 'curiosidade' na tabela, constraints, etc
```

---

## Como Ativar os Logs

Os logs estão em nível `INFO` e `WARN`, portanto já devem aparecer se você não desativou logging.

Se não aparecer, adicione ao `application.properties`:
```properties
logging.level.br.com.literalura.literalura.service.ConsultaGemini=DEBUG
logging.level.br.com.literalura.literalura.controller.LivroController=DEBUG
```

Para ver TUDO:
```properties
logging.level.root=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

---

## Exemplo de Trace Completo (Caso de Sucesso)

```
14:23:45.123 CURIOSIDADE_REQ titulo='Dracula'
14:23:45.124 GEMINI_IN tituloOriginal='Dracula' tituloNormalizado='dracula'
14:23:45.125 CACHE_MISS titulo='dracula'
14:23:47.342 GEMINI_OUT textNull=false textBlank=false finishReason=STOP partsCount=1 candidatesCount=1
14:23:47.456 CACHE_SAVE id=1 titulo='dracula' curiosidadeNull=false
14:23:47.457 CURIOSIDADE_RES titulo='Dracula' curiosidadeNull=false tamanho=147
```
✓ JSON sai com curiosidade preenchida

---

## Exemplo de Trace Completo (Caso de Problema)

```
14:24:10.234 CURIOSIDADE_REQ titulo='Dune'
14:24:10.235 GEMINI_IN tituloOriginal='Dune' tituloNormalizado='dune'
14:24:10.236 CACHE_HIT id=5 titulo='dune' curiosidadeNull=true curiosidadeBlank=false
14:24:10.237 CURIOSIDADE_RES titulo='Dune' curiosidadeNull=true tamanho=0
```
✗ JSON sai com `"curiosidade": null`

**Diagnóstico:** Um livro antigo (id=5) foi salvo sem curiosidade. Soluções:
1. Delete o registro: `DELETE FROM livros WHERE id=5;`
2. Ou force Gemini novamente mantendo a nova lógica que já verifica isBlank()
